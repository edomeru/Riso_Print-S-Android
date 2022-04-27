package jp.co.riso.smartdeviceapp.model.printsettings

import android.util.Log
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.*
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class PreviewTest {

    private var _settingList: NodeList? = null

    @Before
    fun setUp() {
        val xmlString = AppUtils.getFileContentsFromAssets(
            SmartDeviceApp.getAppContext(),
            AppConstants.XML_FILENAME
        )
        var printSettingsContent: Document? = null
        val dbf = DocumentBuilderFactory.newInstance()
        try {
            val db = dbf.newDocumentBuilder()
            val `is` = InputSource()
            `is`.characterStream = StringReader(xmlString)
            printSettingsContent = db.parse(`is`)
        } catch (e: ParserConfigurationException) {
            Log.e(TAG, "Error: " + e.message)
        } catch (e: IOException) {
            Log.e(TAG, "Error: " + e.message)
        } catch (e: SAXException) {
            Log.e(TAG, "Error: " + e.message)
        }
        if (printSettingsContent == null) {
            return
        }
        _settingList = printSettingsContent.getElementsByTagName("setting")
    }

    @Test
    fun testConstructor() {
        TestCase.assertNotNull(Preview())
    }

    @Test
    fun testColorMode() {
        TestCase.assertEquals(0, ColorMode.AUTO.ordinal)
        TestCase.assertEquals(1, ColorMode.FULL_COLOR.ordinal)
        TestCase.assertEquals(2, ColorMode.MONOCHROME.ordinal)
        TestCase.assertEquals(3, ColorMode.DUAL_COLOR.ordinal)
        TestCase.assertEquals(ColorMode.AUTO, ColorMode.valueOf("AUTO"))
        TestCase.assertEquals(ColorMode.FULL_COLOR, ColorMode.valueOf("FULL_COLOR"))
        TestCase.assertEquals(ColorMode.MONOCHROME, ColorMode.valueOf("MONOCHROME"))
    }

    @Test
    fun testOrientation() {
        TestCase.assertEquals(0, Orientation.PORTRAIT.ordinal)
        TestCase.assertEquals(1, Orientation.LANDSCAPE.ordinal)
        TestCase.assertEquals(Orientation.PORTRAIT, Orientation.valueOf("PORTRAIT"))
        TestCase.assertEquals(
            Orientation.LANDSCAPE,
            Orientation.valueOf("LANDSCAPE")
        )
    }

    @Test
    fun testDuplex() {
        TestCase.assertEquals(0, Duplex.OFF.ordinal)
        TestCase.assertEquals(1, Duplex.LONG_EDGE.ordinal)
        TestCase.assertEquals(2, Duplex.SHORT_EDGE.ordinal)
        TestCase.assertEquals(Duplex.OFF, Duplex.valueOf("OFF"))
        TestCase.assertEquals(Duplex.LONG_EDGE, Duplex.valueOf("LONG_EDGE"))
        TestCase.assertEquals(Duplex.SHORT_EDGE, Duplex.valueOf("SHORT_EDGE"))
    }

    @Test
    fun testPaperSizeValueOf() {
        TestCase.assertEquals(PaperSize.A3, PaperSize.valueOf("A3"))
    }

    @Test
    fun testPaperSizeGetWidth() {
        TestCase.assertEquals(297.0f, PaperSize.A3.width)
        TestCase.assertEquals(210.0f, PaperSize.A4.width)
    }

    @Test
    fun testPaperSizeGetHeight() {
        TestCase.assertEquals(420.0f, PaperSize.A3.height)
        TestCase.assertEquals(297.0f, PaperSize.A4.height)
    }

    @Test
    fun testInputTray() {
        TestCase.assertEquals(0, InputTray_FT_GL_CEREZONA_S.AUTO.ordinal)
        TestCase.assertEquals(1, InputTray_FT_GL_CEREZONA_S.STANDARD.ordinal)
        TestCase.assertEquals(2, InputTray_FT_GL_CEREZONA_S.TRAY1.ordinal)
        TestCase.assertEquals(3, InputTray_FT_GL_CEREZONA_S.TRAY2.ordinal)
        TestCase.assertEquals(4, InputTray_FT_GL_CEREZONA_S.TRAY3.ordinal)
        TestCase.assertEquals(5, InputTray_FT_GL_CEREZONA_S.EXTERNAL_FEEDER.ordinal)
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.AUTO,
            InputTray_FT_GL_CEREZONA_S.valueOf("AUTO")
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.STANDARD,
            InputTray_FT_GL_CEREZONA_S.valueOf("STANDARD")
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.EXTERNAL_FEEDER,
            InputTray_FT_GL_CEREZONA_S.valueOf("EXTERNAL_FEEDER")
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.AUTO,
            InputTray_FT_GL_CEREZONA_S.valuesFT_CEREZONA_S()[0]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.STANDARD,
            InputTray_FT_GL_CEREZONA_S.valuesFT_CEREZONA_S()[1]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.TRAY1,
            InputTray_FT_GL_CEREZONA_S.valuesFT_CEREZONA_S()[2]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.TRAY2,
            InputTray_FT_GL_CEREZONA_S.valuesFT_CEREZONA_S()[3]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.EXTERNAL_FEEDER,
            InputTray_FT_GL_CEREZONA_S.valuesFT_CEREZONA_S()[4]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.AUTO,
            InputTray_FT_GL_CEREZONA_S.valuesGL()[0]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.STANDARD,
            InputTray_FT_GL_CEREZONA_S.valuesGL()[1]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.TRAY1,
            InputTray_FT_GL_CEREZONA_S.valuesGL()[2]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.TRAY2,
            InputTray_FT_GL_CEREZONA_S.valuesGL()[3]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.TRAY3,
            InputTray_FT_GL_CEREZONA_S.valuesGL()[4]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.EXTERNAL_FEEDER,
            InputTray_FT_GL_CEREZONA_S.valuesGL()[5]
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.AUTO,
            InputTray_FT_GL_CEREZONA_S.valueOf("AUTO")
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.STANDARD,
            InputTray_FT_GL_CEREZONA_S.valueOf("STANDARD")
        )
        TestCase.assertEquals(
            InputTray_FT_GL_CEREZONA_S.EXTERNAL_FEEDER,
            InputTray_FT_GL_CEREZONA_S.valueOf("EXTERNAL_FEEDER")
        )
    }

    @Test
    fun testImpositionValueOf() {
        TestCase.assertEquals(Imposition.OFF, Imposition.valueOf("OFF"))
    }

    @Test
    fun testImpositionGetPerPage() {
        TestCase.assertEquals(1, Imposition.OFF.perPage)
    }

    @Test
    fun testImpositionGetRows() {
        TestCase.assertEquals(1, Imposition.OFF.rows)
    }

    @Test
    fun testImpositionGetCols() {
        TestCase.assertEquals(1, Imposition.OFF.cols)
    }

    @Test
    fun testImpositionIsFlipLandscape() {
        TestCase.assertEquals(false, Imposition.OFF.isFlipLandscape)
    }

    @Test
    fun testImpositionOrderIsLeftToRight() {
        TestCase.assertEquals(true, ImpositionOrder.L_R.isLeftToRight)
    }

    @Test
    fun testImpositionOrderIsTopToBottom() {
        TestCase.assertEquals(true, ImpositionOrder.L_R.isTopToBottom)
    }

    @Test
    fun testImpositionOrderIsHorizontalFlow() {
        TestCase.assertEquals(true, ImpositionOrder.L_R.isHorizontalFlow)
    }

    @Test
    fun testImpositionOrderValueOf() {
        TestCase.assertEquals(ImpositionOrder.L_R, ImpositionOrder.valueOf("L_R"))
    }

    @Test
    fun testSort() {
        TestCase.assertEquals(0, Sort.PER_COPY.ordinal)
        TestCase.assertEquals(1, Sort.PER_PAGE.ordinal)
        TestCase.assertEquals(Sort.PER_PAGE, Sort.valueOf("PER_PAGE"))
        TestCase.assertEquals(Sort.PER_COPY, Sort.valueOf("PER_COPY"))
    }

    @Test
    fun testBookletFinish() {
        TestCase.assertEquals(0, BookletFinish.OFF.ordinal)
        TestCase.assertEquals(1, BookletFinish.PAPER_FOLDING.ordinal)
        TestCase.assertEquals(2, BookletFinish.FOLD_AND_STAPLE.ordinal)
        TestCase.assertEquals(BookletFinish.OFF, BookletFinish.valueOf("OFF"))
        TestCase.assertEquals(BookletFinish.PAPER_FOLDING, BookletFinish.valueOf("PAPER_FOLDING"))
        TestCase.assertEquals(
            BookletFinish.FOLD_AND_STAPLE,
            BookletFinish.valueOf("FOLD_AND_STAPLE")
        )
    }

    @Test
    fun testBookletLayout() {
        TestCase.assertEquals(0, BookletLayout.FORWARD.ordinal)
        TestCase.assertEquals(1, BookletLayout.REVERSE.ordinal)
        TestCase.assertEquals(BookletLayout.FORWARD, BookletLayout.valueOf("FORWARD"))
        TestCase.assertEquals(BookletLayout.REVERSE, BookletLayout.valueOf("REVERSE"))
    }

    @Test
    fun testFinishingSide() {
        TestCase.assertEquals(0, FinishingSide.LEFT.ordinal)
        TestCase.assertEquals(1, FinishingSide.TOP.ordinal)
        TestCase.assertEquals(2, FinishingSide.RIGHT.ordinal)
        TestCase.assertEquals(FinishingSide.LEFT, FinishingSide.valueOf("LEFT"))
        TestCase.assertEquals(FinishingSide.TOP, FinishingSide.valueOf("TOP"))
        TestCase.assertEquals(FinishingSide.RIGHT, FinishingSide.valueOf("RIGHT"))
    }

    @Test
    fun testStapleValueOf() {
        TestCase.assertEquals(Staple.OFF, Staple.valueOf("OFF"))
    }

    @Test
    fun testStapleGetCount() {
        TestCase.assertEquals(0, Staple.OFF.count)
        TestCase.assertEquals(1, Staple.ONE_UL.count)
        TestCase.assertEquals(2, Staple.TWO.count)
    }

    @Test
    fun testPunchValueOf() {
        TestCase.assertEquals(Punch.OFF, Punch.valueOf("OFF"))
    }

    @Test
    fun testPunchGetCount() {
        TestCase.assertEquals(0, Punch.OFF.getCount(false))
        TestCase.assertEquals(2, Punch.HOLES_2.getCount(false))
        TestCase.assertEquals(4, Punch.HOLES_4.getCount(false))
    }

    @Test
    fun testPunchGetCount_JapanesePrinter() {
        TestCase.assertEquals(0, Punch.OFF.getCount(true))
        TestCase.assertEquals(2, Punch.HOLES_2.getCount(true))
        TestCase.assertEquals(3, Punch.HOLES_4.getCount(true))
    }

    @Test
    fun testOutputTray() {
        TestCase.assertEquals(0, OutputTray.AUTO.ordinal)
        TestCase.assertEquals(1, OutputTray.FACEDOWN.ordinal)
        TestCase.assertEquals(2, OutputTray.TOP.ordinal)
        TestCase.assertEquals(3, OutputTray.STACKING.ordinal)
        TestCase.assertEquals(OutputTray.AUTO, OutputTray.valueOf("AUTO"))
        TestCase.assertEquals(OutputTray.FACEDOWN, OutputTray.valueOf("FACEDOWN"))
        TestCase.assertEquals(OutputTray.TOP, OutputTray.valueOf("TOP"))
        TestCase.assertEquals(OutputTray.STACKING, OutputTray.valueOf("STACKING"))
    }

    @Test
    fun testColorMode_XML() {
        val optionsNodeList = _settingList!!.item(0).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(
            optionsList.size,
            ColorMode.values().size - 1
        ) // Default does not have dual color mode
    }

    @Test
    fun testOrientation_XML() {
        val optionsNodeList = _settingList!!.item(1).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, Orientation.values().size)
    }

    @Test
    fun testDuplex_XML() {
        val optionsNodeList = _settingList!!.item(3).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, Duplex.values().size)
    }

    @Test
    fun testPaperSize_XML() {
        val optionsNodeList = _settingList!!.item(4).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(
            optionsList.size,
            PaperSize.values().size - 4
        ) // Only tests IS
    }

    @Test
    fun testImposition_XML() {
        val optionsNodeList = _settingList!!.item(8).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, Imposition.values().size)
    }

    @Test
    fun testImpositionOrder_XML() {
        val optionsNodeList = _settingList!!.item(9).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, ImpositionOrder.values().size)
    }

    @Test
    fun testSort_XML() {
        val optionsNodeList = _settingList!!.item(10).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, Sort.values().size)
    }

    @Test
    fun testBookletFinish_XML() {
        val optionsNodeList = _settingList!!.item(12).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, BookletFinish.values().size)
    }

    @Test
    fun testBookletLayout_XML() {
        val optionsNodeList = _settingList!!.item(13).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, BookletLayout.values().size)
    }

    @Test
    fun testFinishingSide_XML() {
        val optionsNodeList = _settingList!!.item(14).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, FinishingSide.values().size)
    }

    @Test
    fun testStaple_XML() {
        val optionsNodeList = _settingList!!.item(15).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, Staple.values().size)
    }

    @Test
    fun testPunch_XML() {
        val optionsNodeList = _settingList!!.item(16).childNodes
        val optionsList: MutableList<Option> = ArrayList()
        var i = 1
        while (i < optionsNodeList.length) {
            optionsList.add(Option(optionsNodeList.item(i)))
            i += 2
        }
        TestCase.assertEquals(optionsList.size, Punch.values().size)
    }

    companion object {
        private const val TAG = "PreviewTest"
    }
}