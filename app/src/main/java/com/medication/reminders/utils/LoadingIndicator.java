package com.medication.reminders.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.medication.reminders.R;

/**
 * 加载指示器工具类
 * 提供统一的加载状态显示和管理
 * 支持多种加载指示器样式和自定义消息
 */
public class LoadingIndicator {
    
    private static final String TAG = "LoadingIndicator";
    
    /**
     * 加载指示器类型
     */
    public enum IndicatorType {
        PROGRESS_DIALOG,    // 进度对话框
        PROGRESS_BAR,       // 进度条
        INLINE_LOADING,     // 内联加载
        OVERLAY_LOADING     // 覆盖层加载
    }
    
    /**
     * 加载指示器管理器
     */
    public static class Manager {
        private ProgressDialog progressDialog;
        private ProgressBar progressBar;
        private TextView loadingText;
        private View loadingOverlay;
        private Context context;
        private boolean isShowing = false;
        
        public Manager(Context context) {
            this.context = context;
        }
        
        /**
         * 显示进度对话框
         * @param message 加载消息
         * @param cancelable 是否可取消
         */
        public void showProgressDialog(String message, boolean cancelable) {
            if (context == null || !(context instanceof Activity)) {
                return;
            }
            
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            
            hideProgressDialog(); // 先隐藏之前的对话框
            
            try {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(message != null ? message : context.getString(R.string.loading_please_wait));
                progressDialog.setCancelable(cancelable);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                isShowing = true;
            } catch (Exception e) {
                // 在某些情况下（如Activity已销毁）可能会抛出异常
                progressDialog = null;
                isShowing = false;
            }
        }
        
        /**
         * 显示进度对话框（默认不可取消）
         * @param message 加载消息
         */
        public void showProgressDialog(String message) {
            showProgressDialog(message, false);
        }
        
        /**
         * 显示进度对话框（使用资源ID）
         * @param messageResId 加载消息资源ID
         * @param cancelable 是否可取消
         */
        public void showProgressDialog(int messageResId, boolean cancelable) {
            String message = context.getString(messageResId);
            showProgressDialog(message, cancelable);
        }
        
        /**
         * 显示进度对话框（使用资源ID，默认不可取消）
         * @param messageResId 加载消息资源ID
         */
        public void showProgressDialog(int messageResId) {
            showProgressDialog(messageResId, false);
        }
        
        /**
         * 隐藏进度对话框
         */
        public void hideProgressDialog() {
            if (progressDialog != null && progressDialog.isShowing()) {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    // 忽略异常，可能是Activity已销毁
                }
            }
            progressDialog = null;
            isShowing = false;
        }
        
        /**
         * 显示进度条
         * @param progressBar 进度条视图
         * @param loadingText 加载文本视图
         * @param message 加载消息
         */
        public void showProgressBar(ProgressBar progressBar, TextView loadingText, String message) {
            this.progressBar = progressBar;
            this.loadingText = loadingText;
            
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            if (loadingText != null) {
                loadingText.setText(message != null ? message : context.getString(R.string.loading_please_wait));
                loadingText.setVisibility(View.VISIBLE);
            }
            
            isShowing = true;
        }
        
        /**
         * 显示进度条（使用资源ID）
         * @param progressBar 进度条视图
         * @param loadingText 加载文本视图
         * @param messageResId 加载消息资源ID
         */
        public void showProgressBar(ProgressBar progressBar, TextView loadingText, int messageResId) {
            String message = context.getString(messageResId);
            showProgressBar(progressBar, loadingText, message);
        }
        
        /**
         * 隐藏进度条
         */
        public void hideProgressBar() {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            
            if (loadingText != null) {
                loadingText.setVisibility(View.GONE);
            }
            
            progressBar = null;
            loadingText = null;
            
            // 只有当没有其他加载指示器显示时才设置为false
            updateShowingState();
        }
        
        /**
         * 显示覆盖层加载
         * @param overlay 覆盖层视图
         * @param message 加载消息
         */
        public void showOverlayLoading(View overlay, String message) {
            this.loadingOverlay = overlay;
            
            if (overlay != null) {
                overlay.setVisibility(View.VISIBLE);
                
                // 查找覆盖层中的文本视图并设置消息
                TextView textView = overlay.findViewById(R.id.tvLoadingMessage);
                if (textView != null) {
                    textView.setText(message != null ? message : context.getString(R.string.loading_please_wait));
                }
            }
            
            isShowing = true;
        }
        
        /**
         * 显示覆盖层加载（使用资源ID）
         * @param overlay 覆盖层视图
         * @param messageResId 加载消息资源ID
         */
        public void showOverlayLoading(View overlay, int messageResId) {
            String message = context.getString(messageResId);
            showOverlayLoading(overlay, message);
        }
        
        /**
         * 隐藏覆盖层加载
         */
        public void hideOverlayLoading() {
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
            }
            
            loadingOverlay = null;
            
