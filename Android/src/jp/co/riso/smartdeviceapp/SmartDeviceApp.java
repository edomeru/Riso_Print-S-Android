/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

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
        if (!prefs.contains(AppConstants.PREF_KEY_CARD_ID)) {
            editor.putString(AppConstants.PREF_KEY_CARD_ID, AppConstants.PREF_DEFAULT_CARD_ID);
        }
        if (!prefs.contains(AppConstants.PREF_KEY_READ_COMM_NAME)) {
            editor.putString(AppConstants.PREF_KEY_READ_COMM_NAME, AppConstants.PREF_DEFAULT_READ_COMM_NAME);
        }
        editor.commit();
    }
}
