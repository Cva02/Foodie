package com.foodie.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UploadService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Pattern STORED_FILE_NAME = Pattern.compile("[a-f0-9]{32}\\.(jpg|png|pdf)");

    private final Path uploadDirectory;

    public UploadService(@Value("${foodie.upload.directory:uploads}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String saveRestaurantImage(MultipartFile file) {
        return "/api/public/images/restaurants/" + save(file, "restaurants", false);
    }

    public String saveRiderPhoto(MultipartFile file) {
        return save(file, "rider-photos", false);
    }

    public String riderPhotoUrl(String fileName) {
        return fileName == null ? null : "/api/public/images/riders/" + fileName;
    }

    public String saveRiderLicense(MultipartFile file) {
        return save(file, "rider-licenses", true);
    }

    public String saveSellerFssaiDocument(MultipartFile file) {
        return save(file, "seller-documents", true);
    }

    public String saveSellerGstDocument(MultipartFile file) {
    return save(file, "seller-gst-documents", true);
}

    public Resource restaurantImage(String fileName) {
        return resource("restaurants", fileName);
    }

    public String saveMenuImage(MultipartFile file) {
    return "/api/public/images/menu/" + save(file, "menu-images", false);
}

public Resource menuImage(String fileName) {
    return resource("menu-images", fileName);
}

    public Resource riderPhoto(String fileName) {
        return resource("rider-photos", fileName);
    }

    public Resource riderLicense(String fileName) {
        return resource("rider-licenses", fileName);
    }

    public Resource sellerFssaiDocument(String fileName) {
        return resource("seller-documents", fileName);
    }

    public Resource sellerGstDocument(String fileName) {
    return resource("seller-gst-documents", fileName);
}

    public MediaType mediaType(String fileName) {
        if (fileName.endsWith(".jpg")) return MediaType.IMAGE_JPEG;
        if (fileName.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (fileName.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploaded file not found");
    }

    private String save(MultipartFile file, String directory, boolean allowPdf) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a file to upload");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Files must be 5 MB or smaller");
        }

        try {
            byte[] content = file.getBytes();
            String extension = validatedExtension(file.getContentType(), content, allowPdf);
            Path folder = uploadDirectory.resolve(directory).normalize();
            if (!folder.startsWith(uploadDirectory)) {
                throw new IllegalStateException("Invalid upload directory");
            }
            Files.createDirectories(folder);
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
            Files.write(folder.resolve(fileName), content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store uploaded file", ex);
        }
    }

    private Resource resource(String directory, String fileName) {
        if (fileName == null || !STORED_FILE_NAME.matcher(fileName.toLowerCase(Locale.ROOT)).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploaded file not found");
        }
        Path folder = uploadDirectory.resolve(directory).normalize();
        Path file = folder.resolve(fileName).normalize();
        if (!file.startsWith(folder) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploaded file not found");
        }
        try {
            return new UrlResource(file.toUri());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read uploaded file", ex);
        }
    }

    private static String validatedExtension(String contentType, byte[] bytes, boolean allowPdf) {
        if (MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)
                && bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)
                && bytes.length >= 8 && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
                && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return ".png";
        }
        if (allowPdf && MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)
                && bytes.length >= 5 && bytes[0] == 0x25 && bytes[1] == 0x50
                && bytes[2] == 0x44 && bytes[3] == 0x46 && bytes[4] == 0x2d) {
            return ".pdf";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                allowPdf ? "Upload a valid JPEG, PNG or PDF file" : "Upload a valid JPEG or PNG image");
    }
}
