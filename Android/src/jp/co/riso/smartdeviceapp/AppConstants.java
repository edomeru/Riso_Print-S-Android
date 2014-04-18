/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AppConstants.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

public class AppConstants {
    public static final long APP_SPLASH_DURATION = 3000;
    public static final String APP_FONT_FILE = "fonts/Raleway/Raleway-Regular.ttf";
    
    // Part of Memory to be allocated to Print Preview
    // Bit shift operator
    // Size = TOTAL MEMORY >> APP_BMP_CACHE_PART
    public static final int APP_BMP_CACHE_PART = 4;
    
    public static final String PREF_KEY_LOGIN_ID = "pref_key_card_id";
    public static final String PREF_DEFAULT_LOGIN_ID = "";
    
    public static final String PREF_KEY_PIN_CODE = "pref_key_pin_code";
    public static final String PREF_DEFAULT_PIN_CODE = "";
    
    public static final int CONST_MAX_PRINTER_COUNT = 10;
    public static final int CONST_TIMEOUT_PING = 100;
    public static final int CONST_UPDATE_INTERVAL = 5000; // 5 seconds
}
