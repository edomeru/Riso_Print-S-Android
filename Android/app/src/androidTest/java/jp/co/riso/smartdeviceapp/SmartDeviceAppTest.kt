package jp.co.riso.smartdeviceapp

import android.test.ApplicationTestCase
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import kotlin.Throws
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class SmartDeviceAppTest {

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        val edit = prefs.edit()
        edit.clear()
        edit.apply()
        val smartDeviceApp = SmartDeviceApp()
        TestCase.assertNotNull(smartDeviceApp)
    }

    // ================================================================================
    // Tests - getAppContext
    // ================================================================================
    @Test
    fun testGetAppContext() {
        TestCase.assertNotNull(SmartDeviceApp.getAppContext())
    }
}