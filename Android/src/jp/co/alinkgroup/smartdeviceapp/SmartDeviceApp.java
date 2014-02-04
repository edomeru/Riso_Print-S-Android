/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.smartdeviceapp;

import jp.co.alinkgroup.android.util.Logger;
import android.app.Application;
import android.content.Context;

public class SmartDeviceApp extends Application {

    private static volatile Context context;
    
    @Override
    public void onCreate() {
        Logger.init(getApplicationContext());
        Logger.setLogLevel(Logger.LOGLEVEL_DEBUG);
        
        SmartDeviceApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SmartDeviceApp.context;
    }
}
