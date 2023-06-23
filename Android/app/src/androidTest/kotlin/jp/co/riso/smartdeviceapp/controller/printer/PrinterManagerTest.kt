package jp.co.riso.smartdeviceapp.controller.printer

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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
import org.junit.After
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
    private var _imageView: ImageView? = null
    private var _printerManager: PrinterManager? = null
    private var _printersList: List<Printer?>? = null

    @Before
    fun setUp() {
        initialize()
        _imageView = ImageView(mainActivity)
    }

    @After
    fun cleanUp() {
        clearPrintersList()
        _printerManager = null
    }

    // ================================================================================
    // Tests - getInstance
    // ================================================================================
    @Test
    fun testGetInstance() {
        try {
            _printerManager = null
            _printerManager = getInstance(appContext!!)
            TestCase.assertNotNull(_printerManager)
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
            val count: Int = _printerManager!!.printerCount
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
            _printerManager!!.clearDefaultPrinter()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testClearDefaultPrinter_NullDatabaseManager() {
        try {
            _printerManager = PrinterManager(appContext, null)
            _printerManager!!.clearDefaultPrinter()
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
            if (_printersList != null && _printersList!!.isNotEmpty()) {
                printer = _printersList!![0]
            }
            if (printer == null) {
                printer =
                    Printer("testSetDefaultPrinter_NoDefaultPrinter", IPV4_OFFLINE_PRINTER_ADDRESS,
                    VALID_MAC_ADDRESS2)
                _printerManager!!.savePrinterToDB(printer, true)
            }
            testClearDefaultPrinter()
            TestCase.assertTrue(_printerManager!!.setDefaultPrinter(printer))
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetDefaultPrinter_WithDefaultPrinter() {
        try {
            var printer: Printer? = null
            if (_printersList != null && _printersList!!.isNotEmpty()) {
                printer = _printersList!![0]
            }
            if (printer == null) {
                printer = Printer(
                    "testSetDefaultPrinter_WithDefaultPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS,
                    VALID_MAC_ADDRESS2
                )
                _printerManager!!.savePrinterToDB(printer, true)
            }
            testSetDefaultPrinter_NoDefaultPrinter()
            TestCase.assertTrue(_printerManager!!.setDefaultPrinter(printer))
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetDefaultPrinter_NullPrinter() {
        try {
            TestCase.assertFalse(_printerManager!!.setDefaultPrinter(null))
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetDefaultPrinter_NullDatabaseManager() {
        try {
            var printer: Printer? = null
            _printerManager = PrinterManager(appContext, null)
            TestCase.assertFalse(_printerManager!!.setDefaultPrinter(printer))
            if (_printersList != null && _printersList!!.isNotEmpty()) {
                printer = _printersList!![0]
            }
            if (printer == null) {
                printer = Printer(
                    "testSetDefaultPrinter_WithDefaultPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS,
                    VALID_MAC_ADDRESS2
                )
                _printerManager!!.savePrinterToDB(printer, true)
            }
            TestCase.assertTrue(_printerManager!!.setDefaultPrinter(printer))
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetDefaultPrinter_DatabaseError() {
        val printer = Printer("", IPV4_OFFLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS2)
        val dbManager = MockedDatabaseManager(
            appContext
        )
        _printerManager = PrinterManager(appContext, dbManager)
        TestCase.assertFalse(_printerManager!!.setDefaultPrinter(printer))
    }

    // ================================================================================
    // Tests - getDefaultPrinter
    // ================================================================================
    @Test
    fun testGetDefaultPrinter_NoDefaultPrinter() {
        try {
            testClearDefaultPrinter()
            val defaultPrinter: Int = _printerManager!!.defaultPrinter
            TestCase.assertEquals(false, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetDefaultPrinter_WithDefaultPrinter() {
        try {
            testSetDefaultPrinter_NoDefaultPrinter()
            val defaultPrinter: Int = _printerManager!!.defaultPrinter
            TestCase.assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetDefaultPrinter_WithDefaultPrinterActivityRestarted() {
        testSetDefaultPrinter_WithDefaultPrinter()
        _printerManager = null
        testRule.scenario.recreate()
        try {
            _printerManager = getInstance(appContext!!)
            val defaultPrinter: Int = _printerManager!!.defaultPrinter
            TestCase.assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetDefaultPrinter_NullDatabaseManager() {
        try {
            _printerManager = PrinterManager(appContext, null)
            _printerManager!!.defaultPrinter
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
            _printerManager!!.startPrinterSearch()
            val isCancelled: Boolean = _printerManager!!.isCancelled
            TestCase.assertEquals(false, isCancelled)
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsCancelled_StateCancelled() {
        try {
            _printerManager!!.cancelPrinterSearch()
            val isCancelled: Boolean = _printerManager!!.isCancelled
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
            _printerManager!!.startPrinterSearch()
            val isSearching: Boolean = _printerManager!!.isSearching
            TestCase.assertEquals(true, isSearching)
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsSearching_StateNotSearching() {
        try {
            _printerManager!!.cancelPrinterSearch()
            val isSearching: Boolean = _printerManager!!.isSearching
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
            _printerManager!!.setPrintersCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetPrintersCallback_ValidCallback() {
        try {
            _printerManager!!.setPrintersCallback(this)
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
            _printerManager!!.setPrinterSearchCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetPrinterSearchCallback_ValidCallback() {
        try {
            _printerManager!!.setPrinterSearchCallback(this)
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
            _printerManager!!.setUpdateStatusCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSetUpdateStatusCallback_ValidCallback() {
        try {
            _printerManager!!.setUpdateStatusCallback(this)
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
            _printerManager!!.startPrinterSearch()
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
            _printerManager!!.cancelPrinterSearch()
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
            _printerManager!!.createUpdateStatusThread()

            // Create another instance
            _printerManager!!.createUpdateStatusThread()
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
            _printerManager!!.cancelUpdateStatusThread()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testCancelUpdateStatusThread_AfterCreate() {
        try {
            _printerManager!!.createUpdateStatusThread()
            _printerManager!!.cancelUpdateStatusThread()
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
            _printerManager!!.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testUpdateOnlineStatus_NullIpAddress() {
        try {
            _printerManager!!.updateOnlineStatus(null, _imageView)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testUpdateOnlineStatus_ValidParameters() {
        _printerManager!!.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, _imageView)
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
            _printerManager!!.onEndDiscovery(null, -1)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnEndDiscovery_ValidParameters() {
        try {
            _printerManager!!.onEndDiscovery(SNMPManager(), -1)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnEndDiscovery_NullCallback() {
        try {
            _printerManager!!.setPrinterSearchCallback(null)
            _printerManager!!.onEndDiscovery(SNMPManager(), -1)
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
            _printerManager!!.startPrinterSearch()
            _printerManager!!.onFoundDevice(
                SNMPManager(),
                IPV4_ONLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS,
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
            _printerManager!!.onFoundDevice(
                null,
                IPV4_ONLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS,
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
            _printerManager!!.onFoundDevice(
                SNMPManager(),
                null,
                VALID_MAC_ADDRESS,
                "testOnFoundDevice_NullIpAddress",
                BooleanArray(10)
            )
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnFoundDevice_NullMacAddress() {
        try {
            _printerManager!!.onFoundDevice(
                SNMPManager(),
                IPV4_ONLINE_PRINTER_ADDRESS,
                null,
                "testOnFoundDevice_NullMacAddress",
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
            _printerManager!!.onFoundDevice(
                SNMPManager(),
                IPV4_ONLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS,
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
            _printerManager!!.onFoundDevice(
                SNMPManager(), IPV4_ONLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS,
                "testOnFoundDevice_NullCapabilities", null
            )
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnFoundDevice_NullCallback() {
        try {
            _printerManager!!.setPrinterSearchCallback(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }

        try {
            // Trigger Printer Search
            _printerManager!!.startPrinterSearch()
            _printerManager!!.onFoundDevice(
                SNMPManager(),
                IPV4_ONLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS,
                "testOnFoundDevice_ValidParameters",
                BooleanArray(10)
            )
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
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
                IPV4_OFFLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS2
            )
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            } else {
                for (printerItem in _printersList!!) {
                    if (printerItem!!.ipAddress.contentEquals(IPV4_OFFLINE_PRINTER_ADDRESS)) {
                        printer = printerItem
                        break
                    }
                }
            }
            var ret: Boolean = _printerManager!!.removePrinter(printer)
            TestCase.assertEquals(true, ret)
            ret = _printerManager!!.removePrinter(printer)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testRemovePrinter_MultiplePrintersExists() {
        try {
            initialize()

            val printers: List<Printer?>?
            printers = mutableListOf(
                Printer("Printer1", IPV4_ONLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS),
                Printer("Printer2", IPV4_OFFLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS2)
            )

            for ((index, printer) in printers.withIndex()) {
                if (!_printerManager!!.isExists(printer)) {
                    _printerManager!!.savePrinterToDB(printer, true)
                }
                for (printerItem in _printersList!!) {
                    if (printerItem!!.ipAddress.contentEquals(printers[index].ipAddress)) {
                        printers[index].id = printerItem.id
                        break
                    }
                }
            }

            val ret: Boolean = _printerManager!!.removePrinter(printers[0])
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testRemovePrinter_EmptyList() {
        val printer = Printer(
            "testRemovePrinter_ValidAndInvalidPrinter",
            IPV4_OFFLINE_PRINTER_ADDRESS,
            VALID_MAC_ADDRESS2
        )

        for (printerItem in _printersList!!) {
            _printerManager!!.removePrinter(printerItem)
        }

        val ret: Boolean = _printerManager!!.removePrinter(printer)
        TestCase.assertEquals(false, ret)
    }

    @Test
    fun testRemovePrinter_NullPrinter() {
        try {
            _printerManager!!.removePrinter(null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }
/*
    @Test
    fun testRemovePrinter_IpAddressExists() {
        try {
            val printer = Printer("testRemovePrinter_IpAddressExists", IPV4_OFFLINE_PRINTER_ADDRESS)
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            }
            printer.name = IPV4_OFFLINE_PRINTER_ADDRESS
            val ret: Boolean = _printerManager!!.removePrinter(printer)
            TestCase.assertEquals(true, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }
*/
    @Test
    fun testRemovePrinter_NullDatabaseManager() {
        try {
            val printer = Printer("testRemovePrinter_IpAddressExists", IPV4_OFFLINE_PRINTER_ADDRESS,
            VALID_MAC_ADDRESS2)
            _printerManager = PrinterManager(appContext, null)
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            }
            _printerManager!!.removePrinter(printer)
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
                IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2
            )
            for (savedPrinter in _printerManager!!.savedPrintersList) {
                if (printer!!.ipAddress == savedPrinter!!.ipAddress) {
                    printer = savedPrinter
                    break
                }
            }
            _printerManager!!.removePrinter(printer)
            if (_printerManager!!.printerCount == AppConstants.CONST_MAX_PRINTER_COUNT) {
                _printerManager!!.removePrinter(_printerManager!!.savedPrintersList[0])
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
            printer.config!!.isExternalFeederAvailable = true
            printer.config!!.isPunch0Available = true
            val ret = _printerManager!!.savePrinterToDB(printer, true)
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
            printer.config!!.isExternalFeederAvailable = false
            printer.config!!.isPunch0Available = false
            _printerManager!!.removePrinter(printer)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSavePrinterToDB_DisabledCapabilities() {
        try {
            var printer: Printer? = Printer(
                "testSavePrinterToDB_ValidPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2
            )
            for (savedPrinter in _printerManager!!.savedPrintersList) {
                if (printer!!.ipAddress == savedPrinter!!.ipAddress) {
                    printer = savedPrinter
                    break
                }
            }
            _printerManager!!.removePrinter(printer)
            if (_printerManager!!.printerCount == AppConstants.CONST_MAX_PRINTER_COUNT) {
                _printerManager!!.removePrinter(_printerManager!!.savedPrintersList[0])
            }

            //Disabled Capabilities
            printer!!.config!!.isLprAvailable = false
            printer.config!!.isRawAvailable = false
            printer.config!!.isBookletFinishingAvailable = false
            printer.config!!.isStaplerAvailable = false
            printer.config!!.isPunch3Available = true
            printer.config!!.isPunch4Available = false
            printer.config!!.isTrayFaceDownAvailable = false
            printer.config!!.isTrayTopAvailable = false
            printer.config!!.isTrayStackAvailable = false
            printer.config!!.isExternalFeederAvailable = false
            printer.config!!.isPunch0Available = false
            val ret = _printerManager!!.savePrinterToDB(printer, true)
            TestCase.assertEquals(true, ret)

            _printerManager!!.removePrinter(printer)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSavePrinterToDB_ExistingPrinter() {
        try {
            val printer =
                Printer("testSavePrinterToDB_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2)
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            }
            val ret: Boolean = _printerManager!!.savePrinterToDB(printer, true)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSavePrinterToDB_NullPrinter() {
        try {
            _printerManager!!.savePrinterToDB(null, true)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSavePrinterToDB_DefaultPrinter() {
        try {
            val printer = Printer(
                "testSavePrinterToDB_ExistingPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2
            )
            if (_printersList!!.isNotEmpty()) {
                for (i in _printersList!!.size downTo 1) {
                    _printerManager!!.removePrinter(_printersList!![i - 1])
                }
            }
            _printerManager!!.setPrintersCallback(this)
            _printerManager!!.savePrinterToDB(printer, true)
            _printerManager!!.setPrintersCallback(null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSavePrinterToDB_DatabaseError() {
        try {
            val printer = Printer(
                "testSavePrinterToDB_ExistingPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2
            )
            val dbManager = MockedDatabaseManager(
                appContext
            )
            _printerManager = PrinterManager(appContext, dbManager)
            _printerManager!!.savePrinterToDB(printer, true)
            dbManager.setSavePrinterInfoRet(true)
            _printerManager!!.savePrinterToDB(printer, true)
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
            if (_printersList!!.isEmpty()) {
                _printerManager!!.savePrinterToDB(Printer("", IPV4_OFFLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS2), false)
                _printerManager!!.savePrinterToDB(Printer("", IPV4_ONLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS), true)
            }
            _printerManager!!.savedPrintersList
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetSavedPrintersList_EmptyList() {
        try {
            if (_printersList!!.isNotEmpty()) {
                for (i in _printersList!!.size downTo 1) {
                    _printerManager!!.removePrinter(_printersList!![i - 1])
                }
            }
            _printerManager!!.savedPrintersList
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetSavedPrintersList_NullDatabaseManager() {
        try {
            _printerManager = PrinterManager(appContext, null)
            _printerManager!!.savedPrintersList
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
            val printer = Printer("testIsExists_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS,
            VALID_MAC_ADDRESS2)
            if (!_printerManager!!.isExists(printer)) {
                _printerManager!!.savePrinterToDB(printer, true)
            }
            var ret: Boolean = _printerManager!!.isExists(printer)
            TestCase.assertEquals(true, ret)
            ret = _printerManager!!.isExists(printer.ipAddress)
            TestCase.assertEquals(true, ret)
            ret = _printerManager!!.isExists(printer.id)
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
                IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2
            )
            if (_printersList != null) {
                for (savedPrinter in _printersList!!) {
                    if (printer!!.ipAddress == savedPrinter!!.ipAddress) {
                        printer = savedPrinter
                        break
                    }
                }
            }
            if (_printersList!!.isEmpty()) {
                _printerManager!!.savePrinterToDB(Printer("", IPV4_ONLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS), true)
                _printerManager!!.savePrinterToDB(Printer("", IPV6_ONLINE_PRINTER_ADDRESS, VALID_MAC_ADDRESS2), true)
            }
            _printerManager!!.removePrinter(printer)
            var ret: Boolean = _printerManager!!.isExists(printer)
            TestCase.assertEquals(false, ret)
            ret = _printerManager!!.isExists(printer!!.ipAddress)
            TestCase.assertEquals(false, ret)
            ret = _printerManager!!.isExists(printer.id)
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
            val printID: Int = -1
            _printerManager!!.isExists(printer)
            _printerManager!!.isExists(ipAddress)
            _printerManager!!.isExists(printID)
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
            
            ret = _printerManager.isOnline(IPV4_ONLINE_PRINTER_ADDRESS);
            assertEquals(true, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

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
            ipv6Addr = TEST_IPV6_ONLINE_PRINTER_ADDRESS
               // localIpv6Address // If test fails, comment out this line to use actual ipv6
            TestCase.assertNotNull(ipv6Addr)
            while (retry > 0) {
                ret = _printerManager!!.isOnline(ipv6Addr)
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
    */
    @Test
    fun testIsOnline_OfflinePrinter() {
        try {
            val ret: Boolean = _printerManager!!.isOnline(IPV4_OFFLINE_PRINTER_ADDRESS)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsOnline_NullAddress() {
        try {
            val ret: Boolean = _printerManager!!.isOnline(null)
            TestCase.assertEquals(false, ret)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testIsOnline_InvalidAddress() {
        try {
            val ret: Boolean = _printerManager!!.isOnline(INVALID_ADDRESS)
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
            _printerManager!!.searchPrinter(IPV4_ONLINE_PRINTER_ADDRESS)
            _signal.await(_timeout.toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testSearchPrinter_NullIpAddress() {
        try {
            _printerManager!!.searchPrinter(null)
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
        _printerManager!!.UpdateOnlineStatusTask(null, "")
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
        _printerManager!!.UpdateOnlineStatusTask(_imageView, IPV4_OFFLINE_PRINTER_ADDRESS)
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
        _printerManager!!.UpdateOnlineStatusTask(_imageView, IPV4_ONLINE_PRINTER_ADDRESS)
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
        ipv6Addr = localIpv6Address
        TestCase.assertNotNull(ipv6Addr)
        _printerManager!!.UpdateOnlineStatusTask(_imageView, ipv6Addr!!)
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
            _printerManager!!.getIdFromCursor(null, null)
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
            _printerManager!!.getIdFromCursor(cursor, null)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    @Test
    fun testGetIdFromCursor_ValidParameters() {
        try {
            val printer =
                Printer("testGetIdFromCursor_ValidParameters", IPV4_ONLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS)
            val dbManager = DatabaseManager(appContext)
            val cursor = dbManager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE, null,
                KeyConstants.KEY_SQL_PRINTER_IP + "=?", arrayOf(IPV4_ONLINE_PRINTER_ADDRESS),
                null, null, null
            )
            _printerManager!!.getIdFromCursor(cursor, printer)
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
            setupPrinterConfig(Printer("test", "ip", "mac"), null)
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
        val target = Printer("test GL", "ip", "mac")
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
        val target = Printer("test GL", "ip", "mac")
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
        val target = Printer("test", "ip", "mac")
        try {
            setupPrinterConfig(target, capabilities)
        } catch (e: Exception) {
            // No exception should happen
            TestCase.fail()
        }
        val defaultPrinter = Printer("test", "ip", "mac")
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
            if (_printersList != null && _printersList!!.isNotEmpty()) {
                printer = _printersList!![0]
            }
            if (printer == null) {
                printer = Printer("testUpdatePortSettings", IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2)
                _printerManager!!.savePrinterToDB(printer, true)
            }
            val dbManager = DatabaseManager(appContext)
            var ret: Boolean = _printerManager!!.updatePortSettings(printer.id, PortSetting.LPR)
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
            ret = _printerManager!!.updatePortSettings(printer.id, PortSetting.RAW)
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
            if (_printersList != null && _printersList!!.isNotEmpty()) {
                printer = _printersList!![0]
            }
            if (printer == null) {
                printer = Printer("testUpdatePortSettings", IPV4_OFFLINE_PRINTER_ADDRESS,
                VALID_MAC_ADDRESS2)
                _printerManager!!.savePrinterToDB(printer, true)
            }
            val ret = _printerManager!!.updatePortSettings(printer.id, null)
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
            var printer = Printer("RISO IS1000C-G", "192.168.0.1", "08:00:27:93:79:AA")
            _printerManager!!.savePrinterToDB(printer, true)
            var printerType = _printerManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_IS)
            _printerManager!!.removePrinter(printer)
            printer = Printer("ORPHIS GD500", "192.168.0.2", "08:00:27:93:79:AB")
            _printerManager!!.savePrinterToDB(printer, true)
            printerType = _printerManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_GD)
            _printerManager!!.removePrinter(printer)
            printer = Printer("ORPHIS FW1000", "192.168.0.3", "08:00:27:93:79:AC")
            _printerManager!!.savePrinterToDB(printer, true)
            printerType = _printerManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_FW)
            _printerManager!!.removePrinter(printer)
            printer = Printer("ORPHIS FT100", "192.168.0.4", "08:00:27:93:79:AD")
            _printerManager!!.savePrinterToDB(printer, true)
            printerType = _printerManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
            _printerManager!!.removePrinter(printer)
            printer = Printer("ORPHIS GL200", "192.168.0.5", "08:00:27:93:79:AE")
            _printerManager!!.savePrinterToDB(printer, true)
            printerType = _printerManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_GL)
            _printerManager!!.removePrinter(printer)
            printer = Printer("RISO CEREZONA S200", "192.168.0.4", "08:00:27:93:79:AF")
            _printerManager!!.savePrinterToDB(printer, true)
            printerType = _printerManager!!.getPrinterType(printer.id)
            TestCase.assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
            _printerManager!!.removePrinter(printer)
            var nonExistentId = 2
            if (_printerManager!!.printerCount > 0) {
                nonExistentId += _printerManager!!.savedPrintersList[_printerManager!!.printerCount - 1]!!.id
            }
            printerType = _printerManager!!.getPrinterType(nonExistentId)
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
        private var _insert = false
        fun setSavePrinterInfoRet(ret: Boolean) {
            _insert = ret
        }

        override fun insert(
            table: String,
            nullColumnHack: String?,
            values: ContentValues?
        ): Boolean {
            return _insert
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
        _printerManager = PrinterManager(appContext, null)
        TestCase.assertNotNull(_printerManager)
        _printerManager!!.setPrinterSearchCallback(this)
        _printerManager!!.setPrintersCallback(this)
        _printerManager!!.setUpdateStatusCallback(this)
        _printersList = _printerManager!!.savedPrintersList
        TestCase.assertNotNull(_printersList)
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
            } catch (_: SocketException) {
            }
            return null
        }

    companion object {
        private const val IPV4_ONLINE_PRINTER_ADDRESS = "192.168.1.206"
        private const val IPV6_ONLINE_PRINTER_ADDRESS = "fe80::a00:27ff:fe95:7387"
        private const val IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.206"
        private const val INVALID_ADDRESS = "invalid"
        private const val VALID_MAC_ADDRESS = "08:00:27:93:79:5D"
        private const val VALID_MAC_ADDRESS2 = "08:00:27:93:79:5E"
    }
}