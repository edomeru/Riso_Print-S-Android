package jp.co.riso.smartdeviceapp.common

import android.os.Build
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.common.SNMPManager.SNMPManagerCallback
import junit.framework.TestCase
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SNMPManagerTest : TestCase(), SNMPManagerCallback {
    val mSignal = CountDownLatch(1)
    private val _timeout = 15
    private var mSnmpManager: SNMPManager? = null
    private var mOnEndDiscovery = false
    private var mOnFoundDevice = false
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mSnmpManager = SNMPManager()
        assertNotNull(mSnmpManager)
        mSnmpManager!!.initializeSNMPManager(AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME)
        mOnEndDiscovery = false
        mOnFoundDevice = false
        testSetCallback_ValidCallback()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
        mSnmpManager!!.finalizeSNMPManager()
        testSetCallback_NullCallback()
    }

    // ================================================================================
    // Tests - setCallback
    // ================================================================================
    @Test
    fun testSetCallback_ValidCallback() {
        try {
            mSnmpManager!!.setCallback(this)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetCallback_NullCallback() {
        try {
            mSnmpManager!!.setCallback(null)
        } catch (e: NullPointerException) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - deviceDiscovery
    // ================================================================================
    @Test
    fun testDeviceDiscovery() {
        // if test fails, make sure there are online printers available in network
        if (appContext!!.packageManager.hasSystemFeature(AppConstants.CHROME_BOOK)) {
            return  // if chrome os, skip test because printer search is not supported
        }
        try {
            mSnmpManager!!.deviceDiscovery()
            mSignal.await(_timeout.toLong(), TimeUnit.SECONDS)
            assertEquals(true, mOnFoundDevice)
            assertEquals(true, mOnEndDiscovery)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - manualDiscovery
    // ================================================================================
    /*
    public void testManualDiscovery_OnlineRisoPrinter() {
        try {
            mSnmpManager.manualDiscovery(IPV4_ONLINE_RISO_PRINTER_ADDRESS);

            mSignal.await(_timeout, TimeUnit.SECONDS);

            assertEquals(true, mOnFoundDevice);
            assertEquals(true, mOnEndDiscovery);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testManualDiscovery_OnlineNonRisoPrinter() {
        try {
            mSnmpManager.manualDiscovery(IPV4_ONLINE_NONRISO_PRINTER_ADDRESS);

            mSignal.await(_timeout, TimeUnit.SECONDS);

            assertEquals(true, mOnFoundDevice);
            assertEquals(true, mOnEndDiscovery);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    */
    @Test
    fun testManualDiscovery_OfflinePrinter() {
        try {
            mSnmpManager!!.manualDiscovery(IPV4_OFFLINE_PRINTER_ADDRESS)
            mSignal.await(_timeout.toLong(), TimeUnit.SECONDS)
            assertEquals(true, mOnEndDiscovery)
            assertEquals(false, mOnFoundDevice)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancel
    // ================================================================================
    @Test
    fun testCancel_DuringIdle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Test case causes test app to crash in Android 8
            return
        }
        try {
            mSnmpManager!!.cancel()
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    @Test
    fun testCancel_DuringAutoSearch() {
        try {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    mSnmpManager!!.cancel()
                }
            }, 1000)
            mSnmpManager!!.deviceDiscovery()
            mSignal.await(_timeout.toLong(), TimeUnit.SECONDS)
            assertEquals(false, mOnEndDiscovery)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    @Test
    fun testCancel_DuringManualSearch() {
        try {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    mSnmpManager!!.cancel()
                    mSignal.countDown()
                }
            }, 1000)
            mSnmpManager!!.manualDiscovery(IPV4_OFFLINE_PRINTER_ADDRESS)
            mSignal.await(_timeout.toLong(), TimeUnit.SECONDS)
            assertEquals(false, mOnEndDiscovery)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - finalizeSNMPManager
    // ================================================================================
    @Test
    fun testFinalizeSNMPManager_DuringIdle() {
        try {
            mSnmpManager!!.finalizeSNMPManager()
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Interface
    // ================================================================================
    override fun onEndDiscovery(manager: SNMPManager?, result: Int) {
        mOnEndDiscovery = true
        try {
            manager!!.finalizeSNMPManager()
            mSignal.countDown()
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    override fun onFoundDevice(
        manager: SNMPManager?, ipAddress: String?, name: String?,
        capabilities: BooleanArray?
    ) {
        mOnFoundDevice = true
    }

    companion object {
        private const val IPV4_ONLINE_RISO_PRINTER_ADDRESS = "192.168.1.206"
        private const val IPV4_ONLINE_NONRISO_PRINTER_ADDRESS = "192.168.1.203"
        private const val IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.24"
    }
}