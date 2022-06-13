package jp.co.riso.smartdeviceapp

import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmartDeviceAppTest {

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext!!)
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
        TestCase.assertNotNull(SmartDeviceApp.appContext!!)
    }
}