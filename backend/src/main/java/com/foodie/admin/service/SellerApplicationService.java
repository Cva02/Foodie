package com.foodie.admin.service;

import com.foodie.admin.dto.SellerApplicationView;
import com.foodie.admin.dto.SellerRequest;
import com.foodie.admin.model.PortalAccount;
import com.foodie.admin.model.Seller;
import com.foodie.admin.model.SellerApplication;
import com.foodie.admin.repository.PortalAccountRepository;
import com.foodie.admin.repository.RiderApplicationRepository;
import com.foodie.admin.repository.SellerApplicationRepository;
import com.foodie.admin.repository.SellerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SellerApplicationService {

    private final SellerApplicationRepository applications;
    private final RiderApplicationRepository riderApplications;
    private final SellerRepository sellers;
    private final PortalAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final AdminService admin;
    private final UploadService uploads;

    public SellerApplicationService(SellerApplicationRepository applications,
                                    RiderApplicationRepository riderApplications, SellerRepository sellers,
                                    PortalAccountRepository accounts, PasswordEncoder passwordEncoder,
                                    AdminService admin, UploadService uploads) {
        this.applications = applications;
        this.riderApplications = riderApplications;
        this.sellers = sellers;
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.admin = admin;
        this.uploads = uploads;
    }

    @Transactional(readOnly = true)
    public List<SellerApplicationView> all() {
        return applications.findAllByOrderBySubmittedAtDesc().stream().map(SellerApplicationService::view).toList();
    }

    @Transactional(readOnly = true)
    public String fssaiDocumentFileName(String id) {
        SellerApplication application = findApplication(id);
        if (application.getFssaiDocumentFileName() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "FSSAI certificate file not found");
        }
        return application.getFssaiDocumentFileName();
    }

    @Transactional
    public synchronized SellerApplicationView approve(String id) {
        SellerApplication application = findApplication(id);
        requirePending(application);
        if (application.getPasswordHash() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application has no pending credentials");
        }
        if (accounts.findByEmailIgnoreCase(application.getEmail()).isPresent()
                || riderApplications.findByEmailIgnoreCase(application.getEmail())
                .filter(other -> !"REJECTED".equals(other.getStatus())).isPresent()
                || sellers.existsByEmailIgnoreCase(application.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The application email is already assigned to an account or restaurant");
        }

        Seller seller = admin.createSeller(new SellerRequest(
                application.getRestaurantName(), application.getOwnerName(), null, application.getImage(),
                application.getCuisine(), application.getPhone(), application.getEmail(),
                application.getAddress(), application.getCity(), application.getFssaiNumber(), "ACTIVE", true, null));
        accounts.save(new PortalAccount(application.getEmail(), application.getPasswordHash(), "SELLER",
                application.getOwnerName(), seller.getId(), application.getPhone()));
        application.approve(seller.getId());
        return view(applications.save(application));
    }

    @Transactional
    public synchronized SellerApplicationView reject(String id, String note) {
        SellerApplication application = findApplication(id);
        requirePending(application);
        application.reject(note.trim());
        return view(applications.save(application));
    }

    private SellerApplication findApplication(String id) {
        return applications.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller application not found"));
    }

    private static void requirePending(SellerApplication application) {
        if (!"PENDING".equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application has already been reviewed");
        }
    }

    private static SellerApplicationView view(SellerApplication application) {
        return new SellerApplicationView(application.getId(), application.getRestaurantName(),
                application.getOwnerName(), application.getCuisine(), application.getPhone(),
                application.getEmail(), application.getAddress(), application.getCity(),
                application.getFssaiNumber(), application.getImage(),
                application.getFssaiDocumentFileName() != null, application.getStatus(),
                application.getReviewNote(), application.getSellerId(), application.getSubmittedAt());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
