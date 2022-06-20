package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.fragment.MenuFragment.Companion.STATE_PRINTERS
import jp.co.riso.smartprint.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.*

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
    }

    @After
    fun cleanUp() {
        Intents.release()
        clearPrintersList()
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
        testClickAndWait(R.id.view_id_print_button)
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

    private fun initPreviewAndOpenPrintSettings() {
        // hide print settings screen
        pressBack()
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))

        // return to print settings screen
        onView(withId(R.id.view_id_print_button)).perform(click())
    }

    private fun openDefaultPrintSettings() {
        // Close print settings
        pressBack()
        switchScreen(STATE_PRINTERS)

        if (!_printSettingsFragment!!.isTablet) {
            selectPrinterInfoScreen(0)
            testClickAndWait(R.id.menu_id_action_print_settings_button)
        } else {
            getViewInteractionFromMatchAtPosition(R.id.default_print_settings, 0).perform(click())
        }

        _printSettingsFragment =
            (mainActivity!!.supportFragmentManager.findFragmentById(R.id.rightLayout)) as PrintSettingsFragment
    }

    private fun selectPrinterInfoScreen(index: Int) {
        getViewInteractionFromMatchAtPosition(R.id.printerListRow, index).perform(click())
        waitForAnimation()
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

    private fun selectPrinterPrintSettings(printer: Printer) {
        // open printer select
        testClick(R.id.view_id_print_selected_printer)

        // select printer
        getViewInteractionFromMatchAtPosition(allOf(
            withId(R.id.view_id_subview_printer_item),
            hasDescendant(withText(containsString(printer.ipAddress)))), 0
        ).perform(click())

        // hide printer select
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }

    @Test
    fun testSelectPrinter() {
        val selectedPrinter = _printerManager!!.savedPrintersList[0]!!
        val nextPrinter = TEST_OFFLINE_PRINTER

        // Check selected Printer
        onView(allOf(withId(R.id.listValueTextView),isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(selectedPrinter.name))))
        onView(allOf(withId(R.id.listValueSubTextView),isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(selectedPrinter.ipAddress))))

        // Select Printer
        selectPrinterPrintSettings(nextPrinter)

        // Check selected Printer
        onView(allOf(withId(R.id.listValueTextView), isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(nextPrinter.name))))
        onView(allOf(withId(R.id.listValueSubTextView), isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(nextPrinter.ipAddress))))
    }

    @Test
    fun testPrint_Success() {
        initPreviewAndOpenPrintSettings()

        // select online printer
        selectPrinterPrintSettings(TEST_ONLINE_PRINTER)

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_successful
        )
    }

    @Ignore("TODO")
    fun testPrint_RawPort() {
        // set raw port
        pressBack()
        switchScreen(STATE_PRINTERS)

        if (!_printSettingsFragment!!.isTablet) {
            selectPrinterInfoScreen(0)
        }

        testClickAndWait(R.layout.printerinfo_port_item)
        val rawPort =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).findObject(
                UiSelector().textMatches(
                    mainActivity!!.getString(R.string.ids_lbl_port_raw)
                )
            )
        if (rawPort.exists()) {
            rawPort.click()
        }

        initPreviewAndOpenPrintSettings()
        testClick(R.id.view_id_print_header)
        waitForPrint()

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_successful
        )
    }

    @Test
    fun testPrint_Cancel() {
        initPreviewAndOpenPrintSettings()
        testClickAndWait(R.id.view_id_print_header)

        // Cancel print
        pressBack()

        Assert.assertNull(
            mainActivity!!.supportFragmentManager.findFragmentByTag(
                PrintSettingsFragment.TAG_MESSAGE_DIALOG
        ))
    }

    @Test
    fun testPrint_Fail() {
        initPreviewAndOpenPrintSettings()

        // select offline printer
        selectPrinterPrintSettings(TEST_OFFLINE_PRINTER)

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_failed
        )
    }

    @Test
    fun testPrint_NoNetwork() {
        initPreviewAndOpenPrintSettings()

        // disable wifi
        NetUtils.unregisterWifiCallback(mainActivity!!)

        testClick(R.id.view_id_print_header)
        waitForPrint()

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_err_msg_network_error
        )
    }

    @Test
    fun testDefaultPrintSettings_ClickEditText() {
        openDefaultPrintSettings()
        testOnClick_EditText()
    }

    @Test
    fun testDefaultPrintSettings_UpdateSettings() {
        openDefaultPrintSettings()
        testSettings_Update()
    }
}