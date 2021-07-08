package jp.co.riso.smartdeviceapp.model.printsettings;

import android.test.AndroidTestCase;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

public class XmlNodeTest extends AndroidTestCase {
    private static final String TAG = "XmlNodeTest";
    private List<XmlNode> mGroupList = new ArrayList<XmlNode>();
    private List<XmlNode> mOptionList = new ArrayList<XmlNode>();
    private List<XmlNode> mSettingList = new ArrayList<XmlNode>();

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
        } catch (ParserConfigurationException | IOException | SAXException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }

        if (printSettingsContent == null) {
            return;
        }

        NodeList groupList = printSettingsContent.getElementsByTagName(XmlNode.NODE_GROUP);

        // looping through all item nodes <item>
        for (int i = 0; i < groupList.getLength(); i++) {
            XmlNode group = new Group(groupList.item(i));
            mGroupList.add(group);
        }


        NodeList optionList = printSettingsContent.getElementsByTagName("option");

        // looping through all item nodes <item>
        for (int i = 0; i < groupList.getLength(); i++) {
            XmlNode option = new Option(optionList.item(i));
            mOptionList.add(option);
        }


        NodeList settingList = printSettingsContent.getElementsByTagName("setting");

        // looping through all item nodes <item>
        for (int i = 0; i < groupList.getLength(); i++) {
            XmlNode setting = new Setting(settingList.item(i));
            mSettingList.add(setting);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testPreConditions() {
        assertEquals(3*AppConstants.PRINTER_TYPES.length, mGroupList.size());
    }

    public void testGetAttributeValue() {
        for (int i = 0; i < mGroupList.size(); i++) {
            XmlNode g = mGroupList.get(i);
            String attribName = g.getAttributeValue("name");
            String attribText = g.getAttributeValue("text");
            switch(i%(AppConstants.PRINTER_TYPES.length-2)) {
                case 0:
                    assertEquals("basic", attribName);
                    assertEquals("ids_lbl_basic", attribText);
                    break;
                case 1:
                    assertEquals("layout", attribName);
                    assertEquals("ids_lbl_layout", attribText);
                    break;
                case 2:
                    assertEquals("finishing", attribName);
                    assertEquals("ids_lbl_finishing", attribText);
                    break;
                default:
                    fail("invalid group");
            }
        }

        assertEquals("", mOptionList.get(0).getAttributeValue("name"));
        assertEquals("colorMode", mSettingList.get(0).getAttributeValue("name"));
    }
    
    public void testGetAttributeValue_NullValues() {
        assertEquals("", mGroupList.get(0).getAttributeValue(null));
        assertEquals("", mOptionList.get(0).getAttributeValue(null));
        assertEquals("", mSettingList.get(0).getAttributeValue(null));
    }

}
