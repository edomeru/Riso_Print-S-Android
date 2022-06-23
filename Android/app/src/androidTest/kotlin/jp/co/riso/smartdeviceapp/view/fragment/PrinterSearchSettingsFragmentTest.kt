package jp.co.riso.smartdeviceapp.view.fragment

import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrinterSearchSettingsFragmentTest : BaseActivityTestUtil() {

    private var _printerSearchSettingsFragment: PrinterSearchSettingsFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _existingPrinter: Printer? = null

    @Before
    fun setup() {
        Intents.init()
        initPrinter()
        initPrinterSearchSettingsFragment()
    }

    @After
    fun cleanUp() {
        Intents.release()
        clearPrintersList()
        _printerSearchSettingsFragment = null
        _printerManager = null
        _existingPrinter = null
    }

    private fun initPrinter() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)
        _existingPrinter = TEST_ONLINE_PRINTER
        if (!_printerManager!!.isExists(_existingPrinter)) {
            _printerManager!!.savePrinterToDB(_existingPrinter, true)
        }
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
        val printerSearchSettingsFragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(printerSearchSettingsFragment is PrinterSearchSettingsFragment)
        _printerSearchSettingsFragment = printerSearchSettingsFragment as PrinterSearchSettingsFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printerSearchSettingsFragment)
    }

    @Test
    fun testBackButton() {
        testClickAndWait(R.id.menu_id_back_button)
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
        testClickAndWait(R.id.menu_id_back_button)
        testClickAndWait(R.id.menu_id_printer_search_settings_button)
    }
}