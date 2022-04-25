package jp.co.riso.smartdeviceapp.view;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.release;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.ViewFlipper;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartprint.R;

public class SplashActivityTest {
    private SplashActivity mActivity;

    @Rule
    public ActivityScenarioRule<SplashActivity> testRule = new ActivityScenarioRule<>(SplashActivity.class);

    @Before
    public void initSplashActivity() {
        AtomicReference<SplashActivity> activityRef = new AtomicReference<>();
        testRule.getScenario().onActivity(activityRef::set);
        mActivity = activityRef.get();
    }

    @Before
    public void initEspresso() {
        init();
    }

    @After
    public void releaseEspresso() {
        release();
    }

    public void testClick(int id) {
        final View button = mActivity.findViewById(id);
        mActivity.runOnUiThread(button::callOnClick);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private void clearLicenseAgreementPrefs() {
        mActivity.getSharedPreferences("licenseAgreementPrefs", MODE_PRIVATE)
                .edit().clear().apply();
    }

    private void waitForSplashDone() {
        // wait for splash to finish
        SystemClock.sleep(AppConstants.APP_SPLASH_DURATION + 100);
    }

    private boolean isLicenseAgreementDone() {
        return mActivity.getSharedPreferences("licenseAgreementPrefs", MODE_PRIVATE)
                .getBoolean("licenseAgreementDone", false);
    }

    private boolean isSecurePrintReset() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.Companion.getAppContext());

        return prefs.getBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true) == AppConstants.PREF_DEFAULT_AUTH_SECURE_PRINT
                && prefs.getString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "").equals(AppConstants.PREF_DEFAULT_AUTH_PIN_CODE);
    }

    @Test
    public void newInstance() {
        clearLicenseAgreementPrefs();

        assertNotNull(mActivity);

        // check that splash screen is displayed and EULA is not yet displayed
        ViewFlipper viewFlipper = mActivity.findViewById(R.id.viewFlipper);
        assertEquals(viewFlipper.getCurrentView().getId(), R.id.splash);
        assertNull(viewFlipper.getCurrentView().findViewById(R.id.LicenseButtonLayout));

        waitForSplashDone();

        // check that splash screen is replaced by EULA
        assertNotEquals(viewFlipper.getCurrentView().getId(), R.id.splash);
        assertNotNull(viewFlipper.getCurrentView().findViewById(R.id.LicenseButtonLayout));
    }

    @Test
    public void onClickAgree() {
        clearLicenseAgreementPrefs();
        waitForSplashDone();

        assertFalse(isLicenseAgreementDone());
        ViewFlipper viewFlipper = mActivity.findViewById(R.id.viewFlipper);
        assertNull(viewFlipper.getCurrentView().findViewById(R.id.txtPermissionInfo));

        testClick(R.id.licenseAgreeButton);

        assertTrue(isLicenseAgreementDone());
        assertNotNull(viewFlipper.getCurrentView().findViewById(R.id.txtPermissionInfo));
    }

    @Test
    public void onClickDisagree() {
        clearLicenseAgreementPrefs();
        waitForSplashDone();

        assertFalse(isLicenseAgreementDone());
        ViewFlipper viewFlipper = mActivity.findViewById(R.id.viewFlipper);
        assertNull(viewFlipper.getCurrentView().findViewById(R.id.txtPermissionInfo));

        testClick(R.id.licenseDisagreeButton);

        assertFalse(isLicenseAgreementDone());
        // check if EULA is still displayed and permission info is not yet displayed
        assertNotNull(viewFlipper.getCurrentView().findViewById(R.id.LicenseButtonLayout));
        assertNull(viewFlipper.getCurrentView().findViewById(R.id.txtPermissionInfo));

        // TODO: check that dialog is displayed - refactor for testability or use other test framework
    }

    @Test
    public void onClickStart() {
        clearLicenseAgreementPrefs();
        waitForSplashDone();

        testClick(R.id.licenseAgreeButton);
        testClick(R.id.startButton);

        assertTrue(isSecurePrintReset());

        Intent intent = Intents.getIntents().get(0);
        assertNotNull(intent);
        assertEquals(intent.getComponent().getClassName(), MainActivity.class.getName());
    }

    @Test
    public void onClickSettings() {

        // stubs handling of start activity to prevent launching of device settings
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(FragmentActivity.RESULT_OK, null);
        Intents.intending(IntentMatchers.hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)).respondWith(result);

        clearLicenseAgreementPrefs();
        waitForSplashDone();

        testClick(R.id.licenseAgreeButton);
        testClick(R.id.settingsButton);

        Intent intent = Intents.getIntents().get(0);
        assertNotNull(intent);
        assertEquals(intent.getAction(), Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        assertEquals(intent.getData(), Uri.fromParts("package", mActivity.getPackageName(), null));
    }

    @Test
    public void licenseAgreementDone() {
        mActivity.getSharedPreferences("licenseAgreementPrefs", MODE_PRIVATE)
                .edit().putBoolean("licenseAgreementDone", true).apply();

        waitForSplashDone();

        Intent intent = Intents.getIntents().get(0);
        assertNotNull(intent);
        assertEquals(intent.getComponent().getClassName(), MainActivity.class.getName());
    }
}
