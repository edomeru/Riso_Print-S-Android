
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
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletFinish;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletLayout;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ColorMode;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.FinishingSide;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Imposition;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ImpositionOrder;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Orientation;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.OutputTray;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Punch;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Sort;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Staple;

public class PreviewTest extends AndroidTestCase {
    private static final String TAG = "PreviewTest";
    private NodeList mSettingList;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String xmlString = AppUtils.getFileContentsFromAssets(SmartDeviceApp.getAppContext(),
                AppConstants.XML_FILENAME);

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

        mSettingList = printSettingsContent.getElementsByTagName("setting");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor() {
        assertNotNull(new Preview());
    }

    public void testColorMode() {
        assertEquals(0, ColorMode.AUTO.ordinal());
        assertEquals(1, ColorMode.FULL_COLOR.ordinal());
        assertEquals(2, ColorMode.MONOCHROME.ordinal());
        assertEquals(3, ColorMode.DUAL_COLOR.ordinal());
        assertEquals(ColorMode.AUTO, ColorMode.valueOf("AUTO"));
        assertEquals(ColorMode.FULL_COLOR, ColorMode.valueOf("FULL_COLOR"));
        assertEquals(ColorMode.MONOCHROME, ColorMode.valueOf("MONOCHROME"));
    }

    public void testOrientation() {
        assertEquals(0, Orientation.PORTRAIT.ordinal());
        assertEquals(1, Orientation.LANDSCAPE.ordinal());
        assertEquals(Orientation.PORTRAIT, Orientation.valueOf("PORTRAIT"));
        assertEquals(Orientation.LANDSCAPE, Orientation.valueOf("LANDSCAPE"));
    }

    public void testDuplex() {
        assertEquals(0, Duplex.OFF.ordinal());
        assertEquals(1, Duplex.LONG_EDGE.ordinal());
        assertEquals(2, Duplex.SHORT_EDGE.ordinal());
        assertEquals(Duplex.OFF, Duplex.valueOf("OFF"));
        assertEquals(Duplex.LONG_EDGE, Duplex.valueOf("LONG_EDGE"));
        assertEquals(Duplex.SHORT_EDGE, Duplex.valueOf("SHORT_EDGE"));
    }

    public void testPaperSizeValueOf() {
        assertEquals(PaperSize.A3, PaperSize.valueOf("A3"));
    }

    public void testPaperSizeGetWidth() {
        assertEquals(297.0f, PaperSize.A3.getWidth());
        assertEquals(210.0f, PaperSize.A4.getWidth());
    }

    public void testPaperSizeGetHeight() {
        assertEquals(420.0f, PaperSize.A3.getHeight());
        assertEquals(297.0f, PaperSize.A4.getHeight());
    }

    public void testImpositionValueOf() {
        assertEquals(Imposition.OFF, Imposition.valueOf("OFF"));
    }

    public void testImpositionGetPerPage() {
        assertEquals(1, Imposition.OFF.getPerPage());
    }

    public void testImpositionGetRows() {
        assertEquals(1, Imposition.OFF.getRows());
    }

    public void testImpositionGetCols() {
        assertEquals(1, Imposition.OFF.getCols());
    }

    public void testImpositionIsFlipLandscape() {
        assertEquals(false, Imposition.OFF.isFlipLandscape());
    }

    public void testImpositionOrderIsLeftToRight() {
        assertEquals(true, ImpositionOrder.L_R.isLeftToRight());
    }

    public void testImpositionOrderIsTopToBottom() {
        assertEquals(true, ImpositionOrder.L_R.isTopToBottom());
    }

    public void testImpositionOrderIsHorizontalFlow() {
        assertEquals(true, ImpositionOrder.L_R.isHorizontalFlow());
    }

    public void testImpositionOrderValueOf() {
        assertEquals(ImpositionOrder.L_R, ImpositionOrder.valueOf("L_R"));
    }

    public void testSort() {
        assertEquals(0, Sort.PER_PAGE.ordinal());
        assertEquals(1, Sort.PER_COPY.ordinal());
        assertEquals(Sort.PER_PAGE, Sort.valueOf("PER_PAGE"));
        assertEquals(Sort.PER_COPY, Sort.valueOf("PER_COPY"));
    }

    public void testBookletFinish() {
        assertEquals(0, BookletFinish.OFF.ordinal());
        assertEquals(1, BookletFinish.PAPER_FOLDING.ordinal());
        assertEquals(2, BookletFinish.FOLD_AND_STAPLE.ordinal());
        assertEquals(BookletFinish.OFF, BookletFinish.valueOf("OFF"));
        assertEquals(BookletFinish.PAPER_FOLDING, BookletFinish.valueOf("PAPER_FOLDING"));
        assertEquals(BookletFinish.FOLD_AND_STAPLE, BookletFinish.valueOf("FOLD_AND_STAPLE"));
    }

