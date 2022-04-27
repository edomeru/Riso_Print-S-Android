package jp.co.riso.smartdeviceapp.model.printsettings

import android.util.Log
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class GroupTest {

    private var _groupListMap: HashMap<String, List<Group>>? = null

    @Before
    fun setUp() {
        _groupListMap = HashMap()
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
        for (printerType in AppConstants.PRINTER_TYPES) {
            val settings = printSettingsContent.getElementById(printerType)
            val groupNodeList = settings.getElementsByTagName(XmlNode.NODE_GROUP)
            val groupList: MutableList<Group> = ArrayList()

            // looping through all item nodes <item>
            for (i in 0 until groupNodeList.length) {
                val group = Group(groupNodeList.item(i))
                groupList.add(group)
                _groupListMap!![printerType] = groupList
            }
        }
    }

    @After
    fun tearDown() {
        _groupListMap!!.clear()
    }

    @Test
    fun testPreConditions() {
        for (printerType in AppConstants.PRINTER_TYPES) {
            TestCase.assertEquals(3, _groupListMap!![printerType]!!.size)
        }
    }

    @Test
    fun testGetSettings() {
        for (printerType in AppConstants.PRINTER_TYPES) {
            val groupList = _groupListMap!![printerType]!!
            for (i in groupList.indices) {
                val g = groupList[i]
                val attrib = g.getAttributeValue("name")
                when (i) {
                    0 -> {
                        TestCase.assertEquals("basic", attrib)
                        TestCase.assertNotNull(g.settings)
                        TestCase.assertEquals(8, g.settings.size)
                    }
                    1 -> {
                        TestCase.assertEquals("layout", attrib)
                        TestCase.assertNotNull(g.settings)
                        TestCase.assertEquals(2, g.settings.size)
                    }
                    2 -> {
                        TestCase.assertEquals("finishing", attrib)
                        TestCase.assertNotNull(g.settings)
                        TestCase.assertEquals(8, g.settings.size)
                    }
                    else -> TestCase.fail("invalid group")
                }
            }
            TestCase.assertEquals("colorMode", groupList[0].settings[0].getAttributeValue("name"))
        }
    }

    companion object {
        private const val TAG = "GroupTest"
    }
}