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

public class OptionTest extends AndroidTestCase {
    private static final String TAG = "OptionTest";
    private List<Option> mOptionList = new ArrayList<Option>();

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

        NodeList optionList = printSettingsContent.getElementsByTagName("option");

        // looping through all item nodes <item>
        for (int i = 0; i < optionList.getLength(); i++) {
            Option option = new Option(optionList.item(i));
            mOptionList.add(option);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mOptionList.clear();
    }

    public void testPreConditions() {
        assertEquals(64, mOptionList.size());
    }

    public void testGetTextContent() {
        assertEquals("ids_lbl_colormode_auto", mOptionList.get(0).getTextContent());
        assertEquals("ids_lbl_colormode_fullcolor", mOptionList.get(1).getTextContent());
        assertEquals("ids_lbl_colormode_black", mOptionList.get(2).getTextContent());
        assertEquals("ids_lbl_orientation_portrait", mOptionList.get(3).getTextContent());
    }
}


