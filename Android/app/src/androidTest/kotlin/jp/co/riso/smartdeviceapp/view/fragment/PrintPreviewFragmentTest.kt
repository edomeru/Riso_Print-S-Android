package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.app.Instrumentation
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView
import jp.co.riso.smartprint.R
import org.hamcrest.Matchers.not
import org.junit.*

class PrintPreviewFragmentTest : BaseActivityTestUtil() {

    private var _printPreviewFragment: PrintPreviewFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _printer: Printer? = null

    private val _printPreviewView: PrintPreviewView
        get() = mainActivity!!.findViewById(R.id.printPreviewView)

    @get:Rule
    var storagePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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
        _printer = TEST_ONLINE_PRINTER
        if (!_printerManager!!.isExists(_printer)) {
            _printerManager!!.savePrinterToDB(_printer, true)
        }
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printPreviewFragment)
    }

    @Test
    fun testPrintSettingsButton_NoPrinter() {
        clearPrintersList()
        Assert.assertEquals(
            0,
            _printerManager!!.savedPrintersList.size)

        testClickAndWait(R.id.view_id_print_button)

        checkDialog(
            PrintPreviewFragment.TAG_MESSAGE_DIALOG,
            R.string.ids_lbl_print_settings,
            R.string.ids_err_msg_no_selected_printer
        )
    }

    @Test
    fun testPrintSettingsButton_WithPrinter() {
        testClickAndWait(R.id.view_id_print_button)
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
    }

    @Test
    fun testPreview_ChangePage() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))

        Assert.assertEquals(
            0,
            _printPreviewView.currentPage
        )

        onView(withId(R.id.printPreviewView))
            .perform(swipeLeft())
        waitForAnimation()

        Assert.assertEquals(
            1,
            _printPreviewView.currentPage
        )
    }

    @Test
    fun testPreview_SwipeUntilLastPage() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF_4PAGES))

        Assert.assertEquals(
            0,
            _printPreviewView.currentPage
        )

        for (i in 0 until _printPreviewView.pageCount) {
            onView(withId(R.id.printPreviewView))
                .perform(swipeLeft())
            waitForAnimation()

            if (i == (_printPreviewView.pageCount - 1)) {
                Assert.assertEquals(
                    i,
                    _printPreviewView.currentPage
                )

                onView(withId(R.id.printPreviewView))
                    .perform(swipeLeft())
                waitForAnimation()

                Assert.assertEquals(
                    i,
                    _printPreviewView.currentPage
                )
            }
        }
    }

    @Test
    fun testFileOpen_Consecutive() {
        switchScreen(MenuFragment.STATE_HOME)
        val intent = Intent()
        intent.setData(getUriFromPath(DOC_TXT))
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    intent))
        onView(withId(R.id.fileButton)).perform(click())

        switchScreen(MenuFragment.STATE_HOME)
        selectPhotos(getUriFromPath(IMG_BMP))
    }

    @Test
    fun testPdfError_Encrypted() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF_ERR_WITH_ENCRYPTION))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_pdf_encrypted
        )
    }

    @Test
    fun testPdfError_PrintNotAllowed() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF_ERR_PRINT_NOT_ALLOWED))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_pdf_printing_not_allowed
        )
    }

    @Test
    fun testPdfError_OpenFailed() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF_ERR_OPEN_FAILED))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_open_failed
        )
    }

    @Test
    fun testConversionError_Fail() {
        switchScreen(MenuFragment.STATE_HOME)
        selectPhotos(getUriFromPath(IMG_ERR_FAIL_CONVERSION))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_conversion_failed
        )
    }

    @Ignore("TODO")
    fun testConversionError_Unsupported() {
        switchScreen(MenuFragment.STATE_HOME)

        //open in ?

        waitForAnimation()

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_invalid_file_selection
        )
    }


    @Test
    fun testConversionError_TxtSizeLimit() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_TXT_ERR_SIZE_LIMIT))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_txt_size_limit
        )
    }

    @Test
    fun testOrientationChange() {
        switchOrientation()
        waitForAnimation()
        testPrintSettingsButton_NoPrinter()

        switchOrientation()
        waitForAnimation()
        testPrintSettingsButton_NoPrinter()
    }
}