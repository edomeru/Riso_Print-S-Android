package jp.co.riso.smartdeviceapp.view.fragment

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrinterInfoFragmentTest : BaseActivityTestUtil() {

    private var _printersFragment: PrintersFragment? = null
    private var _printerInfoFragment: PrinterInfoFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _printer: Printer? = null

    @Before
    fun setup() {
        Intents.init()
        wakeUpScreen()
        initPrinter()
        initFragment()
    }

    @After
    fun cleanUp() {
        Intents.release()
        clearPrintersList()
        _printersFragment = null
        _printerInfoFragment = null
        _printerManager = null
        _printer = null
    }

    private fun initPrinter() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)
        _printer = TEST_PRINTER_ONLINE
        if (!_printerManager!!.isExists(_printer)) {
            _printerManager!!.savePrinterToDB(_printer, true)
        }
    }

    private fun initFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintersFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // for phone devices
        if (!mainActivity!!.isTablet) {
            getViewInteractionFromMatchAtPosition(
                ViewMatchers.withText(_printer!!.ipAddress), 0
            ).perform(ViewActions.click())
            waitForAnimation()
            val printerInfoFragment = fm.findFragmentById(R.id.mainLayout)
            Assert.assertTrue(printerInfoFragment is PrinterInfoFragment)
            _printerInfoFragment = printerInfoFragment as PrinterInfoFragment?
        } else {
            val printersFragment = fm.findFragmentById(R.id.mainLayout)
            Assert.assertTrue(printersFragment is PrintersFragment)
            _printersFragment = printersFragment as PrintersFragment?
        }
    }

    @Test
    fun testNewInstance() {
        if (!mainActivity!!.isTablet) {
            // for phone devices
            Assert.assertNotNull(_printerInfoFragment)
        } else {
            Assert.assertNotNull(_printersFragment)
        }
    }

    @Test
    fun testConfigurationChange() {
        switchOrientation()
        waitForAnimation()
        switchOrientation()
    }

    @Test
    fun testBackButton() {
        if (!mainActivity!!.isTablet) {
            // for phone devices
            testClickAndWait(R.id.menu_id_back_button)
        }
    }

    @Test
    fun testClickDefaultPrintSettingsButton() {
        testClickAndWait(R.id.menu_id_action_print_settings_button)
        pressBack()
    }
}