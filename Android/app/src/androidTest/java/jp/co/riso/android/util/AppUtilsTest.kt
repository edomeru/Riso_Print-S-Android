package jp.co.riso.android.util

import android.content.Context
import jp.co.riso.android.util.AppUtils.createActivityIntent
import jp.co.riso.android.util.AppUtils.localeCode
import jp.co.riso.android.util.AppUtils.getApplicationPackageName
import jp.co.riso.android.util.AppUtils.getApplicationLastInstallDate
import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.android.util.AppUtils.getFileContentsFromAssets
import jp.co.riso.android.util.AppUtils.assetExists
import jp.co.riso.android.util.AppUtils.getLocalizedAssetRelativePath
import jp.co.riso.android.util.AppUtils.getLocalizedAssetFullPath
import jp.co.riso.android.util.AppUtils.changeChildrenFont
import jp.co.riso.android.util.AppUtils.getResourceId
import jp.co.riso.android.util.AppUtils.getFitToAspectRatioSize
import jp.co.riso.android.util.AppUtils.getNextIntegerMultiple
import jp.co.riso.android.util.AppUtils.authenticationString
import jp.co.riso.android.util.AppUtils.ownerName
import android.test.ActivityInstrumentationTestCase2
import jp.co.riso.smartdeviceapp.view.MainActivity
import android.graphics.Typeface
import kotlin.Throws
import jp.co.riso.android.util.AppUtils
import android.content.Intent
import android.content.pm.PackageManager
import jp.co.riso.android.util.AppUtilsTest
import android.os.Build
import android.os.LocaleList
import jp.co.riso.android.util.AppUtilsTest.MockClass
import jp.co.riso.smartprint.test.R.string
import android.content.SharedPreferences
import android.graphics.Point
import android.view.View
import android.widget.*
import androidx.preference.PreferenceManager
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.AppConstants
import junit.framework.TestCase
import java.lang.Exception
import java.util.*

class AppUtilsTest : ActivityInstrumentationTestCase2<MainActivity> {
    val FONT_FILE = "fonts/Raleway/Raleway-Regular.ttf"
    var mAppFont: Typeface? = null

    constructor() : super(MainActivity::class.java) {}
    constructor(activityClass: Class<MainActivity?>?) : super(activityClass) {}

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        Locale.setDefault(Locale.US)
        mAppFont = Typeface.DEFAULT
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    //================================================================================
    // Tests - constructors
    //================================================================================
    fun testConstructor() {
        val appUtils = AppUtils
        TestCase.assertNotNull(appUtils)
    }

    //================================================================================
    // Tests - createActivityIntent
    //================================================================================
    fun testCreateActivityIntent_ValidContextAndClass() {
        val testIntent: Intent?
        testIntent = createActivityIntent(activity, MainActivity::class.java)
        TestCase.assertNotNull(testIntent)
    }

    fun testCreateActivityIntent_NullContextOrClass() {
        var testIntent: Intent?

        // Both null
        testIntent = createActivityIntent(null, null)
        TestCase.assertNull(testIntent)

        // Context is null
        testIntent = createActivityIntent(null, MainActivity::class.java)
        TestCase.assertNull(testIntent)

        // Class is null
        testIntent = createActivityIntent(instrumentation.context, null)
        TestCase.assertNull(testIntent)
    }

    //================================================================================
    // Tests - getLocaleCode
    //================================================================================
    fun testGetLocaleCode_Default() {
        Locale.setDefault(Locale.getDefault())
        val str = localeCode
        TestCase.assertEquals(2, str.length)
    }

    fun testGetLocaleCode_EN() {
        Locale.setDefault(Locale.ENGLISH)
        var str = localeCode
        TestCase.assertEquals("en", str)
        Locale.setDefault(Locale.US)
        str = localeCode
        TestCase.assertEquals("en", str)
    }

