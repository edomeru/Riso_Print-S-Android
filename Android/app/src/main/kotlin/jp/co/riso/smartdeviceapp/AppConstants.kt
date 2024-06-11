/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * AppConstants.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp

/**
 * @class AppConstants
 *
 * @brief Application Constants
 */
object AppConstants {
    /// Flag for debug
    const val DEBUG = false

    /// Flag for creating initial database
    const val INITIAL_DB = false

    /// Flag for debug database lower version
    const val DEBUG_LOWER_DB_VERSION = false

    /// Flag for creating performance log
    const val FOR_PERF_LOGS = false

    /// Flag for using PDF orientation
    @JvmField
    var USE_PDF_ORIENTATION = true

    /// Flag for showing splash screen
    const val APP_SHOW_SPLASH = true

    /// Splash screen duration in milliseconds
    const val APP_SPLASH_DURATION: Long = 2000

    /// PDF directory
    const val CONST_PDF_DIR = "pdfs"

    /// PDF temporary file name
    //ver.2.0.2.2 If we can't get file name, the job name is "Unknown".(not "temp.pdf")(20160710 RISO Saito)
    //public static final String CONST_TEMP_PDF_PATH = "temp.pdf";
    const val CONST_TEMP_PDF_PATH = "Unknown"

    //End
    /// Part of Memory to be allocated to Print. Value of Bit shift operator, Preview Size = TOTAL MEMORY >> APP_BMP_CACHE_PART
    const val APP_BMP_CACHE_PART = 4

    /// Log-in ID maximum length
    const val CONST_LOGIN_ID_LIMIT = 20

    /// PIN code maximum length
    const val CONST_PIN_CODE_LIMIT = 8

    /// Print Settings copies maximum length
    const val CONST_COPIES_LIMIT = 4

    /// Log-in ID maximum length
    const val CONST_COMMUNITY_NAME_LIMIT = 32

    /// Database version key for saving value to shared reference   
    const val PREF_KEY_DB_VERSION = "pref_db_version"

    /// Log-in ID key for saving value to shared preference
    const val PREF_KEY_LOGIN_ID = "pref_key_card_id"

    /// Default log-in ID
    const val PREF_DEFAULT_LOGIN_ID = ""

    /// Secure print key for saving value to shared preference
    const val PREF_KEY_AUTH_SECURE_PRINT = "pref_key_secure_print"

    /// Default Secure print value
    const val PREF_DEFAULT_AUTH_SECURE_PRINT = false

    /// PIN code key for saving value to shared preference
    const val PREF_KEY_AUTH_PIN_CODE = "pref_key_pin_code"

    /// Default PIN code
    const val PREF_DEFAULT_AUTH_PIN_CODE = ""

    /// SNMP Community Name
    const val PREF_KEY_SNMP_COMMUNITY_NAME = "pref_key_snmp_community_name"

    // Default SNMP Community Name
    const val PREF_DEFAULT_SNMP_COMMUNITY_NAME = "public"

    //  LPR Print
    // Job Number Counter
    const val PREF_KEY_JOB_NUMBER_COUNTER = "job_number_counter"

    // Default Job Number Counter - Start at 0
    const val PREF_DEFAULT_JOB_NUMBER_COUNTER = 0

    // Max Job Number
    const val CONST_MAX_JOB_NUMBER = 999

    /// Maximum printer count
    const val CONST_MAX_PRINTER_COUNT = 10

    /// Ping timeout in milliseconds
    const val CONST_TIMEOUT_PING = 100

    /// Update interval for update online status thread in milliseconds
    const val CONST_UPDATE_INTERVAL = 5000 // 5 seconds

    /// Print settings xml name
    const val XML_FILENAME = "printsettings.xml"

    /// Tag for secure print
    const val KEY_SECURE_PRINT = "securePrint"

    /// Tag for login ID
    const val KEY_LOGINID = "loginId"

    /// Tag for PIN code
    const val KEY_PINCODE = "pinCode"

    /// Maximum printer count
    const val CONST_FREE_SPACE_BUFFER: Long = 104857600

    // Printer Types
    const val PRINTER_MODEL_GD = "GD"
    const val PRINTER_MODEL_FW = "FW"
    const val PRINTER_MODEL_IS = "IS"
    const val PRINTER_MODEL_FT = "FT"
    const val PRINTER_MODEL_GL = "GL"
    const val PRINTER_MODEL_CEREZONA_S =
        "CEREZONA S" // As of V4.1.0.0, CEREZONA S model will be classified under FT since they have the same print settings
    const val PRINTER_MODEL_OGA = "OGA" // As of V6.0.0.0, OGA model will be classified under GL since they have the same print settings

