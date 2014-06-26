/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * KeyConstatnts.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.db;

/**
 * @class KeyConstants
 * 
 * @brief Keys used for Database Access
 */
public final class KeyConstants {
    /// SQL key for printer ID
    public static final String KEY_SQL_PRINTER_ID = "prn_id";
    /// SQL key for printer IP address
    public static final String KEY_SQL_PRINTER_IP = "prn_ip_address";
    /// SQL key for printer name
    public static final String KEY_SQL_PRINTER_NAME = "prn_name";
    /// SQL key for printer port setting
    public static final String KEY_SQL_PRINTER_PORT = "prn_port_setting";
    /// SQL key for printer LPR print capability
    public static final String KEY_SQL_PRINTER_LPR = "prn_enabled_lpr";
    /// SQL key for printer raw print capability
    public static final String KEY_SQL_PRINTER_RAW = "prn_enabled_raw";
    /// SQL key for printer booklet finishing capability
    public static final String KEY_SQL_PRINTER_BOOKLET_FINISHING = "prn_enabled_booklet_finishing";
    /// SQL key for printer staple capability
    public static final String KEY_SQL_PRINTER_STAPLER = "prn_enabled_stapler";
    /// SQL key for printer punch 3 holes capability
    public static final String KEY_SQL_PRINTER_PUNCH3 = "prn_enabled_punch3";
    /// SQL key for printer punch 4 holes capability
    public static final String KEY_SQL_PRINTER_PUNCH4 = "prn_enabled_punch4";
    /// SQL key for printer tray face-down capability
    public static final String KEY_SQL_PRINTER_TRAYFACEDOWN = "prn_enabled_tray_facedown";
    /// SQL key for printer tray top capability
    public static final String KEY_SQL_PRINTER_TRAYTOP = "prn_enabled_tray_top";
    /// SQL key for printer tray stack capability
    public static final String KEY_SQL_PRINTER_TRAYSTACK = "prn_enabled_tray_stack";
    /// SQL key for printer table
    public static final String KEY_SQL_PRINTER_TABLE = "Printer";
    /// SQL key for default printer table
    public static final String KEY_SQL_DEFAULT_PRINTER_TABLE = "DefaultPrinter";
    /// SQL key for print job table
    public static final String KEY_SQL_PRINTJOB_TABLE = "PrintJob";
    /// SQL key for print job ID
    public static final String KEY_SQL_PRINTJOB_ID = "pjb_id";
    /// SQL key for print job name
    public static final String KEY_SQL_PRINTJOB_NAME = "pjb_name";
    /// SQL key for print job date
    public static final String KEY_SQL_PRINTJOB_DATE = "pjb_date";
    /// SQL key for print job result
    public static final String KEY_SQL_PRINTJOB_RESULT = "pjb_result";
    /// SQL key for print setting table
    public static final String KEY_SQL_PRINTSETTING_TABLE = "PrintSetting";
    /// SQL key for print setting id
    public static final String KEY_SQL_PRINTSETTING_ID = "pst_id";
}