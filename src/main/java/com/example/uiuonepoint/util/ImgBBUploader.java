package com.example.uiuonepoint.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Base64;

public class ImgBBUploader {
    private static final String IMGBB_API_KEY = "19a9ead07abe8b31be0c5e74d52f4b27";
    private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";

    public static String uploadImage(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            throw new IOException("Image file does not exist");
        }

        try {
            // Read image file and convert to base64
            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(fileContent);

            // Create URL and connection
            URL url = new URL(IMGBB_UPLOAD_URL + "?key=" + IMGBB_API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = ("image=" + URLEncoder.encode(base64Image, "UTF-8")).getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            // Get response
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse response
            String responseString = response.toString();
            int urlStart = responseString.indexOf("\"url\":\"") + 7;
            int urlEnd = responseString.indexOf("\"", urlStart);
            
            if (urlStart > 6 && urlEnd > urlStart) {
                return responseString.substring(urlStart, urlEnd);
            } else {
                throw new IOException("Failed to parse response: " + responseString);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }
} 