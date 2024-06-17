package jp.co.riso.smartdeviceapp.view.fragment

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.LruCache
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.OnGenericMotionListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.WaitingDialogFragment
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.MockTestUtil
import jp.co.riso.smartdeviceapp.ReflectionTestUtil
import jp.co.riso.smartdeviceapp.controller.pdf.PDFConverterManager
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.model.ContentPrintPrintSettings
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseActivity
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView
import jp.co.riso.smartprint.R
import org.junit.*
import java.lang.reflect.InvocationTargetException


class PrintPreviewFragmentTest {
    // ================================================================================
    // Tests - initializeFragment
    // ================================================================================
    @Test
    fun testInitializeFragment_Initialized() {
        ContentPrintManager.isLoggedIn = false
        ContentPrintManager.isFileFromContentPrint = false
        ContentPrintManager.selectedFile = null

        val fragment = getInitializedFragment()
        val bundle = mockBundle()
        val view = mockView()
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_printpreview)
    }

    @Test
    fun testInitializeFragment_NotInitialized_ContentPrintFile() {
        ContentPrintManager.isLoggedIn = true
        ContentPrintManager.isFileFromContentPrint = true
        ContentPrintManager.selectedFile = selectedFile

        val fragment = spyk<PrintPreviewFragment>()
        val mockActivity = mockFragmentActivity()
        // Since mockkConstructor() does not work for PDFFileManager,
        // Set a condition to prevent _pdfManager!!.initializeAsync() from being called
        every { mockActivity.intent } returns null
        addActivity(fragment, mockActivity)
        val bundle = mockBundle()
        val view = mockView()
        fragment.initializeFragment(null)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_printpreview)
    }

    @Test
    fun testInitializeFragment_NotInitialized_NoFile() {
        ContentPrintManager.isLoggedIn = false
        ContentPrintManager.isFileFromContentPrint = false
        ContentPrintManager.selectedFile = null

        val fragment = spyk<PrintPreviewFragment>()
        val mockActivity = mockFragmentActivity()
        // Since mockkConstructor() does not work for PDFFileManager,
        // Set a condition to prevent _pdfManager!!.initializeAsync() from being called
        every { mockActivity.intent } returns null
        addActivity(fragment, mockActivity)
        val bundle = mockBundle()
        val view = mockView()
        fragment.initializeFragment(null)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_printpreview)
    }

    @Test
    fun testInitializeFragment_NotInitialized_IsLoggedIn() {
        ContentPrintManager.isLoggedIn = true
        ContentPrintManager.isFileFromContentPrint = false
        ContentPrintManager.selectedFile = null

        val fragment = spyk<PrintPreviewFragment>()
        val mockActivity = mockFragmentActivity()
        // Since mockkConstructor() does not work for PDFFileManager,
        // Set a condition to prevent _pdfManager!!.initializeAsync() from being called
        every { mockActivity.intent } returns null
        addActivity(fragment, mockActivity)
        val bundle = mockBundle()
        val view = mockView()
        fragment.initializeFragment(null)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_printpreview)
    }

    @Test
    fun testInitializeFragment_NotInitialized_NullContentPrintFile() {
        ContentPrintManager.isLoggedIn = true
        ContentPrintManager.isFileFromContentPrint = true
        ContentPrintManager.selectedFile = null

        val fragment = spyk<PrintPreviewFragment>()
        val mockActivity = mockFragmentActivity()
        // Since mockkConstructor() does not work for PDFFileManager,
        // Set a condition to prevent _pdfManager!!.initializeAsync() from being called
        every { mockActivity.intent } returns null
        addActivity(fragment, mockActivity)
        val bundle = mockBundle()
        val view = mockView()
        fragment.initializeFragment(null)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_printpreview)
    }

    // ================================================================================
    // Tests - initializeView
    // ================================================================================
    @Test
    fun testInitializeView_Landscape() {
        ContentPrintManager.isFileFromContentPrint = true
        AppConstants.USE_PDF_ORIENTATION = true
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val manager = setPDFFileManager(fragment)
        every { manager.isInitialized } returns true
        every { manager.fileName } returns TEST_FILE_NAME
        every { manager.isPDFLandscape } returns true
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS, PrintSettings())
        val view = mockView()
        val bundle = mockBundle()
        fragment.initializeView(view, bundle)
        Assert.assertNotNull(fragment)
    }

    @Test
    fun testInitializeView_Portrait() {
        ContentPrintManager.isFileFromContentPrint = true
        AppConstants.USE_PDF_ORIENTATION = true
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val manager = setPDFFileManager(fragment)
        every { manager.isInitialized } returns true
        every { manager.fileName } returns TEST_FILE_NAME
        every { manager.isPDFLandscape } returns false
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS, PrintSettings())
        val view = mockView()
        val bundle = mockBundle()
        fragment.initializeView(view, bundle)
        Assert.assertNotNull(fragment)
    }

    @Test
    fun testInitializeView_ScrollEvent() {
        ContentPrintManager.isFileFromContentPrint = true
        AppConstants.USE_PDF_ORIENTATION = false
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val manager = setPDFFileManager(fragment)
        every { manager.isInitialized } returns false
        every { manager.fileName } returns null
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS, PrintSettings())
        ReflectionTestUtil.setField(fragment, FIELD_PAGE, 1)

        val view = mockView()
        val printPreviewView = view.findViewById<View>(R.id.printPreviewView)
        every { printPreviewView.setOnGenericMotionListener(any()) } answers {
            val listener = firstArg<OnGenericMotionListener>()
            val ctrlScrollUp = mockk<MotionEvent>(relaxed = true)
            every { ctrlScrollUp.action } returns MotionEvent.ACTION_SCROLL
            every { ctrlScrollUp.metaState } returns KeyEvent.META_CTRL_ON // isCtrlPressed() = true
            every { ctrlScrollUp.getAxisValue(any()) } returns 1f // Scroll is upward
            listener.onGenericMotion(view, ctrlScrollUp)

            val ctrlScrollDown = mockk<MotionEvent>(relaxed = true)
            every { ctrlScrollDown.action } returns MotionEvent.ACTION_SCROLL
            every { ctrlScrollDown.metaState } returns KeyEvent.META_CTRL_ON // isCtrlPressed() = true
            every { ctrlScrollDown.getAxisValue(any()) } returns -1f // Scroll is downward
            listener.onGenericMotion(view, ctrlScrollDown)

            val ctrlScrollEvent = mockk<MotionEvent>(relaxed = true)
            every { ctrlScrollEvent.action } returns MotionEvent.ACTION_SCROLL
            every { ctrlScrollEvent.metaState } returns KeyEvent.META_CTRL_ON // isCtrlPressed() = true
            every { ctrlScrollEvent.getAxisValue(any()) } returns 0f // No scroll movement
            listener.onGenericMotion(view, ctrlScrollEvent)

            val scrollUp = mockk<MotionEvent>(relaxed = true)
            every { scrollUp.action } returns MotionEvent.ACTION_SCROLL
            every { scrollUp.metaState } returns 0 // isCtrlPressed() = false
            every { scrollUp.getAxisValue(any()) } returns 1f // Scroll is upward
            listener.onGenericMotion(view, scrollUp)

            val scrollDown = mockk<MotionEvent>(relaxed = true)
            every { scrollDown.action } returns MotionEvent.ACTION_SCROLL
            every { scrollDown.metaState } returns 0 // isCtrlPressed() = false
            every { scrollDown.getAxisValue(any()) } returns -1f // Scroll is downward
            listener.onGenericMotion(view, scrollDown)

            val scrollEvent = mockk<MotionEvent>(relaxed = true)
            every { scrollEvent.action } returns MotionEvent.ACTION_SCROLL
            every { scrollEvent.metaState } returns 0 // isCtrlPressed() = false
            every { scrollEvent.getAxisValue(any()) } returns 0f // No scroll movement
            listener.onGenericMotion(view, scrollEvent)

            val pressEvent = mockk<MotionEvent>(relaxed = true)
            every { pressEvent.action } returns MotionEvent.ACTION_DOWN
            listener.onGenericMotion(view, pressEvent)
        }

        val bundle = null
        fragment.initializeView(view, bundle)
        Assert.assertNotNull(fragment)
    }

    @Test
    fun testInitializeView_NoCache() {
        ContentPrintManager.isFileFromContentPrint = false
        AppConstants.USE_PDF_ORIENTATION = false
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        setHandler(fragment, true)
        val manager = setPDFFileManager(fragment)
        every { manager.isInitialized } returns false
        every { manager.fileName } returns null
        val view = mockView()
        val bundle = mockBundle()
        fragment.initializeView(view, bundle)
        Assert.assertNotNull(fragment)
    }

    // ================================================================================
    // Tests - onDestroyView
    // ================================================================================
    @Test
    fun testOnDestroyView_OngoingPDFConversion() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        setPDFConverterManager(fragment)
        fragment.onDestroyView()
        fragment.clearIconStates()
        val printPreviewView = ReflectionTestUtil.getField(fragment, FIELD_PRINT_PREVIEW)
        val pdfConverter = ReflectionTestUtil.getField(fragment, FIELD_PDF_CONVERTER)
        Assert.assertNull(printPreviewView)
        Assert.assertNull(pdfConverter)
    }

    @Test
    fun testOnDestroyView_NotInitialized() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, null)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, null)
        ReflectionTestUtil.setField(fragment, FIELD_PDF_CONVERTER, null)
        fragment.onDestroyView()
        fragment.clearIconStates()
        val printPreviewView = ReflectionTestUtil.getField(fragment, FIELD_PRINT_PREVIEW)
        val pdfConverter = ReflectionTestUtil.getField(fragment, FIELD_PDF_CONVERTER)
        Assert.assertNull(printPreviewView)
        Assert.assertNull(pdfConverter)
    }

    @Test
    fun testOnDestroyView_WaitingDialog() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, null)
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        ReflectionTestUtil.setField(fragment, FIELD_PDF_CONVERTER, null)
        fragment.onDestroyView()
        fragment.clearIconStates()
        val printPreviewView = ReflectionTestUtil.getField(fragment, FIELD_PRINT_PREVIEW)
        val pdfConverter = ReflectionTestUtil.getField(fragment, FIELD_PDF_CONVERTER)
        Assert.assertNull(printPreviewView)
        Assert.assertNull(pdfConverter)
    }

    // ================================================================================
    // Tests - onSaveInstanceState
    // ================================================================================
    @Test
    fun testOnSaveInstanceState() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val bundle = mockBundle()
        fragment.onSaveInstanceState(bundle)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, null)
        fragment.onSaveInstanceState(bundle)
    }

    // ================================================================================
    // Tests - onResume
    // ================================================================================
    @Test
    fun testOnResume_NoPdfFile_PermissionDialogClosed() {
        val fragment = getInitializedFragment(true)
        val activity = mockk<MainActivity>(relaxed = true)
        val mockInputMethod = mockk<InputMethodManager>(relaxed = true)
        every { activity.getSystemService(Activity.INPUT_METHOD_SERVICE) } returns mockInputMethod
        // No PDF file in open-in (No PDF file in sandbox by default)
        every { activity.intent } returns null
        addActivity(fragment, activity)
        ReflectionTestUtil.setField(fragment, FIELD_IS_PERMISSION_DIALOG, false)
        ReflectionTestUtil.setField(fragment, FIELD_PRINTER_ID, 1)
        ReflectionTestUtil.setField(fragment, FIELD_PAUSEABLE_HANDLER, null)
        printerType = null
        // _printerId != PrinterManager.EMPTY_ID && !printerManager.isExists(_printerId)
        printerExists = true
        fragment.onResume()
        printerExists = false
        fragment.onResume()
        ReflectionTestUtil.setField(fragment, FIELD_PRINTER_ID, PrinterManager.EMPTY_ID)
        val mockPauseableHandler = mockk<PauseableHandler>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAUSEABLE_HANDLER, mockPauseableHandler)
        printerType = AppConstants.PRINTER_MODEL_GL
        // _printerId == PrinterManager.EMPTY_ID && defaultPrinterId != PrinterManager.EMPTY_ID
        printerId = PrinterManager.EMPTY_ID
        fragment.onResume()
        ReflectionTestUtil.setField(fragment, FIELD_PRINTER_ID, PrinterManager.EMPTY_ID)
        printerId = 1
        fragment.onResume()
    }

    @Test
    fun testOnResume_HasPdfFile_PermissionDenied() {
        val fragment = getInitializedFragment(true)
        val activity = mockk<MainActivity>(relaxed = true)
        val mockInputMethod = mockk<InputMethodManager>(relaxed = true)
        every { activity.getSystemService(Activity.INPUT_METHOD_SERVICE) } returns mockInputMethod
        val intent = mockk<Intent>(relaxed = true)
        every { intent.data } returns mockk<Uri>()
        every { activity.intent } returns intent
        every {
            activity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_DENIED
        addActivity(fragment, activity)
        ReflectionTestUtil.setField(fragment, FIELD_IS_PERMISSION_DIALOG, true)
        fragment.onResume()
        ReflectionTestUtil.setField(fragment, FIELD_IS_PERMISSION_DIALOG, false)
        ReflectionTestUtil.setField(fragment, FIELD_SHOULD_DISPLAY_EXPLANATION, false)
        fragment.onResume()
        ReflectionTestUtil.setField(fragment, FIELD_SHOULD_DISPLAY_EXPLANATION, true)
        every { fragment.view } returns mockView()
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, null)
        fragment.onResume()
        val mockConfirmDialog = mockk<ConfirmDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, mockConfirmDialog)
        fragment.onResume()
    }

    @Test
    fun testOnResume_HasPdfFile_PermissionGranted() {
        val fragment = getInitializedFragment(true)
        val activity = mockk<MainActivity>(relaxed = true)
        val mockInputMethod = mockk<InputMethodManager>(relaxed = true)
        every { activity.getSystemService(Activity.INPUT_METHOD_SERVICE) } returns mockInputMethod
        val intent = mockk<Intent>(relaxed = true)
        every { intent.data } returns mockk<Uri>()
        every { activity.intent } returns intent
        every {
            activity.checkPermission(any(), any(), any())
        } returns PackageManager.PERMISSION_GRANTED
        addActivity(fragment, activity)
        ReflectionTestUtil.setField(fragment, FIELD_IS_PERMISSION_DIALOG, true)
        fragment.onResume()
        ReflectionTestUtil.setField(fragment, FIELD_IS_PERMISSION_DIALOG, false)
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, null)
        ReflectionTestUtil.setField(fragment, FIELD_PDF_CONVERTER, null)
        ReflectionTestUtil.setField(fragment, FIELD_IS_PDF_INITIALIZED, false)
        fragment.onResume()
        val mockConfirmDialog = mockk<ConfirmDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, mockConfirmDialog)
        setPDFConverterManager(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_IS_PDF_INITIALIZED, true)
        ReflectionTestUtil.setField(fragment, FIELD_IS_CONVERTER_INITIALIZED, true)
        fragment.onResume()
        ReflectionTestUtil.setField(fragment, FIELD_IS_CONVERTER_INITIALIZED, false)
        fragment.onResume()
    }

    // ================================================================================
    // Tests - onConfigurationChanged
    // ================================================================================
    @Test
    fun testOnConfigurationChanged_Visible() {
        val fragment = getInitializedFragment()
        val mockView = mockView()
        every { mockView.visibility } returns VISIBLE
        every { fragment.requireView() } returns mockView
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        mockkStatic(View::class)
        every { View.inflate(any(), any(), any()) } returns mockView
        val config = mockk<Configuration>()
        fragment.onConfigurationChanged(config)
        unmockkStatic(View::class)
    }

    @Test
    fun testOnConfigurationChanged_Invisible() {
        val fragment = getInitializedFragment()
        val mockView = mockView()
        every { mockView.visibility } returns INVISIBLE
        every { fragment.requireView() } returns mockView
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        mockkStatic(View::class)
        every { View.inflate(any(), any(), any()) } returns mockView
        val config = mockk<Configuration>()
        fragment.onConfigurationChanged(config)
        unmockkStatic(View::class)
    }

    // ================================================================================
    // Tests - isConversionOngoing
    // ================================================================================
    @Test
    fun testIsConversionOngoing_True() {
        val fragment = getInitializedFragment()
        val result = fragment.isConversionOngoing
        Assert.assertTrue(result)
    }

    @Test
    fun testIsConversionOngoing_False() {
        val fragment = spyk<PrintPreviewFragment>()
        val result = fragment.isConversionOngoing
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Tests - onKey
    // ================================================================================
    @Test
    fun testOnKey_SliderLeft() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val keyCode = KeyEvent.KEYCODE_DPAD_LEFT
        val event = mockk<KeyEvent>(relaxed = true)
        every { event.action } returns KeyEvent.ACTION_UP
        val result = fragment.onKey(view, keyCode, event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnKey_SliderRight() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val keyCode = KeyEvent.KEYCODE_DPAD_RIGHT
        val event = mockk<KeyEvent>(relaxed = true)
        every { event.action } returns KeyEvent.ACTION_UP
        val result = fragment.onKey(view, keyCode, event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnKey_SliderKeyUp() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val keyCode = KeyEvent.KEYCODE_0
        val event = mockk<KeyEvent>(relaxed = true)
        every { event.action } returns KeyEvent.ACTION_UP
        val result = fragment.onKey(view, keyCode, event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnKey_SliderKeyPress() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val keyCode = KeyEvent.KEYCODE_0
        val event = mockk<KeyEvent>(relaxed = true)
        every { event.action } returns KeyEvent.ACTION_DOWN
        val result = fragment.onKey(view, keyCode, event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnKey_KeyPress() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns 0
        val keyCode = KeyEvent.KEYCODE_0
        val event = mockk<KeyEvent>(relaxed = true)
        every { event.action } returns KeyEvent.ACTION_DOWN
        val result = fragment.onKey(view, keyCode, event)
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Tests - onFocusChange
    // ================================================================================
    @Test
    fun testOnFocusChange_NoProgress() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        every { mockSeekBar.progress } returns 0
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val hasFocus = false
        fragment.onFocusChange(view, hasFocus)
    }

    @Test
    fun testOnFocusChange_MaxProgress() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        every { mockSeekBar.progress } returns 100
        every { mockSeekBar.max } returns 100
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val hasFocus = false
        fragment.onFocusChange(view, hasFocus)
    }

    @Test
    fun testOnFocusChange_Progress() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        every { mockSeekBar.progress } returns 50
        every { mockSeekBar.max } returns 100
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val hasFocus = false
        fragment.onFocusChange(view, hasFocus)
    }

    @Test
    fun testOnFocusChange_SliderFocus() {
        val fragment = spyk<PrintPreviewFragment>()
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.pageSlider
        val hasFocus = true
        fragment.onFocusChange(view, hasFocus)
    }

    @Test
    fun testOnFocusChange_Other() {
        val fragment = spyk<PrintPreviewFragment>()
        val view = mockk<View>(relaxed = true)
        every { view.id } returns 0
        val hasFocus = true
        fragment.onFocusChange(view, hasFocus)
    }

    // ================================================================================
    // Tests - onClick
    // ================================================================================
    @Test
    fun testOnClick_PrintButton() {
        val fragment = spyk<PrintPreviewFragment>()
        setHandler(fragment)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.view_id_print_button
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_ActionButton() {
        val fragment = spyk<PrintPreviewFragment>()
        setHandler(fragment)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns R.id.menu_id_action_button
        fragment.onClick(view)
    }

    @Test
    fun testOnClick_Other() {
        val fragment = spyk<PrintPreviewFragment>()
        setHandler(fragment)
        val view = mockk<View>(relaxed = true)
        every { view.id } returns 0
        fragment.onClick(view)
    }

    // ================================================================================
    // Tests - onFileInitialized
    // ================================================================================
    @Test
    fun testOnFileInitialized_NoFileOK() {
        val fragment = getInitializedFragment()
        every { fragment.isDetached } returns false
        val mockView = mockView()
        every { fragment.view } returns mockView
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        ReflectionTestUtil.setField(fragment, FIELD_OPEN_IN_VIEW, mockView)
        val mockProgressBar = mockk<ProgressBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PROGRESS_BAR, mockProgressBar)
        ReflectionTestUtil.setField(fragment, FIELD_HAS_CONVERSION_ERROR, false)
        ReflectionTestUtil.setField(fragment, FIELD_FILENAME, null)
        ReflectionTestUtil.setField(fragment, FIELD_SHOULD_RESET_PAGE, false)
        ContentPrintManager.isFileFromContentPrint = true
        fragment.onFileInitialized(PDFFileManager.PDF_OK)
        ContentPrintManager.isFileFromContentPrint = false
        fragment.onFileInitialized(PDFFileManager.PDF_OK)
    }

    @Test
    fun testOnFileInitialized_WithFileOK() {
        val fragment = getInitializedFragment()
        every { fragment.isDetached } returns false
        val mockView = mockView()
        every { fragment.view } returns mockView
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        ReflectionTestUtil.setField(fragment, FIELD_OPEN_IN_VIEW, mockView)
        val mockProgressBar = mockk<ProgressBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PROGRESS_BAR, mockProgressBar)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val mockTextView = mockk<TextView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_LABEL, mockTextView)
        ReflectionTestUtil.setField(fragment, FIELD_HAS_CONVERSION_ERROR, false)
        ReflectionTestUtil.setField(fragment, FIELD_FILENAME, TEST_FILE_NAME)
        ReflectionTestUtil.setField(fragment, FIELD_SHOULD_RESET_PAGE, true)
        fragment.onFileInitialized(PDFFileManager.PDF_OK)
    }

    @Test
    fun testOnFileInitialized_Cancelled() {
        val fragment = getInitializedFragment()
        every { fragment.isDetached } returns false
        val mockView = mockView()
        every { fragment.view } returns mockView
        val mockFragmentActivity = mockk<MainActivity>(relaxed = true)
        val mockFragment = mockk<MenuFragment>(relaxed = true)
        every { mockFragment.setCurrentState(any()) } just Runs
        every { mockFragmentActivity.menuFragment } returns mockFragment
        every { mockFragmentActivity.cacheDir } returns MockTestUtil.cacheDir()
        addActivity(fragment, mockFragmentActivity)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        ReflectionTestUtil.setField(fragment, FIELD_OPEN_IN_VIEW, mockView)
        val mockProgressBar = mockk<ProgressBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PROGRESS_BAR, mockProgressBar)
        ReflectionTestUtil.setField(fragment, FIELD_HAS_CONVERSION_ERROR, false)
        fragment.onFileInitialized(PDFFileManager.PDF_CANCELLED)
    }

    @Test
    fun testOnFileInitialized_Detached() {
        val fragment = spyk<PrintPreviewFragment>()
        every { fragment.isDetached } returns true
        every { fragment.view } returns null
        ReflectionTestUtil.setField(fragment, FIELD_HAS_CONVERSION_ERROR, true)
        fragment.onFileInitialized(PDFFileManager.PDF_OK)
    }

    @Test
    fun testOnFileInitialized_NoView() {
        val fragment = spyk<PrintPreviewFragment>()
        every { fragment.isDetached } returns false
        every { fragment.view } returns null
        ReflectionTestUtil.setField(fragment, FIELD_HAS_CONVERSION_ERROR, true)
        fragment.onFileInitialized(PDFFileManager.PDF_OK)
    }

    @Test
    fun testOnFileInitialized_HasConversionError() {
        val fragment = spyk<PrintPreviewFragment>()
        every { fragment.isDetached } returns false
        every { fragment.view } returns mockView()
        ReflectionTestUtil.setField(fragment, FIELD_HAS_CONVERSION_ERROR, true)
        fragment.onFileInitialized(PDFFileManager.PDF_OK)
    }

    // ================================================================================
    // Tests - onFileConverted
    // ================================================================================
    @Test
    fun testOnFileConverted_OK() {
        val fragment = getInitializedFragment()
        every { fragment.isDetached } returns false
        val mockView = mockView()
        every { fragment.view } returns mockView
        fragment.onFileConverted(PDFConverterManager.CONVERSION_OK)
    }

    @Test
    fun testOnFileConverted_Error() {
        val fragment = getInitializedFragment()
        every { fragment.isDetached } returns false
        val mockView = mockView()
        every { fragment.view } returns mockView
        fragment.onFileConverted(PDFConverterManager.CONVERSION_FAILED)
    }

    @Test
    fun testOnFileConverted_IsDetached() {
        val fragment = getInitializedFragment()
        every { fragment.isDetached } returns true
        every { fragment.view } returns null
        fragment.onFileConverted(PDFConverterManager.CONVERSION_FAILED)
    }

    @Test
    fun testOnFileConverted_NoView() {
        val fragment = getInitializedFragment()
        every { fragment.isDetached } returns false
        every { fragment.view } returns null
        fragment.onFileConverted(PDFConverterManager.CONVERSION_FAILED)
    }

    // ================================================================================
    // Tests - onNotifyProgress
    // ================================================================================
    @Test
    fun testOnNotifyProgress_IsPercentage() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        val current = 0
        val total = 100
        val isPercentage = true
        fragment.onNotifyProgress(current, total, isPercentage)
    }

    @Test
    fun testOnNotifyProgress_IsNotPercentage() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, null)
        val current = 0
        val total = 100
        val isPercentage = false
        fragment.onNotifyProgress(current, total, isPercentage)
    }

    // ================================================================================
    // Tests - onIndexChanged
    // ================================================================================
    @Test
    fun testOnIndexChanged() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val mockTextView = mockk<TextView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_LABEL, mockTextView)
        fragment.onIndexChanged(0)
        fragment.panChanged(0f, 0f)
        fragment.zoomLevelChanged(0f)
        fragment.setControlsEnabled(true)
        Assert.assertEquals(0, fragment.controlsHeight)
    }

    // ================================================================================
    // Tests - onProgressChanged
    // ================================================================================
    @Test
    fun testOnProgressChanged() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockTextView = mockk<TextView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_LABEL, mockTextView)
        val seekBar = mockk<SeekBar>(relaxed = true)
        fragment.onProgressChanged(seekBar, 0, true)
        fragment.onProgressChanged(seekBar, 0, false)
        fragment.onStartTrackingTouch(seekBar)
        fragment.onStopTrackingTouch(seekBar)
    }

    // ================================================================================
    // Tests - processMessage
    // ================================================================================
    @Test
    fun testProcessMessage_ContentPrint() {
        ContentPrintManager.isFileFromContentPrint = true
        val fragment = getInitializedFragment()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val message = Message()
        message.what = R.id.view_id_print_button
        fragment.processMessage(message)
        fragment.processMessage(message)
        fragment.handleMessage(message)
        Assert.assertFalse(fragment.storeMessage(message))
    }

    @Test
    fun testProcessMessage_DirectPrint() {
        ContentPrintManager.isFileFromContentPrint = false
        val fragment = getInitializedFragment()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val message = Message()
        message.what = R.id.view_id_print_button
        fragment.processMessage(message)
        fragment.processMessage(message)
        fragment.handleMessage(message)
        Assert.assertFalse(fragment.storeMessage(message))
    }

    @Test
    fun testProcessMessage_OpenDrawer() {
        val fragment = getInitializedFragment()
        val message = Message()
        message.what = R.id.menu_id_action_button
        fragment.processMessage(message)
        fragment.processMessage(message)
        Assert.assertFalse(fragment.storeMessage(message))
    }

    @Test
    fun testProcessMessage_Other() {
        val fragment = getInitializedFragment()
        val message = Message()
        message.what = 0
        fragment.processMessage(message)
        fragment.processMessage(message)
        Assert.assertFalse(fragment.storeMessage(message))
    }

    // ================================================================================
    // Tests - togglePrintSettingsDrawer
    // ================================================================================
    @Test
    fun testTogglePrintSettingsDrawer_BoxRegistrationWithPrinter() {
        ContentPrintManager.isBoxRegistrationMode = true
        printerId = 1 // Not PrinterManager.EMPTY_ID
        val fragment = getInitializedFragment()
        fragment.togglePrintSettingsDrawer()
    }

    @Test
    fun testTogglePrintSettingsDrawer_BoxRegistrationNoPrinter() {
        ContentPrintManager.isBoxRegistrationMode = true
        printerId = PrinterManager.EMPTY_ID
        val fragment = getInitializedFragment()
        fragment.togglePrintSettingsDrawer()
    }

    @Test
    fun testTogglePrintSettingsDrawer_NotBoxRegistrationWithPrinter() {
        ContentPrintManager.isBoxRegistrationMode = false
        printerId = 1 // Not PrinterManager.EMPTY_ID
        val fragment = getInitializedFragment()
        fragment.togglePrintSettingsDrawer()
    }

    @Test
    fun testTogglePrintSettingsDrawer_NotBoxRegistrationNoPrinter() {
        ContentPrintManager.isBoxRegistrationMode = false
        printerId = PrinterManager.EMPTY_ID
        val fragment = getInitializedFragment()
        fragment.togglePrintSettingsDrawer()
    }

    @Test
    fun testTogglePrintSettingsDrawer_IsDrawerOpen() {
        ContentPrintManager.isBoxRegistrationMode = true
        printerId = PrinterManager.EMPTY_ID
        val fragment = spyk<PrintPreviewFragment>()
        setHandler(fragment)
        val mockActivity = mockk<MainActivity>(relaxed = true)
        every { mockActivity.isDrawerOpen(any()) } returns true
        every { fragment.activity } returns mockActivity
        every { fragment.context } returns mockActivity
        fragment.togglePrintSettingsDrawer()
    }

    @Test
    fun testTogglePrintSettingsDrawer_IsDrawerClosed() {
        ContentPrintManager.isBoxRegistrationMode = true
        printerId = PrinterManager.EMPTY_ID
        val fragment = spyk<PrintPreviewFragment>()
        setHandler(fragment)
        setPDFFileManager(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS, PrintSettings())
        val mockActivity = mockk<MainActivity>(relaxed = true)
        every { mockActivity.isDrawerOpen(any()) } returns false
        every { fragment.activity } returns mockActivity
        every { fragment.context } returns mockActivity
        fragment.togglePrintSettingsDrawer()
    }

    @Test
    fun testTogglePrintSettingsDrawer_NotMainActivity() {
        ContentPrintManager.isBoxRegistrationMode = true
        printerId = PrinterManager.EMPTY_ID
        val fragment = spyk<PrintPreviewFragment>()
        setHandler(fragment)
        val mockActivity = mockk<BaseActivity>(relaxed = true)
        every { fragment.activity } returns mockActivity
        every { fragment.context } returns mockActivity
        fragment.togglePrintSettingsDrawer()
    }

    // ================================================================================
    // Tests - onConfirm
    // ================================================================================
    @Test
    fun testOnConfirm_IsBoxRegistrationDialog() {
        ContentPrintManager.isFileFromContentPrint = true
        val fragment = getInitializedFragment()
        ReflectionTestUtil.setField(fragment, FIELD_IS_BOX_REGISTRATION_DIALOG, true)
        fragment.onConfirm()
    }

    @Test
    fun testOnConfirm_NotBoxRegistrationDialog() {
        ContentPrintManager.isFileFromContentPrint = true
        val fragment = spyk<PrintPreviewFragment>()
        val mockLauncher = mockk<ActivityResultLauncher<Array<String>>>()
        every { mockLauncher.launch(any()) } just Runs
        ReflectionTestUtil.setField(fragment, FIELD_RESULT_LAUNCHER, mockLauncher)
        ReflectionTestUtil.setField(fragment, FIELD_IS_BOX_REGISTRATION_DIALOG, false)
        fragment.onConfirm()
    }

    @Test
    fun testOnConfirm_NotContentPrint() {
        ContentPrintManager.isFileFromContentPrint = false
        val fragment = spyk<PrintPreviewFragment>()
        val mockLauncher = mockk<ActivityResultLauncher<Array<String>>>()
        every { mockLauncher.launch(any()) } just Runs
        ReflectionTestUtil.setField(fragment, FIELD_RESULT_LAUNCHER, mockLauncher)
        ReflectionTestUtil.setField(fragment, FIELD_IS_BOX_REGISTRATION_DIALOG, false)
        fragment.onConfirm()
    }

    // ================================================================================
    // Tests - onCancel
    // ================================================================================
    @Test
    fun testOnCancel_IsBoxRegistrationDialog() {
        ContentPrintManager.isFileFromContentPrint = true
        val fragment = getInitializedFragment()
        ReflectionTestUtil.setField(fragment, FIELD_IS_BOX_REGISTRATION_DIALOG, true)
        fragment.onCancel()
    }

    @Test
    fun testOnCancel_NotBoxRegistrationDialog() {
        ContentPrintManager.isFileFromContentPrint = true
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        setPDFConverterManager(fragment)
        val mockLauncher = mockk<ActivityResultLauncher<Array<String>>>()
        every { mockLauncher.launch(any()) } just Runs
        ReflectionTestUtil.setField(fragment, FIELD_RESULT_LAUNCHER, mockLauncher)
        ReflectionTestUtil.setField(fragment, FIELD_IS_BOX_REGISTRATION_DIALOG, false)
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        fragment.onCancel()
    }

    @Test
    fun testOnCancel_NotContentPrint() {
        ContentPrintManager.isFileFromContentPrint = false
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockLauncher = mockk<ActivityResultLauncher<Array<String>>>()
        every { mockLauncher.launch(any()) } just Runs
        ReflectionTestUtil.setField(fragment, FIELD_RESULT_LAUNCHER, mockLauncher)
        ReflectionTestUtil.setField(fragment, FIELD_IS_BOX_REGISTRATION_DIALOG, false)
        val mockConfirmDialog = mockk<ConfirmDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_CONFIRM_DIALOG, mockConfirmDialog)
        fragment.onCancel()
    }

    @Test
    fun testOnCancel_NoDialog() {
        ContentPrintManager.isFileFromContentPrint = false
        val fragment = spyk<PrintPreviewFragment>()
        val mockLauncher = mockk<ActivityResultLauncher<Array<String>>>()
        every { mockLauncher.launch(any()) } just Runs
        ReflectionTestUtil.setField(fragment, FIELD_RESULT_LAUNCHER, mockLauncher)
        ReflectionTestUtil.setField(fragment, FIELD_IS_BOX_REGISTRATION_DIALOG, false)
        fragment.onCancel()
    }

    // ================================================================================
    // Private Method Tests - hasPdfFile
    // ================================================================================
    @Test
    fun testHasPdfFile_NullIntent() {
        val fragment = getInitializedFragment(true)
        val mockActivity = mockFragmentActivity()
        every { mockActivity.intent } returns null
        addActivity(fragment, mockActivity)
        val result = ReflectionTestUtil.callMethod(fragment, METHOD_HAS_PDF_FILE) as Boolean
        Assert.assertFalse(result)
    }

    @Test
    fun testHasPdfFile_WithData() {
        val fragment = getInitializedFragment(true)
        val mockActivity = mockFragmentActivity()
        val mockIntent = mockk<Intent>(relaxed = true)
        val mockUri = mockk<Uri>(relaxed = true)
        every { mockIntent.data } returns mockUri
        every { mockIntent.clipData } returns null
        every { mockActivity.intent } returns mockIntent
        addActivity(fragment, mockActivity)
        val result = ReflectionTestUtil.callMethod(fragment, METHOD_HAS_PDF_FILE) as Boolean
        Assert.assertTrue(result)
    }

    @Test
    fun testHasPdfFile_WithClipData() {
        val fragment = getInitializedFragment(true)
        val mockActivity = mockFragmentActivity()
        val mockIntent = mockk<Intent>(relaxed = true)
        val mockClipData = mockk<ClipData>(relaxed = true)
        every { mockIntent.data } returns null
        every { mockIntent.clipData } returns mockClipData
        every { mockActivity.intent } returns mockIntent
        addActivity(fragment, mockActivity)
        val result = ReflectionTestUtil.callMethod(fragment, METHOD_HAS_PDF_FILE) as Boolean
        Assert.assertTrue(result)
    }

    // ================================================================================
    // Private Method Tests - initializePdfConverterAndRunAsync
    // ================================================================================
    @Test
    fun testInitializePdfConverterAndRunAsync() {
        val fragment = getInitializedFragment()
        val manager = setPDFConverterManager(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_INITIALIZE_PDF_CONVERTER)
        verify(exactly = 1) { manager.initializeAsync() }
    }

    // ================================================================================
    // Private Method Tests - initializePdfManagerAndRunAsync
    // ================================================================================
    @Test
    fun testInitializePdfManagerAndRunAsync_NotInitialized() {
        val fragment = getInitializedFragment()
        val manager = setPDFFileManager(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_INITIALIZE_PDF_FILE)
        verify(exactly = 0) { manager.initializeAsync(any()) }
    }

    @Test
    fun testInitializePdfManagerAndRunAsync_PDFFile() {
        val fragment = getInitializedFragment()
        val manager = setPDFFileManager(fragment)
        val mockIntentData = mockk<Uri>()
        every { mockIntentData.scheme } returns "file"
        every { mockIntentData.path } returns TEST_FILE_NAME
        ReflectionTestUtil.setField(fragment, FIELD_INTENT_DATA, mockIntentData)
        ReflectionTestUtil.callMethod(fragment, METHOD_INITIALIZE_PDF_FILE)
        verify(exactly = 1) { manager.initializeAsync(null) }
    }

    @Test
    fun testInitializePdfManagerAndRunAsync_NoFile() {
        val fragment = getInitializedFragment()
        val manager = setPDFFileManager(fragment)
        val mockIntentData = mockk<Uri>()
        every { mockIntentData.scheme } returns "http"
        ReflectionTestUtil.setField(fragment, FIELD_INTENT_DATA, mockIntentData)
        ReflectionTestUtil.callMethod(fragment, METHOD_INITIALIZE_PDF_FILE)
        verify(exactly = 1) { manager.initializeAsync(any()) }
    }

    // ================================================================================
    // Private Method Tests - setPrintId
    // ================================================================================
    @Test
    fun testSetPrintId_WithPreview() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val printerId = PrinterManager.EMPTY_ID
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PRINTER_ID,
            ReflectionTestUtil.Param(Int::class.java, printerId)
        )
    }

    @Test
    fun testSetPrintId_NoPreview() {
        val fragment = spyk<PrintPreviewFragment>()
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, null)
        val printerId = PrinterManager.EMPTY_ID
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PRINTER_ID,
            ReflectionTestUtil.Param(Int::class.java, printerId)
        )
    }

    // ================================================================================
    // Private Method Tests - setPrintSettings
    // ================================================================================
    @Test
    fun testSetPrintSettings_Null() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val mockTextView = mockk<TextView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_LABEL, mockTextView)
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PRINT_SETTINGS,
            ReflectionTestUtil.Param(PrintSettings::class.java, null)
        )
    }

    @Test
    fun testSetPrintSettings_WithPreview() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val mockTextView = mockk<TextView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_LABEL, mockTextView)
        val printSettings = PrintSettings()
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PRINT_SETTINGS,
            ReflectionTestUtil.Param(PrintSettings::class.java, printSettings)
        )
    }

    @Test
    fun testSetPrintSettings_NoPreview() {
        val fragment = spyk<PrintPreviewFragment>()
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, null)
        val printSettings = PrintSettings()
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PRINT_SETTINGS,
            ReflectionTestUtil.Param(PrintSettings::class.java, printSettings)
        )
    }

    // ================================================================================
    // Private Method Tests - setPrintPreviewViewDisplayed
    // ================================================================================
    @Test
    fun testSetPrintPreviewViewDisplayed_ShowInitialized() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val manager = setPDFFileManager(fragment)
        every { manager.isInitialized } returns true
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockView = mockView()
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        ReflectionTestUtil.setField(fragment, FIELD_OPEN_IN_VIEW, mockView)
        val mockProgressBar = mockk<ProgressBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PROGRESS_BAR, mockProgressBar)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        val mockTextView = mockk<TextView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_LABEL, mockTextView)
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PREVIEW_DISPLAYED,
            ReflectionTestUtil.Param(View::class.java, mockView),
            ReflectionTestUtil.Param(Boolean::class.java, true)
        )
    }

    @Test
    fun testSetPrintPreviewViewDisplayed_ShowNotInitialized() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val manager = setPDFFileManager(fragment)
        every { manager.isInitialized } returns false
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockView = mockView()
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        ReflectionTestUtil.setField(fragment, FIELD_OPEN_IN_VIEW, mockView)
        val mockProgressBar = mockk<ProgressBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PROGRESS_BAR, mockProgressBar)
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PREVIEW_DISPLAYED,
            ReflectionTestUtil.Param(View::class.java, mockView),
            ReflectionTestUtil.Param(Boolean::class.java, true)
        )
    }

    @Test
    fun testSetPrintPreviewViewDisplayed_Hide() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        setPDFFileManager(fragment)
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockView = mockView()
        ReflectionTestUtil.setField(fragment, FIELD_PAGE_CONTROLS, mockView)
        ReflectionTestUtil.setField(fragment, FIELD_OPEN_IN_VIEW, mockView)
        val mockProgressBar = mockk<ProgressBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PROGRESS_BAR, mockProgressBar)
        ReflectionTestUtil.callMethod(fragment, METHOD_SET_PREVIEW_DISPLAYED,
            ReflectionTestUtil.Param(View::class.java, mockView),
            ReflectionTestUtil.Param(Boolean::class.java, false)
        )
    }

    // ================================================================================
    // Private Method Tests - showPrintSettingsButton
    // ================================================================================
    @Test
    fun testShowPrintSettingsButton_NoButton() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.findViewById<View>(R.id.view_id_print_button) } returns null
        ReflectionTestUtil.callMethod(fragment, METHOD_SHOW_PRINT_SETTINGS,
            ReflectionTestUtil.Param(View::class.java, mockView),
            ReflectionTestUtil.Param(Boolean::class.java, false)
        )
    }

    @Test
    fun testShowPrintSettingsButton_Show() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.findViewById<View>(R.id.view_id_print_button) } returns mockView
        ReflectionTestUtil.callMethod(fragment, METHOD_SHOW_PRINT_SETTINGS,
            ReflectionTestUtil.Param(View::class.java, mockView),
            ReflectionTestUtil.Param(Boolean::class.java, true)
        )
    }

    @Test
    fun testShowPrintSettingsButton_Hide() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.findViewById<View>(R.id.view_id_print_button) } returns mockView
        ReflectionTestUtil.callMethod(fragment, METHOD_SHOW_PRINT_SETTINGS,
            ReflectionTestUtil.Param(View::class.java, mockView),
            ReflectionTestUtil.Param(Boolean::class.java, false)
        )
    }

    // ================================================================================
    // Private Method Tests - getPdfErrorMessage
    // ================================================================================
    @Test
    fun testGetPdfErrorMessage_PDFEncrypted() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_PDF_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFFileManager.PDF_ENCRYPTED)
        )
    }

    @Test
    fun testGetPdfErrorMessage_PDFPrintRestricted() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_PDF_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFFileManager.PDF_PRINT_RESTRICTED)
        )
    }

    @Test
    fun testGetPdfErrorMessage_OpenFailed() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_PDF_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFFileManager.PDF_OPEN_FAILED)
        )
    }

    @Test
    fun testGetPdfErrorMessage_NotEnoughSpace() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_PDF_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFFileManager.PDF_NOT_ENOUGH_FREE_SPACE)
        )
    }

    @Test
    fun testGetPdfErrorMessage_Cancelled() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val result = ReflectionTestUtil.callMethod(fragment, METHOD_GET_PDF_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFFileManager.PDF_CANCELLED)
        ) as String
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun testGetPdfErrorMessage_NoError() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val result = ReflectionTestUtil.callMethod(fragment, METHOD_GET_PDF_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFFileManager.PDF_OK)
        ) as String
        Assert.assertTrue(result.isEmpty())
    }

    // ================================================================================
    // Private Method Tests - getConversionErrorMessage
    // ================================================================================
    @Test
    fun testGetConversionErrorMessage_Failed() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_CONVERSION_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFConverterManager.CONVERSION_FAILED)
        )
    }

    @Test
    fun testGetConversionErrorMessage_Unsupported() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_CONVERSION_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFConverterManager.CONVERSION_UNSUPPORTED)
        )
    }

    @Test
    fun testGetConversionErrorMessage_ExceedFileSizeLimit() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_CONVERSION_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFConverterManager.CONVERSION_EXCEED_TEXT_FILE_SIZE_LIMIT)
        )
    }

    @Test
    fun testGetConversionErrorMessage_SecurityError() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_CONVERSION_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFConverterManager.CONVERSION_SECURITY_ERROR)
        )
    }

    @Test
    fun testGetConversionErrorMessage_NotFound() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        ReflectionTestUtil.callMethod(fragment, METHOD_GET_CONVERSION_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFConverterManager.CONVERSION_FILE_NOT_FOUND)
        )
    }

    @Test
    fun testGetConversionErrorMessage_NoError() {
        val fragment = spyk<PrintPreviewFragment>()
        addActivity(fragment)
        val result = ReflectionTestUtil.callMethod(fragment, METHOD_GET_CONVERSION_ERROR,
            ReflectionTestUtil.Param(Int::class.java, PDFConverterManager.CONVERSION_OK)
        ) as String
        Assert.assertTrue(result.isEmpty())
    }

    // ================================================================================
    // Private Method Tests - transitionToHomeScreen
    // ================================================================================
    @Test
    fun testTransitionToHomeScreen_WithMenu() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockFragmentActivity = mockk<MainActivity>(relaxed = true)
        val mockFragment = mockk<MenuFragment>(relaxed = true)
        every { mockFragment.setCurrentState(any()) } just Runs
        every { mockFragmentActivity.menuFragment } returns mockFragment
        addActivity(fragment, mockFragmentActivity)
        ReflectionTestUtil.callMethod(fragment, METHOD_TRANSITION_HOME)
    }

    @Test
    fun testTransitionToHomeScreen_NoMenu() {
        try {
            val fragment = spyk<PrintPreviewFragment>()
            val mockFragmentActivity = mockk<MainActivity>(relaxed = true)
            val mockFragment = mockk<MenuFragment>(relaxed = true)
            every { mockFragment.setCurrentState(any()) } just Runs
            every { mockFragmentActivity.menuFragment } returns null
            val mockFragmentManager = mockk<FragmentManager>(relaxed = true)
            val mockTransaction = mockk<FragmentTransaction>(relaxed = true)
            every { mockFragmentManager.beginTransaction() } returns mockTransaction
            every { mockFragmentActivity.supportFragmentManager } returns mockFragmentManager
            addActivity(fragment, mockFragmentActivity)
            ReflectionTestUtil.callMethod(fragment, METHOD_TRANSITION_HOME)
        } catch (_: InvocationTargetException) {
            // Expected IllegalStateException: Fragment MenuFragment not associated with a fragment manager
            // From MenuFragment.switchToFragment
        }
    }

    // ================================================================================
    // Private Method Tests - updateSeekBar
    // ================================================================================
    @Test
    fun testUpdateSeekBar() {
        val fragment = spyk<PrintPreviewFragment>()
        val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
        every { mockPrintPreviewView.allowLastPageCurl } returns true
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_PREVIEW, mockPrintPreviewView)
        val mockSeekBar = mockk<SeekBar>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_SEEK_BAR, mockSeekBar)
        ReflectionTestUtil.callMethod(fragment, METHOD_UPDATE_SEEK_BAR)
    }

    // ================================================================================
    // Private Functions
    // ================================================================================
    private fun getInitializedFragment(withoutActivity: Boolean = false): PrintPreviewFragment {
        val fragment = spyk<PrintPreviewFragment>()
        if (!withoutActivity) {
            addActivity(fragment)
        }
        setPDFConverterManager(fragment)
        setPDFFileManager(fragment)
        setHandler(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_PRINTER_ID, 1)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS, PrintSettings(AppConstants.PRINTER_MODEL_GL))
        return fragment
    }

    private fun addActivity(fragment: PrintPreviewFragment,
                            activity: FragmentActivity? = fragmentActivity) {
        every { fragment.activity } returns activity
        every { fragment.context } returns activity
    }

    private fun setPDFConverterManager(fragment: PrintPreviewFragment): PDFConverterManager {
        // mockkConstructor is not working for PDFConverterManager
        // Use Java reflection to replace PDFConverterManager with a mock object
        val mockConverterManager = mockk<PDFConverterManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PDF_CONVERTER, mockConverterManager)
        return mockConverterManager
    }

    private fun setPDFFileManager(fragment: PrintPreviewFragment): PDFFileManager {
        // mockkConstructor is not working for PDFFileManager
        // Use Java reflection to replace PDFFileManager with a mock object
        val mockFileManager = mockk<PDFFileManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PDF_FILE, mockFileManager)
        return mockFileManager
    }

    private fun setHandler(fragment: PrintPreviewFragment, withoutCache: Boolean = false) {
        // Use Java reflection to replace PauseableHandler with a mock object
        val mockPauseableHandler = mockk<PauseableHandler>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAUSEABLE_HANDLER, mockPauseableHandler)
        // Use Java reflection to replace Handler with a mock object
        val mockHandler = mockk<Handler>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_HANDLER, mockHandler)
        if (!withoutCache) {
            // Use Java reflection to replace LruCache with a mock object
            val mockCache = mockk<LruCache<String, Bitmap>>(relaxed = true)
            ReflectionTestUtil.setField(fragment, FIELD_CACHE, mockCache)
        }
    }

    companion object {
        private const val TEST_FILE_NAME = "test.pdf"
        private const val FIELD_INTENT_DATA = "_intentData"
        private const val FIELD_PDF_CONVERTER = "_pdfConverterManager"
        private const val FIELD_PDF_FILE = "_pdfManager"
        private const val FIELD_PAUSEABLE_HANDLER = "_pauseableHandler"
        private const val FIELD_HANDLER = "_handler"
        private const val FIELD_CACHE = "_bmpCache"
        private const val FIELD_PRINTER_ID = "_printerId"
        private const val FIELD_PRINT_SETTINGS = "_printSettings"
        private const val FIELD_PRINT_PREVIEW = "_printPreviewView"
        private const val FIELD_PAGE_CONTROLS = "_pageControls"
        private const val FIELD_OPEN_IN_VIEW = "_openInView"
        private const val FIELD_PROGRESS_BAR = "_progressBar"
        private const val FIELD_SEEK_BAR = "_seekBar"
        private const val FIELD_PAGE_LABEL = "_pageLabel"
        private const val FIELD_WAITING_DIALOG = "_waitingDialog"
        private const val FIELD_CONFIRM_DIALOG = "_confirmDialogFragment"
        private const val FIELD_FILENAME = "_filenameFromContent"
        private const val FIELD_PAGE = "_currentPage"
        private const val FIELD_IS_PDF_INITIALIZED = "_isPdfInitialized"
        private const val FIELD_IS_CONVERTER_INITIALIZED = "_isConverterInitialized"
        private const val FIELD_IS_PERMISSION_DIALOG = "_isPermissionDialogOpen"
        private const val FIELD_SHOULD_DISPLAY_EXPLANATION = "_shouldDisplayExplanation"
        private const val FIELD_HAS_CONVERSION_ERROR = "_hasConversionError"
        private const val FIELD_SHOULD_RESET_PAGE = "_shouldResetToFirstPageOnInitialize"
        private const val FIELD_IS_BOX_REGISTRATION_DIALOG = "_isBoxRegistrationDialog"
        private const val FIELD_RESULT_LAUNCHER ="_resultLauncherPermissionStorage"

        private const val METHOD_HAS_PDF_FILE = "hasPdfFile"
        private const val METHOD_INITIALIZE_PDF_CONVERTER = "initializePdfConverterAndRunAsync"
        private const val METHOD_INITIALIZE_PDF_FILE = "initializePdfManagerAndRunAsync"
        private const val METHOD_SET_PRINTER_ID = "setPrintId"
        private const val METHOD_SET_PRINT_SETTINGS = "setPrintSettings"
        private const val METHOD_SET_PREVIEW_DISPLAYED = "setPrintPreviewViewDisplayed"
        private const val METHOD_SHOW_PRINT_SETTINGS = "showPrintSettingsButton"
        private const val METHOD_GET_PDF_ERROR = "getPdfErrorMessage"
        private const val METHOD_GET_CONVERSION_ERROR = "getConversionErrorMessage"
        private const val METHOD_TRANSITION_HOME = "transitionToHomeScreen"
        private const val METHOD_UPDATE_SEEK_BAR = "updateSeekBar"

        private var fragmentActivity: FragmentActivity? = null
        private val selectedFile = ContentPrintFile(1, TEST_FILE_NAME, ContentPrintPrintSettings())
        private var printerId = PrinterManager.EMPTY_ID
        private var printerExists = false
        private var printerType: String? = AppConstants.PRINTER_MODEL_GL

        private fun mockBundle(): Bundle {
            val mockBundle = mockk<Bundle>(relaxed = true)
            every { mockBundle.getInt(any(), any()) } returns 1 // KEY_CURRENT_PAGE
            every { mockBundle.getBoolean(any(), any()) } returns false // KEY_EXTERNAL_STORAGE_DIALOG_OPEN
            val mockPoint = mockk<Point>(relaxed = true)
            every { mockBundle.getParcelable<Point>(any(), any()) } returns mockPoint // KEY_SCREEN_SIZE
            return mockBundle
        }

        private fun mockView(): View {
            val mockView = mockk<View>(relaxed = true)
            val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
            every { mockView.findViewById<View>(R.id.printPreviewView) } returns mockPrintPreviewView
            every { mockView.findViewById<View>(R.id.previewControls) } returns mockView
            val mockLayout = mockk<LinearLayout>(relaxed = true)
            every { mockView.findViewById<LinearLayout>(R.id.previewView) } returns mockLayout
            val mockTextView = mockk<TextView>(relaxed = true)
            every { mockView.findViewById<TextView>(R.id.pageDisplayTextView) } returns mockTextView
            every { mockView.findViewById<TextView>(R.id.actionBarTitle) } returns mockTextView
            val mockSeekBar = mockk<SeekBar>(relaxed = true)
            every { mockView.findViewById<SeekBar>(R.id.pageSlider) } returns mockSeekBar
            every { mockView.findViewById<View>(R.id.openInView) } returns mockView
            val mockProgressBar = mockk<ProgressBar>(relaxed = true)
            every { mockView.findViewById<ProgressBar>(R.id.pdfLoadIndicator) } returns mockProgressBar
            every { mockView.context } returns fragmentActivity
            val mockViewGroup = mockk<ViewGroup>(relaxed = true)
            every { mockView.findViewById<ViewGroup>(R.id.leftActionLayout) } returns mockViewGroup
            every { mockView.findViewById<ViewGroup>(R.id.rightActionLayout) } returns mockViewGroup
            return mockView
        }

        private fun mockFragmentActivity(): FragmentActivity {
            val mockFragmentActivity = mockk<MainActivity>(relaxed = true)
            every { mockFragmentActivity.cacheDir } returns MockTestUtil.cacheDir()
            val mockActivityManager = mockk<ActivityManager>(relaxed = true)
            every { mockActivityManager.memoryClass } returns 1
            every { mockFragmentActivity.getSystemService(Context.ACTIVITY_SERVICE) } returns mockActivityManager
            val mockInflater = mockk<LayoutInflater>(relaxed = true)
            val mockImageView = mockk<ImageView>(relaxed = true)
            every { mockInflater.inflate(R.layout.actionbar_button, any() )} returns mockImageView
            every { mockFragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) } returns mockInflater
            val mockInputMethod = mockk<InputMethodManager>(relaxed = true)
            every { mockFragmentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) } returns mockInputMethod
            every { mockFragmentActivity.runOnUiThread(any()) } answers {
                val runnable = firstArg<Runnable>()
                runnable.run()
            }
            return mockFragmentActivity
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            fragmentActivity = mockFragmentActivity()
            MockTestUtil.mockUI()

            mockkObject(PrinterManager)
            val mockPrinterManager = mockk<PrinterManager>()
            every { mockPrinterManager.isExists(any<Int>()) } returns printerExists
            every { mockPrinterManager.getPrinterType(any()) } returns printerType
            every { mockPrinterManager.defaultPrinter } returns printerId
            every { PrinterManager.getInstance(any<Context>()) } returns mockPrinterManager
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            MockTestUtil.unMockUI()
            unmockkObject(PrinterManager)
        }
    }
}