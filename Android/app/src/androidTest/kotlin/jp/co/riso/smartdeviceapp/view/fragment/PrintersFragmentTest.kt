package jp.co.riso.smartdeviceapp.view.fragment

import android.view.Gravity
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrintersFragmentTest : BaseActivityTestUtil() {
    private var _printersFragment: PrintersFragment? = null

    @Before
    fun initPrintersFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintersFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val printersFragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(printersFragment is PrintersFragment)
        _printersFragment = printersFragment as PrintersFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printersFragment)
    }

    @Test
    fun testOnClick_AddPrinter() {
        testClickAndWait(R.id.menu_id_action_add_button)
        var layoutId = R.id.mainLayout
        if (_printersFragment!!.isTablet) {
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }
        val fm = mainActivity!!.supportFragmentManager
        val addPrinterFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(addPrinterFragment is AddPrinterFragment)
        if (_printersFragment!!.isTablet) {
            testClickAndWait(R.id.menu_id_action_add_button)
            Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        } else {
            testClickAndWait(R.id.menu_id_back_button)
            val printersFragment = fm.findFragmentById(layoutId)
            Assert.assertTrue(printersFragment is PrintersFragment)
        }
    }

    @Test
    fun testOnClick_SearchPrinter() {
        testClickAndWait(R.id.menu_id_action_search_button)
        var layoutId = R.id.mainLayout
        if (_printersFragment!!.isTablet) {
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }
        val fm = mainActivity!!.supportFragmentManager
        val searchPrintersFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(searchPrintersFragment is PrinterSearchFragment)
        if (_printersFragment!!.isTablet) {
            testClickAndWait(R.id.menu_id_action_search_button)
            Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        } else {
            testClickAndWait(R.id.menu_id_back_button)
            val printersFragment = fm.findFragmentById(layoutId)
            Assert.assertTrue(printersFragment is PrintersFragment)
        }
    }

    @Test
    fun testOnClick_PrinterSearchSettings() {
        testClickAndWait(R.id.menu_id_printer_search_settings_button)
        var layoutId = R.id.mainLayout
        if (_printersFragment!!.isTablet) {
            Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
            layoutId = R.id.rightLayout
        }
        val fm = mainActivity!!.supportFragmentManager
        val printerSearchSettingsFragment = fm.findFragmentById(layoutId)
        Assert.assertTrue(printerSearchSettingsFragment is PrinterSearchSettingsFragment)
        if (_printersFragment!!.isTablet) {
            testClickAndWait(R.id.menu_id_printer_search_settings_button)
            Assert.assertFalse(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
        } else {
            testClickAndWait(R.id.menu_id_back_button)
            val printersFragment = fm.findFragmentById(layoutId)
            Assert.assertTrue(printersFragment is PrintersFragment)
        }
    }
}