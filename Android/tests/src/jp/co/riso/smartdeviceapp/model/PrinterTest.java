
package jp.co.riso.smartdeviceapp.model;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer.Config;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import junit.framework.TestCase;
import android.os.Bundle;
import android.os.Parcel;

public class PrinterTest extends TestCase {
    final private String PRINTER_NAME = "Test Printer";
    final private String PRINTER_ADDRESS = "192.168.1.206";
    final private String PRINTER_TAG = "PRINTER_BUNDLE";
    final private String PRINTER_ARRAY_TAG = "PRINTER_ARRAY_BUNDLE";


    public PrinterTest(String name) {
        super(name);
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
        Printer printer = new Printer(PRINTER_NAME, PRINTER_ADDRESS);

        assertNotNull(printer);
    }
    
    public void testConstructor_Parcel() {
        Printer printer = new Printer(PRINTER_NAME, PRINTER_ADDRESS);
        Printer printerArray[] = new Printer[2];
        
        // Create parcelable object and put to Bundle
        Bundle bundlePut = new Bundle();

        // Null config
        printer.setConfig(null);
        // Parcel in
        bundlePut.putParcelable(PRINTER_TAG, printer);
        // New Array
        bundlePut.putParcelableArray(PRINTER_ARRAY_TAG, printerArray);
        
        // Save bundle to parcel
        Parcel parcel = Parcel.obtain();
        bundlePut.writeToParcel(parcel, 0);
        printer.writeToParcel(parcel, 0);
        
        parcel.setDataPosition(0);
        Bundle bundleExtract = parcel.readBundle();
        bundleExtract.setClassLoader(Printer.class.getClassLoader());
        printer = bundleExtract.getParcelable(PRINTER_TAG);
        bundleExtract.getParcelableArray(PRINTER_ARRAY_TAG);
        
        Printer createFromParcel = Printer.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        assertNotNull(createFromParcel);
    }
    
    // ================================================================================
    // Tests - newArray
    // ================================================================================
    
    public void testNewArray_Parcel() {        
        Printer printer[] = Printer.CREATOR.newArray(2);
        assertNotNull(printer);
    }
    
    // ================================================================================
    // Tests - setId
    // ================================================================================

    public void testSetId() {
        try {
            Printer printer = new Printer("", "");
            int id = 0;

            printer.setId(id);
            assertEquals(id, printer.getId());
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getId
    // ================================================================================

    public void testGetId_EmptyId() {
        try {
            Printer printer = new Printer("", "");

            assertEquals(PrinterManager.EMPTY_ID, printer.getId());
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setName
    // ================================================================================

    public void testSetName_NullName() {
        try {
            Printer printer = new Printer("", "");

            printer.setName(null);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    public void testSetName_ValidName() {
        try {
            Printer printer = new Printer("", "");

            printer.setName(PRINTER_NAME);
            assertEquals(PRINTER_NAME, printer.getName());
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getIpAddress
    // ================================================================================

    public void testGetIpAddress() {
        try {
            Printer printer = new Printer("", "");

            printer.getIpAddress();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setIpAddress
    // ================================================================================

    public void testSetIpAddress_ValidAddress() {
        Printer printer = new Printer("", "");

        printer.setIpAddress(PRINTER_ADDRESS);
        assertEquals(PRINTER_ADDRESS, printer.getIpAddress());
    }

    public void testSetIpAddress_NullAddress() {
        try {
            Printer printer = new Printer("", "");

            printer.setIpAddress(null);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getPortSetting
    // ================================================================================

    public void testGetPortSetting() {
        try {
            Printer printer = new Printer("", "");

            printer.getPortSetting();
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setPortSetting
    // ================================================================================

    public void testSetPortSetting() {
        try {
            Printer printer = new Printer("", "");
            int portSetting = 1;

            printer.setPortSetting(portSetting);
            assertEquals(portSetting, printer.getPortSetting());

            portSetting = 0;
            printer.setPortSetting(portSetting);
            assertEquals(portSetting, printer.getPortSetting());
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getConfig
    // ================================================================================

    public void testGetConfig() {
        Printer printer = new Printer("", "");
        Config config = printer.getConfig();

        assertNotNull(config);
    }

    // ================================================================================
    // Tests - setConfig
    // ================================================================================

    public void testSetConfig_ValidConfig() {
        Printer printer = new Printer("", "");
        Config config = printer.new Config();

        printer.setConfig(config);
        assertNotNull(config);
    }

    public void testSetConfig_NullConfig() {
        try {
            Printer printer = new Printer("", "");

            printer.setConfig(null);
        } catch (Exception e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setConfig
    // ================================================================================

    public void testGetPrintSettings() {
        Printer printer = new Printer("", "");
        PrintSettings printSettings = printer.getPrintSettings();

        assertNotNull(printSettings);

        printer.setId(AppConstants.CONST_MAX_PRINTER_COUNT + 1);
        printer.getPrintSettings();
    }
    
    // ================================================================================
    // Tests - describeContents
    // ================================================================================

    public void testDescribeContents() {
        Printer printer = new Printer("", "");
        printer.describeContents();
    }
    
    // ================================================================================
    // Tests - Config
    // ================================================================================

    public void testConfig() {
        Printer printer = new Printer("", "");
        printer.getConfig().isLprAvailable();
        printer.getConfig().isRawAvailable();
        printer.getConfig().isBookletAvailable();
        printer.getConfig().isStaplerAvailable();
        printer.getConfig().isPunch3Available();
        printer.getConfig().isPunch4Available();
        printer.getConfig().isTrayFaceDownAvailable();
        printer.getConfig().isTrayTopAvailable();
        printer.getConfig().isTrayStackAvailable();
        printer.getConfig().isTrayTopAvailable();
    }
}