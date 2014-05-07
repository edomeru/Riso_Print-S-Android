/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

public class SmartDeviceApp extends Application {

    private static volatile Context sContext;
    private static volatile Typeface sAppFont;
    
    @Override
    public void onCreate() {
        SmartDeviceApp.sContext = getApplicationContext();
        SmartDeviceApp.sAppFont = Typeface.createFromAsset(getResources().getAssets(),
                AppConstants.APP_FONT_FILE);
        
        initializeSharedPrefs();
    }

    public static Context getAppContext() {
        return SmartDeviceApp.sContext;
    }

    public static Typeface getAppFont() {
        return SmartDeviceApp.sAppFont;
    }
    
    /**
     * Initializes state of Shared Preferences.
     * Adds default values if default shared preferences does not contain value.
     */
    private void initializeSharedPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getAppContext());
        SharedPreferences.Editor editor = prefs.edit();
        
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(AppConstants.PREF_KEY_LOGIN_ID, AppConstants.PREF_DEFAULT_LOGIN_ID);
        hashMap.put(AppConstants.PREF_KEY_PIN_CODE, AppConstants.PREF_DEFAULT_PIN_CODE);
        
        for (String key : hashMap.keySet()) {
            String val = hashMap.get(key);
            if (!prefs.contains(key)) {
                editor.putString(key, val);
            }
        }
        
        editor.commit();
    }
}
