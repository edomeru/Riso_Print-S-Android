package jp.co.riso.smartdeviceapp.model.printsettings

import android.util.Log
import jp.co.riso.android.util.AppUtils
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

class OptionTest {

    private val _optionList: MutableList<Option> = ArrayList()

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
        val optionList = printSettingsContent.getElementsByTagName("option")

        // looping through all item nodes <item>
        for (i in 0 until optionList.length) {
            val option = Option(optionList.item(i))
            _optionList.add(option)
        }
    }

    @After
    fun tearDown() {
        _optionList.clear()
    }

    @Test
    fun testPreConditions() {
        TestCase.assertEquals(345, _optionList.size)
    }

    @Test
    fun testGetTextContent() {
        TestCase.assertEquals("ids_lbl_colormode_auto", _optionList[0].textContent)
        TestCase.assertEquals("ids_lbl_colormode_fullcolor", _optionList[1].textContent)
        TestCase.assertEquals("ids_lbl_colormode_black", _optionList[2].textContent)
        TestCase.assertEquals("ids_lbl_orientation_portrait", _optionList[3].textContent)
    }

    companion object {
        private const val TAG = "OptionTest"
    }
}