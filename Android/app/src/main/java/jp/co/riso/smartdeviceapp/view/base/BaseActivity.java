/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * BaseActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.base;

import androidx.fragment.app.FragmentActivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartprint.R;

/**
 * @class BaseActivity
 * 
 * @brief Base activity class
 */
public abstract class BaseActivity extends FragmentActivity {

    private int systemUIFlags;      // Stores initial System UI Visibility flags of device. Initialized and used only on Android 10 Phones.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        onCreateContent(savedInstanceState);

        /* V2.2 BUG: Display bug occurs only on Android 10 Phones with 2-3 button system navigation enabled when device is rotated.
         * Fix:
         *  - Detect display rotation (landscape to reverse landscape rotation not handled in `onConfigurationChanged()`
         *  - Hide system navigation bar upon rotation (this allows the app to cover the whole screen's width)
         *  - Display system navigation bar again immediately
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isTablet()) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                // 031521 - For API Level 30 deprecation
                getSystemUIFlagsForSDK29();
            }

            DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    handleSystemUIRotation();
                }
            };
            DisplayManager displayManager = (DisplayManager) SmartDeviceApp.getAppContext().getSystemService(Context.DISPLAY_SERVICE);
            if (displayManager != null) {
                displayManager.registerDisplayListener(mDisplayListener, new Handler(Looper.myLooper()));
            }
        }
    }
    
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    
    /**
     * @brief Called in onCreate which will serve as the main activity initialization.
     * 
     * @param savedInstanceState Bundle which contains a saved state during recreation
     */
    protected abstract void onCreateContent(Bundle savedInstanceState);

    // ================================================================================
    // Private Functions
    // ================================================================================

    private void handleSystemUIRotation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 031521 - For API Level 30 deprecation
            if (getWindow().getInsetsController() != null) {
                // Hide system navigation bar
                getWindow().setDecorFitsSystemWindows(false);
                getWindow().getInsetsController().hide(WindowInsets.Type.navigationBars());
                getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

                Handler handler = new Handler(Looper.myLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Show system navigation bar
                        getWindow().setDecorFitsSystemWindows(true);
                        getWindow().getInsetsController().show(WindowInsets.Type.navigationBars());
                    }
                }, 10);
            }
        } else {
            // 031521 - For API Level 30 deprecation
            handleSystemUIRotationForSDK29();
        }
    }

    @SuppressWarnings("deprecation")
    private void getSystemUIFlagsForSDK29() {
        View decorView = getWindow().getDecorView();
        systemUIFlags = decorView.getSystemUiVisibility();
    }

    @SuppressWarnings("deprecation")
    private void handleSystemUIRotationForSDK29() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(systemUIFlags | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);       // Hide system navigation bar

        Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(systemUIFlags | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);         // Show system navigation bar
            }
        }, 10);
    }

    // ================================================================================
    // Public Functions
    // ================================================================================
    
    /**
     * @brief Checks whether the device is in tablet mode.
     * 
     * @retval true Device is a tablet
     * @retval false Device is a phone
     */
    public boolean isTablet() {
        return getResources().getBoolean(R.bool.is_tablet);
    }
    
    /**
     * @brief Gets the action bar height from the android defaults.
     * 
     * @return Action bar height in pixels
     */
    public int getActionBarHeight() {
        // Calculate ActionBar height
        int actionBarHeight = getResources().getDimensionPixelSize(R.dimen.actionbar_height);
        return actionBarHeight;
    }
    
    /**
     * @brief Gets the drawer width.
     * 
     * @return Drawer width in pixels
     */
    public int getDrawerWidth() {
        Point screenSize = AppUtils.getScreenDimensions(this);
        float drawerWidthPercentage = getResources().getFraction(R.fraction.drawer_width_percentage, 1, 1);
        int minDrawerWidth = getResources().getDimensionPixelSize(R.dimen.drawer_width_min);
        int maxDrawerWidth = getResources().getDimensionPixelSize(R.dimen.drawer_width_max);
        
        float drawerWidth = Math.min(screenSize.x, screenSize.y) * drawerWidthPercentage;
        drawerWidth = Math.max(drawerWidth, minDrawerWidth);
        drawerWidth = Math.min(drawerWidth, maxDrawerWidth);
        
        return (int) drawerWidth;
    }
}
