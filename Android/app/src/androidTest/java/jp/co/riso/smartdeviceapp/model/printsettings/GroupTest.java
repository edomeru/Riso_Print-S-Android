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

public class GroupTest extends AndroidTestCase {
    private static final String TAG = "GroupTest";
    private List<Group> mGroupList = new ArrayList<Group>();

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

        NodeList groupList = printSettingsContent.getElementsByTagName(XmlNode.NODE_GROUP);

        // looping through all item nodes <item>
        for (int i = 0; i < groupList.getLength(); i++) {
            Group group = new Group(groupList.item(i));
            mGroupList.add(group);
        }
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mGroupList.clear();
    }

    public void testPreConditions() {
        assertEquals(3, mGroupList.size());
    }

    public void testGetSettings() {
        for (int i = 0; i < mGroupList.size(); i++) {
            Group g = mGroupList.get(i);
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

        assertEquals("colorMode", mGroupList.get(0).getSettings().get(0).getAttributeValue("name"));
    }
}
