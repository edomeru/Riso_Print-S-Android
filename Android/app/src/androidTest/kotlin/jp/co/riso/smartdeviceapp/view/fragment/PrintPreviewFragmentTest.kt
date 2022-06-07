package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import junit.framework.TestCase
import org.hamcrest.Matchers.not
import org.junit.*

class PrintPreviewFragmentTest : BaseActivityTestUtil() {

    private var _printPreviewFragment: PrintPreviewFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _printer: Printer? = null

    @get:Rule
    var storagePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun initEspresso() {
        Intents.init()

        wakeUpScreen()
        initPrinter()
        initFragment()
    }

    @After
    fun releaseEspresso() {
        Intents.release()
        _printPreviewFragment = null
        _printerManager = null
        _printer = null
    }

    private fun initFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintPreviewFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val fragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(fragment is PrintPreviewFragment)
        _printPreviewFragment = fragment as PrintPreviewFragment
    }

    private fun initPrinter() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)
        _printer = Printer(TEST_PRINTER_NAME, TEST_PRINTER_ADDRESS)
        if (!_printerManager!!.isExists(_printer)) {
            _printerManager!!.savePrinterToDB(_printer, true)
        }
        for (printerItem in _printerManager!!.savedPrintersList) {
            if (printerItem!!.ipAddress.contentEquals(TEST_PRINTER_ADDRESS)) {
                _printer = printerItem
                break
            }
        }
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printPreviewFragment)
    }

    @Test
    fun testPrintSettings_NoPrinter() {
        for (printerItem in _printerManager!!.savedPrintersList) {
            _printerManager!!.removePrinter(printerItem)
        }

        testClick(R.id.view_id_print_button)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintPreviewFragment.TAG_MESSAGE_DIALOG
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        val titleId = mainActivity!!.resources.getIdentifier("alertTitle", "id", "android")
        val title = dialog!!.findViewById<View>(titleId)
        val msg = dialog.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_print_settings),
            (title as TextView).text
        )
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_err_msg_no_selected_printer),
            (msg as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

    @Test
    fun testPrintSettings_WithPrinter() {
        testClick(R.id.view_id_print_button)
        val rightFragment = mainActivity!!.supportFragmentManager.findFragmentById(R.id.rightLayout)
        Assert.assertTrue(rightFragment is PrintSettingsFragment)
        pressBack()
        waitForAnimation()
        onView(withId(R.id.rightLayout))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.mainLayout))
            .check(matches(isCompletelyDisplayed()))
        val mainFragment = mainActivity!!.supportFragmentManager.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(mainFragment is PrintPreviewFragment)
        var ret: Boolean = _printerManager!!.removePrinter(_printer)
        TestCase.assertEquals(true, ret)
        ret = _printerManager!!.removePrinter(_printer)
        TestCase.assertEquals(false, ret)
    }

    @Test
    fun testOrientationChange() {
        switchOrientation()
        waitForAnimation()
        testPrintSettings_NoPrinter()

        switchOrientation()
        waitForAnimation()
        testPrintSettings_NoPrinter()
    }

    companion object {
        private const val TEST_PRINTER_NAME = "RISO IS1000C-G"
        private const val TEST_PRINTER_ADDRESS = "192.168.0.1"
    }
}