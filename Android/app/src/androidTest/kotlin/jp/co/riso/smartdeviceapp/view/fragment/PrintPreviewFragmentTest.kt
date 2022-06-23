package jp.co.riso.smartdeviceapp.view.fragment

import android.Manifest
import android.app.Instrumentation
import android.content.ClipData
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import jp.co.riso.smartdeviceapp.AppConstants.MULTI_IMAGE_PDF_FILENAME
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager
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
        _printer = TEST_PRINTER_ONLINE
        if (!_printerManager!!.isExists(_printer)) {
            _printerManager!!.savePrinterToDB(_printer, true)
        }
    }

    private fun turnPageForward(isForward: Boolean) {
        val printPreviewView = onView(withId(R.id.printPreviewView))
        if (mainActivity!!.resources.configuration.orientation == ORIENTATION_LANDSCAPE &&
            mainActivity!!.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            /** TODO: For now, force orientation to portrait.
             *  Swipe doesn't work when orientation is landscape (tried either swipe left/up/down) */
            mainActivity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        waitForAnimation()
        if (isForward) {
            printPreviewView.perform(swipeLeft())
        } else {
            printPreviewView.perform(swipeRight())
        }
        waitForAnimation()
    }


    private fun fileNameNoExtension(fileName: String): String {
        return fileName.substring(0, fileName.lastIndexOf('.'))
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

    @Ignore("test fails")
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
        selectDocument(getUriFromPath(DOC_PDF))
        Assert.assertEquals(0, _printPreviewView.currentPage)
        turnPageForward(true)
        Assert.assertEquals(1, _printPreviewView.currentPage)
        turnPageForward(false)
        Assert.assertEquals(0, _printPreviewView.currentPage)
    }

    @Test
    fun testPreview_SwipeUntilLastPage() {
        selectDocument(getUriFromPath(DOC_PDF_4PAGES))
        for (i in 0 until _printPreviewView.pageCount) {
            Assert.assertEquals(i, _printPreviewView.currentPage)
            turnPageForward(true)
        }
        Assert.assertEquals((_printPreviewView.pageCount - 1), _printPreviewView.currentPage)
    }

    @Ignore("test fails")
    fun testFileOpenIn_Document() {
        val testFile = DOC_PDF

        loadFileUsingOpenIn(getUriFromPath(testFile))
        waitForAnimation()

        Assert.assertEquals(
            fileNameNoExtension(testFile),
            fileNameNoExtension(PDFFileManager.getSandboxPDFName(SmartDeviceApp.appContext)!!))
    }

    @Ignore("test fails")
    fun testFileOpenIn_DocumentTxt() {
        val testFile = DOC_TXT

        loadFileUsingOpenIn(getUriFromPath(testFile))
        waitForAnimation()

        Assert.assertEquals(
            fileNameNoExtension(testFile),
            fileNameNoExtension(PDFFileManager.getSandboxPDFName(SmartDeviceApp.appContext)!!))
    }

    @Ignore("test fails")
    fun testFileOpenIn_Photo() {
        val testFile = IMG_BMP

        loadFileUsingOpenIn(getUriFromPath(testFile))
        waitForAnimation()

        Assert.assertEquals(
            fileNameNoExtension(testFile),
            fileNameNoExtension(PDFFileManager.getSandboxPDFName(SmartDeviceApp.appContext)!!))
    }

    @Ignore("test fails")
    fun testFileOpenIn_MultiplePhotos() {
        val testFiles = ClipData.newUri(
            mainActivity!!.contentResolver,
            IMG_BMP,
            getUriFromPath(IMG_BMP))
        testFiles.addItem(ClipData.Item(getUriFromPath(IMG_GIF)))
        testFiles.addItem(ClipData.Item(getUriFromPath(IMG_PNG)))

        loadFileUsingOpenIn(testFiles)
        waitForAnimation()

        Assert.assertEquals(
            fileNameNoExtension(MULTI_IMAGE_PDF_FILENAME),
            fileNameNoExtension(PDFFileManager.getSandboxPDFName(SmartDeviceApp.appContext)!!))
    }

    @Ignore("test fails")
    fun testFileOpenConsecutive() {
        val testFile1 = DOC_TXT
        val testFile2 = IMG_BMP

        switchScreen(MenuFragment.STATE_HOME)
        val intent = Intent()
        intent.setData(getUriFromPath(testFile1))
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    intent))
        onView(withId(R.id.fileButton)).perform(click())

        selectPhotos(getUriFromPath(testFile2))
        waitForAnimation()

        Assert.assertEquals(
            fileNameNoExtension(testFile2),
            fileNameNoExtension(PDFFileManager.getSandboxPDFName(SmartDeviceApp.appContext)!!))
    }

    @Ignore("test fails")
    fun testFileOpenConsecutive_OpenIn() {
        val testFile1 = DOC_TXT
        val testFile2 = IMG_BMP

        switchScreen(MenuFragment.STATE_HOME)
        val intent = Intent()
        intent.setData(getUriFromPath(testFile1))
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
            .respondWith(
                Instrumentation.ActivityResult(
                    FragmentActivity.RESULT_OK,
                    intent))
        onView(withId(R.id.fileButton)).perform(click())
        waitFewSeconds()

        loadFileUsingOpenIn(getUriFromPath(testFile2))
        waitForAnimation()

        Assert.assertEquals(
            fileNameNoExtension(testFile2),
            fileNameNoExtension(PDFFileManager.getSandboxPDFName(SmartDeviceApp.appContext)!!))
    }

    @Test
    fun testPdfError_Encrypted() {
        selectDocument(getUriFromPath(DOC_PDF_ERR_WITH_ENCRYPTION))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_pdf_encrypted
        )
    }

    @Test
    fun testPdfError_PrintNotAllowed() {
        selectDocument(getUriFromPath(DOC_PDF_ERR_PRINT_NOT_ALLOWED))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_pdf_printing_not_allowed
        )
    }

    @Test
    fun testPdfError_OpenFailed() {
        selectDocument(getUriFromPath(DOC_PDF_ERR_OPEN_FAILED))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_open_failed
        )
    }

    @Test
    fun testConversionError_Fail() {
        selectPhotos(getUriFromPath(IMG_ERR_FAIL_CONVERSION))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_conversion_failed
        )
    }

    @Ignore("test fails")
    fun testConversionError_Unsupported() {
        val testFiles = ClipData.newUri(
            mainActivity!!.contentResolver,
            IMG_BMP,
            getUriFromPath(IMG_BMP))
        testFiles.addItem(ClipData.Item(getUriFromPath(IMG_GIF)))
        testFiles.addItem(ClipData.Item(getUriFromPath(IMG_ERR_UNSUPPORTED)))

        loadFileUsingOpenIn(testFiles)
        waitForAnimation()

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_invalid_file_selection
        )
    }

    @Test
    fun testConversionError_TxtSizeLimit() {
        selectDocument(getUriFromPath(DOC_TXT_ERR_SIZE_LIMIT))

        checkDialog(
            PrintPreviewFragment.FRAGMENT_TAG_DIALOG,
            R.string.ids_err_msg_txt_size_limit
        )
    }

    @Ignore("test fails")
    fun testOrientationChange() {
        switchOrientation()
        waitForAnimation()
        testPrintSettingsButton_NoPrinter()

        switchOrientation()
        waitForAnimation()
        testPrintSettingsButton_NoPrinter()
    }
}