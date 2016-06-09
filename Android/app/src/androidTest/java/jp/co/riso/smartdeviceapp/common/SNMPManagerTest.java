
package jp.co.riso.smartdeviceapp.common;

import junit.framework.TestCase;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.common.SNMPManager.SNMPManagerCallback;

public class SNMPManagerTest extends TestCase implements
        SNMPManagerCallback {
    final CountDownLatch mSignal = new CountDownLatch(1);
    final int TIMEOUT = 15;
    private static final String IPV4_ONLINE_RISO_PRINTER_ADDRESS = "192.168.1.206";
    private static final String IPV4_ONLINE_NONRISO_PRINTER_ADDRESS = "192.168.1.203";
    private static final String IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.24";

    private SNMPManager mSnmpManager = null;
    private boolean mOnEndDiscovery = false;
    private boolean mOnFoundDevice = false;

    public SNMPManagerTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        mSnmpManager = new SNMPManager();
        assertNotNull(mSnmpManager);
        mSnmpManager.initializeSNMPManager(AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME);
        mOnEndDiscovery = false;
        mOnFoundDevice = false;
        testSetCallback_ValidCallback();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        mSnmpManager.finalizeSNMPManager();
        testSetCallback_NullCallback();
    }

    // ================================================================================
    // Tests - setCallback
    // ================================================================================

    public void testSetCallback_ValidCallback() {
        try {
            mSnmpManager.setCallback(this);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetCallback_NullCallback() {
        try {
            mSnmpManager.setCallback(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - deviceDiscovery
    // ================================================================================

    public void testDeviceDiscovery() {
        try {
            mSnmpManager.deviceDiscovery();

            mSignal.await(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(true, mOnFoundDevice);
            assertEquals(true, mOnEndDiscovery);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - manualDiscovery
    // ================================================================================

    /*
    public void testManualDiscovery_OnlineRisoPrinter() {
        try {
            mSnmpManager.manualDiscovery(IPV4_ONLINE_RISO_PRINTER_ADDRESS);

            mSignal.await(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(true, mOnFoundDevice);
            assertEquals(true, mOnEndDiscovery);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testManualDiscovery_OnlineNonRisoPrinter() {
        try {
            mSnmpManager.manualDiscovery(IPV4_ONLINE_NONRISO_PRINTER_ADDRESS);

            mSignal.await(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(true, mOnFoundDevice);
            assertEquals(true, mOnEndDiscovery);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    */

    public void testManualDiscovery_OfflinePrinter() {
        try {
            mSnmpManager.manualDiscovery(IPV4_OFFLINE_PRINTER_ADDRESS);

            mSignal.await(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(true, mOnEndDiscovery);
            assertEquals(false, mOnFoundDevice);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancel
    // ================================================================================

    public void testCancel_DuringIdle() {
        try {
            mSnmpManager.cancel();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testCancel_DuringAutoSearch() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    mSnmpManager.cancel();
                }
            }, 1000);
            mSnmpManager.deviceDiscovery();

            mSignal.await(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(false, mOnEndDiscovery);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testCancel_DuringManualSearch() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    mSnmpManager.cancel();
                    mSignal.countDown();
                }
            }, 1000);
            mSnmpManager.manualDiscovery(IPV4_OFFLINE_PRINTER_ADDRESS);

            mSignal.await(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(false, mOnEndDiscovery);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - finalizeSNMPManager
    // ================================================================================

    public void testFinalizeSNMPManager_DuringIdle() {
        try {
            mSnmpManager.finalizeSNMPManager();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Interface
    // ================================================================================

    @Override
    public void onEndDiscovery(SNMPManager manager, int result) {
        mOnEndDiscovery = true;
        try {
            manager.finalizeSNMPManager();
            mSignal.countDown();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    @Override
    public void onFoundDevice(SNMPManager manager, String ipAddress, String name,
            boolean[] capabilities) {
        mOnFoundDevice = true;
    }
}
