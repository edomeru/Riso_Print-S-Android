/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SmartDeviceApp.java
 * MemoryUtils
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;

public class MemoryUtils {
    /**
     * @param activity
     * @return cache size
     */
    public static int getCacheSizeBasedOnMemoryClass(Activity activity) {
        if (activity == null) {
            return 0;
        }
        
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        
        // Get memory class of this device, exceeding this amount will throw an OutOfMemory exception.
        final int memClass = manager.getMemoryClass();
        
        return 1024 * 1024 * memClass;
    }
    
    /**
     * @param context
     * @return available memory
     */
    public static float getAvailableMemory(Context context) {
        if (context == null) {
            return Float.NaN;
        }
        
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return (mi.availMem / 1048576L);
    }
}
