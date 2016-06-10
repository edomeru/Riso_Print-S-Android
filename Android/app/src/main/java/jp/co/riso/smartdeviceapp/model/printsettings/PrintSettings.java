/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettings.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

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
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.controller.printsettings.PrintSettingsManager;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletFinish;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletLayout;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ColorMode;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.FinishingSide;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Imposition;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ImpositionOrder;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Orientation;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Punch;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Sort;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Staple;

/**
 * @class PrintSettings
 * 
 * @brief PrintSettings class representing the printer settings data.
 */
public class PrintSettings {
    public static final String TAG_COLOR_MODE = "colorMode"; ///< Tag used to identify ColorMode settings
    public static final String TAG_ORIENTATION = "orientation"; ///< Tag used to identify Orientation settings
    public static final String TAG_COPIES = "copies"; ///< Tag used to identify Copies settings
    public static final String TAG_DUPLEX = "duplex"; ///< Tag used to identify Duplex settings
    public static final String TAG_PAPER_SIZE = "paperSize"; ///< Tag used to identify PaperSize settings
    public static final String TAG_SCALE_TO_FIT = "scaleToFit"; ///< Tag used to identify ScaleToFit settings
    public static final String TAG_PAPER_TYPE = "paperType"; ///< Tag used to identify PaperType settings
    public static final String TAG_INPUT_TRAY = "inputTray"; ///< Tag used to identify InputTray settings
    public static final String TAG_IMPOSITION = "imposition"; ///< Tag used to identify Imposition settings
    public static final String TAG_IMPOSITION_ORDER = "impositionOrder"; ///< Tag used to identify ImpositionOrder settings
    public static final String TAG_SORT = "sort"; ///< Tag used to identify Sort settings
    public static final String TAG_BOOKLET = "booklet"; ///< Tag used to identify Booklet settings
    public static final String TAG_BOOKLET_FINISH = "bookletFinish"; ///< Tag used to identify BookletFinish settings
    public static final String TAG_BOOKLET_LAYOUT = "bookletLayout"; ///< Tag used to identify BookletLayout settings
    public static final String TAG_FINISHING_SIDE = "finishingSide"; ///< Tag used to identify FinishingSide settings
    public static final String TAG_STAPLE = "staple"; ///< Tag used to identify Staple settings
    public static final String TAG_PUNCH = "punch"; ///< Tag used to identify Punch settings
    public static final String TAG_OUTPUT_TRAY = "outputTray"; ///< Tag used to identify OutputTray settings

    public static final HashMap<String, List<Group>> sGroupListMap;
    public static final HashMap<String, HashMap<String, Setting>> sSettingsMaps;
    
    private HashMap<String, Integer> mSettingValues;
    private String mSettingMapKey;
    
    /**
     * @brief Creates a PrintSettings instance using default values of print settings
     *         of the printer type
     *
     * @param printerType The type of printer used as key to the different set of settings.
     *                    based on printer type
     */
    public PrintSettings(String printerType) {
        mSettingValues = new HashMap<String, Integer>();
        mSettingMapKey = printerType;

        //Use IS as default settings map
        for (String key :  sSettingsMaps.get(printerType).keySet()) {
            Setting setting =  sSettingsMaps.get(printerType).get(key);
            
            mSettingValues.put(key, setting.getDefaultValue());
        }
    }

    /**
     * @brief Default constructor. Creates a PrintSettings instance using default values
     *          of print settings of the default printer type
     */
    public PrintSettings(){
        this(AppConstants.PRINTER_MODEL_IS);
    }

    /**
     * @brief Creates a PrintSettings instance from another existing PrintSettings instance.
     * 
     * @param printSettings Print settings to be copied
     */
    public PrintSettings(PrintSettings printSettings) {
        mSettingMapKey = printSettings.getSettingMapKey();
        mSettingValues = new HashMap<String, Integer>();
        
        for (String key : printSettings.getSettingValues().keySet()) {
            mSettingValues.put(key, printSettings.getSettingValues().get(key));
        }
    }
    