    // TODO: Add CEREZONA S in PRINTER_TYPES and add a separate entry for CEREZONA S model in `printsettings.xml` file if there are changes in print settings that differentiate it to FT
    // TODO: Add OGA in PRINTER_TYPES and add a separate entry for OGA model in `printsettings.xml` file if there are changes in print settings that differentiate it to GL
    //Array of printer types
    @JvmField
    val PRINTER_TYPES = arrayOf(
        PRINTER_MODEL_IS,
        PRINTER_MODEL_GD,
        PRINTER_MODEL_FW,
        PRINTER_MODEL_FT,
        PRINTER_MODEL_GL
    )

    // Supported document types
    @JvmField
    val DOC_TYPES = arrayOf("application/pdf", "text/plain")

    // Supported image types for Android 9 to 11
    @JvmField
    val IMAGE_TYPES = arrayOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/gif",
        "image/x-ms-bmp",
        "image/bmp",
        "image/x-windows-bmp",
        "image/heif",
        "image/heic"
    )

    // Supported image types for Android 8 (without HEIF and AVIF support)
    @JvmField
    val IMAGE_TYPES_ANDROID_8 = arrayOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/gif",
        "image/x-ms-bmp",
        "image/bmp",
        "image/x-windows-bmp",
    )

    // Supported image types for Android 12 and above (with AVIF support)
    @JvmField
    val IMAGE_TYPES_ANDROID_12 = arrayOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/gif",
        "image/x-ms-bmp",
        "image/bmp",
        "image/x-windows-bmp",
        "image/heif",
        "image/heic",
        "image/avif"
    )

    // Intent extra key for flag if file is not pdf and from picker
    const val EXTRA_FILE_FROM_PICKER = "file_from_picker"

    // PDF filename for converted multiple images
    const val MULTI_IMAGE_PDF_FILENAME = "MultiPage_Images.pdf"

    // File size limit for text files (5MB)
    const val TEXT_FILE_SIZE_LIMIT = 5 * 1024 * 1024

    // Image captured filename
    const val CONST_IMAGE_CAPTURED_FILENAME = "Captured_Image.pdf"

    // add URI authorities here for cloud storage that need temporary copy for text/image to PDF conversion
    // i.e. when text/image Open-In from cloud storage fails with CONVERSION_FILE_NOT_FOUND error
    // Google Drive (account-specified) URI authority
    private const val GOOGLE_DRIVE_URI_AUTHORITY = "com.google.android.apps.docs.storage"

    // OneDrive URI authority
    private const val ONE_DRIVE_URI_AUTHORITY = "com.microsoft.skydrive.content.external"

    // add URI authorities here for cloud storage that need temporary copy for text to PDF conversion
    @JvmField
    val TXT_URI_AUTHORITIES = arrayOf(ONE_DRIVE_URI_AUTHORITY)

    // add URI authorities here for cloud storage that need temporary copy for image/images to PDF conversion
    @JvmField
    val IMG_URI_AUTHORITIES = arrayOf(GOOGLE_DRIVE_URI_AUTHORITY, ONE_DRIVE_URI_AUTHORITY)

    // Temporary copy of image/text file - filename
    const val TEMP_COPY_FILENAME = "TMP_COPY"

    // PMS Ports
    const val CONST_PORT_HTTP = 80
    const val CONST_PORT_LPR = 515
    const val CONST_PORT_RAW = 9100
    const val CONST_PORT_IPPS = 631

    // Error for invalid intent data from third party app
    const val ERR_KEY_INVALID_INTENT = "error_key_invalid_intent"

    // For checking if chromebook
    const val CHROME_BOOK = "org.chromium.arc.device_management"

    // Content Print - START
    /// Print Settings Fragment for Printing key for saving value to shared preference
    const val PREF_KEY_FRAGMENT_FOR_PRINTING = "pref_key_fragment_for_printing"

    /// Content Print key for saving value to intent
    const val VAL_KEY_CONTENT_PRINT = "body"

    // The time before tap is considered a double tap
    const val DOUBLE_TAP_TIME_ELAPSED = 1000
    // Content Print - END
}