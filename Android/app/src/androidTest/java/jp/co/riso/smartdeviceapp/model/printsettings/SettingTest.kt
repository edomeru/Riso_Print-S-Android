package jp.co.riso.smartdeviceapp.model.printsettings

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class SettingTest {

    private val _settingList: MutableList<Setting> = ArrayList()

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
        val settingList = printSettingsContent.getElementsByTagName("setting")

        // looping through all item nodes <item>
        for (i in 0 until settingList.length) {
            val setting = Setting(settingList.item(i))
            _settingList.add(setting)
        }
    }

    @Test
    fun testPreConditions() {
        TestCase.assertEquals(18 * AppConstants.PRINTER_TYPES.size, _settingList.size)
    }

    @Test
    fun testGetOptions() {
        TestCase.assertEquals(3, _settingList[0].options.size) //colormode
        TestCase.assertEquals(2, _settingList[1].options.size) //orientation
        TestCase.assertEquals(0, _settingList[2].options.size) //copies
        TestCase.assertEquals(0, _settingList[5].options.size) //scaletofit
    }

    @Test
    fun testGetType() {
        TestCase.assertEquals(Setting.TYPE_LIST, _settingList[0].type) //colormode
        TestCase.assertEquals(Setting.TYPE_NUMERIC, _settingList[2].type) //copies
        TestCase.assertEquals(Setting.TYPE_BOOLEAN, _settingList[5].type) //scaletofit
    }

    @Test
    fun testGetDefaultValue() {
        TestCase.assertEquals(0, _settingList[0].defaultValue as Int) //colormode
        TestCase.assertEquals(1, _settingList[2].defaultValue as Int) //copies
        TestCase.assertEquals(1, _settingList[5].defaultValue as Int) //scaletofit
    }

    @Test
    fun testGetDefaultValue_Invalid() {
        val xmlString = getFileContentsFromAssets("printsettings_invalidType.xml")
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
        TestCase.assertNotNull(printSettingsContent)
        val settingList = printSettingsContent!!.getElementsByTagName("setting")
        val invalidSettingList: MutableList<Setting> = ArrayList()
        // looping through all item nodes <item>
        for (i in 0 until settingList.length) {
            val setting = Setting(settingList.item(i))
            invalidSettingList.add(setting)
        }
        TestCase.assertEquals(1, invalidSettingList.size)
        TestCase.assertEquals(-1, invalidSettingList[0].defaultValue as Int)
    }

    @Test
    fun testGetType_Invalid() {
        val xmlString = getFileContentsFromAssets("printsettings_invalidType.xml")
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
        TestCase.assertNotNull(printSettingsContent)
        val settingList = printSettingsContent!!.getElementsByTagName("setting")
        val invalidSettingList: MutableList<Setting> = ArrayList()
        // looping through all item nodes <item>
        for (i in 0 until settingList.length) {
            val setting = Setting(settingList.item(i))
            invalidSettingList.add(setting)
        }
        TestCase.assertEquals(1, invalidSettingList.size)
        TestCase.assertEquals(-1, invalidSettingList[0].type)
    }

    @Test
    fun testGetDbKey() {
        TestCase.assertEquals("pst_color_mode", _settingList[0].dbKey) //colormode
        TestCase.assertEquals("pst_copies", _settingList[2].dbKey) //copies
        TestCase.assertEquals("pst_scale_to_fit", _settingList[5].dbKey) //scaletofit
    }

    // ================================================================================
    // Private methods
    // ================================================================================
    private fun getFileContentsFromAssets(assetFile: String): String? {
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        val buf = StringBuilder()
        val stream: InputStream
        try {
            stream = assetManager.open(assetFile)
            val `in` = BufferedReader(InputStreamReader(stream))
            var str: String?
            while (`in`.readLine().also { str = it } != null) {
                buf.append(str)
            }
            `in`.close()
        } catch (e: IOException) {
            return null
        }
        return buf.toString()
    }

    companion object {
        private const val TAG = "SettingTest"
    }
}