/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AppConstants.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

/**
 * @class AppConstants
 * 
 * @brief Application Constants
 */
public class AppConstants {
    /// Flag for debug
    public static final boolean DEBUG = false;
    /// Flag for creating initial database
    public static final boolean INITIAL_DB = false;
    /// Flag for debug database lower version
    public static final boolean DEBUG_LOWER_DB_VERSION = false;
    /// Flag for creating performance log
    public static final boolean FOR_PERF_LOGS = false;
    
    /// Flag for using PDF orientation
    public static boolean USE_PDF_ORIENTATION = true;
    
    /// Flag for showing splash screen
    public static final boolean APP_SHOW_SPLASH = true;
    /// Splash screen duration in milliseconds
    public static final long APP_SPLASH_DURATION = 2000;
    
    /// PDF directory
    public static final String CONST_PDF_DIR = "pdfs";
    /// PDF temporary file name
    //ver.2.0.2.2 If we can't get file name, the job name is "Unknown".(not "temp.pdf")(20160710 RISO Saito)
    //public static final String CONST_TEMP_PDF_PATH = "temp.pdf";
    public static final String CONST_TEMP_PDF_PATH = "Unknown";
    //End
    
    /// Part of Memory to be allocated to Print. Value of Bit shift operator, Preview Size = TOTAL MEMORY >> APP_BMP_CACHE_PART
    public static final int APP_BMP_CACHE_PART = 4;

    /// Log-in ID maximum length
    public static final int CONST_LOGIN_ID_LIMIT = 20;
    /// PIN code maximum length
    public static final int CONST_PIN_CODE_LIMIT = 8;
    /// Print Settings copies maximum length
    public static final int CONST_COPIES_LIMIT = 4;
    /// Log-in ID maximum length
    public static final int CONST_COMMUNITY_NAME_LIMIT = 32;
    
    /// Database version key for saving value to shared reference   
    public static final String PREF_KEY_DB_VERSION = "pref_db_version";

    /// Log-in ID key for saving value to shared preference
    public static final String PREF_KEY_LOGIN_ID = "pref_key_card_id";
    /// Default log-in ID
    public static final String PREF_DEFAULT_LOGIN_ID = "";
    
    /// Secure print key for saving value to shared preference
    public static final String PREF_KEY_AUTH_SECURE_PRINT = "pref_key_secure_print";
    /// Default Secure print value
    public static final boolean PREF_DEFAULT_AUTH_SECURE_PRINT = false;
    
    /// PIN code key for saving value to shared preference
    public static final String PREF_KEY_AUTH_PIN_CODE = "pref_key_pin_code";
    /// Default PIN code
    public static final String PREF_DEFAULT_AUTH_PIN_CODE = "";

    /// SNMP Community Name
    public static final String PREF_KEY_SNMP_COMMUNITY_NAME = "pref_key_snmp_community_name";
    // Default SNMP Community Name
    public static final String PREF_DEFAULT_SNMP_COMMUNITY_NAME = "public";

    /// Maximum printer count
    public static final int CONST_MAX_PRINTER_COUNT = 10;
    /// Ping timeout in milliseconds
    public static final int CONST_TIMEOUT_PING = 100;
    /// Update interval for update online status thread in milliseconds
    public static final int CONST_UPDATE_INTERVAL = 5000; // 5 seconds
    /// Print settings xml name
    public static final String XML_FILENAME = "printsettings.xml";
    
    /// Tag for secure print
    public static final String KEY_SECURE_PRINT = "securePrint";
    /// Tag for login ID
    public static final String KEY_LOGINID = "loginId";
    /// Tag for PIN code
    public static final String KEY_PINCODE = "pinCode";
    
    /// Maximum printer count
    public static final long CONST_FREE_SPACE_BUFFER = 104857600;

    // Printer Types
    public static final String PRINTER_MODEL_GD = "GD";
    public static final String PRINTER_MODEL_FW = "FW";
    public static final String PRINTER_MODEL_IS = "IS";
    public static final String PRINTER_MODEL_RAG = "RAG";
    public static final String PRINTER_MODEL_LIO = "LIO";

    //Array of printer types
    public static final String[] PRINTER_TYPES = {PRINTER_MODEL_IS, PRINTER_MODEL_GD, PRINTER_MODEL_FW, PRINTER_MODEL_RAG, PRINTER_MODEL_LIO};
}
