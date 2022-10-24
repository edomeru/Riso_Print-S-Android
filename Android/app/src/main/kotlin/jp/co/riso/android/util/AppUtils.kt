/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * AppUtils.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * @class AppUtils
 *
 * @brief Utility class for application operations
 */
object AppUtils {
    /// Path to asset files
    private const val CONST_ASSET_PATH = "file:///android_asset/"

    /**
     * @brief Creates an activity intent launcher.
     *
     * @param context Application Context
     * @param cls Activity class
     *
     * @return Intent generated
     */
    @JvmStatic
    fun createActivityIntent(context: Context?, cls: Class<*>?): Intent? {
        // Should not be created if context or class is null
        if (context == null || cls == null) {
            return null
        }
        val intent = Intent()
        intent.setClass(context, cls)
        return intent
    }

    /**
     * @brief Gets the 2 character locale code based on the current Locale. (e.g., en, ja, etc)
     *
     * @return Locale Code String
     */
    @JvmStatic
    val localeCode: String
        get() {
            val defaultLocale = Locale.getDefault()
            return defaultLocale.toString().substring(0, 2).lowercase(defaultLocale)
        }

    /**
     * @brief Gets the Application package name.
     *
     * @param context Application Context
     *
     * @return Package name of the application
     */
    @JvmStatic
    fun getApplicationPackageName(context: Context?): String? {
        return context?.packageName
    }

    /**
     * @brief Gets the Application version.
     *
     * @param context Application Context
     *
     * @return Version of the application in string
     */
    @JvmStatic
    fun getApplicationVersion(context: Context?): String? {
        if (context == null) {
            return null
        }
        var appVersion: String? = null
        try {
            appVersion = context.getPackageInfo(getApplicationPackageName(context)!!).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersion
    }

    @Suppress("DEPRECATION")
    fun Context.getPackageInfo(applicationPackageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(applicationPackageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(applicationPackageName, 0)
        }
    }

    /**
     * @brief Gets the Application install date using the package manager.
     *
     * @param context Application Context
     * @param packageName Package name
     *
     * @return Time in millisecond of the the last install date
     */
    @JvmStatic
    @Throws(PackageManager.NameNotFoundException::class)
    fun getApplicationLastInstallDate(context: Context?, packageName: String?): Long {
        if (context == null) {
            return 0
        }
        val appInfo: ApplicationInfo = try {
            context.getApplicationInformation(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            throw e
        }
        val appFilePath = appInfo.sourceDir
        val appFile = File(appFilePath)
        return appFile.lastModified()
    }

    @Suppress("DEPRECATION")
    private fun Context.getApplicationInformation(packageName: String?): ApplicationInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(packageName.toString(), PackageManager.ApplicationInfoFlags.of(0))
        } else {
            packageManager.getApplicationInfo(packageName.toString(), 0)
        }
    }

