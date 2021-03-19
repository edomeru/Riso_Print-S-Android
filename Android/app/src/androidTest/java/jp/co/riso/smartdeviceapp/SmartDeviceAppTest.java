
package jp.co.riso.smartdeviceapp;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.test.ApplicationTestCase;

public class SmartDeviceAppTest extends ApplicationTestCase<SmartDeviceApp> {

    public SmartDeviceAppTest() {
        super(SmartDeviceApp.class);
    }

    public SmartDeviceAppTest(Class<SmartDeviceApp> applicationClass) {
        super(applicationClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();

    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================

    public void testConstructor() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
        SmartDeviceApp smartDeviceApp = new SmartDeviceApp();
        assertNotNull(smartDeviceApp);
    }

    // ================================================================================
    // Tests - getAppContext
    // ================================================================================

    public void testGetAppContext() {
        assertNotNull(SmartDeviceApp.getAppContext());
    }
}
