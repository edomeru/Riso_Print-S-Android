package jp.co.riso.smartdeviceapp.model.printsettings;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.test.AndroidTestCase;
import android.util.Log;

public class SettingTest extends AndroidTestCase {
    private static final String TAG = "SettingTest";
    private List<Setting> mSettingList = new ArrayList<Setting>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String xmlString = AppUtils.getFileContentsFromAssets(SmartDeviceApp.getAppContext(), "printsettings.xml");

        Document printSettingsContent = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            printSettingsContent = db.parse(is);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        } catch (SAXException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }

        if (printSettingsContent == null) {
            return;
        }

        NodeList settingList = printSettingsContent.getElementsByTagName("setting");

        // looping through all item nodes <item>
        for (int i = 0; i < settingList.getLength(); i++) {
            Setting setting = new Setting(settingList.item(i));
            mSettingList.add(setting);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testPreConditions() {
        assertEquals(18, mSettingList.size());
    }

    public void testGetOptions() {
        assertEquals(3, mSettingList.get(0).getOptions().size()); //colormode
        assertEquals(2, mSettingList.get(1).getOptions().size()); //orientation
        assertEquals(0, mSettingList.get(2).getOptions().size()); //copies
        assertEquals(0, mSettingList.get(5).getOptions().size()); //scaletofit
    }

    public void testGetType() {
        assertEquals(Setting.TYPE_LIST, mSettingList.get(0).getType()); //colormode
        assertEquals(Setting.TYPE_NUMERIC, mSettingList.get(2).getType()); //copies
        assertEquals(Setting.TYPE_BOOLEAN, mSettingList.get(5).getType());//scaletofit
    }

    public void testGetDefaultValue() {
        assertEquals(1, (int) mSettingList.get(0).getDefaultValue()); //colormode
        assertEquals(1, (int) mSettingList.get(2).getDefaultValue()); //copies
        assertEquals(1, (int) mSettingList.get(5).getDefaultValue()); //scaletofit
    }

    public void testGetDbKey() {
        assertEquals("pst_color_mode", mSettingList.get(0).getDbKey()); //colormode
        assertEquals("pst_copies", mSettingList.get(2).getDbKey()); //copies
        assertEquals("pst_scale_to_fit", mSettingList.get(5).getDbKey()); //scaletofit
    }

}
