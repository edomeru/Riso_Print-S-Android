/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * BaseActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.base;

import java.io.File;
import java.io.IOException;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.FileUtils;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartprint.R;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Debug;
import android.util.DebugUtils;

/**
 * @class BaseActivity
 * 
 * @brief Base activity class
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        onCreateContent(savedInstanceState);
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