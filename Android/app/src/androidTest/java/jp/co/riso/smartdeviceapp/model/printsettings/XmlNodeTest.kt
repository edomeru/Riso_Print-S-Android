package jp.co.riso.smartdeviceapp.model.printsettings

import android.util.Log
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class XmlNodeTest {

    private val _groupList: MutableList<XmlNode> = ArrayList()
    private val _optionList: MutableList<XmlNode> = ArrayList()
    private val _settingList: MutableList<XmlNode> = ArrayList()

    @Before
    fun setUp() {
        val xmlString =
            AppUtils.getFileContentsFromAssets(SmartDeviceApp.getAppContext(), "printsettings.xml")
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
        val groupList = printSettingsContent.getElementsByTagName(XmlNode.NODE_GROUP)

        // looping through all item nodes <item>
        for (i in 0 until groupList.length) {
            val group: XmlNode = Group(groupList.item(i))
            _groupList.add(group)
        }
        val optionList = printSettingsContent.getElementsByTagName("option")

        // looping through all item nodes <item>
        for (i in 0 until groupList.length) {
            val option: XmlNode = Option(optionList.item(i))
            _optionList.add(option)
        }
        val settingList = printSettingsContent.getElementsByTagName("setting")

        // looping through all item nodes <item>
        for (i in 0 until groupList.length) {
            val setting: XmlNode = Setting(settingList.item(i))
            _settingList.add(setting)
        }
    }

    @Test
    fun testPreConditions() {
        TestCase.assertEquals(3 * AppConstants.PRINTER_TYPES.size, _groupList.size)
    }

    @Test
    fun testGetAttributeValue() {
        for (i in _groupList.indices) {
            val g = _groupList[i]
            val attribName = g.getAttributeValue("name")
            val attribText = g.getAttributeValue("text")
            when (i % (AppConstants.PRINTER_TYPES.size - 2)) {
                0 -> {
                    TestCase.assertEquals("basic", attribName)
                    TestCase.assertEquals("ids_lbl_basic", attribText)
                }
                1 -> {
                    TestCase.assertEquals("layout", attribName)
                    TestCase.assertEquals("ids_lbl_layout", attribText)
                }
                2 -> {
                    TestCase.assertEquals("finishing", attribName)
                    TestCase.assertEquals("ids_lbl_finishing", attribText)
                }
                else -> TestCase.fail("invalid group")
            }
        }
        TestCase.assertEquals("", _optionList[0].getAttributeValue("name"))
        TestCase.assertEquals("colorMode", _settingList[0].getAttributeValue("name"))
    }

    @Test
    fun testGetAttributeValue_NullValues() {
        TestCase.assertEquals("", _groupList[0].getAttributeValue(null))
        TestCase.assertEquals("", _optionList[0].getAttributeValue(null))
        TestCase.assertEquals("", _settingList[0].getAttributeValue(null))
    }

    companion object {
        private const val TAG = "XmlNodeTest"
    }
}