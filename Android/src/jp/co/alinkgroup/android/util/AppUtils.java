/*
 * Copyright (c) 2014 All rights reserved.
 *
 * AppUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.android.util;

import java.io.File;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.AndroidRuntimeException;

public final class AppUtils {
    
    private AppUtils() {
        // Avoid initialization
    }
    
    /**
     * Creates an activity intent launcher
     * 
     * @param context
     *            Application Context
     * @param cls
     *            Activity class
     * @return Intent generated
     */
    public static Intent createActivityIntent(Context context, Class<?> cls) {
        // Should not be created if context o
        if (context == null || cls == null) {
            return null;
        }
        
        Intent intent = new Intent();
        intent.setClass(context, cls);
        return intent;
    }
    
    /**
     * Starts an Activity
     * 
     * @param context
     *            Application Context
     * @param cls
     *            Activity class
     */
    public static void startActivityIntent(Context context, Class<?> cls) throws ActivityNotFoundException, NullPointerException, AndroidRuntimeException {
        Intent intent = createActivityIntent(context, cls);
        
        if (intent == null) {
            throw new NullPointerException("Cannot create intent");
        } else {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                throw e;
            } catch (AndroidRuntimeException e) {
                throw e;
            }
        }
    }
    
    /**
     * Gets the 2 character locale code based on the current Locale. (e.g., en, ja, etc)
     * 
     * @return Locale Code String
     */
    public static String getLocaleCode() {
        Locale defaultLocale = Locale.getDefault();
        String localeCode = defaultLocale.toString().substring(0, 2).toLowerCase(defaultLocale);
        
        return localeCode;
    }
    
    public static long getApplicationLastInstallDate(Context context) {
        String packageName = context.getApplicationContext().getPackageName();
        
        long info = 0;
        
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo;
            appInfo = pm.getApplicationInfo(packageName, 0);
            
            String appFilePath = appInfo.sourceDir;
            
            File appFile = new File(appFilePath);
            
            info = appFile.lastModified();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        
        return info;
    }
}
