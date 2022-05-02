package jp.co.riso.smartdeviceapp.view

import android.app.Instrumentation
import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import android.widget.ViewFlipper
import jp.co.riso.smartprint.R
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import java.util.concurrent.atomic.AtomicReference

class SplashActivityTest {

    private var _activity: SplashActivity? = null

    @get:Rule
    var testRule = ActivityScenarioRule(
        SplashActivity::class.java
    )

    @Before
    fun initSplashActivity() {
        val activityRef = AtomicReference<SplashActivity>()
        testRule.scenario.onActivity { newValue: SplashActivity -> activityRef.set(newValue) }
        _activity = activityRef.get()
    }

    @Before
    fun initEspresso() {
        Intents.init()
    }

    @After
    fun releaseEspresso() {
        Intents.release()
    }

    private fun testClick(id: Int) {
        val button = _activity!!.findViewById<View>(id)
        _activity!!.runOnUiThread { button.callOnClick() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun clearLicenseAgreementPrefs() {
        _activity!!.getSharedPreferences("licenseAgreementPrefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    private fun waitForSplashDone() {
        // wait for splash to finish
        SystemClock.sleep(AppConstants.APP_SPLASH_DURATION + 100)
    }

    private val isLicenseAgreementDone: Boolean
        get() = _activity!!.getSharedPreferences(
            "licenseAgreementPrefs",
            Context.MODE_PRIVATE
        )
            .getBoolean("licenseAgreementDone", false)
    private val isSecurePrintReset: Boolean
        get() {
            val prefs =
                PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext!!)
            return (prefs.getBoolean(
                AppConstants.PREF_KEY_AUTH_SECURE_PRINT,
                true
            ) == AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT
                    && prefs.getString(
                AppConstants.PREF_KEY_AUTH_PIN_CODE,
                ""
            ) == AppConstants.PREF_DEFAULT_AUTH_PIN_CODE)
        }

    @Test
    fun newInstance() {
        clearLicenseAgreementPrefs()
        Assert.assertNotNull(_activity)

        // check that splash screen is displayed and EULA is not yet displayed
        val viewFlipper = _activity!!.findViewById<ViewFlipper>(R.id.viewFlipper)
        Assert.assertEquals(viewFlipper.currentView.id.toLong(), R.id.splash.toLong())
        Assert.assertNull(viewFlipper.currentView.findViewById(R.id.LicenseButtonLayout))
        waitForSplashDone()

        // check that splash screen is replaced by EULA
        Assert.assertNotEquals(viewFlipper.currentView.id.toLong(), R.id.splash.toLong())
        Assert.assertNotNull(viewFlipper.currentView.findViewById(R.id.LicenseButtonLayout))
    }

    @Test
    fun onClickAgree() {
        clearLicenseAgreementPrefs()
        waitForSplashDone()
        Assert.assertFalse(isLicenseAgreementDone)
        val viewFlipper = _activity!!.findViewById<ViewFlipper>(R.id.viewFlipper)
        Assert.assertNull(viewFlipper.currentView.findViewById(R.id.txtPermissionInfo))
        testClick(R.id.licenseAgreeButton)
        Assert.assertTrue(isLicenseAgreementDone)
        Assert.assertNotNull(viewFlipper.currentView.findViewById(R.id.txtPermissionInfo))
    }

    @Test
    fun onClickDisagree() {
        clearLicenseAgreementPrefs()
        waitForSplashDone()
        Assert.assertFalse(isLicenseAgreementDone)
        val viewFlipper = _activity!!.findViewById<ViewFlipper>(R.id.viewFlipper)
        Assert.assertNull(viewFlipper.currentView.findViewById(R.id.txtPermissionInfo))
        testClick(R.id.licenseDisagreeButton)
        Assert.assertFalse(isLicenseAgreementDone)
        // check if EULA is still displayed and permission info is not yet displayed
        Assert.assertNotNull(viewFlipper.currentView.findViewById(R.id.LicenseButtonLayout))
        Assert.assertNull(viewFlipper.currentView.findViewById(R.id.txtPermissionInfo))

        // TODO: check that dialog is displayed - refactor for testability or use other test framework
    }

    @Test
    fun onClickStart() {
        clearLicenseAgreementPrefs()
        waitForSplashDone()
        testClick(R.id.licenseAgreeButton)
        testClick(R.id.startButton)
        Assert.assertTrue(isSecurePrintReset)
        val intent = Intents.getIntents()[0]
        Assert.assertNotNull(intent)
        Assert.assertEquals(intent.component!!.className, MainActivity::class.java.name)
    }

    @Test
    fun onClickSettings() {

        // stubs handling of start activity to prevent launching of device settings
        val result = Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null)
        Intents.intending(IntentMatchers.hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
            .respondWith(result)
        clearLicenseAgreementPrefs()
        waitForSplashDone()
        testClick(R.id.licenseAgreeButton)
        testClick(R.id.settingsButton)
        val intent = Intents.getIntents()[0]
        Assert.assertNotNull(intent)
        Assert.assertEquals(intent.action, Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        Assert.assertEquals(intent.data, Uri.fromParts("package", _activity!!.packageName, null))
    }

    @Test
    fun licenseAgreementDone() {
        _activity!!.getSharedPreferences("licenseAgreementPrefs", Context.MODE_PRIVATE)
            .edit().putBoolean("licenseAgreementDone", true).apply()
        waitForSplashDone()
        val intent = Intents.getIntents()[0]
        Assert.assertNotNull(intent)
        Assert.assertEquals(intent.component!!.className, MainActivity::class.java.name)
    }
}