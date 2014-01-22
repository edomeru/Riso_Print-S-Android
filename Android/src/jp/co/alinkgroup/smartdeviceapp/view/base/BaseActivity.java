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

public abstract class BaseActivity extends Activity {
    
    public boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }
}
