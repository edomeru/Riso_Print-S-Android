package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
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
        _printersList = null
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
        // Click setting
        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(setting)))
            .perform(scrollTo(), click())

        // Check options
        for (option in options) {
            onView(allOf(
                withId(R.id.view_id_subview_option_item),
                hasDescendant(allOf(
                    withId(R.id.menuTextView),
                    withText(option))))
                ).perform(scrollTo())
        }
        // Check that no other options exist
        getViewInteractionFromMatchAtPosition(R.id.view_id_subview_option_item, options.size)
            .check(doesNotExist())

        // return to print settings
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }

    private fun checkSettingOptionIsDisplayed(setting: String, option: String, isDisplayed: Boolean) {
        // Click setting
        onView(
            allOf(
                isDescendantOfA(withId(R.id.view_id_show_subview_container)),
                withText(setting)
            )
        )
            .perform(scrollTo(), click())

        if(isDisplayed) {
            // Check option is displayed
            onView(
                allOf(
                    withId(R.id.view_id_subview_option_item),
                    hasDescendant(
                        allOf(
                            withId(R.id.menuTextView),
                            withText(option)
                        )
                    )
                )
            ).perform(scrollTo())

            // return to print settings
            onView(withId(R.id.view_id_hide_subview_container))
                .perform(click())
        } else {
            // Check option is hidden
            onView(
                allOf(
                    withId(R.id.view_id_subview_option_item),
                    hasDescendant(
                        allOf(
                            withId(R.id.menuTextView),
                            withText(option)
                        )
                    )
                )
            ).check(doesNotExist())
        }
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

    private fun checkSettingIsNotDisplayed(setting: String) {
        // Check selected option displayed in item header
        onView(
            allOf(
                withId(R.id.view_id_show_subview_container),
                hasDescendant(
                    allOf(
                        withText(setting)
                    )
                )
            )
        ).check(
            matches(not(isDisplayed()))
        )
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
    fun testSettings_Copies() {
        val text = "12345"
        val result = "1234"

        onView(allOf(
                withClassName(`is`(EditText::class.java.canonicalName)),
                isDescendantOfA(
                    allOf(
                        withId(R.id.view_id_show_subview_container),
                        hasDescendant(withText(getString(R.string.ids_lbl_number_of_copies))))))
        ).apply {
            perform(scrollTo(), click())
            waitForView(hasFocus())
            Assert.assertTrue(isKeyboardOpen(_printSettingsFragment!!))
            perform(
                replaceText(text)
            ).check(matches(withText(result)))

            // Back
            pressBack()
            waitForAnimation()
            Assert.assertFalse(isKeyboardOpen(_printSettingsFragment!!))
            check(matches(withText(result)))

            // Action button
            perform(click())
            waitForView(hasFocus())
            Assert.assertTrue(isKeyboardOpen(_printSettingsFragment!!))
            perform(pressImeActionButton())
            waitForAnimation()
            Assert.assertFalse(isKeyboardOpen(_printSettingsFragment!!))

            // Enter button
            perform(click())
            waitForView(hasFocus())
            Assert.assertTrue(isKeyboardOpen(_printSettingsFragment!!))
            perform(pressKey(KeyEvent.KEYCODE_ENTER))
            waitForAnimation()
            Assert.assertFalse(isKeyboardOpen(_printSettingsFragment!!))

            // Back to Print Preview
            pressBack()
            onView(withId(R.id.view_id_print_button)).perform(click())
            check(matches(withText(result)))
        }
    }

    @Test
    fun testSettings_PinCode() {
        val text = "12345678"

        onView(withId(R.id.view_id_pin_code_edit_text)
        ).apply {
            toggleSwitchOn(getString(R.string.ids_lbl_secure_print))

            perform(scrollTo(), click())
            waitForView(hasFocus())
            Assert.assertTrue(isKeyboardOpen(_printSettingsFragment!!))
            perform(
                replaceText(text)
            )
            check(matches(isPasswordHidden()))
            pressBack()
            waitForAnimation()
            Assert.assertFalse(isKeyboardOpen(_printSettingsFragment!!))
            check(matches(isPasswordHidden()))

            perform(click())
            waitForView(hasFocus())
            Assert.assertTrue(isKeyboardOpen(_printSettingsFragment!!))
            perform(pressImeActionButton())
            waitForAnimation()
            Assert.assertFalse(isKeyboardOpen(_printSettingsFragment!!))

            perform(click())
            waitForView(hasFocus())
            Assert.assertTrue(isKeyboardOpen(_printSettingsFragment!!))
            perform(pressKey(KeyEvent.KEYCODE_ENTER))
            waitForAnimation()
            Assert.assertFalse(isKeyboardOpen(_printSettingsFragment!!))

            toggleSwitchOff(getString(R.string.ids_lbl_secure_print))
            toggleSwitchOn(getString(R.string.ids_lbl_secure_print))

            perform(scrollTo())
            check(matches(isPasswordHidden()))
        }
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

        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(getString(R.string.ids_lbl_colormode))
        )).check(matches(not(isDisplayed())))

        // hide/show print settings
        pressBack()
        testClickAndWait(R.id.view_id_print_button)

        // Collapse
        onView(allOf(
            withId(R.id.view_id_collapse_container),
            hasDescendant(withText(getString(R.string.ids_lbl_basic)))
        )).perform(click())

        onView(allOf(isDescendantOfA(withId(R.id.view_id_show_subview_container)),
            withText(getString(R.string.ids_lbl_colormode))
        )).check(matches(not(isDisplayed())))

        // Expand
        onView(allOf(
            withId(R.id.view_id_collapse_container),
            hasDescendant(withText(getString(R.string.ids_lbl_basic)))
        )).perform(click())

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
        waitForDialogWithText(R.string.ids_info_msg_print_job_successful)

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_successful
        )
    }

    @Test
    fun testPrint_RawPort() {
        // Open pdf in preview
        pressBack()
        selectDocument(getUriFromPath(DOC_PDF_4PAGES))

        // set raw port
        switchScreen(STATE_PRINTERS)
        if (!mainActivity!!.isTablet) {
            selectPrinterInfoScreen(TEST_PRINTER_ONLINE)
        }

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).perform(click())
        waitForAnimation()

        onView(withText(R.string.ids_lbl_port_raw)).perform(click())
        waitForAnimation()

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).check(matches(withSpinnerText(R.string.ids_lbl_port_raw)))

        switchScreen(STATE_PRINTPREVIEW)
        testClick(R.id.view_id_print_button)
        waitForView(withId(R.id.view_id_print_header))
        testClick(R.id.view_id_print_header)
        waitForPrint(30)

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_successful
        )
    }

    @Test
    fun testPrint_LprPort() {
        // Open pdf in preview
        pressBack()
        selectDocument(getUriFromPath(DOC_PDF_4PAGES))

        // set lpr port
        switchScreen(STATE_PRINTERS)
        if (!mainActivity!!.isTablet) {
            selectPrinterInfoScreen(TEST_PRINTER_ONLINE)
        }

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).perform(click())
        waitForAnimation()

        onView(withText(R.string.ids_lbl_port_lpr)).perform(click())
        waitForAnimation()

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).check(matches(withSpinnerText(R.string.ids_lbl_port_lpr)))

        switchScreen(STATE_PRINTPREVIEW)
        testClick(R.id.view_id_print_button)
        waitForView(withId(R.id.view_id_print_header))
        testClick(R.id.view_id_print_header)
        waitForPrint(30)

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
        waitForPrint(30)
        testClickAndWait(R.id.view_id_print_header)

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_failed
        )
    }

    @Test
    fun testPrint_NoNetwork() {
        initPreviewAndOpenPrintSettings()

        // disable network
        NetUtils.unregisterNetworkCallback(mainActivity!!)

        testClick(R.id.view_id_print_header)
        waitForDialogWithText(R.string.ids_err_msg_network_error)

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_err_msg_network_error
        )
    }

    @Test
    fun testPrint_NoNetworkWhilePrinting() {
        initPreviewAndOpenPrintSettings()

        testClick(R.id.view_id_print_header)
        // disable network
        NetUtils.unregisterNetworkCallback(mainActivity!!)
        waitForDialogWithText(R.string.ids_info_msg_print_job_failed)

        checkDialog(
            PrintSettingsFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_info_msg_print_job_failed
        )
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

            when(printer.printerType) {
                AppConstants.PRINTER_MODEL_GL -> {
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
                }
                AppConstants.PRINTER_MODEL_IS -> {
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
                else -> {
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
                }
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

            when(printer.printerType) {
                AppConstants.PRINTER_MODEL_GL -> {
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
                        getString(R.string.ids_lbl_papersize),
                        getString(R.string.ids_lbl_papersize_b5)
                    )
                    checkSettingOptionIsDisplayed(
                        getString(R.string.ids_lbl_inputtray),
                        getString(R.string.ids_lbl_inputtray_external),
                        true
                    )

                    updateSetting(
                        getString(R.string.ids_lbl_papersize),
                        getString(R.string.ids_lbl_papersize_letter)
                    )
                    checkSettingOptionIsDisplayed(
                        getString(R.string.ids_lbl_inputtray),
                        getString(R.string.ids_lbl_inputtray_external),
                        true
                    )
                    updateSetting(
                        getString(R.string.ids_lbl_papersize),
                        getString(R.string.ids_lbl_papersize_16k)
                    )
                    checkSettingOptionIsDisplayed(
                        getString(R.string.ids_lbl_inputtray),
                        getString(R.string.ids_lbl_inputtray_external),
                        true
                    )

                    updateSetting(
                        getString(R.string.ids_lbl_papersize),
                        getString(R.string.ids_lbl_papersize_b5)
                    )
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
                }
                AppConstants.PRINTER_MODEL_CEREZONA_S, AppConstants.PRINTER_MODEL_FT -> {
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
                }
                AppConstants.PRINTER_MODEL_IS, AppConstants.PRINTER_MODEL_GD -> {
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
                }
                else -> {
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
    }

    @Test
    fun testSettings_FinishingSide() {

        // Finishing Side - Left
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_left))
        updateSetting(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_1))
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_top))
        checkSetting(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_upperleft)
        )
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_left))
        checkSetting(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_1))

        // Finishing Side - Right
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_right))
        updateSetting(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_1))
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_top))
        checkSetting(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_upperright)
        )
        updateSetting(
            getString(R.string.ids_lbl_finishingside),
            getString(R.string.ids_lbl_finishingside_right))
        checkSetting(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_staple_1))
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
        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_off))

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
        updateSettingAndCheck(
            getString(R.string.ids_lbl_staple),
            getString(R.string.ids_lbl_off))

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
    fun testSettingsConstraints_Imposition() {

        toggleSwitchOn(getString(R.string.ids_lbl_booklet))

        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up)
        )

        // Imposition - Booking constraint
        Assert.assertFalse(isSwitchOn(getString(R.string.ids_lbl_booklet)))
        checkSetting(
            getString(R.string.ids_lbl_duplex),
            getString(R.string.ids_lbl_off)
        )
    }

    @Test
    fun testSettings_Imposition2up() {
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up)
        )

        // Imposition - 2up
        checkSettingOptions(
            getString(R.string.ids_lbl_imposition_order),
            listOf(
                getString(R.string.ids_lbl_imposition_order_2up_lr),
                getString(R.string.ids_lbl_imposition_order_2up_rl)
            )
        )

        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_2up_rl)
        )
        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_2up_lr)
        )
    }

    @Test
    fun testSettings_Imposition4up() {
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

    @Test
    fun testSettings_ImpositionSequence() {

        // Imposition - 2up with prev value 4up right to left/bottom
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_4up))
        updateSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_url))
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up))
        checkSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_2up_rl))

        // Imposition - 2up with prev value 4up left to bottom/right
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_4up))
        updateSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_ulb))
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up))
        checkSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_2up_lr))

        // Imposition - 2up with prev value 4up right to left/bottom
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_4up))
        updateSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_urb))
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up))
        checkSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_2up_rl))

        // Imposition - 2up with prev value 4up left to bottom/right
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_4up))
        updateSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_4up_ulr))
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_2up))
        checkSetting(
            getString(R.string.ids_lbl_imposition_order),
            getString(R.string.ids_lbl_imposition_order_2up_lr))

        // Imposition - Off
        updateSettingAndCheck(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_off))

        // Imposition - 4up with prev value off
        updateSetting(
            getString(R.string.ids_lbl_imposition),
            getString(R.string.ids_lbl_imposition_4up))
    }

    @Test
    fun testSettings_Punch() {
        // Punch - Output Tray facedown
        updateSetting(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_facedown))
        updateSetting(
            getString(R.string.ids_lbl_punch),
            getString(R.string.ids_lbl_punch_2holes))
        checkSetting(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_auto))

        updateSettingAndCheck(
            getString(R.string.ids_lbl_punch),
            getString(R.string.ids_lbl_off))
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

        updateSetting(
            getString(R.string.ids_lbl_booklet_finishing),
            getString(R.string.ids_lbl_off),
        )
        checkSettingOptions(
            getString(R.string.ids_lbl_outputtray),
            listOf(
                getString(R.string.ids_lbl_outputtray_auto),
                getString(R.string.ids_lbl_outputtray_facedown),
                getString(R.string.ids_lbl_outputtray_top),
                getString(R.string.ids_lbl_outputtray_stacking)
            ))

        updateSettingAndCheck(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_facedown)
        )
        updateSettingAndCheck(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_top)
        )
        updateSettingAndCheck(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_stacking)
        )

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

        // Output Tray - Punch On
        toggleSwitchOff(getString(R.string.ids_lbl_booklet))
        checkSettingOptionIsDisplayed(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_facedown),
            true
        )

        updateSetting(
            getString(R.string.ids_lbl_punch),
            getString(R.string.ids_lbl_punch_2holes))
        checkSettingOptionIsDisplayed(
            getString(R.string.ids_lbl_outputtray),
            getString(R.string.ids_lbl_outputtray_facedown),
            false
        )
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

    @Test
    fun testCapabilitiesDisabled_Punch() {
        for (printer in TEST_PRINTER_MODELS) {
            printer.config!!.isPunch0Available = true // if false, punch is enabled. Refer to definition in Printer.kt
            addTestPrinter(printer)
            selectPrinterPrintSettings(printer)

            checkSettingIsNotDisplayed(
                getString(R.string.ids_lbl_punch))
        }
    }

    @Test
    fun testCapabilitiesDisabled_Staple() {
        for (printer in TEST_PRINTER_MODELS) {
            printer.config!!.isStaplerAvailable = false
            addTestPrinter(printer)
            selectPrinterPrintSettings(printer)

            checkSettingIsNotDisplayed(
                getString(R.string.ids_lbl_staple))
        }
    }
}