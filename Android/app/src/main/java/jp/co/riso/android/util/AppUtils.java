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

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;

/**
 * @class AppUtils
 * 
 * @brief Utility class for application operations
 */
public final class AppUtils {
    
    /// Path to asset files
    public static final String CONST_ASSET_PATH = "file:///android_asset/";
    
    /**
     * @brief Creates an activity intent launcher.
     * 
     * @param context Application Context
     * @param cls Activity class
     * 
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
     * @brief Gets the 2 character locale code based on the current Locale. (e.g., en, ja, etc)
     * 
     * @return Locale Code String
     */
    public static String getLocaleCode() {
        Locale defaultLocale = Locale.getDefault();
        String localeCode = defaultLocale.toString().substring(0, 2).toLowerCase(defaultLocale);
        
        return localeCode;
    }
    
    /**
     * @brief Gets the Application package name.
     * 
     * @param context Application Context
     * 
     * @return Package name of the application
     */
    public static String getApplicationPackageName(Context context) {
        if (context == null) {
            return null;
        }
        
        return context.getPackageName();
    }
    
    /**
     * @brief Gets the Application version.
     *
     * @param context Application Context
     *
     * @return Version of the application in string
     */
    public static String getApplicationVersion(Context context) {
        if (context == null) {
            return null;
        }
        String appVersion = null;

        try {
            appVersion =  context.getPackageManager().getPackageInfo(getApplicationPackageName(context), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return appVersion;
    }

    /**
     * @brief Gets the Application install date using the package manager.
     * 
     * @param context Application Context
     * @param packageName Package name
     * 
     * @return Time in millisecond of the the last install date
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
     * @brief Forcibly dismisses the Softkeyboard.
     * 
     * @param activity Valid activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // activity.getCurrentFocus() returns null at some cases on Android P
            // Force dismiss keyboard
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    /**
     * @brief Gets the Screen Dimensions of the Device.
     * 
     * @param activity Valid activity
     * 
     * @return Screen dimensions
     */
    public static Point getScreenDimensions(Activity activity) {
        if (activity == null) {
            return null;
        }

        Point size = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            WindowInsets windowInsets = metrics.getWindowInsets();
            Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() | WindowInsets.Type.displayCutout());
            Rect bounds = metrics.getBounds();

            size.x = bounds.width() - (insets.right + insets.left);
            size.y = bounds.height() - (insets.top + insets.bottom);
        } else {
            // Retain this part to support API below 30
            size = getScreenDimensionsForSDK29(activity, size);
        }

        return size;
    }

