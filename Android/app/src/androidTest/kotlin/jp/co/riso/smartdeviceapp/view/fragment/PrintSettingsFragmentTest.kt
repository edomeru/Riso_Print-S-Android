package jp.co.riso.smartdeviceapp.view.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.MockTestUtil
import jp.co.riso.smartdeviceapp.ReflectionTestUtil
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.base.BaseActivity
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView
import jp.co.riso.smartprint.R
import org.junit.*

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
    private fun addActivity(fragment: PrintSettingsFragment) {
        every { fragment.activity } returns fragmentActivity
        every { fragment.context } returns fragmentActivity
    }

    companion object {
        private const val FIELD_PRINT_SETTINGS = "_printSettings"
        private const val FIELD_HANDLER = "_pauseableHandler"
        private const val FIELD_BUNDLE = "_printSettingsBundle"
        private const val FIELD_WAITING_DIALOG = "_waitingDialog"

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
            return mockk<BaseActivity>(relaxed = true)
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            fragmentActivity = mockFragmentActivity()
            MockTestUtil.mockUI()
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            MockTestUtil.unMockUI()
        }
    }
}