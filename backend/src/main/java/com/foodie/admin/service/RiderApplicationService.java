package com.foodie.admin.service;

import com.foodie.admin.dto.RiderApplicationView;
import com.foodie.admin.dto.RiderRequest;
import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderApplication;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderApplicationRepository;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.repository.SellerApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordEncoder passwordEncoder;
    private final AdminService admin;
    private final UploadService uploads;

    public RiderApplicationService(RiderApplicationRepository applications,
                                   SellerApplicationRepository sellerApplications, RiderRepository riders,
                                   PortalAccountRepository accounts, PasswordEncoder passwordEncoder,
                                   AdminService admin, UploadService uploads) {
        this.applications = applications;
        this.sellerApplications = sellerApplications;
        this.riders = riders;
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.admin = admin;
        this.uploads = uploads;
    }

    @Transactional(readOnly = true)
    public List<RiderApplicationView> all() {
        return applications.findAllByOrderBySubmittedAtDesc().stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public String licenseDocumentFileName(String id) {
        RiderApplication application = findApplication(id);
        if (application.getLicenseFileName() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driving licence file not found");
        }
        return application.getLicenseFileName();
    }

    @Transactional(readOnly = true)
    public String photoFileName(String id) {
        RiderApplication application = findApplication(id);
        if (application.getPhotoFileName() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rider photo file not found");
        }
        return application.getPhotoFileName();
    }

    @Transactional
    public synchronized RiderApplicationView approve(String id) {
        RiderApplication application = findApplication(id);
        requirePending(application);
        if (application.getPasswordHash() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application has no pending credentials");
        }
        if (accounts.findByEmailIgnoreCase(application.getEmail()).isPresent()
                || sellerApplications.findByEmailIgnoreCase(application.getEmail())
                .filter(other -> !"REJECTED".equals(other.getStatus())).isPresent()
                || riders.existsByEmailIgnoreCase(application.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The application email is already assigned to an account or rider profile");
        }

        Rider rider = admin.createRider(new RiderRequest(
                application.getName(), uploads.riderPhotoUrl(application.getPhotoFileName()),
                application.getPhone(), application.getEmail(),
                application.getVehicle(), application.getVehicleNumber(), application.getLicenseNumber(),
                application.getCity(), "OFFLINE", null));
        accounts.save(new PortalAccount(application.getEmail(), application.getPasswordHash(), "RIDER",
                application.getName(), rider.getId(), application.getPhone()));
        application.approve(rider.getId());
        return view(applications.save(application));
    }

    @Transactional
    public synchronized RiderApplicationView reject(String id, String note) {
        RiderApplication application = findApplication(id);
        requirePending(application);
        application.reject(note.trim());
        return view(applications.save(application));
    }

    private RiderApplication findApplication(String id) {
        return applications.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Rider application not found"));
    }

    private static void requirePending(RiderApplication application) {
        if (!"PENDING".equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application has already been reviewed");
        }
    }

    private RiderApplicationView view(RiderApplication application) {
        return new RiderApplicationView(application.getId(), application.getName(), application.getPhone(),
                application.getEmail(), application.getVehicle(), application.getVehicleNumber(),
                application.getLicenseNumber(), application.getCity(),
                uploads.riderPhotoUrl(application.getPhotoFileName()),
                application.getLicenseFileName() != null, application.getStatus(),
                application.getReviewNote(), application.getRiderId(), application.getSubmittedAt());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
