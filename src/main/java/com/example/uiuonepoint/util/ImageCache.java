package com.example.uiuonepoint.util;

import javafx.scene.image.Image;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Utility class for caching and efficiently loading images.
 * Uses a thread pool for parallel loading and a concurrent hash map for caching.
 */
public class ImageCache {
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final ExecutorService imageLoadExecutor = Executors.newFixedThreadPool(4);
    private static final Image DEFAULT_FOOD_IMAGE;
    private static final Image DEFAULT_GROCERY_IMAGE;
    
    static {
        // Load default images once at startup
        Image defaultFood = null;
        Image defaultGrocery = null;
        try {
            defaultFood = new Image(ImageCache.class.getResourceAsStream("/com/example/uiuonepoint/images/default-food.png"));
            defaultGrocery = new Image(ImageCache.class.getResourceAsStream("/com/example/uiuonepoint/images/default-item.png"));
        } catch (Exception e) {
            System.err.println("Error loading default images: " + e.getMessage());
        }
        DEFAULT_FOOD_IMAGE = defaultFood;
        DEFAULT_GROCERY_IMAGE = defaultGrocery;
    }
    
    /**
     * Get an image from cache or load it asynchronously with default thumbnail size.
     * 
     * @param imageUrl The URL of the image to load
     * @param isFood Whether this is a food item image (for default fallback)
     * @param imageConsumer Consumer that will be called when the image is loaded
     */
    public static void getImage(String rawImageUrl, boolean isFood, Consumer<Image> imageConsumer) {
        // Use default thumbnail size
        getImage(rawImageUrl, isFood, 180, 120, imageConsumer);
    }
    
    /**
     * Get an image from cache or load it asynchronously with specified dimensions.
     * 
     * @param rawImageUrl The URL of the image to load
     * @param isFood Whether this is a food item image (for default fallback)
     * @param width Target width for the image view
     * @param height Target height for the image view
     * @param imageConsumer Consumer that will be called when the image is loaded
     */
    public static void getImage(String rawImageUrl, boolean isFood, double width, double height, Consumer<Image> imageConsumer) {
        // Format the image URL
        String imageUrl = ImageUtils.formatImageUrl(rawImageUrl);
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Return default image if URL is invalid
            imageConsumer.accept(isFood ? DEFAULT_FOOD_IMAGE : DEFAULT_GROCERY_IMAGE);
            return;
        }
        
        // Check if image is already in cache
        Image cachedImage = imageCache.get(imageUrl);
        if (cachedImage != null) {
            imageConsumer.accept(cachedImage);
            return;
        }
        
        // Load default image first
        imageConsumer.accept(isFood ? DEFAULT_FOOD_IMAGE : DEFAULT_GROCERY_IMAGE);
        
        // Load actual image in background at original size (no scaling)
        imageLoadExecutor.submit(() -> {
            try {
                // Background loading with background loading flag set to true
                // We don't specify dimensions so it loads at original resolution
                Image image = new Image(imageUrl, 0, 0, true, true, true);
                
                // Only cache and update image if loading was successful
                if (!image.isError()) {
                    imageCache.put(imageUrl, image);
                    javafx.application.Platform.runLater(() -> imageConsumer.accept(image));
                }
            } catch (Exception e) {
                System.err.println("Error loading image from " + imageUrl + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Clears the image cache to free up memory.
     */
    public static void clearCache() {
        imageCache.clear();
    }
    
    /**
     * Shuts down the image loading executor service.
     * Call this when your application is shutting down.
     */
    public static void shutdown() {
        imageLoadExecutor.shutdown();
    }
} 