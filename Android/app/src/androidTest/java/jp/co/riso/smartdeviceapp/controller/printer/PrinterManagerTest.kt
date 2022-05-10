package jp.co.riso.smartdeviceapp.controller.printer

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.widget.ImageView
import androidx.test.platform.app.InstrumentationRegistry
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
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PrinterManagerTest : BaseActivityTestUtil(), UpdateStatusCallback, PrintersCallback, PrinterSearchCallback {
    private val _signal = CountDownLatch(1)
    private val _timeout = 20
    private var mImageView: ImageView? = null
    private var mPrinterManager: PrinterManager? = null
    private var mPrintersList: List<Printer?>? = null

    @Before
    fun setUp() {
        initialize()
        mImageView = ImageView(mainActivity)
    }

//    @After
//    override fun tearDown() {
//        mPrinterManager = null
//    }

    // ================================================================================
    // Tests - getInstance
    // ================================================================================
    @Test
    fun testGetInstance() {
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
    @Test
    fun testGetPrinterCount_DuringIdle() {
        try {
            val count: Int = mPrinterManager!!.printerCount
            TestCase.assertEquals(false, count < 0)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - clearDefaultPrinter
    // ================================================================================
    @Test
    fun testClearDefaultPrinter() {
        try {
            mPrinterManager!!.clearDefaultPrinter()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testSetDefaultPrinter_NoDefaultPrinter() {
        try {
            var printer: Printer? = null
            if (mPrintersList != null && mPrintersList!!.isNotEmpty()) {
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

    @Test
    fun testSetDefaultPrinter_WithDefaultPrinter() {
        try {
            var printer: Printer? = null
            if (mPrintersList != null && mPrintersList!!.isNotEmpty()) {
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

    @Test
    fun testSetDefaultPrinter_NullPrinter() {
        try {
            TestCase.assertFalse(mPrinterManager!!.setDefaultPrinter(null))
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetDefaultPrinter_NullDatabaseManager() {
        try {
            var printer: Printer? = null
            mPrinterManager = PrinterManager(appContext, null)
            TestCase.assertFalse(mPrinterManager!!.setDefaultPrinter(printer))
            if (mPrintersList != null && mPrintersList!!.isNotEmpty()) {
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

    @Test
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
    @Test
    fun testGetDefaultPrinter_NoDefaultPrinter() {
        try {
            testClearDefaultPrinter()
            val defaultPrinter: Int = mPrinterManager!!.defaultPrinter
            TestCase.assertEquals(false, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetDefaultPrinter_WithDefaultPrinter() {
        try {
            testSetDefaultPrinter_NoDefaultPrinter()
            val defaultPrinter: Int = mPrinterManager!!.defaultPrinter
            TestCase.assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetDefaultPrinter_WithDefaultPrinterActivityRestarted() {
        try {
            mPrinterManager = getInstance(appContext!!)
            val defaultPrinter: Int = mPrinterManager!!.defaultPrinter
            TestCase.assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testIsCancelled_StateNotCancelled() {
        try {
            mPrinterManager!!.startPrinterSearch()
            val isCancelled: Boolean = mPrinterManager!!.isCancelled
            TestCase.assertEquals(false, isCancelled)
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsCancelled_StateCancelled() {
        try {
            mPrinterManager!!.cancelPrinterSearch()
            val isCancelled: Boolean = mPrinterManager!!.isCancelled
            TestCase.assertEquals(true, isCancelled)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isSearching
    // ================================================================================
    @Test
    fun testIsSearching_StateSearching() {
        try {
            mPrinterManager!!.startPrinterSearch()
            val isSearching: Boolean = mPrinterManager!!.isSearching
            TestCase.assertEquals(true, isSearching)
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsSearching_StateNotSearching() {
        try {
            mPrinterManager!!.cancelPrinterSearch()
            val isSearching: Boolean = mPrinterManager!!.isSearching
            TestCase.assertEquals(false, isSearching)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setPrintersCallback
    // ================================================================================
    @Test
    fun testSetPrintersCallback_NullCallback() {
        try {
            mPrinterManager!!.setPrintersCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testSetPrinterSearchCallback_NullCallback() {
        try {
            mPrinterManager!!.setPrinterSearchCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testSetUpdateStatusCallback_NullCallback() {
        try {
            mPrinterManager!!.setUpdateStatusCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testStartPrinterSearch() {
        try {
            mPrinterManager!!.startPrinterSearch()
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancelPrinterSearch
    // ================================================================================
    @Test
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
    @Test
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
    @Test
    fun testCancelUpdateStatusThread_DuringIdle() {
        try {
            mPrinterManager!!.cancelUpdateStatusThread()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testUpdateOnlineStatus_NullImageView() {
        try {
            mPrinterManager!!.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testUpdateOnlineStatus_NullIpAddress() {
        try {
            mPrinterManager!!.updateOnlineStatus(null, mImageView)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testUpdateOnlineStatus_ValidParameters() {
        mPrinterManager!!.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, mImageView)
        try {
            // Wait and Check if Address is ONLINE
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            Thread.sleep(10000)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - onEndDiscovery
    // ================================================================================
    @Test
    fun testOnEndDiscovery_NullManager() {
        try {
            mPrinterManager!!.onEndDiscovery(null, -1)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
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
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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
    @Test
    fun testRemovePrinter_ValidAndInvalidPrinter() {
        try {
            initialize()
            var printer: Printer? = Printer(
                "testRemovePrinter_ValidAndInvalidPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
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
            var ret: Boolean = mPrinterManager!!.removePrinter(printer)
            TestCase.assertEquals(true, ret)
            ret = mPrinterManager!!.removePrinter(printer)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testRemovePrinter_EmptyList() {
        val printer = Printer(
            "testRemovePrinter_ValidAndInvalidPrinter",
            IPV4_OFFLINE_PRINTER_ADDRESS
        )
        for (printerItem in mPrintersList!!) {
            mPrinterManager!!.removePrinter(printerItem)
        }
        val ret: Boolean = mPrinterManager!!.removePrinter(printer)
        TestCase.assertEquals(false, ret)
    }

    @Test
    fun testRemovePrinter_NullPrinter() {
        try {
            mPrinterManager!!.removePrinter(null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testRemovePrinter_IpAddressExists() {
        try {
            val printer = Printer("testRemovePrinter_IpAddressExists", IPV4_OFFLINE_PRINTER_ADDRESS)
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            printer.name = IPV4_OFFLINE_PRINTER_ADDRESS
            val ret: Boolean = mPrinterManager!!.removePrinter(printer)
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testSavePrinterToDB_ValidPrinter() {
        try {
            var printer: Printer? = Printer(
                "testSavePrinterToDB_ValidPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
            for (savedPrinter in mPrinterManager!!.savedPrintersList) {
                if (printer!!.ipAddress == savedPrinter!!.ipAddress) {
                    printer = savedPrinter
                    break
                }
            }
            mPrinterManager!!.removePrinter(printer)
            if (mPrinterManager!!.printerCount == AppConstants.CONST_MAX_PRINTER_COUNT) {
                mPrinterManager!!.removePrinter(mPrinterManager!!.savedPrintersList[0])
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

    @Test
    fun testSavePrinterToDB_ExistingPrinter() {
        try {
            val printer =
                Printer("testSavePrinterToDB_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS)
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            val ret: Boolean = mPrinterManager!!.savePrinterToDB(printer, true)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSavePrinterToDB_NullPrinter() {
        try {
            mPrinterManager!!.savePrinterToDB(null, true)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSavePrinterToDB_DefaultPrinter() {
        try {
            val printer = Printer(
                "testSavePrinterToDB_ExistingPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
            if (mPrintersList!!.isNotEmpty()) {
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

    @Test
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
    @Test
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

    @Test
    fun testGetSavedPrintersList_EmptyList() {
        try {
            if (mPrintersList!!.isNotEmpty()) {
                for (i in mPrintersList!!.size downTo 1) {
                    mPrinterManager!!.removePrinter(mPrintersList!![i - 1])
                }
            }
            mPrinterManager!!.savedPrintersList
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
    fun testIsExists_ExistingPrinter() {
        try {
            val printer = Printer("testIsExists_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS)
            if (!mPrinterManager!!.isExists(printer)) {
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            var ret: Boolean = mPrinterManager!!.isExists(printer)
            TestCase.assertEquals(true, ret)
            ret = mPrinterManager!!.isExists(printer.ipAddress)
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsExists_NotExistingPrinter() {
        try {
            var printer: Printer? = Printer(
                "testIsExists_NotExistingPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS
            )
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
            var ret: Boolean = mPrinterManager!!.isExists(printer)
            TestCase.assertEquals(false, ret)
            ret = mPrinterManager!!.isExists(printer!!.ipAddress)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
    @Test
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
                _signal.await(1, TimeUnit.SECONDS)
                retry--
            }
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsOnline_OfflinePrinter() {
        try {
            val ret: Boolean = mPrinterManager!!.isOnline(IPV4_OFFLINE_PRINTER_ADDRESS)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsOnline_NullAddress() {
        try {
            val ret: Boolean = mPrinterManager!!.isOnline(null)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsOnline_InvalidAddress() {
        try {
            val ret: Boolean = mPrinterManager!!.isOnline(INVALID_ADDRESS)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - searchPrinter
    // ================================================================================
    @Test
    fun testSearchPrinter_ValidIpAddress() {
        try {
            mPrinterManager!!.searchPrinter(IPV4_ONLINE_PRINTER_ADDRESS)
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSearchPrinter_NullIpAddress() {
        try {
            mPrinterManager!!.searchPrinter(null)
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - UpdateOnlineStatusTask
    // ================================================================================
    @Test
    fun testUpdateOnlineStatusTask_EmptyIpAddress() {
        mPrinterManager!!.UpdateOnlineStatusTask(null, "")
            .execute()
        try {
            // Wait and Check if Address is OFFLINE
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            Thread.sleep(1000)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testUpdateOnlineStatusTask_ValidOfflineParameters() {
        mPrinterManager!!.UpdateOnlineStatusTask(mImageView, IPV4_OFFLINE_PRINTER_ADDRESS)
            .execute()
        try {
            // Wait and Check if Address is OFFLINE
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            Thread.sleep(10000)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testUpdateOnlineStatusTask_ValidIpv4OnlineParameters() {
        mPrinterManager!!.UpdateOnlineStatusTask(mImageView, IPV4_ONLINE_PRINTER_ADDRESS)
            .execute()
        try {
            // Wait and Check if Address is ONLINE
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            Thread.sleep(10000)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            Thread.sleep(10000)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - UpdateOnlineStatusTask
    // ================================================================================
    @Test
    fun testGetIdFromCursor_NullCursor() {
        try {
            mPrinterManager!!.getIdFromCursor(null, null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
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

    @Test
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
    @Test
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

    @Test
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

    @Test
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

    @Test
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
    @Test
    fun testUpdatePortSettings() {
        try {
            var printer: Printer? = null
            if (mPrintersList != null && mPrintersList!!.isNotEmpty()) {
                printer = mPrintersList!![0]
            }
            if (printer == null) {
                printer = Printer("testUpdatePortSettings", IPV4_OFFLINE_PRINTER_ADDRESS)
                mPrinterManager!!.savePrinterToDB(printer, true)
            }
            val dbManager = DatabaseManager(appContext)
            var ret: Boolean = mPrinterManager!!.updatePortSettings(printer.id, PortSetting.LPR)
            TestCase.assertTrue(ret)
            var cursor: Cursor? = dbManager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE, arrayOf(KeyConstants.KEY_SQL_PRINTER_PORT),
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", arrayOf("" + printer.id),
                null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
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
            if (cursor != null && cursor.moveToFirst()) {
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

    @Test
    fun testUpdatePortSettings_NullPortSetting() {
        try {
            var printer: Printer? = null
            if (mPrintersList != null && mPrintersList!!.isNotEmpty()) {
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
    @Test
    fun testGetPrinterType() {
        try {
            var printer = Printer("RISO IS1000C-G", "192.168.0.1")
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
                nonExistentId += mPrinterManager!!.savedPrintersList[mPrinterManager!!.printerCount - 1]!!.id
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
        _signal.countDown()
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
        get() {
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