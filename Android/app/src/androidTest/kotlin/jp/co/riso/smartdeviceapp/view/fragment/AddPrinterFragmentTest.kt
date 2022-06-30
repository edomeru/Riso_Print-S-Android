package jp.co.riso.smartdeviceapp.view.fragment

import android.view.Gravity
import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
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

class AddPrinterFragmentTest : BaseActivityTestUtil() {

    private var _addPrinterFragment: AddPrinterFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _existingPrinter: Printer? = null
    private var _newPrinter: Printer? = null

    @Before
    fun setup() {
        initPrinter()
        initAddPrinterFragment()
    }

    @After
    fun cleanUp() {
        clearPrintersList()
        _addPrinterFragment = null
        _printerManager = null
        _existingPrinter = null
        _newPrinter = null
    }

    private fun initPrinter() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)
        _existingPrinter = TEST_PRINTER_ONLINE
        _newPrinter  = TEST_PRINTER_ONLINE2
        if (!_printerManager!!.isExists(_existingPrinter)) {
            _printerManager!!.savePrinterToDB(_existingPrinter, true)
        }
    }

    private fun initAddPrinterFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintersFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        testClickAndWait(R.id.menu_id_action_add_button)

        var layoutId = R.id.mainLayout
        if (mainActivity!!.isTablet) {
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }

        val addPrinterFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(addPrinterFragment is AddPrinterFragment)
        _addPrinterFragment = addPrinterFragment as AddPrinterFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_addPrinterFragment)
    }

    @Test
    fun testOnPrinterAdd() {
        // Existing printer
        _addPrinterFragment!!.onPrinterAdd(_existingPrinter)
        _addPrinterFragment!!.onSearchEnd()

        // Not yet exists
        _addPrinterFragment!!.onPrinterAdd(_newPrinter)
        _addPrinterFragment!!.onSearchEnd()
    }

    @Test
    fun testSaveButtonInvalidPrinter() {
        // Invalid IP address
        onView(withId(R.id.inputIpAddress)).perform(typeText("try"))
        testClickAndWait(R.id.img_save_button)

        checkDialog(
            AddPrinterFragment.KEY_ADD_PRINTER_DIALOG,
            R.string.ids_err_msg_invalid_ip_address
        )
    }

    @Test
    fun testSaveButtonBroadcastAddress() {
        // Invalid IP address
        onView(withId(R.id.inputIpAddress)).perform(typeText("255.255.255.255"))
        testClickAndWait(R.id.img_save_button)

        checkDialog(
            AddPrinterFragment.KEY_ADD_PRINTER_DIALOG,
            R.string.ids_err_msg_invalid_ip_address
        )
    }

    @Test
    fun testSaveButtonExistingPrinter() {
        // Existing IP address
        onView(withId(R.id.inputIpAddress)).perform(typeText(_existingPrinter!!.ipAddress))
        testClickAndWait(R.id.img_save_button)

        checkDialog(
            AddPrinterFragment.KEY_ADD_PRINTER_DIALOG,
            R.string.ids_err_msg_cannot_add_printer
        )
    }

    @Test
    fun testSaveButtonNewPrinter() {
        // New IP Address
        onView(withId(R.id.inputIpAddress)).perform(typeText(_newPrinter!!.ipAddress))
        testClickAndWait(R.id.img_save_button)

        checkDialog(
            AddPrinterFragment.KEY_ADD_PRINTER_DIALOG,
            R.string.ids_info_msg_printer_add_successful
        )
    }

    @Test
    fun testOnEditorAction() {
        onView(withId(R.id.inputIpAddress)).perform(click())
        onView(withId(R.id.inputIpAddress)).perform(pressImeActionButton())
    }

    @Test
    fun testOnKeyUpEnterFocused() {
        onView(withId(R.id.inputIpAddress)).perform(click())
        _addPrinterFragment!!.onKeyUp(KeyEvent.KEYCODE_ENTER)
    }

    @Test
    fun testOnKeyUpEnterNotFocused() {
        _addPrinterFragment!!.onKeyUp(KeyEvent.KEYCODE_ENTER)
    }

    @Test
    fun testOnKeyUpOthers() {
        _addPrinterFragment!!.onKeyUp(KeyEvent.KEYCODE_A)

        onView(withId(R.id.inputIpAddress)).perform(click())
        onView(withId(R.id.inputIpAddress)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
    }
}