package jp.co.riso.smartdeviceapp.model.printsettings;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

public class PrintSettings {
    public static final String TAG = "PrintSettings";
    
    public static List<Group> sGroupList;
    public static HashMap<String, Setting> sSettingMap; // ConvenienceHashMap
    
    private HashMap<String, Integer> mSettingValues; 
    
    /**
     * Default Print Settings Constructor
     */
    public PrintSettings() {
        mSettingValues = new HashMap<String, Integer>();
        
        for (String key : PrintSettings.sSettingMap.keySet()) {
            Setting setting = PrintSettings.sSettingMap.get(key);
            
            mSettingValues.put(key, setting.getDefaultValue());
        }
    }
    
    public PrintSettings(PrintSettings printSettings) {
        mSettingValues = new HashMap<String, Integer>();
        
        for (String key : printSettings.getSettingValues().keySet()) {
            mSettingValues.put(key, printSettings.getSettingValues().get(key));
        }
    }
    
    public PrintSettings(int printerId) {
        this();
    }
    
    private static void initializeStaticObjects() {
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
        
        parsePrintSettings(printSettingsContent);
    }
    
    private static void parsePrintSettings(Document printSettingsContent) {
        if (printSettingsContent == null) {
            return;
        }
        
        sGroupList = new ArrayList<Group>(); 
        sSettingMap = new HashMap<String, Setting>();
        
        NodeList groupList = printSettingsContent.getElementsByTagName("group");
        
        // looping through all item nodes <item>
        for (int i = 0; i < groupList.getLength(); i++) {
            Group group = new Group(groupList.item(i));
            sGroupList.add(group);
            
            for (Setting setting : group.getSettings()) {
                sSettingMap.put(setting.getAttributeValue("name"), setting);
            }
        }
    }
    
    // ================================================================================
    // Getter for int values
    // ================================================================================
    
    public HashMap<String, Integer> getSettingValues() {
        return mSettingValues;
    }
    
    public int getValue(String key) {
        return mSettingValues.get(key);
    }
    
    public boolean setValue(String key, int value) {
        mSettingValues.put(key, value);
        return true;
    }
    
    // ================================================================================
    // Getter for PrintPreview
    // ================================================================================

    /**
     * @return the ColorMode
     */
    public ColorMode getColorMode() {
        return ColorMode.values()[mSettingValues.get("colorMode")];
    }
    
    /**
     * @return the Orientation
     */
    public Orientation getOrientation() {
        return Orientation.values()[mSettingValues.get("orientation")];
    }
    
    /**
     * @return the mCopies
     */
    public int getCopies() {
        return mSettingValues.get("numCopies");
    }
    
    /**
     * @return the mDuplex
     */
    public Duplex getDuplex() {
        return Duplex.values()[mSettingValues.get("duplex")];
    }
    
    /**
     * @return the mPaperSize
     */
    public PaperSize getPaperSize() {
        return PaperSize.values()[mSettingValues.get("paperSize")];
    }
    
    /**
     * @return the mScaleToFit
     */
    public boolean isScaleToFit() {
        return (mSettingValues.get("scaleToFit") == 1) ? true : false;
    }
    
    /**
     * @return the mPaperType
     */
    public PaperType getPaperType() {
        return PaperType.values()[mSettingValues.get("paperType")];
    }
    
    /**
     * @return the mInputTray
     */
    public InputTray getInputTray() {
        return InputTray.values()[mSettingValues.get("inputTray")];
    }
    
    /**
     * @return the mImposition
     */
    public Imposition getImposition() {
        return Imposition.values()[mSettingValues.get("imposition")];
    }
    
    /**
     * @return the mImpositionOrder
     */
    public ImpositionOrder getImpositionOrder() {
        return ImpositionOrder.values()[mSettingValues.get("impositionOrder")];
    }
    
    /**
     * @return the mSort
     */
    public Sort getSort() {
        return Sort.values()[mSettingValues.get("ids_lbl_sort")];
    }
    /**
     * @return the mBooklet
     */
    public boolean isBooklet() {
        return (mSettingValues.get("booklet") == 1) ? true : false;
    }
    
