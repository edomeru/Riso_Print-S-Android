package jp.co.riso.smartdeviceapp.model

import android.os.Bundle
import android.os.Parcel
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting
import junit.framework.TestCase

class PrinterTest(name: String?) : TestCase(name) {
    private val PRINTER_NAME = "Test Printer"
    private val PRINTER_ADDRESS = "192.168.1.206"
    private val PRINTER_TAG = "PRINTER_BUNDLE"
    private val PRINTER_ARRAY_TAG = "PRINTER_ARRAY_BUNDLE"

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    fun testConstructor() {
        val printer = Printer(PRINTER_NAME, PRINTER_ADDRESS)
        assertNotNull(printer)
    }

    fun testConstructor_NullName() {
        val printer = Printer(null, PRINTER_ADDRESS)
        assertNotNull(printer)
    }

    fun testConstructor_Parcel() {
        val printer = Printer(PRINTER_NAME, PRINTER_ADDRESS)
        val printerArray = arrayOfNulls<Printer>(2)

        // Create parcelable object and put to Bundle
        val bundlePut = Bundle()

        // Null config
        printer.config = null
        // Parcel in
        bundlePut.putParcelable(PRINTER_TAG, printer)
        // New Array
        bundlePut.putParcelableArray(PRINTER_ARRAY_TAG, printerArray)

        // Save bundle to parcel
        val parcel = Parcel.obtain()
        bundlePut.writeToParcel(parcel, 0)
        printer.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val bundleExtract = parcel.readBundle()
        bundleExtract!!.classLoader = Printer::class.java.classLoader
        bundleExtract.getParcelableArray(PRINTER_ARRAY_TAG)
        val createFromParcel = Printer.CREATOR.createFromParcel(parcel)!!
        parcel.recycle()
        assertNotNull(createFromParcel)
    }

    // ================================================================================
    // Tests - newArray
    // ================================================================================
    fun testNewArray_Parcel() {
        val printer: Array<out Printer?>? = Printer.CREATOR.newArray(2)
        assertNotNull(printer)
    }

