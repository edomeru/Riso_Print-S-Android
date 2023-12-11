package jp.co.riso.android.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Typeface
import android.os.Build
import android.os.LocaleList
import android.view.View
import android.widget.*
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.AppUtils.assetExists
import jp.co.riso.android.util.AppUtils.authenticationString
import jp.co.riso.android.util.AppUtils.changeChildrenFont
import jp.co.riso.android.util.AppUtils.checkViewHitTest
import jp.co.riso.android.util.AppUtils.createActivityIntent
import jp.co.riso.android.util.AppUtils.getApplicationLastInstallDate
import jp.co.riso.android.util.AppUtils.getApplicationPackageName
import jp.co.riso.android.util.AppUtils.getFileContentsFromAssets
import jp.co.riso.android.util.AppUtils.getFitToAspectRatioSize
import jp.co.riso.android.util.AppUtils.getLocalizedAssetFullPath
import jp.co.riso.android.util.AppUtils.getLocalizedAssetRelativePath
import jp.co.riso.android.util.AppUtils.getNextIntegerMultiple
import jp.co.riso.android.util.AppUtils.getResourceId
import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.android.util.AppUtils.localeCode
import jp.co.riso.android.util.AppUtils.ownerName
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartprint.test.R.string
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import java.util.*

class AppUtilsTest : BaseActivityTestUtil() {
    private var _appFont: Typeface? = null

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
        _appFont = Typeface.DEFAULT
    }

    //================================================================================
    // Tests - constructors
    //================================================================================
    @Test
    fun testConstructor() {
        val appUtils = AppUtils
        TestCase.assertNotNull(appUtils)
    }

    //================================================================================
    // Tests - createActivityIntent
    //================================================================================
    @Test
    fun testCreateActivityIntent_ValidContextAndClass() {
        val testIntent: Intent? = createActivityIntent(mainActivity, MainActivity::class.java)
        TestCase.assertNotNull(testIntent)
    }

    @Test
    fun testCreateActivityIntent_NullContextOrClass() {

        // Both null
        var testIntent: Intent? = createActivityIntent(null, null)
        TestCase.assertNull(testIntent)

        // Context is null
        testIntent = createActivityIntent(null, MainActivity::class.java)
        TestCase.assertNull(testIntent)

        // Class is null
        testIntent = createActivityIntent(InstrumentationRegistry.getInstrumentation().context, null)
        TestCase.assertNull(testIntent)
    }

    //================================================================================
    // Tests - getLocaleCode
    //================================================================================
    @Test
    fun testGetLocaleCode_Default() {
        Locale.setDefault(Locale.getDefault())
        val str = localeCode
        TestCase.assertEquals(2, str.length)
    }

    @Test
    fun testGetLocaleCode_EN() {
        Locale.setDefault(Locale.ENGLISH)
        var str = localeCode
        TestCase.assertEquals("en", str)
        Locale.setDefault(Locale.US)
        str = localeCode
        TestCase.assertEquals("en", str)
    }

    @Test
    fun testGetLocaleCode_FR() {
        Locale.setDefault(Locale.FRENCH)
        var str = localeCode
        TestCase.assertEquals("fr", str)
        Locale.setDefault(Locale.FRANCE)
        str = localeCode
        TestCase.assertEquals("fr", str)
    }

    @Test
    fun testGetLocaleCode_IT() {
        Locale.setDefault(Locale.ITALIAN)
        var str = localeCode
        TestCase.assertEquals("it", str)
        Locale.setDefault(Locale.ITALY)
        str = localeCode
        TestCase.assertEquals("it", str)
    }

    @Test
    fun testGetLocaleCode_DE() {
        Locale.setDefault(Locale.GERMAN)
        var str = localeCode
        TestCase.assertEquals("de", str)
        Locale.setDefault(Locale.GERMANY)
        str = localeCode
        TestCase.assertEquals("de", str)
    }

    @Test
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
    @Test
    fun testGetApplicationPackageName_ValidContext() {
        val packageName = getApplicationPackageName(mainActivity)
        TestCase.assertNotNull(packageName)
    }

    @Test
    fun testGetApplicationPackageName_NullContext() {
        val packageName = getApplicationPackageName(null)
        TestCase.assertNull(packageName)
    }

    //================================================================================
    // Tests - getApplicationLastInstallDate
    //================================================================================
    @Test
    fun testGetApplicationLastInstallDate_ValidContextAndPackageName() {
        val packageName = getApplicationPackageName(mainActivity)
        try {
            val result = getApplicationLastInstallDate(mainActivity, packageName)
            TestCase.assertFalse(result == 0L)
        } catch (e: PackageManager.NameNotFoundException) {
            TestCase.fail()
        }
    }

    @Test
    fun testGetApplicationLastInstallDate_NullContext() {
        val packageName = getApplicationPackageName(mainActivity)
        try {
            val result = getApplicationLastInstallDate(null, packageName)
            TestCase.assertTrue(result == 0L)
        } catch (e: PackageManager.NameNotFoundException) {
            TestCase.fail()
        }
    }

    @Test
    fun testGetApplicationLastInstallDate_InvalidPackageName() {
        val packageName = "this is an invalid package name"
        try {
            getApplicationLastInstallDate(mainActivity, packageName)
            TestCase.fail() // Should throw exception
        } catch (e: PackageManager.NameNotFoundException) {
        }
    }

    @Test
    fun testGetApplicationLastInstallDate_NullPackageName() {
        val packageName: String? = null
        try {
            getApplicationLastInstallDate(mainActivity, packageName)
            TestCase.fail() // Should throw exception
        } catch (e: PackageManager.NameNotFoundException) {
        }
    }

    //================================================================================
    // Tests - getScreenDimensions
    //================================================================================
    @Test
    fun testGetScreenDimensions_Valid() {
        val expected = Point()
        val display = mainActivity!!.windowManager.defaultDisplay
        display.getSize(expected)
        val size = getScreenDimensions(mainActivity)
        TestCase.assertEquals(expected.x, size!!.x)
        TestCase.assertEquals(expected.y, size.y)
    }

    @Test
    fun testGetScreenDimensions_ContextNull() {
        val size = getScreenDimensions(null)
        TestCase.assertNull(size)
    }

    //================================================================================
    // Tests - getFileContentsFromAssets
    //================================================================================
    @Test
    fun testGetFileContentsFromAssets_ContextNull() {
        val str = getFileContentsFromAssets(null, null)
        TestCase.assertNull(str)
    }

    @Test
    fun testGetFileContentsFromAssets_Valid() {
        val str = getFileContentsFromAssets(mainActivity, "db/SmartDeviceAppDB.sql")
        TestCase.assertNotNull(str)
    }

    @Test
    fun testGetFileContentsFromAssets_InvalidAsset() {
        val str = getFileContentsFromAssets(mainActivity, "db/non-existent.sql")
        TestCase.assertNull(str)
    }

    //================================================================================
    // Tests - assetExists
    //================================================================================
    @Test
    fun testAssetExists_Valid() {
        val isExists = assetExists(mainActivity, ASSET)
        TestCase.assertTrue(isExists)
    }

    @Test
    fun testAssetExists_ContextNull() {
        val isExists = assetExists(null, ASSET)
        TestCase.assertFalse(isExists)
    }

    @Test
    fun testAssetExists_PathNull() {
        val isExists = assetExists(mainActivity, null)
        TestCase.assertFalse(isExists)
    }

    @Test
    fun testAssetExists_PathEmptyString() {
        val isExists = assetExists(mainActivity, "")
        TestCase.assertFalse(isExists)
    }

    @Test
    fun testAssetExists_PathNotExisting() {
        val isExists = assetExists(mainActivity, INVALID_VAL)
        TestCase.assertFalse(isExists)
    }

    //================================================================================
    // Tests - getLocalizedAssetRelativePath
    //================================================================================
    @Test
    fun testGetLocalizedAssetRelativePath_Valid() {
        val localized = getLocalizedAssetRelativePath(mainActivity, FOLDER, RESOURCE)
        TestCase.assertEquals(RELATIVE_PATH, localized)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_Valid_ja() {
        var locale = Locale("ja")
        var res = mainActivity!!.resources
        var config = res.configuration
        config.setLocale(locale)
        var list = LocaleList(locale)
        LocaleList.setDefault(list)
        mainActivity!!.createConfigurationContext(config)
        val localized = getLocalizedAssetRelativePath(mainActivity, FOLDER, RESOURCE)
        TestCase.assertEquals(RELATIVE_PATH_JA, localized)

        // Revert to EN locale
        locale = Locale("en")
        res = mainActivity!!.resources
        config = res.configuration
        config.setLocale(locale)
        list = LocaleList(locale)
        LocaleList.setDefault(list)
        mainActivity!!.createConfigurationContext(config)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_Valid_missingLocale() {
        Locale.setDefault(Locale.KOREAN)
        val localized = getLocalizedAssetRelativePath(mainActivity, FOLDER, RESOURCE)
        TestCase.assertEquals(RELATIVE_PATH, localized)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_ContextNull() {
        val localized = getLocalizedAssetRelativePath(null, FOLDER, RESOURCE)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_FolderNull() {
        val localized = getLocalizedAssetRelativePath(mainActivity, null, RESOURCE)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_FolderEmptyString() {
        val localized = getLocalizedAssetRelativePath(mainActivity, "", RESOURCE)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_FolderNotExisting() {
        val localized = getLocalizedAssetRelativePath(mainActivity, INVALID_VAL, RESOURCE)
        TestCase.assertEquals(INVALID_FOLDER_PATH, localized)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_ResourceNull() {
        val localized = getLocalizedAssetRelativePath(mainActivity, FOLDER, null)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetRelativePath_ResourceEmptyString() {
        val localized = getLocalizedAssetRelativePath(mainActivity, FOLDER, "")
        TestCase.assertNull(localized)
    }

    //================================================================================
    // Tests - getLocalizedAssetFullPath
    //================================================================================
    @Test
    fun testGetLocalizedAssetFullPath_Valid() {
        val localized = getLocalizedAssetFullPath(mainActivity, FOLDER, RESOURCE)
        TestCase.assertEquals(FULL_PATH, localized)
    }

    @Test
    fun testGetLocalizedAssetFullPath_Valid_ja() {

        var locale = Locale("ja")
        var res = mainActivity!!.resources
        var config = res.configuration
        config.setLocale(locale)
        var list = LocaleList(locale)
        LocaleList.setDefault(list)
        mainActivity!!.createConfigurationContext(config)

        val localized = getLocalizedAssetFullPath(mainActivity, FOLDER, RESOURCE)
        TestCase.assertEquals(FULL_PATH_JA, localized)

        // Revert to EN locale
        locale = Locale("en")
        res = mainActivity!!.resources
        config = res.configuration
        config.setLocale(locale)
        list = LocaleList(locale)
        LocaleList.setDefault(list)
        mainActivity!!.createConfigurationContext(config)
    }

    @Test
    fun testGetLocalizedAssetFullPath_Valid_missingLocale() {
        Locale.setDefault(Locale.KOREAN)
        val localized = getLocalizedAssetFullPath(mainActivity, FOLDER, RESOURCE)
        TestCase.assertEquals(FULL_PATH, localized)
    }

    @Test
    fun testGetLocalizedAssetFullPath_ContextNull() {
        val localized = getLocalizedAssetFullPath(null, FOLDER, RESOURCE)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetFullPath_FolderNull() {
        val localized = getLocalizedAssetFullPath(mainActivity, null, RESOURCE)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetFullPath_FolderEmptyString() {
        val localized = getLocalizedAssetFullPath(mainActivity, "", RESOURCE)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetFullPath_FolderNotExisting() {
        val localized = getLocalizedAssetFullPath(mainActivity, INVALID_VAL, RESOURCE)
        TestCase.assertEquals(INVALID_FOLDER_FULLPATH, localized)
    }

    @Test
    fun testGetLocalizedAssetFullPath_ResourceNull() {
        val localized = getLocalizedAssetFullPath(mainActivity, FOLDER, null)
        TestCase.assertNull(localized)
    }

    @Test
    fun testGetLocalizedAssetFullPath_ResourceEmptyString() {
        val localized = getLocalizedAssetFullPath(mainActivity, FOLDER, "")
        TestCase.assertNull(localized)
    }

    //================================================================================
    // Tests - changeChildrenFont
    //================================================================================
    @Test
    fun testChangeChildrenFont_ValidViewGroupValidFont() {
        val ll = LinearLayout(mainActivity)
        val ll2 = LinearLayout(mainActivity)
        ll.addView(TextView(mainActivity))
        ll.addView(View(mainActivity))
        ll.addView(Spinner(mainActivity))
        ll.addView(EditText(mainActivity))
        ll.addView(Switch(mainActivity))
        ll.addView(ll2)
        var typeFace = TextView(mainActivity)
        typeFace.typeface = null
        ll.addView(typeFace)
        typeFace = TextView(mainActivity)
        typeFace.typeface = _appFont
        ll.addView(typeFace)
        ll2.addView(TextView(mainActivity))
        ll2.addView(View(mainActivity))
        ll2.addView(Spinner(mainActivity))
        ll2.addView(EditText(mainActivity))
        ll2.addView(Switch(mainActivity))
        changeChildrenFont(ll, _appFont)
    }

    @Test
    fun testChangeChildrenFont_NullViewGroupValidFont() {
        changeChildrenFont(null, _appFont)
    }

    @Test
    fun testChangeChildrenFont_ValidViewGroupNullFont() {
        val ll = LinearLayout(mainActivity)
        ll.addView(TextView(mainActivity))
        ll.addView(View(mainActivity))
        ll.addView(Spinner(mainActivity))
        ll.addView(EditText(mainActivity))
        ll.addView(Switch(mainActivity))
        changeChildrenFont(ll, null)
    }

    @Test
    fun testChangeChildrenFont_NullViewGroupNullFont() {
        changeChildrenFont(null, null)
    }

    @Test
    fun testChangeChildrenFont_InvalidAccess() {
        val ll = LinearLayout(mainActivity)
        ll.addView(MockClass(mainActivity))
        changeChildrenFont(ll, _appFont)
    }

    @Test
    fun testChangeChildrenFont_TypeFaceNull() {
        val ll = LinearLayout(mainActivity)
        val nullTypeFace = TextView(mainActivity)
        nullTypeFace.typeface = null
        ll.addView(nullTypeFace)
        changeChildrenFont(ll, _appFont)
    }

    //================================================================================
    // Tests - getResourceId
    //================================================================================
    @Test
    fun testGetResourceId_Valid() {
        val value = getResourceId("app_name", string::class.java, -1)
        TestCase.assertTrue(-1 != value)
    }

    @Test
    fun testGetResourceId_Null() {
        val value = getResourceId(null, null, -1)
        TestCase.assertEquals(-1, value)
    }

    @Test
    fun testGetResourceId_NullVariableName() {
        val value = getResourceId(null, string::class.java, -1)
        TestCase.assertEquals(-1, value)
    }

    @Test
    fun testGetResourceId_NullClass() {
        val value = getResourceId("app_name", null, -1)
        TestCase.assertEquals(-1, value)
    }

    @Test
    fun testGetResourceId_InvalidClass() {
        val value = getResourceId("app_name", this.javaClass, -1)
        TestCase.assertEquals(-1, value)
    }

    @Test
    fun testGetResourceId_InvalidAccess() {
        val value = getResourceId("app_name", MockClass::class.java, -1)
        TestCase.assertEquals(-1, value)
    }

    @Test
    fun testGetResourceId_InvalidArgumentAccess() {
        val value = getResourceId("app_name_2", MockClass::class.java, -1)
        TestCase.assertEquals(-1, value)
    }

    //================================================================================
    // Tests - getFitToAspectRatioSize
    //================================================================================
    @Test
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

    @Test
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

    @Test
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

    @Test
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
    @Test
    fun testGetNextIntegerMultiple_Valid() {
        var `val`: Int = getNextIntegerMultiple(12, 2)
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

    @Test
    fun testGetNextIntegerMultiple_Invalid() {
        val `val`: Int = getNextIntegerMultiple(-3, 0)
        TestCase.assertEquals(-3, `val`)
    }

    //================================================================================
    // Test getAuthenticationString
    //================================================================================
    @Test
    fun testGetAuthenticationString_Valid() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext!!)
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

    @Test
    fun testGetAuthenticationString_Invalid() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext!!)
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
    @Test
    fun testGetOwnerName_Valid() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext!!)
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

    @Test
    fun testGetApplicationVersion_NullContext() {
        val ret = AppUtils.getApplicationVersion(null)
        TestCase.assertNull(ret)
    }

    @Test
    fun testGetApplicationVersion_ValidContext() {
        val ret = AppUtils.getApplicationVersion(SmartDeviceApp.appContext!!)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testCheckViewHitTest_NullContext() {
        val ret = checkViewHitTest(null, 100, 100)
        TestCase.assertFalse(ret)
    }

    @Test
    fun testCheckViewHitTest_ValidContext() {
        val ll = LinearLayout(mainActivity)
        val ret = checkViewHitTest(ll, 100, 100)
        TestCase.assertFalse(ret)
    }

    //================================================================================
    // Mock Classes
    //================================================================================
    inner class MockClass(context: Context?) : View(context)

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