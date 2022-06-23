package jp.co.riso.smartdeviceapp.view.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.*

class PrintJobsFragmentTest: BaseActivityTestUtil() {

    private var _printJobsFragment: PrintJobsFragment? = null
    private var _printerManager: PrinterManager? = null
    private var _printersList: List<Printer?>? = null

    @get:Rule
    var storagePermission: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setUp() {
        Intents.init()
        wakeUpScreen()
        initPrinters()
        initPrintJobsFragment()
    }

    @After
    fun cleanUp() {
        Intents.release()
        clearPrintersList()
        _printJobsFragment = null
        _printerManager = null
        _printersList = null
    }

    private fun initPrinters() {
        _printerManager = PrinterManager.getInstance(mainActivity!!)

        _printersList = mutableListOf(
            TEST_ONLINE_PRINTER,
            TEST_OFFLINE_PRINTER
        )

        for ((index, printer) in _printersList!!.withIndex()) {
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            }
            for (printerItem in _printerManager!!.savedPrintersList) {
                if (printerItem!!.ipAddress.contentEquals(_printersList!![index]!!.ipAddress)) {
                    _printersList!![index]!!.id = printerItem.id
                    break
                }
            }
        }
    }

    // TODO: cleanup
    private fun preparePrintJobs() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))

        // return to print settings screen
        onView(withId(R.id.view_id_print_button)).perform(click())

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        // Hide confirmation dialog after printing
        pressBack()

        selectPrinterPrintSettings(TEST_OFFLINE_PRINTER)

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()
        waitForPrint()

        // Hide confirmation dialog after printing
        pressBack()

        // Go back to print jobs view
        pressBack()
        switchScreen(MenuFragment.STATE_PRINTJOBS)
    }

    private fun selectPrinterPrintSettings(printer: Printer) {
        // open printer select
        testClick(R.id.view_id_print_selected_printer)

        // select printer
        getViewInteractionFromMatchAtPosition(allOf(
            withId(R.id.view_id_subview_printer_item),
            hasDescendant(withText(containsString(printer.ipAddress)))), 0
        ).perform(click())

        // hide printer select
        onView(withId(R.id.view_id_hide_subview_container))
            .perform(click())
    }

    private fun initPrintJobsFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintJobsFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val printJobsFragment = fm.findFragmentById(R.id.mainLayout)
        Assert.assertTrue(printJobsFragment is PrintJobsFragment)
        _printJobsFragment = printJobsFragment as PrintJobsFragment?
    }

    @Test
    fun testNewInstance() {
        Assert.assertNotNull(_printJobsFragment)
    }

    @Test
    fun testDisplayFragmentWithJobsCollapsed() {
        preparePrintJobs()

        // Collapse
        getViewInteractionFromMatchAtPosition(
            withText(TEST_ONLINE_PRINTER.ipAddress), 0
        ).perform(click())
        waitForAnimation()

        // Expand again
        getViewInteractionFromMatchAtPosition(
            withText(TEST_ONLINE_PRINTER.ipAddress), 0
        ).perform(click())
        waitForAnimation()
    }
}