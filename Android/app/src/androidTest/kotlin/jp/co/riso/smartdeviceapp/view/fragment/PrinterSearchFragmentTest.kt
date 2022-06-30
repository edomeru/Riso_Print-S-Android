package jp.co.riso.smartdeviceapp.view.fragment

import android.content.Intent
import android.view.Gravity
import android.view.View
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAction
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity
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
        _existingPrinter = TEST_PRINTER_ONLINE2
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

    @Test
    fun testSaveInstanceState() {
        val intent = Intent(mainActivity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = null
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        mainActivity!!.startActivity(intent)
    }

    @Test
    fun testBackButton() {
        if (!_printerSearchFragment!!.isTablet) {
            testClickAndWait(R.id.menu_id_back_button)
        } else {
            pressBack()
        }
    }

    @Test
    fun testPrint_NoNetwork() {
        if (!_printerSearchFragment!!.isTablet) {
            testClickAndWait(R.id.menu_id_back_button)
        } else {
            pressBack()
        }

        // disable wifi
        NetUtils.unregisterWifiCallback(mainActivity!!)

        waitForAnimation()
        testClickAndWait(R.id.menu_id_action_search_button)

        checkDialog(
            PrinterSearchFragment.KEY_PRINTER_ERR_DIALOG,
            R.string.ids_err_msg_network_error
        )
    }

//    @Test
//    fun testRefreshPrinterSearchScreen() {
//        onView(withId(R.id.printer_list))
//            .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(85)));
//    }
//
//    private fun withCustomConstraints(action: ViewAction, constraints: Matcher<View>): ViewAction {
//        return object : ViewAction {
//            override fun getConstraints(): Matcher<View> {
//                return constraints
//            }
//
//            override fun getDescription(): String {
//                return action.description
//            }
//
//            override fun perform(uiController: UiController?, view: View?) {
//                action.perform(uiController, view)
//            }
//        }
//    }

    @Test
    fun testSaveButtonExistingPrinter() {
        // Search for printers
        waitForAnimation()

        // New IP address
        testClickAndWait(R.id.addPrinterButton)

        checkDialog(
            AddPrinterFragment.KEY_ADD_PRINTER_DIALOG,
            R.string.ids_err_msg_cannot_add_printer
        )

        pressBack()
        waitForAnimation()

        // Go to printer search screen again
        testClickAndWait(R.id.menu_id_action_search_button)
        waitForAnimation()

        // try to click existing printer
        testClickAndWait(R.id.addPrinterButton)
    }

    @Test
    fun testSaveButtonNewPrinter() {
        // Search for printers
        waitForAnimation()

        // New IP address
        testClickAndWait(R.id.addPrinterButton)

        checkDialog(
            AddPrinterFragment.KEY_ADD_PRINTER_DIALOG,
            R.string.ids_err_msg_cannot_add_printer
        )
    }
}