    /**
     * @brief Gets the Screen Dimensions of the Device for API 29 and below.
     *
     * @param activity Valid activity
     * @param size Point
     *
     * @return Screen dimensions
     */
    @SuppressWarnings("deprecation")
    public static Point getScreenDimensionsForSDK29(Activity activity, Point size) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        return size;
    }

    /**
     * @brief Get file contents from assets.
     * 
     * @param context Valid Context
     * @param assetFile Relative path of the asset file (from assets/)
     * 
     * @return File contents
     * @retval null Error in reading asset file
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
     * @brief Checks whether the asset exists.
     * 
     * @param context Valid Context
     * @param assetFile Relative path of the asset (from assets/)
     * 
     * @retval true The asset exists
     * @retval false The asset does not exist
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
            Logger.logWarn(AppUtils.class, "assetExists failed: " + e.toString());
        } catch (IOException e) {
            Logger.logWarn(AppUtils.class, "assetExists failed: " + e.toString());
        }
        return assetOk;
    }
    
    /**
     * @brief Gets the relative localized path.
     * 
     * @param context Valid Context
     * @param folder Directory of the file for localization
     * @param resource File to be opened
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
     * @brief Gets the full localized path.
     * 
     * @param context Valid Context
     * @param folder Directory of the file for localization
     * @param resource File to be opened
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
    
    /**
     * @brief Change children font <br>
     * 
     * Note: Known issue on Jellybean ellipsize="middle" when using custom font.
     * Based on: http://stackoverflow.com/questions/2711858/is-it-possible-to-set-font-for-entire-application
     * 
     * @param v ViewGroup to be changed
     * @param font Font to be set
     */
    protected static void changeChildrenFont(ViewGroup v, Typeface font) {
        if (font == null || v == null) {
            return;
        }
        
        for (int i = 0; i < v.getChildCount(); i++) {
            
            // For the ViewGroup, we'll have to use recursion
            if (v.getChildAt(i) instanceof ViewGroup) {
                changeChildrenFont((ViewGroup) v.getChildAt(i), font);
            } else {
                try {
                    Object[] nullArgs = null;
                    // Test whether setTypeface and getTypeface methods exists
                    Method methodTypeFace = v.getChildAt(i).getClass().getMethod("setTypeface", Typeface.class, Integer.TYPE);
                    // With getTypeface we'll get back the style (Bold, Italic...) set in XML
                    Method methodGetTypeFace = v.getChildAt(i).getClass().getMethod("getTypeface");
                    Typeface typeFace = ((Typeface) methodGetTypeFace.invoke(v.getChildAt(i)));
                    // Invoke the method and apply the new font with the defined style to the view if the method exists
                    // (textview,...)
                    methodTypeFace.invoke(v.getChildAt(i), font, typeFace == null ? 0 : typeFace.getStyle());
                }
                // Will catch the view with no such methods (listview...)
                catch (NoSuchMethodException e) {
                    Logger.logWarn(AppUtils.class, "NoSuchMethodException (setTypeface and getTypeface)");
                } catch (IllegalAccessException e) {
                    Logger.logWarn(AppUtils.class, "IllegalAccessException, on invoke");
                } catch (IllegalArgumentException e) {
                    Logger.logWarn(AppUtils.class, "IllegalArgumentException on invoke");
                } catch (InvocationTargetException e) {
                    Logger.logWarn(AppUtils.class, "InvocationTargetException on invoke");
                }
            }
        }
    }
    
    /**
     * @brief Dynamically retrieve resource Id.
     * 
     * Based on: http://daniel-codes.blogspot.jp/2009/12/dynamically-retrieving-resources-in.html
     * 
     * @param variableName Variable name
     * @param c Resource class
     * @param defaultId Default resource ID
     * 
     * @return Resource ID
     */
    public static int getResourceId(String variableName, Class<?> c, int defaultId) {
        if (variableName == null || c == null) {
            return defaultId;
        }
        
        int id = defaultId;
        try {
            Field idField = c.getDeclaredField(variableName);
            id = idField.getInt(idField);
        } catch (NoSuchFieldException e) {
            Logger.logWarn(AppUtils.class, "No id on class");
        } catch (IllegalAccessException e) {
            Logger.logWarn(AppUtils.class, "IllegalAccessException on getInt");
        } catch (IllegalArgumentException e) {
            Logger.logWarn(AppUtils.class, "IllegalArgumentException on getInt");
        }
        
        return id;
    }
    
    /**
     * @brief Get fit to aspect ratio size
     * 
     * @param srcWidth Source width
     * @param srcHeight Source height
     * @param destWidth Destination width
     * @param destHeight Destination height
     * 
     * @return New width and height
     */
    public static int[] getFitToAspectRatioSize(float srcWidth, float srcHeight, int destWidth, int destHeight) {
        float ratioSrc = srcWidth / srcHeight;
        float ratioDest = (float) destWidth / destHeight;
        
        int newWidth = 0;
        int newHeight = 0;
        
        if (ratioDest > ratioSrc) {
            newHeight = destHeight;
            newWidth = (int) (destHeight * ratioSrc);
            
        } else {
            newWidth = destWidth;
            newHeight = (int) (destWidth / ratioSrc);
        }
        
        return new int[] { newWidth, newHeight };
    }
    
    /**
     * @brief Gets the next integer multiple
     * 
     * @param n First Factor
     * @param m Second Factor
     * 
     * @return Next multiple of m and n 
     */
    public static int getNextIntegerMultiple(int n, int m) {
        if (m == 0) {
            Logger.logWarn(AppUtils.class, "Cannot divide by 0");
            return n;
        }
        
        if (n % m != 0) {
            return n + (m - n % m);
        }
        
        return n;
    }
    
    /**
     * @brief Checks if x and y is inside the view coordinates
     * 
     * @param view View to check
     * @param x MotionEvent.getRawX();
     * @param y MotionEvent.getRawY();
     * 
     * @retval true x and y is inside the View.
     * @retval false x and y is outside the View.
     */
    public static boolean checkViewHitTest(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        
        Rect r = new Rect();
        int[] coords = new int[2];
        view.getHitRect(r);
        view.getLocationOnScreen(coords);
        r.offset(coords[0] - view.getLeft(), coords[1] - view.getTop());
        if (r.contains(x, y)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * @brief Gets Secure Print, Login ID and PIN Code from preferences and returns formatted string
     * 
     * @return Authentication String
     */
    public static String getAuthenticationString() {
        StringBuffer strBuf = new StringBuffer();
        final String pinCodeFormat = "%s=%s\n";
        final String loginIdFormat = "%s=%s\n";
        final String securePrintFormat = "%s=%d\n";
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        boolean isSecurePrint = prefs.getBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT);
        String loginId = prefs.getString(AppConstants.PREF_KEY_LOGIN_ID, AppConstants.PREF_DEFAULT_LOGIN_ID);
        String pinCode = prefs.getString(AppConstants.PREF_KEY_AUTH_PIN_CODE, AppConstants.PREF_DEFAULT_AUTH_PIN_CODE);
        
        strBuf.append(String.format(Locale.getDefault(), securePrintFormat, AppConstants.KEY_SECURE_PRINT, isSecurePrint ? 1 : 0));
        strBuf.append(String.format(Locale.getDefault(), loginIdFormat, AppConstants.KEY_LOGINID, loginId));
        if (isSecurePrint) {
            strBuf.append(String.format(Locale.getDefault(), pinCodeFormat, AppConstants.KEY_PINCODE, pinCode));
        } else {
            strBuf.append(String.format(Locale.getDefault(), pinCodeFormat, AppConstants.KEY_PINCODE, ""));
        }
        
        return strBuf.toString();
    }
    
    /**
     * @brief Get the owner name based on the Log-in ID as seen from the Settings screen.
     * 
     * The Log-in ID is retrieved from the shared preferences.
     * 
     * @return Owner name
     */
    public static String getOwnerName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        return prefs.getString(AppConstants.PREF_KEY_LOGIN_ID, AppConstants.PREF_DEFAULT_LOGIN_ID);
    }
}