    /**
     * @brief Creates a PrintSettings instance from the database using printer ID.
     * 
     * If print settings is not existing in the database, default values are used.
     * 
     * @param printerId Printer ID of the Print Settings to be retrieved from the database.
     * @param printerType The type of printer used as key to determine the setting set to be used
     */
    public PrintSettings(int printerId, String printerType) {
        this(printerType);
        
        // overwrite values if valid printer id
        if (printerId != PrinterManager.EMPTY_ID) {
            PrintSettingsManager manager = PrintSettingsManager.getInstance(SmartDeviceApp.getAppContext());
            
            PrintSettings printSettings = manager.getPrintSetting(printerId, printerType);
            
            for (String key : printSettings.getSettingValues().keySet()) {
                mSettingValues.put(key, printSettings.getSettingValues().get(key));
            }
        }
    }
    
    /**
     * @brief Initialize static objects
     * 
     * @param xmlString Text content of the XML for initialization of PrintSettings values. 
     */
    protected static void initializeStaticObjects(String xmlString) {
        if (xmlString == null) {
            return;
        }
        
        Document printSettingsContent = null;
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            printSettingsContent = db.parse(is);
        } catch (ParserConfigurationException e) {
            Logger.logError(PrintSettings.class, "Error: " + e.getMessage());
        } catch (SAXException e) {
            Logger.logError(PrintSettings.class, "Error: " + e.getMessage());
        } catch (IOException e) {
            Logger.logError(PrintSettings.class, "Error: " + e.getMessage());
        }
        