    fun testGetLocaleCode_FR() {
        Locale.setDefault(Locale.FRENCH)
        var str = localeCode
        TestCase.assertEquals("fr", str)
        Locale.setDefault(Locale.FRANCE)
        str = localeCode
        TestCase.assertEquals("fr", str)
    }

    fun testGetLocaleCode_IT() {
        Locale.setDefault(Locale.ITALIAN)
        var str = localeCode
        TestCase.assertEquals("it", str)
        Locale.setDefault(Locale.ITALY)
        str = localeCode
        TestCase.assertEquals("it", str)
    }

    fun testGetLocaleCode_DE() {
        Locale.setDefault(Locale.GERMAN)
        var str = localeCode
        TestCase.assertEquals("de", str)
        Locale.setDefault(Locale.GERMANY)
        str = localeCode
        TestCase.assertEquals("de", str)
    }

    fun testGetLocaleCode_JA() {
        Locale.setDefault(Locale.JAPANESE)
        var str = localeCode
        TestCase.assertEquals("ja", str)
        Locale.setDefault(Locale.JAPAN)
        str = localeCode
        TestCase.assertEquals("ja", str)
    }

    //================================================================================
    // Tests - getApplicationLastInstallDate
    //================================================================================
    fun testGetApplicationPackageName_ValidContext() {
        val packageName = getApplicationPackageName(activity)
        TestCase.assertNotNull(packageName)
    }

    fun testGetApplicationPackageName_NullContext() {
        val packageName = getApplicationPackageName(null)
        TestCase.assertNull(packageName)
    }

    //================================================================================
    // Tests - getApplicationLastInstallDate
    //================================================================================
    fun testGetApplicationLastInstallDate_ValidContextAndPackageName() {
        val packageName = getApplicationPackageName(activity)
        try {
            val result = getApplicationLastInstallDate(activity, packageName)
            TestCase.assertFalse(result == 0L)
        } catch (e: PackageManager.NameNotFoundException) {
            TestCase.fail()
        }
    }

    fun testGetApplicationLastInstallDate_NullContext() {
        val packageName = getApplicationPackageName(activity)
        try {
            val result = getApplicationLastInstallDate(null, packageName)
            TestCase.assertTrue(result == 0L)
        } catch (e: PackageManager.NameNotFoundException) {
            TestCase.fail()
        }
    }

    fun testGetApplicationLastInstallDate_InvalidPackageName() {
        val packageName = "this is an invalid package name"
        try {
            getApplicationLastInstallDate(activity, packageName)
            TestCase.fail() // Should throw exception
        } catch (e: PackageManager.NameNotFoundException) {
        }
    }

    fun testGetApplicationLastInstallDate_NullPackageName() {
        val packageName: String? = null
        try {
            getApplicationLastInstallDate(activity, packageName)
            TestCase.fail() // Should throw exception
        } catch (e: PackageManager.NameNotFoundException) {
        }
    }

    //================================================================================
    // Tests - getScreenDimensions
    //================================================================================
    fun testGetScreenDimensions_Valid() {
        val expected = Point()
        val display = activity!!.windowManager.defaultDisplay
        display.getSize(expected)
        val size = getScreenDimensions(activity)
        TestCase.assertEquals(expected.x, size!!.x)
        TestCase.assertEquals(expected.y, size.y)
    }

    fun testGetScreenDimensions_ContextNull() {
        val size = getScreenDimensions(null)
        TestCase.assertNull(size)
    }

    //================================================================================
    // Tests - getFileContentsFromAssets
    //================================================================================
    fun testGetFileContentsFromAssets_ContextNull() {
        val str = getFileContentsFromAssets(null, null)
        TestCase.assertNull(str)
    }

    fun testGetFileContentsFromAssets_Valid() {
        val str = getFileContentsFromAssets(activity, "db/SmartDeviceAppDB.sql")
        TestCase.assertNotNull(str)
    }

