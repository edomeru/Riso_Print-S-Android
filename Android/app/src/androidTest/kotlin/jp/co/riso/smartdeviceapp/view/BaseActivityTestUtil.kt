package jp.co.riso.smartdeviceapp.view

import android.app.AlertDialog
import android.app.Instrumentation
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage.RESUMED
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.fragment.AddPrinterFragment
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
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
// Azure Notification Hubs - START
import android.Manifest.permission.POST_NOTIFICATIONS
import androidx.test.rule.GrantPermissionRule
// Azure Notification Hubs - END

open class BaseActivityTestUtil {

    @JvmField
    var mainActivity: MainActivity? = null

    @get:Rule
    var testRule = ActivityScenarioRule(
        MainActivity::class.java
    )

    // Azure Notification Hubs - START
    @get:Rule
    var notificationPermission: GrantPermissionRule = GrantPermissionRule.grant(POST_NOTIFICATIONS)
    // Azure Notification Hubs - END

    @Before
    fun initMainActivity() {
        val activityRef = AtomicReference<MainActivity>()
        testRule.scenario.onActivity { newValue: MainActivity -> activityRef.set(newValue) }
        mainActivity = activityRef.get()

        if(!_isLicenseAgreementDone) {
            agreeLicenseAgreement(true)
        }
    }

