package com.mycompany.apartmentsytem1;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileStorageUtil {

    // The root folder where all images will be stored
    private static final String UPLOAD_DIR = "uploads/";

    static {
        // Automatically create the directory if it doesn't exist
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Copies a file to the local uploads folder and returns the new unique filename.
     */
    public static String saveImage(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) return null;

        try {
            // Generate a unique filename using UUID (e.g., a1b2-c3d4-receipt.jpg)
            String extension = "";
            String name = sourceFile.getName();
            if (name.contains(".")) {
                extension = name.substring(name.lastIndexOf("."));
            }
            
            String uniqueName = UUID.randomUUID().toString() + extension;
            File destination = new File(UPLOAD_DIR + uniqueName);

            // Perform the copy
            Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            return uniqueName; // This is the string you save in the DB
        } catch (Exception e) {
            System.err.println("File Upload Error: " + e.getMessage());
            return null;
        }
    }

    public static String getUploadPath() {
        return UPLOAD_DIR;
    }
}