    fun testGetFileContentsFromAssets_InvalidAsset() {
        val str = getFileContentsFromAssets(activity, "db/non-existent.sql")
        TestCase.assertNull(str)
    }

    //================================================================================
    // Tests - assetExists
    //================================================================================
    fun testAssetExists_Valid() {
        val isExists = assetExists(activity, ASSET)
        TestCase.assertTrue(isExists)
    }

    fun testAssetExists_ContextNull() {
        val isExists = assetExists(null, ASSET)
        TestCase.assertFalse(isExists)
    }

    fun testAssetExists_PathNull() {
        val isExists = assetExists(activity, null)
        TestCase.assertFalse(isExists)
    }

    fun testAssetExists_PathEmptyString() {
        val isExists = assetExists(activity, "")
        TestCase.assertFalse(isExists)
    }

    fun testAssetExists_PathNotExisting() {
        val isExists = assetExists(activity, INVALID_VAL)
        TestCase.assertFalse(isExists)
    }

    //================================================================================
    // Tests - getLocalizedAssetRelativePath
    //================================================================================
    fun testGetLocalizedAssetRelativePath_Valid() {
        val localized = getLocalizedAssetRelativePath(activity, FOLDER, RESOURCE)
        TestCase.assertEquals(RELATIVE_PATH, localized)
    }

