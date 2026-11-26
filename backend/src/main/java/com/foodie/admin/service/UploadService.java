
package com.foodie.admin.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UploadService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Pattern STORED_FILE_NAME =
            Pattern.compile("[a-f0-9]{32}\\.(jpg|png|pdf)");

    private static final Pattern CLOUDINARY_FILE_NAME =
            Pattern.compile("cld_[a-f0-9]{32}\\.(jpg|png|pdf)");

    private final Path uploadDirectory;
    private final Cloudinary cloudinary;
    private final boolean cloudinaryEnabled;

    public UploadService(
            @Value("${foodie.upload.directory:uploads}") String uploadDirectory,
            ObjectProvider<Cloudinary> cloudinaryProvider,
            @Value("${foodie.storage.cloudinary.enabled:false}") boolean cloudinaryEnabled) {

        this.uploadDirectory =
                Path.of(uploadDirectory).toAbsolutePath().normalize();

        this.cloudinary = cloudinaryProvider.getIfAvailable();
        this.cloudinaryEnabled = cloudinaryEnabled;

        if (cloudinaryEnabled && cloudinary == null) {
            throw new IllegalStateException(
                    "Cloudinary is enabled but its configuration is unavailable"
            );
        }
    }

    // ---------------- PUBLIC IMAGE UPLOADS ----------------

    public String saveRestaurantImage(MultipartFile file) {
        return "/api/public/images/restaurants/"
                + save(file, "restaurants", false);
    }

    public String saveMenuImage(MultipartFile file) {
        return "/api/public/images/menu/"
                + save(file, "menu-images", false);
    }

    public String saveRiderPhoto(MultipartFile file) {
        return save(file, "rider-photos", false);
    }

    public String riderPhotoUrl(String fileName) {
        return fileName == null
                ? null
                : "/api/public/images/riders/" + fileName;
    }

    // ---------------- PRIVATE DOCUMENT UPLOADS ----------------

    public String saveRiderLicense(MultipartFile file) {
        return save(file, "rider-licenses", true);
    }

    public String saveSellerFssaiDocument(MultipartFile file) {
        return save(file, "seller-documents", true);
    }

    public String saveSellerGstDocument(MultipartFile file) {
        return save(file, "seller-gst-documents", true);
    }

    // ---------------- FILE RETRIEVAL ----------------

    public Resource restaurantImage(String fileName) {
        return resource("restaurants", fileName);
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
        if (!isValidFileName(fileName)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uploaded file not found"
            );
        }

        String extension = getExtension(fileName);

        return switch (extension) {
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uploaded file not found"
            );
        };
    }

    // ---------------- SAVE FILE ----------------

    private String save(
            MultipartFile file,
            String directory,
            boolean allowPdf) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a file to upload"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Files must be 5 MB or smaller"
            );
        }

        try {
            byte[] content = file.getBytes();

            String extension = validatedExtension(
                    file.getContentType(),
                    content,
                    allowPdf
            );

            if (cloudinaryEnabled) {
                return saveToCloudinary(content, directory, extension, allowPdf);
            }

            return saveLocally(content, directory, extension);

        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store uploaded file",
                    ex
            );
        }
    }

    private String saveLocally(
            byte[] content,
            String directory,
            String extension) throws IOException {

        Path folder = uploadDirectory.resolve(directory).normalize();

        if (!folder.startsWith(uploadDirectory)) {
            throw new IllegalStateException("Invalid upload directory");
        }

        Files.createDirectories(folder);

        String fileName =
                UUID.randomUUID().toString().replace("-", "") + extension;

        Files.write(
                folder.resolve(fileName),
                content,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );

        return fileName;
    }

    // ---------------- CLOUDINARY UPLOAD ----------------

    private String saveToCloudinary(
            byte[] content,
            String directory,
            String extension,
            boolean privateDocument) {

        String uuid = UUID.randomUUID().toString().replace("-", "");

        boolean rawFile = privateDocument;

        String publicId = "foodie/" + directory + "/" + uuid;

        // Cloudinary raw assets require the extension in their public ID.
        if (rawFile) {
            publicId += extension;
        }

        String resourceType = rawFile ? "raw" : "image";

        String deliveryType = rawFile ? "authenticated" : "upload";

        Map<String, Object> options = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", resourceType,
                "type", deliveryType,
                "overwrite", false,
                "unique_filename", false,
                "use_filename", false
        );

        try {
            cloudinary.uploader().upload(content, options);

            return "cld_" + uuid + extension;

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to upload file to cloud storage",
                    ex
            );
        }
    }

    // ---------------- RETRIEVE LOCAL OR CLOUDINARY FILE ----------------

    private Resource resource(String directory, String fileName) {

        if (!isValidFileName(fileName)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uploaded file not found"
            );
        }

        if (isCloudinaryFile(fileName)) {
            return cloudinaryResource(directory, fileName);
        }

        return localResource(directory, fileName);
    }

    private Resource localResource(String directory, String fileName) {

        Path folder = uploadDirectory.resolve(directory).normalize();
        Path file = folder.resolve(fileName).normalize();

        if (!folder.startsWith(uploadDirectory)
                || !file.startsWith(folder)
                || !Files.isRegularFile(file)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uploaded file not found"
            );
        }

        try {
            return new UrlResource(file.toUri());

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to read uploaded file",
                    ex
            );
        }
    }

    // ---------------- CLOUDINARY FILE RETRIEVAL ----------------

    private Resource cloudinaryResource(
            String directory,
            String fileName) {

        if (!cloudinaryEnabled || cloudinary == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloud storage is not configured"
            );
        }

        String uuid = fileName.substring(4, fileName.lastIndexOf('.'));
        String extension = getExtension(fileName);

        boolean privateDocument = isPrivateDirectory(directory);

        String publicId = "foodie/" + directory + "/" + uuid;

        try {
            String url;

            if (privateDocument) {

                // Raw private documents include their extension in public ID.
                publicId += "." + extension;

                long expiresAt = Instant.now().getEpochSecond() + 300;

                Map<String, Object> options = ObjectUtils.asMap(
                        "resource_type", "raw",
                        "type", "authenticated",
                        "expires_at", expiresAt
                );

                url = cloudinary.privateDownload(
                        publicId,
                        extension,
                        options
                );

            } else {

                url = cloudinary.url()
                        .resourceType("image")
                        .type("upload")
                        .secure(true)
                        .publicId(publicId)
                        .format(extension)
                        .generate();
            }

            return new UrlResource(url);

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to retrieve file from cloud storage",
                    ex
            );
        }
    }

    private boolean isPrivateDirectory(String directory) {
        return directory.equals("rider-licenses")
                || directory.equals("seller-documents")
                || directory.equals("seller-gst-documents");
    }

    // ---------------- VALIDATION ----------------

    private boolean isValidFileName(String fileName) {
        if (fileName == null) {
            return false;
        }

        String normalized = fileName.toLowerCase(Locale.ROOT);

        return STORED_FILE_NAME.matcher(normalized).matches()
                || CLOUDINARY_FILE_NAME.matcher(normalized).matches();
    }

    private boolean isCloudinaryFile(String fileName) {
        return fileName != null
                && CLOUDINARY_FILE_NAME.matcher(
                        fileName.toLowerCase(Locale.ROOT)
                ).matches();
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);
    }

    private static String validatedExtension(
            String contentType,
            byte[] bytes,
            boolean allowPdf) {

        if (MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)
                && bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {

            return ".jpg";
        }

        if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)
                && bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4e
                && bytes[3] == 0x47
                && bytes[4] == 0x0d
                && bytes[5] == 0x0a
                && bytes[6] == 0x1a
                && bytes[7] == 0x0a) {

            return ".png";
        }

        if (allowPdf
                && MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)
                && bytes.length >= 5
                && bytes[0] == 0x25
                && bytes[1] == 0x50
                && bytes[2] == 0x44
                && bytes[3] == 0x46
                && bytes[4] == 0x2d) {

            return ".pdf";
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                allowPdf
                        ? "Upload a valid JPEG, PNG or PDF file"
                        : "Upload a valid JPEG or PNG image"
        );
    }
}
