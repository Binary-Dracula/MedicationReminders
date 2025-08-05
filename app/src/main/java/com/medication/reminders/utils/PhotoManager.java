package com.medication.reminders.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * PhotoManager class handles profile photo operations including saving, loading, 
 * deleting, and resizing profile photos. Photos are stored in the app's internal 
 * storage directory with specific naming conventions for user profiles.
 */
public class PhotoManager {
    private static final String TAG = "PhotoManager";
    private static final String PROFILE_PHOTO_DIRECTORY = "profile_photos";
    private static final String PROFILE_PHOTO_PREFIX = "profile_";
    private static final String PHOTO_EXTENSION = ".jpg";
    private static final int PHOTO_QUALITY = 85; // JPEG compression quality (0-100)
    private static final int MAX_PHOTO_SIZE = 512; // Maximum photo dimensions in pixels
    
    /**
     * Interface for photo operation callbacks
     */
    public interface PhotoCallback {
        /**
         * Called when photo operation is successful
         * @param photoPath The path to the saved photo
         */
        void onSuccess(String photoPath);
        
        /**
         * Called when photo operation fails
         * @param error The error message
         */
        void onError(String error);
    }
    
    /**
     * Interface for permission request callbacks
     */
    public interface PermissionCallback {
        /**
         * Called when permission is granted
         */
        void onPermissionGranted();
        
        /**
         * Called when permission is denied
         */
        void onPermissionDenied();
    }

