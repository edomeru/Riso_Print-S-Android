
package jp.co.riso.smartdeviceapp.controller.printer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.SNMPManager;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.UpdateStatusCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting;
import jp.co.riso.smartdeviceapp.view.MainActivity;

public class PrinterManagerTest extends ActivityInstrumentationTestCase2<MainActivity> implements
        UpdateStatusCallback, PrintersCallback, PrinterSearchCallback {
    final CountDownLatch mSignal = new CountDownLatch(1);
    final int TIMEOUT = 20;
    private static final String IPV4_ONLINE_PRINTER_ADDRESS = "192.168.1.206";
    private static final String IPV6_ONLINE_PRINTER_ADDRESS = "fe80::2a0:deff:fe69:7fb2";
    private static final String IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.206";
    private static final String INVALID_ADDRESS = "invalid";
    
    private ImageView mImageView = null;

    private PrinterManager mPrinterManager = null;
    private List<Printer> mPrintersList = null;
    
    public PrinterManagerTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        initialize();
        mImageView = new ImageView(getActivity());        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        mPrinterManager = null;
    }

    // ================================================================================
    // Tests - getInstance
    // ================================================================================

    public void testgetInstance() {
        try {
            mPrinterManager = null;
            mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
            assertNotNull(mPrinterManager);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    // ================================================================================
    // Tests - getPrinterCount
    // ================================================================================

    public void testGetPrinterCount_DuringIdle() {
        try {
            
            int count;
            count = mPrinterManager.getPrinterCount();
            assertEquals(false, count < 0);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - clearDefaultPrinter
    // ================================================================================

    public void testClearDefaultPrinter() {
        try {

            mPrinterManager.clearDefaultPrinter();            
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testClearDefaultPrinter_NullDatabaseManager() {
        try {
            mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(), null);
            mPrinterManager.clearDefaultPrinter();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    // ================================================================================
    // Tests - setDefaultPrinter
    // ================================================================================

    public void testSetDefaultPrinter_NoDefaultPrinter() {
        try {
            Printer printer = null;
            
            if (mPrintersList != null && !mPrintersList.isEmpty()) {
                printer = mPrintersList.get(0);
            }
            if(printer == null) {
                printer = new Printer("testSetDefaultPrinter_NoDefaultPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
                mPrinterManager.savePrinterToDB(printer, true);
            }
            
            testClearDefaultPrinter();
            assertTrue(mPrinterManager.setDefaultPrinter(printer));
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetDefaultPrinter_WithDefaultPrinter() {
        try {
            Printer printer = null;

            if (mPrintersList != null && !mPrintersList.isEmpty()) {
                printer = mPrintersList.get(0);
            }
            if(printer == null) {
                printer = new Printer("testSetDefaultPrinter_WithDefaultPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
                mPrinterManager.savePrinterToDB(printer, true);
            }
            testSetDefaultPrinter_NoDefaultPrinter();
            assertTrue(mPrinterManager.setDefaultPrinter(printer));
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetDefaultPrinter_NullPrinter() {
        try {
            
            assertFalse(mPrinterManager.setDefaultPrinter(null));
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetDefaultPrinter_NullDatabaseManager() {
        try {
            Printer printer = null;

            mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(), null);
            assertFalse(mPrinterManager.setDefaultPrinter(printer));
            
            if (mPrintersList != null && !mPrintersList.isEmpty()) {
                printer = mPrintersList.get(0);
            }
            if(printer == null) {
                printer = new Printer("testSetDefaultPrinter_WithDefaultPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
                mPrinterManager.savePrinterToDB(printer, true);
            }
            assertTrue(mPrinterManager.setDefaultPrinter(printer));
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testSetDefaultPrinter_DatabaseError() {
        Printer printer = new Printer("", IPV4_OFFLINE_PRINTER_ADDRESS);
        MockedDatabaseManager dbManager = new MockedDatabaseManager(
                SmartDeviceApp.getAppContext());

        mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(), dbManager);
        assertFalse(mPrinterManager.setDefaultPrinter(printer));
    }
    
    // ================================================================================
    // Tests - getDefaultPrinter
    // ================================================================================

    public void testGetDefaultPrinter_NoDefaultPrinter() {
        try {
            int defaultPrinter;
            testClearDefaultPrinter();

            defaultPrinter = mPrinterManager.getDefaultPrinter();
            assertEquals(false, defaultPrinter != PrinterManager.EMPTY_ID);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testGetDefaultPrinter_WithDefaultPrinter() {
        try {
            int defaultPrinter;
            testSetDefaultPrinter_NoDefaultPrinter();

            defaultPrinter = mPrinterManager.getDefaultPrinter();
            assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testGetDefaultPrinter_WithDefaultPrinterActivityRestarted() {
        try {
            mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
            int defaultPrinter;

            defaultPrinter = mPrinterManager.getDefaultPrinter();
            assertEquals(true, defaultPrinter != PrinterManager.EMPTY_ID);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testGetDefaultPrinter_NullDatabaseManager() {
        try {
            mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(), null);
            mPrinterManager.getDefaultPrinter();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    // ================================================================================
    // Tests - isCancelled
    // ================================================================================

    public void testIsCancelled_StateNotCancelled() {
        try {
            boolean isCancelled;
            mPrinterManager.startPrinterSearch();

            isCancelled = mPrinterManager.isCancelled();
            assertEquals(false, isCancelled);
            
            mSignal.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testIsCancelled_StateCancelled() {
        try {
            boolean isCancelled;
            mPrinterManager.cancelPrinterSearch();

            isCancelled = mPrinterManager.isCancelled();
            assertEquals(true, isCancelled);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isSearching
    // ================================================================================

    public void testIsSearching_StateSearching() {
        try {
            boolean isSearching;
            mPrinterManager.startPrinterSearch();

            isSearching = mPrinterManager.isSearching();
            assertEquals(true, isSearching);
            
            mSignal.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testIsSearching_StateNotSearching() {
        try {
            boolean isSearching;
            mPrinterManager.cancelPrinterSearch();
            isSearching = mPrinterManager.isSearching();
            
            assertEquals(false, isSearching);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setPrintersCallback
    // ================================================================================

    public void testSetPrintersCallback_NullCallback() {
        try {
            
            mPrinterManager.setPrintersCallback(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetPrintersCallback_ValidCallback() {
        try {
            
            mPrinterManager.setPrintersCallback(this);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setPrinterSearchCallback
    // ================================================================================

    public void testSetPrinterSearchCallback_NullCallback() {
        try {
            
            mPrinterManager.setPrinterSearchCallback(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetPrinterSearchCallback_ValidCallback() {
        try {
            
            mPrinterManager.setPrinterSearchCallback(this);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setUpdateStatusCallback
    // ================================================================================

    public void testSetUpdateStatusCallback_NullCallback() {
        try {
            
            mPrinterManager.setUpdateStatusCallback(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetUpdateStatusCallback_ValidCallback() {
        try {
            
            mPrinterManager.setUpdateStatusCallback(this);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - startPrinterSearch
    // ================================================================================

    public void testStartPrinterSearch() {
        try {

            mPrinterManager.startPrinterSearch();
            
            mSignal.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancelPrinterSearch
    // ================================================================================

    public void testCancelPrinterSearch() {
        try {

            mPrinterManager.cancelPrinterSearch();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - createUpdateStatusThread
    // ================================================================================

    public void testCreateUpdateStatusThread() {
        try {

            mPrinterManager.createUpdateStatusThread();
            
            // Create another instance
            mPrinterManager.createUpdateStatusThread();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancelUpdateStatusThread
    // ================================================================================

    public void testCancelUpdateStatusThread_DuringIdle() {
        try {

            mPrinterManager.cancelUpdateStatusThread();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testCancelUpdateStatusThread_AfterCreate() {
        try {

            mPrinterManager.createUpdateStatusThread();
            mPrinterManager.cancelUpdateStatusThread();

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    // ================================================================================
    // Tests - updateOnlineStatus
    // ================================================================================

    public void testUpdateOnlineStatus_NullImageView() {
        try {

            mPrinterManager.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testUpdateOnlineStatus_NullIpAddress() {
        try {

            mPrinterManager.updateOnlineStatus(null, mImageView);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testUpdateOnlineStatus_ValidParameters() {

        mPrinterManager.updateOnlineStatus(IPV4_ONLINE_PRINTER_ADDRESS, mImageView);
        try {
            // Wait and Check if Address is ONLINE
            getInstrumentation().waitForIdleSync();
            Thread.sleep(10000);
            getInstrumentation().waitForIdleSync();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    // ================================================================================
    // Tests - onEndDiscovery
    // ================================================================================

    public void testOnEndDiscovery_NullManager() {
        try {

            mPrinterManager.onEndDiscovery(null, -1);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnEndDiscovery_ValidParameters() {
        try {

            mPrinterManager.onEndDiscovery(new SNMPManager(), -1);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }
    // ================================================================================
    // Tests - onFoundDevice
    // ================================================================================
    
    public void testOnFoundDevice_ValidParameters() {
        try {

            // Trigger Printer Search
            mPrinterManager.startPrinterSearch();
            mPrinterManager.onFoundDevice(new SNMPManager(), IPV4_ONLINE_PRINTER_ADDRESS, "testOnFoundDevice_ValidParameters",
                    new boolean[10]);
            
            mSignal.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testOnFoundDevice_NullManager() {
        try {

            mPrinterManager.onFoundDevice(null, IPV4_ONLINE_PRINTER_ADDRESS, "testOnFoundDevice_NullManager",
                    new boolean[10]);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnFoundDevice_NullIpAddress() {
        try {

            mPrinterManager.onFoundDevice(new SNMPManager(), null, "testOnFoundDevice_NullIpAddress",
                    new boolean[10]);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnFoundDevice_NullName() {
        try {
            initialize();

            mPrinterManager.onFoundDevice(new SNMPManager(), IPV4_ONLINE_PRINTER_ADDRESS, null,
                    new boolean[10]);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnFoundDevice_NullCapabilities() {
        try {
            initialize();

            mPrinterManager.onFoundDevice(new SNMPManager(), IPV4_ONLINE_PRINTER_ADDRESS,
                    "testOnFoundDevice_NullCapabilities", null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - removePrinter
    // ================================================================================

    public void testRemovePrinter_ValidAndInvalidPrinter() {
        try {
            initialize();

            Printer printer = new Printer("testRemovePrinter_ValidAndInvalidPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret;

            if (!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer, true);
            } else {
                for (Printer printerItem : mPrintersList) {
                    if (printerItem.getIpAddress().contentEquals(IPV4_OFFLINE_PRINTER_ADDRESS)) {
                        printer = printerItem;
                        break;
                    }
                }
            }
            ret = mPrinterManager.removePrinter(printer);
            assertEquals(true, ret);

            ret = mPrinterManager.removePrinter(printer);
            assertEquals(false, ret);

        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testRemovePrinter_EmptyList() {

        Printer printer = new Printer("testRemovePrinter_ValidAndInvalidPrinter",
                IPV4_OFFLINE_PRINTER_ADDRESS);
        boolean ret;

        for (Printer printerItem : mPrintersList) {
            mPrinterManager.removePrinter(printerItem);
        }
        ret = mPrinterManager.removePrinter(printer);
        assertEquals(false, ret);
    }
    
    public void testRemovePrinter_NullPrinter() {
        try {

            mPrinterManager.removePrinter(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testRemovePrinter_IpAddressExists() {
        try {

            Printer printer = new Printer("testRemovePrinter_IpAddressExists", IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret;

            if (!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer, true);
            }

            printer.setName(IPV4_OFFLINE_PRINTER_ADDRESS);
            ret = mPrinterManager.removePrinter(printer);
            assertEquals(true, ret);

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testRemovePrinter_NullDatabaseManager() {
        try {
            Printer printer = new Printer("testRemovePrinter_IpAddressExists", IPV4_OFFLINE_PRINTER_ADDRESS);
            mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(), null);

            if (!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer, true);
            }

            mPrinterManager.removePrinter(printer);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - savePrinterToDB
    // ================================================================================

    public void testSavePrinterToDB_ValidPrinter() {
        try {
            Printer printer = new Printer("testSavePrinterToDB_ValidPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS);

            for (Printer savedPrinter : mPrinterManager.getSavedPrintersList()) {
                if (printer.getIpAddress().equals(savedPrinter.getIpAddress())) {
                    printer = savedPrinter;
                    break;
                }
            }
            mPrinterManager.removePrinter(printer);

            if (mPrinterManager.getPrinterCount() == AppConstants.CONST_MAX_PRINTER_COUNT) {
                mPrinterManager.removePrinter(mPrinterManager.getSavedPrintersList().get(0));
            }
            
            //Enabled Capabilities
            printer.getConfig().setLprAvailable(true);
            printer.getConfig().setRawAvailable(true);
            printer.getConfig().setBookletFinishingAvailable(true);
            printer.getConfig().setStaplerAvailable(true);
            printer.getConfig().setPunch3Available(true);
            printer.getConfig().setPunch4Available(true);
            printer.getConfig().setTrayFaceDownAvailable(true);
            printer.getConfig().setTrayTopAvailable(true);
            printer.getConfig().setTrayStackAvailable(true);
            
            boolean ret = mPrinterManager.savePrinterToDB(printer, true);
            assertEquals(true, ret);
            
            //Disabled Capabilities
            printer.getConfig().setLprAvailable(false);
            printer.getConfig().setRawAvailable(false);
            printer.getConfig().setBookletFinishingAvailable(false);
            printer.getConfig().setStaplerAvailable(false);
            printer.getConfig().setPunch3Available(true);
            printer.getConfig().setPunch4Available(false);
            printer.getConfig().setTrayFaceDownAvailable(false);
            printer.getConfig().setTrayTopAvailable(false);
            printer.getConfig().setTrayStackAvailable(false);
            
            mPrinterManager.removePrinter(printer);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSavePrinterToDB_ExistingPrinter() {
        try {
            Printer printer = new Printer("testSavePrinterToDB_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret;

            if (!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer, true);
            }
            
            ret = mPrinterManager.savePrinterToDB(printer, true);
            assertEquals(false, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSavePrinterToDB_NullPrinter() {
        try {

            mPrinterManager.savePrinterToDB(null, true);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testSavePrinterToDB_DefaultPrinter() {
        try {

            Printer printer = new Printer("testSavePrinterToDB_ExistingPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS);
            if (!mPrintersList.isEmpty()) {
                for (int i = mPrintersList.size(); i > 0; i--) {
                    mPrinterManager.removePrinter(mPrintersList.get(i - 1));
                }
            }
            mPrinterManager.setPrintersCallback(this);
            mPrinterManager.savePrinterToDB(printer, true);
            mPrinterManager.setPrintersCallback(null);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSavePrinterToDB_DatabaseError() {
        try {
            Printer printer = new Printer("testSavePrinterToDB_ExistingPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS);
            MockedDatabaseManager dbManager = new MockedDatabaseManager(
                    SmartDeviceApp.getAppContext());
            
            mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(), dbManager);

            mPrinterManager.savePrinterToDB(printer, true);
            dbManager.setSavePrinterInfoRet(true);
            mPrinterManager.savePrinterToDB(printer, true);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }
    
    // ================================================================================
    // Tests - getSavedPrintersList
    // ================================================================================
   
    public void testGetSavedPrintersList_NonEmptyList() {

        try {

            if (mPrintersList.isEmpty()) {
                mPrinterManager.savePrinterToDB(new Printer("", IPV4_OFFLINE_PRINTER_ADDRESS), false);
                mPrinterManager.savePrinterToDB(new Printer("", IPV4_ONLINE_PRINTER_ADDRESS), true);
            }
            mPrinterManager.getSavedPrintersList();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testGetSavedPrintersList_EmptyList() {

        try {
            
            if (!mPrintersList.isEmpty()) {
                for (int i = mPrintersList.size(); i > 0; i--) {
                    mPrinterManager.removePrinter(mPrintersList.get(i - 1));
                }
            }
            mPrinterManager.getSavedPrintersList();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testGetSavedPrintersList_NullDatabaseManager() {
        try {
            mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(), null);
            mPrinterManager.getSavedPrintersList();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isExists
    // ================================================================================

    public void testIsExists_ExistingPrinter() {
        try {
            Printer printer = new Printer("testIsExists_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret;

            if(!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer, true);
            }
            
            ret = mPrinterManager.isExists(printer);
            assertEquals(true, ret);

            ret = mPrinterManager.isExists(printer.getIpAddress());
            assertEquals(true, ret);

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testIsExists_NotExistingPrinter() {
        try {
            Printer printer = new Printer("testIsExists_NotExistingPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret;
            if (mPrintersList != null) {
                for (Printer savedPrinter : mPrintersList) {
                    if (printer.getIpAddress().equals(savedPrinter.getIpAddress())) {
                        printer = savedPrinter;
                        break;
                    }
                }
            }
            if(mPrintersList.isEmpty()) {
                mPrinterManager.savePrinterToDB(new Printer("", IPV4_ONLINE_PRINTER_ADDRESS), true);
                mPrinterManager.savePrinterToDB(new Printer("", IPV6_ONLINE_PRINTER_ADDRESS), true);
            }
            mPrinterManager.removePrinter(printer);

            ret = mPrinterManager.isExists(printer);
            assertEquals(false, ret);

            ret = mPrinterManager.isExists(printer.getIpAddress());
            assertEquals(false, ret);

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testIsExists_NullPrinter() {
        try {
            Printer printer = null;
            String ipAddress = null;
            mPrinterManager.isExists(printer);
            mPrinterManager.isExists(ipAddress);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
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

    public void testIsOnline_OnlineIpv6Printer() {
        try {
            boolean ret = false;
            int retry =10;
            initialize();
            String ipv6Addr = IPV6_ONLINE_PRINTER_ADDRESS;
            
            // Ipv6 Address
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ipv6Addr = getLocalIpv6Address();
            }
            assertNotNull(ipv6Addr);

            while (retry > 0) {
                ret = mPrinterManager.isOnline(ipv6Addr);
                if (ret) {
                    break;
                }
                mSignal.await(1, TimeUnit.SECONDS);
                retry--;
            }
            assertEquals(true, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testIsOnline_OfflinePrinter() {
        try {
            boolean ret;

            ret = mPrinterManager.isOnline(IPV4_OFFLINE_PRINTER_ADDRESS);
            assertEquals(false, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testIsOnline_NullAddress() {
        try {
            boolean ret;

            ret = mPrinterManager.isOnline(null);
            assertEquals(false, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testIsOnline_InvalidAddress() {
        try {
            boolean ret;

            ret = mPrinterManager.isOnline(INVALID_ADDRESS);
            assertEquals(false, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - searchPrinter
    // ================================================================================

    public void testSearchPrinter_ValidIpAddress() {
        try {

            mPrinterManager.searchPrinter(IPV4_ONLINE_PRINTER_ADDRESS);    
            
            mSignal.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testSearchPrinter_NullIpAddress() {
        try {

            mPrinterManager.searchPrinter(null);
            
            mSignal.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    // ================================================================================
    // Tests - UpdateOnlineStatusTask
    // ================================================================================

    public void testUpdateOnlineStatusTask_EmptyIpAddress() {

        mPrinterManager.new UpdateOnlineStatusTask(null, "")
                .execute();
        
        try {
            // Wait and Check if Address is OFFLINE
            getInstrumentation().waitForIdleSync();
            Thread.sleep(1000);
            getInstrumentation().waitForIdleSync();

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testUpdateOnlineStatusTask_ValidOfflineParameters() {

        mPrinterManager.new UpdateOnlineStatusTask(mImageView, IPV4_OFFLINE_PRINTER_ADDRESS)
                .execute();
        try {
            // Wait and Check if Address is OFFLINE
            getInstrumentation().waitForIdleSync();
            Thread.sleep(10000);
            getInstrumentation().waitForIdleSync();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testUpdateOnlineStatusTask_ValidIpv4OnlineParameters() {

        mPrinterManager.new UpdateOnlineStatusTask(mImageView, IPV4_ONLINE_PRINTER_ADDRESS)
                .execute();
        try {
            // Wait and Check if Address is ONLINE
            getInstrumentation().waitForIdleSync();
            Thread.sleep(10000);
            getInstrumentation().waitForIdleSync();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testUpdateOnlineStatusTask_ValidIpv6OnlineParameters() {
        String ipv6Addr = IPV6_ONLINE_PRINTER_ADDRESS;
        
        // Ipv6 Address
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ipv6Addr = getLocalIpv6Address();
        }
        assertNotNull(ipv6Addr);
        mPrinterManager.new UpdateOnlineStatusTask(mImageView, ipv6Addr)
                .execute();
        try {
            // Wait and Check if Address is ONLINE
            getInstrumentation().waitForIdleSync();
            Thread.sleep(10000);
            getInstrumentation().waitForIdleSync();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    // ================================================================================
    // Tests - UpdateOnlineStatusTask
    // ================================================================================

    public void testGetIdFromCursor_NullCursor() {

        try {
            mPrinterManager.getIdFromCursor(null, null);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testGetIdFromCursor_NullPrinter() {
        try {
            DatabaseManager dbManager = new DatabaseManager(SmartDeviceApp.getAppContext());
            Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null,
                    KeyConstants.KEY_SQL_PRINTER_IP + "=?", new String[] {IPV4_ONLINE_PRINTER_ADDRESS}, 
                    null, null, null);
            mPrinterManager.getIdFromCursor(cursor, null);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
     
    public void testGetIdFromCursor_ValidParameters() {
        try {
            Printer printer = new Printer("testGetIdFromCursor_ValidParameters", IPV4_ONLINE_PRINTER_ADDRESS);            
            DatabaseManager dbManager = new DatabaseManager(SmartDeviceApp.getAppContext());
            Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null,
                    KeyConstants.KEY_SQL_PRINTER_IP + "=?", new String[] {IPV4_ONLINE_PRINTER_ADDRESS}, 
                    null, null, null);
            
            mPrinterManager.getIdFromCursor(cursor, printer);
            
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setupPrinterConfig
    // ================================================================================

    public void testSetupPrinterConfig_null() {
        try {
            PrinterManager.setupPrinterConfig(null, null);
            PrinterManager.setupPrinterConfig(null, new boolean[1]);
            PrinterManager.setupPrinterConfig(new Printer("test", "ip"), null);
        } catch (Exception e) {
            // No exception should happen
            fail();
        }
    }
    public void testSetupPrinterConfig_allAvailable() {
        boolean capabilities[] = new boolean[] {
                true, true, true, true, true, true, true, true, true, true, true
        };
        Printer target = new Printer("test GL", "ip");
        PrinterManager.setupPrinterConfig(target, capabilities);
        
        assertTrue(target.getConfig().isBookletFinishingAvailable());
        assertTrue(target.getConfig().isStaplerAvailable());
        assertTrue(target.getConfig().isPunch3Available());
        assertTrue(target.getConfig().isPunch4Available());
        assertTrue(target.getConfig().isPunch0Available());
        assertTrue(target.getConfig().isExternalFeederAvailable());
        assertTrue(target.getConfig().isTrayFaceDownAvailable());
        assertTrue(target.getConfig().isTrayStackAvailable());
        assertTrue(target.getConfig().isTrayTopAvailable());
        assertTrue(target.getConfig().isLprAvailable());
        assertTrue(target.getConfig().isRawAvailable());
    }
    
    public void testSetupPrinterConfig_allFalse() {
        boolean capabilities[] = new boolean[] {
                false, false, false, false, false, false, false, false, false, false, false
        };
        Printer target = new Printer("test GL", "ip");
        PrinterManager.setupPrinterConfig(target, capabilities);
        
        assertFalse(target.getConfig().isBookletFinishingAvailable());
        assertFalse(target.getConfig().isStaplerAvailable());
        assertFalse(target.getConfig().isPunch3Available());
        assertFalse(target.getConfig().isPunch4Available());
        assertFalse(target.getConfig().isPunch0Available());
        assertFalse(target.getConfig().isExternalFeederAvailable());
        assertFalse(target.getConfig().isTrayFaceDownAvailable());
        assertFalse(target.getConfig().isTrayStackAvailable());
        assertFalse(target.getConfig().isTrayTopAvailable());
        assertFalse(target.getConfig().isLprAvailable());
        assertFalse(target.getConfig().isRawAvailable());
    }
    
    public void testSetupPrinterConfig_Incomplete() {
        boolean capabilities[] = new boolean[] {
                false
        };
        Printer target = new Printer("test", "ip");
        try {
            PrinterManager.setupPrinterConfig(target, capabilities);
        } catch (Exception e) {
            // No exception should happen
            fail();
        }

        Printer defaultPrinter = new Printer("test", "ip");
        
        assertFalse(target.getConfig().isBookletFinishingAvailable());
        assertEquals(defaultPrinter.getConfig().isStaplerAvailable(), target.getConfig().isStaplerAvailable());
        assertEquals(defaultPrinter.getConfig().isPunch3Available(), target.getConfig().isPunch3Available());
        assertEquals(defaultPrinter.getConfig().isPunch4Available(), target.getConfig().isPunch4Available());
        assertEquals(defaultPrinter.getConfig().isTrayFaceDownAvailable(), target.getConfig().isTrayFaceDownAvailable());
        assertEquals(defaultPrinter.getConfig().isTrayStackAvailable(), target.getConfig().isTrayStackAvailable());
        assertEquals(defaultPrinter.getConfig().isTrayTopAvailable(), target.getConfig().isTrayTopAvailable());
        assertEquals(defaultPrinter.getConfig().isLprAvailable(), target.getConfig().isLprAvailable());
        assertEquals(defaultPrinter.getConfig().isRawAvailable(), target.getConfig().isRawAvailable());
    }
    
    // ================================================================================
    // Tests - updatePortSettings
    // ================================================================================

    public void testUpdatePortSettings() {

        try {
            Printer printer = null;
            boolean ret;
            
            if (mPrintersList != null && !mPrintersList.isEmpty()) {
                printer = mPrintersList.get(0);
            }
            if (printer == null) {
                printer = new Printer("testUpdatePortSettings", IPV4_OFFLINE_PRINTER_ADDRESS);
                mPrinterManager.savePrinterToDB(printer, true);
            }
            
            DatabaseManager dbManager = new DatabaseManager(SmartDeviceApp.getAppContext());
            Cursor cursor;
            
            ret = mPrinterManager.updatePortSettings(printer.getId(), PortSetting.LPR);
            assertTrue(ret);
            
            cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, new String[] {KeyConstants.KEY_SQL_PRINTER_PORT},
                    KeyConstants.KEY_SQL_PRINTER_ID + "=?", new String[] {"" + printer.getId()}, 
                    null, null, null);
            if (cursor != null && cursor.moveToFirst() && printer != null) {
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_PORT)));
                cursor.close();
            }
            
            ret = mPrinterManager.updatePortSettings(printer.getId(), PortSetting.RAW);
            assertTrue(ret);
            
            cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, new String[] {KeyConstants.KEY_SQL_PRINTER_PORT},
                    KeyConstants.KEY_SQL_PRINTER_ID + "=?", new String[] {"" + printer.getId()}, 
                    null, null, null);
            if (cursor != null && cursor.moveToFirst() && printer != null) {
                assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_PORT)));
                cursor.close();
            }
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testUpdatePortSettings_NullPortSetting() {

        try {
            Printer printer = null;

            if (mPrintersList != null && !mPrintersList.isEmpty()) {
                printer = mPrintersList.get(0);
            }
            if (printer == null) {
                printer = new Printer("testUpdatePortSettings", IPV4_OFFLINE_PRINTER_ADDRESS);
                mPrinterManager.savePrinterToDB(printer, true);
            }

            boolean ret = mPrinterManager.updatePortSettings(printer.getId(), null);
            assertFalse(ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getPrinterType
    // ================================================================================
    public void testGetPrinterType() {

        try {
            Printer printer;

            printer = new Printer("RISO IS1000C-G", "192.168.0.1");
            mPrinterManager.savePrinterToDB(printer, true);
            String printerType =  mPrinterManager.getPrinterType(printer.getId());
            assertEquals(printerType, AppConstants.PRINTER_MODEL_IS);
            mPrinterManager.removePrinter(printer);

            printer = new Printer("ORPHIS GD500", "192.168.0.2");
            mPrinterManager.savePrinterToDB(printer, true);
            printerType =  mPrinterManager.getPrinterType(printer.getId());
            assertEquals(printerType, AppConstants.PRINTER_MODEL_GD);
            mPrinterManager.removePrinter(printer);

            printer = new Printer("ORPHIS FW1000", "192.168.0.3");
            mPrinterManager.savePrinterToDB(printer, true);
            printerType =  mPrinterManager.getPrinterType(printer.getId());
            assertEquals(printerType, AppConstants.PRINTER_MODEL_FW);
            mPrinterManager.removePrinter(printer);

            printer = new Printer("ORPHIS FT100", "192.168.0.4");
            mPrinterManager.savePrinterToDB(printer, true);
            printerType =  mPrinterManager.getPrinterType(printer.getId());
            assertEquals(printerType, AppConstants.PRINTER_MODEL_FT);
            mPrinterManager.removePrinter(printer);

            printer = new Printer("ORPHIS GL200", "192.168.0.5");
            mPrinterManager.savePrinterToDB(printer, true);
            printerType =  mPrinterManager.getPrinterType(printer.getId());
            assertEquals(printerType, AppConstants.PRINTER_MODEL_GL);
            mPrinterManager.removePrinter(printer);

            printer = new Printer("RISO CEREZONA S200", "192.168.0.4");
            mPrinterManager.savePrinterToDB(printer, true);
            printerType =  mPrinterManager.getPrinterType(printer.getId());
            assertEquals(printerType, AppConstants.PRINTER_MODEL_FT);
            mPrinterManager.removePrinter(printer);

            int nonExistentId = 2;
            if(mPrinterManager.getPrinterCount() > 0) {
                nonExistentId += mPrinterManager.getSavedPrintersList().get(mPrinterManager.getPrinterCount() - 1).getId();
            }

            printerType =  mPrinterManager.getPrinterType(nonExistentId);
            assertNull(printerType);

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }


    // ================================================================================
    // Interface
    // ================================================================================

    @Override
    public void updateOnlineStatus() {
    }

    @Override
    public void onAddedNewPrinter(Printer printer, boolean isOnline) {
    }

    @Override
    public void onPrinterAdd(Printer printer) {
    }

    @Override
    public void onSearchEnd() {
        mSignal.countDown();
    }
    
    public static class MockedDatabaseManager extends DatabaseManager {
        private boolean mInsert = false;
        
        public MockedDatabaseManager(Context context) {
            super(context);
        }

        public void setSavePrinterInfoRet(boolean ret){
            mInsert = ret;
        }
        
        @Override
        public boolean insert(String table, String nullColumnHack, ContentValues values) {
            return mInsert;
        }
        
        @Override
        public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
            return null;
        }
    }
    
    // ================================================================================
    // Private
    // ================================================================================
    
    private void initialize() {
        mPrinterManager = new PrinterManager(SmartDeviceApp.getAppContext(),null);
        assertNotNull(mPrinterManager);

        mPrinterManager.setPrinterSearchCallback(this);
        mPrinterManager.setPrintersCallback(this);
        mPrinterManager.setUpdateStatusCallback(this);

        mPrintersList = mPrinterManager.getSavedPrintersList();
        assertNotNull(mPrintersList);
    }

    private String getLocalIpv6Address() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }
}
