package jp.co.riso.smartdeviceapp.model.printsettings;

import android.test.AndroidTestCase;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

public class GroupTest extends AndroidTestCase {
    private static final String TAG = "GroupTest";
    public HashMap<String, List<Group>> mGroupListMap;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mGroupListMap = new HashMap<>();
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

        for(String printerType : AppConstants.PRINTER_TYPES) {
            Element settings = printSettingsContent.getElementById(printerType);
            NodeList groupNodeList = settings.getElementsByTagName(XmlNode.NODE_GROUP);
            List<Group> groupList = new ArrayList<>();

            // looping through all item nodes <item>
            for (int i = 0; i < groupNodeList.getLength(); i++) {
                Group group = new Group(groupNodeList.item(i));
                groupList.add(group);
                mGroupListMap.put(printerType, groupList);
            }
        }
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mGroupListMap.clear();
    }

    public void testPreConditions() {
        for(String printerType : AppConstants.PRINTER_TYPES) {
            assertEquals(3, mGroupListMap.get(printerType).size());
        }
    }

    public void testGetSettings() {
        for(String printerType : AppConstants.PRINTER_TYPES) {
            List<Group> groupList = mGroupListMap.get(printerType);

            for (int i = 0; i < groupList.size(); i++) {
                Group g = groupList.get(i);
                String attrib = g.getAttributeValue("name");
                switch(i) {
                    case 0:
                        assertEquals("basic", attrib);
                        assertNotNull(g.getSettings());
                        assertEquals(8, g.getSettings().size());
                        break;
                    case 1:
                        assertEquals("layout", attrib);
                        assertNotNull(g.getSettings());
                        assertEquals(2, g.getSettings().size());
                        break;
                    case 2:
                        assertEquals("finishing", attrib);
                        assertNotNull(g.getSettings());
                        assertEquals(8, g.getSettings().size());
                        break;
                    default:
                        fail("invalid group");
                }
            }

            assertEquals("colorMode", groupList.get(0).getSettings().get(0).getAttributeValue("name"));
        }
    }
}
