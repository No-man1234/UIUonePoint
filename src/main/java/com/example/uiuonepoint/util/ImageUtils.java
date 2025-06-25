package com.example.uiuonepoint.util;

public class ImageUtils {
    /**
     * Formats an image URL from the database for use with JavaFX Image class.
     * Handles cases where the URL might start with '@' or contain escaped slashes.
     */
    public static String formatImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        // Remove '@' prefix if present
        if (imageUrl.startsWith("@")) {
            imageUrl = imageUrl.substring(1);
        }

        // Replace escaped slashes with regular slashes
        imageUrl = imageUrl.replace("\\/", "/");

        return imageUrl;
    }
} 