    // ================================================================================
    // Tests - setId
    // ================================================================================
    fun testSetId() {
        try {
            val printer = Printer("", "")
            val id = 0
            printer.id = id
            assertEquals(id, printer.id)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getId
    // ================================================================================
    fun testGetId_EmptyId() {
        try {
            val printer = Printer("", "")
            assertEquals(PrinterManager.EMPTY_ID, printer.id)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setName
    // ================================================================================
    fun testSetName_NullName() {
        try {
            val printer = Printer("", "")
            printer.name = null
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    fun testSetName_ValidName() {
        try {
            val printer = Printer("", "")
            printer.name = PRINTER_NAME
            assertEquals(PRINTER_NAME, printer.name)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getIpAddress
    // ================================================================================
    fun testGetIpAddress() {
        try {
            val printer = Printer("", "")
            printer.ipAddress
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setIpAddress
    // ================================================================================
    fun testSetIpAddress_ValidAddress() {
        val printer = Printer("", "")
        printer.ipAddress = PRINTER_ADDRESS
        assertEquals(PRINTER_ADDRESS, printer.ipAddress)
    }

    fun testSetIpAddress_NullAddress() {
        try {
            val printer = Printer("", "")
            printer.ipAddress = null
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getPrinterType
    // ================================================================================
    fun testGetPrinterType_BlankName() {
        val printer = Printer("", "")
        val printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_IS)
    }

    fun testGetPrinterType_IS() {
        var printer = Printer("RISO IS950C-G", "")
        var printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_IS)
        printer = Printer("RISO IS1000C-G", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_IS)
        printer = Printer("RISO IS1000C-J", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_IS)
        printer = Printer("RISO IS10", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_IS)
    }

    fun testGetPrinterType_GD() {
        var printer = Printer("RISO IS950C-GD", "")
        var printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_GD)
        printer = Printer("ORPHIS GD", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_GD)
        printer = Printer("ORPHIS GDMODEL", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_GD)
    }

    fun testGetPrinterType_FW() {
        var printer = Printer("RISO IS950C-FW", "")
        var printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FW)
        printer = Printer("ORPHIS FW", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FW)
        printer = Printer("ORPHIS FWMODEL", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FW)
    }

    fun testGetPrinterType_FT() {
        var printer = Printer("RISO IS950C-FT", "")
        var printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
        printer = Printer("ORPHIS FT", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
        printer = Printer("ComColor FT", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
    }

    fun testGetPrinterType_GL() {
        var printer = Printer("RISO IS950C-GL", "")
        var printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_GL)
        printer = Printer("ORPHIS GL", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_GL)
        printer = Printer("ComColor GL", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_GL)
    }

    fun testGetPrinterType_CEREZONA_S() {
        var printer = Printer("RISO CEREZONA S", "")
        var printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
        printer = Printer("RISO CEREZONA S200", "")
        printerType = printer.printerType
        assertEquals(printerType, AppConstants.PRINTER_MODEL_FT)
    }

    // ================================================================================
    // Tests - getPortSetting
    // ================================================================================
    fun testGetPortSetting() {
        try {
            val printer = Printer("", "")
            printer.portSetting
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - setPortSetting
    // ================================================================================
    fun testSetPortSetting() {
        try {
            val printer = Printer("", "")
            var portSetting = 1
            printer.portSetting = PortSetting.RAW
            assertTrue(PortSetting.RAW == printer.portSetting)
            assertEquals(PortSetting.RAW.ordinal, portSetting)
            portSetting = 0
            printer.portSetting = PortSetting.LPR
            assertTrue(PortSetting.LPR == printer.portSetting)
            assertEquals(PortSetting.LPR.ordinal, portSetting)
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - getConfig
    // ================================================================================
    fun testGetConfig() {
        val printer = Printer("", "")
        val config = printer.config
        assertNotNull(config)
    }

    // ================================================================================
    // Tests - setConfig
    // ================================================================================
    fun testSetConfig_ValidConfig() {
        val printer = Printer("", "")
        val config = printer.Config()
        printer.config = config
        assertNotNull(config)
    }

    fun testSetConfig_NullConfig() {
        try {
            val printer = Printer("", "")
            printer.config = null
        } catch (e: Exception) {
            fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - describeContents
    // ================================================================================
    fun testDescribeContents() {
        val printer = Printer("", "")
        printer.describeContents()
    }

    // ================================================================================
    // Tests - Config
    // ================================================================================
    fun testGetConfigParams() {
        val printer = Printer("", "")
        printer.config!!.isLprAvailable
        printer.config!!.isRawAvailable
        printer.config!!.isBookletFinishingAvailable
        printer.config!!.isStaplerAvailable
        printer.config!!.isPunch3Available
        printer.config!!.isPunch4Available
        printer.config!!.isTrayFaceDownAvailable
        printer.config!!.isTrayTopAvailable
        printer.config!!.isTrayStackAvailable
        printer.config!!.isTrayTopAvailable
        printer.config!!.isExternalFeederAvailable
        printer.config!!.isPunch0Available
    }

    fun testSetConfigParams() {
        val printer = Printer("", "")
        printer.config!!.isLprAvailable = true
        printer.config!!.isRawAvailable = true
        printer.config!!.isBookletFinishingAvailable = true
        printer.config!!.isStaplerAvailable = true
        printer.config!!.isPunch3Available = true
        printer.config!!.isPunch4Available = true
        printer.config!!.isTrayFaceDownAvailable = true
        printer.config!!.isTrayTopAvailable = true
        printer.config!!.isTrayStackAvailable = true
        printer.config!!.isTrayTopAvailable = true
        printer.config!!.isExternalFeederAvailable = true
        printer.config!!.isPunch0Available = true
    }

    fun testGetPunchAvailability() {
        val printer = Printer("", "")
        printer.config!!.isPunchAvailable

        // isPunch0Available is true
        printer.config!!.isPunch0Available = true
        printer.config!!.isPunch3Available = false
        printer.config!!.isPunch4Available = false
        printer.config!!.isPunchAvailable

        printer.config!!.isPunch0Available = true
        printer.config!!.isPunch3Available = true
        printer.config!!.isPunch4Available = false
        printer.config!!.isPunchAvailable

        printer.config!!.isPunch0Available = true
        printer.config!!.isPunch3Available = false
        printer.config!!.isPunch4Available = true
        printer.config!!.isPunchAvailable

        printer.config!!.isPunch0Available = true
        printer.config!!.isPunch3Available = true
        printer.config!!.isPunch4Available = true
        printer.config!!.isPunchAvailable

        // isPunch0Available is false
        printer.config!!.isPunch0Available = false
        printer.config!!.isPunch3Available = false
        printer.config!!.isPunch4Available = false
        printer.config!!.isPunchAvailable

        printer.config!!.isPunch0Available = false
        printer.config!!.isPunch3Available = true
        printer.config!!.isPunch4Available = false
        printer.config!!.isPunchAvailable

        printer.config!!.isPunch0Available = false
        printer.config!!.isPunch3Available = false
        printer.config!!.isPunch4Available = true
        printer.config!!.isPunchAvailable

        printer.config!!.isPunch0Available = false
        printer.config!!.isPunch3Available = true
        printer.config!!.isPunch4Available = true
        printer.config!!.isPunchAvailable
    }
}