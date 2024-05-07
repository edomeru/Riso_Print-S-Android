package jp.co.riso.smartdeviceapp.model

import jp.co.riso.smartdeviceapp.model.printsettings.Preview
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import org.junit.Assert
import org.junit.Test

class ContentPrintPrintSettingsTest {
    @Test
    fun testContentPrintPrintSettings_Init() {
        val contentPrintPrintSettings = ContentPrintPrintSettings()
        Assert.assertNotNull(contentPrintPrintSettings)
        Assert.assertEquals(contentPrintPrintSettings.colorMode, 0)
        Assert.assertEquals(contentPrintPrintSettings.orientation, 0)
        Assert.assertEquals(contentPrintPrintSettings.paperSize, 0)
        Assert.assertEquals(contentPrintPrintSettings.copies, 1)
        Assert.assertEquals(contentPrintPrintSettings.duplex, 0)
        Assert.assertEquals(contentPrintPrintSettings.paperType, 0)
        Assert.assertEquals(contentPrintPrintSettings.scaleToFit, true)
        Assert.assertEquals(contentPrintPrintSettings.paperType, 0)
        Assert.assertEquals(contentPrintPrintSettings.inputTray, 0)
        Assert.assertEquals(contentPrintPrintSettings.imposition, 0)
        Assert.assertEquals(contentPrintPrintSettings.impositionOrder, 0)
        Assert.assertEquals(contentPrintPrintSettings.sort, 0)
        Assert.assertEquals(contentPrintPrintSettings.booklet, false)
        Assert.assertEquals(contentPrintPrintSettings.bookletFinish, 0)
        Assert.assertEquals(contentPrintPrintSettings.bookletLayout, 0)
        Assert.assertEquals(contentPrintPrintSettings.finishingSide, 0)
        Assert.assertEquals(contentPrintPrintSettings.staple, 0)
        Assert.assertEquals(contentPrintPrintSettings.punch, 0)
        Assert.assertEquals(contentPrintPrintSettings.outputTray, 0)
        Assert.assertNull(contentPrintPrintSettings.loginId)
        Assert.assertNotNull(contentPrintPrintSettings.toString())
    }

    @Test
    fun testContentPrintPrintSettings_SetValues() {
        val contentPrintPrintSettings = ContentPrintPrintSettings()
        contentPrintPrintSettings.colorMode = 1
        contentPrintPrintSettings.orientation = 1
        contentPrintPrintSettings.paperSize = 1
        contentPrintPrintSettings.copies = 3
        contentPrintPrintSettings.duplex = 1
        contentPrintPrintSettings.paperType = 1
        contentPrintPrintSettings.scaleToFit = false
        contentPrintPrintSettings.paperType = 1
        contentPrintPrintSettings.inputTray = 1
        contentPrintPrintSettings.imposition = 1
        contentPrintPrintSettings.impositionOrder = 1
        contentPrintPrintSettings.sort = 1
        contentPrintPrintSettings.booklet = true
        contentPrintPrintSettings.bookletFinish = 1
        contentPrintPrintSettings.bookletLayout = 1
        contentPrintPrintSettings.finishingSide = 1
        contentPrintPrintSettings.staple = 1
        contentPrintPrintSettings.punch = 1
        contentPrintPrintSettings.outputTray = 1
        contentPrintPrintSettings.loginId = LOGIN_ID
        Assert.assertNotNull(contentPrintPrintSettings)
        Assert.assertNotNull(contentPrintPrintSettings.toString())
    }

