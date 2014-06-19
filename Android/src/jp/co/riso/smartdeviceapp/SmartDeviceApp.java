/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

import java.util.HashMap;

import jp.co.riso.android.util.Logger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SmartDeviceApp extends Application {

    private static volatile Context sContext;
    
    /** {@inheritDoc} */
    @Override
    public void onCreate() {
        SmartDeviceApp.sContext = getApplicationContext();
        
        initializeSharedPrefs();
        
        if (AppConstants.DEBUG) {
            Logger.initialize(Logger.LOGLEVEL_VERBOSE, AppConstants.FOR_PERF_LOGS, AppConstants.FOR_PERF_LOGS);
            Logger.runDeleteTask(getApplicationContext());
        }
    }

    /**
     * Retrieve the application context
     * 
     * @return SmartDeviceAPP context
     */
    public static Context getAppContext() {
        return SmartDeviceApp.sContext;
    }
    
    //TODO: Remove this NOT NECESSARY
    /**
     * Initializes state of Shared Preferences.
     * Adds default values if default shared preferences does not contain value.
     */
    private void initializeSharedPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getAppContext());
        SharedPreferences.Editor editor = prefs.edit();
        
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(AppConstants.PREF_KEY_LOGIN_ID, AppConstants.PREF_DEFAULT_LOGIN_ID);
        
        for (String key : hashMap.keySet()) {
            String val = hashMap.get(key);
            if (!prefs.contains(key)) {
                editor.putString(key, val);
            }
        }
        
        editor.apply();
    }
}
