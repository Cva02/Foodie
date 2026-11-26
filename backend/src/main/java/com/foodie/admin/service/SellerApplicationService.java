
package com.foodie.admin.service;

import com.foodie.admin.dto.SellerApplicationView;
import com.foodie.admin.dto.SellerRegistrationRequest;
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
import com.foodie.customer.model.Restaurant;
import com.foodie.customer.repo.Repos.RestaurantRepo;

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
    private final RestaurantRepo restaurants;

    public SellerApplicationService(
            SellerApplicationRepository applications,
            RiderApplicationRepository riderApplications,
            SellerRepository sellers,
            PortalAccountRepository accounts,
            PasswordEncoder passwordEncoder,
            AdminService admin,
	    RestaurantRepo restaurants) {

        this.applications = applications;
        this.riderApplications = riderApplications;
        this.sellers = sellers;
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.admin = admin;
	this.restaurants = restaurants;
    }

    // ------------------------------------------------------------
    // Seller registration
    // ------------------------------------------------------------

    @Transactional
    public synchronized SellerApplicationView register(
            SellerRegistrationRequest request) {

        String email = normalizeEmail(request.email());

        // Check whether this email already belongs to an account.
        if (accounts.findByEmailIgnoreCase(email).isPresent()
                || sellers.existsByEmailIgnoreCase(email)
                || riderApplications.findByEmailIgnoreCase(email)
                        .filter(other -> !"REJECTED".equals(other.getStatus()))
                        .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already registered"
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        SellerApplication existing =
                applications.findByEmailIgnoreCase(email).orElse(null);

        // Allow a rejected applicant to submit again.
        if (existing != null) {

            if (!"REJECTED".equals(existing.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "An application with this email already exists"
                );
            }

            existing.resubmit(
                    passwordHash,
                    request.restaurantName().trim(),
                    request.ownerName().trim(),
                    request.cuisine().trim(),
                    request.phone().trim(),
                    request.address().trim(),
                    request.city().trim(),
                    request.fssaiNumber().trim(),
                    
                    blankToNull(request.image()),
                    blankToNull(request.fssaiDocumentFileName()),
                    blankToNull(request.gstDocumentFileName()),
                    request.bankAccountNumber().trim(),
                    request.bankIfscCode().trim().toUpperCase(Locale.ROOT),
                    request.bankAccountHolderName().trim()
            );

            return view(applications.save(existing));
        }

        // Create a new pending application.
        SellerApplication application = new SellerApplication(
                UUID.randomUUID().toString(),
                email,
                passwordHash,
                request.restaurantName().trim(),
                request.ownerName().trim(),
                request.cuisine().trim(),
                request.phone().trim(),
                request.address().trim(),
                request.city().trim(),
                request.fssaiNumber().trim(),
                
                blankToNull(request.image()),
                blankToNull(request.fssaiDocumentFileName()),
                blankToNull(request.gstDocumentFileName()),
                request.bankAccountNumber().trim(),
                request.bankIfscCode().trim().toUpperCase(Locale.ROOT),
                request.bankAccountHolderName().trim()
        );

        return view(applications.save(application));
    }

    // ------------------------------------------------------------
    // Admin application list
    // ------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SellerApplicationView> all() {
        return applications.findAllByOrderBySubmittedAtDesc()
                .stream()
                .map(SellerApplicationService::view)
                .toList();
    }

    // ------------------------------------------------------------
    // Certificate documents
    // ------------------------------------------------------------

    @Transactional(readOnly = true)
    public String fssaiDocumentFileName(String id) {
        SellerApplication application = findApplication(id);

        if (application.getFssaiDocumentFileName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "FSSAI certificate file not found"
            );
        }

        return application.getFssaiDocumentFileName();
    }

    @Transactional(readOnly = true)
    public String gstDocumentFileName(String id) {
        SellerApplication application = findApplication(id);

        if (application.getGstDocumentFileName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "GST certificate file not found"
            );
        }

        return application.getGstDocumentFileName();
    }

    // ------------------------------------------------------------
    // Admin approves application
    // ------------------------------------------------------------

    @Transactional
    public synchronized SellerApplicationView approve(String id) {

        SellerApplication application = findApplication(id);
        requirePending(application);

        if (application.getPasswordHash() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Application has no pending credentials"
            );
        }

        if (accounts.findByEmailIgnoreCase(application.getEmail()).isPresent()
                || riderApplications.findByEmailIgnoreCase(application.getEmail())
                        .filter(other -> !"REJECTED".equals(other.getStatus()))
                        .isPresent()
                || sellers.existsByEmailIgnoreCase(application.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The application email is already assigned to an account or restaurant"
            );
        }

        Seller seller = admin.createSellerFromApplication(
                application.getRestaurantName(),
                application.getOwnerName(),
                application.getImage(),
                application.getCuisine(),
                application.getPhone(),
                application.getEmail(),
                application.getAddress(),
                application.getCity(),
                application.getFssaiNumber()
        );
	
	        // Create the restaurant record used by the Customer module.
        Restaurant restaurant = new Restaurant();
        restaurant.setName(application.getRestaurantName());
        restaurant.setCuisines(application.getCuisine());
        restaurant.setEmoji("🍽️");
        restaurant.setImageUrl(application.getImage());
        restaurant.setRating(0.0);
        restaurant.setDeliveryMinutes(35);
        restaurant.setCostForTwo(0);
        restaurant.setPureVeg(false);
        restaurant.setArea(
                application.getCity() != null && !application.getCity().isBlank()
                        ? application.getCity()
                        : application.getAddress()
        );
        restaurant.setOpen(true);

        Restaurant savedRestaurant = restaurants.save(restaurant);

        // Link the Admin seller to the Customer restaurant.
        seller.setRestaurantId(savedRestaurant.getId());
        sellers.save(seller);

        accounts.save(new PortalAccount(
                application.getEmail(),
                application.getPasswordHash(),
                "SELLER",
                application.getOwnerName(),
                seller.getId(),
                application.getPhone()
        ));

        application.approve(seller.getId());

        return view(applications.save(application));
    }

    // ------------------------------------------------------------
    // Admin rejects application
    // ------------------------------------------------------------

    @Transactional
    public synchronized SellerApplicationView reject(
            String id,
            String note) {

        SellerApplication application = findApplication(id);
        requirePending(application);

        application.reject(note.trim());

        return view(applications.save(application));
    }

    // ------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------

    private SellerApplication findApplication(String id) {
        return applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Seller application not found"
                ));
    }

    private static void requirePending(SellerApplication application) {
        if (!"PENDING".equals(application.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Application has already been reviewed"
            );
        }
    }

    private static SellerApplicationView view(
            SellerApplication application) {

        return new SellerApplicationView(
                application.getId(),
                application.getRestaurantName(),
                application.getOwnerName(),
                application.getCuisine(),
                application.getPhone(),
                application.getEmail(),
                application.getAddress(),
                application.getCity(),
                application.getFssaiNumber(),
                application.getImage(),
                application.getFssaiDocumentFileName() != null,
                application.getGstDocumentFileName() != null,
                application.getStatus(),
                application.getReviewNote(),
                application.getSellerId(),
                application.getSubmittedAt()
        );
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
