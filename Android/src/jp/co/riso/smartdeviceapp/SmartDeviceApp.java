/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

public class SmartDeviceApp extends Application {

    private static volatile Context mContext;
    private static volatile Typeface mAppFont;
    
    @Override
    public void onCreate() {
        SmartDeviceApp.mContext = getApplicationContext();
        SmartDeviceApp.mAppFont = Typeface.createFromAsset(getResources().getAssets(),
                AppConstants.APP_FONT_FILE);
    }

    public static Context getAppContext() {
        return SmartDeviceApp.mContext;
    }

    public static Typeface getAppFont() {
        return SmartDeviceApp.mAppFont;
    }
}
