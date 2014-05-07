
package jp.co.riso.smartdeviceapp.controller.printer;

import java.util.List;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.SNMPManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.UpdateStatusCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;

public class PrinterManagerTest extends ActivityInstrumentationTestCase2<MainActivity> implements
        UpdateStatusCallback, PrintersCallback, PrinterSearchCallback {
    private static final String IPV4_ONLINE_PRINTER_ADDRESS = "192.168.1.206";
    private static final String IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.206";

    private PrinterManager mPrinterManager = null;
    private List<Printer> mPrintersList = null;

    public PrinterManagerTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        assertNotNull(mPrinterManager);

        mPrintersList = mPrinterManager.getSavedPrintersList();
        assertNotNull(mPrintersList);
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
            Printer printer = mPrinterManager.getSavedPrintersList().get(0);
            testClearDefaultPrinter();
            mPrinterManager.setDefaultPrinter(printer);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetDefaultPrinter_WithDefaultPrinter() {
        try {
            Printer printer = mPrinterManager.getSavedPrintersList().get(0);
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
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - cancelUpdateStatusThread
    // ================================================================================

    public void testCancelUpdateStatusThread() {
        try {
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
            mPrinterManager.updateOnlineStatus(null, new
                    ImageView(SmartDeviceApp.getAppContext()));
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
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

    // ================================================================================
    // Tests - onFoundDevice
    // ================================================================================

    public void testOnFoundDevice_NullManager() {
        try {
            mPrinterManager.onFoundDevice(null, IPV4_ONLINE_PRINTER_ADDRESS, "testOnEndDiscovery",
                    new boolean[6]);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnFoundDevice_NullIpAddress() {
        try {
            mPrinterManager.onFoundDevice(new SNMPManager(), null, "testOnEndDiscovery",
                    new boolean[6]);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnFoundDevice_NullName() {
        try {
            mPrinterManager.onFoundDevice(new SNMPManager(), IPV4_ONLINE_PRINTER_ADDRESS, null,
                    new boolean[6]);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnFoundDevice_NullCapabilities() {
        try {
            mPrinterManager.onFoundDevice(new SNMPManager(), IPV4_ONLINE_PRINTER_ADDRESS,
                    "testOnEndDiscovery", null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testOnFoundDevice_InvalidCapabilities() {
        try {
            mPrinterManager.onFoundDevice(null, IPV4_ONLINE_PRINTER_ADDRESS, "testOnEndDiscovery",
                    new boolean[5]);
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
            Printer printer = mPrinterManager.getSavedPrintersList().get(0);
            boolean ret = false;

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
            Printer printer = mPrinterManager.getSavedPrintersList().get(0);
            boolean ret = false;

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

    // ================================================================================
    // Tests - isExists
    // ================================================================================

    public void testIsExists_ExsistingPrinter() {
        try {
            Printer printer = mPrinterManager.getSavedPrintersList().get(0);
            boolean ret = false;

            ret = mPrinterManager.isExists(printer);
            assertEquals(true, ret);

            ret = mPrinterManager.isExists(printer.getIpAddress());
            assertEquals(true, ret);

        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testIsExists_NotExsistingPrinter() {
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