    public void testBookletLayout() {
        assertEquals(0, BookletLayout.FORWARD.ordinal());
        assertEquals(1, BookletLayout.REVERSE.ordinal());
        assertEquals(BookletLayout.FORWARD, BookletLayout.valueOf("FORWARD"));
        assertEquals(BookletLayout.REVERSE, BookletLayout.valueOf("REVERSE"));
    }

    public void testFinishingSide() {
        assertEquals(0, FinishingSide.LEFT.ordinal());
        assertEquals(1, FinishingSide.TOP.ordinal());
        assertEquals(2, FinishingSide.RIGHT.ordinal());
        assertEquals(FinishingSide.LEFT, FinishingSide.valueOf("LEFT"));
        assertEquals(FinishingSide.TOP, FinishingSide.valueOf("TOP"));
        assertEquals(FinishingSide.RIGHT, FinishingSide.valueOf("RIGHT"));
    }

    public void testStapleValueOf() {
        assertEquals(Staple.OFF, Staple.valueOf("OFF"));
    }

    public void testStapleGetCount() {
        assertEquals(0, Staple.OFF.getCount());
        assertEquals(1, Staple.ONE_UL.getCount());
        assertEquals(2, Staple.TWO.getCount());
    }

    public void testPunchValueOf() {
        assertEquals(Punch.OFF, Punch.valueOf("OFF"));
    }

    public void testPunchGetCount() {
        assertEquals(0, Punch.OFF.getCount(false));
        assertEquals(2, Punch.HOLES_2.getCount(false));
        assertEquals(4, Punch.HOLES_4.getCount(false));
    }

    public void testPunchGetCount_JapanesePrinter() {
        assertEquals(0, Punch.OFF.getCount(true));
        assertEquals(2, Punch.HOLES_2.getCount(true));
        assertEquals(3, Punch.HOLES_4.getCount(true));
    }

    public void testOutputTray() {
        assertEquals(0, OutputTray.AUTO.ordinal());
        assertEquals(1, OutputTray.FACEDOWN.ordinal());
        assertEquals(2, OutputTray.TOP.ordinal());
        assertEquals(3, OutputTray.STACKING.ordinal());
        assertEquals(OutputTray.AUTO, OutputTray.valueOf("AUTO"));
        assertEquals(OutputTray.FACEDOWN, OutputTray.valueOf("FACEDOWN"));
        assertEquals(OutputTray.TOP, OutputTray.valueOf("TOP"));
        assertEquals(OutputTray.STACKING, OutputTray.valueOf("STACKING"));
    }

    public void testColorMode_XML() {
        NodeList optionsNodeList  = mSettingList.item(0).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();
        
        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), ColorMode.values().length - 1); // Default does not have dual color mode

    }

    public void testOrientation_XML() {
        NodeList optionsNodeList  = mSettingList.item(1).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), Orientation.values().length);
    }

    public void testDuplex_XML() {
        NodeList optionsNodeList  = mSettingList.item(3).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), Duplex.values().length);
    }

    public void testPaperSize_XML() {
        NodeList optionsNodeList  = mSettingList.item(4).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), PaperSize.values().length-3); // Only tests IS
    }

    public void testImposition_XML() {
        NodeList optionsNodeList  = mSettingList.item(8).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), Imposition.values().length);
    }

    public void testImpositionOrder_XML() {
        NodeList optionsNodeList  = mSettingList.item(9).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), ImpositionOrder.values().length);
    }

    public void testSort_XML() {
        NodeList optionsNodeList  = mSettingList.item(10).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), Sort.values().length);
    }

    public void testBookletFinish_XML() {
        NodeList optionsNodeList  = mSettingList.item(12).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), BookletFinish.values().length);
    }

    public void testBookletLayout_XML() {
        NodeList optionsNodeList  = mSettingList.item(13).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();
;
        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), BookletLayout.values().length);
    }

    public void testFinishingSide_XML() {
        NodeList optionsNodeList  = mSettingList.item(14).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), FinishingSide.values().length);
    }

    public void testStaple_XML() {
        NodeList optionsNodeList  = mSettingList.item(15).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), Staple.values().length);
    }

    public void testPunch_XML() {
        NodeList optionsNodeList  = mSettingList.item(16).getChildNodes();
        List<Option> optionsList = new ArrayList<Option>();

        for (int i = 1; i < optionsNodeList.getLength(); i += 2) {
            optionsList.add(new Option(optionsNodeList.item(i)));
        }
        assertEquals(optionsList.size(), Punch.values().length);
    }

}
