package jp.co.riso.smartdeviceapp.view;

import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivityTestUtil {
    protected MainActivity mActivity = null;

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void initMainActivity() {
        AtomicReference<MainActivity> activityRef = new AtomicReference<>();
        testRule.getScenario().onActivity(activityRef::set);
        mActivity = activityRef.get();
    }

    protected void testClick(int id) {
        final View button = mActivity.findViewById(id);
        mActivity.runOnUiThread(button::callOnClick);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
}
