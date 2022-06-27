package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
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
import junit.framework.AssertionFailedError
import org.hamcrest.Matchers.*
import org.junit.*
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*

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
            TEST_PRINTER_ONLINE,
            TEST_PRINTER_OFFLINE,
            TEST_PRINTER_NO_NAME,
            TEST_PRINTER_CEREZONA,
            TEST_PRINTER_GL,
            TEST_PRINTER_FW
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

    private fun openDefaultPrintSettings() {
        // Close print settings
        pressBack()
        switchScreen(STATE_PRINTERS)

        if (!_printSettingsFragment!!.isTablet) {
            selectPrinterInfoScreen(TEST_PRINTER_ONLINE)
            testClickAndWait(R.id.menu_id_action_print_settings_button)
        } else {
            getViewInteractionFromMatchAtPosition(R.id.default_print_settings, 0).perform(click())
        }

        _printSettingsFragment =
            (mainActivity!!.supportFragmentManager.findFragmentById(R.id.rightLayout)) as PrintSettingsFragment
    }

    private fun selectPrinterInfoScreen(printer: Printer) {
        getViewInteractionFromMatchAtPosition(
            withText(printer.ipAddress), 0
        ).perform(click())
        waitForAnimation()
    }

    private fun displaySettingOptions(setting: String) {
        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(setting)))
            .perform(scrollTo(), click())
        waitForAnimation()
    }

    private fun checkSettingOptions(options: List<String?>) {
        for ((i,option) in options.withIndex()) {
            getViewInteractionFromMatchAtPosition(R.id.view_id_subview_option_item, i)
                .check(matches(hasDescendant(allOf(
                        withId(R.id.menuTextView),
                        withText(option)))))
        }
        // return to print settings
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }

    private fun updateSetting(setting: String, option: String) {
        // Select setting
        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
                withText(setting)))
            .perform(scrollTo(), click())

        // Select option
        repeat(2) {
            onView(allOf(withId(R.id.view_id_subview_option_item),
                    hasDescendant(
                        allOf(
                            withId(R.id.menuTextView),
                            withText(option)))))
                .perform(scrollTo(), click())
        }

        // return to print settings
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }

    private fun updateSetting(setting: String) {
        onView(
            allOf(
                withClassName(`is`(Switch::class.java.canonicalName)),
                isDescendantOfA(
                    allOf(
                        withId(R.id.view_id_show_subview_container),
                        hasDescendant(withText(setting))
                    )
                )
            )
        ).perform(scrollTo(), click())
    }

    private fun toggleSwitchOn(setting: String) {
        try {
            onView(
                allOf(
                    withClassName(`is`(Switch::class.java.canonicalName)),
                    isDescendantOfA(
                        allOf(
                            withId(R.id.view_id_show_subview_container),
                            hasDescendant(withText(setting))
                        )),
                ))
                .check(matches(isNotChecked()))
                .perform(scrollTo(), click())
        } catch (e: AssertionFailedError) {
            // switch is on
        }
    }

    private fun confirmSetting(setting: String, option: String) {
        // Check selected option displayed in item header
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

    private fun checkSelectedPrinter(name: String?, ipAddress: String?) {
        onView(allOf(withId(R.id.listValueTextView),isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(name))))
        onView(allOf(withId(R.id.listValueSubTextView),isDescendantOfA(withId(R.id.view_id_print_selected_printer))))
            .check(matches(withText(containsString(ipAddress))))
    }

    private fun getString(id: Int):String {
        return mainActivity!!.resources.getString(id)
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
    fun testSettings_Collapsible() {
        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(getString(R.string.ids_lbl_colormode))
        )).check(matches(isDisplayed()))

        // Collapse
        onView(allOf(
            withId(R.id.view_id_collapse_container),
            hasDescendant(withText(getString(R.string.ids_lbl_basic)))
        )).perform(click())
        waitForAnimation()

        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(getString(R.string.ids_lbl_colormode))
        )).check(matches(not(isDisplayed())))

        // Expand
        onView(allOf(
            withId(R.id.view_id_collapse_container),
            hasDescendant(withText(getString(R.string.ids_lbl_basic)))
        )).perform(click())
        waitForAnimation()

        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(getString(R.string.ids_lbl_colormode))
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testSettings_Update() {
        // setting 1
        updateSetting(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black)
        )
        confirmSetting(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black)
        )

        pressBack()
        // return to print settings screen
        onView(withId(R.id.view_id_print_button))
            .perform(click())

        // setting 2
        updateSetting(
            getString(R.string.ids_lbl_papersize),
            getString(R.string.ids_lbl_papersize_b4)
        )
        confirmSetting(
            getString(R.string.ids_lbl_papersize),
            getString(R.string.ids_lbl_papersize_b4)
        )
    }

    @Test
    fun testSettings_UpdateAuthentication() {
        val text = "12345678"
        val password = "••••••••"

        // authentication
        toggleSwitchOn(getString(R.string.ids_lbl_secure_print))

        onView(
            allOf(
                withClassName(`is`(EditText::class.java.canonicalName)),
                isDescendantOfA(
                    allOf(
                        withId(R.id.view_id_show_subview_container),
                        hasDescendant(withText(getString(R.string.ids_lbl_pin_code))))))
        ).apply {
            perform(scrollTo())
            perform(ViewActions.clearText())
            waitForAnimation()
            waitForAnimation()
            perform(ViewActions.typeText(text))
            waitForAnimation()
            check(matches(withText(password)))
        }
    }

    @Test
    fun testSettingsConstraints_Staple() {
        selectPrinterPrintSettings(TEST_PRINTER_CEREZONA)

        // Staple - Finishing Left
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_left))
        displaySettingOptions(getString(R.string.ids_lbl_staple))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_off),
                getString(R.string.ids_lbl_staple_1),
                getString(R.string.ids_lbl_staple_2)
            ))

        // Staple - Finishing Top
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_top))
        displaySettingOptions(getString(R.string.ids_lbl_staple))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_off),
                getString(R.string.ids_lbl_staple_upperleft),
                getString(R.string.ids_lbl_staple_upperright)
            ))

        // Staple - Finishing Right
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_right))
        displaySettingOptions(getString(R.string.ids_lbl_staple))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_off),
                getString(R.string.ids_lbl_staple_1),
                getString(R.string.ids_lbl_staple_2)
            ))
    }

    @Test
    fun testSettingsConstraints_Imposition() {
        selectPrinterPrintSettings(TEST_PRINTER_CEREZONA)

        // Imposition - 2up
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up))
        displaySettingOptions(getString(R.string.ids_lbl_imposition_order))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_imposition_order_2up_lr),
                getString(R.string.ids_lbl_imposition_order_2up_rl)
            ))

        // Imposition - 4up
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_4up))
        displaySettingOptions(getString(R.string.ids_lbl_imposition_order))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_imposition_order_4up_ulr),
                getString(R.string.ids_lbl_imposition_order_4up_url),
                getString(R.string.ids_lbl_imposition_order_4up_ulb),
                getString(R.string.ids_lbl_imposition_order_4up_urb),

            ))
    }

    @Ignore("TODO")
    fun testSettingsConstraints_OutputTray() {
        selectPrinterPrintSettings(TEST_PRINTER_CEREZONA)

        // Output Tray
        toggleSwitchOn(getString(R.string.ids_lbl_booklet))
        displaySettingOptions(getString(R.string.ids_lbl_imposition_order))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_outputtray_auto),
                getString(R.string.ids_lbl_outputtray_facedown),
                getString(R.string.ids_lbl_outputtray_top),
                getString(R.string.ids_lbl_outputtray_stacking)
            ))

        toggleSwitchOn(getString(R.string.ids_lbl_booklet))
        displaySettingOptions(getString(R.string.ids_lbl_imposition_order))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_outputtray_auto),
                getString(R.string.ids_lbl_outputtray_facedown),
                getString(R.string.ids_lbl_outputtray_top),
                getString(R.string.ids_lbl_outputtray_stacking)
            ))

        updateSetting(
            getString(R.string.ids_lbl_punch),
            getString(R.string.ids_lbl_off))
        displaySettingOptions(getString(R.string.ids_lbl_imposition_order))
        checkSettingOptions(
            listOf(
                getString(R.string.ids_lbl_outputtray_auto),
                getString(R.string.ids_lbl_outputtray_facedown),
                getString(R.string.ids_lbl_outputtray_top),
                getString(R.string.ids_lbl_outputtray_stacking)
            ))
    }

    @Ignore("TODO")
    fun testSettingsConstraints_InputTray() {
        for (printer in listOf(TEST_PRINTER_CEREZONA, TEST_PRINTER_GL, TEST_PRINTER_FW)) {
            selectPrinterPrintSettings(printer)
            // Paper Size / Input Tray


        }
    }

    @Test
    fun testSelectPrinter() {
        val selectedPrinter = _printerManager!!.savedPrintersList[0]!!
        val nextPrinter = TEST_PRINTER_OFFLINE

        // Check current printer
        checkSelectedPrinter(selectedPrinter.name, selectedPrinter.ipAddress)

        // Select Printer
        selectPrinterPrintSettings(nextPrinter)
        checkSelectedPrinter(nextPrinter.name, nextPrinter.ipAddress)

    }

    @Test
    fun testSelectPrinter_NoName() {
        val nextPrinter = TEST_PRINTER_NO_NAME

        // Select Printer
        selectPrinterPrintSettings(nextPrinter)
        checkSelectedPrinter(getString(R.string.ids_lbl_no_name), nextPrinter.ipAddress)
    }

    @Test
    fun testPrint_Success() {
        initPreviewAndOpenPrintSettings()

        // select online printer
        selectPrinterPrintSettings(TEST_PRINTER_ONLINE)

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_successful
        )
    }

    @Test
    fun testPrint_RawPort() {
        // set raw port
        pressBack()
        switchScreen(STATE_PRINTERS)

        if (!_printSettingsFragment!!.isTablet) {
            selectPrinterInfoScreen(TEST_PRINTER_ONLINE)
        }

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).check(matches(withSpinnerText(R.string.ids_lbl_port_lpr)))

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).perform(click())
        waitForAnimation()

        onView(withText(R.string.ids_lbl_port_raw)).perform(click())
        waitForAnimation()

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).check(matches(withSpinnerText(R.string.ids_lbl_port_raw)))

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
        selectPrinterPrintSettings(TEST_PRINTER_OFFLINE)

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
    fun testPrint_NoNetworkWhilePrinting() {
        initPreviewAndOpenPrintSettings()

        testClick(R.id.view_id_print_header)
        // disable wifi
        NetUtils.unregisterWifiCallback(mainActivity!!)
        waitForPrint()

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_failed
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

        // setting 1
        updateSetting(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black))
        confirmSetting(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black))

        pressBack()
        // return to print settings screen
        if (!_printSettingsFragment!!.isTablet) {
            selectPrinterInfoScreen(TEST_PRINTER_ONLINE)
            testClickAndWait(R.id.menu_id_action_print_settings_button)
        } else {
            getViewInteractionFromMatchAtPosition(R.id.default_print_settings, 0).perform(click())
        }

        // setting 2
        updateSetting(
            getString(R.string.ids_lbl_papersize),
            getString(R.string.ids_lbl_papersize_b4))
        confirmSetting(
            getString(R.string.ids_lbl_papersize),
            getString(R.string.ids_lbl_papersize_b4))
    }
}