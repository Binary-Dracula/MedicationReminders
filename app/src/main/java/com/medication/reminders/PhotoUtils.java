package com.medication.reminders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Utility class for handling photo operations including saving, loading, and deleting photos
 * for medication management. Photos are stored in the app's internal storage directory.
 */
public class PhotoUtils {
    private static final String TAG = "PhotoUtils";
    private static final String PHOTO_DIRECTORY = "medication_photos";
    private static final String PHOTO_PREFIX = "medication_photo_";
    private static final String PHOTO_EXTENSION = ".jpg";
    private static final int PHOTO_QUALITY = 85; // JPEG compression quality (0-100)

    /**
     * Saves a bitmap photo to internal storage with a unique filename
     * 
     * @param context Application context
     * @param bitmap The bitmap to save
     * @param filename Optional custom filename (if null, generates unique name)
     * @return The absolute path of the saved photo, or null if save failed
     */
    public static String savePhotoToInternalStorage(Context context, Bitmap bitmap, String filename) {
        if (context == null || bitmap == null) {
            Log.e(TAG, "Context or bitmap is null");
            return null;
        }

        try {
            // Create photos directory if it doesn't exist
            File photosDir = new File(context.getFilesDir(), PHOTO_DIRECTORY);
            if (!photosDir.exists() && !photosDir.mkdirs()) {
                Log.e(TAG, "Failed to create photos directory");
                return null;
            }

            // Generate filename if not provided
            if (filename == null || filename.trim().isEmpty()) {
                filename = generateUniqueFilename();
            }

            // Ensure filename has correct extension
            if (!filename.toLowerCase().endsWith(PHOTO_EXTENSION)) {
                filename += PHOTO_EXTENSION;
            }

            File photoFile = new File(photosDir, filename);
            
            // Save bitmap to file
            try (FileOutputStream fos = new FileOutputStream(photoFile)) {
                boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, fos);
                if (success) {
                    Log.d(TAG, "Photo saved successfully: " + photoFile.getAbsolutePath());
                    return photoFile.getAbsolutePath();
                } else {
                    Log.e(TAG, "Failed to compress bitmap");
                    return null;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving photo", e);
            return null;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while saving photo", e);
            return null;
        }
    }

    /**
     * Loads a bitmap from the given file path
     * 
     * @param path The absolute path to the photo file
     * @return The loaded bitmap, or null if loading failed
     */
    public static Bitmap loadPhotoFromPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            Log.e(TAG, "Photo path is null or empty");
            return null;
        }

        File photoFile = new File(path);
        if (!photoFile.exists()) {
            Log.e(TAG, "Photo file does not exist: " + path);
            return null;
        }

        if (!photoFile.canRead()) {
            Log.e(TAG, "Cannot read photo file: " + path);
            return null;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from path: " + path);
            }
            return bitmap;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory while loading photo: " + path, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error loading photo from path: " + path, e);
            return null;
        }
    }

    /**
     * Generates a unique filename for medication photos
     * Format: medication_photo_[timestamp]_[uuid].jpg
     * 
     * @return A unique filename string
     */
    public static String generateUniqueFilename() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8); // Use first 8 chars of UUID
        return PHOTO_PREFIX + timestamp + "_" + uuid + PHOTO_EXTENSION;
    }

    /**
     * Deletes a photo file from the given path
     * 
     * @param path The absolute path to the photo file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    public static boolean deletePhoto(String path) {
        if (path == null || path.trim().isEmpty()) {
            Log.e(TAG, "Photo path is null or empty");
            return false;
        }

        File photoFile = new File(path);
        if (!photoFile.exists()) {
            Log.w(TAG, "Photo file does not exist, considering as deleted: " + path);
            return true; // File doesn't exist, so it's effectively "deleted"
        }

        try {
            boolean deleted = photoFile.delete();
            if (deleted) {
                Log.d(TAG, "Photo deleted successfully: " + path);
            } else {
                Log.e(TAG, "Failed to delete photo: " + path);
            }
            return deleted;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while deleting photo: " + path, e);
            return false;
        }
    }

    /**
     * Checks if a photo file exists at the given path
     * 
     * @param path The absolute path to check
     * @return true if the file exists and is readable, false otherwise
     */
    public static boolean photoExists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        File photoFile = new File(path);
        return photoFile.exists() && photoFile.canRead();
    }

    /**
     * Gets the size of a photo file in bytes
     * 
     * @param path The absolute path to the photo file
     * @return The file size in bytes, or -1 if the file doesn't exist or can't be read
     */
    public static long getPhotoSize(String path) {
        if (path == null || path.trim().isEmpty()) {
            return -1;
        }

        File photoFile = new File(path);
        if (!photoFile.exists() || !photoFile.canRead()) {
            return -1;
        }

        return photoFile.length();
    }

    /**
     * Creates a scaled down version of a bitmap to reduce memory usage
     * 
     * @param originalBitmap The original bitmap to scale
     * @param maxWidth Maximum width for the scaled bitmap
     * @param maxHeight Maximum height for the scaled bitmap
     * @return A scaled bitmap, or the original if scaling is not needed
     */
    public static Bitmap scalePhoto(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        if (originalBitmap == null) {
            return null;
        }

        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // Check if scaling is needed
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalBitmap;
        }

        // Calculate scaling factor
        float scaleWidth = (float) maxWidth / originalWidth;
        float scaleHeight = (float) maxHeight / originalHeight;
        float scaleFactor = Math.min(scaleWidth, scaleHeight);

        // Calculate new dimensions
        int newWidth = Math.round(originalWidth * scaleFactor);
        int newHeight = Math.round(originalHeight * scaleFactor);

        try {
            return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory while scaling photo", e);
            return originalBitmap; // Return original if scaling fails
        }
    }
}