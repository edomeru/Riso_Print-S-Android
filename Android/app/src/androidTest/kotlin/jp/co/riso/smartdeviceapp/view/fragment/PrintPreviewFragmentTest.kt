package jp.co.riso.smartdeviceapp.view.fragment

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrintPreviewFragmentTest : BaseActivityTestUtil() {
    private var _printPreviewFragment: PrintPreviewFragment? = null

    // sleep is needed because drawer calls goes through mHandler
    private fun waitForDrawer() {
        SystemClock.sleep(1000)
    }

    @Before
    fun initFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintPreviewFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val fragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(fragment is PrintPreviewFragment)
        _printPreviewFragment = fragment as PrintPreviewFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printPreviewFragment)
    }
}