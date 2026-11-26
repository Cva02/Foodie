package com.foodie.admin.controller;

import com.foodie.admin.service.UploadService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/public/images")
public class PublicImageController {

    private final UploadService uploads;

    public PublicImageController(UploadService uploads) {
        this.uploads = uploads;
    }

    @GetMapping("/restaurants/{fileName}")
    public ResponseEntity<Resource> restaurantImage(@PathVariable String fileName) {
        return image(uploads.restaurantImage(fileName), fileName);
    }

    @GetMapping("/riders/{fileName}")
    public ResponseEntity<Resource> riderPhoto(@PathVariable String fileName) {
        return image(uploads.riderPhoto(fileName), fileName);
    }

    @GetMapping("/menu/{fileName}")
public ResponseEntity<Resource> menuImage(@PathVariable String fileName) {
    return image(uploads.menuImage(fileName), fileName);
}

    private ResponseEntity<Resource> image(Resource resource, String fileName) {
        return ResponseEntity.ok()
                .contentType(uploads.mediaType(fileName))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString())
                .body(resource);
    }
}
