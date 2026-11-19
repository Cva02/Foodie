package com.foodie.admin.controller;

import com.foodie.admin.dto.RiderApplicationView;
import com.foodie.admin.dto.ApplicationReviewRequest;
import com.foodie.admin.service.RiderApplicationService;
import com.foodie.admin.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rider-applications")
public class RiderApplicationController {

    private final RiderApplicationService applications;
    private final UploadService uploads;

    public RiderApplicationController(RiderApplicationService applications, UploadService uploads) {
        this.applications = applications;
        this.uploads = uploads;
    }

    @GetMapping
    public List<RiderApplicationView> all() {
        return applications.all();
    }

    @PatchMapping("/{id}/approve")
    public RiderApplicationView approve(@PathVariable String id) {
        return applications.approve(id);
    }

    @PatchMapping("/{id}/reject")
    public RiderApplicationView reject(@PathVariable String id,
                                       @Valid @RequestBody ApplicationReviewRequest body) {
        return applications.reject(id, body.note());
    }

    @GetMapping("/{id}/license-document")
    public ResponseEntity<Resource> licenseDocument(@PathVariable String id) {
        String fileName = applications.licenseDocumentFileName(id);
        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(uploads.riderLicense(fileName));
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> photo(@PathVariable String id) {
        String fileName = applications.photoFileName(id);
        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(uploads.riderPhoto(fileName));
    }
}