            // 只有当没有其他加载指示器显示时才设置为false
            updateShowingState();
        }
        
        /**
         * 隐藏所有加载指示器
         */
        public void hideAll() {
            hideProgressDialog();
            hideProgressBar();
            hideOverlayLoading();
        }
        
        /**
         * 检查是否正在显示加载指示器
         * @return 是否正在显示
         */
        public boolean isShowing() {
            return isShowing;
        }
        
        /**
         * 更新显示状态
         */
        private void updateShowingState() {
            isShowing = (progressDialog != null && progressDialog.isShowing()) ||
                       (progressBar != null && progressBar.getVisibility() == View.VISIBLE) ||
                       (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE);
        }
        
        /**
         * 更新加载消息
         * @param message 新的加载消息
         */
        public void updateMessage(String message) {
            if (message == null) return;
            
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.setMessage(message);
            }
            
            if (loadingText != null && loadingText.getVisibility() == View.VISIBLE) {
                loadingText.setText(message);
            }
            
            if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
                TextView textView = loadingOverlay.findViewById(R.id.tvLoadingMessage);
                if (textView != null) {
                    textView.setText(message);
                }
            }
        }
        
        /**
         * 更新加载消息（使用资源ID）
         * @param messageResId 新的加载消息资源ID
         */
        public void updateMessage(int messageResId) {
            String message = context.getString(messageResId);
            updateMessage(message);
        }
        
        /**
         * 清理资源
         */
        public void cleanup() {
            hideAll();
            context = null;
        }
    }
    
    /**
     * 健康日记专用加载指示器
     */
    public static class HealthDiary {
        
        /**
         * 显示日记列表加载指示器
         * @param manager 加载管理器
         */
        public static void showListLoading(Manager manager) {
            if (manager != null) {
                manager.showProgressDialog(R.string.loading_diary_list);
            }
        }
        
        /**
         * 显示日记详情加载指示器
         * @param manager 加载管理器
         */
        public static void showDetailLoading(Manager manager) {
            if (manager != null) {
                manager.showProgressDialog(R.string.loading_diary_detail);
            }
        }
        
        /**
         * 显示保存日记加载指示器
         * @param manager 加载管理器
         */
        public static void showSaveLoading(Manager manager) {
            if (manager != null) {
                manager.showProgressDialog(R.string.loading_save_diary);
            }
        }
        
        /**
         * 显示更新日记加载指示器
         * @param manager 加载管理器
         */
        public static void showUpdateLoading(Manager manager) {
            if (manager != null) {
                manager.showProgressDialog(R.string.loading_update_diary);
            }
        }
        
        /**
         * 显示删除日记加载指示器
         * @param manager 加载管理器
         */
        public static void showDeleteLoading(Manager manager) {
            if (manager != null) {
                manager.showProgressDialog(R.string.loading_delete_diary);
            }
        }
        
        /**
         * 显示内联加载指示器（用于列表页面）
         * @param manager 加载管理器
         * @param progressBar 进度条
         * @param loadingText 加载文本
         */
        public static void showInlineListLoading(Manager manager, ProgressBar progressBar, TextView loadingText) {
            if (manager != null) {
                manager.showProgressBar(progressBar, loadingText, R.string.loading_diary_list);
            }
        }
        
        /**
         * 显示内联加载指示器（用于详情页面）
         * @param manager 加载管理器
         * @param progressBar 进度条
         * @param loadingText 加载文本
         */
        public static void showInlineDetailLoading(Manager manager, ProgressBar progressBar, TextView loadingText) {
            if (manager != null) {
                manager.showProgressBar(progressBar, loadingText, R.string.loading_diary_detail);
            }
        }
        
        /**
         * 显示覆盖层加载指示器
         * @param manager 加载管理器
         * @param overlay 覆盖层视图
         * @param operation 操作类型（save, update, delete）
         */
        public static void showOverlayLoading(Manager manager, View overlay, String operation) {
            if (manager == null || overlay == null) return;
            
            int messageResId;
            switch (operation.toLowerCase()) {
                case "save":
                case "保存":
                    messageResId = R.string.loading_save_diary;
                    break;
                case "update":
                case "更新":
                    messageResId = R.string.loading_update_diary;
                    break;
                case "delete":
                case "删除":
                    messageResId = R.string.loading_delete_diary;
                    break;
                default:
                    messageResId = R.string.loading_please_wait;
                    break;
            }
            
            manager.showOverlayLoading(overlay, messageResId);
        }
    }
    
    /**
     * 创建加载管理器的便捷方法
     * @param context 上下文
     * @return 加载管理器实例
     */
    public static Manager createManager(Context context) {
        return new Manager(context);
    }
    
    /**
     * 全局加载管理器（单例模式）
     */
    private static Manager globalManager;
    
    /**
     * 获取全局加载管理器
     * @param context 上下文
     * @return 全局加载管理器
     */
    public static Manager getGlobalManager(Context context) {
        if (globalManager == null || globalManager.context != context) {
            globalManager = new Manager(context);
        }
        return globalManager;
    }
    
    /**
     * 清理全局加载管理器
     */
    public static void cleanupGlobalManager() {
        if (globalManager != null) {
            globalManager.cleanup();
            globalManager = null;
        }
    }
}