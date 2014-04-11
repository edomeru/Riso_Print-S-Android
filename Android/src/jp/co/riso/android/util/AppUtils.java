/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AppUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public final class AppUtils {
    public static final String TAG = "AppUtils"; 
    
    public static final String CONST_ASSET_PATH = "file:///android_asset/";
    
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
        // Should not be created if context or class is null
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
    
    /**
     * Gets the Application package name
     * 
     * @param context
     *            Application Context
     * @return Package name of the application
     */
    public static String getApplicationPackageName(Context context) {
        if (context == null) {
            return null;
        }
        
        return context.getPackageName();
    }
    
    /**
     * Gets the Application install date using the package manager
     * 
     * @param context
     *            Application Context
     * 
     * @return Time in millis of the the last install date
     */
    public static long getApplicationLastInstallDate(Context context, String packageName) throws NameNotFoundException {
        if (context == null) {
            return 0;
        }
        
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;
        
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            throw e;
        }
        
        String appFilePath = appInfo.sourceDir;
        File appFile = new File(appFilePath);
        
        return appFile.lastModified();
    }
    
    /**
     * Forcibly dismisses the Softkeyboard
     * 
     * @param activity
     *            Valid activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    
    /**
     * Gets the Screen Dimensions of the Device
     * 
     * @param activity
     *            Valid activity
     *            
     * @return Screen size
     */
    public static Point getScreenDimensions(Activity activity) {
        if (activity == null) {
            return null;
        }
        
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);
        
        return size;
    }
    
    /**
     * Checks whether the asset exists
     * 
     * @param context
     *            Valid Context
     * @param assetFile
     *            Relative path of the asset (from assets/)
     *            
     * @return Whether the asset exists or not
     */
    public static String getFileContentsFromAssets(Context context, String assetFile) {
        if (context == null) {
            return null;
        }
        
        StringBuilder buf = new StringBuilder();
        InputStream stream;
        try {
            stream = context.getApplicationContext().getAssets().open(assetFile);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String str;
            
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            
            in.close();
        } catch (IOException e) {
            return null;
        }
        
        return buf.toString();
    }
    
    /**
     * Checks whether the asset exists
     * 
     * @param context
     *            Valid Context
     * @param assetFile
     *            Relative path of the asset (from assets/)
     *            
     * @return Whether the asset exists or not
     */
    public static boolean assetExists(Context context, String assetFile) {
        if (context == null || assetFile == null) {
            return false;
        }
        
        boolean assetOk = false;
        try {
            InputStream stream = context.getAssets().open(assetFile);
            stream.close();
            assetOk = true;
        } catch (FileNotFoundException e) {
            Log.w(TAG, "assetExists failed: "+e.toString());
        } catch (IOException e) {
            Log.w(TAG, "assetExists failed: "+e.toString());
        }
        return assetOk;
    }
    
    /**
     * Gets the relative localized path
     * 
     * @param context
     *            Valid Context
     * @param folder
     *            Directory of the file for localization
     * @param resource
     *            File to be opened
     *            
     * @return Localized relative path of the asset file
     */
    public static String getLocalizedAssetRelativePath(Context context, String folder, String resource) {
        if (context == null || folder == null || resource == null) {
            return null;
        }
        
        if (folder.isEmpty() || resource.isEmpty()) {
            return null;
        }
        
        String relativePath = folder + "-" + AppUtils.getLocaleCode() + "/" + resource;
        
        boolean assetExists = assetExists(context, relativePath);
        
        if (!assetExists) {
            relativePath = folder + "/" + resource;
        }
        
        return relativePath;
    }
    
    /**
     * Gets the full localized path
     * 
     * @param context
     *            Valid Context
     * @param folder
     *            Directory of the file for localization
     * @param resource
     *            File to be opened
     *            
     * @return Localized full path of the asset file
     */
    public static String getLocalizedAssetFullPath(Context context, String folder, String resource) {
        String relativePath = getLocalizedAssetRelativePath(context, folder, resource);
        
        if (relativePath != null) {
            return CONST_ASSET_PATH + relativePath;
        }
        
        return null;
    }
    
    //http://stackoverflow.com/questions/2711858/is-it-possible-to-set-font-for-entire-application
    public static void changeChildrenFont(ViewGroup v, Typeface font){
        if (font == null) {
            return;
        }
        
        for (int i = 0; i < v.getChildCount(); i++) {
            
            // For the ViewGroup, we'll have to use recursivity
            if (v.getChildAt(i) instanceof ViewGroup) {
                changeChildrenFont((ViewGroup) v.getChildAt(i), font);
            } else {
                try {
                    Object[] nullArgs = null;
                    // Test wether setTypeface and getTypeface methods exists
                    Method methodTypeFace = v.getChildAt(i).getClass().getMethod("setTypeface", new Class[] { Typeface.class, Integer.TYPE });
                    // With getTypefaca we'll get back the style (Bold, Italic...) set in XML
                    Method methodGetTypeFace = v.getChildAt(i).getClass().getMethod("getTypeface", new Class[] {});
                    Typeface typeFace = ((Typeface) methodGetTypeFace.invoke(v.getChildAt(i), nullArgs));
                    // Invoke the method and apply the new font with the defined style to the view if the method exists
                    // (textview,...)
                    methodTypeFace.invoke(v.getChildAt(i), new Object[] { font, typeFace == null ? 0 : typeFace.getStyle() });
                }
                // Will catch the view with no such methods (listview...)
                catch (NoSuchMethodException e) {
                    Log.w(TAG, "NoSuchMethodException (setTypeface and getTypeface)");
                } catch (IllegalAccessException e) {
                    Log.w(TAG, "IllegalAccessException, on invoke");
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "IllegalArgumentException on invoke");
                } catch (InvocationTargetException e) {
                    Log.w(TAG, "InvocationTargetException on invoke");
                }
            }
        }
    }
    
    //http://daniel-codes.blogspot.jp/2009/12/dynamically-retrieving-resources-in.html
    public static int getResourseId(String variableName, Class<?> c, int defaultId) {
        if (variableName == null) {
            return defaultId;
        }
        
        int id = defaultId;
        try {
            Field idField = c.getDeclaredField(variableName);
            id = idField.getInt(idField);
        } catch (NoSuchFieldException e) {
            Log.w(TAG, "No id on class");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException on getInt");
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "IllegalArgumentException on getInt");
        } 
        
        return id;
    }
    
    public static int getCacheSizeBasedOnMemoryClass(Activity activity) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE); 
        
        // Get memory class of this device, exceeding this amount will throw an OutOfMemory exception.
        final int memClass = manager.getMemoryClass();
        
        return 1024 * 1024 * memClass;
    }
}