package jp.co.riso.smartdeviceapp.view.fragment

import android.content.Intent
import android.view.Gravity
import android.widget.Spinner
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.PDFHandlerActivity
import jp.co.riso.smartprint.R
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrinterInfoFragmentTest : BaseActivityTestUtil() {

    private var _printersFragment: PrintersFragment? = null
    private var _printerInfoFragment: PrinterInfoFragment? = null
    private var _printersList: List<Printer?>? = null

    @Before
    fun setup() {
        Intents.init()
        wakeUpScreen()
        initPrinter()
        initFragment()
    }

    @After
    fun cleanUp() {
        Intents.release()
        clearPrintersList()
        _printersFragment = null
        _printerInfoFragment = null
        _printersList = null
    }

    private fun initPrinter() {
        _printersList = mutableListOf(
            TEST_PRINTER_ONLINE,
            TEST_PRINTER_ONLINE2
        )
    }

    private fun initFragment() {
        wakeUpScreen()
        val fm = mainActivity!!.supportFragmentManager
        mainActivity!!.runOnUiThread {
            fm.beginTransaction().add(R.id.mainLayout, PrintersFragment()).commit()
            fm.executePendingTransactions()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        addPrinter(_printersList, 2)

        // for phone devices
        if (!mainActivity!!.isTablet) {
            getViewInteractionFromMatchAtPosition(
                R.id.printerItem, 1 // explicitly select second printer item for testing purposes
            ).perform(click())
            waitForAnimation()

            val printerInfoFragment = fm.findFragmentById(R.id.mainLayout)
            Assert.assertTrue(printerInfoFragment is PrinterInfoFragment)
            _printerInfoFragment = printerInfoFragment as PrinterInfoFragment?
        } else {
            val printersFragment = fm.findFragmentById(R.id.mainLayout)
            Assert.assertTrue(printersFragment is PrintersFragment)
            _printersFragment = printersFragment as PrintersFragment?
        }
    }

    @Test
    fun testNewInstance() {
        if (!mainActivity!!.isTablet) {
            // for phone devices
            Assert.assertNotNull(_printerInfoFragment)
        } else {
            Assert.assertNotNull(_printersFragment)
        }
    }

    @Test
    fun testSaveInstanceState() {
        val intent = Intent(mainActivity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = null
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        mainActivity!!.startActivity(intent)

        waitForAnimation()
        if (!mainActivity!!.isTablet) {
            Assert.assertTrue(_printerInfoFragment is PrinterInfoFragment)
        } else {
            Assert.assertTrue(_printersFragment is PrintersFragment)
        }
    }

    @Test
    fun testConfigurationChange() {
        switchOrientation()
        waitForAnimation()
        switchOrientation()
    }

    @Test
    fun testBackButtonAndSelectDifferentPrinter() {
        if (!mainActivity!!.isTablet) {
            // for phone devices
            testClickAndWait(R.id.menu_id_back_button)
            waitForAnimation()

            getViewInteractionFromMatchAtPosition(
                R.id.printerItem, 0
            ).perform(click())
        }
    }

    @Test
    fun testClickDefaultPrintSettingsButton() {
        if (!mainActivity!!.isTablet) {
            testClickAndWait(R.id.menu_id_action_print_settings_button)
            pressBack()
        } else {
            waitForAnimation()
            getViewInteractionFromMatchAtPosition(R.id.default_print_settings, 0).perform(
                click()
            )
            pressBack()
        }
    }

    @Test
    fun testClickPort() {
        if (mainActivity!!.isTablet) {
            waitForAnimation()
        }

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).check(matches(withSpinnerText(R.string.ids_lbl_port_lpr)))

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).perform(click())
        waitForAnimation()

        onView(withText(R.string.ids_lbl_port_raw)).perform(click())
        waitForAnimation()

        getViewInteractionFromMatchAtPosition(
            withClassName(`is`(Spinner::class.java.canonicalName)), 0
        ).check(matches(withSpinnerText(R.string.ids_lbl_port_raw)))
    }

    @Test
    fun testClickDefaultPrinter() {
        if (mainActivity!!.isTablet) {
            waitForAnimation()

            // Set second added printer as the default printer
            getViewInteractionFromMatchAtPosition(
                withClassName(`is`(Spinner::class.java.canonicalName)), 3 // for second printer
            ).check(matches(withSpinnerText(R.string.ids_lbl_no)))

            getViewInteractionFromMatchAtPosition(
                withClassName(`is`(Spinner::class.java.canonicalName)), 3 // for second printer
            ).perform(click())
            waitForAnimation()

            onView(withText(R.string.ids_lbl_yes)).perform(click())
            waitForAnimation()

            getViewInteractionFromMatchAtPosition(
                withClassName(`is`(Spinner::class.java.canonicalName)), 3 // for second printer
            ).check(matches(withSpinnerText(R.string.ids_lbl_yes)))
        } else {
            // Set second added printer as the default printer
            getViewInteractionFromMatchAtPosition(
                withClassName(`is`(Spinner::class.java.canonicalName)), 1
            ).check(matches(withSpinnerText(R.string.ids_lbl_no)))

            getViewInteractionFromMatchAtPosition(
                withClassName(`is`(Spinner::class.java.canonicalName)), 1
            ).perform(click())
            waitForAnimation()

            onView(withText(R.string.ids_lbl_yes)).perform(click())
            waitForAnimation()

            getViewInteractionFromMatchAtPosition(
                withClassName(`is`(Spinner::class.java.canonicalName)), 1
            ).check(matches(withSpinnerText(R.string.ids_lbl_yes)))

            testClickAndWait(R.id.menu_id_back_button)
        }
    }
}