package jp.co.riso.smartdeviceapp.view.fragment

import android.app.AlertDialog
import android.content.Intent
import android.view.Gravity
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment.Companion.KEY_PRINTERS_DIALOG
import jp.co.riso.smartprint.R
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrintersFragmentTest : BaseActivityTestUtil() {
    private var _printersFragment: PrintersFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _printersList: List<Printer?>? = null
    private var _maxCountPrinterList: List<Printer?>? = null

    private val OK_DELETE = -1
    private val CANCEL_DELETE = -2

    @Before
    fun setUp() {
        populatePrinters()
        initPrintersFragment()
    }

    private fun populatePrinters() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)

        _printersList = mutableListOf(
            TEST_PRINTER_ONLINE,
            TEST_PRINTER_ONLINE2
        )
    }

    private fun populateMaxPrinterList() {
        _maxCountPrinterList = mutableListOf(
            Printer("ORPHIS FW5230", "192.168.0.2"),
            Printer("ORPHIS FW5230", "192.168.0.3"),
            Printer("ORPHIS FW5230", "192.168.0.4"),
            Printer("ORPHIS FW5230", "192.168.0.5"),
            Printer("ORPHIS FW5230", "192.168.0.6"),
            Printer("ORPHIS FW5230", "192.168.0.7"),
            Printer("ORPHIS FW5230", "192.168.0.8"),
            Printer("ORPHIS FW5230", "192.168.0.9"),
            Printer("ORPHIS FW5230", "192.168.0.10"),
            Printer("ORPHIS FW5230", "192.168.0.11")
        )

        for ((index, printer) in _maxCountPrinterList!!.withIndex()) {
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            }
            for (printerItem in _printerManager!!.savedPrintersList) {
                if (printerItem!!.ipAddress.contentEquals(_maxCountPrinterList!![index]!!.ipAddress)) {
                    _maxCountPrinterList!![index]!!.id = printerItem.id
                    break
                }
            }
        }
    }

    private fun initPrintersFragment() {
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

    @After
    fun cleanUp() {
        clearPrintersList()
        _printersFragment = null
        _printerManager = null
        _printersList = null
        _maxCountPrinterList = null
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

    @Test
    fun testOnClick_MainMenuButton() {
        testClickAndWait(R.id.menu_id_action_button)

        Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.LEFT))

        val fm = mainActivity!!.supportFragmentManager
        testClickAndWait(R.id.menu_id_action_button)
        val printersFragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(printersFragment is PrintersFragment)
    }

    @Test
    fun testConfigurationChange() {
        switchOrientation()
        waitForAnimation()
        switchOrientation()
    }

    @Test
    fun testSelectPrinter() {
        // Only applies to phone devices
        if (!_printersFragment!!.isTablet) {
            addPrinter(_printersList)

            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(click())

            waitForAnimation()

            val fm = mainActivity!!.supportFragmentManager
            val printerInfoFragment = fm.findFragmentById(R.id.mainLayout)
            Assert.assertTrue(printerInfoFragment is PrinterInfoFragment)
            pressBack()
        }
    }

    @Test
    fun testClickDefaultPrinterSettings() {
        // Only applies to tablet devices
        if (_printersFragment!!.isTablet) {
            addPrinter(_printersList)

            getViewInteractionFromMatchAtPosition(
                withId(R.id.default_print_settings),
                0
            ).perform(click())

            waitForAnimation()

            val fm = mainActivity!!.supportFragmentManager
            val printSettingsFragment = fm.findFragmentById(R.id.rightLayout)
            Assert.assertTrue(printSettingsFragment is PrintSettingsFragment)
            pressBack()
        }
    }

    @Test
    fun testSaveInstanceStateWithoutPrinters() {
        val intent = Intent(mainActivity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = null
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        mainActivity!!.startActivity(intent)
    }

    @Test
    fun testSaveInstanceStateWithPrinters() {
        addPrinter(_printersList)

        val intent = Intent(mainActivity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = null
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        mainActivity!!.startActivity(intent)
    }

    @Test
    fun testAddPrinterThenDelete() {
        addPrinter(_printersList)
        deletePrinter(OK_DELETE)
    }

    @Test
    fun testAddPrinterThenCancelDelete() {
        addPrinter(_printersList)
        deletePrinter(CANCEL_DELETE)
    }

//    @Test
//    fun testDoubleClickDeleteButton() {
//        addPrinter()
//        deletePrinter(CANCEL_DELETE, clickMethod = true)
//    }


    @Test
    fun testDeletePrinterStartingRow() {
        addPrinter(_printersList, 2)

        deletePrinter(OK_DELETE)
        waitForAnimation()

        // Delete again
        deletePrinter(OK_DELETE)
    }

    @Test
    fun testDeletePrinterEndRow() {
        addPrinter(_printersList, 2)

        deletePrinter(OK_DELETE, 1)
        waitForAnimation()

        // Delete again
        deletePrinter(OK_DELETE)
    }

    @Test
    fun testSwipeMultiplePrinters() {
        // Only applies to phone devices
        if (!_printersFragment!!.isTablet) {
            addPrinter(_printersList, 2)

            // swipe a printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(swipeLeft())

            // swipe another printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                1
            ).perform(swipeLeft())

            // swipe again
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                1
            ).perform(swipeLeft())
        }
    }

    @Test
    fun testReSwipePrintJob() {
        // Only applies to phone devices
        if (!_printersFragment!!.isTablet) {
            addPrinter(_printersList)

            // swipe a printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(swipeLeft())

            // re-swipe
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(swipeLeft())
        }
    }

    @Test
    fun testHideDeleteButton() {
        // Only applies to phone devices
        if (!_printersFragment!!.isTablet) {
            addPrinter(_printersList, 2)

            // swipe a printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(swipeLeft())

            // click on same printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(click())

            // swipe again printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(swipeLeft())

            // click anywhere
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printer_list),
                0
            ).perform(click())

            // swipe again printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(swipeLeft())

            // swipe to the right
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                0
            ).perform(swipeRight())
        }
    }

    @Test
    fun testMaxPrinterCountReached() {
        populateMaxPrinterList()

        // Open Add Printers Screen
        testClickAndWait(R.id.menu_id_action_add_button)
        pressBack()

        // Open Printer Search Screen
        testClickAndWait(R.id.menu_id_action_search_button)
        pressBack()
    }

    private fun deletePrinter(deleteFlag: Int, index: Int = 0, clickMethod: Boolean = false) {
        if (!_printersFragment!!.isTablet) {
            // swipe a printer
            getViewInteractionFromMatchAtPosition(
                withId(R.id.printerItem),
                index
            ).perform(swipeLeft())

            testClickAndWait(R.id.btn_delete)
        } else {
            if (!clickMethod) {
                getViewInteractionFromMatchAtPosition(R.id.btn_delete, index).perform(click())
            } else {
                val button = mainActivity!!.findViewById<View>(R.id.btn_delete)
                mainActivity!!.runOnUiThread { button.callOnClick() }
                mainActivity!!.runOnUiThread { button.callOnClick() }
            }
        }

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            KEY_PRINTERS_DIALOG
        )

        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment).showsDialog)

        val dialog = fragment.dialog as AlertDialog

        val b = dialog.getButton(deleteFlag)
        mainActivity!!.runOnUiThread { b.callOnClick() }

    }
}