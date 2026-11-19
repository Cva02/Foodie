package com.foodie.admin.controller;

import com.foodie.admin.dto.SellerApplicationView;
import com.foodie.admin.dto.ApplicationReviewRequest;
import com.foodie.admin.service.SellerApplicationService;
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
@RequestMapping("/api/seller-applications")
public class SellerApplicationController {

    private final SellerApplicationService applications;
    private final UploadService uploads;

    public SellerApplicationController(SellerApplicationService applications, UploadService uploads) {
        this.applications = applications;
        this.uploads = uploads;
    }

    @GetMapping
    public List<SellerApplicationView> all() {
        return applications.all();
    }

    @PatchMapping("/{id}/approve")
    public SellerApplicationView approve(@PathVariable String id) {
        return applications.approve(id);
    }

    @PatchMapping("/{id}/reject")
    public SellerApplicationView reject(@PathVariable String id,
                                        @Valid @RequestBody ApplicationReviewRequest body) {
        return applications.reject(id, body.note());
    }

    @GetMapping("/{id}/fssai-document")
    public ResponseEntity<Resource> fssaiDocument(@PathVariable String id) {
        String fileName = applications.fssaiDocumentFileName(id);
        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(uploads.sellerFssaiDocument(fileName));
    }
}
