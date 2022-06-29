package jp.co.riso.smartdeviceapp.view.fragment

import android.view.Gravity
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrinterSearchFragmentTest : BaseActivityTestUtil() {
    private var _printerSearchFragment: PrinterSearchFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _existingPrinter: Printer? = null
    private var _newPrinter: Printer? = null

    @Before
    fun setup() {
        initPrinter()
        initPrinterSearchFragment()
    }

    @After
    fun cleanUp() {
        clearPrintersList()
        _printerSearchFragment = null
        _printerManager = null
        _existingPrinter = null
        _newPrinter = null
    }

    private fun initPrinter() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)
        _existingPrinter = TEST_PRINTER_ONLINE
        if (!_printerManager!!.isExists(_existingPrinter)) {
            _printerManager!!.savePrinterToDB(_existingPrinter, true)
        }
    }

    private fun initPrinterSearchFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintersFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        testClickAndWait(R.id.menu_id_action_search_button)

        var layoutId = R.id.mainLayout
        if (mainActivity!!.isTablet) {
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }

        val printerSearchFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(printerSearchFragment is PrinterSearchFragment)
        _printerSearchFragment = printerSearchFragment as PrinterSearchFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printerSearchFragment)
    }
}