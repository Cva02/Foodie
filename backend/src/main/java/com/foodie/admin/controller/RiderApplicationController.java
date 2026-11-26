
package com.foodie.admin.controller;

import com.foodie.admin.dto.RiderApplicationView;
import com.foodie.admin.dto.RiderRegisterRequest;
import com.foodie.admin.dto.ApplicationReviewRequest;
import com.foodie.admin.service.RiderApplicationService;
import com.foodie.admin.service.UploadService;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rider-applications")
public class RiderApplicationController {

    private final RiderApplicationService applications;
    private final UploadService uploads;

    public RiderApplicationController(
            RiderApplicationService applications,
            UploadService uploads) {
        this.applications = applications;
        this.uploads = uploads;
    }

    // Public rider registration
    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public RiderApplicationView register(
            @Valid @RequestPart("rider") RiderRegisterRequest rider,
            @RequestPart("vehiclePhoto") MultipartFile vehiclePhoto,
            @RequestPart("licenseDocument") MultipartFile licenseDocument) {

        return applications.register(rider, vehiclePhoto, licenseDocument);
    }

    // Admin: view all applications
    @GetMapping
    public List<RiderApplicationView> all() {
        return applications.all();
    }

    // Admin: approve application
    @PatchMapping("/{id}/approve")
    public RiderApplicationView approve(@PathVariable String id) {
        return applications.approve(id);
    }

    // Admin: reject application
    @PatchMapping("/{id}/reject")
    public RiderApplicationView reject(
            @PathVariable String id,
            @Valid @RequestBody ApplicationReviewRequest body) {
        return applications.reject(id, body.note());
    }

    // Admin: download licence
    @GetMapping("/{id}/license-document")
    public ResponseEntity<Resource> licenseDocument(@PathVariable String id) {
        String fileName = applications.licenseDocumentFileName(id);

        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName).build().toString()
                )
                .body(uploads.riderLicense(fileName));
    }

    // Admin: download rider photo
    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> photo(@PathVariable String id) {
        String fileName = applications.photoFileName(id);

        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName).build().toString()
                )
                .body(uploads.riderPhoto(fileName));
    }
}
