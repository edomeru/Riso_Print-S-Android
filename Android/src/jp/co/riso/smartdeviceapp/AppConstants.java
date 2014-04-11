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
    
    public static final String PREF_KEY_CARD_ID = "pref_key_card_id";
    public static final String PREF_DEFAULT_CARD_ID = "";
    
    public static final String PREF_KEY_READ_COMM_NAME = "pref_key_read_comm_name";
    public static final String PREF_DEFAULT_READ_COMM_NAME = "public";
}