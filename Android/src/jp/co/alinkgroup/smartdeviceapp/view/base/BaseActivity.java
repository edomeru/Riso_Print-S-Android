/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SplashActivity.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.smartdeviceapp.view.base;

import jp.co.alinkgroup.smartdeviceapp.R;
import android.app.Activity;
import android.content.Intent;
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

    /*
     * Called in on create
     */
    protected abstract void onCreateContent(Bundle savedInstanceState);
    
    // ================================================================================
    // Public Functions
    // ================================================================================

    public boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }
    
    public int getActionBarHeight() {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return -1;
    }
}
