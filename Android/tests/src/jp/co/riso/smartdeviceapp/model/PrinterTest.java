
package jp.co.riso.smartdeviceapp.model;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer.Config;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import junit.framework.TestCase;

public class PrinterTest extends TestCase {
    final private String PRINTER_NAME = "Test Printer";
    final private String PRINTER_ADDRESS = "192.168.1.206";

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
}
