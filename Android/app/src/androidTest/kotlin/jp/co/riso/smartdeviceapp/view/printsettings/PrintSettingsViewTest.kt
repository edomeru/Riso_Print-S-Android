package jp.co.riso.smartdeviceapp.view.printsettings

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.text.InputType
import android.view.InputDevice
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.MockTestUtil
import jp.co.riso.smartdeviceapp.ReflectionTestUtil
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManagerTest
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartdeviceapp.model.ContentPrintPrintSettings
import jp.co.riso.smartdeviceapp.model.ContentPrintPrinter
import jp.co.riso.smartdeviceapp.model.ContentPrintPrinterCapabilities
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.printsettings.Option
import jp.co.riso.smartdeviceapp.model.printsettings.Preview
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.model.printsettings.Setting
import jp.co.riso.smartprint.R
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test


class PrintSettingsViewTest {
    class TestPrintSettingsViewInterface: PrintSettingsView.PrintSettingsViewInterface {
        var selectedPrinterId = -1
        var selectedPrintSettings: PrintSettings? = null
        var printPrinter: Printer? = null
        var printPrintSettings: PrintSettings? = null

        override fun onPrinterIdSelectedChanged(printerId: Int) {
            selectedPrinterId = printerId
        }

        override fun onPrintSettingsValueChanged(printSettings: PrintSettings?) {
            selectedPrintSettings = printSettings
        }

        override fun onPrint(printer: Printer?, printSettings: PrintSettings?) {
            printPrinter = printer
            printPrintSettings = printSettings
        }
    }

    @Test
    fun testInit_ContentPrint() {
        val context = mockContext()
        ContentPrintManager.isBoxRegistrationMode = true
        val printSettingsView = PrintSettingsView(context, null, 0)
        Assert.assertNotNull(printSettingsView)
    }

    // ================================================================================
    // Tests - onInterceptTouchEvent
    // ================================================================================
    @Test
    fun testOnInterceptTouchEvent_MouseClick() {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.isFromSource(InputDevice.SOURCE_MOUSE) } returns true
        every { event.rawX } returns 1.0f
        every { event.rawY } returns 1.0f
        every { event.action } returns MotionEvent.ACTION_DOWN
        val result = _printSettingsView!!.onInterceptTouchEvent(event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnInterceptTouchEvent_MouseMove() {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.isFromSource(InputDevice.SOURCE_MOUSE) } returns true
        every { event.rawX } returns 1.0f
        every { event.rawY } returns 1.0f
        every { event.action } returns MotionEvent.ACTION_MOVE
        val result = _printSettingsView!!.onInterceptTouchEvent(event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnInterceptTouchEvent_MouseScroll() {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.isFromSource(InputDevice.SOURCE_MOUSE) } returns true
        every { event.rawX } returns 1.0f
        every { event.rawY } returns 1.0f
        every { event.action } returns MotionEvent.ACTION_SCROLL
        val result = _printSettingsView!!.onInterceptTouchEvent(event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnInterceptTouchEvent_KeyPress() {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.isFromSource(InputDevice.SOURCE_MOUSE) } returns false
        every { event.rawX } returns 0.0f
        every { event.rawY } returns 0.0f
        every { event.action } returns MotionEvent.ACTION_DOWN
        val result = _printSettingsView!!.onInterceptTouchEvent(event)
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Tests - onGenericMotionEvent
    // ================================================================================
    @Test
    fun testOnGenericMotionEvent_KeyPress() {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.isFromSource(InputDevice.SOURCE_MOUSE) } returns true
        every { event.actionMasked  } returns MotionEvent.ACTION_SCROLL
        val result = _printSettingsView!!.onGenericMotionEvent(event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnGenericMotionEvent_MouseScroll() {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.isFromSource(InputDevice.SOURCE_MOUSE) } returns true
        every { event.actionMasked  } returns MotionEvent.ACTION_SCROLL
        val result = _printSettingsView!!.onGenericMotionEvent(event)
        Assert.assertFalse(result)
    }

    @Test
    fun testOnGenericMotionEvent_MouseMove() {
        val event = mockk<MotionEvent>(relaxed = true)
        every { event.isFromSource(InputDevice.SOURCE_MOUSE) } returns true
        every { event.actionMasked  } returns MotionEvent.ACTION_MOVE
        val result = _printSettingsView!!.onGenericMotionEvent(event)
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Tests - saveState
    // ================================================================================
    @Test
    fun testSaveState() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.tag } returns "test"
        val subView = ReflectionTestUtil.getField(_printSettingsView!!, FIELD_SUB_VIEW)
        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_SUB_VIEW, mockLayout)
        val scrollView = mockk<ScrollView>(relaxed = true)
        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_SUB_SCROLL_VIEW, scrollView)

        val bundle = mockk<Bundle>(relaxed = true)
        _printSettingsView!!.saveState(bundle)
        _printSettingsView!!.restoreState(bundle)

        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Tests - setShowPrintControls
    // ================================================================================
    @Test
    fun testSetShowPrintControls() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.getTag(any()) } returns mockView
        every { mockLayout.findViewWithTag<View>(any()) } returns mockView
        every { mockLayout.findViewById<View>(any()) } returns mockView
        setLayout(FIELD_PRINT_CONTROLS, mockLayout)
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        _printSettingsView!!.setShowPrintControls(true)
        _printSettingsView!!.setShowPrintControls(false)
        setLayout(FIELD_PRINT_CONTROLS, _printControls)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    // ================================================================================
    // Tests - getPrinterFromList
    // ================================================================================
    @Test
    fun testGetPrinterFromList() {
        ContentPrintManager.isBoxRegistrationMode = false
        var printer = _printSettingsView!!.cdsPrinter
        Assert.assertNull(printer)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = null
        ContentPrintManager.printerList = ArrayList()
        printer = _printSettingsView!!.cdsPrinter
        Assert.assertNull(printer)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = null
        ContentPrintManager.printerList = listOf(selectedPrinterFT, selectedPrinterGL)
        printer = _printSettingsView!!.cdsPrinter
        Assert.assertNotNull(printer)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        printer = _printSettingsView!!.cdsPrinter
        Assert.assertNotNull(printer)
    }

