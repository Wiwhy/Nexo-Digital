package com.nexodigital.service;

import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class S3Service {
    private static final String UPLOAD_DIR = "/data/uploads";

    static {
        // Crear directorio si no existe
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void uploadImage(String fileName, InputStream fileContent, long contentLength) throws Exception {
        try {
            String filePath = UPLOAD_DIR + File.separator + fileName;
            Files.copy(fileContent, Paths.get(filePath));
        } catch (Exception e) {
            throw new Exception("Error uploading image: " + e.getMessage(), e);
        }
    }

    public static String getImageUrl(String fileName) {
        return "/uploads/" + fileName;
    }

    public static void closeClient() {
        // No-op para volúmenes
    }
}
