package jp.co.riso.smartdeviceapp.view.fragment

import android.view.Gravity
import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrinterSearchSettingsFragmentTest : BaseActivityTestUtil() {

    private var _printerSearchSettingsFragment: PrinterSearchSettingsFragment? = null

    @Before
    fun setup() {
        Intents.init()
        initPrinterSearchSettingsFragment()
    }

    @After
    fun cleanUp() {
        Intents.release()
        clearPrintersList()
        _printerSearchSettingsFragment = null
    }

    private fun initPrinterSearchSettingsFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintersFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        testClickAndWait(R.id.menu_id_printer_search_settings_button)

        var layoutId = R.id.mainLayout
        if (mainActivity!!.isTablet) {
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }

        val printerSearchSettingsFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(printerSearchSettingsFragment is PrinterSearchSettingsFragment)
        _printerSearchSettingsFragment = printerSearchSettingsFragment as PrinterSearchSettingsFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printerSearchSettingsFragment)
    }

    @Test
    fun testBackButton() {
        if (!_printerSearchSettingsFragment!!.isTablet) {
            testClickAndWait(R.id.menu_id_back_button)
        } else {
            pressBack()
        }
    }

    @Test
    fun testDefaultInput() {
        onView(withId(R.id.inputSnmpCommunityName)).perform(click())
        onView(withId(R.id.inputSnmpCommunityName)).perform(pressImeActionButton())
    }

    @Test
    fun testDefaultInputKeyEventActionUp() {
        onView(withId(R.id.inputSnmpCommunityName)).perform(click())
        onView(withId(R.id.inputSnmpCommunityName)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
    }

    @Test
    fun testEmptyInput() {
        onView(withId(R.id.inputSnmpCommunityName)).perform(click())
        onView(withId(R.id.inputSnmpCommunityName)).perform(clearText())
        if (!_printerSearchSettingsFragment!!.isTablet) {
            testClickAndWait(R.id.menu_id_back_button)
        } else {
            testClickAndWait(R.id.menu_id_printer_search_settings_button)
        }
        testClickAndWait(R.id.menu_id_printer_search_settings_button)
    }
}