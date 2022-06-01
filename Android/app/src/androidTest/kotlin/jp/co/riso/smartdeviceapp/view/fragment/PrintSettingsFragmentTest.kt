package jp.co.riso.smartdeviceapp.view.fragment

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrintSettingsFragmentTest : BaseActivityTestUtil() {
    private var _fragment: PrintSettingsFragment? = null

    // sleep is needed because drawer calls goes through mHandler
    private fun waitForDrawer() {
        SystemClock.sleep(1000)
    }

    @Before
    fun initFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.rightLayout, PrintSettingsFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val fragment = fm.findFragmentById(R.id.rightLayout)
        Assert.assertTrue(fragment is PrintSettingsFragment)
        _fragment = fragment as PrintSettingsFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_fragment)
    }
}