    // ================================================================================
    // Tests - onClick
    // ================================================================================
    @Test
    fun testOnClick_CollapseContainer() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_collapse_container
        every { mockView.getTag(any()) } returns mockView
        _printSettingsView!!.onClick(mockView)
    }

    @Test
    fun testOnClick_ShowContainer_Disabled() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.isEnabled } returns false
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_show_subview_container
        every { mockView.tag } returns KEY_TAG_PRINTER
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test
    fun testOnClick_ShowContainer_Enabled() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_show_subview_container
        every { mockView.tag } returns KEY_TAG_PRINTER
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.isEnabled } returns true
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_MAIN_VIEW, _mainView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testOnClick_HideContainer_Enabled() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_hide_subview_container
        every { mockView.tag } returns KEY_TAG_PRINTER
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.isEnabled } returns true
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testOnClick_HideContainer_Disabled() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_hide_subview_container
        every { mockView.tag } returns KEY_TAG_PRINTER
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.isEnabled } returns false
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testOnClick_SubviewOption() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_subview_option_item
        every { mockView.tag } returns 0
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.tag } returns PrintSettings.TAG_COLOR_MODE
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testOnClick_SubviewPrinter() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_subview_printer_item
        every { mockView.tag } returns 0
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.tag } returns PrintSettings.TAG_COLOR_MODE
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testOnClick_SelectedPrinter_Enabled() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.printerList = listOf(selectedPrinterFT, selectedPrinterGL, selectedPrinterUnknown)
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.isEnabled } returns true
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_print_selected_printer
        every { mockView.tag } returns KEY_TAG_PRINTER
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_SUB_VIEW, _mainView)
    }

    @Test
    fun testOnClick_SelectedPrinter_Disabled() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.printerList = listOf(selectedPrinterFT, selectedPrinterGL, selectedPrinterUnknown)
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.isEnabled } returns false
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_print_selected_printer
        every { mockView.tag } returns KEY_TAG_PRINTER
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_SUB_VIEW, _mainView)
    }

    @Test
    fun testOnClick_ContentPrint() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedFile = selectedFile
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_print_header
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val mockEditText = mockk<EditText>(relaxed = true)
        every { mockLayout.findViewById<View>(any()) } returns mockEditText
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        _printSettingsView!!.onClick(mockView)
        val callback = ContentPrintManagerTest.TestRegisterToBoxCallback()
        _printSettingsView!!.setRegisterToBoxCallback(callback)
        _printSettingsView!!.onClick(mockView)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test
    fun testOnClick_DirectPrint() {
        ContentPrintManager.isBoxRegistrationMode = false
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_print_header
        _printSettingsView!!.onClick(mockView)
    }

    @Test
    fun testOnClick_Unknown() {
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns 0
        _printSettingsView!!.onClick(mockView)
    }

    // ================================================================================
    // Tests - handleMessage
    // ================================================================================
    @Test
    fun testHandleMessage() {
        // Store the print setting titles
        val printSettingsTitles = ReflectionTestUtil.getField(
            _printSettingsView!!, FIELD_PRINT_SETTING_TITLES
        )

        val scrollView = mockk<ScrollView>(relaxed = true)
        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_SUB_SCROLL_VIEW, scrollView)

        val layout = mockk<LinearLayout>(relaxed = true)
        val view = mockk<View>(relaxed = true)
        every { layout.getTag(ID_COLLAPSE_TARGET_GROUP) } returns view
        val titles = arrayListOf(layout)
        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_PRINT_SETTING_TITLES, titles)

        val mockView = mockSubview()
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.findViewWithTag<View>(any()) } returns mockView
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)

        val message = Message()
        message.arg1 = 0
        message.what = MSG_COLLAPSE
        val result1 = _printSettingsView!!.handleMessage(message)
        message.what = MSG_EXPAND
        val result2 = _printSettingsView!!.handleMessage(message)
        message.what = MSG_SET_SCROLL
        val result3 = _printSettingsView!!.handleMessage(message)
        message.what = MSG_SLIDE_IN
        val result4 = _printSettingsView!!.handleMessage(message)
        message.what = MSG_SLIDE_OUT
        val result5 = _printSettingsView!!.handleMessage(message)
        message.what = MSG_SET_SUB_SCROLL
        val result6 = _printSettingsView!!.handleMessage(message)
        message.what = MSG_SHOW_SUBVIEW
        message.obj = KEY_TAG_PRINTER
        val result7 = _printSettingsView!!.handleMessage(message)
        message.obj = "test"
        _printSettingsView!!.handleMessage(message)
        message.what = 100
        val result8 = _printSettingsView!!.handleMessage(message)

        // Revert the print setting titles
        ReflectionTestUtil.setField(
            _printSettingsView!!, FIELD_PRINT_SETTING_TITLES, printSettingsTitles
        )
        setLayout(FIELD_MAIN_VIEW, _mainView)
        setLayout(FIELD_SUB_VIEW, subView)

        Assert.assertTrue(result1)
        Assert.assertTrue(result2)
        Assert.assertTrue(result3)
        Assert.assertTrue(result4)
        Assert.assertTrue(result5)
        Assert.assertTrue(result6)
        Assert.assertTrue(result7)
        Assert.assertFalse(result8)
    }

    // ================================================================================
    // Tests - onCheckedChanged
    // ================================================================================
    @Test
    fun testOnCheckedChanged() {
        var buttonView = mockk<CompoundButton>(relaxed = true)
        every { buttonView.id } returns R.id.view_id_secure_print_switch
        every { buttonView.tag } returns "test"
        _printSettingsView!!.onCheckedChanged(buttonView, true)

        buttonView = mockk<CompoundButton>(relaxed = true)
        every { buttonView.id } returns R.id.view_id_pin_code_edit_text
        every { buttonView.tag } returns "test"
        _printSettingsView!!.onCheckedChanged(buttonView, true)
        _printSettingsView!!.onCheckedChanged(buttonView, false)
    }

    // ================================================================================
    // Tests - updateOnlineStatus
    // ================================================================================
    @Test
    fun testUpdateOnlineStatus_ContentPrint() {
        ContentPrintManager.isBoxRegistrationMode = true
        _printSettingsView!!.updateOnlineStatus()
    }

    @Test
    fun testUpdateOnlineStatus_DirectPrint_WithoutSubview() {
        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterList(printerList)
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, null)
        _printSettingsView!!.updateOnlineStatus()
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testUpdateOnlineStatus_DirectPrint_WithoutView() {
        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterList(printerList)
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.findViewWithTag<View>(any()) } returns null
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        _printSettingsView!!.updateOnlineStatus()
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testUpdateOnlineStatus_DirectPrint_WithSubview() {
        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterList(printerList)
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.findViewById<View>(any()) } returns mockView
        every { mockLayout.findViewWithTag<View>(any()) } returns mockView
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        _printSettingsView!!.updateOnlineStatus()
        setLayout(FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Tests - onEditorAction
    // ================================================================================
    @Test
    fun testOnEditorAction() {
        val mockTextView = mockk<TextView>(relaxed = true)
        every { mockTextView.inputType } returns InputType.TYPE_CLASS_TEXT
        every { mockTextView.id } returns R.id.view_id_pin_code_edit_text

        var result = _printSettingsView!!.onEditorAction(mockTextView, EditorInfo.IME_ACTION_DONE, null)
        Assert.assertFalse(result)
        result = _printSettingsView!!.onEditorAction(mockTextView, EditorInfo.IME_NULL, null)
        Assert.assertTrue(result)

        var mockEvent = mockk<KeyEvent>(relaxed = true)
        every { mockEvent.action } returns KeyEvent.ACTION_UP
        result = _printSettingsView!!.onEditorAction(mockTextView, EditorInfo.IME_NULL, mockEvent)
        Assert.assertTrue(result)

        mockEvent = mockk<KeyEvent>(relaxed = true)
        every { mockEvent.action } returns KeyEvent.ACTION_DOWN
        result = _printSettingsView!!.onEditorAction(mockTextView, EditorInfo.IME_NULL, mockEvent)
        Assert.assertTrue(result)

        result = _printSettingsView!!.onEditorAction(mockTextView, EditorInfo.IME_ACTION_GO, null)
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Private Method Tests - loadPrintersList
    // ================================================================================
    @Test
    fun testLoadPrintersList_InDirectPrintMode() {
        ContentPrintManager.isBoxRegistrationMode = false
        callLoadPrintersList()
    }

    @Test
    fun testLoadPrintersList_InBoxRegistrationMode() {
        ContentPrintManager.isBoxRegistrationMode = true
        callLoadPrintersList()
    }

    // ================================================================================
    // Private Method Tests - shouldDisplayOptionFromConstraints
    // ================================================================================
    @Test
    fun testShouldDisplayOptionFromConstraints_StapleOff() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = null

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.TOP.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_STAPLE,
            Preview.Staple.OFF.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_StapleTopLeft() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_STAPLE,
            Preview.Staple.ONE_UL.ordinal)
        Assert.assertFalse(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_StapleTop() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_STAPLE,
            Preview.Staple.ONE.ordinal)
        Assert.assertFalse(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_ImpositionOrderRightToLeft() {
        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_GL)

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_IMPOSITION_ORDER,
            Preview.ImpositionOrder.R_L.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_ImpositionOrderTopRightToLeft() {
        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_IMPOSITION_ORDER,
        Preview.ImpositionOrder.TR_L.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_OutputTrayAuto() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_2.ordinal)
        printSettings.setValue(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.PAPER_FOLDING.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.AUTO.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_OutputTrayStacking() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PUNCH, Preview.Punch.OFF.ordinal)
        printSettings.setValue(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.OFF.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.STACKING.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_OutputTrayFaceDown() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.FACEDOWN.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_PaperSizeA4() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_PAPER_SIZE,
            Preview.PaperSize.A4.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_PaperSizeA3() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)
        setPrintSettings(printSettings)

        val result1 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_PAPER_SIZE,
            Preview.PaperSize.A3.ordinal)
        Assert.assertTrue(result1)

        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)

        val result2 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_PAPER_SIZE,
            Preview.PaperSize.A3.ordinal)
        Assert.assertFalse(result2)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_InputTrayAuto() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A4.ordinal)
        setPrintSettings(printSettings)

        val result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_InputTrayTray3() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL

        var printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.B5.ordinal)
        setPrintSettings(printSettings)

        val result1 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal)
        Assert.assertTrue(result1)

        ContentPrintManager.selectedPrinter = selectedPrinterFT

        val result2 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal)
        Assert.assertTrue(result2)

        ContentPrintManager.selectedPrinter = null

        val result3 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal)
        Assert.assertTrue(result3)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_GL)

        printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.LETTER.ordinal)
        setPrintSettings(printSettings)

        val result4 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal)
        Assert.assertTrue(result4)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_InputTrayExternalFeeder() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.JUROKUKAI.ordinal)
        setPrintSettings(printSettings)

        val result1 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        Assert.assertTrue(result1)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_GL)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.LEGAL.ordinal)
        setPrintSettings(printSettings)

        val result2 = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        Assert.assertFalse(result2)
    }

    @Test
    fun testShouldDisplayOptionFromConstraints_ColorMode() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT

        var result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_COLOR_MODE,
            Preview.ColorMode.FULL_COLOR.ordinal)
        Assert.assertTrue(result)


        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        result = callShouldDisplayOptionFromConstraints(PrintSettings.TAG_COLOR_MODE,
            Preview.ColorMode.FULL_COLOR.ordinal)
        Assert.assertTrue(result)
    }

    // ================================================================================
    // Private Method Tests - getDefaultValueWithConstraints
    // ================================================================================
    @Test
    fun testGetDefaultValueWithConstraints_Null() {
        setPrintSettings(null)
        val tag = PrintSettings.TAG_COLOR_MODE
        val result = callGetDefaultValueWithConstraints(tag)
        Assert.assertEquals(-1, result)
    }

    @Test
    fun testGetDefaultValueWithConstraints_Unknown() {
        val printSettings = PrintSettings()
        // The value cannot be set during construction (NullPointerException)
        val field = printSettings.javaClass.getDeclaredField("settingMapKey")
        field.isAccessible = true
        field.set(printSettings, "Unknown")
        setPrintSettings(printSettings)
        val tag = PrintSettings.TAG_COLOR_MODE
        val result = callGetDefaultValueWithConstraints(tag)
        Assert.assertEquals(-1, result)
    }

    @Test
    fun testGetDefaultValueWithConstraints_UnknownTag() {
        val printSettings = PrintSettings()
        setPrintSettings(printSettings)
        val tag = "Unknown"
        val result = callGetDefaultValueWithConstraints(tag)
        Assert.assertEquals(-1, result)
    }

    @Test
    fun testcallGetDefaultValueWithConstraints() {
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        setPrintSettings(printSettings)
        val tag = PrintSettings.TAG_COLOR_MODE
        val result = callGetDefaultValueWithConstraints(tag)
        Assert.assertEquals(0, result)
    }

    // ================================================================================
    // Private Method Tests - isViewEnabled
    // ================================================================================
    @Test
    fun testIsViewEnabled_Null() {
        setLayout(FIELD_MAIN_VIEW, null)
        val result = callIsViewEnabled("test")
        setLayout(FIELD_MAIN_VIEW, _mainView)
        Assert.assertFalse(result)
    }

    @Test
    fun testIsViewEnabled_NoView() {
        val mockView = mockk<LinearLayout>(relaxed = true)
        every { mockView.findViewWithTag<View>(any()) } returns null
        setLayout(FIELD_MAIN_VIEW, mockView)
        val result = callIsViewEnabled("test")
        setLayout(FIELD_MAIN_VIEW, _mainView)
        Assert.assertFalse(result)
    }

    @Test
    fun testIsViewEnabled_False() {
        val mockView = mockk<LinearLayout>(relaxed = true)
        every { mockView.findViewWithTag<View>(any()) } returns mockView
        every { mockView.isEnabled } returns false
        setLayout(FIELD_MAIN_VIEW, mockView)
        val result = callIsViewEnabled("test")
        setLayout(FIELD_MAIN_VIEW, _mainView)
        Assert.assertFalse(result)
    }

    @Test
    fun testIsViewEnabled_True() {
        val mockView = mockk<LinearLayout>(relaxed = true)
        every { mockView.findViewWithTag<View>(any()) } returns mockView
        every { mockView.isEnabled } returns true
        setLayout(FIELD_MAIN_VIEW, mockView)
        val result = callIsViewEnabled("test")
        setLayout(FIELD_MAIN_VIEW, _mainView)
        Assert.assertTrue(result)
    }

    // ================================================================================
    // Private Method Tests - setViewEnabledWithConstraints
    // ================================================================================
    @Test fun testSetViewEnabledWithConstraints_Null() {
        setLayout(FIELD_MAIN_VIEW, null)
        callSetViewEnabledWithConstraints("test", enabled = false, hideControl = false)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test fun testSetViewEnabledWithConstraints_NoView() {
        val mockView = mockk<LinearLayout>(relaxed = true)
        every { mockView.findViewWithTag<View>(any()) } returns null
        setLayout(FIELD_MAIN_VIEW, mockView)
        callSetViewEnabledWithConstraints("test", enabled = false, hideControl = false)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test fun testSetViewEnabledWithConstraints_NoDisclosureView() {
        val mockView = mockk<LinearLayout>(relaxed = true)
        every { mockView.findViewWithTag<View>(any()) } returns mockView
        val mockSubView = mockk<View>(relaxed = true)
        every { mockView.getTag(any()) } returns mockSubView
        every { mockView.findViewById<View>(any()) } returns null
        setLayout(FIELD_MAIN_VIEW, mockView)
        callSetViewEnabledWithConstraints("test", enabled = false, hideControl = false)
        callSetViewEnabledWithConstraints("test", enabled = true, hideControl = false)
        callSetViewEnabledWithConstraints("test", enabled = false, hideControl = true)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test fun testSetViewEnabledWithConstraints_WithDisclosureView() {
        val mockView = mockk<LinearLayout>(relaxed = true)
        every { mockView.findViewWithTag<View>(any()) } returns mockView
        val mockSubView = mockk<View>(relaxed = true)
        every { mockView.getTag(any()) } returns mockSubView
        every { mockView.findViewById<View>(any()) } returns mockSubView
        setLayout(FIELD_MAIN_VIEW, mockView)
        callSetViewEnabledWithConstraints("test", enabled = true, hideControl = false)
        callSetViewEnabledWithConstraints("test", enabled = false, hideControl = true)
        callSetViewEnabledWithConstraints("test", enabled = false, hideControl = false)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    // ================================================================================
    // Private Method Tests - updateValueWithConstraints
    // ================================================================================
    @Test
    fun testUpdateValueWithConstraints_ColorMode() {
        callUpdateValueWithConstraints(PrintSettings.TAG_COLOR_MODE,
            Preview.ColorMode.FULL_COLOR.ordinal)
    }

    // ================================================================================
    // Private Method Tests - applyViewConstraints
    // ================================================================================
    @Test
    fun testApplyViewConstraints_BookletFalse() {
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 0)
        setPrintSettings(printSettings)
        callApplyViewConstraints(PrintSettings.TAG_BOOKLET)
    }

    @Test
    fun testApplyViewConstraints_BookletTrue() {
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 1)
        setPrintSettings(printSettings)
        callApplyViewConstraints(PrintSettings.TAG_BOOKLET)
    }

    @Test
    fun testApplyViewConstraints_ImpositionOff() {
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 1)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)
        setPrintSettings(printSettings)
        callApplyViewConstraints(PrintSettings.TAG_IMPOSITION)
    }

    @Test
    fun testApplyViewConstraints_Imposition2Up() {
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 0)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)
        setPrintSettings(printSettings)
        callApplyViewConstraints(PrintSettings.TAG_IMPOSITION)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)
        setPrintSettings(printSettings)
        val mockView1 = mockk<LinearLayout>(relaxed = true)
        every { mockView1.findViewWithTag<View>(any()) } returns mockView1
        every { mockView1.getTag(any()) } returns mockk<View>(relaxed = true)
        every { mockView1.isEnabled } returns false
        setLayout(FIELD_MAIN_VIEW, mockView1)
        callApplyViewConstraints(PrintSettings.TAG_IMPOSITION)
        setLayout(FIELD_MAIN_VIEW, _mainView)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)
        setPrintSettings(printSettings)
        val mockView2 = mockk<LinearLayout>(relaxed = true)
        every { mockView2.findViewWithTag<View>(any()) } returns mockView2
        every { mockView2.getTag(any()) } returns mockk<View>(relaxed = true)
        every { mockView2.isEnabled } returns true
        setLayout(FIELD_MAIN_VIEW, mockView2)
        callApplyViewConstraints(PrintSettings.TAG_IMPOSITION)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    // ================================================================================
    // Private Method Tests - applyValueConstraints
    // ================================================================================
    @Test
    fun testApplyValueConstraints_BookletOff() {
        ContentPrintManager.isBoxRegistrationMode = true
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 0)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_BOOKLET, 1)
    }

    @Test
    fun testApplyValueConstraints_BookletOn() {
        ContentPrintManager.isBoxRegistrationMode = true
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 1)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_BOOKLET, 0)
    }

    @Test
    fun testApplyValueConstraints_FinishingSide() {
        ContentPrintManager.isBoxRegistrationMode = true
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_STAPLE, Preview.Staple.ONE.ordinal)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.TOP.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)

        printSettings.setValue(PrintSettings.TAG_STAPLE, Preview.Staple.ONE.ordinal)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.TOP.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.TOP.ordinal)

        printSettings.setValue(PrintSettings.TAG_STAPLE, Preview.Staple.TWO.ordinal)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.TOP.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)

        printSettings.setValue(PrintSettings.TAG_STAPLE, Preview.Staple.ONE_UL.ordinal)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)

        printSettings.setValue(PrintSettings.TAG_STAPLE, Preview.Staple.ONE_UR.ordinal)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)

        printSettings.setValue(PrintSettings.TAG_STAPLE, Preview.Staple.TWO.ordinal)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_FINISHING_SIDE, Preview.FinishingSide.LEFT.ordinal)
    }

    @Test
    fun testApplyValueConstraints_Punch() {
        ContentPrintManager.isBoxRegistrationMode = true
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_2.ordinal)
        printSettings.setValue(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.FACEDOWN.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PUNCH, Preview.Punch.OFF.ordinal)

        printSettings.setValue(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_2.ordinal)
        printSettings.setValue(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.TOP.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PUNCH, Preview.Punch.OFF.ordinal)

        printSettings.setValue(PrintSettings.TAG_PUNCH, Preview.Punch.OFF.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PUNCH, Preview.Punch.OFF.ordinal)
    }

    @Test
    fun testApplyValueConstraints_Imposition() {
        ContentPrintManager.isBoxRegistrationMode = true
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 0)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 1)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION_ORDER, Preview.ImpositionOrder.L_R.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.FOUR_UP.ordinal)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 1)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION_ORDER, Preview.ImpositionOrder.TR_B.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.FOUR_UP.ordinal)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.FOUR_UP.ordinal)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION_ORDER, Preview.ImpositionOrder.R_L.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.FOUR_UP.ordinal)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION_ORDER, Preview.ImpositionOrder.TR_B.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.TWO_UP.ordinal)

        printSettings.setValue(PrintSettings.TAG_IMPOSITION, Preview.Imposition.FOUR_UP.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_IMPOSITION, Preview.Imposition.OFF.ordinal)
    }

    @Test
    fun testApplyValueConstraints_BookletFinish() {
        ContentPrintManager.isBoxRegistrationMode = true
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.OFF.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.OFF.ordinal)

        printSettings.setValue(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.PAPER_FOLDING.ordinal)
        printSettings.setValue(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.AUTO.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.OFF.ordinal)

        printSettings.setValue(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.PAPER_FOLDING.ordinal)
        printSettings.setValue(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.TOP.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_BOOKLET_FINISH, Preview.BookletFinish.OFF.ordinal)
    }

    @Test
    fun testApplyValueConstraints_PaperSize() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = null
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)

        ContentPrintManager.selectedPrinter = selectedPrinterFT
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)

        ContentPrintManager.selectedPrinter = selectedPrinterGL
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A4.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.B5.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.LETTER.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.JUROKUKAI.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)
    }

    @Test
    fun testApplyValueConstraints_InputTray() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL

        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A3.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.A4.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.B5.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.LETTER.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)

        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, Preview.PaperSize.JUROKUKAI.ordinal)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal)
        setPrintSettings(printSettings)
        callApplyValueConstraints(PrintSettings.TAG_INPUT_TRAY, Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal)
    }

    // ================================================================================
    // Private Method Tests - updateHighlightedPrinter
    // ================================================================================
    @Test
    fun testUpdateHighlightedPrinter() {
        setLayout(FIELD_PRINT_CONTROLS, null)
        callUpdateHighlightedPrinter()
        setLayout(FIELD_PRINT_CONTROLS, _printControls)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = null
        callUpdateHighlightedPrinter()

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        callUpdateHighlightedPrinter()

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        callUpdateHighlightedPrinter()

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        callUpdateHighlightedPrinter()
    }

    // ================================================================================
    // Private Method Tests - hideDisabledPrintSettings
    // ================================================================================
    @Test
    fun testHideDisabledPrintSettings() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = null
        ContentPrintManager.printerList = ArrayList()
        callHideDisabledPrintSettings()

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        ContentPrintManager.printerList = listOf(selectedPrinterFT, selectedPrinterGL, selectedPrinterUnknown)
        callHideDisabledPrintSettings()

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.staple = true
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher0Holes = true
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher23Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher24Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.booklet = false
        callHideDisabledPrintSettings()

        ContentPrintManager.selectedPrinter?.printerCapabilities?.staple = true
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher0Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher23Holes = true
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher24Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.booklet = true
        callHideDisabledPrintSettings()

        ContentPrintManager.selectedPrinter?.printerCapabilities?.staple = true
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher0Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher23Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher24Holes = true
        ContentPrintManager.selectedPrinter?.printerCapabilities?.booklet = false
        callHideDisabledPrintSettings()

        ContentPrintManager.selectedPrinter?.printerCapabilities?.staple = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher0Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher23Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher24Holes = false
        ContentPrintManager.selectedPrinter?.printerCapabilities?.booklet = false
        callHideDisabledPrintSettings()

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(100)
        callHideDisabledPrintSettings()

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isStaplerAvailable = true
        printerList[0].config?.isPunch0Available = false
        printerList[0].config?.isPunch3Available = true
        printerList[0].config?.isPunch4Available = false
        printerList[0].config?.isBookletFinishingAvailable = true
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        callHideDisabledPrintSettings()

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isStaplerAvailable = false
        printerList[0].config?.isPunch0Available = false
        printerList[0].config?.isPunch3Available = false
        printerList[0].config?.isPunch4Available = false
        printerList[0].config?.isBookletFinishingAvailable = false
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        callHideDisabledPrintSettings()
    }

    // ================================================================================
    // Private Method Tests - shouldDisplayOptionFromPrinter
    // ================================================================================
    @Test
    fun testShouldDisplayOptionFromPrinter_PaperType() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = null
        ContentPrintManager.printerList = ArrayList()
        var result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.LW_PAPER.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        ContentPrintManager.printerList = listOf(selectedPrinterFT, selectedPrinterGL, selectedPrinterUnknown)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.LW_PAPER.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.paperTypeLightweight = true
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.LW_PAPER.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.paperTypeLightweight = false
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.LW_PAPER.ordinal)
        Assert.assertFalse(result)

        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.ANY.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(100)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.LW_PAPER.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.LW_PAPER.ordinal)
        Assert.assertTrue(result)

        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PAPER_TYPE, Preview.PaperType.ANY.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_OutputTrayAuto() {
        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(100)
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.AUTO.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.AUTO.ordinal
        )
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_OutputTrayFaceDown() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.FACEDOWN.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.outputTrayFacedown = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.FACEDOWN.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.outputTrayFacedown = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.FACEDOWN.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.FACEDOWN.ordinal
        )
        Assert.assertTrue(result)
    }
    @Test
    fun testShouldDisplayOptionFromPrinter_OutputTrayTop() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.TOP.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.outputTrayTop = true
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.TOP.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.outputTrayTop = false
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.TOP.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isTrayTopAvailable = false
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.TOP.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isTrayTopAvailable = true
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.TOP.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_OutputTrayStacking() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_OUTPUT_TRAY,
            Preview.OutputTray.STACKING.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.outputTrayStacking = true
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.STACKING.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.outputTrayStacking = false
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.STACKING.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isTrayStackAvailable = false
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.STACKING.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isTrayStackAvailable = true
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_OUTPUT_TRAY, Preview.OutputTray.STACKING.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_PunchOff() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        val result =
            callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.OFF.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_Punch2Holes() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        var result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_2.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher0Holes = true
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_2.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher0Holes = false
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_2.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_2.ordinal)
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_Punch3Holes() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        var result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_3.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher23Holes = true
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_3.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher23Holes = false
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_3.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isPunch3Available = true
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_3.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isPunch3Available = false
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_3.ordinal)
        Assert.assertFalse(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_Punch4Holes() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        var result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_4.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher24Holes = true
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_4.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities?.finisher24Holes = false
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_4.ordinal)
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isPunch4Available = true
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_4.ordinal)
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isPunch4Available = false
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(PrintSettings.TAG_PUNCH, Preview.Punch.HOLES_4.ordinal)
        Assert.assertFalse(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_InputTrayAuto() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.AUTO.ordinal
        )
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_InputTrayStandard() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayStandard = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayStandard = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayStandard = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayStandard = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_GL)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.STANDARD.ordinal
        )
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_InputTrayTray1() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray1 = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray1 = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray1 = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray1 = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_GL)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY1.ordinal
        )
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_InputTrayTray2() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray2 = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray2 = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray2 = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray2 = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_GL)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY2.ordinal
        )
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_InputTrayTray3() {
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray3 = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray3 = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray3 = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayTray3 = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        setPrinterId(TEST_PRINTER_ID_GL)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.TRAY3.ordinal
        )
        Assert.assertTrue(result)
    }

    @Test
    fun testShouldDisplayOptionFromPrinter_InputTrayExternalFeeder() {
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_FT)
        setPrintSettings(printSettings)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterUnknown
        var result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterFT
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayExternal = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayExternal = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.selectedPrinter = selectedPrinterGL
        ContentPrintManager.selectedPrinter?.printerCapabilities = null
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities = ContentPrintPrinterCapabilities()
        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayExternal = true
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.selectedPrinter?.printerCapabilities?.inputTrayExternal = false
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isExternalFeederAvailable = true
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.valuesFtCerezonaS().indexOf(Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER)
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[0].config?.isExternalFeederAvailable = false
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_FT)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.valuesFtCerezonaS().indexOf(Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER)
        )
        Assert.assertFalse(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[1].config?.isExternalFeederAvailable = true
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_GL)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertTrue(result)

        ContentPrintManager.isBoxRegistrationMode = false
        printerList[1].config?.isExternalFeederAvailable = false
        setPrinterList(printerList)
        setPrinterId(TEST_PRINTER_ID_GL)
        result = callShouldDisplayOptionFromPrinter(
            PrintSettings.TAG_INPUT_TRAY,
            Preview.InputTrayFtGlCerezonaSOga.EXTERNAL_FEEDER.ordinal
        )
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Private Method Tests - updateValue
    // ================================================================================
    @Test
    fun testUpdateValue() {
        callUpdateValue(PrintSettings.TAG_COLOR_MODE, Preview.ColorMode.AUTO.ordinal)
    }

    // ================================================================================
    // Private Method Tests - updateDisplayedValue
    // ================================================================================
    @Test
    fun testUpdateDisplayedValue_WithoutView() {
        val mockLayout = mockk<LinearLayout>()
        every { mockLayout.findViewWithTag<View>(any()) } returns null
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        callUpdateDisplayedValue(PrintSettings.TAG_COLOR_MODE)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test
    fun testUpdateDisplayedValue_WithLayoutView() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val mockTextView = mockk<TextView>(relaxed = true)
        every { mockLayout.findViewById<View>(any()) } returns mockTextView
        val mockSetting = mockk<Setting>(relaxed = true)
        val mockOption = mockk<Option>(relaxed = true)
        every { mockOption.textContent } returns TEST_SERIAL_NO
        val optionsList = listOf(mockOption, mockOption)
        every { mockSetting.options } returns optionsList
        every { mockLayout.getTag(any()) } returns mockSetting
        every { mockLayout.findViewWithTag<View>(any()) } returns mockLayout
        setLayout(FIELD_MAIN_VIEW, mockLayout)

        val printSettings = PrintSettings()
        printSettings.setValue(PrintSettings.TAG_COLOR_MODE, -1)
        setPrintSettings(printSettings)
        callUpdateDisplayedValue(PrintSettings.TAG_COLOR_MODE)

        printSettings.setValue(PrintSettings.TAG_COLOR_MODE, 100)
        setPrintSettings(printSettings)
        callUpdateDisplayedValue(PrintSettings.TAG_COLOR_MODE)

        printSettings.setValue(PrintSettings.TAG_COLOR_MODE, Preview.ColorMode.AUTO.ordinal)
        setPrintSettings(printSettings)
        callUpdateDisplayedValue(PrintSettings.TAG_COLOR_MODE)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test
    fun testUpdateDisplayedValue_WithEditText() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val mockEditText = mockk<EditText>(relaxed = true)
        every { mockLayout.findViewWithTag<View>(any()) } returns mockEditText
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val printSettings = PrintSettings()
        printSettings.setValue(PrintSettings.TAG_COLOR_MODE, Preview.ColorMode.AUTO.ordinal)
        setPrintSettings(printSettings)
        callUpdateDisplayedValue(PrintSettings.TAG_COLOR_MODE)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    @Test
    fun testUpdateDisplayedValue_WithSwitch() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val mockSwitch = mockk<Switch>(relaxed = true)
        every { mockSwitch.isChecked } returns true
        val mockView = mockk<View>(relaxed = true)
        every { mockView.getTag(any()) } returns mockView
        every { mockLayout.findViewWithTag<View>(any()) } answers {
            val tag = firstArg<String>()
            if (tag == PrintSettings.TAG_BOOKLET) {
                mockSwitch
            } else {
                mockView
            }
        }
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val printSettings = PrintSettings()
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 1)
        setPrintSettings(printSettings)
        callUpdateDisplayedValue(PrintSettings.TAG_BOOKLET)

        printSettings.setValue(PrintSettings.TAG_BOOKLET, 0)
        setPrintSettings(printSettings)
        callUpdateDisplayedValue(PrintSettings.TAG_BOOKLET)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    // ================================================================================
    // Private Method Tests - setPrintSettings
    // ================================================================================
    @Test
    fun testSetPrintSettings() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val mockEditText = mockk<EditText>(relaxed = true)
        val mockSwitch = mockk<Switch>(relaxed = true)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.getTag(any()) } returns mockView
        every { mockLayout.findViewById<View>(any()) } answers {
            val id = firstArg<Int>()
            if (id == R.id.view_id_pin_code_edit_text) {
                mockEditText
            } else if (id == R.id.view_id_secure_print_switch) {
                mockSwitch
            } else {
                mockView
            }
        }
        every { mockLayout.findViewWithTag<View>(any()) } returns mockView

        setLayout(FIELD_MAIN_VIEW, mockLayout)
        var printSettings = PrintSettings(AppConstants.PRINTER_MODEL_FT)
        callSetPrintSettings(printSettings)
        callSetPrintSettings(printSettings)
        printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        callSetPrintSettings(printSettings)
        setLayout(FIELD_MAIN_VIEW, _mainView)
    }

    // ================================================================================
    // Private Method Tests - initializePrinterControls
    // ================================================================================
    @Test
    fun testInitializePrinterControls() {
        ContentPrintManager.isBoxRegistrationMode = false
        callInitializePrinterControls()
        ContentPrintManager.isBoxRegistrationMode = true
        callInitializePrinterControls()
    }

    // ================================================================================
    // Private Method Tests - getOptionsStrings
    // ================================================================================
    @Test
    fun testGetOptionsStrings() {
        val mockOption = mockk<Option>(relaxed = true)
        every { mockOption.textContent } returns "app_name"
        val options = listOf(mockOption)
        val result = callGetOptionsStrings(options)
        Assert.assertEquals(1, result.size)
    }

    // ================================================================================
    // Private Method Tests - initializePrintSettingsControls
    // ================================================================================
    @Test
    fun testInitializePrintSettingsControls() {
        val printSettings = mockk<PrintSettings>(relaxed = true)
        every { printSettings.settingMapKey } returns "test"
        setPrintSettings(printSettings)
        callInitializePrintSettingsControls()
        setPrintSettings(PrintSettings(AppConstants.PRINTER_MODEL_GL))
    }

    // ================================================================================
    // Private Method Tests - initializeAuthenticationValues
    // ================================================================================
    @Test
    fun testInitializeAuthenticationValues() {
        callInitializeAuthenticationValues(true)
        callInitializeAuthenticationValues(false)
    }

    // ================================================================================
    // Private Method Tests - animateDisplaySubview
    // ================================================================================
    @Test
    fun testAnimateDisplaySubview() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.startAnimation(any()) } answers {
            val animation = firstArg<TranslateAnimation>()
            val listener = getAnimationListener(animation)
            listener?.onAnimationStart(animation)
            listener?.onAnimationRepeat(animation)
            listener?.onAnimationEnd(animation)
        }
        setLayout(FIELD_MAIN_VIEW, mockLayout)
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        callAnimateDisplaySubview()
        setLayout(FIELD_MAIN_VIEW, _mainView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Private Method Tests - animateDismissSubview
    // ================================================================================
    @Test
    fun testAnimateDismissSubview() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.startAnimation(any()) } answers {
            val animation = firstArg<TranslateAnimation>()
            val listener = getAnimationListener(animation)
            listener?.onAnimationStart(animation)
            listener?.onAnimationRepeat(animation)
            listener?.onAnimationEnd(animation)
        }
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        callAnimateDismissSubview()
        setLayout(FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Private Method Tests - addSubviewOptionsList
    // ================================================================================
    @Test
    fun testAddSubviewOptionsList() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val optionsLayout = getLayout(FIELD_OPTIONS_LAYOUT)
        setLayout(FIELD_OPTIONS_LAYOUT, mockLayout)
        callAddSubviewOptionsList("test", 0, 0, false, 0)
        callAddSubviewOptionsList("test", 0, 1, false, 0)
        setLayout(FIELD_OPTIONS_LAYOUT, optionsLayout)
    }

    // ================================================================================
    // Private Method Tests - addSubviewOptionsTitle
    // ================================================================================
    @Test
    fun testAddSubviewOptionsTitle() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        callAddSubviewOptionsTitle("test", false, 0)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Private Method Tests - createSubview
    // ================================================================================
    @Test
    fun testCreateSubview_Printer() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val subView = getLayout(FIELD_SUB_VIEW)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.tag } returns KEY_TAG_PRINTER
        val optionsLayout = getLayout(FIELD_OPTIONS_LAYOUT)
        ContentPrintManager.printerList = listOf(
            selectedPrinterFT, selectedPrinterGL, selectedPrinterUnknown, selectedPrinterNoModel
        )
        setLayout(FIELD_SUB_VIEW, mockLayout)
        setLayout(FIELD_OPTIONS_LAYOUT, mockLayout)
        ContentPrintManager.isBoxRegistrationMode = true
        callCreateSubview(mockView)
        ContentPrintManager.isBoxRegistrationMode = false
        callCreateSubview(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
        setLayout(FIELD_OPTIONS_LAYOUT, optionsLayout)
    }

    @Test
    fun testCreateSubview_Option() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val subView = getLayout(FIELD_SUB_VIEW)
        val mockView = mockk<View>(relaxed = true)
        every { mockView.tag } returns "test"
        val mockSetting = mockk<Setting>(relaxed = true)
        val mockOption = mockk<Option>(relaxed = true)
        every { mockOption.textContent } returns PrintSettings.TAG_COLOR_MODE
        val optionsList = listOf(mockOption)
        every { mockSetting.options } returns optionsList
        every { mockView.getTag(any()) } answers {
            val tag = firstArg<Int>()
            if (tag == ID_TAG_SETTING) {
                mockSetting
            } else {
                "test"
            }
        }
        val optionsLayout = getLayout(FIELD_OPTIONS_LAYOUT)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        setLayout(FIELD_OPTIONS_LAYOUT, mockLayout)
        callCreateSubview(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
        setLayout(FIELD_OPTIONS_LAYOUT, optionsLayout)
    }

    // ================================================================================
    // Private Method Tests - subviewOptionsItemClicked
    // ================================================================================
    @Test
    fun testSubviewOptionsItemClicked_OptionItem() {
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_subview_option_item
        every { mockView.tag } returns 0
        every { mockView.findViewById<View>(any()) } returns mockView
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.tag } returns PrintSettings.TAG_COLOR_MODE
        every { mockLayout.getTag(any()) } returns arrayOf("a", "b", "c")
        every { mockLayout.findViewWithTag<View>(any()) } returnsMany listOf(mockView, null, mockView)
        val subView = getLayout(FIELD_SUB_VIEW)
        val printSettings = PrintSettings()
        setPrintSettings(printSettings)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        callSubviewOptionsItemClicked(mockView)
        printSettings.setValue(PrintSettings.TAG_COLOR_MODE, Preview.ColorMode.MONOCHROME.ordinal)
        setPrintSettings(printSettings)
        callSubviewOptionsItemClicked(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testSubviewOptionsItemClicked_PrinterItem_ContentPrint() {
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_subview_printer_item
        every { mockView.tag } returns TEST_PRINTER_ID_FT
        every { mockView.findViewById<View>(any()) } returns mockView
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.tag } returns PrintSettings.TAG_COLOR_MODE
        every { mockLayout.findViewWithTag<View>(any()) } returns mockView
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        ContentPrintManager.isBoxRegistrationMode = true
        ContentPrintManager.printerList =
            listOf(selectedPrinterFT, selectedPrinterGL, selectedPrinterUnknown)
        callSubviewOptionsItemClicked(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testSubviewOptionsItemClicked_PrinterItem_DirectPrint() {
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns R.id.view_id_subview_printer_item
        every { mockView.tag } returns TEST_PRINTER_ID_FT
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.tag } returns PrintSettings.TAG_COLOR_MODE
        every { mockLayout.findViewById<View>(any()) } returns mockView
        every { mockLayout.findViewWithTag<View>(any()) } returns mockView
        ContentPrintManager.isBoxRegistrationMode = false
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        setPrinterId(TEST_PRINTER_ID_FT)
        callSubviewOptionsItemClicked(mockView)
        setPrinterId(TEST_PRINTER_ID_GL)
        callSubviewOptionsItemClicked(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    @Test
    fun testSubviewOptionsItemClicked_Other() {
        val mockView = mockk<View>(relaxed = true)
        every { mockView.id } returns 0
        every { mockView.tag } returns 0
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        every { mockLayout.tag } returns PrintSettings.TAG_COLOR_MODE
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        callSubviewOptionsItemClicked(mockView)
        setLayout(FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Private Method Tests - displayOptionsSubview
    // ================================================================================
    @Test
    fun testDisplayOptionsSubview() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val subView = ReflectionTestUtil.getField(_printSettingsView!!, FIELD_SUB_VIEW)
        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_SUB_VIEW, mockLayout)

        val view = mockSubview()
        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_DISPLAY_OPTIONS_SUBVIEW,
            ReflectionTestUtil.Param(View::class.java, view),
            ReflectionTestUtil.Param(Boolean::class.java, false)
        )

        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Private Method Tests - dismissOptionsSubview
    // ================================================================================
    @Test
    fun testDismissOptionsSubview() {
        val mockLayout = mockk<LinearLayout>(relaxed = true)
        val subView = getLayout(FIELD_SUB_VIEW)
        setLayout(FIELD_SUB_VIEW, mockLayout)
        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_DISMISS_OPTIONS_SUBVIEW,
            ReflectionTestUtil.Param(Boolean::class.java, false)
        )
        setLayout(FIELD_SUB_VIEW, subView)
    }

    // ================================================================================
    // Private Method Tests - animateExpand
    // ================================================================================
    @Test
    fun testAnimateExpand() {
        // Store the print setting titles
        val printSettingsTitles = ReflectionTestUtil.getField(
            _printSettingsView!!, FIELD_PRINT_SETTING_TITLES
        )

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_ANIMATE_EXPAND,
            ReflectionTestUtil.Param(View::class.java, null)
        )

        val view = mockk<View>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        every { viewGroup.childCount } returns 3
        every { viewGroup.getChildAt(0) } returns view
        every { view.visibility } returns View.GONE
        val visibleView = mockk<View>(relaxed = true)
        every { viewGroup.getChildAt(1) } returns visibleView
        every { viewGroup.getChildAt(2) } returns visibleView
        every { visibleView.visibility } returns View.VISIBLE
        every { view.getTag(ID_COLLAPSE_TARGET_GROUP) } returns viewGroup
        val layout1 = mockk<LinearLayout>(relaxed = true)
        every { layout1.getTag(ID_COLLAPSE_TARGET_GROUP) } returns view
        val layout2 = mockk<LinearLayout>(relaxed = true)
        every { layout2.getTag(ID_COLLAPSE_TARGET_GROUP) } returns visibleView
        val titles = arrayListOf(view, layout1, layout2)
        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_PRINT_SETTING_TITLES, titles)

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_ANIMATE_EXPAND,
            ReflectionTestUtil.Param(View::class.java, view)
        )

        // Revert the print setting titles
        ReflectionTestUtil.setField(
            _printSettingsView!!, FIELD_PRINT_SETTING_TITLES, printSettingsTitles
        )
    }

    // ================================================================================
    // Private Method Tests - animateCollapse
    // ================================================================================
    @Test
    fun testAnimateCollapse() {
        // Store the print setting titles
        val printSettingsTitles = ReflectionTestUtil.getField(
            _printSettingsView!!, FIELD_PRINT_SETTING_TITLES
        )

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_ANIMATE_COLLAPSE,
            ReflectionTestUtil.Param(View::class.java, null)
        )

        val view = mockk<View>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        every { viewGroup.childCount } returns 3
        every { viewGroup.getChildAt(0) } returns view
        every { view.visibility } returns View.GONE
        val visibleView = mockk<View>(relaxed = true)
        every { viewGroup.getChildAt(1) } returns visibleView
        every { viewGroup.getChildAt(2) } returns visibleView
        every { visibleView.visibility } returns View.VISIBLE
        every { view.getTag(ID_COLLAPSE_TARGET_GROUP) } returns viewGroup
        val layout1 = mockk<LinearLayout>(relaxed = true)
        every { layout1.getTag(ID_COLLAPSE_TARGET_GROUP) } returns view
        val layout2 = mockk<LinearLayout>(relaxed = true)
        every { layout2.getTag(ID_COLLAPSE_TARGET_GROUP) } returns visibleView
        val titles = arrayListOf(view, layout1, layout2)
        ReflectionTestUtil.setField(_printSettingsView!!, FIELD_PRINT_SETTING_TITLES, titles)

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_ANIMATE_COLLAPSE,
            ReflectionTestUtil.Param(View::class.java, view)
        )

        // Revert the print setting titles
        ReflectionTestUtil.setField(
            _printSettingsView!!, FIELD_PRINT_SETTING_TITLES, printSettingsTitles
        )
    }

    // ================================================================================
    // Private Method Tests - collapseControl
    // ================================================================================
    @Test
    fun testCollapseControl() {
        val view = mockSubview()

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_COLLAPSE,
            ReflectionTestUtil.Param(View::class.java, view),
            ReflectionTestUtil.Param(Boolean::class.java, false)
        )

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_COLLAPSE,
            ReflectionTestUtil.Param(View::class.java, view),
            ReflectionTestUtil.Param(Boolean::class.java, true)
        )
    }

    // ================================================================================
    // Private Method Tests - expandControl
    // ================================================================================
    @Test
    fun testExpandControl() {
        val view = mockSubview()

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_EXPAND,
            ReflectionTestUtil.Param(View::class.java, view),
            ReflectionTestUtil.Param(Boolean::class.java, false)
        )

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_EXPAND,
            ReflectionTestUtil.Param(View::class.java, view),
            ReflectionTestUtil.Param(Boolean::class.java, true)
        )
    }

    // ================================================================================
    // Private Method Tests - toggleCollapse
    // ================================================================================
    @Test
    fun testToggleCollapse() {
        var view = mockSubview()
        every { view.isSelected } returns true

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_TOGGLE,
            ReflectionTestUtil.Param(View::class.java, view)
        )

        view = mockSubview()
        every { view.isSelected } returns false

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_TOGGLE,
            ReflectionTestUtil.Param(View::class.java, view)
        )
    }

    // ================================================================================
    // Private Method Tests - checkEditTextValue
    // ================================================================================
    @Test
    fun testCheckEditTextValue_NotNumber() {
        val mockTextView = mockk<TextView>(relaxed = true)
        every { mockTextView.inputType } returns InputType.TYPE_CLASS_TEXT
        every { mockTextView.id } returns R.id.view_id_pin_code_edit_text

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_CHECK_EDIT_TEXT,
            ReflectionTestUtil.Param(TextView::class.java, mockTextView)
        )
    }

    @Test
    fun testCheckEditTextValue_PinCode() {
        val mockTextView = mockk<TextView>(relaxed = true)
        every { mockTextView.inputType } returns InputType.TYPE_CLASS_NUMBER
        every { mockTextView.id } returns R.id.view_id_pin_code_edit_text

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_CHECK_EDIT_TEXT,
            ReflectionTestUtil.Param(TextView::class.java, mockTextView)
        )
    }

    @Test
    fun testCheckEditTextValue_EmptyString() {
        val mockTextView = mockk<TextView>(relaxed = true)
        every { mockTextView.inputType } returns InputType.TYPE_CLASS_NUMBER
        every { mockTextView.id } returns R.id.edit_text_id
        every { mockTextView.text } returns ""

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_CHECK_EDIT_TEXT,
            ReflectionTestUtil.Param(TextView::class.java, mockTextView)
        )
    }

    @Test
    fun testCheckEditTextValue_0() {
        val mockTextView = mockk<TextView>(relaxed = true)
        every { mockTextView.inputType } returns InputType.TYPE_CLASS_NUMBER
        every { mockTextView.id } returns R.id.edit_text_id
        every { mockTextView.text } returns "0"

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_CHECK_EDIT_TEXT,
            ReflectionTestUtil.Param(TextView::class.java, mockTextView)
        )
    }

    @Test
    fun testCheckEditTextValue_100() {
        val mockTextView = mockk<TextView>(relaxed = true)
        every { mockTextView.inputType } returns InputType.TYPE_CLASS_NUMBER
        every { mockTextView.id } returns R.id.edit_text_id
        every { mockTextView.text } returns "100"

        ReflectionTestUtil.callMethod(_printSettingsView!!,
            METHOD_CHECK_EDIT_TEXT,
            ReflectionTestUtil.Param(TextView::class.java, mockTextView)
        )
    }

    companion object {
        private const val TEST_FILE_NAME = "test.pdf"
        private const val TEST_PRINTER_ID_FT = 1
        private const val TEST_PRINTER_ID_GL = 2
        private const val TEST_PRINTER_ID_UNKNOWN = 3
        private const val TEST_SERIAL_NO = "12345"
        private const val TEST_PRINTER_NAME = "test printer"
        private const val TEST_PRINTER_MODEL_FT = "test ORPHIS FT"
        private const val TEST_PRINTER_MODEL_GL = "test ORPHIS GL"
        private const val TEST_IP_ADDRESS = "192.168.1.100"
        private const val FIELD_MAIN_VIEW = "_mainView"
        private const val FIELD_SUB_VIEW = "_subView"
        private const val FIELD_SUB_SCROLL_VIEW = "_subScrollView"
        private const val FIELD_PRINT_CONTROLS = "_printControls"
        private const val FIELD_OPTIONS_LAYOUT = "_subOptionsLayout"
        private const val FIELD_PRINT_SETTINGS = "_printSettings"
        private const val FIELD_PRINTER_ID = "_printerId"
        private const val FIELD_PRINTER_LIST = "_printersList"
        private const val FIELD_PRINT_SETTING_TITLES = "_printSettingsTitles"
        private const val METHOD_LOAD_PRINTERS_LIST = "loadPrintersList"
        private const val METHOD_DISPLAY_OPTION = "shouldDisplayOptionFromConstraints"
        private const val METHOD_GET_DEFAULT_VALUE = "getDefaultValueWithConstraints"
        private const val METHOD_IS_VIEW_ENABLED = "isViewEnabled"
        private const val METHOD_SET_VIEW_ENABLED = "setViewEnabledWithConstraints"
        private const val METHOD_UPDATE_VALUE_WITH_CONSTRAINTS = "updateValueWithConstraints"
        private const val METHOD_APPLY_VIEW_CONSTRAINTS = "applyViewConstraints"
        private const val METHOD_APPLY_VALUE_CONSTRAINTS = "applyValueConstraints"
        private const val METHOD_UPDATE_PRINTER = "updateHighlightedPrinter"
        private const val METHOD_HIDE_DISABLED = "hideDisabledPrintSettings"
        private const val METHOD_SHOULD_DISPLAY = "shouldDisplayOptionFromPrinter"
        private const val METHOD_UPDATE_VALUE = "updateValue"
        private const val METHOD_UPDATE_DISPLAYED_VALUE = "updateDisplayedValue"
        private const val METHOD_SET_PRINT_SETTINGS = "setPrintSettings"
        private const val METHOD_INITIALIZE_PRINTER = "initializePrinterControls"
        private const val METHOD_GET_OPTIONS = "getOptionsStrings"
        private const val METHOD_INITIALIZE_PRINT_SETTINGS = "initializePrintSettingsControls"
        private const val METHOD_INITIALIZE_AUTHENTICATION = "initializeAuthenticationValues"
        private const val METHOD_ANIMATE_DISPLAY = "animateDisplaySubview"
        private const val METHOD_ANIMATE_DISMISS = "animateDismissSubview"
        private const val METHOD_ADD_OPTIONS_LIST = "addSubviewOptionsList"
        private const val METHOD_ADD_OPTIONS_TITLE = "addSubviewOptionsTitle"
        private const val METHOD_CREATE_SUBVIEW = "createSubview"
        private const val METHOD_SUBVIEW_CLICKED = "subviewOptionsItemClicked"
        private const val METHOD_DISPLAY_OPTIONS_SUBVIEW = "displayOptionsSubview"
        private const val METHOD_DISMISS_OPTIONS_SUBVIEW = "dismissOptionsSubview"
        private const val METHOD_ANIMATE_EXPAND = "animateExpand"
        private const val METHOD_ANIMATE_COLLAPSE = "animateCollapse"
        private const val METHOD_COLLAPSE = "collapseControl"
        private const val METHOD_EXPAND = "expandControl"
        private const val METHOD_TOGGLE = "toggleCollapse"
        private const val METHOD_CHECK_EDIT_TEXT = "checkEditTextValue"

        // Copied from PrintSettingsView
        private const val KEY_TAG_PRINTER = "key_tag_printer"
        private const val MSG_COLLAPSE = 0
        private const val MSG_EXPAND = 1
        private const val MSG_SET_SCROLL = 2
        private const val MSG_SLIDE_IN = 3
        private const val MSG_SLIDE_OUT = 4
        private const val MSG_SET_SUB_SCROLL = 5
        private const val MSG_SHOW_SUBVIEW = 6
        private const val ID_COLLAPSE_TARGET_GROUP = 0x11000002
        private const val ID_TAG_TEXT = 0x11000008
        private const val ID_TAG_ICON = 0x11000009
        private const val ID_TAG_SETTING = 0x1100000A
        private const val ID_TAG_SUB_OPTIONS = 0x1100000B

        private var _printSettingsView : PrintSettingsView? = null
        private var _mainView: LinearLayout? = null
        private var _printControls: LinearLayout? = null
        private var printerList = listOf<Printer>()
        private val printSettings = ContentPrintPrintSettings()
        private val selectedFile = ContentPrintFile(1, TEST_FILE_NAME, printSettings)
        private val selectedPrinterFT = ContentPrintPrinter()
        private val selectedPrinterGL = ContentPrintPrinter()
        private val selectedPrinterUnknown = ContentPrintPrinter()
        private val selectedPrinterNoModel = ContentPrintPrinter()
        private val listener = TestPrintSettingsViewInterface()

        private fun callLoadPrintersList() {
            ReflectionTestUtil.callMethod(_printSettingsView!!, METHOD_LOAD_PRINTERS_LIST)
        }

        private fun callShouldDisplayOptionFromConstraints(tag: String, value: Int): Boolean {
            return ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_DISPLAY_OPTION,
                ReflectionTestUtil.Param(String::class.java, tag),
                ReflectionTestUtil.Param(Int::class.java, value)
            ) as Boolean
        }

        private fun callGetDefaultValueWithConstraints(tag: String): Int {
            return ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_GET_DEFAULT_VALUE,
                ReflectionTestUtil.Param(String::class.java, tag)
            ) as Int
        }

        private fun callIsViewEnabled(tag: String): Boolean {
            return ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_IS_VIEW_ENABLED,
                ReflectionTestUtil.Param(String::class.java, tag)
            ) as Boolean
        }

        private fun callSetViewEnabledWithConstraints(tag: String, enabled: Boolean, hideControl: Boolean) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_SET_VIEW_ENABLED,
                ReflectionTestUtil.Param(String::class.java, tag),
                ReflectionTestUtil.Param(Boolean::class.java, enabled),
                ReflectionTestUtil.Param(Boolean::class.java, hideControl)
            )
        }

        private fun callApplyViewConstraints(tag: String) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_APPLY_VIEW_CONSTRAINTS,
                ReflectionTestUtil.Param(String::class.java, tag)
            )
        }

        private fun callUpdateValueWithConstraints(tag: String, value: Int) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_UPDATE_VALUE_WITH_CONSTRAINTS,
                ReflectionTestUtil.Param(String::class.java, tag),
                ReflectionTestUtil.Param(Int::class.java, value)
            )
        }

        private fun callApplyValueConstraints(tag: String, prevValue: Int) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_APPLY_VALUE_CONSTRAINTS,
                ReflectionTestUtil.Param(String::class.java, tag),
                ReflectionTestUtil.Param(Int::class.java, prevValue)
            )
        }

        private fun callUpdateHighlightedPrinter() {
            ReflectionTestUtil.callMethod(_printSettingsView!!, METHOD_UPDATE_PRINTER)
        }

        private fun callHideDisabledPrintSettings() {
            ReflectionTestUtil.callMethod(_printSettingsView!!, METHOD_HIDE_DISABLED)
        }

        private fun callShouldDisplayOptionFromPrinter(name: String, value: Int): Boolean {
            return ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_SHOULD_DISPLAY,
                ReflectionTestUtil.Param(String::class.java, name),
                ReflectionTestUtil.Param(Int::class.java, value)
            ) as Boolean
        }

        private fun callUpdateValue(tag: String, newValue: Int) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_UPDATE_VALUE,
                ReflectionTestUtil.Param(String::class.java, tag),
                ReflectionTestUtil.Param(Int::class.java, newValue)
            )
        }

        private fun callUpdateDisplayedValue(tag: String) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_UPDATE_DISPLAYED_VALUE,
                ReflectionTestUtil.Param(String::class.java, tag)
            )
        }

        private fun callSetPrintSettings(printSettings: PrintSettings) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_SET_PRINT_SETTINGS,
                ReflectionTestUtil.Param(PrintSettings::class.java, printSettings)
            )
        }

        private fun callInitializePrinterControls() {
            ReflectionTestUtil.callMethod(_printSettingsView!!, METHOD_INITIALIZE_PRINTER)
        }

        @Suppress("UNCHECKED_CAST")
        private fun callGetOptionsStrings(options: List<Option>): Array<Any> {
            val result = ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_GET_OPTIONS,
                ReflectionTestUtil.Param(List::class.java, options)
            )
            return result as Array<Any>
        }

        private fun callInitializePrintSettingsControls() {
            ReflectionTestUtil.callMethod(_printSettingsView!!, METHOD_INITIALIZE_PRINT_SETTINGS)
        }

        private fun callInitializeAuthenticationValues(shouldClear: Boolean) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_INITIALIZE_AUTHENTICATION,
                ReflectionTestUtil.Param(Boolean::class.java, shouldClear)
            )
        }

        private fun callAnimateDisplaySubview() {
            ReflectionTestUtil.callMethod(_printSettingsView!!, METHOD_ANIMATE_DISPLAY)
        }

        private fun callAnimateDismissSubview() {
            ReflectionTestUtil.callMethod(_printSettingsView!!, METHOD_ANIMATE_DISMISS)
        }

        private fun callAddSubviewOptionsList(str: String, value: Int, tagValue: Int,
                                              withSeparator: Boolean, itemId: Int) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_ADD_OPTIONS_LIST,
                ReflectionTestUtil.Param(String::class.java, str),
                ReflectionTestUtil.Param(Int::class.java, value),
                ReflectionTestUtil.Param(Int::class.java, tagValue),
                ReflectionTestUtil.Param(Boolean::class.java, withSeparator),
                ReflectionTestUtil.Param(Int::class.java, itemId)
            )
        }

        private fun callAddSubviewOptionsTitle(str: String, showIcon: Boolean, iconId: Int) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_ADD_OPTIONS_TITLE,
                ReflectionTestUtil.Param(String::class.java, str),
                ReflectionTestUtil.Param(Boolean::class.java, showIcon),
                ReflectionTestUtil.Param(Int::class.java, iconId)
            )
        }

        private fun callCreateSubview(view: View) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_CREATE_SUBVIEW,
                ReflectionTestUtil.Param(View::class.java, view)
            )
        }

        private fun callSubviewOptionsItemClicked(view: View) {
            ReflectionTestUtil.callMethod(_printSettingsView!!,
                METHOD_SUBVIEW_CLICKED,
                ReflectionTestUtil.Param(View::class.java, view)
            )
        }

        private fun mockGetTag(tag: Int): Any {
            return when (tag) {
                ID_COLLAPSE_TARGET_GROUP -> {
                    mockk<ViewGroup>(relaxed = true)
                }

                ID_TAG_SETTING -> {
                    mockk<Setting>(relaxed = true)
                }

                ID_TAG_SUB_OPTIONS -> {
                    arrayOf<View>()
                }

                else -> { // ID_TAG_TARGET_VIEW, ID_TAG_TEXT, ID_TAG_ICON
                    mockk<View>(relaxed = true)
                }
            }
        }

        private fun mockSubview(): View {
            val mockSubview = mockk<View>(relaxed = true)
            val mockSetting = mockk<Setting>(relaxed = true)
            every { mockSubview.getTag(ID_TAG_TEXT) } returns "text"
            every { mockSubview.getTag(ID_TAG_ICON) } returns "icon"
            every { mockSubview.getTag(ID_TAG_SETTING) } returns mockSetting
            every { mockSubview.getTag(ID_COLLAPSE_TARGET_GROUP) } returns mockSubview
            every { mockSubview.tag } returns "tag"
            return mockSubview
        }

        private fun mockContext(): Context {
            val mockInflater = mockk<LayoutInflater>()
            val mockLayout = mockk<LinearLayout>(relaxed = true)
            every { mockLayout.parent } returns null
            val mockLayoutParams = mockk<LinearLayout.LayoutParams>(relaxed = true)
            every { mockLayout.layoutParams } returns mockLayoutParams
            val mockEditText = mockk<EditText>(relaxed = true)
            every { mockLayout.findViewById<EditText>(R.id.view_id_pin_code_edit_text)} returns mockEditText
            every { mockInflater.inflate(R.layout.printsettings_input_edittext, null )} returns mockEditText
            val mockSwitch = mockk<Switch>(relaxed = true)
            every { mockLayout.findViewById<Switch>(R.id.view_id_secure_print_switch)} returns mockSwitch
            every { mockInflater.inflate(R.layout.printsettings_switch, null )} returns mockSwitch
            val mockTextView = mockk<TextView>(relaxed = true)
            every { mockLayout.findViewById<TextView>(R.id.menuTextView) } returns mockTextView
            every { mockLayout.findViewById<TextView>(R.id.subTextView) } returns mockTextView
            val mockImageView = mockk<ImageView>(relaxed = true)
            every { mockLayout.findViewById<ImageView>(R.id.menuIcon) } returns mockImageView
            every { mockLayout.findViewById<ImageView>(R.id.menuSeparator) } returns mockImageView
            every { mockLayout.findViewById<ImageView>(R.id.returnButtonIndicator) } returns mockImageView
            every { mockLayout.findViewById<ImageView>(R.id.disclosureIndicator) } returns mockImageView
            val mockView = mockk<View>(relaxed = true)
            every { mockView.findViewById<TextView>(R.id.listValueTextView) } returns mockTextView
            every { mockView.findViewById<TextView>(R.id.listValueSubTextView) } returns mockTextView
            every { mockView.findViewById<LinearLayout>(R.id.menuContainer) } returns mockLayout
            every { mockView.getTag(any()) } answers {
                mockGetTag(firstArg())
            }
            every { mockLayout.findViewById<LinearLayout>(R.id.menuContainer) } returns mockLayout
            every { mockLayout.findViewById<View>(R.id.view_id_disclosure) } returns mockView
            every { mockLayout.findViewById<View>(R.id.view_id_subview_status) } returns mockView
            every { mockLayout.findViewById<View>(R.id.view_id_print_header) } returns mockView
            every { mockLayout.findViewById<View>(R.id.view_id_print_selected_printer) } returns mockView
            every { mockLayout.findViewWithTag<View>(any()) } returns mockView
            every { mockLayout.getTag(any()) } answers {
                mockGetTag(firstArg())
            }
            every { mockInflater.inflate(R.layout.printsettings_print, null )} returns mockLayout
            every { mockInflater.inflate(R.layout.printsettings_registertobox, null )} returns mockLayout
            every { mockInflater.inflate(R.layout.printsettings_container_title, null )} returns mockLayout
            every { mockInflater.inflate(R.layout.printsettings_container_item, null )} returns mockLayout
            every { mockInflater.inflate(R.layout.printsettings_container_item_with_sub, null )} returns mockLayout
            every { mockInflater.inflate(R.layout.printsettings_value_disclosure, null )} returns mockView
            every { mockInflater.inflate(R.layout.printsettings_value_disclosure_with_sub, null )} returns mockView
            every { mockInflater.inflate(R.layout.printsettings_radiobutton, null )} returns mockView
            val mockContext =  MockTestUtil.mockUiContext(mockInflater)
            val mockInputMethod = mockk<InputMethodManager>(relaxed = true)
            every { mockContext.getSystemService(Activity.INPUT_METHOD_SERVICE) } returns mockInputMethod
            return mockContext
        }

        private fun initializeTestPrinters() {
            // Initialize test printers
            selectedPrinterFT.serialNo = TEST_SERIAL_NO
            selectedPrinterFT.printerName = TEST_PRINTER_NAME
            selectedPrinterFT.model = TEST_PRINTER_MODEL_FT
            selectedPrinterFT.printerCapabilities = ContentPrintPrinterCapabilities()
            Assert.assertTrue(selectedPrinterFT.isPrinterFTorCEREZONA_S)

            selectedPrinterGL.serialNo = TEST_SERIAL_NO
            selectedPrinterGL.printerName = TEST_PRINTER_NAME
            selectedPrinterGL.model = TEST_PRINTER_MODEL_GL
            selectedPrinterGL.printerCapabilities = ContentPrintPrinterCapabilities()
            Assert.assertTrue(selectedPrinterGL.isPrinterGLorOGA)

            selectedPrinterUnknown.serialNo = ""
            selectedPrinterUnknown.printerName = ""
            selectedPrinterUnknown.model = ""
            selectedPrinterUnknown.printerCapabilities = null

            selectedPrinterNoModel.serialNo = ""
            selectedPrinterNoModel.printerName = ""
            selectedPrinterNoModel.model = null
            selectedPrinterNoModel.printerCapabilities = null

            val printerFT = Printer(TEST_PRINTER_MODEL_FT, TEST_IP_ADDRESS, null)
            printerFT.id = TEST_PRINTER_ID_FT
            val printerGL = Printer(TEST_PRINTER_MODEL_GL, TEST_IP_ADDRESS, null)
            printerGL.id = TEST_PRINTER_ID_GL
            val printerUnknown = Printer("", TEST_IP_ADDRESS, null)
            printerUnknown.id = TEST_PRINTER_ID_UNKNOWN
            printerList = listOf(printerFT, printerGL, printerUnknown)
        }

        private fun initializePrintSettingsView() {
            ContentPrintManager.isBoxRegistrationMode = false

            val context = mockContext()
            _printSettingsView = PrintSettingsView(context, null, 0)
            val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
            _printSettingsView!!.setInitialValues(TEST_PRINTER_ID_GL, printSettings)
            _printSettingsView!!.setValueChangedListener(listener)
            val callback = ContentPrintManagerTest.TestRegisterToBoxCallback()
            _printSettingsView!!.setRegisterToBoxCallback(callback)

            setPrinterList(printerList)

            // Save the initialized views
            _mainView = getLayout(FIELD_MAIN_VIEW)
            _printControls = getLayout(FIELD_PRINT_CONTROLS)
        }

        private fun getLayout(propertyName: String): LinearLayout? {
            return ReflectionTestUtil.getField(_printSettingsView!!, propertyName) as? LinearLayout
        }

        private fun getAnimationListener(animation: Animation): AnimationListener? {
            val declaredFields = animation.javaClass.declaredFields
            for (declaredField in declaredFields) {
                declaredField.isAccessible = true
                val value = declaredField.get(animation)
                if (value is AnimationListener) {
                    return value
                }
            }
            return null
        }

        private fun setLayout(propertyName: String, mainView: LinearLayout?) {
            ReflectionTestUtil.setField(_printSettingsView!!, propertyName, mainView)
        }

        private fun setPrintSettings(printSettings: PrintSettings?) {
            ReflectionTestUtil.setField(_printSettingsView!!, FIELD_PRINT_SETTINGS, printSettings)
        }

        private fun setPrinterId(printerId: Int) {
            ReflectionTestUtil.setField(_printSettingsView!!, FIELD_PRINTER_ID, printerId)
        }

        private fun setPrinterList(printerList: List<Printer>?) {
            ReflectionTestUtil.setField(_printSettingsView!!, FIELD_PRINTER_LIST, printerList)
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            MockTestUtil.mockUI()
            MockTestUtil.mockConfiguration()

            mockkObject(PrinterManager)
            val mockPrinterManager = mockk<PrinterManager>(relaxed = true)
            every { mockPrinterManager.getPrinterType(any()) } returns AppConstants.PRINTER_MODEL_GL
            every { PrinterManager.getInstance(any<Context>()) } returns mockPrinterManager

            val activity = MockTestUtil.mockActivity()
            ContentPrintManager.newInstance(activity)

            initializeTestPrinters()
            initializePrintSettingsView()
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            MockTestUtil.unMockUI()
            MockTestUtil.unMockConfiguration()
            unmockkObject(PrinterManager)
            unmockkConstructor(LinearLayout::class)
        }
    }
}