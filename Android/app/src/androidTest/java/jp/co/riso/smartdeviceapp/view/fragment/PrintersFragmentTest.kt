package jp.co.riso.smartdeviceapp.view.fragment

import android.os.SystemClock
import android.view.Gravity
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrintersFragmentTest : BaseActivityTestUtil() {
    var mPrintersFragment: PrintersFragment? = null

    // sleep is needed because drawer calls goes through mHandler
    private fun waitForDrawer() {
        SystemClock.sleep(1000)
    }

    @Before
    fun initPrintersFragment() {
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintersFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val printersFragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(printersFragment is PrintersFragment)
        mPrintersFragment = printersFragment as PrintersFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(mPrintersFragment)
    }

    @Test
    fun testOnClick_AddPrinter() {
        testClick(R.id.menu_id_action_add_button)
        var layoutId = R.id.mainLayout
        if (mPrintersFragment!!.isTablet) {
            waitForDrawer()
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }
        val fm = mainActivity!!.supportFragmentManager
        val addPrinterFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(addPrinterFragment is AddPrinterFragment)
        if (mPrintersFragment!!.isTablet) {
            testClick(R.id.menu_id_action_add_button)
            waitForDrawer()
            Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        } else {
            testClick(R.id.menu_id_back_button)
            val printersFragment = fm.findFragmentById(layoutId)
            Assert.assertTrue(printersFragment is PrintersFragment)
        }
    }

    @Test
    fun testOnClick_SearchPrinter() {
        testClick(R.id.menu_id_action_search_button)
        var layoutId = R.id.mainLayout
        if (mPrintersFragment!!.isTablet) {
            waitForDrawer()
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }
        val fm = mainActivity!!.supportFragmentManager
        val searchPrintersFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(searchPrintersFragment is PrinterSearchFragment)
        if (mPrintersFragment!!.isTablet) {
            testClick(R.id.menu_id_action_search_button)
            waitForDrawer()
            Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        } else {
            testClick(R.id.menu_id_back_button)
            val printersFragment = fm.findFragmentById(layoutId)
            Assert.assertTrue(printersFragment is PrintersFragment)
        }
    }

    @Test
    fun testOnClick_PrinterSearchSettings() {
        testClick(R.id.menu_id_printer_search_settings_button)
        var layoutId = R.id.mainLayout
        if (mPrintersFragment!!.isTablet) {
            waitForDrawer()
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }
        val fm = mainActivity!!.supportFragmentManager
        val printerSearchSettingsFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(printerSearchSettingsFragment is PrinterSearchSettingsFragment)
        if (mPrintersFragment!!.isTablet) {
            testClick(R.id.menu_id_printer_search_settings_button)
            waitForDrawer()
            Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        } else {
            testClick(R.id.menu_id_back_button)
            val printersFragment = fm.findFragmentById(layoutId)
            Assert.assertTrue(printersFragment is PrintersFragment)
        }
    }
}