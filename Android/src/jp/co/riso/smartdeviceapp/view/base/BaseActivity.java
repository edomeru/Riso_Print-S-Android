/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * BaseActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.base;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;

public abstract class BaseActivity extends Activity {

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_NO_ANIMATION) == 0) {
            // Override transition for consistency with Fragment Transition
            overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
        }
        
        onCreateContent(savedInstanceState);
    }
    
    // ================================================================================
    // Abstract Functions
    // ================================================================================
    
    /**
     * Called in onCreate which will serve as the main activity initialization
     * 
     * @param savedInstanceState
     *            Bundle which contains a saved state during recreation
     */
    protected abstract void onCreateContent(Bundle savedInstanceState);
    
    // ================================================================================
    // Public Functions
    // ================================================================================
    
    /**
     * Checks whether the device is in tablet mode
     * 
     * @return True if device is tablet, False otherwise
     */
    public boolean isTablet() {
        return getResources().getBoolean(R.bool.is_tablet);
    }
    
    /**
     * Gets the action bar height from the android defaults
     * 
     * @return Action bar height in pixels
     */
    public int getActionBarHeight() {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return -1;
    }
    
    /**
     * Gets the drawer width.
     * 
     * @return Drawer width in pixels
     */
    public int getDrawerWidth() {
        Point screenSize = AppUtils.getScreenDimensions(this);
        float drawerWidthPercentage = getResources().getFraction(R.dimen.drawer_width_percentage, 1, 1);
        float minDrawerWidth = getResources().getDimension(R.dimen.drawer_width_min);
        float maxDrawerWidth = getResources().getDimension(R.dimen.drawer_width_max);
        
        float drawerWidth = Math.min(screenSize.x, screenSize.y) * drawerWidthPercentage;
        drawerWidth = Math.max(drawerWidth, minDrawerWidth);
        drawerWidth = Math.min(drawerWidth, maxDrawerWidth);
        
        return (int) drawerWidth;
    }
}