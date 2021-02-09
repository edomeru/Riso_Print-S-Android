/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

import jp.co.riso.android.util.Logger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @class SmartDeviceApp
 * 
 * @brief Application class.
 */
public class SmartDeviceApp extends Application implements Application.ActivityLifecycleCallbacks {

    private static volatile Context sContext;
    private static volatile Activity sActivity = null;
    
    @Override
    public void onCreate() {
        super.onCreate();
        SmartDeviceApp.sContext = getApplicationContext();
        
        if (AppConstants.DEBUG) {
            Logger.initialize(Logger.LOGLEVEL_VERBOSE, AppConstants.FOR_PERF_LOGS, AppConstants.FOR_PERF_LOGS);
            Logger.runDeleteTask(getApplicationContext());
        }
        registerActivityLifecycleCallbacks(this);
    }

    /**
     * @brief Retrieve the application context.
     * 
     * @return Application shared context
     */
    public static Context getAppContext() {
        return SmartDeviceApp.sContext;
    }

    public static Activity getActivity() { return sActivity; }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        sActivity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        sActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        sActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
