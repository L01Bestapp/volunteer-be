package com.ctxh.volunteer.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageValidator {

    private static final long MAX_FILE_SIZE = 2 * 1024L * 1024; // 1MB
    private static final int MAX_WIDTH = 2048;
    private static final int MAX_HEIGHT = 2048;

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png"};

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return; // Nếu ảnh optional, không cần validate
        }

        // 1. Validate định dạng MIME type
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) {
            throw new IllegalArgumentException("Only JPG and PNG images are allowed");
        }

        // 2. Validate size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must be less than 2MB");
        }

//        // 3. Validate kích thước ảnh
//        try {
//            BufferedImage image = ImageIO.read(file.getInputStream());
//            if (image == null) {
//                throw new IllegalArgumentException("Invalid image file");
//            }
//
//            int width = image.getWidth();
//            int height = image.getHeight();
//
//            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
//                throw new IllegalArgumentException("Image dimensions must not exceed 1024x1024 pixels");
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Error reading image file", e);
//        }
    }
}
