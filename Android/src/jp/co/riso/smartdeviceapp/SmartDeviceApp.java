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

    private static volatile Context sContext;
    private static volatile Typeface sAppFont;
    
    @Override
    public void onCreate() {
        SmartDeviceApp.sContext = getApplicationContext();
        SmartDeviceApp.sAppFont = Typeface.createFromAsset(getResources().getAssets(),
                AppConstants.APP_FONT_FILE);
    }

    public static Context getAppContext() {
        return SmartDeviceApp.sContext;
    }

    public static Typeface getAppFont() {
        return SmartDeviceApp.sAppFont;
    }
}
