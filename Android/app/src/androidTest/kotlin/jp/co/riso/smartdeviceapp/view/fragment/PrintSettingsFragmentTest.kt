package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.fragment.MenuFragment.Companion.STATE_PRINTERS
import jp.co.riso.smartdeviceapp.view.fragment.MenuFragment.Companion.STATE_PRINTPREVIEW
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView
import jp.co.riso.smartprint.R
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.junit.*


class PrintSettingsFragmentTest : BaseActivityTestUtil() {

    private var _printSettingsFragment: PrintSettingsFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _printersList: MutableList<Printer?>? = null

    private val _printPreviewView: PrintPreviewView
        get() = SmartDeviceApp.activity!!.findViewById(R.id.printPreviewView)

    @get:Rule
    var storagePermission: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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
    }

    private fun initFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintPreviewFragment()).commit()
            fm.executePendingTransactions()
        }
        waitForAnimation()

        switchScreen(STATE_PRINTERS)
        addPrinter(_printersList)

        switchScreen(STATE_PRINTPREVIEW)
        testClickAndWait(R.id.view_id_print_button)
        val fragment = fm.findFragmentById(R.id.rightLayout)
        Assert.assertTrue(fragment is PrintSettingsFragment)
        _printSettingsFragment = fragment as PrintSettingsFragment?
    }

    private fun initPrinters() {
        _printerManager = PrinterManager.getInstance(SmartDeviceApp.appContext!!)
        _printersList = mutableListOf(
            TEST_PRINTER_ONLINE
        )
    }

    private fun addAndSelectPrinter(printer: Printer) {
        addTestPrinter(printer)
        selectPrinterPrintSettings(printer)
    }

    private fun addTestPrinter(printer: Printer) {
        _printersList!!.add(printer)
        if (!_printerManager!!.isExists(printer)) {
            _printerManager!!.savePrinterToDB(printer, true)
            for (printerItem in _printerManager!!.savedPrintersList) {
                if (printerItem!!.ipAddress.contentEquals(printer.ipAddress)) {
                    _printersList!![(_printersList!!.size - 1)]!!.id = printerItem.id
                    break
                }
            }
        }
    }

    private fun initPreviewAndOpenPrintSettings() {
        // hide print settings screen
        pressBack()
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF_4PAGES))

        // return to print settings screen
        onView(withId(R.id.view_id_print_button)).perform(click())
    }

    private fun selectPrinterPrintSettings(printer: Printer) {
        // open printer select
        testClick(R.id.view_id_print_selected_printer)

        // select printer
        onView(allOf(
            withId(R.id.view_id_subview_printer_item),
            hasDescendant(withText(containsString(printer.ipAddress)))
        )).perform(scrollTo(),click())

        // hide printer select
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }

    private fun openDefaultPrintSettings() {
        // Close print settings
        pressBack()
        switchScreen(STATE_PRINTERS)

        if (!mainActivity!!.isTablet) {
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

    private fun checkSettingOptions(setting: String, options: List<String?>) {
        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(setting)))
            .perform(scrollTo(), click())
        //waitForAnimation()
        for (option in options) {
            onView(allOf(
                withId(R.id.view_id_subview_option_item),
                hasDescendant(allOf(
                    withId(R.id.menuTextView),
                    withText(option))))
                ).perform(scrollTo())
        }

        getViewInteractionFromMatchAtPosition(R.id.view_id_subview_option_item, options.size)
            .check(doesNotExist())

        // return to print settings
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }

    private fun updateSettingAndCheck(setting: String, option: String) {
        updateSetting(setting, option)
        checkSetting(setting, option)
    }

    private fun updateSetting(setting: String, option: String) {
        // Select setting
        onView(allOf(
                isDescendantOfA(withId(R.id.view_id_show_subview_container)),
                withText(setting)))
            .perform(scrollTo(), click())

        // Select option
        onView(allOf(
                withId(R.id.view_id_subview_option_item),
                hasDescendant(
                    allOf(
                        withId(R.id.menuTextView),
                        withText(option)
                    ))))
            .perform(scrollTo(), click())

        // return to print settings
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }


    private fun checkSetting(setting: String, option: String) {
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

    private fun updateSettingDoubleTap(setting: String, option: String) {
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

    private fun getSwitchView(setting: String): ViewInteraction {
        return onView(allOf(
                withClassName(`is`(Switch::class.java.canonicalName)),
                isDescendantOfA(
                    allOf(
                        withId(R.id.view_id_show_subview_container),
                        hasDescendant(withText(setting))))))
    }

    private fun isSwitchOn(setting: String): Boolean {
        return try {
            getSwitchView(setting)
                .check(matches(isChecked()))
            true
        } catch (e: AssertionFailedError) {
            false
        }
    }

    private fun toggleSwitchOn(setting: String) {
        if (!isSwitchOn(setting)) {
            getSwitchView(setting)
                .perform(scrollTo(), click())
        }
    }

    private fun toggleSwitchOff(setting: String) {
        if (isSwitchOn(setting)) {
            getSwitchView(setting)
                .perform(scrollTo(), click())
        }
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

    private fun isPasswordHidden(): Matcher<View?> {
        return object : BoundedMatcher<View?, EditText>(EditText::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Password is hidden")
            }
            override fun matchesSafely(editText: EditText): Boolean {
                //returns true if password is hidden
                return editText.transformationMethod is PasswordTransformationMethod
            }
        }
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printSettingsFragment)
        onView(withId(R.id.view_id_print_header)).check(matches(isDisplayed()))
        onView(withId(R.id.view_id_print_selected_printer)).check(matches(isDisplayed()))
    }

    @Test
    fun testOnClick_EditText() {
        onView(allOf(
                withClassName(`is`(EditText::class.java.canonicalName)),
                isDescendantOfA(
                    allOf(
                        withId(R.id.view_id_show_subview_container),
                        hasDescendant(withText(getString(R.string.ids_lbl_number_of_copies))))))
        ).perform(scrollTo(), click())
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
    fun testSelectPrinter() {
        val selectedPrinter = _printerManager!!.savedPrintersList[0]!!
        val nextPrinter = TEST_PRINTER_OFFLINE
        addTestPrinter(nextPrinter)
        checkSelectedPrinter(selectedPrinter.name, selectedPrinter.ipAddress)
        selectPrinterPrintSettings(nextPrinter)
        checkSelectedPrinter(nextPrinter.name, nextPrinter.ipAddress)
    }

    @Test
    fun testSelectPrinter_NoName() {
        val nextPrinter = TEST_PRINTER_NO_NAME
        addAndSelectPrinter(nextPrinter)
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

        if (!mainActivity!!.isTablet) {
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

        if(!mainActivity!!.isTablet) {
            initPreviewAndOpenPrintSettings()
        } else {
            switchScreen(MenuFragment.STATE_HOME)
            selectDocument(getUriFromPath(DOC_PDF_4PAGES))
            onView(withId(R.id.view_id_print_button)).perform(click())
        }

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
        addTestPrinter(TEST_PRINTER_OFFLINE)
        initPreviewAndOpenPrintSettings()

        // select offline printer
        selectPrinterPrintSettings(TEST_PRINTER_OFFLINE)

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint(30)

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
        updateSettingDoubleTap(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black))
        checkSetting(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black))

        pressBack()
        // return to print settings screen
        if (!mainActivity!!.isTablet) {
            selectPrinterInfoScreen(TEST_PRINTER_ONLINE)
            testClickAndWait(R.id.menu_id_action_print_settings_button)
        } else {
            getViewInteractionFromMatchAtPosition(R.id.default_print_settings, 0).perform(click())
        }

        // setting 2
        updateSettingAndCheck(
            getString(R.string.ids_lbl_papersize),
            getString(R.string.ids_lbl_papersize_b4))
    }

    @Test
    fun testSettings_Update() {
        // setting 1
        updateSettingDoubleTap(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black))
        checkSetting(
            getString(R.string.ids_lbl_colormode),
            getString(R.string.ids_lbl_colormode_black))

        pressBack()
        // return to print settings screen
        onView(withId(R.id.view_id_print_button))
            .perform(click())

        // setting 2
        updateSettingAndCheck(
            getString(R.string.ids_lbl_papersize),
            getString(R.string.ids_lbl_papersize_b4))
    }

    @Test
    fun testSettings_Authentication() {
        val text = "12345678"

        toggleSwitchOn(getString(R.string.ids_lbl_secure_print))

        onView(
            withId(R.id.view_id_pin_code_edit_text)
        ).apply {
            perform(
                scrollTo(),
                ViewActions.clearText(),
                ViewActions.typeText(text))
            check(matches(isPasswordHidden()))
        }

        toggleSwitchOff(getString(R.string.ids_lbl_secure_print))
        toggleSwitchOn(getString(R.string.ids_lbl_secure_print))

        onView(
            withId(R.id.view_id_pin_code_edit_text)
        ).apply {
            perform(scrollTo())
            check(matches(isPasswordHidden()))
        }
    }

    @Test
    fun testSettings_ColorMode() {
        for (printer in TEST_PRINTER_MODELS) {
            addAndSelectPrinter(printer)

            if ((printer.printerType != AppConstants.PRINTER_MODEL_GD) &&
                 printer.printerType != AppConstants.PRINTER_MODEL_IS) {
                checkSettingOptions(
                    getString(R.string.ids_lbl_colormode),
                    listOf(
                        getString(R.string.ids_lbl_colormode_auto),
                        getString(R.string.ids_lbl_colormode_fullcolor),
                        getString(R.string.ids_lbl_colormode_black),
                        getString(R.string.ids_lbl_colormode_2color)))
                updateSettingAndCheck(
                    getString(R.string.ids_lbl_colormode),
                    getString(R.string.ids_lbl_colormode_2color))
            } else {
                checkSettingOptions(
                    getString(R.string.ids_lbl_colormode),
                    listOf(
                        getString(R.string.ids_lbl_colormode_auto),
                        getString(R.string.ids_lbl_colormode_fullcolor),
                        getString(R.string.ids_lbl_colormode_black)
                    )
                )
            }

            updateSettingAndCheck(
                getString(R.string.ids_lbl_colormode),
                getString(R.string.ids_lbl_colormode_fullcolor))
            updateSettingAndCheck(
                getString(R.string.ids_lbl_colormode),
                getString(R.string.ids_lbl_colormode_black))
            updateSettingAndCheck(
                getString(R.string.ids_lbl_colormode),
                getString(R.string.ids_lbl_colormode_auto))
        }
    }

    @Test
    fun testSettings_Orientation() {
        val field = PrintPreviewView::class.java.getDeclaredField("_pdfManager")
        field.isAccessible = true

        selectPhotos(getUriFromPath(IMG_PORTRAIT))
        var pdfManager = field.get(_printPreviewView) as PDFFileManager
        Assert.assertFalse(pdfManager.isPDFLandscape)

        selectPhotos(getUriFromPath(IMG_LANDSCAPE))
        pdfManager = field.get(_printPreviewView) as PDFFileManager
        Assert.assertTrue(pdfManager.isPDFLandscape)
    }

    @Test
    fun testSettings_DuplexPrint() {
        toggleSwitchOn(getString(R.string.ids_lbl_booklet))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_booklet_finishing),
            getString(R.string.ids_lbl_booklet_finishing_fold))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_booklet_layout),
            getString(R.string.ids_lbl_booklet_layout_reverse))

        Assert.assertTrue(isSwitchOn(getString(R.string.ids_lbl_booklet)))
        checkSetting(
            getString(R.string.ids_lbl_duplex),
            getString(R.string.ids_lbl_duplex_short_edge))

        toggleSwitchOff(getString(R.string.ids_lbl_booklet))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_duplex),
            getString(R.string.ids_lbl_duplex_long_edge))
        checkSetting(
            getString(R.string.ids_lbl_booklet_finishing),
            getString(R.string.ids_lbl_off))
        checkSetting(
            getString(R.string.ids_lbl_booklet_layout),
            getString(R.string.ids_lbl_booklet_layout_forward))
    }

    @Test
    fun testSettings_PaperSize() {
        for (printer in TEST_PRINTER_MODELS) {
            addAndSelectPrinter(printer)

            if (printer.isPrinterGL) {
                checkSettingOptions(
                    getString(R.string.ids_lbl_papersize),
                    listOf(
                        getString(R.string.ids_lbl_papersize_a3),
                        getString(R.string.ids_lbl_papersize_a3w),
                        getString(R.string.ids_lbl_papersize_sra3),
                        getString(R.string.ids_lbl_papersize_a4),
                        getString(R.string.ids_lbl_papersize_a5),
                        getString(R.string.ids_lbl_papersize_a6),
                        getString(R.string.ids_lbl_papersize_b4),
                        getString(R.string.ids_lbl_papersize_b5),
                        getString(R.string.ids_lbl_papersize_b6),
                        getString(R.string.ids_lbl_papersize_foolscap),
                        getString(R.string.ids_lbl_papersize_tabloid),
                        getString(R.string.ids_lbl_papersize_legal),
                        getString(R.string.ids_lbl_papersize_letter),
                        getString(R.string.ids_lbl_papersize_statement),
                        getString(R.string.ids_lbl_papersize_legal13),
                        getString(R.string.ids_lbl_papersize_8k),
                        getString(R.string.ids_lbl_papersize_16k)
                    )
                )
            } else if (printer.printerType != AppConstants.PRINTER_MODEL_IS) {
                checkSettingOptions(
                    getString(R.string.ids_lbl_papersize),
                    listOf(
                        getString(R.string.ids_lbl_papersize_a3),
                        getString(R.string.ids_lbl_papersize_a3w),
                        getString(R.string.ids_lbl_papersize_a4),
                        getString(R.string.ids_lbl_papersize_a5),
                        getString(R.string.ids_lbl_papersize_a6),
                        getString(R.string.ids_lbl_papersize_b4),
                        getString(R.string.ids_lbl_papersize_b5),
                        getString(R.string.ids_lbl_papersize_b6),
                        getString(R.string.ids_lbl_papersize_foolscap),
                        getString(R.string.ids_lbl_papersize_tabloid),
                        getString(R.string.ids_lbl_papersize_legal),
                        getString(R.string.ids_lbl_papersize_letter),
                        getString(R.string.ids_lbl_papersize_statement),
                        getString(R.string.ids_lbl_papersize_legal13),
                        getString(R.string.ids_lbl_papersize_8k),
                        getString(R.string.ids_lbl_papersize_16k)
                    )
                )
            } else {
                checkSettingOptions(
                    getString(R.string.ids_lbl_papersize),
                    listOf(
                        getString(R.string.ids_lbl_papersize_a3),
                        getString(R.string.ids_lbl_papersize_a3w),
                        getString(R.string.ids_lbl_papersize_a4),
                        getString(R.string.ids_lbl_papersize_a5),
                        getString(R.string.ids_lbl_papersize_a6),
                        getString(R.string.ids_lbl_papersize_b4),
                        getString(R.string.ids_lbl_papersize_b5),
                        getString(R.string.ids_lbl_papersize_b6),
                        getString(R.string.ids_lbl_papersize_foolscap),
                        getString(R.string.ids_lbl_papersize_tabloid),
                        getString(R.string.ids_lbl_papersize_legal),
                        getString(R.string.ids_lbl_papersize_letter),
                        getString(R.string.ids_lbl_papersize_statement)
                    )
                )
            }
        }
    }

    @Test
    fun testSettings_PaperType() {
        for (printer in TEST_PRINTER_MODELS) {
            addAndSelectPrinter(printer)

            if (printer.isPrinterFTorCEREZONA_S or (printer.printerType == AppConstants.PRINTER_MODEL_FW)) {
                checkSettingOptions(
                    getString(R.string.ids_lbl_papertype),
                    listOf(
                        getString(R.string.ids_lbl_papertype_any),
                        getString(R.string.ids_lbl_papertype_plain),
                        getString(R.string.ids_lbl_papertype_ijpaper),
                        getString(R.string.ids_lbl_papertype_mattcoated),
                        getString(R.string.ids_lbl_papertype_highquality),
                        getString(R.string.ids_lbl_papertype_cardij),
                        getString(R.string.ids_lbl_papertype_lwpaper),
                        getString(R.string.ids_lbl_papertype_roughpaper),
                        getString(R.string.ids_lbl_papertype_plain_premium)
                    )
                )
            } else {
                checkSettingOptions(
                    getString(R.string.ids_lbl_papertype),
                    listOf(
                        getString(R.string.ids_lbl_papertype_any),
                        getString(R.string.ids_lbl_papertype_plain),
                        getString(R.string.ids_lbl_papertype_ijpaper),
                        getString(R.string.ids_lbl_papertype_mattcoated),
                        getString(R.string.ids_lbl_papertype_highquality),
                        getString(R.string.ids_lbl_papertype_cardij),
                        getString(R.string.ids_lbl_papertype_lwpaper)
                    )
                )
            }
        }
    }

    @Test
    fun testSettings_InputTray() {
        TEST_PRINTER_GL.config!!.isExternalFeederAvailable = true
        TEST_PRINTER_FT.config!!.isExternalFeederAvailable = true
        TEST_PRINTER_CEREZONA.config!!.isExternalFeederAvailable = true

        for (printer in TEST_PRINTER_MODELS) {
            addAndSelectPrinter(printer)

            if (printer.isPrinterGL) {
                checkSettingOptions(
                    getString(R.string.ids_lbl_inputtray),
                    listOf(
                        getString(R.string.ids_lbl_inputtray_auto),
                        getString(R.string.ids_lbl_inputtray_standard),
                        getString(R.string.ids_lbl_inputtray_tray1),
                        getString(R.string.ids_lbl_inputtray_tray2),
                        getString(R.string.ids_lbl_inputtray_tray3),
                        getString(R.string.ids_lbl_inputtray_external)
                    )
                )

                updateSetting(
                    getString(R.string.ids_lbl_inputtray),
                    getString(R.string.ids_lbl_inputtray_external)
                )
                checkSettingOptions(
                    getString(R.string.ids_lbl_papersize),
                    listOf(
                        getString(R.string.ids_lbl_papersize_a4),
                        getString(R.string.ids_lbl_papersize_b5),
                        getString(R.string.ids_lbl_papersize_letter),
                        getString(R.string.ids_lbl_papersize_16k)
                    )
                )

                updateSetting(
                    getString(R.string.ids_lbl_inputtray),
                    getString(R.string.ids_lbl_inputtray_tray1)
                )
                updateSetting(
                    getString(R.string.ids_lbl_papersize),
                    getString(R.string.ids_lbl_papersize_8k)
                )
                checkSettingOptions(
                    getString(R.string.ids_lbl_inputtray),
                    listOf(
                        getString(R.string.ids_lbl_inputtray_auto),
                        getString(R.string.ids_lbl_inputtray_standard),
                        getString(R.string.ids_lbl_inputtray_tray1),
                        getString(R.string.ids_lbl_inputtray_tray2),
                        getString(R.string.ids_lbl_inputtray_tray3)
                    )
                )
            } else if (printer.isPrinterFTorCEREZONA_S) {
                checkSettingOptions(
                    getString(R.string.ids_lbl_inputtray),
                    listOf(
                        getString(R.string.ids_lbl_inputtray_auto),
                        getString(R.string.ids_lbl_inputtray_standard),
                        getString(R.string.ids_lbl_inputtray_tray1),
                        getString(R.string.ids_lbl_inputtray_tray2),
                        getString(R.string.ids_lbl_inputtray_external)
                    )
                )

                updateSetting(
                    getString(R.string.ids_lbl_inputtray),
                    getString(R.string.ids_lbl_inputtray_external)
                )
                checkSettingOptions(
                    getString(R.string.ids_lbl_papersize),
                    listOf(
                        getString(R.string.ids_lbl_papersize_a4),
                        getString(R.string.ids_lbl_papersize_b5),
                        getString(R.string.ids_lbl_papersize_letter),
                        getString(R.string.ids_lbl_papersize_16k)
                    )
                )

                updateSetting(
                    getString(R.string.ids_lbl_inputtray),
                    getString(R.string.ids_lbl_inputtray_tray2)
                )
                updateSetting(
                    getString(R.string.ids_lbl_papersize),
                    getString(R.string.ids_lbl_papersize_legal)
                )
                checkSettingOptions(
                    getString(R.string.ids_lbl_inputtray),
                    listOf(
                        getString(R.string.ids_lbl_inputtray_auto),
                        getString(R.string.ids_lbl_inputtray_standard),
                        getString(R.string.ids_lbl_inputtray_tray1),
                        getString(R.string.ids_lbl_inputtray_tray2)
                    )
                )
            } else if ((printer.printerType == AppConstants.PRINTER_MODEL_IS) or
                (printer.printerType == AppConstants.PRINTER_MODEL_GD)
            ) {
                checkSettingOptions(
                    getString(R.string.ids_lbl_inputtray),
                    listOf(
                        getString(R.string.ids_lbl_inputtray_auto),
                        getString(R.string.ids_lbl_inputtray_standard),
                        getString(R.string.ids_lbl_inputtray_tray1),
                        getString(R.string.ids_lbl_inputtray_tray2),
                        getString(R.string.ids_lbl_inputtray_tray3)
                    )
                )
            } else {
                checkSettingOptions(
                    getString(R.string.ids_lbl_inputtray),
                    listOf(
                        getString(R.string.ids_lbl_inputtray_auto),
                        getString(R.string.ids_lbl_inputtray_standard),
                        getString(R.string.ids_lbl_inputtray_tray1),
                        getString(R.string.ids_lbl_inputtray_tray2)
                    )
                )
            }
        }
    }

    @Test
    fun testSettings_Staple() {
        // Staple - Finishing Left
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_left))
        checkSettingOptions(
            getString(R.string.ids_lbl_staple),
            listOf(
                getString(R.string.ids_lbl_off),
                getString(R.string.ids_lbl_staple_1),
                getString(R.string.ids_lbl_staple_2)
            ))

        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_1))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_2))

        // Staple - Finishing Top
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_top))
        checkSettingOptions(
            getString(R.string.ids_lbl_staple),
            listOf(
                getString(R.string.ids_lbl_off),
                getString(R.string.ids_lbl_staple_upperleft),
                getString(R.string.ids_lbl_staple_upperright),
                getString(R.string.ids_lbl_staple_2)
            ))

        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_upperleft))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_upperright))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_2))

        // Staple - Finishing Right
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_right))
        checkSettingOptions(
            getString(R.string.ids_lbl_staple),
            listOf(
                getString(R.string.ids_lbl_off),
                getString(R.string.ids_lbl_staple_1),
                getString(R.string.ids_lbl_staple_2)
            ))

        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_1))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_2))
    }

    @Test
    fun testSettings_Imposition() {
        // Imposition - 2up
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up))
        checkSettingOptions(
            getString(R.string.ids_lbl_imposition_order),
            listOf(
                getString(R.string.ids_lbl_imposition_order_2up_lr),
                getString(R.string.ids_lbl_imposition_order_2up_rl)
            ))

        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_2up_rl))

        // Imposition - 4up
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_4up))
        checkSettingOptions(
            getString(R.string.ids_lbl_imposition_order),
            listOf(
                getString(R.string.ids_lbl_imposition_order_4up_ulr),
                getString(R.string.ids_lbl_imposition_order_4up_url),
                getString(R.string.ids_lbl_imposition_order_4up_ulb),
                getString(R.string.ids_lbl_imposition_order_4up_urb),
            ))

        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_url))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_ulb))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_urb))
        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_ulr))
    }

    @Ignore("TODO")
    fun testSettings_Punch() {
        // Output Tray - Punch On
        updateSetting(
            getString(R.string.ids_lbl_punch),
            getString(R.string.ids_lbl_punch_2holes))
        checkSettingOptions(
            getString(R.string.ids_lbl_outputtray),
            listOf(
                getString(R.string.ids_lbl_outputtray_auto),
                getString(R.string.ids_lbl_outputtray_top),
                getString(R.string.ids_lbl_outputtray_stacking)
            ))

        updateSetting(
            getString(R.string.ids_lbl_punch),
            getString(R.string.ids_lbl_off))

        updateSetting(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_facedown))

        // Output Tray - Punch On
        checkSettingOptions(
            getString(R.string.ids_lbl_punch),
            listOf(
                getString(R.string.ids_lbl_off)
            ))
    }

    @Test
    fun testSettings_OutputTray() {
        // Output Tray - Booklet On
        toggleSwitchOn(getString(R.string.ids_lbl_booklet))
        checkSettingOptions(
            getString(R.string.ids_lbl_outputtray),
            listOf(
                getString(R.string.ids_lbl_outputtray_auto),
                getString(R.string.ids_lbl_outputtray_facedown),
                getString(R.string.ids_lbl_outputtray_top),
                getString(R.string.ids_lbl_outputtray_stacking)
            ))

        // Output Tray - Booklet On Paper folding
        updateSetting(
            getString(R.string.ids_lbl_booklet_finishing),
            getString(R.string.ids_lbl_booklet_finishing_fold),
        )
        checkSettingOptions(
            getString(R.string.ids_lbl_outputtray),
            listOf(
                getString(R.string.ids_lbl_outputtray_auto)
            ))

        // Output Tray - Booklet On Paper folding
        updateSetting(
            getString(R.string.ids_lbl_booklet_finishing),
            getString(R.string.ids_lbl_booklet_finishing_fold),
        )
        checkSettingOptions(
            getString(R.string.ids_lbl_outputtray),
            listOf(
                getString(R.string.ids_lbl_outputtray_auto)
            ))
    }

    @Test
    fun testCapabilities_Punch() {
        for (printer in TEST_PRINTER_MODELS) {
            printer.config!!.isPunch0Available = false // if false, punch is enabled. Refer to definition in Printer.kt
            printer.config!!.isPunch3Available = (printer.name!!.contains("Orphis", true))
            printer.config!!.isPunch4Available = !(printer.name!!.contains("Orphis", true))
            addTestPrinter(printer)
            selectPrinterPrintSettings(printer)

            val punch3or4 = if (printer.name!!.contains("Orphis", true)) {
                getString(R.string.ids_lbl_punch_3holes)
            } else {
                getString(R.string.ids_lbl_punch_4holes)
            }

            checkSettingOptions(
                getString(R.string.ids_lbl_punch),
                listOf(
                    getString(R.string.ids_lbl_off),
                    getString(R.string.ids_lbl_punch_2holes),
                    punch3or4
                ))
        }
    }
}