    @After
    fun tearDown() {
        if (NetUtils.isNetworkAvailable) {
            NetUtils.unregisterNetworkCallback(mainActivity!!)
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

    fun waitForPrint(time: Int = 5) {
        getInstrumentation().waitForIdleSync()
        repeat(time) {
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
            mainActivity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT ||
            mainActivity!!.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
            mainActivity!!.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
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
                isDisplayed()
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
                isDisplayed()
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
        waitForAnimation()
        updateMainActivity()
    }

    fun loadFileUsingOpenIn(clipData: ClipData) {
        val intent = Intent(mainActivity, PDFHandlerActivity::class.java)
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.clipData = clipData
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        mainActivity!!.startActivity(intent)
        waitForAnimation()
        updateMainActivity()
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

    fun checkDialog(tag: String, msgId: Int, toConfirm: Boolean = false) {
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

        if (toConfirm) {
            mainActivity!!.runOnUiThread { b.callOnClick() }
        }
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

    private val _isLicenseAgreementDone: Boolean
        get() = mainActivity!!.getSharedPreferences("licenseAgreementPrefs", Context.MODE_PRIVATE)
            .getBoolean("licenseAgreementDone", false)

    private fun agreeLicenseAgreement(agree: Boolean) {
        val preferences = mainActivity!!.getSharedPreferences("licenseAgreementPrefs", Context.MODE_PRIVATE)
        if (agree) {
            preferences.edit().putBoolean("licenseAgreementDone", true).apply()
        } else {
            preferences.edit().clear().apply()
        }
    }

    fun getString(id: Int):String {
        return mainActivity!!.resources.getString(id)
    }

    fun waitForDialogWithText(stringId: Int) {
        onView(isRoot()).inRoot(RootMatchers.isDialog())
            .perform(waitForViewMatcher(withText(getString(stringId))))
    }

    fun waitForView(matcher: Matcher<View>) {
        onView(isRoot()).perform(waitForViewMatcher(matcher))
    }

    fun waitForViewMatcher(matcher: Matcher<View>): ViewAction {
        return object : ViewAction {
            val timeout = TIMEOUT_WAITFORVIEW
            override fun getConstraints(): Matcher<View> {
                return isRoot()
            }

            override fun getDescription(): String {
                return "wait for a specific view with matcher $matcher; during $timeout millis."
            }

            override fun perform(uiController: UiController, rootView: View) {
                uiController.loopMainThreadUntilIdle()
                val startTime = System.currentTimeMillis()
                val endTime = startTime + timeout
                val viewMatcher = allOf(matcher, isDisplayed())

                do {
                    // Iterate through all views on the screen and see if the view we are looking for is there already
                    for (child in TreeIterables.breadthFirstViewTraversal(rootView)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return
                        }
                    }
                    // Loops the main thread for a specified period of time.
                    // Control may not return immediately, instead it'll return after the provided delay has passed and the queue is in an idle state again.
                    uiController.loopMainThreadForAtLeast(100)
                } while (System.currentTimeMillis() < endTime) // in case of a timeout we throw an exception -> test fails
                throw PerformException.Builder()
                    .withCause(TimeoutException())
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(rootView))
                    .build()
            }
        }
    }

    fun addPrinter(printersList: List<Printer?>?, num: Int = 1) {
        var index = num

        for (printers in printersList!!) {
            if (index == 0) {
                break
            }

            testClickAndWait(R.id.menu_id_action_add_button)
            var layoutId = R.id.mainLayout
            if (mainActivity!!.isTablet) {
                Assert.assertTrue(mainActivity!!.isDrawerOpen(Gravity.RIGHT))
                layoutId = R.id.rightLayout
            }
            val fm = mainActivity!!.supportFragmentManager
            val addPrinterFragment = fm.findFragmentById(layoutId)
            Assert.assertTrue(addPrinterFragment is AddPrinterFragment)

            // Add a printer
            onView(withId(R.id.inputIpAddress))
                .perform(ViewActions.typeText(printers!!.ipAddress))
            testClickAndWait(R.id.img_save_button)
            pressBack()
            waitForAnimation()

            index--
        }
    }

    fun getId(id: String): Int {
        val targetContext = getInstrumentation().targetContext
        val packageName: String = targetContext.packageName
        return targetContext.resources.getIdentifier(id, "id", packageName)
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

        val TEST_PRINTER_ONLINE = Printer("ORPHIS FW5230", "192.168.1.121", "08:00:27:C8:B0:77") // update with online printer details
        val TEST_PRINTER_ONLINE2 = Printer("RISO CEREZONA S200", "192.168.1.167", "08:00:27:93:79:5C") // update with online printer details
        val TEST_PRINTER_OFFLINE = Printer("ORPHIS GD500", "192.168.0.02", "08:00:27:93:79:5E")
        val TEST_PRINTER_NO_NAME = Printer("", "192.168.0.03", "08:00:27:93:79:5F")
        val TEST_PRINTER_CEREZONA = Printer("RISO CEREZONA S200", "192.168.0.04", "08:00:27:93:79:5G")
        val TEST_PRINTER_GL = Printer("RISO ComColor GL9730", "192.168.0.05", "08:00:27:93:79:5H")
        val TEST_PRINTER_FW = Printer("RISO ORPHIS FW5230", "192.168.0.06", "08:00:27:93:79:5I")
        val TEST_PRINTER_GD = Printer("ORPHIS GD500", "192.168.0.07", "08:00:27:93:79:5J")
        val TEST_PRINTER_IS = Printer("RISO IS1000C-J", "192.168.0.08", "08:00:27:93:79:5K")
        val TEST_PRINTER_FT = Printer("ComColor FT5430", "192.168.0.09", "08:00:27:93:79:5L")
        val TEST_PRINTER_OGA = Printer("RISO ComColor OGA9730", "192.168.0.10", "08:00:27:93:79:5M")
	
        val TEST_PRINTER_MODELS = listOf(
            TEST_PRINTER_IS,
            TEST_PRINTER_GD,
            TEST_PRINTER_FW,
            TEST_PRINTER_FT,
            TEST_PRINTER_GL,
            TEST_PRINTER_CEREZONA,
            TEST_PRINTER_OGA)

        const val TEST_IPV6_ONLINE_PRINTER_ADDRESS = "fe80::a00:27ff:fe22:44a2"

        const val IMG_JPG = "Universe.jpg"
        const val IMG_JPG_90 = "Universe_rotate90.jpg"
        const val IMG_JPG_180 = "Universe_rotate180.jpg"
        const val IMG_JPG_270 = "Universe_rotate270.jpg"
        const val IMG_HEIC = "autumn.heic"
        const val IMG_PORTRAIT = "Portrait.PNG"
        const val IMG_LANDSCAPE = "Landscape.jpg"

        const val TIMEOUT_WAITFORVIEW = 30000f
    }
}