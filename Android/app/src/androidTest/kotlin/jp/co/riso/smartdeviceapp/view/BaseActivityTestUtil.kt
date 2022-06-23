package jp.co.riso.smartdeviceapp.view

import android.app.AlertDialog
import android.app.Instrumentation
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage.RESUMED
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.fragment.HomeFragment
import jp.co.riso.smartdeviceapp.view.fragment.MenuFragment
import jp.co.riso.smartprint.R
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicReference

open class BaseActivityTestUtil {

    @JvmField
    var mainActivity: MainActivity? = null

    @get:Rule
    var testRule = ActivityScenarioRule(
        MainActivity::class.java
    )

    @Before
    fun initMainActivity() {
        val activityRef = AtomicReference<MainActivity>()
        testRule.scenario.onActivity { newValue: MainActivity -> activityRef.set(newValue) }
        mainActivity = activityRef.get()
    }

    @After
    fun tearDown() {
        if (NetUtils.isWifiAvailable) {
            NetUtils.unregisterWifiCallback(mainActivity!!)
        }
        testRule.scenario.close()
        /** Use to set default state: permissions are not granted
         *  Not yet working. Clearing of permissions is needed at the end of each test, however, this also clears coverage report generated in app storage
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm reset-permissions")
        */
    }

    /**
     *  Utility Methods
     */

    // wait some seconds so that you can see the change on emulator/device.
    fun waitFewSeconds() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun testClick(id: Int) {
        val button = mainActivity!!.findViewById<View>(id)
        mainActivity!!.runOnUiThread { button.callOnClick() }
    }

    fun testClickAndWait(id: Int) {
        val button = mainActivity!!.findViewById<View>(id)
        mainActivity!!.runOnUiThread { button.callOnClick() }
        waitForAnimation()
    }

    fun waitForAnimation() {
        getInstrumentation().waitForIdleSync()
        repeat(5) {
            waitFewSeconds()
        }
    }

    fun waitForPrint() {
        getInstrumentation().waitForIdleSync()
        repeat(5) {
            waitFewSeconds()
        }
    }

