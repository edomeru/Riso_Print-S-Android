/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettings.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.co.riso.android.util.AppUtils;
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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class PrintSettings {
    public static final String TAG = "PrintSettings";
    
    public static final String TAG_COLOR_MODE = "colorMode";
    public static final String TAG_ORIENTATION = "orientation";
    public static final String TAG_COPIES = "copies";
    public static final String TAG_DUPLEX = "duplex";
    public static final String TAG_PAPER_SIZE = "paperSize";
    public static final String TAG_SCALE_TO_FIT = "scaleToFit";
    public static final String TAG_PAPER_TYPE = "paperType";
    public static final String TAG_INPUT_TRAY = "inputTray";
    public static final String TAG_IMPOSITION = "imposition";
    public static final String TAG_IMPOSITION_ORDER = "impositionOrder";
    public static final String TAG_SORT = "sort";
    public static final String TAG_BOOKLET = "booklet";
    public static final String TAG_BOOKLET_FINISH = "bookletFinish";
    public static final String TAG_BOOKLET_LAYOUT = "bookletLayout";
    public static final String TAG_FINISHING_SIDE = "finishingSide";
    public static final String TAG_STAPLE = "staple";
    public static final String TAG_PUNCH = "punch";
    public static final String TAG_OUTPUT_TRAY = "outputTray";
    
    public static final List<Group> sGroupList;
    public static final HashMap<String, Setting> sSettingMap; // ConvenienceHashMap
    
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
        PrintSettingsManager manager = PrintSettingsManager.getInstance(SmartDeviceApp.getAppContext());
        // will overwrite the value if values are retrieved
        
        PrintSettings printSettings = manager.getPrintSetting(printerId);
        if (printSettings != null){
            for (String key : printSettings.getSettingValues().keySet()) {
                mSettingValues.put(key, printSettings.getSettingValues().get(key));
            }
        }
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
        
        NodeList groupList = printSettingsContent.getElementsByTagName(XmlNode.NODE_GROUP);
        
        // looping through all item nodes <item>
        for (int i = 0; i < groupList.getLength(); i++) {
            Group group = new Group(groupList.item(i));
            sGroupList.add(group);
            
            for (Setting setting : group.getSettings()) {
                sSettingMap.put(setting.getAttributeValue(XmlNode.ATTR_NAME), setting);
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
        return ColorMode.values()[mSettingValues.get(TAG_COLOR_MODE)];
    }
    
    /**
     * @return the Orientation
     */
    public Orientation getOrientation() {
        return Orientation.values()[mSettingValues.get(TAG_ORIENTATION)];
    }
    
    /**
     * @return the mDuplex
     */
    public Duplex getDuplex() {
        return Duplex.values()[mSettingValues.get(TAG_DUPLEX)];
    }
    
    /**
     * @return the mPaperSize
     */
    public PaperSize getPaperSize() {
        return PaperSize.values()[mSettingValues.get(TAG_PAPER_SIZE)];
    }
    
    /**
     * @return the mScaleToFit
     */
    public boolean isScaleToFit() {
        return (mSettingValues.get(TAG_SCALE_TO_FIT) == 1) ? true : false;
    }
    
    /**
     * @return the mImposition
     */
    public Imposition getImposition() {
        return Imposition.values()[mSettingValues.get(TAG_IMPOSITION)];
    }
    
    /**
     * @return the mImpositionOrder
     */
    public ImpositionOrder getImpositionOrder() {
        return ImpositionOrder.values()[mSettingValues.get(TAG_IMPOSITION_ORDER)];
    }
    
    /**
     * @return the mSort
     */
    public Sort getSort() {
        return Sort.values()[mSettingValues.get(TAG_SORT)];
    }
    /**
     * @return the mBooklet
     */
    public boolean isBooklet() {
        return (mSettingValues.get(TAG_BOOKLET) == 1) ? true : false;
    }
    
    /**
     * @return the mBookletFinish
     */
    public BookletFinish getBookletFinish() {
        return BookletFinish.values()[mSettingValues.get(TAG_BOOKLET_FINISH)];
    }
    
    /**
     * @return the mBookletLayout
     */
    public BookletLayout getBookletLayout() {
        return BookletLayout.values()[mSettingValues.get(TAG_BOOKLET_LAYOUT)];
    }
    
    /**
     * @return the mFinishingSide
     */
    public FinishingSide getFinishingSide() {
        return FinishingSide.values()[mSettingValues.get(TAG_FINISHING_SIDE)];
    }
    
    /**
     * @return the mStaple
     */
    public Staple getStaple() {
        return Staple.values()[mSettingValues.get(TAG_STAPLE)];
    }
    
    /**
     * @return the mPunch
     */
    public Punch getPunch() {
        return Punch.values()[mSettingValues.get(TAG_PUNCH)];
    }
    
    // ================================================================================
    // Public methods
    // ================================================================================
    
    /**
     * This method saves the Print Setting in the database
     * 
     * @param printerId
     *            current printer ID selected
     * @return boolean result of insert/replace to DB, returns true if successful.
     */
    public boolean savePrintSettingToDB(int printerId) {
        if (printerId == PrinterManager.EMPTY_ID) {
            return false;
        }
        
        PrintSettingsManager manager = PrintSettingsManager.getInstance(SmartDeviceApp.getAppContext());
        return manager.saveToDB(printerId, this);
    }
    
    static {
        sGroupList = new ArrayList<Group>();
        sSettingMap = new HashMap<String, Setting>();
        
        initializeStaticObjects();
    }
}