    @Test
    fun testConvertToPrintSettings_Default() {
        val contentPrintPrintSettings = ContentPrintPrintSettings()
        val printSettings = ContentPrintPrintSettings.convertToPrintSettings(contentPrintPrintSettings)
        Assert.assertNotNull(printSettings)
        Assert.assertEquals(printSettings.colorMode, Preview.ColorMode.AUTO)
        Assert.assertEquals(printSettings.orientation, Preview.Orientation.PORTRAIT)
        Assert.assertEquals(printSettings.duplex, Preview.Duplex.OFF)
        Assert.assertEquals(printSettings.paperSize, Preview.PaperSize.A3)
        Assert.assertEquals(printSettings.inputTray, Preview.InputTrayFtGlCerezonaSOga.AUTO)
        Assert.assertEquals(printSettings.isScaleToFit, true)
        Assert.assertEquals(printSettings.imposition, Preview.Imposition.OFF)
        Assert.assertEquals(printSettings.sort, Preview.Sort.PER_COPY)
        Assert.assertEquals(printSettings.isBooklet, false)
        Assert.assertEquals(printSettings.bookletFinish, Preview.BookletFinish.OFF)
        Assert.assertEquals(printSettings.bookletLayout, Preview.BookletLayout.FORWARD)
        Assert.assertEquals(printSettings.finishingSide, Preview.FinishingSide.LEFT)
        Assert.assertEquals(printSettings.staple, Preview.Staple.OFF)
        Assert.assertEquals(printSettings.punch, Preview.Punch.OFF)
    }

    @Test
    fun testConvertToPrintSettings_SetValues() {
        val contentPrintPrintSettings = ContentPrintPrintSettings()
        contentPrintPrintSettings.colorMode = 1
        contentPrintPrintSettings.orientation = 1
        contentPrintPrintSettings.paperSize = 1
        contentPrintPrintSettings.copies = 3
        contentPrintPrintSettings.duplex = 1
        contentPrintPrintSettings.paperType = 1
        contentPrintPrintSettings.scaleToFit = false
        contentPrintPrintSettings.paperType = 1
        contentPrintPrintSettings.inputTray = 1
        contentPrintPrintSettings.imposition = 1
        contentPrintPrintSettings.impositionOrder = 1
        contentPrintPrintSettings.sort = 1
        contentPrintPrintSettings.booklet = true
        contentPrintPrintSettings.bookletFinish = 1
        contentPrintPrintSettings.bookletLayout = 1
        contentPrintPrintSettings.finishingSide = 1
        contentPrintPrintSettings.staple = 1
        contentPrintPrintSettings.punch = 1
        contentPrintPrintSettings.outputTray = 1
        contentPrintPrintSettings.loginId = LOGIN_ID
        val printSettings = ContentPrintPrintSettings.convertToPrintSettings(contentPrintPrintSettings)
        Assert.assertNotNull(printSettings)
        Assert.assertEquals(printSettings.colorMode, Preview.ColorMode.FULL_COLOR)
        Assert.assertEquals(printSettings.orientation, Preview.Orientation.LANDSCAPE)
        Assert.assertEquals(printSettings.duplex, Preview.Duplex.LONG_EDGE)
        Assert.assertEquals(printSettings.paperSize, Preview.PaperSize.A3W)
        Assert.assertEquals(printSettings.inputTray, Preview.InputTrayFtGlCerezonaSOga.STANDARD)
        Assert.assertEquals(printSettings.isScaleToFit, false)
        Assert.assertEquals(printSettings.imposition, Preview.Imposition.TWO_UP)
        Assert.assertEquals(printSettings.sort, Preview.Sort.PER_PAGE)
        Assert.assertEquals(printSettings.isBooklet, true)
        Assert.assertEquals(printSettings.bookletFinish, Preview.BookletFinish.PAPER_FOLDING)
        Assert.assertEquals(printSettings.bookletLayout, Preview.BookletLayout.REVERSE)
        Assert.assertEquals(printSettings.finishingSide, Preview.FinishingSide.TOP)
        Assert.assertEquals(printSettings.staple, Preview.Staple.ONE_UL)
        Assert.assertEquals(printSettings.punch, Preview.Punch.HOLES_2)
    }