    fun wakeUpScreen() {
        mainActivity!!.runOnUiThread {
            mainActivity!!.window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        waitFewSeconds()
    }

    fun switchOrientation() {
        mainActivity!!.requestedOrientation = if (
            mainActivity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    fun getViewInteractionFromMatchAtPosition(id: Int, position: Int): ViewInteraction {
        return onView(
            allOf(
                getElementFromMatchAtPosition(
                    allOf(withId(id)),
                    position
                ),
                ViewMatchers.isDisplayed()
            )
        )
    }

    fun getViewInteractionFromMatchAtPosition(matcher: Matcher<View>, position: Int): ViewInteraction {
        return onView(
            allOf(
                getElementFromMatchAtPosition(
                    matcher,
                    position
                ),
                ViewMatchers.isDisplayed()
            )
        )
    }

    private fun getElementFromMatchAtPosition(
        matcher: Matcher<View>,
        position: Int
    ): Matcher<View?> {
        return object : BaseMatcher<View?>() {
            var counter = 0
            override fun matches(item: Any): Boolean {
                if (matcher.matches(item)) {
                    if (counter == position) {
                        counter++
                        return true
                    }
                    counter++
                }
                return false
            }

            override fun describeTo(description: Description) {
                description.appendText("Element at hierarchy position $position")
            }
        }
    }

    fun getUriFromPath(filename: String): Uri {
        return Uri.fromFile(File(getPath(filename)))
    }
    
    private fun getPath(filename: String): String {
        val f = File(mainActivity!!.cacheDir.toString() + "/" + filename)
        val assetManager = getInstrumentation().context.assets
        try {
            val `is` = assetManager.open(filename)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val fos = FileOutputStream(f)
            fos.write(buffer)
            fos.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return f.path
    }

    fun isKeyboardOpen(fragment: Fragment):Boolean {
        return WindowInsetsCompat
            .toWindowInsetsCompat(fragment.view!!.rootWindowInsets)
            .isVisible(WindowInsetsCompat.Type.ime())
    }

    fun clearPrintersList() {
        val pm = PrinterManager.getInstance(SmartDeviceApp.appContext!!)
        val settingsScreen =
            UiDevice.getInstance(getInstrumentation()).findObject(
                UiSelector().text(
                    "Print Settings"
                )
            )
        if (settingsScreen.exists()) {
            pressBack()
        }

        for (printerItem in pm!!.savedPrintersList.reversed()) {
            pm.removePrinter(printerItem)
        }
    }

    fun selectDocument(uri: Uri) {
        loadFileUsingButton(R.id.fileButton, uri)
    }

    fun selectPhotos(uri: Uri) {
        loadFileUsingButton(R.id.photosButton, uri)
    }

    fun capturePhoto(uri: Uri) {
        loadFileUsingButton(R.id.cameraButton, uri)
    }

    private fun loadFileUsingButton(id: Int, uri: Uri) {
        val fm = mainActivity!!.supportFragmentManager
        val fragment = fm.findFragmentById(R.id.mainLayout)
        if (fragment !is HomeFragment) {
            switchScreen(MenuFragment.STATE_HOME)
        }

        val intent = Intent()
        if (id != R.id.cameraButton) {
            intent.setData(uri)
            Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
                .respondWith(
                    Instrumentation.ActivityResult(
                        FragmentActivity.RESULT_OK,
                        intent))
        } else {
            intent.putExtra(ScanConstants.SCANNED_RESULT, uri)
            Intents.intending(IntentMatchers.hasComponent(ScanActivity::class.java.name))
                .respondWith(
                    Instrumentation.ActivityResult(
                        FragmentActivity.RESULT_OK,
                        intent
                    )
                )
        }
        testClickAndWait(id)
        updateMainActivity()
    }

    fun selectPhotos(clipData: ClipData) {
        val intent = Intent()
        intent.clipData = clipData
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_CHOOSER)).respondWith(result)
        testClickAndWait(R.id.photosButton)
        updateMainActivity()
    }

    fun loadFileUsingOpenIn(uri: Uri) {
        val intent = Intent(mainActivity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = uri
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        mainActivity!!.startActivity(intent)
    }

    fun loadFileUsingOpenIn(clipData: ClipData) {
        val intent = Intent(mainActivity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.clipData = clipData
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        mainActivity!!.startActivity(intent)
    }

    open fun updateMainActivity() {
        mainActivity = getCurrentActivity()
    }

    private fun getCurrentActivity(): MainActivity? {
        var currentActivity = mainActivity
        getInstrumentation().runOnMainSync {
            val resumedActivities: Collection<*> =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED)
            if (resumedActivities.iterator().hasNext()) {
                currentActivity = resumedActivities.iterator().next() as MainActivity
            }
        }
        return currentActivity
    }

    fun switchScreen(state: Int) {
        val id = when (state) {
                MenuFragment.STATE_HOME -> R.id.homeButton
                MenuFragment.STATE_PRINTPREVIEW -> R.id.printPreviewButton
                MenuFragment.STATE_PRINTERS -> R.id.printersButton
                MenuFragment.STATE_PRINTJOBS -> R.id.printJobsButton
                MenuFragment.STATE_SETTINGS -> R.id.settingsButton
                MenuFragment.STATE_HELP -> R.id.helpButton
                MenuFragment.STATE_LEGAL -> R.id.legalButton
            else -> {R.id.homeButton}
        }
        testClickAndWait(id)
    }

    fun checkDialog(tag: String, msgId: Int) {
        updateMainActivity()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            tag
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment).showsDialog)
        val dialog = fragment.dialog as AlertDialog
        val msg = dialog.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            mainActivity!!.resources.getString(msgId),
            (msg as TextView).text
        )

        val buttonType = if (fragment is ConfirmDialogFragment) {
            DialogInterface.BUTTON_POSITIVE
        } else {
            DialogInterface.BUTTON_NEGATIVE
        }

        val b = dialog.getButton(buttonType)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

    fun checkDialog(tag: String, titleId: Int, msgId: Int) {
        updateMainActivity()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(
            tag
        )
        Assert.assertTrue(fragment is DialogFragment)
        Assert.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        val titleIdentifier = mainActivity!!.resources.getIdentifier("alertTitle", "id", "android")
        val title = dialog!!.findViewById<View>(titleIdentifier)
        val msg = dialog.findViewById<View>(android.R.id.message)
        Assert.assertEquals(
            mainActivity!!.resources.getString(titleId),
            (title as TextView).text
        )
        Assert.assertEquals(
            mainActivity!!.resources.getString(msgId),
            (msg as TextView).text
        )

        val buttonType = if (fragment is ConfirmDialogFragment) {
            DialogInterface.BUTTON_POSITIVE
        } else {
            DialogInterface.BUTTON_NEGATIVE
        }

        val b = dialog.getButton(buttonType)
        Assert.assertEquals(
            mainActivity!!.resources.getString(R.string.ids_lbl_ok),
            b.text
        )
    }

    companion object {
        const val DOC_PDF = "PDF-squarish.pdf"
        const val DOC_PDF_4PAGES = "4pages_Landscape_TestData.pdf"
        const val DOC_PDF_ERR_OPEN_FAILED = "Invalid_PDF.pdf"
        const val DOC_PDF_ERR_WITH_ENCRYPTION = "PDF-withEncryption.pdf"
        const val DOC_PDF_ERR_PRINT_NOT_ALLOWED = "PDF-PrintNotAllowed.pdf"

        const val DOC_TXT = "1_7MB.txt"
        const val DOC_TXT_ERR_SIZE_LIMIT = "6MB.txt"

        const val IMG_PNG = "Fairy.png"
        const val IMG_BMP = "BMP.bmp"
        const val IMG_GIF = "Circles.gif"
        const val IMG_ERR_FAIL_CONVERSION = "Invalid_JPEG.jpg"
        const val IMG_ERR_UNSUPPORTED = "MARBLES.TIF"

        val TEST_PRINTER_ONLINE = Printer("ORPHIS FW5230", "192.168.0.41") // update with online printer details
        val TEST_PRINTER_OFFLINE = Printer("ORPHIS GD500", "192.168.0.2")
        val TEST_PRINTER_NO_NAME = Printer("", "192.168.0.3")
        val TEST_PRINTER_CEREZONA = Printer("RISO CEREZONA S200", "192.168.0.4")
        val TEST_PRINTER_GL = Printer("RISO CEREZONA S200", "192.168.0.5")
        val TEST_PRINTER_FW = Printer("RISO ORPHIS FW5230", "192.168.0.6")

        const val IMG_JPG = "Universe.jpg"
        const val IMG_JPG_90 = "Universe_rotate90.jpg"
        const val IMG_JPG_180 = "Universe_rotate180.jpg"
        const val IMG_JPG_270 = "Universe_rotate270.jpg"
        const val IMG_HEIC = "autumn.heic"
        const val IMG_PORTRAIT = "Portrait.PNG"
        const val IMG_LANDSCAPE = "Landscape.jpg"
    }
}