    /**
     * @brief Forcibly dismisses the Softkeyboard.
     *
     * @param activity Valid activity
     */
    @JvmStatic
    fun hideSoftKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (activity.currentFocus != null) {
            imm!!.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // activity.getCurrentFocus() returns null at some cases on Android P
            // Force dismiss keyboard
            imm!!.hideSoftInputFromWindow(
                activity.window.decorView.rootView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /**
     * @brief Gets the Screen Dimensions of the Device.
     *
     * @param activity Valid activity
     *
     * @return Screen dimensions
     */
    @JvmStatic
    fun getScreenDimensions(activity: Activity?): Point? {
        if (activity == null) {
            return null
        }
        var size = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = activity.windowManager.currentWindowMetrics
            val windowInsets = metrics.windowInsets
            val insets =
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
            val bounds = metrics.bounds
            size.x = bounds.width() - (insets.right + insets.left)
            size.y = bounds.height() - (insets.top + insets.bottom)
        } else {
            // Retain this part to support API below 30
            size = getScreenDimensionsForSDK29(activity, size)
        }
        return size
    }

    /**
     * @brief Gets the Screen Dimensions of the Device for API 29 and below.
     *
     * @param activity Valid activity
     * @param size Point
     *
     * @return Screen dimensions
     */
    @Suppress("DEPRECATION")
    fun getScreenDimensionsForSDK29(activity: Activity, size: Point): Point {
        val display = activity.windowManager.defaultDisplay
        display.getSize(size)
        return size
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
    @JvmStatic
    fun getFileContentsFromAssets(context: Context?, assetFile: String?): String? {
        if (context == null) {
            return null
        }
        val buf = StringBuilder()
        val stream: InputStream
        try {
            stream = context.applicationContext.assets.open(assetFile!!)
            val `in` = BufferedReader(InputStreamReader(stream))
            var str: String?
            while (`in`.readLine().also { str = it } != null) {
                buf.append(str)
            }
            `in`.close()
        } catch (e: IOException) {
            return null
        }
        return buf.toString()
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
    @JvmStatic
    fun assetExists(context: Context?, assetFile: String?): Boolean {
        if (context == null || assetFile == null) {
            return false
        }
        var assetOk = false
        try {
            val stream = context.assets.open(assetFile)
            stream.close()
            assetOk = true
        } catch (e: IOException) {
            Logger.logWarn(AppUtils::class.java, "assetExists failed: $e")
        }
        return assetOk
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
    @JvmStatic
    fun getLocalizedAssetRelativePath(
        context: Context?,
        folder: String?,
        resource: String?
    ): String? {
        if (context == null || folder == null || resource == null) {
            return null
        }
        if (folder.isEmpty() || resource.isEmpty()) {
            return null
        }
        var relativePath = "$folder-$localeCode/$resource"
        val assetExists = assetExists(context, relativePath)
        if (!assetExists) {
            relativePath = "$folder/$resource"
        }
        return relativePath
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
    @JvmStatic
    fun getLocalizedAssetFullPath(context: Context?, folder: String?, resource: String?): String? {
        val relativePath = getLocalizedAssetRelativePath(context, folder, resource)
        return if (relativePath != null) {
            CONST_ASSET_PATH + relativePath
        } else null
    }

    /**
     * @brief Change children font <br></br>
     *
     * Note: Known issue on Jellybean ellipsize="middle" when using custom font.
     * Based on: http://stackoverflow.com/questions/2711858/is-it-possible-to-set-font-for-entire-application
     *
     * @param v ViewGroup to be changed
     * @param font Font to be set
     */
    @JvmStatic
    fun changeChildrenFont(v: ViewGroup?, font: Typeface?) {
        if (font == null || v == null) {
            return
        }
        for (i in 0 until v.childCount) {

            // For the ViewGroup, we'll have to use recursion
            if (v.getChildAt(i) is ViewGroup) {
                changeChildrenFont(v.getChildAt(i) as ViewGroup?, font)
            } else {
                try {
                    // Test whether setTypeface and getTypeface methods exists
                    val methodTypeFace = v.getChildAt(i).javaClass.getMethod(
                        "setTypeface",
                        Typeface::class.java,
                        Integer.TYPE
                    )
                    // With getTypeface we'll get back the style (Bold, Italic...) set in XML
                    val methodGetTypeFace = v.getChildAt(i).javaClass.getMethod("getTypeface")
                    val typeFace = methodGetTypeFace.invoke(v.getChildAt(i)) as Typeface?
                    // Invoke the method and apply the new font with the defined style to the view if the method exists
                    // (textview,...)
                    methodTypeFace.invoke(v.getChildAt(i), font, typeFace?.style ?: 0)
                } // Will catch the view with no such methods (listview...)
                catch (e: NoSuchMethodException) {
                    Logger.logWarn(
                        AppUtils::class.java,
                        "NoSuchMethodException (setTypeface and getTypeface)"
                    )
                } catch (e: IllegalAccessException) {
                    Logger.logWarn(AppUtils::class.java, "IllegalAccessException, on invoke")
                } catch (e: IllegalArgumentException) {
                    Logger.logWarn(AppUtils::class.java, "IllegalArgumentException on invoke")
                } catch (e: InvocationTargetException) {
                    Logger.logWarn(AppUtils::class.java, "InvocationTargetException on invoke")
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
    @JvmStatic
    fun getResourceId(variableName: String?, c: Class<*>?, defaultId: Int): Int {
        if (variableName == null || c == null) {
            return defaultId
        }
        var id = defaultId
        try {
            val idField = c.getDeclaredField(variableName)
            id = idField.getInt(idField)
        } catch (e: NoSuchFieldException) {
            Logger.logWarn(AppUtils::class.java, "No id on class")
        } catch (e: IllegalAccessException) {
            Logger.logWarn(AppUtils::class.java, "IllegalAccessException on getInt")
        } catch (e: IllegalArgumentException) {
            Logger.logWarn(AppUtils::class.java, "IllegalArgumentException on getInt")
        }
        return id
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
    @JvmStatic
    fun getFitToAspectRatioSize(
        srcWidth: Float,
        srcHeight: Float,
        destWidth: Int,
        destHeight: Int
    ): IntArray {
        val ratioSrc = srcWidth / srcHeight
        val ratioDest = destWidth.toFloat() / destHeight
        val newWidth: Int
        val newHeight: Int
        if (ratioDest > ratioSrc) {
            newHeight = destHeight
            newWidth = (destHeight * ratioSrc).toInt()
        } else {
            newWidth = destWidth
            newHeight = (destWidth / ratioSrc).toInt()
        }
        return intArrayOf(newWidth, newHeight)
    }

    /**
     * @brief Gets the next integer multiple
     *
     * @param n First Factor
     * @param m Second Factor
     *
     * @return Next multiple of m and n
     */
    @JvmStatic
    fun getNextIntegerMultiple(n: Int, m: Int): Int {
        if (m == 0) {
            Logger.logWarn(AppUtils::class.java, "Cannot divide by 0")
            return n
        }
        return if (n % m != 0) {
            n + (m - n % m)
        } else n
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
    @JvmStatic
    fun checkViewHitTest(view: View?, x: Int, y: Int): Boolean {
        if (view == null) {
            return false
        }
        val r = Rect()
        val coords = IntArray(2)
        view.getHitRect(r)
        view.getLocationOnScreen(coords)
        r.offset(coords[0] - view.left, coords[1] - view.top)
        return r.contains(x, y)
    }

    /**
     * @brief Gets Secure Print, Login ID and PIN Code from preferences and returns formatted string
     *
     * @return Authentication String
     */
    @JvmStatic
    val authenticationString: String
        get() {
            val strBuf = StringBuffer()
            val pinCodeFormat = "%s=%s\n"
            val loginIdFormat = "%s=%s\n"
            val securePrintFormat = "%s=%d\n"
            val prefs =
                PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
            val isSecurePrint = prefs.getBoolean(
                AppConstants.PREF_KEY_AUTH_SECURE_PRINT,
                AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT
            )
            val loginId =
                prefs.getString(AppConstants.PREF_KEY_LOGIN_ID, AppConstants.PREF_DEFAULT_LOGIN_ID)
            val pinCode = prefs.getString(
                AppConstants.PREF_KEY_AUTH_PIN_CODE,
                AppConstants.PREF_DEFAULT_AUTH_PIN_CODE
            )
            strBuf.append(
                String.format(
                    Locale.getDefault(),
                    securePrintFormat,
                    AppConstants.KEY_SECURE_PRINT,
                    if (isSecurePrint) 1 else 0
                )
            )
            strBuf.append(
                String.format(
                    Locale.getDefault(),
                    loginIdFormat,
                    AppConstants.KEY_LOGINID,
                    loginId
                )
            )
            if (isSecurePrint) {
                strBuf.append(
                    String.format(
                        Locale.getDefault(),
                        pinCodeFormat,
                        AppConstants.KEY_PINCODE,
                        pinCode
                    )
                )
            } else {
                strBuf.append(
                    String.format(
                        Locale.getDefault(),
                        pinCodeFormat,
                        AppConstants.KEY_PINCODE,
                        ""
                    )
                )
            }
            return strBuf.toString()
        }

    /**
     * @brief Get the owner name based on the Log-in ID as seen from the Settings screen.
     *
     * The Log-in ID is retrieved from the shared preferences.
     *
     * @return Owner name
     */
    @JvmStatic
    val ownerName: String?
        get() {
            val prefs =
                PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
            return prefs.getString(
                AppConstants.PREF_KEY_LOGIN_ID,
                AppConstants.PREF_DEFAULT_LOGIN_ID
            )
        }
}