    /**
     * @return the mBookletFinish
     */
    public BookletFinish getBookletFinish() {
        return BookletFinish.values()[mSettingValues.get("bookletFinish")];
    }
    
    /**
     * @return the mBookletLayout
     */
    public BookletLayout getBookletLayout() {
        return BookletLayout.values()[mSettingValues.get("bookletLayout")];
    }
    
    /**
     * @return the mFinishingSide
     */
    public FinishingSide getFinishingSide() {
        return FinishingSide.values()[mSettingValues.get("finishingSide")];
    }
    
    /**
     * @return the mStaple
     */
    public Staple getStaple() {
        return Staple.values()[mSettingValues.get("staple")];
    }
    
    /**
     * @return the mPunch
     */
    public Punch getPunch() {
        return Punch.values()[mSettingValues.get("punch")];
    }
    
    /**
     * @return the mOutputTray
     */
    public OutputTray getOutputTray() {
        return OutputTray.values()[mSettingValues.get("outputTray")];
    }
    

    public enum ColorMode {
        AUTO, FULL_COLOR, MONOCHROME;
    }
    
    public enum Orientation {
        PORTRAIT, LANDSCAPE;
    }
    
    public enum Duplex {
        OFF, LONG_EDGE, SHORT_EDGE;
    }
    
    // sizes from http://en.wikipedia.org/wiki/Paper_size
    public enum PaperSize {
        A3W(297.0f, 420.0f),
        A4(210.0f, 297.0f),
        A5(300.0f, 400.0f),
        A6(300.0f, 400.0f),
        B4(300.0f, 400.0f),
        B5(300.0f, 400.0f),
        FOOLSCAP(300.0f, 400.0f),
        TABLOID(300.0f, 400.0f),
        LEGAL(215.9f, 355.6f),
        LETTER(215.9f, 279.4f),
        STATEMENT(300.0f, 400.0f);
        
        private final float mWidth;
        private final float mHeight;
        
        PaperSize(float width, float height) {
            mWidth = width;
            mHeight = height;
        }
        
        public float getWidth() {
            return mWidth;
        }
        
        public float getHeight() {
            return mHeight;
        }
    }
    
    public enum PaperType {
        ANY, PLAIN, IJ_PAPER, MATTE, HIGH_QUALITY, CARD_IJ, LW_PAPER;
    }
    
    public enum InputTray {
        AUTO, STANDARD, TRAY_1, TRAY_2, TRAY_3;
    }
    
    public enum Imposition {
        OFF (1, 1, 1, false),
        TWO_UP (2, 2, 1, true),
        FOUR_UP (4, 2, 2, false);
        
        private final int mPerPage;
        private final int mRows;
        private final int mCols;
        private final boolean mFlipLandscape;
        
        Imposition(int perPage, int x, int y, boolean flipLandscape) {
            mPerPage = perPage;
            mCols = x;
            mRows = y;
            mFlipLandscape = flipLandscape;
        }
        
        public int getPerPage() {
            return mPerPage;
        }
        
        public int getRows() {
            return mRows;
        }
        
        public int getCols() {
            return mCols;
        }
        
        public boolean isFlipLandscape() {
            return mFlipLandscape;
        }
    }
    
    public enum ImpositionOrder {
        R_L, L_R, TL_B, TL_R, TR_B, TR_L;
    }
    
    public enum Sort {
        PER_PAGE, PER_COPY;
    }
    
    public enum BookletFinish {
        PAPER_FOLDING, FOLD_AND_STAPLE;
    }
    
    public enum BookletLayout {
        L_R, R_L, T_B;
    }
    
    public enum FinishingSide {
        LEFT, RIGHT, TOP;
    }
    
    public enum Staple {
        OFF, UL, UR, SIDE_2, SIDE_1;
    }
    
    public enum Punch {
        OFF, HOLES_2, HOLES_3, HOLES_4;
    }
    
    public enum OutputTray {
        AUTO, FACEDOWN, FACEUP, TOP, STACKING;
    }
    
    static {
        initializeStaticObjects();
    }
}
