package jp.co.riso.smartdeviceapp.model

import org.junit.Assert
import org.junit.Test

class ContentPrintPrinterCapabilitiesTest {
    @Test
    fun testContentPrintPrinterCapabilities_Init() {
        val capabilities = ContentPrintPrinterCapabilities()
        Assert.assertNotNull(capabilities)
        Assert.assertNotNull(capabilities.toString())
        Assert.assertFalse(capabilities.paperTypeLightweight)
        Assert.assertFalse(capabilities.inputTrayStandard)
        Assert.assertFalse(capabilities.inputTrayTray1)
        Assert.assertFalse(capabilities.inputTrayTray2)
        Assert.assertFalse(capabilities.inputTrayTray3)
        Assert.assertFalse(capabilities.inputTrayExternal)
        Assert.assertFalse(capabilities.booklet)
        Assert.assertFalse(capabilities.finisher0Holes)
        Assert.assertFalse(capabilities.finisher23Holes)
        Assert.assertFalse(capabilities.finisher24Holes)
        Assert.assertFalse(capabilities.staple)
        Assert.assertFalse(capabilities.outputTrayFacedown)
        Assert.assertFalse(capabilities.outputTrayTop)
        Assert.assertFalse(capabilities.outputTrayStacking)
    }

    @Test
    fun testContentPrintPrinterCapabilities_SetValues() {
        val capabilities = ContentPrintPrinterCapabilities()
        capabilities.paperTypeLightweight = true
        capabilities.inputTrayStandard = true
        capabilities.inputTrayTray1 = true
        capabilities.inputTrayTray2 = true
        capabilities.inputTrayTray3 = true
        capabilities.inputTrayExternal = true
        capabilities.booklet = true
        capabilities.finisher0Holes = true
        capabilities.finisher23Holes = true
        capabilities.finisher24Holes = true
        capabilities.staple = true
        capabilities.outputTrayFacedown = true
        capabilities.outputTrayTop = true
        capabilities.outputTrayStacking = true
        Assert.assertNotNull(capabilities)
        Assert.assertNotNull(capabilities.toString())
    }
}