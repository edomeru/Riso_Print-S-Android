/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * KeyConstants.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.db

/**
 * @class KeyConstants
 *
 * @brief Keys used for Database Access
 */
object KeyConstants {
    /// SQL key for printer ID
    const val KEY_SQL_PRINTER_ID = "prn_id"

    /// SQL key for printer IP address
    const val KEY_SQL_PRINTER_IP = "prn_ip_address"

    /// SQL key for printer MAC address
    const val KEY_SQL_PRINTER_MAC = "prn_mac_address"

    /// SQL key for printer name
    const val KEY_SQL_PRINTER_NAME = "prn_name"

    /// SQL key for printer port setting
    const val KEY_SQL_PRINTER_PORT = "prn_port_setting"

    /// SQL key for printer LPR print capability
    const val KEY_SQL_PRINTER_LPR = "prn_enabled_lpr"

    /// SQL key for printer raw print capability
    const val KEY_SQL_PRINTER_RAW = "prn_enabled_raw"

    /// SQL key for printer IPPS print capability
    const val KEY_SQL_PRINTER_IPPS = "prn_enabled_ipps"

    /// SQL key for printer booklet finishing capability
    const val KEY_SQL_PRINTER_BOOKLET_FINISHING = "prn_enabled_booklet_finishing"

    /// SQL key for printer staple capability
    const val KEY_SQL_PRINTER_STAPLER = "prn_enabled_stapler"

    /// SQL key for printer punch 3 holes capability
    const val KEY_SQL_PRINTER_PUNCH3 = "prn_enabled_punch3"

    /// SQL key for printer punch 4 holes capability
    const val KEY_SQL_PRINTER_PUNCH4 = "prn_enabled_punch4"

    /// SQL key for printer tray face-down capability
    const val KEY_SQL_PRINTER_TRAYFACEDOWN = "prn_enabled_tray_facedown"

    /// SQL key for printer tray top capability
    const val KEY_SQL_PRINTER_TRAYTOP = "prn_enabled_tray_top"

    /// SQL key for printer tray stack capability
    const val KEY_SQL_PRINTER_TRAYSTACK = "prn_enabled_tray_stack"

    /// SQL key for printer external feeder capability
    const val KEY_SQL_PRINTER_EXTERNALFEEDER = "prn_enabled_external_feeder"

    /// SQL key for printer punch 0 holes capability
    const val KEY_SQL_PRINTER_PUNCH0 = "prn_enabled_punch0"

    /// SQL key for printer table
    const val KEY_SQL_PRINTER_TABLE = "Printer"

    /// SQL key for default printer table
    const val KEY_SQL_DEFAULT_PRINTER_TABLE = "DefaultPrinter"

    /// SQL key for print job table
    const val KEY_SQL_PRINTJOB_TABLE = "PrintJob"

    /// SQL key for print job table
    const val KEY_SQL_CONTENT_PRINT_TABLE = "ContentPrint"

    /// SQL key for print job ID
    const val KEY_SQL_PRINTJOB_ID = "pjb_id"

    /// SQL key for print job name
    const val KEY_SQL_PRINTJOB_NAME = "pjb_name"

    /// SQL key for print job date
    const val KEY_SQL_PRINTJOB_DATE = "pjb_date"

    /// SQL key for print job result
    const val KEY_SQL_PRINTJOB_RESULT = "pjb_result"

    /// SQL key for print setting table
    const val KEY_SQL_PRINTSETTING_TABLE = "PrintSetting"

    /// SQL key for print setting id
    const val KEY_SQL_PRINTSETTING_ID = "pst_id"

    /// SQL key for content file id
    const val KEY_SQL_CONTENT_FILE_ID = "fileId"

    /// SQL key for content file name
    const val KEY_SQL_CONTENT_FILE_NAME = "fileName"

    /// SQL key for user current email
    const val KEY_SQL_CONTENT_USER_CURRENT_EMAIL = "email"

}