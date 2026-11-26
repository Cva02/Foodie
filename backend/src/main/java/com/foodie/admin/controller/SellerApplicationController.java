
package com.foodie.admin.controller;

import com.foodie.admin.dto.SellerApplicationView;
import com.foodie.admin.dto.SellerRegistrationRequest;
import com.foodie.admin.dto.ApplicationReviewRequest;
import com.foodie.admin.service.SellerApplicationService;
import com.foodie.admin.service.UploadService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller-applications")
public class SellerApplicationController {

    private final SellerApplicationService applications;
    private final UploadService uploads;

    public SellerApplicationController(
            SellerApplicationService applications,
            UploadService uploads) {

        this.applications = applications;
        this.uploads = uploads;
    }
@PostMapping("/upload-document")
public Map<String, String> uploadDocument(
        @RequestParam("type") String type,
        @RequestParam("file") MultipartFile file) {

    String fileName;

    if ("FSSAI".equalsIgnoreCase(type)) {
        fileName = uploads.saveSellerFssaiDocument(file);
    } else if ("GST".equalsIgnoreCase(type)) {
        fileName = uploads.saveSellerGstDocument(file);
    } else {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Document type must be FSSAI or GST"
        );
    }

    return Map.of("fileName", fileName);
}

    // Public seller registration
    @PostMapping("/register")
    public ResponseEntity<SellerApplicationView> register(
            @Valid @RequestBody SellerRegistrationRequest request) {

        return ResponseEntity.ok(applications.register(request));
    }

    // Admin: view all applications
    @GetMapping
    public List<SellerApplicationView> all() {
        return applications.all();
    }

    // Admin: approve application
    @PatchMapping("/{id}/approve")
    public SellerApplicationView approve(@PathVariable String id) {
        return applications.approve(id);
    }

    // Admin: reject application
    @PatchMapping("/{id}/reject")
    public SellerApplicationView reject(
            @PathVariable String id,
            @Valid @RequestBody ApplicationReviewRequest body) {

        return applications.reject(id, body.note());
    }

    // Admin: download FSSAI certificate
    @GetMapping("/{id}/fssai-document")
    public ResponseEntity<Resource> fssaiDocument(
            @PathVariable String id) {

        String fileName = applications.fssaiDocumentFileName(id);

        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName)
                                .build()
                                .toString()
                )
                .body(uploads.sellerFssaiDocument(fileName));
    }

    // Admin: download GST certificate
    @GetMapping("/{id}/gst-document")
    public ResponseEntity<Resource> gstDocument(
            @PathVariable String id) {

        String fileName = applications.gstDocumentFileName(id);

        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName)
                                .build()
                                .toString()
                )
                .body(uploads.sellerGstDocument(fileName));
    }
}