        parsePrintSettings(printSettingsContent);
    }
    
    /**
     * @brief Parses print settings from a document.
     * 
     * @param printSettingsContent Document to be parsed.
     */
    private static void parsePrintSettings(Document printSettingsContent) {
        if (printSettingsContent == null) {
            return;
        }

        for(String printerType : AppConstants.PRINTER_TYPES)
        {
            Element settings =  printSettingsContent.getElementById(printerType);
            if(settings != null) {
                HashMap <String, Setting> settingsMap = new HashMap<>();
                List<Group> groupList = new ArrayList<>();
                parsePrintSettings(settings, settingsMap, groupList);
                sSettingsMaps.put(printerType, settingsMap);
                sGroupListMap.put(printerType, groupList);
            }
        }
    }

    private static void parsePrintSettings(Element settings, HashMap<String, Setting> map, List<Group> list) {
        NodeList groupList = settings.getElementsByTagName(XmlNode.NODE_GROUP);

        // looping through all item nodes <item>
        for (int i = 0; i < groupList.getLength(); i++) {
            Group group = new Group(groupList.item(i));
            list.add(group);

            for (Setting setting : group.getSettings()) {
                map.put(setting.getAttributeValue(XmlNode.ATTR_NAME), setting);
            }
        }
    }
    
    // ================================================================================
    // Getter for PJL
    // ================================================================================
    
    /**
     * @brief Formats print settings into PJL string. 
     * 
     * @param isLandscape true if PDF's first page is in landscape mode
     * 
     * @return PJL formatted string
     */
    public String formattedString(boolean isLandscape) {
        StringBuffer strBuf = new StringBuffer();
        String KEY_VAL_FORMAT = "%s=%d\n";
        
        for (String key : getSettingValues().keySet()) {
            int value = getSettingValues().get(key);
            
            if (AppConstants.USE_PDF_ORIENTATION) {
                if (key.equals(TAG_ORIENTATION)) {
                    value = isLandscape ? 1 : 0;
                }
            }

            if(mSettingMapKey == AppConstants.PRINTER_MODEL_IS && key.equals(TAG_PUNCH) && value == 3) {
                value = 2;
            }

            if(key.equals(TAG_SORT)) {
                value ^= 1;
            }

            strBuf.append(String.format(Locale.getDefault(), KEY_VAL_FORMAT, key, value));
        }
        
        strBuf.append(AppUtils.getAuthenticationString());
        return strBuf.toString();
    }
    
    // ================================================================================
    // Getter for int values
    // ================================================================================
    
    /**
     * @brief Gets map containing the settings values.
     * 
     * @return HashMap of settings values
     */
    public HashMap<String, Integer> getSettingValues() {
        return mSettingValues;
    }
    
    /**
     * @brief Gets print settings value from the specified key.
     * 
     * @param key Print settings key
     * 
     * @return Value of the print settings given the specified key
     */
    public int getValue(String key) {
        return mSettingValues.get(key);
    }
    
    /**
     * @brief Sets print settings value to the specified key.
     * 
     * @param key Print settings key to be updated
     * @param value New value to update
     * 
     * @retval true Success (always returns true)
     */
    public boolean setValue(String key, int value) {
        mSettingValues.put(key, value);
        return true;
    }
    
    // ================================================================================
    // Getter for PrintPreview
    // ================================================================================
    
    /**
     * @brief Retrieves ColorMode setting value i.e. the color mode of the print job.
     * 
     * @retval AUTO Auto color mode
     * @retval FULL_COLOR Colored mode
     * @retval MONOCHROME Gray scale color mode
     */
    public ColorMode getColorMode() {
        return ColorMode.values()[mSettingValues.get(TAG_COLOR_MODE)];
    }
    
    /**
     * @brief Retrieves Orientation setting value i.e. page orientation.
     * 
     * @retval PORTRAIT Portrait orientation
     * @retval LANDSCAPE Landscape orientation
     */
    public Orientation getOrientation() {
        return Orientation.values()[mSettingValues.get(TAG_ORIENTATION)];
    }
    
    /**
     * @brief Retrieves Duplex setting value i.e. duplex printing mode.
     * 
     * @retval OFF Duplex mode is OFF
     * @retval LONG_EDGE Long Edge duplex mode
     * @retval SHORT_EDGE Short Edge duplex mode
     */
    public Duplex getDuplex() {
        return Duplex.values()[mSettingValues.get(TAG_DUPLEX)];
    }
    
    /**
     * @brief Retrieves PaperSize setting value i.e. paper to be used during print.
     * 
     * @retval A3 297mm x 420mm
     * @retval A3W 316mm x 460mm
     * @retval A4 210mm x 297mm
     * @retval A5 148mm x 210mm
     * @retval A6 105mm x 148mm
     * @retval B4 257mm x 364mm
     * @retval B5 182mm x 257mm
     * @retval B6 128mm x 182mm
     * @retval FOOLSCAP 216mm x 340mm
     * @retval TABLOID 280mm x 432mm
     * @retval LEGAL 216mm x 356mm
     * @retval LETTER 216mm x 280mm
     * @retval STATEMENT 140mm x 216mm
     */
    public PaperSize getPaperSize() {
        return PaperSize.values()[mSettingValues.get(TAG_PAPER_SIZE)];
    }
    
    /**
     * @brief Retrieves ScaleToFit setting value i.e. whether PDF page will be scaled to fit the whole page.
     * 
     * @retval true PDF page will be scaled to fit the whole page
     * @retval false PDF page will not be scaled
     */
    public boolean isScaleToFit() {
        return (mSettingValues.get(TAG_SCALE_TO_FIT) == 1) ? true : false;
    }
    
    /**
     * @brief Retrieves Imposition setting value i.e. number of pages to print per sheet.
     * 
     * @retval OFF 1 page per sheet
     * @retval TWO_UP 2 pages per sheet
     * @retval FOUR_UP 4 pages per sheet
     */
    public Imposition getImposition() {
        return Imposition.values()[mSettingValues.get(TAG_IMPOSITION)];
    }
    
    /**
     * @brief Retrieves ImpositionOrder setting value i.e. direction of the PDF pages printed in one sheet.
     * 
     * @retval L_R Left to right
     * @retval R_L Right to left
     * @retval TL_R Upper left to right
     * @retval TR_L Upper right to left
     * @retval TL_B Upper left to bottom
     * @retval TR_B Upper right to bottom

     */
    public ImpositionOrder getImpositionOrder() {
        return ImpositionOrder.values()[mSettingValues.get(TAG_IMPOSITION_ORDER)];
    }
    
    /**
     * @brief Retrieves Sort setting value i.e. defines how the print output will be sorted.
     * 
     * @retval PER_PAGE To be grouped according to page
     * @retval PER_COPY To be sorted according to copy
     */
    public Sort getSort() {
        return Sort.values()[mSettingValues.get(TAG_SORT)];
    }
    /**
     * @brief Retrieves Booklet setting value i.e. the pages is in booklet format.
     * 
     * @retval true is in booklet format
     * @retval false not in booklet format
     */
    public boolean isBooklet() {
        return (mSettingValues.get(TAG_BOOKLET) == 1) ? true : false;
    }
    
    /**
     * @brief Retrieves BookletFinish setting value i.e. finishing options for when booklet is on.
     * 
     * @retval OFF Booklet Finish is OFF
     * @retval PAPER_FOLDING Paper will be folded 
     * @retval FOLD_AND_STAPLE Paper will be folded and stapled
     */
    public BookletFinish getBookletFinish() {
        return BookletFinish.values()[mSettingValues.get(TAG_BOOKLET_FINISH)];
    }
    
    /**
     * @brief Retrieves BookletLayout setting value i.e. direction of pages when booklet is on.
     * 
     * @retval FORWARD Retain direction of pages
     * @retval REVERSE Reverse direction of pages
     */
    public BookletLayout getBookletLayout() {
        return BookletLayout.values()[mSettingValues.get(TAG_BOOKLET_LAYOUT)];
    }
    
    /**
     * @brief Retrieves FinishingSide setting value i.e. the edge where the document will be bound.
     * 
     * @retval LEFT Document will be bound on the left edge
     * @retval TOP Document will be bound on the top edge
     * @retval RIGHT Document will be bound on the right edge
     */
    public FinishingSide getFinishingSide() {
        return FinishingSide.values()[mSettingValues.get(TAG_FINISHING_SIDE)];
    }
    
    /**
     * @brief Retrieves Staple setting value i.e. position where the print job will be stapled.
     * 
     * @retval OFF No staples
     * @retval ONE_UL Upper left staple
     * @retval ONE_UR Upper right staple
     * @retval ONE One staple
     * @retval TWO Two staples
     */
    public Staple getStaple() {
        return Staple.values()[mSettingValues.get(TAG_STAPLE)];
    }
    
    /**
     * @brief Retrieves Punch setting value i.e. punch holes in the print output.
     * 
     * @retval OFF No punch holes
     * @retval HOLES_2 2 holes
     * @retval HOLES_4 3 or 4 holes
     */
    public Punch getPunch() {
        return Punch.values()[mSettingValues.get(TAG_PUNCH)];
    }
    
    // ================================================================================
    // Public methods
    // ================================================================================
    
    /**
     * @brief Saves the Print Setting in the database
     * 
     * @param printerId Printer ID of the print settings to be saved
     * 
     * @retval true Save to database is successful
     * @retval false Save to database has failed
     */
    public boolean savePrintSettingToDB(int printerId) {
        if (printerId == PrinterManager.EMPTY_ID) {
            return false;
        }
        
        PrintSettingsManager manager = PrintSettingsManager.getInstance(SmartDeviceApp.getAppContext());
        return manager.saveToDB(printerId, this);
    }


    public String getSettingMapKey(){
        return mSettingMapKey;
    }

    static {
        sSettingsMaps = new HashMap<>();
        sGroupListMap = new HashMap<>();

        String xmlString = AppUtils.getFileContentsFromAssets(SmartDeviceApp.getAppContext(), AppConstants.XML_FILENAME);
        initializeStaticObjects(xmlString);
    }
}
