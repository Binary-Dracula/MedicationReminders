package com.medication.reminders;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;

import java.util.List;

/**
 * Utility class for handling runtime permissions using PermissionX library.
 * Provides methods for requesting camera and storage permissions with proper
 * user guidance and settings redirection.
 */
public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    /**
     * Interface for permission request callbacks
     */
    public interface PermissionCallback {
        /**
         * Called when permission request is completed
         * 
         * @param allGranted true if all requested permissions were granted
         * @param grantedList list of granted permissions
         * @param deniedList list of denied permissions
         */
        void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList);
    }

    /**
     * Requests camera permission with proper explanation and settings guidance
     * 
     * @param activity The FragmentActivity requesting the permission
     * @param callback Callback to handle the permission result
     */
    public static void requestCameraPermission(FragmentActivity activity, PermissionCallback callback) {
        if (activity == null) {
            Log.e(TAG, "Activity is null, cannot request camera permission");
            if (callback != null) {
                callback.onResult(false, null, null);
            }
            return;
        }

        if (callback == null) {
            Log.e(TAG, "Callback is null, cannot handle permission result");
            return;
        }

        PermissionX.init(activity)
                .permissions(Manifest.permission.CAMERA)
                .onExplainRequestReason((scope, deniedList) -> {
                    // Show explanation dialog when user denies permission
                    scope.showRequestReasonDialog(
                            deniedList,
                            activity.getString(R.string.camera_permission_reason),
                            activity.getString(R.string.confirm),
                            activity.getString(R.string.cancel)
                    );
                })
                .onForwardToSettings((scope, deniedList) -> {
                    // Show settings dialog when permission is permanently denied
                    scope.showForwardToSettingsDialog(
                            deniedList,
                            activity.getString(R.string.camera_permission_settings_message),
                            activity.getString(R.string.go_to_settings),
                            activity.getString(R.string.cancel)
                    );
                })
                .request((allGranted, grantedList, deniedList) -> {
                    Log.d(TAG, "Camera permission request result - All granted: " + allGranted);
                    if (!allGranted) {
                        Log.w(TAG, "Camera permission denied: " + deniedList);
                    }
                    callback.onResult(allGranted, grantedList, deniedList);
                });
    }

    /**
     * Requests storage permission with proper explanation and settings guidance
     * 
     * @param activity The FragmentActivity requesting the permission
     * @param callback Callback to handle the permission result
     */
    public static void requestStoragePermission(FragmentActivity activity, PermissionCallback callback) {
        if (activity == null) {
            Log.e(TAG, "Activity is null, cannot request storage permission");
            if (callback != null) {
                callback.onResult(false, null, null);
            }
            return;
        }

        if (callback == null) {
            Log.e(TAG, "Callback is null, cannot handle permission result");
            return;
        }

        PermissionX.init(activity)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .onExplainRequestReason((scope, deniedList) -> {
                    // Show explanation dialog when user denies permission
                    scope.showRequestReasonDialog(
                            deniedList,
                            activity.getString(R.string.storage_permission_reason),
                            activity.getString(R.string.confirm),
                            activity.getString(R.string.cancel)
                    );
                })
                .onForwardToSettings((scope, deniedList) -> {
                    // Show settings dialog when permission is permanently denied
                    scope.showForwardToSettingsDialog(
                            deniedList,
                            activity.getString(R.string.storage_permission_settings_message),
                            activity.getString(R.string.go_to_settings),
                            activity.getString(R.string.cancel)
                    );
                })
                .request((allGranted, grantedList, deniedList) -> {
                    Log.d(TAG, "Storage permission request result - All granted: " + allGranted);
                    if (!allGranted) {
                        Log.w(TAG, "Storage permission denied: " + deniedList);
                    }
                    callback.onResult(allGranted, grantedList, deniedList);
                });
    }

    /**
     * Requests both camera and storage permissions together
     * 
     * @param activity The FragmentActivity requesting the permissions
     * @param callback Callback to handle the permission result
     */
    public static void requestCameraAndStoragePermissions(FragmentActivity activity, PermissionCallback callback) {
        if (activity == null) {
            Log.e(TAG, "Activity is null, cannot request permissions");
            if (callback != null) {
                callback.onResult(false, null, null);
            }
            return;
        }

        if (callback == null) {
            Log.e(TAG, "Callback is null, cannot handle permission result");
            return;
        }

        PermissionX.init(activity)
                .permissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onExplainRequestReason((scope, deniedList) -> {
                    // Determine which permissions are being requested
                    String message;
                    if (deniedList.contains(Manifest.permission.CAMERA) && 
                        deniedList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        message = activity.getString(R.string.camera_permission_reason) + "\n" +
                                 activity.getString(R.string.storage_permission_reason);
                    } else if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = activity.getString(R.string.camera_permission_reason);
                    } else {
                        message = activity.getString(R.string.storage_permission_reason);
                    }

                    scope.showRequestReasonDialog(
                            deniedList,
                            message,
                            activity.getString(R.string.confirm),
                            activity.getString(R.string.cancel)
                    );
                })
                .onForwardToSettings((scope, deniedList) -> {
                    // Determine which permissions need settings access
                    String message;
                    if (deniedList.contains(Manifest.permission.CAMERA) && 
                        deniedList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        message = activity.getString(R.string.camera_permission_settings_message) + "\n" +
                                 activity.getString(R.string.storage_permission_settings_message);
                    } else if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = activity.getString(R.string.camera_permission_settings_message);
                    } else {
                        message = activity.getString(R.string.storage_permission_settings_message);
                    }

                    scope.showForwardToSettingsDialog(
                            deniedList,
                            message,
                            activity.getString(R.string.go_to_settings),
                            activity.getString(R.string.cancel)
                    );
                })
                .request((allGranted, grantedList, deniedList) -> {
                    Log.d(TAG, "Camera and storage permissions request result - All granted: " + allGranted);
                    if (!allGranted) {
                        Log.w(TAG, "Some permissions denied: " + deniedList);
                    }
                    callback.onResult(allGranted, grantedList, deniedList);
                });
    }

    /**
     * Checks if camera permission is granted
     * 
     * @param context Application context
     * @return true if camera permission is granted, false otherwise
     */
    public static boolean isCameraPermissionGranted(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot check camera permission");
            return false;
        }

        return PermissionX.isGranted(context, Manifest.permission.CAMERA);
    }

    /**
     * Checks if storage permission is granted
     * 
     * @param context Application context
     * @return true if storage permission is granted, false otherwise
     */
    public static boolean isStoragePermissionGranted(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot check storage permission");
            return false;
        }

        return PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * Checks if both camera and storage permissions are granted
     * 
     * @param context Application context
     * @return true if both permissions are granted, false otherwise
     */
    public static boolean areCameraAndStoragePermissionsGranted(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot check permissions");
            return false;
        }

        return PermissionX.isGranted(context, Manifest.permission.CAMERA) &&
               PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * Simple callback interface for basic permission requests
     */
    public interface SimplePermissionCallback {
        /**
         * Called when permission is granted
         */
        void onGranted();

        /**
         * Called when permission is denied
         */
        void onDenied();
    }

    /**
     * Simplified method to request camera permission with basic callback
     * 
     * @param activity The FragmentActivity requesting the permission
     * @param callback Simple callback for granted/denied result
     */
    public static void requestCameraPermissionSimple(FragmentActivity activity, SimplePermissionCallback callback) {
        requestCameraPermission(activity, new PermissionCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                if (callback != null) {
                    if (allGranted) {
                        callback.onGranted();
                    } else {
                        callback.onDenied();
                    }
                }
            }
        });
    }

    /**
     * Simplified method to request storage permission with basic callback
     * 
     * @param activity The FragmentActivity requesting the permission
     * @param callback Simple callback for granted/denied result
     */
    public static void requestStoragePermissionSimple(FragmentActivity activity, SimplePermissionCallback callback) {
        requestStoragePermission(activity, new PermissionCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                if (callback != null) {
                    if (allGranted) {
                        callback.onGranted();
                    } else {
                        callback.onDenied();
                    }
                }
            }
        });
    }
}