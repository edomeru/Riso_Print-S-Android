/*
 * Copyright (c) 2014 All rights reserved.
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
     * Called in on create
     */
    protected abstract void onCreateContent(Bundle savedInstanceState);
    
    // ================================================================================
    // Public Functions
    // ================================================================================

    public boolean isTablet() {
        return getResources().getBoolean(R.bool.is_tablet);
    }
    
    public int getActionBarHeight() {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return -1;
    }
    
    public int getDrawerWidth() {
        Point screenSize = AppUtils.getScreenDimensions(this);
        float drawerWidthPercentage = getResources().getFraction(R.dimen.drawer_width_percentage, 1, 1);
        float minDrawerWidth = getResources().getDimension(R.dimen.drawer_width_min);
        float maxDrawerWidth = getResources().getDimension(R.dimen.drawer_width_max);
        
        float drawerWidth = screenSize.x * drawerWidthPercentage;
        drawerWidth = Math.max(drawerWidth, minDrawerWidth);
        drawerWidth = Math.min(drawerWidth, maxDrawerWidth);
        
        return (int) drawerWidth;
    }
}
