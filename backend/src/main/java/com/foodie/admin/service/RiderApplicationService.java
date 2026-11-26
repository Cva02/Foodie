
package com.foodie.admin.service;

import com.foodie.admin.dto.RiderApplicationView;
import com.foodie.admin.dto.RiderRegisterRequest;
import com.foodie.admin.dto.RiderRequest;
import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderApplication;
import com.foodie.admin.model.RiderApplicationPrivateDetails;
import com.foodie.admin.model.RiderPrivateDetails;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderApplicationPrivateDetailsRepository;
import com.foodie.admin.repository.RiderApplicationRepository;
import com.foodie.admin.repository.RiderPrivateDetailsRepository;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.repository.SellerApplicationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class RiderApplicationService {

    private final RiderApplicationRepository applications;
    private final SellerApplicationRepository sellerApplications;
    private final RiderRepository riders;
    private final PortalAccountRepository accounts;
    private final RiderApplicationPrivateDetailsRepository applicationPrivateDetails;
    private final RiderPrivateDetailsRepository riderPrivateDetails;
    private final PasswordEncoder passwordEncoder;
    private final AdminService admin;
    private final UploadService uploads;

    public RiderApplicationService(
            RiderApplicationRepository applications,
            SellerApplicationRepository sellerApplications,
            RiderRepository riders,
            PortalAccountRepository accounts,
            RiderApplicationPrivateDetailsRepository applicationPrivateDetails,
            RiderPrivateDetailsRepository riderPrivateDetails,
            PasswordEncoder passwordEncoder,
            AdminService admin,
            UploadService uploads) {

        this.applications = applications;
        this.sellerApplications = sellerApplications;
        this.riders = riders;
        this.accounts = accounts;
        this.applicationPrivateDetails = applicationPrivateDetails;
        this.riderPrivateDetails = riderPrivateDetails;
        this.passwordEncoder = passwordEncoder;
        this.admin = admin;
        this.uploads = uploads;
    }

    @Transactional(readOnly = true)
    public List<RiderApplicationView> all() {
        return applications.findAllByOrderBySubmittedAtDesc()
                .stream().map(this::view).toList();
    }

    @Transactional
    public synchronized RiderApplicationView register(
            RiderRegisterRequest request,
            MultipartFile vehiclePhoto,
            MultipartFile licenseDocument) {

        String email = normalizeEmail(request.email());

        if (accounts.findByEmailIgnoreCase(email).isPresent()
                || riders.existsByEmailIgnoreCase(email)
                || sellerApplications.findByEmailIgnoreCase(email)
                .filter(other -> !"REJECTED".equals(other.getStatus()))
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered or has an active application");
        }

        RiderApplication existing = applications.findByEmailIgnoreCase(email)
                .orElse(null);

        if (existing != null && !"REJECTED".equals(existing.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A rider application with this email already exists");
        }

        String photoFileName = uploads.saveRiderPhoto(vehiclePhoto);
        String licenseFileName = uploads.saveRiderLicense(licenseDocument);
        String passwordHash = passwordEncoder.encode(request.password());

        RiderApplication application;

        if (existing != null) {
            application = existing;

            application.resubmit(
                    passwordHash,
                    request.name().trim(),
                    request.phone().trim(),
                    request.vehicle(),
                    request.vehicleNumber().trim(),
                    request.licenseNumber().trim(),
                    request.city().trim(),
                    request.servicePinCodes().trim(),
                    photoFileName,
                    licenseFileName
            );
        } else {
            application = new RiderApplication(
                    UUID.randomUUID().toString(),
                    email,
                    passwordHash,
                    request.name().trim(),
                    request.phone().trim(),
                    request.vehicle(),
                    request.vehicleNumber().trim(),
                    request.licenseNumber().trim(),
                    request.city().trim(),
                    request.servicePinCodes().trim(),
                    photoFileName,
                    licenseFileName
            );
        }

        application = applications.save(application);

        applicationPrivateDetails.save(
                new RiderApplicationPrivateDetails(
                        application.getId(),
                        request.bankName().trim(),
                        request.accountHolderName().trim(),
                        request.bankAccountNumber().trim(),
                        request.bankIfsc().trim().toUpperCase(Locale.ROOT)
                )
        );

        return view(application);
    }

    @Transactional(readOnly = true)
    public String licenseDocumentFileName(String id) {
        RiderApplication application = findApplication(id);

        if (application.getLicenseFileName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Driving licence file not found");
        }

        return application.getLicenseFileName();
    }

    @Transactional(readOnly = true)
    public String photoFileName(String id) {
        RiderApplication application = findApplication(id);

        if (application.getPhotoFileName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Rider photo file not found");
        }

        return application.getPhotoFileName();
    }

    @Transactional
    public synchronized RiderApplicationView approve(String id) {

        RiderApplication application = findApplication(id);
        requirePending(application);

        if (application.getPasswordHash() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Application has no pending credentials");
        }

        if (accounts.findByEmailIgnoreCase(application.getEmail()).isPresent()
                || sellerApplications.findByEmailIgnoreCase(application.getEmail())
                .filter(other -> !"REJECTED".equals(other.getStatus()))
                .isPresent()
                || riders.existsByEmailIgnoreCase(application.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The application email is already assigned to an account or rider profile");
        }

        RiderApplicationPrivateDetails bankDetails =
                applicationPrivateDetails.findById(application.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Pending rider bank details were not found"));

        Rider rider = admin.createRider(new RiderRequest(
                application.getName(),
                uploads.riderPhotoUrl(application.getPhotoFileName()),
                application.getPhone(),
                application.getEmail(),
                application.getVehicle(),
                application.getVehicleNumber(),
                application.getLicenseNumber(),
                application.getCity(),
                "OFFLINE",
                null
        ));

        rider.setServicePinCodes(application.getServicePinCodes());
        riders.save(rider);

        riderPrivateDetails.save(new RiderPrivateDetails(
                rider.getId(),
                bankDetails.getBankName(),
                bankDetails.getAccountHolderName(),
                bankDetails.getBankAccountNumber(),
                bankDetails.getBankIfsc()
        ));

        accounts.save(new PortalAccount(
                application.getEmail(),
                application.getPasswordHash(),
                "RIDER",
                application.getName(),
                rider.getId(),
                application.getPhone()
        ));

        applicationPrivateDetails.deleteById(application.getId());

        application.approve(rider.getId());

        return view(applications.save(application));
    }

    @Transactional
    public synchronized RiderApplicationView reject(String id, String note) {

        RiderApplication application = findApplication(id);
        requirePending(application);

        application.reject(note.trim());

        applicationPrivateDetails.deleteById(application.getId());

        return view(applications.save(application));
    }

    private RiderApplication findApplication(String id) {
        return applications.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rider application not found"));
    }

    private static void requirePending(RiderApplication application) {
        if (!"PENDING".equals(application.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Application has already been reviewed");
        }
    }

    private RiderApplicationView view(RiderApplication application) {
        return new RiderApplicationView(
                application.getId(),
                application.getName(),
                application.getPhone(),
                application.getEmail(),
                application.getVehicle(),
                application.getVehicleNumber(),
                application.getLicenseNumber(),
                application.getCity(),
                uploads.riderPhotoUrl(application.getPhotoFileName()),
                application.getLicenseFileName() != null,
                application.getStatus(),
                application.getReviewNote(),
                application.getRiderId(),
                application.getSubmittedAt()
        );
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