    @Test
    fun testConvertToContentPrintPrintSettings_Default() {
        val printSettings = PrintSettings()
        val contentPrintPrintSettings = ContentPrintPrintSettings.convertToContentPrintPrintSettings(printSettings, LOGIN_ID)
        Assert.assertNotNull(contentPrintPrintSettings)
        Assert.assertEquals(contentPrintPrintSettings.colorMode, 0)
        Assert.assertEquals(contentPrintPrintSettings.orientation, 0)
        Assert.assertEquals(contentPrintPrintSettings.copies, 1)
        Assert.assertEquals(contentPrintPrintSettings.duplex, 0)
        Assert.assertEquals(contentPrintPrintSettings.paperType, 0)
        Assert.assertEquals(contentPrintPrintSettings.scaleToFit, true)
        Assert.assertEquals(contentPrintPrintSettings.paperType, 0)
        Assert.assertEquals(contentPrintPrintSettings.inputTray, 0)
        Assert.assertEquals(contentPrintPrintSettings.imposition, 0)
        Assert.assertEquals(contentPrintPrintSettings.impositionOrder, 0)
        Assert.assertEquals(contentPrintPrintSettings.sort, 0)
        Assert.assertEquals(contentPrintPrintSettings.booklet, false)
        Assert.assertEquals(contentPrintPrintSettings.bookletFinish, 0)
        Assert.assertEquals(contentPrintPrintSettings.bookletLayout, 0)
        Assert.assertEquals(contentPrintPrintSettings.finishingSide, 0)
        Assert.assertEquals(contentPrintPrintSettings.staple, 0)
        Assert.assertEquals(contentPrintPrintSettings.punch, 0)
        Assert.assertEquals(contentPrintPrintSettings.outputTray, 0)
        Assert.assertEquals(contentPrintPrintSettings.loginId, LOGIN_ID)
    }

    @Test
    fun testConvertToContentPrintPrintSettings_SetValues() {
        val printSettings = PrintSettings()
        printSettings.setValue(PrintSettings.TAG_COLOR_MODE, 1)
        printSettings.setValue(PrintSettings.TAG_ORIENTATION, 1)
        printSettings.setValue(PrintSettings.TAG_COPIES, 3)
        printSettings.setValue(PrintSettings.TAG_DUPLEX, 1)
        printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, 1)
        printSettings.setValue(PrintSettings.TAG_SCALE_TO_FIT, 0)
        printSettings.setValue(PrintSettings.TAG_PAPER_TYPE, 1)
        printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, 1)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION, 1)
        printSettings.setValue(PrintSettings.TAG_IMPOSITION_ORDER, 1)
        printSettings.setValue(PrintSettings.TAG_SORT, 1)
        printSettings.setValue(PrintSettings.TAG_BOOKLET, 1)
        printSettings.setValue(PrintSettings.TAG_BOOKLET_FINISH, 1)
        printSettings.setValue(PrintSettings.TAG_BOOKLET_LAYOUT, 1)
        printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, 1)
        printSettings.setValue(PrintSettings.TAG_STAPLE, 1)
        printSettings.setValue(PrintSettings.TAG_PUNCH, 1)
        printSettings.setValue(PrintSettings.TAG_OUTPUT_TRAY, 1)
        val contentPrintPrintSettings = ContentPrintPrintSettings.convertToContentPrintPrintSettings(printSettings, LOGIN_ID)
        Assert.assertNotNull(contentPrintPrintSettings)
        Assert.assertEquals(contentPrintPrintSettings.colorMode, 1)
        Assert.assertEquals(contentPrintPrintSettings.orientation, 1)
        Assert.assertEquals(contentPrintPrintSettings.copies, 3)
        Assert.assertEquals(contentPrintPrintSettings.duplex, 1)
        Assert.assertEquals(contentPrintPrintSettings.paperType, 1)
        Assert.assertEquals(contentPrintPrintSettings.scaleToFit, false)
        Assert.assertEquals(contentPrintPrintSettings.paperType, 1)
        Assert.assertEquals(contentPrintPrintSettings.inputTray, 1)
        Assert.assertEquals(contentPrintPrintSettings.imposition, 1)
        Assert.assertEquals(contentPrintPrintSettings.impositionOrder, 1)
        Assert.assertEquals(contentPrintPrintSettings.sort, 1)
        Assert.assertEquals(contentPrintPrintSettings.booklet, true)
        Assert.assertEquals(contentPrintPrintSettings.bookletFinish, 1)
        Assert.assertEquals(contentPrintPrintSettings.bookletLayout, 1)
        Assert.assertEquals(contentPrintPrintSettings.finishingSide, 1)
        Assert.assertEquals(contentPrintPrintSettings.staple, 1)
        Assert.assertEquals(contentPrintPrintSettings.punch, 1)
        Assert.assertEquals(contentPrintPrintSettings.outputTray, 1)
        Assert.assertEquals(contentPrintPrintSettings.loginId, LOGIN_ID)
    }

    companion object {
        private const val LOGIN_ID = "test"
    }
}