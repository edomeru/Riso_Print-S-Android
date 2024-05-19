package jp.co.riso.smartdeviceapp.view.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.android.volley.Network
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkConstructor
import jp.co.riso.android.dialog.WaitingDialogFragment
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.MockTestUtil
import jp.co.riso.smartdeviceapp.ReflectionTestUtil
import jp.co.riso.smartdeviceapp.common.DirectPrintManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView
import jp.co.riso.smartprint.R
import org.junit.*
import java.util.Timer
import java.util.TimerTask

class PrintSettingsFragmentTest {
    // ================================================================================
    // Tests - initializeView
    // ================================================================================
    @Test
    fun testInitializeView_NotInitialized() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>()
        val view = mockView()
        fragment.setFragmentForPrinting(false)
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_printsettings)
    }

    @Test
    fun testInitializeView_Initialized() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        val bundle = mockk<Bundle>()
        val view = mockView()
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS, printSettings)
        val handler = mockk<PauseableHandler>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_HANDLER, handler)
        ReflectionTestUtil.setField(fragment, FIELD_BUNDLE, bundle)
        fragment.setFragmentForPrinting(true)
        fragment.initializeFragment(bundle)
        fragment.initializeView(view, bundle)
        fragment.initializeCustomActionBar(view, bundle)
        Assert.assertNotNull(fragment)
        Assert.assertEquals(fragment.viewLayout, R.layout.fragment_printsettings)
    }

    // ================================================================================
    // Tests - onSaveInstanceState
    // ================================================================================
    @Test
    fun testOnSaveInstanceState_NoPreview() {
        val fragment = spyk<PrintSettingsFragment>()
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS_VIEW, null)
        val bundle = mockk<Bundle>(relaxed = true)
        fragment.onSaveInstanceState(bundle)
    }

    @Test
    fun testOnSaveInstanceState_WithPreview() {
        val fragment = spyk<PrintSettingsFragment>()
        val mockPrintSettingsView = mockk<PrintSettingsView>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_SETTINGS_VIEW, mockPrintSettingsView)
        val bundle = mockk<Bundle>(relaxed = true)
        fragment.onSaveInstanceState(bundle)
    }

    // ================================================================================
    // Tests - onPause
    // ================================================================================
    @Test
    fun testOnPause() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        fragment.onPause()
    }

    // ================================================================================
    // Tests - onResume
    // ================================================================================
    @Test
    fun testOnResume_WithDialog() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        fragment.onResume()
    }

    @Test
    fun testOnResume_NoDialog() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, null)
        fragment.onResume()
    }

    // ================================================================================
    // Tests - setFragmentForPrinting
    // ================================================================================
    @Test
    fun testSetFragmentForPrinting() {
        val fragment = spyk<PrintSettingsFragment>()
        fragment.setFragmentForPrinting(true)
        var result = ReflectionTestUtil.getField(fragment, FIELD_IS_FOR_PRINTING) as Boolean
        Assert.assertTrue(result)
        fragment.setFragmentForPrinting(false)
        result = ReflectionTestUtil.getField(fragment, FIELD_IS_FOR_PRINTING) as Boolean
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Tests - setPrinterId
    // ================================================================================
    @Test
    fun testSetPrinterId() {
        val fragment = spyk<PrintSettingsFragment>()
        fragment.setPrinterId(1)
        var result = ReflectionTestUtil.getField(fragment, FIELD_PRINTER_ID) as Int
        Assert.assertEquals(1, result)
        fragment.setPrinterId(PrinterManager.EMPTY_ID)
        result = ReflectionTestUtil.getField(fragment, FIELD_PRINTER_ID) as Int
        Assert.assertEquals(PrinterManager.EMPTY_ID, result)
    }

    // ================================================================================
    // Tests - setPrintSettings
    // ================================================================================
    @Test
    fun testSetPrintSettings() {
        val fragment = spyk<PrintSettingsFragment>()
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        fragment.setPrintSettings(printSettings)
        val result = ReflectionTestUtil.getField(fragment, FIELD_PRINT_SETTINGS) as PrintSettings?
        Assert.assertNotNull(result)
        // Cannot be set to null
        //fragment.setPrintSettings(null)
        //result = ReflectionTestUtil.getField(fragment, FIELD_PRINT_SETTINGS) as PrintSettings?
        //Assert.assertNull(result)
    }

    // ================================================================================
    // Tests - setTargetFragmentPrinters
    // ================================================================================
    @Test
    fun testSetTargetFragmentPrinters() {
        val fragment = spyk<PrintSettingsFragment>()
        fragment.setTargetFragmentPrinters()
        val result = ReflectionTestUtil.getField(fragment, FIELD_IS_PRINT_PREVIEW) as Boolean
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Tests - setTargetFragmentPrintPreview
    // ================================================================================
    @Test
    fun testSetTargetFragmentPrintPreview() {
        val fragment = spyk<PrintSettingsFragment>()
        fragment.setTargetFragmentPrintPreview()
        val result = ReflectionTestUtil.getField(fragment, FIELD_IS_PRINT_PREVIEW) as Boolean
        Assert.assertTrue(result)
    }

    // ================================================================================
    // Tests - setPDFisLandscape
    // ================================================================================
    @Test
    fun testSetPDFisLandscape() {
        val fragment = spyk<PrintSettingsFragment>()
        fragment.setPDFisLandscape(true)
        var result = ReflectionTestUtil.getField(fragment, FIELD_IS_LANDSCAPE) as Boolean
        Assert.assertTrue(result)
        fragment.setPDFisLandscape(false)
        result = ReflectionTestUtil.getField(fragment, FIELD_IS_LANDSCAPE) as Boolean
        Assert.assertFalse(result)
    }

    // ================================================================================
    // Tests - onPrintSettingsValueChanged
    // ================================================================================
    @Test
    fun testOnPrintSettingsValueChanged() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        fragment.setTargetFragmentPrinters()
        fragment.setFragmentForPrinting(true)
        fragment.onPrintSettingsValueChanged(printSettings)
        fragment.setFragmentForPrinting(false)
        fragment.onPrintSettingsValueChanged(printSettings)
    }

    // ================================================================================
    // Tests - onPrint
    // ================================================================================
    @Test
    fun testOnPrint() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        val printer = Printer(TEST_FILE_NAME, TEST_IP_ADDRESS, null)
        val printSettings = PrintSettings(AppConstants.PRINTER_MODEL_GL)
        fragment.setPdfPath(null)
        fragment.onPrint(printer, printSettings)
        fragment.setPdfPath("")
        fragment.onPrint(printer, printSettings)
        fragment.setPdfPath(TEST_FILE_NAME)
        fragment.onPrint(null, null)
        fragment.onPrint(printer, null)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf<Network>())
        fragment.onPrint(printer, printSettings)
        val network = mockk<Network>(relaxed = true)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf(network))
        printer.portSetting = Printer.PortSetting.LPR
        fragment.onPrint(printer, printSettings)
        printer.portSetting = Printer.PortSetting.RAW
        fragment.onPrint(printer, printSettings)
        printer.portSetting = Printer.PortSetting.IPPS
        fragment.onPrint(printer, printSettings)
    }

    // ================================================================================
    // Tests - processMessage
    // ================================================================================
    @Test
    fun testProcessMessage() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        val message = Message()
        message.what = 0 // MSG_PRINT
        message.arg1 = DirectPrintManager.PRINT_STATUS_SENT
        fragment.processMessage(message)
        Assert.assertTrue(fragment.storeMessage(message))
        message.what = 0 // MSG_PRINT
        message.arg1 = DirectPrintManager.PRINT_STATUS_ERROR
        fragment.processMessage(message)
        Assert.assertTrue(fragment.storeMessage(message))
        message.what = 1
        message.arg1 = DirectPrintManager.PRINT_STATUS_ERROR
        fragment.processMessage(message)
        Assert.assertFalse(fragment.storeMessage(message))
    }

    // ================================================================================
    // Tests - onNotifyProgress
    // ================================================================================
    @Test
    fun testOnNotifyProgress_NoNetwork() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val mockManager = mockk<DirectPrintManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, mockManager)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf<Network>())
        val status = DirectPrintManager.PRINT_STATUS_ERROR
        fragment.onNotifyProgress(mockManager, status, 0f)
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNull(manager)
    }

    @Test
    fun testOnNotifyProgress_WithNetwork_Error() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val mockManager = mockk<DirectPrintManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, mockManager)
        val network = mockk<Network>(relaxed = true)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf(network))
        var status = DirectPrintManager.PRINT_STATUS_ERROR_CONNECTING
        fragment.onNotifyProgress(mockManager, status, 0f)
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        status = DirectPrintManager.PRINT_STATUS_ERROR_SENDING
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        status = DirectPrintManager.PRINT_STATUS_ERROR_FILE
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        status = DirectPrintManager.PRINT_STATUS_ERROR
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        status = DirectPrintManager.PRINT_STATUS_SENT
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
    }

    @Test
    fun testOnNotifyProgress_WithNetwork_Sending() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val mockManager = mockk<DirectPrintManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, mockManager)
        val network = mockk<Network>(relaxed = true)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf(network))
        val status = DirectPrintManager.PRINT_STATUS_SENDING
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        fragment.onNotifyProgress(mockManager, status, 0f)
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, null)
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
    }

    @Test
    fun testOnNotifyProgress_WithNetwork_Waking() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val mockManager = mockk<DirectPrintManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, mockManager)
        val network = mockk<Network>(relaxed = true)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf(network))
        val status = DirectPrintManager.PRINT_STATUS_WAKING
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        fragment.onNotifyProgress(mockManager, status, 0f)
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, null)
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
    }

    @Test
    fun testOnNotifyProgress_WithNetwork_Connecting() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        setHandler(fragment)
        val mockManager = mockk<DirectPrintManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, mockManager)
        val network = mockk<Network>(relaxed = true)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf(network))
        val status = DirectPrintManager.PRINT_STATUS_CONNECTING
        val mockWaitingDialog = mockk<WaitingDialogFragment>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, mockWaitingDialog)
        fragment.onNotifyProgress(mockManager, status, 0f)
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        ReflectionTestUtil.setField(fragment, FIELD_WAITING_DIALOG, null)
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
    }

    @Test
    fun testOnNotifyProgress_WithNetwork_Started() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        val mockManager = mockk<DirectPrintManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, mockManager)
        val network = mockk<Network>(relaxed = true)
        ReflectionTestUtil.setField(NetUtils::class.java, FIELD_NET_UTILS_NETWORKS, mutableListOf(network))
        var status = DirectPrintManager.PRINT_STATUS_STARTED
        fragment.onNotifyProgress(mockManager, status, 0f)
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
        status = DirectPrintManager.PRINT_STATUS_PDF_PJL_CREATED
        fragment.onNotifyProgress(mockManager, status, 0f)
        manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNotNull(manager)
    }

    // ================================================================================
    // Tests - onCancel
    // ================================================================================
    @Test
    fun testOnCancel_Printing() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        val mockManager = mockk<DirectPrintManager>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, mockManager)
        fragment.onCancel()
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNull(manager)
    }

    @Test
    fun testOnCancel_NotPrinting() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        ReflectionTestUtil.setField(fragment, FIELD_PRINT_MANAGER, null)
        fragment.onCancel()
        var manager = ReflectionTestUtil.getField(fragment, FIELD_PRINT_MANAGER)
        Assert.assertNull(manager)
    }

    // ================================================================================
    // Tests - onStartBoxRegistration
    // ================================================================================
    @Test
    fun testOnStartBoxRegistration() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        fragment.onStartBoxRegistration()
        val dialog = ReflectionTestUtil.getField(fragment, FIELD_WAITING_DIALOG)
        Assert.assertNotNull(dialog)
    }

    // ================================================================================
    // Tests - onBoxRegistered
    // ================================================================================
    @Test
    fun testOnBoxRegistered() {
        val fragment = spyk<PrintSettingsFragment>()
        addActivity(fragment)
        fragment.onBoxRegistered(false)
        fragment.onBoxRegistered(true)
    }

    // ================================================================================
    // Private Functions
    // ================================================================================
    private fun addActivity(fragment: PrintSettingsFragment, activity: FragmentActivity? = fragmentActivity) {
        every { fragment.activity } returns activity
        every { fragment.context } returns activity
    }

    private fun setHandler(fragment: PrintSettingsFragment) {
        // Use Java reflection to replace PauseableHandler with a mock object
        val mockPauseableHandler = mockk<PauseableHandler>(relaxed = true)
        ReflectionTestUtil.setField(fragment, FIELD_PAUSEABLE_HANDLER, mockPauseableHandler)
    }

    companion object {
        private const val TEST_FILE_NAME = "test.pdf"
        private const val TEST_IP_ADDRESS = "192.168.1.1"
        private const val FIELD_PRINTER_ID = "_printerId"
        private const val FIELD_PRINT_SETTINGS = "_printSettings"
        private const val FIELD_HANDLER = "_pauseableHandler"
        private const val FIELD_BUNDLE = "_printSettingsBundle"
        private const val FIELD_PRINT_SETTINGS_VIEW = "_printSettingsView"
        private const val FIELD_WAITING_DIALOG = "_waitingDialog"
        private const val FIELD_PAUSEABLE_HANDLER = "_pauseableHandler"
        private const val FIELD_PRINT_MANAGER = "_directPrintManager"
        private const val FIELD_IS_FOR_PRINTING = "_fragmentForPrinting"
        private const val FIELD_IS_PRINT_PREVIEW = "_isTargetFragmentPrintPreview"
        private const val FIELD_IS_LANDSCAPE = "_pdfIsLandscape"
        private const val FIELD_NET_UTILS_NETWORKS = "availableNetworks"

        private var fragmentActivity: FragmentActivity? = null

        private fun mockView(): View {
            val mockView = mockk<View>(relaxed = true)
            val mockPrintSettingsView = mockk<PrintSettingsView>(relaxed = true)
            every { mockView.findViewById<View>(R.id.rootView) } returns mockPrintSettingsView
            val mockTextView = mockk<TextView>(relaxed = true)
            every { mockView.findViewById<TextView>(R.id.titleTextView) } returns mockTextView
            return mockView
        }

        private fun mockFragmentActivity(): FragmentActivity {
            val mockFragmentActivity = mockk<MainActivity>(relaxed = true)
            val mockPrintPreviewView = mockk<PrintPreviewView>(relaxed = true)
            every { mockFragmentActivity.findViewById<View>(R.id.printPreviewView) } returns mockPrintPreviewView
            val mockFragmentManager = mockk<FragmentManager>(relaxed = true)
            every { mockFragmentActivity.supportFragmentManager } returns mockFragmentManager
            val mockInputMethod = mockk<InputMethodManager>(relaxed = true)
            every { mockFragmentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) } returns mockInputMethod
            return mockFragmentActivity
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            fragmentActivity = mockFragmentActivity()
            MockTestUtil.mockUI()

            mockkConstructor(Timer::class)
            every { anyConstructed<Timer>().schedule(any(), any<Long>())} answers {
                val task = firstArg<TimerTask>()
                task.run()
            }
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            MockTestUtil.unMockUI()

            unmockkConstructor(Timer::class)
        }
    }
}