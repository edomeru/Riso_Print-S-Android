package jp.co.riso.smartdeviceapp.controller.printer

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.test.ActivityInstrumentationTestCase2
import android.widget.ImageView
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.common.SNMPManager
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.*
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.setupPrinterConfig
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting
import jp.co.riso.smartdeviceapp.view.MainActivity
import junit.framework.TestCase
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PrinterManagerTest : ActivityInstrumentationTestCase2<MainActivity>(
    MainActivity::class.java
), UpdateStatusCallback, PrintersCallback, PrinterSearchCallback {
    val mSignal = CountDownLatch(1)
    val TIMEOUT = 20
    private var mImageView: ImageView? = null
    private var mPrinterManager: PrinterManager? = null
    private var mPrintersList: List<Printer?>? = null
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        initialize()
        mImageView = ImageView(activity)
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
        mPrinterManager = null
    }

    // ================================================================================
    // Tests - getInstance
    // ================================================================================
    fun testgetInstance() {
        try {
            mPrinterManager = null
            mPrinterManager = getInstance(appContext!!)
            TestCase.assertNotNull(mPrinterManager)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getPrinterCount
    // ================================================================================
    fun testGetPrinterCount_DuringIdle() {
        try {
            val count: Int
            count = mPrinterManager!!.printerCount
            TestCase.assertEquals(false, count < 0)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - clearDefaultPrinter
    // ================================================================================
    fun testClearDefaultPrinter() {
        try {
            mPrinterManager!!.clearDefaultPrinter()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testClearDefaultPrinter_NullDatabaseManager() {
        try {
            mPrinterManager = PrinterManager(appContext, null)
            mPrinterManager!!.clearDefaultPrinter()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setDefaultPrinter
    // ================================================================================
    fun testSetDefaultPrinter_NoDefaultPrinter() {
        try {
            var printer: Printer? = null
            if (mPrintersList != null && !mPrintersList!!.isEmpty()) {
                printer = mPrintersList!![0]
            }
            if (printer == null) {
                printer =
                    Printer("testSetDefaultPrinter_NoDefaultPrinter", IPV4_OFFLINE_PRINTER_ADDRESS)
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            testClearDefaultPrinter()
            TestCase.assertTrue(mPrinterManager!!.setDefaultPrinter(printer))
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSetDefaultPrinter_WithDefaultPrinter() {
        try {
            var printer: Printer? = null
            if (mPrintersList != null && !mPrintersList!!.isEmpty()) {
                printer = mPrintersList!![0]
            }
            if (printer == null) {
                printer = Printer(
                    "testSetDefaultPrinter_WithDefaultPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS
                )
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            testSetDefaultPrinter_NoDefaultPrinter()
            TestCase.assertTrue(mPrinterManager!!.setDefaultPrinter(printer))
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSetDefaultPrinter_NullPrinter() {
        try {
            TestCase.assertFalse(mPrinterManager!!.setDefaultPrinter(null))
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSetDefaultPrinter_NullDatabaseManager() {
        try {
            var printer: Printer? = null
            mPrinterManager = PrinterManager(appContext, null)
            TestCase.assertFalse(mPrinterManager!!.setDefaultPrinter(printer))
            if (mPrintersList != null && !mPrintersList!!.isEmpty()) {
                printer = mPrintersList!![0]
            }
            if (printer == null) {
                printer = Printer(
                    "testSetDefaultPrinter_WithDefaultPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS
                )
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            TestCase.assertTrue(mPrinterManager!!.setDefaultPrinter(printer))
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSetDefaultPrinter_DatabaseError() {
        val printer = Printer("", IPV4_OFFLINE_PRINTER_ADDRESS)
        val dbManager = MockedDatabaseManager(
            appContext
        )
        mPrinterManager = PrinterManager(appContext, dbManager)
        TestCase.assertFalse(mPrinterManager!!.setDefaultPrinter(printer))
    }

    // ================================================================================
    // Tests - getDefaultPrinter
    // ================================================================================
    fun testGetDefaultPrinter_NoDefaultPrinter() {
        try {
            val defaultPrinter: Int
            testClearDefaultPrinter()
            defaultPrinter = mPrinterManager!!.defaultPrinter
            TestCase.assertEquals(false, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testGetDefaultPrinter_WithDefaultPrinter() {
        try {
            val defaultPrinter: Int
            testSetDefaultPrinter_NoDefaultPrinter()
            defaultPrinter = mPrinterManager!!.defaultPrinter
            TestCase.assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testGetDefaultPrinter_WithDefaultPrinterActivityRestarted() {
        try {
            mPrinterManager = getInstance(appContext!!)
            val defaultPrinter: Int
            defaultPrinter = mPrinterManager!!.defaultPrinter
            TestCase.assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testGetDefaultPrinter_NullDatabaseManager() {
        try {
            mPrinterManager = PrinterManager(appContext, null)
            mPrinterManager!!.defaultPrinter
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isCancelled
    // ================================================================================
    fun testIsCancelled_StateNotCancelled() {
        try {
            val isCancelled: Boolean
            mPrinterManager!!.startPrinterSearch()
            isCancelled = mPrinterManager!!.isCancelled
            TestCase.assertEquals(false, isCancelled)
            mSignal.await(TIMEOUT.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testIsCancelled_StateCancelled() {
        try {
            val isCancelled: Boolean
            mPrinterManager!!.cancelPrinterSearch()
            isCancelled = mPrinterManager!!.isCancelled
            TestCase.assertEquals(true, isCancelled)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isSearching
    // ================================================================================
    fun testIsSearching_StateSearching() {
        try {
            val isSearching: Boolean
            mPrinterManager!!.startPrinterSearch()
            isSearching = mPrinterManager!!.isSearching
            TestCase.assertEquals(true, isSearching)
            mSignal.await(TIMEOUT.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testIsSearching_StateNotSearching() {
        try {
            val isSearching: Boolean
            mPrinterManager!!.cancelPrinterSearch()
            isSearching = mPrinterManager!!.isSearching
            TestCase.assertEquals(false, isSearching)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setPrintersCallback
    // ================================================================================
    fun testSetPrintersCallback_NullCallback() {
        try {
            mPrinterManager!!.setPrintersCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSetPrintersCallback_ValidCallback() {
        try {
            mPrinterManager!!.setPrintersCallback(this)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setPrinterSearchCallback
    // ================================================================================
    fun testSetPrinterSearchCallback_NullCallback() {
        try {
            mPrinterManager!!.setPrinterSearchCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSetPrinterSearchCallback_ValidCallback() {
        try {
            mPrinterManager!!.setPrinterSearchCallback(this)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setUpdateStatusCallback
    // ================================================================================
    fun testSetUpdateStatusCallback_NullCallback() {
        try {
            mPrinterManager!!.setUpdateStatusCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSetUpdateStatusCallback_ValidCallback() {
        try {
            mPrinterManager!!.setUpdateStatusCallback(this)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - startPrinterSearch
    // ================================================================================
    fun testStartPrinterSearch() {
        try {
            mPrinterManager!!.startPrinterSearch()
            mSignal.await(TIMEOUT.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancelPrinterSearch
    // ================================================================================
    fun testCancelPrinterSearch() {
        try {
            mPrinterManager!!.cancelPrinterSearch()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - createUpdateStatusThread
    // ================================================================================
    fun testCreateUpdateStatusThread() {
        try {
            mPrinterManager!!.createUpdateStatusThread()

            // Create another instance
            mPrinterManager!!.createUpdateStatusThread()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancelUpdateStatusThread
    // ================================================================================
    fun testCancelUpdateStatusThread_DuringIdle() {
        try {
            mPrinterManager!!.cancelUpdateStatusThread()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testCancelUpdateStatusThread_AfterCreate() {
        try {
            mPrinterManager!!.createUpdateStatusThread()
            mPrinterManager!!.cancelUpdateStatusThread()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - updateOnlineStatus
    // ================================================================================
    fun testUpdateOnlineStatus_NullImageView() {
        try {
            mPrinterManager!!.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testUpdateOnlineStatus_NullIpAddress() {
        try {
            mPrinterManager!!.updateOnlineStatus(null, mImageView)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testUpdateOnlineStatus_ValidParameters() {
        mPrinterManager!!.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, mImageView)
        try {
            // Wait and Check if Address is ONLINE
            instrumentation.waitForIdleSync()
            Thread.sleep(10000)
            instrumentation.waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - onEndDiscovery
    // ================================================================================
    fun testOnEndDiscovery_NullManager() {
        try {
            mPrinterManager!!.onEndDiscovery(null, -1)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testOnEndDiscovery_ValidParameters() {
        try {
            mPrinterManager!!.onEndDiscovery(SNMPManager(), -1)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - onFoundDevice
    // ================================================================================
    fun testOnFoundDevice_ValidParameters() {
        try {

            // Trigger Printer Search
            mPrinterManager!!.startPrinterSearch()
            mPrinterManager!!.onFoundDevice(
                SNMPManager(),
                IPV4_ONLINE_PRINTER_ADDRESS,
                "testOnFoundDevice_ValidParameters",
                BooleanArray(10)
            )
            mSignal.await(TIMEOUT.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testOnFoundDevice_NullManager() {
        try {
            mPrinterManager!!.onFoundDevice(
                null,
                IPV4_ONLINE_PRINTER_ADDRESS,
                "testOnFoundDevice_NullManager",
                BooleanArray(10)
            )
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testOnFoundDevice_NullIpAddress() {
        try {
            mPrinterManager!!.onFoundDevice(
                SNMPManager(),
                null,
                "testOnFoundDevice_NullIpAddress",
                BooleanArray(10)
            )
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testOnFoundDevice_NullName() {
        try {
            initialize()
            mPrinterManager!!.onFoundDevice(
                SNMPManager(),
                IPV4_ONLINE_PRINTER_ADDRESS,
                null,
                BooleanArray(10)
            )
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testOnFoundDevice_NullCapabilities() {
        try {
            initialize()
            mPrinterManager!!.onFoundDevice(
                SNMPManager(), IPV4_ONLINE_PRINTER_ADDRESS,
                "testOnFoundDevice_NullCapabilities", null
            )
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - removePrinter
    // ================================================================================
    fun testRemovePrinter_ValidAndInvalidPrinter() {
        try {
            initialize()
            var printer: Printer? = Printer(
                "testRemovePrinter_ValidAndInvalidPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
            var ret: Boolean
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            } else {
                for (printerItem in mPrintersList!!) {
                    if (printerItem!!.ipAddress.contentEquals(IPV4_OFFLINE_PRINTER_ADDRESS)) {
                        printer = printerItem
                        break
                    }
                }
            }
            ret = mPrinterManager!!.removePrinter(printer)
            TestCase.assertEquals(true, ret)
            ret = mPrinterManager!!.removePrinter(printer)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testRemovePrinter_EmptyList() {
        val printer = Printer(
            "testRemovePrinter_ValidAndInvalidPrinter",
            IPV4_OFFLINE_PRINTER_ADDRESS
        )
        val ret: Boolean
        for (printerItem in mPrintersList!!) {
            mPrinterManager!!.removePrinter(printerItem)
        }
        ret = mPrinterManager!!.removePrinter(printer)
        TestCase.assertEquals(false, ret)
    }

    fun testRemovePrinter_NullPrinter() {
        try {
            mPrinterManager!!.removePrinter(null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testRemovePrinter_IpAddressExists() {
        try {
            val printer = Printer("testRemovePrinter_IpAddressExists", IPV4_OFFLINE_PRINTER_ADDRESS)
            val ret: Boolean
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            printer.name = IPV4_OFFLINE_PRINTER_ADDRESS
            ret = mPrinterManager!!.removePrinter(printer)
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testRemovePrinter_NullDatabaseManager() {
        try {
            val printer = Printer("testRemovePrinter_IpAddressExists", IPV4_OFFLINE_PRINTER_ADDRESS)
            mPrinterManager = PrinterManager(appContext, null)
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            mPrinterManager!!.removePrinter(printer)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - savePrinterToDB
    // ================================================================================
    fun testSavePrinterToDB_ValidPrinter() {
        try {
            var printer: Printer? = Printer(
                "testSavePrinterToDB_ValidPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
            for (savedPrinter in mPrinterManager!!.savedPrintersList!!) {
                if (printer!!.ipAddress == savedPrinter!!.ipAddress) {
                    printer = savedPrinter
                    break
                }
            }
            mPrinterManager!!.removePrinter(printer)
            if (mPrinterManager!!.printerCount == AppConstants.CONST_MAX_PRINTER_COUNT) {
                mPrinterManager!!.removePrinter(mPrinterManager!!.savedPrintersList!![0])
            }

            //Enabled Capabilities
            printer!!.config!!.isLprAvailable = true
            printer.config!!.isRawAvailable = true
            printer.config!!.isBookletFinishingAvailable = true
            printer.config!!.isStaplerAvailable = true
            printer.config!!.isPunch3Available = true
            printer.config!!.isPunch4Available = true
            printer.config!!.isTrayFaceDownAvailable = true
            printer.config!!.isTrayTopAvailable = true
            printer.config!!.isTrayStackAvailable = true
            val ret = mPrinterManager!!.savePrinterToDB(printer, true)
            TestCase.assertEquals(true, ret)

            //Disabled Capabilities
            printer.config!!.isLprAvailable = false
            printer.config!!.isRawAvailable = false
            printer.config!!.isBookletFinishingAvailable = false
            printer.config!!.isStaplerAvailable = false
            printer.config!!.isPunch3Available = true
            printer.config!!.isPunch4Available = false
            printer.config!!.isTrayFaceDownAvailable = false
            printer.config!!.isTrayTopAvailable = false
            printer.config!!.isTrayStackAvailable = false
            mPrinterManager!!.removePrinter(printer)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSavePrinterToDB_ExistingPrinter() {
        try {
            val printer =
                Printer("testSavePrinterToDB_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS)
            val ret: Boolean
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            ret = mPrinterManager!!.savePrinterToDB(printer, true)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSavePrinterToDB_NullPrinter() {
        try {
            mPrinterManager!!.savePrinterToDB(null, true)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSavePrinterToDB_DefaultPrinter() {
        try {
            val printer = Printer(
                "testSavePrinterToDB_ExistingPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
            if (!mPrintersList!!.isEmpty()) {
                for (i in mPrintersList!!.size downTo 1) {
                    mPrinterManager!!.removePrinter(mPrintersList!![i - 1])
                }
            }
            mPrinterManager!!.setPrintersCallback(this)
            mPrinterManager!!.savePrinterToDB(printer, true)
            mPrinterManager!!.setPrintersCallback(null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSavePrinterToDB_DatabaseError() {
        try {
            val printer = Printer(
                "testSavePrinterToDB_ExistingPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
            val dbManager = MockedDatabaseManager(
                appContext
            )
            mPrinterManager = PrinterManager(appContext, dbManager)
            mPrinterManager!!.savePrinterToDB(printer, true)
            dbManager.setSavePrinterInfoRet(true)
            mPrinterManager!!.savePrinterToDB(printer, true)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getSavedPrintersList
    // ================================================================================
    fun testGetSavedPrintersList_NonEmptyList() {
        try {
            if (mPrintersList!!.isEmpty()) {
                mPrinterManager!!.savePrinterToDB(Printer("", IPV4_OFFLINE_PRINTER_ADDRESS), false)
                mPrinterManager!!.savePrinterToDB(Printer("", IPV4_ONLINE_PRINTER_ADDRESS), true)
            }
            mPrinterManager!!.savedPrintersList
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testGetSavedPrintersList_EmptyList() {
        try {
            if (!mPrintersList!!.isEmpty()) {
                for (i in mPrintersList!!.size downTo 1) {
                    mPrinterManager!!.removePrinter(mPrintersList!![i - 1])
                }
            }
            mPrinterManager!!.savedPrintersList
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testGetSavedPrintersList_NullDatabaseManager() {
        try {
            mPrinterManager = PrinterManager(appContext, null)
            mPrinterManager!!.savedPrintersList
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isExists
    // ================================================================================
    fun testIsExists_ExistingPrinter() {
        try {
            val printer = Printer("testIsExists_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS)
            var ret: Boolean
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            ret = mPrinterManager!!.isExists(printer)
            TestCase.assertEquals(true, ret)
            ret = mPrinterManager!!.isExists(printer.ipAddress)
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testIsExists_NotExistingPrinter() {
        try {
            var printer: Printer? = Printer(
                "testIsExists_NotExistingPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
            var ret: Boolean
            if (mPrintersList != null) {
                for (savedPrinter in mPrintersList!!) {
                    if (printer!!.ipAddress == savedPrinter!!.ipAddress) {
                        printer = savedPrinter
                        break
                    }
                }
            }
            if (mPrintersList!!.isEmpty()) {
                mPrinterManager!!.savePrinterToDB(Printer("", IPV4_ONLINE_PRINTER_ADDRESS), true)
                mPrinterManager!!.savePrinterToDB(Printer("", IPV6_ONLINE_PRINTER_ADDRESS), true)
            }
            mPrinterManager!!.removePrinter(printer)
            ret = mPrinterManager!!.isExists(printer)
            TestCase.assertEquals(false, ret)
            ret = mPrinterManager!!.isExists(printer!!.ipAddress)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testIsExists_NullPrinter() {
        try {
            val printer: Printer? = null
            val ipAddress: String? = null
            mPrinterManager!!.isExists(printer)
            mPrinterManager!!.isExists(ipAddress)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isOnline
    // ================================================================================
    /*
    public void testIsOnline_OnlineIpv4Printer() {
        try {
            boolean ret = false;
            initialize();
            
            ret = mPrinterManager.isOnline(IPV4_ONLINE_PRINTER_ADDRESS);
            assertEquals(true, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    */
    fun testIsOnline_OnlineIpv6Printer() {
        // If test fails, check with actual ipv6 address
        try {
            var ret = false
            var retry = 10
            initialize()
            var ipv6Addr: String? =
                IPV6_ONLINE_PRINTER_ADDRESS // If test fails, update constant to use actual ipv6

            // Ipv6 Address
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ipv6Addr =
                    localIpv6Address // If test fails, comment out this line to use actual ipv6
            }
            TestCase.assertNotNull(ipv6Addr)
            while (retry > 0) {
                ret = mPrinterManager!!.isOnline(ipv6Addr)
                if (ret) {
                    break
                }
                mSignal.await(1, TimeUnit.SECONDS)
                retry--
            }
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testIsOnline_OfflinePrinter() {
        try {
            val ret: Boolean
            ret = mPrinterManager!!.isOnline(IPV4_OFFLINE_PRINTER_ADDRESS)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testIsOnline_NullAddress() {
        try {
            val ret: Boolean
            ret = mPrinterManager!!.isOnline(null)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testIsOnline_InvalidAddress() {
        try {
            val ret: Boolean
            ret = mPrinterManager!!.isOnline(INVALID_ADDRESS)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - searchPrinter
    // ================================================================================
    fun testSearchPrinter_ValidIpAddress() {
        try {
            mPrinterManager!!.searchPrinter(IPV4_ONLINE_PRINTER_ADDRESS)
            mSignal.await(TIMEOUT.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testSearchPrinter_NullIpAddress() {
        try {
            mPrinterManager!!.searchPrinter(null)
            mSignal.await(TIMEOUT.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - UpdateOnlineStatusTask
    // ================================================================================
    fun testUpdateOnlineStatusTask_EmptyIpAddress() {
        mPrinterManager!!.UpdateOnlineStatusTask(null, "")
            .execute()
        try {
            // Wait and Check if Address is OFFLINE
            instrumentation.waitForIdleSync()
            Thread.sleep(1000)
            instrumentation.waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testUpdateOnlineStatusTask_ValidOfflineParameters() {
        mPrinterManager!!.UpdateOnlineStatusTask(mImageView, IPV4_OFFLINE_PRINTER_ADDRESS)
            .execute()
        try {
            // Wait and Check if Address is OFFLINE
            instrumentation.waitForIdleSync()
            Thread.sleep(10000)
            instrumentation.waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testUpdateOnlineStatusTask_ValidIpv4OnlineParameters() {
        mPrinterManager!!.UpdateOnlineStatusTask(mImageView, IPV4_ONLINE_PRINTER_ADDRESS)
            .execute()
        try {
            // Wait and Check if Address is ONLINE
            instrumentation.waitForIdleSync()
            Thread.sleep(10000)
            instrumentation.waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testUpdateOnlineStatusTask_ValidIpv6OnlineParameters() {
        var ipv6Addr: String? = IPV6_ONLINE_PRINTER_ADDRESS

        // Ipv6 Address
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ipv6Addr = localIpv6Address
        }
        TestCase.assertNotNull(ipv6Addr)
        mPrinterManager!!.UpdateOnlineStatusTask(mImageView, ipv6Addr!!)
            .execute()
        try {
            // Wait and Check if Address is ONLINE
            instrumentation.waitForIdleSync()
            Thread.sleep(10000)
            instrumentation.waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - UpdateOnlineStatusTask
    // ================================================================================
    fun testGetIdFromCursor_NullCursor() {
        try {
            mPrinterManager!!.getIdFromCursor(null, null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testGetIdFromCursor_NullPrinter() {
        try {
            val dbManager = DatabaseManager(appContext)
            val cursor = dbManager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE, null,
                KeyConstants.KEY_SQL_PRINTER_IP + "=?", arrayOf(IPV4_ONLINE_PRINTER_ADDRESS),
                null, null, null
            )
            mPrinterManager!!.getIdFromCursor(cursor, null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testGetIdFromCursor_ValidParameters() {
        try {
            val printer =
                Printer("testGetIdFromCursor_ValidParameters", IPV4_ONLINE_PRINTER_ADDRESS)
            val dbManager = DatabaseManager(appContext)
            val cursor = dbManager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE, null,
                KeyConstants.KEY_SQL_PRINTER_IP + "=?", arrayOf(IPV4_ONLINE_PRINTER_ADDRESS),
                null, null, null
            )
            mPrinterManager!!.getIdFromCursor(cursor, printer)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setupPrinterConfig
    // ================================================================================
    fun testSetupPrinterConfig_null() {
        try {
            setupPrinterConfig(null, null)
            setupPrinterConfig(null, BooleanArray(1))
            setupPrinterConfig(Printer("test", "ip"), null)
        } catch (e: Exception) {
            // No exception should happen
            TestCase.fail()
        }
    }

    fun testSetupPrinterConfig_allAvailable() {
        val capabilities = booleanArrayOf(
            true, true, true, true, true, true, true, true, true, true, true
        )
        val target = Printer("test GL", "ip")
        setupPrinterConfig(target, capabilities)
        TestCase.assertTrue(target.config!!.isBookletFinishingAvailable)
        TestCase.assertTrue(target.config!!.isStaplerAvailable)
        TestCase.assertTrue(target.config!!.isPunch3Available)
        TestCase.assertTrue(target.config!!.isPunch4Available)
        TestCase.assertTrue(target.config!!.isPunch0Available)
        TestCase.assertTrue(target.config!!.isExternalFeederAvailable)
        TestCase.assertTrue(target.config!!.isTrayFaceDownAvailable)
        TestCase.assertTrue(target.config!!.isTrayStackAvailable)
        TestCase.assertTrue(target.config!!.isTrayTopAvailable)
        TestCase.assertTrue(target.config!!.isLprAvailable)
        TestCase.assertTrue(target.config!!.isRawAvailable)
    }

    fun testSetupPrinterConfig_allFalse() {
        val capabilities = booleanArrayOf(
            false, false, false, false, false, false, false, false, false, false, false
        )
        val target = Printer("test GL", "ip")
        setupPrinterConfig(target, capabilities)
        TestCase.assertFalse(target.config!!.isBookletFinishingAvailable)
        TestCase.assertFalse(target.config!!.isStaplerAvailable)
        TestCase.assertFalse(target.config!!.isPunch3Available)
        TestCase.assertFalse(target.config!!.isPunch4Available)
        TestCase.assertFalse(target.config!!.isPunch0Available)
        TestCase.assertFalse(target.config!!.isExternalFeederAvailable)
        TestCase.assertFalse(target.config!!.isTrayFaceDownAvailable)
        TestCase.assertFalse(target.config!!.isTrayStackAvailable)
        TestCase.assertFalse(target.config!!.isTrayTopAvailable)
        TestCase.assertFalse(target.config!!.isLprAvailable)
        TestCase.assertFalse(target.config!!.isRawAvailable)
    }

    fun testSetupPrinterConfig_Incomplete() {
        val capabilities = booleanArrayOf(
            false
        )
        val target = Printer("test", "ip")
        try {
            setupPrinterConfig(target, capabilities)
        } catch (e: Exception) {
            // No exception should happen
            TestCase.fail()
        }
        val defaultPrinter = Printer("test", "ip")
        TestCase.assertFalse(target.config!!.isBookletFinishingAvailable)
        TestCase.assertEquals(
            defaultPrinter.config!!.isStaplerAvailable,
            target.config!!.isStaplerAvailable
        )
        TestCase.assertEquals(
            defaultPrinter.config!!.isPunch3Available,
            target.config!!.isPunch3Available
        )
        TestCase.assertEquals(
            defaultPrinter.config!!.isPunch4Available,
            target.config!!.isPunch4Available
        )
        TestCase.assertEquals(
            defaultPrinter.config!!.isTrayFaceDownAvailable,
            target.config!!.isTrayFaceDownAvailable
        )
        TestCase.assertEquals(
            defaultPrinter.config!!.isTrayStackAvailable,
            target.config!!.isTrayStackAvailable
        )
        TestCase.assertEquals(
            defaultPrinter.config!!.isTrayTopAvailable,
            target.config!!.isTrayTopAvailable
        )
        TestCase.assertEquals(
            defaultPrinter.config!!.isLprAvailable,
            target.config!!.isLprAvailable
        )
        TestCase.assertEquals(
            defaultPrinter.config!!.isRawAvailable,
            target.config!!.isRawAvailable
        )
    }

    // ================================================================================
    // Tests - updatePortSettings
    // ================================================================================
    fun testUpdatePortSettings() {
        try {
            var printer: Printer? = null
            var ret: Boolean
            if (mPrintersList != null && !mPrintersList!!.isEmpty()) {
                printer = mPrintersList!![0]
            }
            if (printer == null) {
                printer = Printer("testUpdatePortSettings", IPV4_OFFLINE_PRINTER_ADDRESS)
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            val dbManager = DatabaseManager(appContext)
            var cursor: Cursor?
            ret = mPrinterManager!!.updatePortSettings(printer.id, PortSetting.LPR)
            TestCase.assertTrue(ret)
            cursor = dbManager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE, arrayOf(KeyConstants.KEY_SQL_PRINTER_PORT),
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", arrayOf("" + printer.id),
                null, null, null
            )
            if (cursor != null && cursor.moveToFirst() && printer != null) {
                TestCase.assertEquals(
                    0,
                    cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_PORT))
                )
                cursor.close()
            }
            ret = mPrinterManager!!.updatePortSettings(printer.id, PortSetting.RAW)
            TestCase.assertTrue(ret)
            cursor = dbManager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE, arrayOf(KeyConstants.KEY_SQL_PRINTER_PORT),
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", arrayOf("" + printer.id),
                null, null, null
            )
            if (cursor != null && cursor.moveToFirst() && printer != null) {
                TestCase.assertEquals(
                    1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_PORT))
                )
                cursor.close()
            }
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    fun testUpdatePortSettings_NullPortSetting() {
        try {
            var printer: Printer? = null
            if (mPrintersList != null && !mPrintersList!!.isEmpty()) {
                printer = mPrintersList!![0]
            }
            if (printer == null) {
                printer = Printer("testUpdatePortSettings", IPV4_OFFLINE_PRINTER_ADDRESS)
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            val ret = mPrinterManager!!.updatePortSettings(printer.id, null)
            TestCase.assertFalse(ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getPrinterType
    // ================================================================================
    fun testGetPrinterType() {
        try {
            var printer: Printer
            printer = Printer("RISO IS1000C-G", "192.168.0.1")
            mPrinterManager!!.savePrinterToDB(printer, true)
            var printerType = mPrinterManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_IS)
            mPrinterManager!!.removePrinter(printer)
            printer = Printer("ORPHIS GD500", "192.168.0.2")
            mPrinterManager!!.savePrinterToDB(printer, true)
            printerType = mPrinterManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_GD)
            mPrinterManager!!.removePrinter(printer)
            printer = Printer("ORPHIS FW1000", "192.168.0.3")
            mPrinterManager!!.savePrinterToDB(printer, true)
            printerType = mPrinterManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_FW)
            mPrinterManager!!.removePrinter(printer)
            printer = Printer("ORPHIS FT100", "192.168.0.4")
            mPrinterManager!!.savePrinterToDB(printer, true)
            printerType = mPrinterManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
            mPrinterManager!!.removePrinter(printer)
            printer = Printer("ORPHIS GL200", "192.168.0.5")
            mPrinterManager!!.savePrinterToDB(printer, true)
            printerType = mPrinterManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_GL)
            mPrinterManager!!.removePrinter(printer)
            printer = Printer("RISO CEREZONA S200", "192.168.0.4")
            mPrinterManager!!.savePrinterToDB(printer, true)
            printerType = mPrinterManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
            mPrinterManager!!.removePrinter(printer)
            var nonExistentId = 2
            if (mPrinterManager!!.printerCount > 0) {
                nonExistentId += mPrinterManager!!.savedPrintersList!![mPrinterManager!!.printerCount - 1]!!.id
            }
            printerType = mPrinterManager!!.getPrinterType(nonExistentId)
            TestCase.assertNull(printerType)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Interface
    // ================================================================================
    override fun updateOnlineStatus() {}
    override fun onAddedNewPrinter(printer: Printer?, isOnline: Boolean) {}
    override fun onPrinterAdd(printer: Printer?) {}
    override fun onSearchEnd() {
        mSignal.countDown()
    }

    class MockedDatabaseManager(context: Context?) : DatabaseManager(context) {
        private var mInsert = false
        fun setSavePrinterInfoRet(ret: Boolean) {
            mInsert = ret
        }

        override fun insert(
            table: String,
            nullColumnHack: String?,
            values: ContentValues?
        ): Boolean {
            return mInsert
        }

        override fun query(
            table: String,
            columns: Array<String?>?,
            selection: String?,
            selectionArgs: Array<String?>?,
            groupBy: String?,
            having: String?,
            orderBy: String?
        ): Cursor? {
            return null
        }
    }

    // ================================================================================
    // Private
    // ================================================================================
    private fun initialize() {
        mPrinterManager = PrinterManager(appContext, null)
        TestCase.assertNotNull(mPrinterManager)
        mPrinterManager!!.setPrinterSearchCallback(this)
        mPrinterManager!!.setPrintersCallback(this)
        mPrinterManager!!.setUpdateStatusCallback(this)
        mPrintersList = mPrinterManager!!.savedPrintersList
        TestCase.assertNotNull(mPrintersList)
    }

    private val localIpv6Address: String?
        private get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en
                        .hasMoreElements()
                ) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr
                            .hasMoreElements()
                    ) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet6Address) {
                            return inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (ex: SocketException) {
            }
            return null
        }

    companion object {
        private const val IPV4_ONLINE_PRINTER_ADDRESS = "192.168.1.206"
        private const val IPV6_ONLINE_PRINTER_ADDRESS = "fe80::a00:27ff:fe95:7387"
        private const val IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.206"
        private const val INVALID_ADDRESS = "invalid"
    }
}