    /**
     * Saves a profile photo from URI to internal storage with username-specific naming
     * 
     * @param context Application context
     * @param username The username for the profile photo
     * @param photoUri The URI of the photo to save
     * @param callback Callback for operation result
     */
    public static void saveProfilePhoto(Context context, String username, Uri photoUri, PhotoCallback callback) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            if (callback != null) {
                callback.onError("系统错误：上下文为空");
            }
            return;
        }
        
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Username is null or empty");
            if (callback != null) {
                callback.onError("用户名不能为空");
            }
            return;
        }
        
        if (photoUri == null) {
            Log.e(TAG, "Photo URI is null");
            if (callback != null) {
                callback.onError("照片URI为空");
            }
            return;
        }
        
        try {
            // Load bitmap from URI
            Bitmap bitmap = loadBitmapFromUri(context, photoUri);
            if (bitmap == null) {
                Log.e(TAG, "Failed to load bitmap from URI");
                if (callback != null) {
                    callback.onError("无法加载照片");
                }
                return;
            }
            
            // Resize bitmap to optimize storage
            Bitmap resizedBitmap = resizePhoto(bitmap, MAX_PHOTO_SIZE);
            if (resizedBitmap == null) {
                Log.e(TAG, "Failed to resize bitmap");
                if (callback != null) {
                    callback.onError("照片处理失败");
                }
                return;
            }
            
            // Save resized bitmap
            String photoPath = saveProfilePhotoInternal(context, username, resizedBitmap);
            if (photoPath != null) {
                Log.d(TAG, "Profile photo saved successfully: " + photoPath);
                if (callback != null) {
                    callback.onSuccess(photoPath);
                }
            } else {
                Log.e(TAG, "Failed to save profile photo");
                if (callback != null) {
                    callback.onError("照片保存失败");
                }
            }
            
            // Clean up bitmaps
            if (bitmap != resizedBitmap) {
                bitmap.recycle();
            }
            resizedBitmap.recycle();
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving profile photo", e);
            if (callback != null) {
                callback.onError("照片保存失败：" + e.getMessage());
            }
        }
    }

    /**
     * Gets the profile photo path for a specific username
     * 
     * @param context Application context
     * @param username The username to get photo path for
     * @return The absolute path to the profile photo, or null if not found
     */
    public static String getProfilePhotoPath(Context context, String username) {
        if (context == null || username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Context or username is null/empty");
            return null;
        }
        
        File profilePhotosDir = new File(context.getFilesDir(), PROFILE_PHOTO_DIRECTORY);
        if (!profilePhotosDir.exists()) {
            return null;
        }
        
        // Look for existing profile photo for this username
        File[] files = profilePhotosDir.listFiles();
        if (files != null) {
            String prefix = PROFILE_PHOTO_PREFIX + username + "_";
            for (File file : files) {
                if (file.getName().startsWith(prefix) && file.getName().endsWith(PHOTO_EXTENSION)) {
                    return file.getAbsolutePath();
                }
            }
        }
        
        return null;
    }

    /**
     * Loads a profile photo bitmap from the given path
     * 
     * @param photoPath The absolute path to the profile photo
     * @return The loaded bitmap, or null if loading failed
     */
    public static Bitmap loadProfilePhoto(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            Log.e(TAG, "Photo path is null or empty");
            return null;
        }
        
        File photoFile = new File(photoPath);
        if (!photoFile.exists()) {
            Log.e(TAG, "Profile photo file does not exist: " + photoPath);
            return null;
        }
        
        if (!photoFile.canRead()) {
            Log.e(TAG, "Cannot read profile photo file: " + photoPath);
            return null;
        }
        
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode profile photo from path: " + photoPath);
            }
            return bitmap;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory while loading profile photo: " + photoPath, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile photo from path: " + photoPath, e);
            return null;
        }
    }

    /**
     * Deletes the profile photo for a specific username
     * 
     * @param context Application context
     * @param username The username whose profile photo should be deleted
     * @return true if the photo was successfully deleted or didn't exist, false otherwise
     */
    public static boolean deleteProfilePhoto(Context context, String username) {
        if (context == null || username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Context or username is null/empty");
            return false;
        }
        
        String photoPath = getProfilePhotoPath(context, username);
        if (photoPath == null) {
            Log.d(TAG, "No profile photo found for user: " + username);
            return true; // No photo to delete, consider as success
        }
        
        return deleteProfilePhoto(photoPath);
    }

    /**
     * Deletes a profile photo file from the given path
     * 
     * @param photoPath The absolute path to the profile photo file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    public static boolean deleteProfilePhoto(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            Log.e(TAG, "Photo path is null or empty");
            return false;
        }
        
        File photoFile = new File(photoPath);
        if (!photoFile.exists()) {
            Log.w(TAG, "Profile photo file does not exist, considering as deleted: " + photoPath);
            return true; // File doesn't exist, so it's effectively "deleted"
        }
        
        try {
            boolean deleted = photoFile.delete();
            if (deleted) {
                Log.d(TAG, "Profile photo deleted successfully: " + photoPath);
            } else {
                Log.e(TAG, "Failed to delete profile photo: " + photoPath);
            }
            return deleted;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while deleting profile photo: " + photoPath, e);
            return false;
        }
    }

    /**
     * Checks if a profile photo exists for the given username
     * 
     * @param context Application context
     * @param username The username to check for profile photo
     * @return true if the profile photo exists and is readable, false otherwise
     */
    public static boolean profilePhotoExists(Context context, String username) {
        String photoPath = getProfilePhotoPath(context, username);
        return photoPath != null && photoExists(photoPath);
    }

    /**
     * Checks if a photo file exists at the given path
     * 
     * @param photoPath The absolute path to check
     * @return true if the file exists and is readable, false otherwise
     */
    public static boolean photoExists(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return false;
        }
        
        File photoFile = new File(photoPath);
        return photoFile.exists() && photoFile.canRead();
    }

    /**
     * Resizes a bitmap to the specified maximum size while maintaining aspect ratio
     * 
     * @param originalBitmap The original bitmap to resize
     * @param maxSize Maximum width and height for the resized bitmap
     * @return A resized bitmap, or the original if resizing is not needed
     */
    public static Bitmap resizePhoto(Bitmap originalBitmap, int maxSize) {
        if (originalBitmap == null) {
            Log.e(TAG, "Original bitmap is null");
            return null;
        }
        
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        
        // Check if resizing is needed
        if (originalWidth <= maxSize && originalHeight <= maxSize) {
            Log.d(TAG, "Photo is already within size limits");
            return originalBitmap;
        }
        
        // Calculate scaling factor to maintain aspect ratio
        float scaleFactor = Math.min((float) maxSize / originalWidth, (float) maxSize / originalHeight);
        
        // Calculate new dimensions
        int newWidth = Math.round(originalWidth * scaleFactor);
        int newHeight = Math.round(originalHeight * scaleFactor);
        
        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            Log.d(TAG, "Photo resized from " + originalWidth + "x" + originalHeight + 
                      " to " + newWidth + "x" + newHeight);
            return resizedBitmap;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory while resizing photo", e);
            return originalBitmap; // Return original if resizing fails
        } catch (Exception e) {
            Log.e(TAG, "Error resizing photo", e);
            return originalBitmap;
        }
    }

    /**
     * Requests camera permission for profile photo capture
     * 
     * @param activity The FragmentActivity requesting the permission
     * @param callback Callback for permission result
     */
    public static void requestCameraPermission(FragmentActivity activity, PermissionCallback callback) {
        if (activity == null) {
            Log.e(TAG, "Activity is null, cannot request camera permission");
            if (callback != null) {
                callback.onPermissionDenied();
            }
            return;
        }
        
        PermissionUtils.requestCameraPermissionSimple(activity, new PermissionUtils.SimplePermissionCallback() {
            @Override
            public void onGranted() {
                Log.d(TAG, "Camera permission granted for profile photo");
                if (callback != null) {
                    callback.onPermissionGranted();
                }
            }
            
            @Override
            public void onDenied() {
                Log.w(TAG, "Camera permission denied for profile photo");
                if (callback != null) {
                    callback.onPermissionDenied();
                }
            }
        });
    }

    /**
     * Requests storage permission for profile photo selection
     * 
     * @param activity The FragmentActivity requesting the permission
     * @param callback Callback for permission result
     */
    public static void requestStoragePermission(FragmentActivity activity, PermissionCallback callback) {
        if (activity == null) {
            Log.e(TAG, "Activity is null, cannot request storage permission");
            if (callback != null) {
                callback.onPermissionDenied();
            }
            return;
        }
        
        PermissionUtils.requestStoragePermissionSimple(activity, new PermissionUtils.SimplePermissionCallback() {
            @Override
            public void onGranted() {
                Log.d(TAG, "Storage permission granted for profile photo");
                if (callback != null) {
                    callback.onPermissionGranted();
                }
            }
            
            @Override
            public void onDenied() {
                Log.w(TAG, "Storage permission denied for profile photo");
                if (callback != null) {
                    callback.onPermissionDenied();
                }
            }
        });
    }

    /**
     * Checks if camera permission is granted
     * 
     * @param context Application context
     * @return true if camera permission is granted, false otherwise
     */
    public static boolean isCameraPermissionGranted(Context context) {
        return PermissionUtils.isCameraPermissionGranted(context);
    }

    /**
     * Checks if storage permission is granted
     * 
     * @param context Application context
     * @return true if storage permission is granted, false otherwise
     */
    public static boolean isStoragePermissionGranted(Context context) {
        return PermissionUtils.isStoragePermissionGranted(context);
    }

    // Private helper methods

    /**
     * Loads a bitmap from URI using content resolver
     */
    private static Bitmap loadBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream from URI");
                return null;
            }
            
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap from URI", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading bitmap from URI", e);
            return null;
        }
    }

    /**
     * Internal method to save profile photo bitmap to storage
     */
    private static String saveProfilePhotoInternal(Context context, String username, Bitmap bitmap) {
        try {
            // Create profile photos directory if it doesn't exist
            File profilePhotosDir = new File(context.getFilesDir(), PROFILE_PHOTO_DIRECTORY);
            if (!profilePhotosDir.exists() && !profilePhotosDir.mkdirs()) {
                Log.e(TAG, "Failed to create profile photos directory");
                return null;
            }
            
            // Delete existing profile photo for this user
            deleteProfilePhoto(context, username);
            
            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = PROFILE_PHOTO_PREFIX + username + "_" + timestamp + PHOTO_EXTENSION;
            File photoFile = new File(profilePhotosDir, filename);
            
            // Save bitmap to file
            try (FileOutputStream fos = new FileOutputStream(photoFile)) {
                boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, fos);
                if (success) {
                    Log.d(TAG, "Profile photo saved successfully: " + photoFile.getAbsolutePath());
                    return photoFile.getAbsolutePath();
                } else {
                    Log.e(TAG, "Failed to compress profile photo bitmap");
                    return null;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving profile photo", e);
            return null;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while saving profile photo", e);
            return null;
        }
    }
}