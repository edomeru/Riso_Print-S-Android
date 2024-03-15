/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintSettings.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model.printsettings

import jp.co.riso.android.util.AppUtils
import jp.co.riso.android.util.Logger
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printsettings.PrintSettingsManager
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.*
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize.Companion.valuesDefault
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize.Companion.valuesGl
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * @class PrintSettings
 * 
 * @brief PrintSettings class representing the printer settings data.
 */
class PrintSettings {

    val settingValues: HashMap<String, Int?>
    val settingMapKey: String

    /**
     * @brief Creates a PrintSettings instance using default values of print settings
     * of the default printer type
     *
     * @param printerType The type of printer used as key to the different set of settings.
     * based on printer type
     */
    @JvmOverloads
    constructor(printerType: String = AppConstants.PRINTER_MODEL_IS) {
        settingValues = HashMap()
        settingMapKey = printerType

        //Use IS as default settings map
        for (key in sSettingsMaps!![printerType]!!.keys) {
            val setting = sSettingsMaps!![printerType]!![key]
            settingValues[key] = setting!!.defaultValue
        }
    }

    /**
     * @brief Creates a PrintSettings instance from another existing PrintSettings instance.
     *
     * @param printSettings Print settings to be copied
     */
    constructor(printSettings: PrintSettings) {
        settingMapKey = printSettings.settingMapKey
        settingValues = HashMap()
        for (key in printSettings.settingValues.keys) {
            settingValues[key] = printSettings.settingValues[key]
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
    constructor(printerId: Int, printerType: String) : this(printerType) {

        // overwrite values if valid printer id
        if (printerId != PrinterManager.EMPTY_ID) {
            val manager = SmartDeviceApp.appContext?.let { PrintSettingsManager.getInstance(it) }
            val printSettings = manager?.getPrintSetting(printerId, printerType)
            for (key in printSettings!!.settingValues.keys) {
                settingValues[key] = printSettings.settingValues[key]
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
    fun formattedString(isLandscape: Boolean): String {
        val strBuf = StringBuffer()
        val formatKeyVal = "%s=%d\n"
        for (key in settingValues.keys) {
            var value = settingValues[key]!!
            if (AppConstants.USE_PDF_ORIENTATION) {
                if (key == TAG_ORIENTATION) {
                    value = if (isLandscape) 1 else 0
                }
            }
            val punch = punch
            if (key == TAG_PUNCH) {
                if (settingMapKey == AppConstants.PRINTER_MODEL_IS) {
                    // Use 3-4Holes regardless if 3 or 4 Holes
                    if (punch === Punch.HOLES_3 || punch === Punch.HOLES_4) {
                        value = 2 // Common Library: 3-4Holes
                    }
                } else {
                    // RM #356 Fix: Align with CommonLibrary for 3-Holes Condition for FW, GD, FT, GL & CEREZONA S
                    if (punch === Punch.HOLES_3) {
                        value = 3 // Common Library: 3Holes
                    } else if (punch === Punch.HOLES_4) {
                        value = 2 // Common Library: 4Holes
                    }
                }
            }
            if (key == TAG_SORT && settingMapKey == AppConstants.PRINTER_MODEL_IS) {
                value = value xor 1
            }
            strBuf.append(String.format(Locale.getDefault(), formatKeyVal, key, value))
        }
        strBuf.append(AppUtils.authenticationString)
        return strBuf.toString()
    }
    // ================================================================================
    // Getter for int values
    // ================================================================================
    /**
     * @brief Gets print settings value from the specified key.
     *
     * @param key Print settings key
     *
     * @return Value of the print settings given the specified key
     */
    fun getValue(key: String): Int {
        return if (settingValues[key] == null) {
            -1
        } else {
            settingValues[key]!!
        }
    }

    /**
     * @brief Sets print settings value to the specified key.
     *
     * @param key Print settings key to be updated
     * @param value New value to update
     */
    fun setValue(key: String, value: Int) {
        settingValues[key] = value
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
    val colorMode: ColorMode
        get() = ColorMode.values()[settingValues[TAG_COLOR_MODE]!!]

    /**
     * @brief Retrieves Orientation setting value i.e. page orientation.
     *
     * @retval PORTRAIT Portrait orientation
     * @retval LANDSCAPE Landscape orientation
     */
    val orientation: Orientation
        get() = Orientation.values()[settingValues[TAG_ORIENTATION]!!]

    /**
     * @brief Retrieves Duplex setting value i.e. duplex printing mode.
     *
     * @retval OFF Duplex mode is OFF
     * @retval LONG_EDGE Long Edge duplex mode
     * @retval SHORT_EDGE Short Edge duplex mode
     */
    val duplex: Duplex
        get() = Duplex.values()[settingValues[TAG_DUPLEX]!!]

    /**
     * @brief Retrieves PaperSize setting value i.e. paper to be used during print.
     *
     * @retval A3 297mm x 420mm
     * @retval A3W 316mm x 460mm
     * @retval SRA3 320mm x 450mm
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
    val paperSize: PaperSize
        get() = _paperSizeOnPrinter// Need to know if printer type is GL or non-GL as GL printer has an added paper size SRA3
    // mSettingMapKey holds the printer type string
    /**
     * @brief Retrieves PaperSize setting value based on printer model.
     *
     * @retval PaperSize
     */
    private val _paperSizeOnPrinter: PaperSize
        get() {
            val paperSizeIndex = settingValues[TAG_PAPER_SIZE]!!

            // Need to know if printer type is GL or non-GL as GL printer has an added paper size SRA3
            // mSettingMapKey holds the printer type string
            return if (settingMapKey == AppConstants.PRINTER_MODEL_GL) {
                valuesGl()[paperSizeIndex]
            } else {
                valuesDefault()[paperSizeIndex]
            }
        }

    /**
     * @brief Retrieves InputTray_FT_GL_CEREZONA_S_OGA setting value i.e. tray location of input paper.
     *
     * @retval AUTO
     * @retval STANDARD
     * @retval TRAY1
     * @retval TRAY2
     * @retval TRAY3
     * @retval EXTERNAL_FEEDER
     */
    val inputTray: InputTrayFtGlCerezonaSOga
        get() = InputTrayFtGlCerezonaSOga.values()[settingValues[TAG_INPUT_TRAY]!!]

    /**
     * @brief Retrieves ScaleToFit setting value i.e. whether PDF page will be scaled to fit the whole page.
     *
     * @retval true PDF page will be scaled to fit the whole page
     * @retval false PDF page will not be scaled
     */
    val isScaleToFit: Boolean
        get() = settingValues[TAG_SCALE_TO_FIT] == 1

    /**
     * @brief Retrieves Imposition setting value i.e. number of pages to print per sheet.
     *
     * @retval OFF 1 page per sheet
     * @retval TWO_UP 2 pages per sheet
     * @retval FOUR_UP 4 pages per sheet
     */
    val imposition: Imposition
        get() = Imposition.values()[settingValues[TAG_IMPOSITION]!!]

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
    val impositionOrder: ImpositionOrder
        get() = ImpositionOrder.values()[settingValues[TAG_IMPOSITION_ORDER]!!]

    /**
     * @brief Retrieves Sort setting value i.e. defines how the print output will be sorted.
     *
     * @retval PER_PAGE To be grouped according to page
     * @retval PER_COPY To be sorted according to copy
     */
    val sort: Sort
        get() = Sort.values()[settingValues[TAG_SORT]!!]

    /**
     * @brief Retrieves Booklet setting value i.e. the pages is in booklet format.
     *
     * @retval true is in booklet format
     * @retval false not in booklet format
     */
    val isBooklet: Boolean
        get() = settingValues[TAG_BOOKLET] == 1

    /**
     * @brief Retrieves BookletFinish setting value i.e. finishing options for when booklet is on.
     *
     * @retval OFF Booklet Finish is OFF
     * @retval PAPER_FOLDING Paper will be folded
     * @retval FOLD_AND_STAPLE Paper will be folded and stapled
     */
    val bookletFinish: BookletFinish
        get() = BookletFinish.values()[settingValues[TAG_BOOKLET_FINISH]!!]

    /**
     * @brief Retrieves BookletLayout setting value i.e. direction of pages when booklet is on.
     *
     * @retval FORWARD Retain direction of pages
     * @retval REVERSE Reverse direction of pages
     */
    val bookletLayout: BookletLayout
        get() = BookletLayout.values()[settingValues[TAG_BOOKLET_LAYOUT]!!]

    /**
     * @brief Retrieves FinishingSide setting value i.e. the edge where the document will be bound.
     *
     * @retval LEFT Document will be bound on the left edge
     * @retval TOP Document will be bound on the top edge
     * @retval RIGHT Document will be bound on the right edge
     */
    val finishingSide: FinishingSide
        get() = FinishingSide.values()[settingValues[TAG_FINISHING_SIDE]!!]

    /**
     * @brief Retrieves Staple setting value i.e. position where the print job will be stapled.
     *
     * @retval OFF No staples
     * @retval ONE_UL Upper left staple
     * @retval ONE_UR Upper right staple
     * @retval ONE One staple
     * @retval TWO Two staples
     */
    val staple: Staple
        get() = Staple.values()[settingValues[TAG_STAPLE]!!]

    /**
     * @brief Retrieves Punch setting value i.e. punch holes in the print output.
     *
     * @retval OFF No punch holes
     * @retval HOLES_2 2 holes
     * @retval HOLES_4 3 or 4 holes
     */
    val punch: Punch
        get() = Punch.values()[settingValues[TAG_PUNCH]!!]
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
    fun savePrintSettingToDB(printerId: Int): Boolean {
        if (printerId == PrinterManager.EMPTY_ID) {
            return false
        }
        val manager = SmartDeviceApp.appContext?.let { PrintSettingsManager.getInstance(it) }
        return manager!!.saveToDB(printerId, this)
    }

    companion object {
        const val TAG_COLOR_MODE = "colorMode" ///< Tag used to identify ColorMode settings
        const val TAG_ORIENTATION = "orientation" ///< Tag used to identify Orientation settings
        const val TAG_COPIES = "copies" ///< Tag used to identify Copies settings
        const val TAG_DUPLEX = "duplex" ///< Tag used to identify Duplex settings
        const val TAG_PAPER_SIZE = "paperSize" ///< Tag used to identify PaperSize settings
        const val TAG_SCALE_TO_FIT = "scaleToFit" ///< Tag used to identify ScaleToFit settings
        // const val TAG_PAPER_TYPE = "paperType" ///< Tag used to identify PaperType settings
        const val TAG_INPUT_TRAY = "inputTray" ///< Tag used to identify InputTray settings
        const val TAG_IMPOSITION = "imposition" ///< Tag used to identify Imposition settings
        const val TAG_IMPOSITION_ORDER =
            "impositionOrder" ///< Tag used to identify ImpositionOrder settings
        const val TAG_SORT = "sort" ///< Tag used to identify Sort settings
        const val TAG_BOOKLET = "booklet" ///< Tag used to identify Booklet settings
        const val TAG_BOOKLET_FINISH =
            "bookletFinish" ///< Tag used to identify BookletFinish settings
        const val TAG_BOOKLET_LAYOUT =
            "bookletLayout" ///< Tag used to identify BookletLayout settings
        const val TAG_FINISHING_SIDE =
            "finishingSide" ///< Tag used to identify FinishingSide settings
        const val TAG_STAPLE = "staple" ///< Tag used to identify Staple settings
        const val TAG_PUNCH = "punch" ///< Tag used to identify Punch settings
        const val TAG_OUTPUT_TRAY = "outputTray" ///< Tag used to identify OutputTray settings
        @JvmField
        var sGroupListMap: HashMap<String, List<Group>>? = null
        @JvmField
        var sSettingsMaps: HashMap<String, HashMap<String, Setting>>? = null

        /**
         * @brief Initialize static objects
         *
         * @param xmlString Text content of the XML for initialization of PrintSettings values.
         */
        @JvmStatic
        fun initializeStaticObjects(xmlString: String?) {
            if (xmlString == null) {
                return
            }
            var printSettingsContent: Document? = null
            val dbf = DocumentBuilderFactory.newInstance()
            try {
                val db = dbf.newDocumentBuilder()
                val `is` = InputSource()
                `is`.characterStream = StringReader(xmlString)
                printSettingsContent = db.parse(`is`)
            } catch (e: ParserConfigurationException) {
                Logger.logError(PrintSettings::class.java, "Error: " + e.message)
            } catch (e: IOException) {
                Logger.logError(PrintSettings::class.java, "Error: " + e.message)
            } catch (e: SAXException) {
                Logger.logError(PrintSettings::class.java, "Error: " + e.message)
            }
            parsePrintSettings(printSettingsContent)
        }

        /**
         * @brief Parses print settings from a document.
         *
         * @param printSettingsContent Document to be parsed.
         */
        private fun parsePrintSettings(printSettingsContent: Document?) {
            if (printSettingsContent == null) {
                return
            }
            for (printerType in AppConstants.PRINTER_TYPES) {
                val settings = printSettingsContent.getElementById(printerType)
                if (settings != null) {
                    val settingsMap = HashMap<String, Setting>()
                    val groupList: MutableList<Group> = ArrayList()
                    parsePrintSettings(settings, settingsMap, groupList)
                    sSettingsMaps!![printerType] = settingsMap
                    sGroupListMap!![printerType] = groupList
                }
            }
        }

        private fun parsePrintSettings(
            settings: Element,
            map: HashMap<String, Setting>,
            list: MutableList<Group>
        ) {
            val groupList = settings.getElementsByTagName(XmlNode.NODE_GROUP)

            // looping through all item nodes <item>
            for (i in 0 until groupList.length) {
                val group = Group(groupList.item(i))
                list.add(group)
                for (setting in group.settings) {
                    map[setting.getAttributeValue(XmlNode.ATTR_NAME)] = setting
                }
            }
        }

        init {
            sSettingsMaps = HashMap()
            sGroupListMap = HashMap()
            val xmlString = AppUtils.getFileContentsFromAssets(
                SmartDeviceApp.appContext,
                AppConstants.XML_FILENAME
            )
            initializeStaticObjects(xmlString)
        }
    }
}