    fun testGetLocalizedAssetRelativePath_Valid_ja() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Locale.setDefault(Locale.JAPANESE)
        } else {
            val locale = Locale("ja")
            val res = activity!!.resources
            val config = res.configuration
            config.setLocale(locale)
            val list = LocaleList(locale)
            LocaleList.setDefault(list)
            activity!!.createConfigurationContext(config)
        }
        val localized = getLocalizedAssetRelativePath(activity, FOLDER, RESOURCE)
        TestCase.assertEquals(RELATIVE_PATH_JA, localized)

        // Revert to EN locale
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            val locale = Locale("en")
            val res = activity!!.resources
            val config = res.configuration
            config.setLocale(locale)
            val list = LocaleList(locale)
            LocaleList.setDefault(list)
            activity!!.createConfigurationContext(config)
        }
    }

    fun testGetLocalizedAssetRelativePath_Valid_missingLocale() {
        Locale.setDefault(Locale.KOREAN)
        val localized = getLocalizedAssetRelativePath(activity, FOLDER, RESOURCE)
        TestCase.assertEquals(RELATIVE_PATH, localized)
    }

    fun testGetLocalizedAssetRelativePath_ContextNull() {
        val localized = getLocalizedAssetRelativePath(null, FOLDER, RESOURCE)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetRelativePath_FolderNull() {
        val localized = getLocalizedAssetRelativePath(activity, null, RESOURCE)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetRelativePath_FolderEmptyString() {
        val localized = getLocalizedAssetRelativePath(activity, "", RESOURCE)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetRelativePath_FolderNotExisting() {
        val localized = getLocalizedAssetRelativePath(activity, INVALID_VAL, RESOURCE)
        TestCase.assertEquals(INVALID_FOLDER_PATH, localized)
    }

    fun testGetLocalizedAssetRelativePath_ResourceNull() {
        val localized = getLocalizedAssetRelativePath(activity, FOLDER, null)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetRelativePath_ResourceEmptyString() {
        val localized = getLocalizedAssetRelativePath(activity, FOLDER, "")
        TestCase.assertNull(localized)
    }

    //================================================================================
    // Tests - getLocalizedAssetFullPath
    //================================================================================
    fun testGetLocalizedAssetFullPath_Valid() {
        val localized = getLocalizedAssetFullPath(activity, FOLDER, RESOURCE)
        TestCase.assertEquals(FULL_PATH, localized)
    }

    fun testGetLocalizedAssetFullPath_Valid_ja() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Locale.setDefault(Locale.JAPANESE)
        } else {
            val locale = Locale("ja")
            val res = activity!!.resources
            val config = res.configuration
            config.setLocale(locale)
            val list = LocaleList(locale)
            LocaleList.setDefault(list)
            activity!!.createConfigurationContext(config)
        }
        val localized = getLocalizedAssetFullPath(activity, FOLDER, RESOURCE)
        TestCase.assertEquals(FULL_PATH_JA, localized)

        // Revert to EN locale
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            val locale = Locale("en")
            val res = activity!!.resources
            val config = res.configuration
            config.setLocale(locale)
            val list = LocaleList(locale)
            LocaleList.setDefault(list)
            activity!!.createConfigurationContext(config)
        }
    }

    fun testGetLocalizedAssetFullPath_Valid_missingLocale() {
        Locale.setDefault(Locale.KOREAN)
        val localized = getLocalizedAssetFullPath(activity, FOLDER, RESOURCE)
        TestCase.assertEquals(FULL_PATH, localized)
    }

    fun testGetLocalizedAssetFullPath_ContextNull() {
        val localized = getLocalizedAssetFullPath(null, FOLDER, RESOURCE)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetFullPath_FolderNull() {
        val localized = getLocalizedAssetFullPath(activity, null, RESOURCE)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetFullPath_FolderEmptyString() {
        val localized = getLocalizedAssetFullPath(activity, "", RESOURCE)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetFullPath_FolderNotExisting() {
        val localized = getLocalizedAssetFullPath(activity, INVALID_VAL, RESOURCE)
        TestCase.assertEquals(INVALID_FOLDER_FULLPATH, localized)
    }

    fun testGetLocalizedAssetFullPath_ResourceNull() {
        val localized = getLocalizedAssetFullPath(activity, FOLDER, null)
        TestCase.assertNull(localized)
    }

    fun testGetLocalizedAssetFullPath_ResourceEmptyString() {
        val localized = getLocalizedAssetFullPath(activity, FOLDER, "")
        TestCase.assertNull(localized)
    }

    //================================================================================
    // Tests - changeChildrenFont
    //================================================================================
    fun testChangeChildrenFont_ValidViewGroupValidFont() {
        val ll = LinearLayout(activity)
        val ll2 = LinearLayout(activity)
        ll.addView(TextView(activity))
        ll.addView(View(activity))
        ll.addView(Spinner(activity))
        ll.addView(EditText(activity))
        ll.addView(Switch(activity))
        ll.addView(ll2)
        var typeFace = TextView(activity)
        typeFace.typeface = null
        ll.addView(typeFace)
        typeFace = TextView(activity)
        typeFace.typeface = mAppFont
        ll.addView(typeFace)
        ll2.addView(TextView(activity))
        ll2.addView(View(activity))
        ll2.addView(Spinner(activity))
        ll2.addView(EditText(activity))
        ll2.addView(Switch(activity))
        changeChildrenFont(ll, mAppFont)
    }

    fun testChangeChildrenFont_NullViewGroupValidFont() {
        changeChildrenFont(null, mAppFont)
    }

    fun testChangeChildrenFont_ValidViewGroupNullFont() {
        val ll = LinearLayout(activity)
        ll.addView(TextView(activity))
        ll.addView(View(activity))
        ll.addView(Spinner(activity))
        ll.addView(EditText(activity))
        ll.addView(Switch(activity))
        changeChildrenFont(ll, null)
    }

    fun testChangeChildrenFont_NullViewGroupNullFont() {
        changeChildrenFont(null, null)
    }

    fun testChangeChildrenFont_InvalidAccess() {
        val ll = LinearLayout(activity)
        ll.addView(MockClass(activity))
        changeChildrenFont(ll, mAppFont)
    }

    fun testChangeChildrenFont_TypeFaceNull() {
        val ll = LinearLayout(activity)
        val nullTypeFace = TextView(activity)
        nullTypeFace.typeface = null
        ll.addView(nullTypeFace)
        changeChildrenFont(ll, mAppFont)
    }

    //================================================================================
    // Tests - getResourceId
    //================================================================================
    fun testGetResourceId_Valid() {
        val value = getResourceId("app_name", string::class.java, -1)
        TestCase.assertTrue(-1 != value)
    }

    fun testGetResourceId_Null() {
        val value = getResourceId(null, null, -1)
        TestCase.assertEquals(-1, value)
    }

    fun testGetResourceId_NullVariableName() {
        val value = getResourceId(null, string::class.java, -1)
        TestCase.assertEquals(-1, value)
    }

    fun testGetResourceId_NullClass() {
        val value = getResourceId("app_name", null, -1)
        TestCase.assertEquals(-1, value)
    }

    fun testGetResourceId_InvalidClass() {
        val value = getResourceId("app_name", this.javaClass, -1)
        TestCase.assertEquals(-1, value)
    }

    fun testGetResourceId_InvalidAccess() {
        val value = getResourceId("app_name", MockClass::class.java, -1)
        TestCase.assertEquals(-1, value)
    }

    fun testGetResourceId_InvalidArgumentAccess() {
        val value = getResourceId("app_name_2", MockClass::class.java, -1)
        TestCase.assertEquals(-1, value)
    }

    //================================================================================
    // Tests - getFitToAspectRatioSize
    //================================================================================
    fun testGetFitToAspectRatioSize_SmallerSrc_WillFitToWidth() {
        val srcWidth = 20.0f
        val srcHeight = 10.0f
        val destWidth = 80
        val destHeight = 80
        val newDimensions = getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight)
        TestCase.assertTrue(newDimensions.size == 2)
        TestCase.assertEquals(
            newDimensions[0] / newDimensions[1].toFloat(),
            srcWidth / srcHeight,
            0.0001f
        )
        TestCase.assertEquals(80, newDimensions[0])
        TestCase.assertEquals(40, newDimensions[1])
    }

    fun testGetFitToAspectRatioSize_SmallerSrc_WillFitToHeight() {
        val srcWidth = 10.0f
        val srcHeight = 20.0f
        val destWidth = 80
        val destHeight = 80
        val newDimensions = getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight)
        TestCase.assertTrue(newDimensions.size == 2)
        TestCase.assertEquals(
            newDimensions[0] / newDimensions[1].toFloat(),
            srcWidth / srcHeight,
            0.0001f
        )
        TestCase.assertEquals(40, newDimensions[0])
        TestCase.assertEquals(80, newDimensions[1])
    }

    fun testGetFitToAspectRatioSize_BiggerSrc_WillFitToWidth() {
        val srcWidth = 400.0f
        val srcHeight = 200.0f
        val destWidth = 80
        val destHeight = 80
        val newDimensions = getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight)
        TestCase.assertTrue(newDimensions.size == 2)
        TestCase.assertEquals(
            newDimensions[0] / newDimensions[1].toFloat(),
            srcWidth / srcHeight,
            0.0001f
        )
        TestCase.assertEquals(80, newDimensions[0])
        TestCase.assertEquals(40, newDimensions[1])
    }

    fun testGetFitToAspectRatioSize_BiggerSrc_WillFitToHeight() {
        val srcWidth = 200.0f
        val srcHeight = 400.0f
        val destWidth = 80
        val destHeight = 80
        val newDimensions = getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight)
        TestCase.assertTrue(newDimensions.size == 2)
        TestCase.assertEquals(
            newDimensions[0] / newDimensions[1].toFloat(),
            srcWidth / srcHeight,
            0.0001f
        )
        TestCase.assertEquals(40, newDimensions[0])
        TestCase.assertEquals(80, newDimensions[1])
    }

    //================================================================================
    // Test getNextIntegerMultiple
    //================================================================================
    fun testGetNextIntegerMultiple_Valid() {
        var `val`: Int
        `val` = getNextIntegerMultiple(12, 2)
        TestCase.assertEquals(12, `val`)
        `val` = getNextIntegerMultiple(10, 4)
        TestCase.assertEquals(12, `val`)
        `val` = getNextIntegerMultiple(124, 3)
        TestCase.assertEquals(126, `val`)
        `val` = getNextIntegerMultiple(-3, 2)
        TestCase.assertEquals(0, `val`)
        `val` = getNextIntegerMultiple(-3, -6)
        TestCase.assertEquals(-6, `val`)
    }

    fun testGetNextIntegerMultiple_Invalid() {
        val `val`: Int
        `val` = getNextIntegerMultiple(-3, 0)
        TestCase.assertEquals(-3, `val`)
    }

    //================================================================================
    // Test getAuthenticationString
    //================================================================================
    fun testGetAuthenticationString_Valid() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
        val editor = prefs.edit()
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, false)
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test")
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "1234")
        editor.apply()
        TestCase.assertNotNull(authenticationString)
        TestCase.assertFalse(authenticationString.isEmpty())
        TestCase.assertTrue(authenticationString == "securePrint=0\nloginId=test\npinCode=\n")
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true) // secure print ON        
        editor.apply()
        TestCase.assertNotNull(authenticationString)
        TestCase.assertFalse(authenticationString.isEmpty())
        TestCase.assertTrue(authenticationString == "securePrint=1\nloginId=test\npinCode=1234\n")
    }

    fun testGetAuthenticationString_Invalid() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
        val editor = prefs.edit()
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true)
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test")
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "abcd") // pincode is not numeric
        editor.apply()
        TestCase.assertNotNull(authenticationString)
        TestCase.assertFalse(authenticationString.isEmpty())

        // missing keys
        editor.remove(AppConstants.PREF_KEY_AUTH_SECURE_PRINT)
        editor.remove(AppConstants.PREF_KEY_LOGIN_ID)
        editor.remove(AppConstants.PREF_KEY_AUTH_PIN_CODE)
        editor.apply()
        TestCase.assertNotNull(authenticationString)
        TestCase.assertFalse(authenticationString.isEmpty())
        TestCase.assertTrue(authenticationString == "securePrint=0\nloginId=\npinCode=\n")
    }

    //================================================================================
    // Test getOwnerName
    //================================================================================
    fun testGetOwnerName_Valid() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
        val editor = prefs.edit()
        val expected = "testLogin"
        editor.clear()
        editor.apply()
        var ownerName = ownerName
        TestCase.assertNotNull(ownerName)
        TestCase.assertTrue(ownerName!!.isEmpty())
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, expected)
        editor.apply()
        ownerName = AppUtils.ownerName
        TestCase.assertNotNull(ownerName)
        TestCase.assertFalse(ownerName!!.isEmpty())
        TestCase.assertEquals(expected, ownerName)
    }

    //================================================================================
    // Mock Classes
    //================================================================================
    inner class MockClass(context: Context?) : View(context) {
        protected var typeface: Typeface?
            protected get() = mAppFont
            protected set(tf) {}

//        companion object {
//            // Invoked
//            private const val app_name = 0x7f030000
//            const val app_name_2: Float = 0x7030000f
//        }
    }

    companion object {
        private const val ASSET = "html/help.html"
        private const val INVALID_VAL = "invalid"
        private const val FOLDER = "html"
        private const val RESOURCE = "help.html"
        private const val RELATIVE_PATH = "html/help.html"
        private const val RELATIVE_PATH_JA = "html-ja/help.html"
        private const val FULL_PATH = "file:///android_asset/html/help.html"
        private const val FULL_PATH_JA = "file:///android_asset/html-ja/help.html"
        private const val INVALID_FOLDER_PATH = "invalid/help.html"
        private const val INVALID_FOLDER_FULLPATH = "file:///android_asset/invalid/help.html"
    }
}