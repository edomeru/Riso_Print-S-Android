/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

import jp.co.riso.android.util.Logger;

import android.app.Application;
import android.content.Context;

/**
 * @class SmartDeviceApp
 * 
 * @brief Application class.
 */
public class SmartDeviceApp extends Application {

    private static volatile Context sContext;
    
    @Override
    public void onCreate() {
        SmartDeviceApp.sContext = getApplicationContext();
        
        if (AppConstants.DEBUG) {
            Logger.initialize(Logger.LOGLEVEL_VERBOSE, AppConstants.FOR_PERF_LOGS, AppConstants.FOR_PERF_LOGS);
            Logger.runDeleteTask(getApplicationContext());
        }
    }

    /**
     * @brief Retrieve the application context.
     * 
     * @return Application shared context
     */
    public static Context getAppContext() {
        return SmartDeviceApp.sContext;
    }
}
