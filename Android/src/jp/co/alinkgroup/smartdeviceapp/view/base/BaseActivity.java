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
import android.util.TypedValue;

public abstract class BaseActivity extends Activity {
    
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
