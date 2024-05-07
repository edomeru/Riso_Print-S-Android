package jp.co.riso.smartdeviceapp.model

import jp.co.riso.smartdeviceapp.AppConstants
import org.junit.Assert
import org.junit.Test

class ContentPrintPrinterTest {
    @Test
    fun testContentPrintPrinter_Init() {
        val contentPrintPrinter = ContentPrintPrinter()
        contentPrintPrinter.serialNo = SERIAL_NO
        contentPrintPrinter.printerName = PRINTER_NAME
        contentPrintPrinter.model = PRINTER_MODEL
        contentPrintPrinter.printerCapabilities = ContentPrintPrinterCapabilities()
        Assert.assertNotNull(contentPrintPrinter)
    }

    @Test
    fun testContentPrintPrinter_Init_Default() {
        val contentPrintPrinter = ContentPrintPrinter()
        Assert.assertNotNull(contentPrintPrinter)
        Assert.assertNull(contentPrintPrinter.serialNo)
        Assert.assertNull(contentPrintPrinter.printerName)
        Assert.assertNull(contentPrintPrinter.model)
        Assert.assertNull(contentPrintPrinter.printerCapabilities)
    }

    @Test
    fun testContentPrintPrinter_IsPrinterFT_True() {
        val contentPrintPrinter = ContentPrintPrinter()
        contentPrintPrinter.model = AppConstants.PRINTER_MODEL_FT
        val result = contentPrintPrinter.isPrinterFTorCEREZONA_S
        Assert.assertTrue(result)
    }

    @Test
    fun testContentPrintPrinter_IsPrinterFT_Null() {
        val contentPrintPrinter = ContentPrintPrinter()
        val result = contentPrintPrinter.isPrinterFTorCEREZONA_S
        Assert.assertFalse(result)
    }

    @Test
    fun testContentPrintPrinter_IsPrinterFT_False() {
        val contentPrintPrinter = ContentPrintPrinter()
        contentPrintPrinter.model = PRINTER_MODEL
        val result = contentPrintPrinter.isPrinterFTorCEREZONA_S
        Assert.assertFalse(result)
    }

    @Test
    fun testContentPrintPrinter_IsPrinterGL_True() {
        val contentPrintPrinter = ContentPrintPrinter()
        contentPrintPrinter.model = AppConstants.PRINTER_MODEL_GL
        val result = contentPrintPrinter.isPrinterGLorOGA
        Assert.assertTrue(result)
    }

    @Test
    fun testContentPrintPrinter_IsPrinterGL_Null() {
        val contentPrintPrinter = ContentPrintPrinter()
        val result = contentPrintPrinter.isPrinterGLorOGA
        Assert.assertFalse(result)
    }

    @Test
    fun testContentPrintPrinter_IsPrinterGL_False() {
        val contentPrintPrinter = ContentPrintPrinter()
        contentPrintPrinter.model = PRINTER_MODEL
        val result = contentPrintPrinter.isPrinterGLorOGA
        Assert.assertFalse(result)
    }

    companion object {
        private const val SERIAL_NO = "12345"
        private const val PRINTER_NAME = "Test Printer"
        private const val PRINTER_MODEL = "Test Printer Model"
    }
}