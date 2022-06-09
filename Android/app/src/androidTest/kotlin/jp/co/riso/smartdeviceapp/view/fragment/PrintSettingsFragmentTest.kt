package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.*
import java.io.File

class PrintSettingsFragmentTest : BaseActivityTestUtil() {

    private var _printSettingsFragment: PrintSettingsFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _printersList: List<Printer?>? = null

    @get:Rule
    var storagePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
        Intents.init()

        wakeUpScreen()
        initPrinters()
        initFragment()
        initPreview()
    }

    @After
    fun cleanUp() {
        Intents.release()
        clearPrintersList(_printerManager!!)
        _printSettingsFragment = null
        _printerManager = null
        _printersList = null
    }

    private fun initFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintPreviewFragment()).commit()
            fm.executePendingTransactions()
        }
        waitForAnimation()
        testClick(R.id.view_id_print_button)
        val fragment = fm.findFragmentById(R.id.rightLayout)
        Assert.assertTrue(fragment is PrintSettingsFragment)
        _printSettingsFragment = fragment as PrintSettingsFragment?
    }

    private fun initPrinters() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)

        _printersList = mutableListOf(
            TEST_ONLINE_PRINTER,
            TEST_OFFLINE_PRINTER
        )

        for ((index, printer) in _printersList!!.withIndex()) {
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            }
            for (printerItem in _printerManager!!.savedPrintersList) {
                if (printerItem!!.ipAddress.contentEquals(_printersList!![index]!!.ipAddress)) {
                    _printersList!![index]!!.id = printerItem.id
                    break
                }
            }
        }
    }

    private fun initPreview() {
        /*
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout,HomeFragment()).commit()
            fm.executePendingTransactions()
        }*/
        pressBack()
        getViewInteractionFromMatchAtPosition(
            R.id.menu_id_action_button,
            0
        ).perform(click())
        testClick(R.id.homeButton)
        val intent = Intent()
        val testFile = Uri.fromFile(File(getPath(DOC_PDF)))
        intent.setData(testFile)
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(result)
        testClick(R.id.fileButton)
        getViewInteractionFromMatchAtPosition(
            R.id.menu_id_action_button,
            0
        ).perform(click())
        testClick(R.id.printPreviewButton)
        testClick(R.id.view_id_print_button)
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printSettingsFragment)
        onView(withId(R.id.view_id_print_header)).check(matches(isDisplayed()))
        onView(withId(R.id.view_id_print_selected_printer)).check(matches(isDisplayed()))
    }

    @Test
    fun testOnClick_EditText() {
        val settingsScreen =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).findObject(
                UiSelector().className(
                    "android.widget.EditText"
                )
            )
        if (settingsScreen.exists()) {
            settingsScreen.click()
        }
        waitForAnimation()
        Assert.assertTrue(isKeyboardOpen(_printSettingsFragment!!))
        pressBack()
        Assert.assertFalse(isKeyboardOpen(_printSettingsFragment!!))
    }

    @Test
    fun testSettings_Update() {
        val setting = "Color Mode"
        val option = "Black"

        // Select setting
        onView(allOf(
            isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(setting))
        ).perform(click())

        // Select option
        onView(allOf(
            withId(R.id.view_id_subview_option_item),
            hasDescendant(
                allOf(
                    withId(R.id.menuTextView),
                    withText(option))
            ))
        ).perform(click())

        // Check selected option displayed in item header
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
        onView(allOf(
            withId(R.id.view_id_show_subview_container),
            hasDescendant(allOf(
                withText(setting)
            )))
        ).check(matches(
            hasDescendant(allOf(
                withText(option))
        )))

    }

    @Test
    fun testSelectPrinter() {
        val selectedPrinter = 0
        val nextPrinter = 1

        // Check selected Printer
        onView(allOf(withId(R.id.listValueTextView), isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(_printerManager!!.savedPrintersList[selectedPrinter]!!.name))))
        onView(allOf(withId(R.id.listValueSubTextView), isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(_printerManager!!.savedPrintersList[selectedPrinter]!!.ipAddress))))

        // Select Printer
        testClick(R.id.view_id_print_selected_printer)
        getViewInteractionFromMatchAtPosition(
            R.id.view_id_subview_printer_item,
            nextPrinter
        ).perform(click())

        // Check selected Printer
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
        onView(allOf(withId(R.id.listValueTextView), isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(_printerManager!!.savedPrintersList[nextPrinter]!!.name))))
        onView(allOf(withId(R.id.listValueSubTextView), isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(_printerManager!!.savedPrintersList[nextPrinter]!!.ipAddress))))
    }

    @Test
    fun testPrint_Success() {
        testClick(R.id.view_id_print_header)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        val msg = dialog!!.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_info_msg_print_job_successful),
            (msg as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

    @Test
    fun testPrint_Cancel() {

    }

    @Test
    fun testPrint_Fail() {
        testClick(R.id.view_id_print_header)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        val msg = dialog!!.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_info_msg_print_job_failed),
            (msg as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

    companion object {
        private val TEST_ONLINE_PRINTER = Printer("ORPHIS FW5230", "192.168.0.32") // update with online printer details
        private val TEST_OFFLINE_PRINTER = Printer("ORPHIS GD500", "192.168.0.2")
    }
}