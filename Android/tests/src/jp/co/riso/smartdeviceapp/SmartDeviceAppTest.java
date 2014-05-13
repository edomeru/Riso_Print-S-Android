
package jp.co.riso.smartdeviceapp;

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
        SmartDeviceApp smartDeviceApp = new SmartDeviceApp();
        assertNotNull(smartDeviceApp);
    }

    // ================================================================================
    // Tests - getAppContext
    // ================================================================================

    public void testGetAppContext() {
        assertNotNull(SmartDeviceApp.getAppContext());
    }

    // ================================================================================
    // Tests - getAppFont
    // ================================================================================

    public void testGetAppFont() {
        assertNotNull(SmartDeviceApp.getAppFont());
    }
}
