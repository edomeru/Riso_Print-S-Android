package jp.co.riso.smartdeviceapp.view.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
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

class PrintJobsFragmentTest : BaseActivityTestUtil() {

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
            TEST_PRINTER_ONLINE,
            TEST_PRINTER_ONLINE2,
            TEST_PRINTER_CEREZONA
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
    private fun prepareSinglePrintJob() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))

        // return to print settings screen
        onView(withId(R.id.view_id_print_button)).perform(click())

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        // Hide confirmation dialog after printing
        pressBack()

        // Go back to print jobs view
        pressBack()
        switchScreen(MenuFragment.STATE_PRINTJOBS)

        waitForAnimation()
    }

    private fun prepareMultiplePrintJobs() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))

        // return to print settings screen
        onView(withId(R.id.view_id_print_button)).perform(click())

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        // Print again
        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        // Hide confirmation dialog after printing
        pressBack()

        // Go back to print jobs view
        pressBack()
        switchScreen(MenuFragment.STATE_PRINTJOBS)

        waitForAnimation()
    }

    private fun prepareMultiplePrinters() {
        switchScreen(MenuFragment.STATE_HOME)
        selectDocument(getUriFromPath(DOC_PDF))

        // return to print settings screen
        onView(withId(R.id.view_id_print_button)).perform(click())

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        // Hide confirmation dialog after printing
        pressBack()

        // print again
        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        // Hide confirmation dialog after printing
        pressBack()

        selectPrinterPrintSettings(TEST_PRINTER_ONLINE2)

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint()

        // Hide confirmation dialog after printing
        pressBack()

        selectPrinterPrintSettings(TEST_PRINTER_CEREZONA)

        testClickAndWait(R.id.view_id_print_header)
        waitForPrint(30)

        // Hide confirmation dialog after printing
        pressBack()

        // Go back to print jobs view
        pressBack()
        switchScreen(MenuFragment.STATE_PRINTJOBS)

        waitForAnimation()
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

    private fun deletePrintJob(index: Int) {
        // swipe a print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            index
        ).perform(swipeLeft())

        testClickAndWait(R.id.printJobDeleteBtn)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintJobsFragment.TAG
        )

        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment).showsDialog)

        val dialog = fragment.dialog as AlertDialog

        val b = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )

        mainActivity!!.runOnUiThread { b.callOnClick() }
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
    fun testConfigurationChange() {
        switchOrientation()
        waitForAnimation()
        switchOrientation()
    }

    @Test
    fun testDisplayFragmentWithJobsCollapsed() {
        prepareSinglePrintJob()

        // Collapse
        getViewInteractionFromMatchAtPosition(
            R.id.printJobGroupCollapse, 0
        ).perform(click())
        waitForAnimation()

        // Expand again
        getViewInteractionFromMatchAtPosition(
            R.id.printJobGroupCollapse, 0
        ).perform(click())
        waitForAnimation()
    }

    @Test
    fun testDeleteAllButton() {
        prepareSinglePrintJob()

        testClickAndWait(R.id.printJobGroupDelete)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintJobsFragment.TAG
        )

        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment).showsDialog)

        val dialog = fragment.dialog as AlertDialog

        val b = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )

        mainActivity!!.runOnUiThread { b.callOnClick() }
    }

    @Test
    fun testCancelDeleteAllButton() {
        prepareSinglePrintJob()

        testClickAndWait(R.id.printJobGroupDelete)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintJobsFragment.TAG
        )

        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment).showsDialog)

        val dialog = fragment.dialog as AlertDialog

        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_cancel),
            b.text
        )

        mainActivity!!.runOnUiThread { b.callOnClick() }
    }

    @Test
    fun testDeleteAllButtonWithMultiplePrintersAndJobs() {
        prepareMultiplePrinters()

        testClickAndWait(R.id.printJobGroupDelete)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintJobsFragment.TAG
        )

        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment).showsDialog)

        val dialog = fragment.dialog as AlertDialog

        val b = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )

        mainActivity!!.runOnUiThread { b.callOnClick() }
    }

    @Test
    fun testDeletePrintJobStartingRow() {
        prepareMultiplePrintJobs()
        deletePrintJob(0)

        //delete the remaining job
        deletePrintJob(0)
    }

    @Test
    fun testDeletePrintJobEndRow() {
        prepareMultiplePrintJobs()
        deletePrintJob(1)

        //delete the remaining job
        deletePrintJob(0)
    }

    @Test
    fun testCancelDeletePrintJob() {
        prepareSinglePrintJob()

        // swipe a print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeLeft())

        testClickAndWait(R.id.printJobDeleteBtn)

        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            PrintJobsFragment.TAG
        )

        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment).showsDialog)

        val dialog = fragment.dialog as AlertDialog

        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_cancel),
            b.text
        )

        mainActivity!!.runOnUiThread { b.callOnClick() }
    }

    @Test
    fun testTouchAnyPrintJob() {
        prepareSinglePrintJob()

        // click a print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(click())
    }

    @Test
    fun testTouchContainer() {
        prepareSinglePrintJob()

        // click anywhere
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobContainer),
            0
        ).perform(click())
    }

    @Test
    fun testSwipeMultiplePrintJobs() {
        prepareMultiplePrintJobs()

        // swipe a print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeLeft())

        // swipe another print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            1
        ).perform(swipeLeft())

        // swipe again
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            1
        ).perform(swipeLeft())
    }

    @Test
    fun testReSwipePrintJob() {
        prepareSinglePrintJob()

        // swipe a print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeLeft())

        // re-swipe
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeLeft())
    }

    @Test
    fun testHideDeleteButton() {
        prepareMultiplePrintJobs()

        // swipe a print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeLeft())

        // click on same print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(click())

        // swipe again print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeLeft())

        // click anywhere
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobContainer),
            0
        ).perform(click())

        // swipe again print job
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeLeft())

        // swipe to the right
        getViewInteractionFromMatchAtPosition(
            withId(R.id.printJobItem),
            0
        ).perform(swipeRight())
    }
}