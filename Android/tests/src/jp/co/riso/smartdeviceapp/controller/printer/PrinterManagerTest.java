
package jp.co.riso.smartdeviceapp.controller.printer;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.SNMPManager;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.UpdateStatusCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;

public class PrinterManagerTest extends ActivityInstrumentationTestCase2<MainActivity> implements
        UpdateStatusCallback, PrintersCallback, PrinterSearchCallback {
    final CountDownLatch mSignal = new CountDownLatch(1);
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
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        assertNotNull(mPrinterManager);
        
        mPrinterManager.setPrinterSearchCallback(this);
        mPrinterManager.setPrintersCallback(this);

        mPrintersList = mPrinterManager.getSavedPrintersList();
        assertNotNull(mPrintersList);
        
        mImageView = new ImageView(getActivity());        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ================================================================================
    // Tests - getPrinterCount
    // ================================================================================

    public void testGetPrinterCount_DuringIdle() {
        try {
            int count = -1;
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
                mPrinterManager.savePrinterToDB(printer);
            }
            
            testClearDefaultPrinter();
            mPrinterManager.setDefaultPrinter(printer);
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
            testSetDefaultPrinter_NoDefaultPrinter();
            mPrinterManager.setDefaultPrinter(printer);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetDefaultPrinter_NullPrinter() {
        try {
            mPrinterManager.setDefaultPrinter(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getDefaultPrinter
    // ================================================================================

    public void testGetDefaultPrinter_NoDefaultPrinter() {
        try {
            int defaultPrinter = -1;
            testClearDefaultPrinter();

            defaultPrinter = mPrinterManager.getDefaultPrinter();
            assertEquals(false, defaultPrinter >= 0);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testGetDefaultPrinter_WithDefaultPrinter() {
        try {
            int defaultPrinter = -1;
            testSetDefaultPrinter_NoDefaultPrinter();

            defaultPrinter = mPrinterManager.getDefaultPrinter();
            assertEquals(true, defaultPrinter >= 0);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testGetDefaultPrinter_WithDefaultPrinterActivityRestarted() {
        try {
            int defaultPrinter = -1;

            defaultPrinter = mPrinterManager.getDefaultPrinter();
            assertEquals(true, defaultPrinter >= 0);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    // ================================================================================
    // Tests - isCancelled
    // ================================================================================

    public void testIsCancelled_StateNotCancelled() {
        try {
            boolean isCancelled = true;
            mPrinterManager.startPrinterSearch();

            isCancelled = mPrinterManager.isCancelled();
            assertEquals(false, isCancelled);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testIsCancelled_StateCancelled() {
        try {
            boolean isCancelled = false;
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
            boolean isSearching = false;
            mPrinterManager.startPrinterSearch();

            isSearching = mPrinterManager.isSearching();
            assertEquals(true, isSearching);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testIsSearching_StateNotSearching() {
        try {
            boolean isSearching = true;
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
            Printer printer = new Printer("testRemovePrinter_ValidAndInvalidPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret = false;

            if (!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer);
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
            boolean ret = false;

            if (!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer);
            }

            printer.setName(IPV4_OFFLINE_PRINTER_ADDRESS);
            ret = mPrinterManager.removePrinter(printer);
            assertEquals(true, ret);

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
            boolean ret = false;
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

            ret = mPrinterManager.savePrinterToDB(printer);
            assertEquals(true, ret);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSavePrinterToDB_ExsistingPrinter() {
        try {
            Printer printer = new Printer("testSavePrinterToDB_ExsistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret = false;

            if (!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer);
            }
            
            ret = mPrinterManager.savePrinterToDB(printer);
            assertEquals(false, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSavePrinterToDB_NullPrinter() {
        try {
            mPrinterManager.savePrinterToDB(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testSavePrinterToDB_DefaultPrinter() {
        try {
            Printer printer = new Printer("testSavePrinterToDB_ExsistingPrinter",
                    IPV4_OFFLINE_PRINTER_ADDRESS);
            if (!mPrintersList.isEmpty()) {
                for (int i = mPrintersList.size(); i > 0; i--) {
                    mPrinterManager.removePrinter(mPrintersList.get(i - 1));
                }
            }
            mPrinterManager.setPrintersCallback(this);
            mPrinterManager.savePrinterToDB(printer);
            mPrinterManager.setPrintersCallback(null);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getSavedPrintersList
    // ================================================================================
    
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

    // ================================================================================
    // Tests - isExists
    // ================================================================================

    public void testIsExists_ExistingPrinter() {
        try {
            Printer printer = new Printer("testIsExists_ExistingPrinter", IPV4_OFFLINE_PRINTER_ADDRESS);
            boolean ret = false;

            if(!mPrinterManager.isExists(printer)) {
                mPrinterManager.savePrinterToDB(printer);
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
            boolean ret = false;
            if (mPrinterManager.getSavedPrintersList() != null) {
                for (Printer savedPrinter : mPrinterManager.getSavedPrintersList()) {
                    if (printer.getIpAddress().equals(savedPrinter.getIpAddress())) {
                        printer = savedPrinter;
                        break;
                    }
                }
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

    public void testIsOnline_OnlinePrinter() {
        try {
            boolean ret = false;

            ret = mPrinterManager.isOnline(IPV4_ONLINE_PRINTER_ADDRESS);
            ret = mPrinterManager.isOnline(IPV6_ONLINE_PRINTER_ADDRESS);

            assertEquals(true, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testIsOnline_OfflinePrinter() {
        try {
            boolean ret = true;

            ret = mPrinterManager.isOnline(IPV4_OFFLINE_PRINTER_ADDRESS);
            assertEquals(false, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testIsOnline_NullAddress() {
        try {
            boolean ret = true;

            ret = mPrinterManager.isOnline(null);
            assertEquals(false, ret);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testIsOnline_InvalidAddress() {
        try {
            boolean ret = true;

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
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testSearchPrinter_NullIpAddress() {
        try {
            mPrinterManager.searchPrinter(null);            
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    // ================================================================================
    // Tests - UpdateOnlineStatusTask
    // ================================================================================

    public void testUpdateOnlineStatusTask_EmptyIpAddress() {
        mPrinterManager.new UpdateOnlineStatusTask(null, "")
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
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
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            // Wait and Check if Address is OFFLINE
            getInstrumentation().waitForIdleSync();
            Thread.sleep(10000);
            getInstrumentation().waitForIdleSync();

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testUpdateOnlineStatusTask_ValidOnlineParameters() {
        mPrinterManager.new UpdateOnlineStatusTask(mImageView, IPV4_ONLINE_PRINTER_ADDRESS)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    // Interface
    // ================================================================================

    @Override
    public void updateOnlineStatus() {
    }

    @Override
    public void onAddedNewPrinter(Printer printer) {
    }

    @Override
    public void onPrinterAdd(Printer printer) {
    }

    @Override
    public void onSearchEnd() {
    }
}
