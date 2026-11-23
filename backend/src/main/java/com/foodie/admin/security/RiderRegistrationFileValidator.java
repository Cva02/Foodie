package com.foodie.admin.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@Component
public class RiderRegistrationFileValidator {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> VEHICLE_PHOTO_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> LICENSE_DOCUMENT_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/png");

    public Upload validateVehiclePhoto(MultipartFile file) {
        return validate(file, VEHICLE_PHOTO_TYPES, "Vehicle photo");
    }

    public Upload validateLicenseDocument(MultipartFile file) {
        return validate(file, LICENSE_DOCUMENT_TYPES, "Driving licence");
    }

    private Upload validate(MultipartFile file, Set<String> allowedTypes, String label) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " must be 5 MB or smaller");
        }

        try {
            byte[] content = file.getBytes();
            String detectedType = detectType(content);
            if (detectedType == null || !allowedTypes.contains(detectedType)
                    || !detectedType.equalsIgnoreCase(file.getContentType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " has an unsupported file type");
            }
            return new Upload(content, detectedType);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read " + label.toLowerCase(), exception);
        }
    }

    private static String detectType(byte[] content) {
        if (content.length >= 3 && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8 && (content[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (content.length >= 8 && (content[0] & 0xFF) == 0x89 && content[1] == 'P'
                && content[2] == 'N' && content[3] == 'G' && (content[4] & 0xFF) == 0x0D
                && (content[5] & 0xFF) == 0x0A && (content[6] & 0xFF) == 0x1A
                && (content[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        if (content.length >= 12 && content[0] == 'R' && content[1] == 'I'
                && content[2] == 'F' && content[3] == 'F' && content[8] == 'W'
                && content[9] == 'E' && content[10] == 'B' && content[11] == 'P') {
            return "image/webp";
        }
        if (content.length >= 5 && content[0] == '%' && content[1] == 'P'
                && content[2] == 'D' && content[3] == 'F' && content[4] == '-') {
            return "application/pdf";
        }
        return null;
    }

    public record Upload(byte[] content, String contentType) {
    }
}
