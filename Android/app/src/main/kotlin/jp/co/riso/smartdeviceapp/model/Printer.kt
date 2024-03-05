/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * Printer.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager

/**
 * @class Printer
 *
 * @brief Class representation of printer object
 */
class Printer : Parcelable {
    /**
     * @brief Obtain the printer device name.
     *
     * @return The name of the printer device
     */
    /**
     * @brief Updates the value of printer device name.
     *
     * @param name The name of the printer device
     */
    var name: String?
    /**
     * @brief Obtain the printer device IP address.
     *
     * @return The printer device IP address
     */
    /**
     * @brief Updates the value of printer device IP Address.
     *
     * @param ipAddress The printer device IP address
     */
    var ipAddress: String? = null
    /**
     * @brief Obtain the printer device MAC address.
     *
     * @return The printer device MAC address
     */
    /**
     * @brief Updates the value of printer device MAC Address.
     *
     * @param macAddress The printer device MAC address
     */
    var macAddress: String?
    /**
     * @brief Obtain the Printer ID.
     *
     * @return The printer ID (mId)
     */
    /**
     * @brief Updates the value of printer ID.
     *
     * @param id Printer ID
     */
    var id = PrinterManager.EMPTY_ID
    /**
     * @brief Obtain the printer device port setting.
     *
     * @retval LPR LPR port setting
     * @retval RAW Raw port setting
     */
    /**
     * @brief Updates the value of printer device port setting.
     *
     * @param portSetting The printer device port setting
     */
    var portSetting: PortSetting? = null
    /**
     * @brief Obtain the printer device configuration.
     *
     * @return The printer device configuration
     */
    /**
     * @brief Updates the value of printer device configuration.
     *
     * @param config The printer device configuration
     */
    var config: Config? = null

    /**
     * @brief Returns the printer's printer type .
     */
    var printerType: String? = null
        private set

    /**
     * @brief Returns if the printer model is not supported and is only set to the default printer model
     */
    // Flag to indicate if printer only defaulted to IS;
    // Currently used to disable listing printers in search
    // but can be used in future implementations for handling
    var isActualPrinterTypeInvalid = false
        private set

    var isEnabledIPPS = false
        private set

    /**
     * @brief Printer port setting.
     */
    enum class PortSetting {
        LPR,  ///< LPR port
        RAW,  ///< Raw port
        IPPS  ///< IPPS port
    }

    /**
     * @brief Printer Constructor. <br></br>
     *
     * Create a printer instance
     *
     * @param name Device Name
     * @param ipAddress IP address
     * @param macAddress MAC address
     */
    constructor(name: String?, ipAddress: String?, macAddress: String?) {
        this.name = name
        this.ipAddress = ipAddress
        this.macAddress = macAddress
        portSetting = PortSetting.LPR
        config = Config()
        initializePrinterType()
    }

    /**
     * @brief Printer Constructor. <br></br>
     *
     * Create a Printer instance
     *
     * @param in Parcel containing the printer information.
     */
    constructor(`in`: Parcel) {
        if (config == null) {
            config = Config()
        }
        name = `in`.readString()
        ipAddress = `in`.readString()
        macAddress = `in`.readString()
        id = `in`.readInt()
        portSetting = when (`in`.readInt()) {
            1 -> PortSetting.RAW
            2 -> PortSetting.IPPS
            else -> PortSetting.LPR
        }
        config!!.readFromParcel(`in`)
        initializePrinterType()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        if (config == null) {
            config = Config()
        }
        out.writeString(name)
        out.writeString(ipAddress)
        out.writeString(macAddress)
        out.writeInt(id)
        out.writeInt(portSetting!!.ordinal)
        config!!.writeToParcel(out)
    }
    // ================================================================================
    // Getter/Setters
    // ================================================================================// Classify CEREZONA S as FT model
    /**
     * @brief Determines if the printer is of the FT or CEREZONA S series
     *
     * @return True if the printers if of the FT or CEREZONA S series, false otherwise
     */
    val isPrinterFTorCEREZONA_S: Boolean
        get() = printerType == AppConstants.PRINTER_MODEL_FT // Classify CEREZONA S as FT model

    /**
     * @brief Determines if the printer is of the GL or OGA series
     *
     * @return True if the printers if of the GL or OGA series, false otherwise
     */
    val isPrinterGLorOGA: Boolean
        get() = printerType == AppConstants.PRINTER_MODEL_GL // Classify OGA as GL model

    /**
     * @brief Initializes the printer's printer type based on the printer's model (name) .
     */
    private fun initializePrinterType() {
        if (name == null || name!!.isEmpty()) {
            printerType = DEFAULT_PRINTER_TYPE
            isActualPrinterTypeInvalid = true
            return
        }
        val IS_Printers = arrayOf("RISO IS1000C-J", "RISO IS1000C-G", "RISO IS950C-G")
        for (printerModel in IS_Printers) {
            if (printerModel.equals(name, ignoreCase = true)) {
                printerType = AppConstants.PRINTER_MODEL_IS
                return
            }
        }
        if (name!!.contains(AppConstants.PRINTER_MODEL_FW)) {
            printerType = AppConstants.PRINTER_MODEL_FW
            return
        }
        if (name!!.contains(AppConstants.PRINTER_MODEL_GD)) {
            printerType = AppConstants.PRINTER_MODEL_GD
            return
        }
        if (name!!.contains(AppConstants.PRINTER_MODEL_FT) || name!!.contains(AppConstants.PRINTER_MODEL_CEREZONA_S)) {
            printerType = AppConstants.PRINTER_MODEL_FT // Classify CEREZONA S as FT model
            return
        }
        if (name!!.contains(AppConstants.PRINTER_MODEL_GL) || name!!.contains(AppConstants.PRINTER_MODEL_OGA)) {
            printerType = AppConstants.PRINTER_MODEL_GL // Classify OGA as GL model

            // Workaround: Only enable IPPS if printer is OGA
            if (name!!.contains(AppConstants.PRINTER_MODEL_OGA)) {
                isEnabledIPPS = true
            }

            return
        }
        printerType = AppConstants.PRINTER_MODEL_IS
        isActualPrinterTypeInvalid = true
    }
    // ================================================================================
    // Internal Class - Printer Config
    // ================================================================================
    /**
     * @class Config
     *
     * @brief Configuration class for printer capabilities
     */
    inner class Config
    /**
     * @brief Config Constructor.
     */
    {
        /**
         * @brief Determines LPR capability for printing.
         *
         * @retval true LPR is enabled
         * @retval false LPR is disabled
         */
        /**
         * @brief Updates the value of mLprAvailable.
         *
         * @param lprAvailable Enable/Disable LPR capability
         */
        var isLprAvailable = true
        /**
         * @brief Determines the Raw capability for printing.
         *
         * @retval true Raw is enabled
         * @retval false Raw is disabled
         */
        /**
         * @brief Updates the value of mRawAvailable.
         *
         * @param isRawAvailable Enable/Disable Raw print capability
         */
        var isRawAvailable = true

        /**
         * @brief Determines the IPPS capability for printing.
         *
         * @retval true IPPS is enabled
         * @retval false IPPS is disabled
         */
        var isIppsAvailable = false
        /**
         * @brief Determines the Booklet finishing capability of the device.
         *
         * @retval true Booklet Finishing is enabled
         * @retval false Booklet Finishing is disabled
         */
        /**
         * @brief Updates the value of mBookletFinishingAvailable.
         *
         * @param bookletFinishingAvailable Enable/Disable Booklet Finishing
         */
        var isBookletFinishingAvailable = true
        /**
         * @brief Determines the Staple capability of the device.
         *
         * @retval true Staple is enabled
         * @retval false Staple is disabled
         */
        /**
         * @brief Updates the value of mStaplerAvailable.
         *
         * @param staplerAvailable Enable/Disable Staple capability
         */
        var isStaplerAvailable = true
        /**
         * @brief Determines the Punch capability of the device for 3 holes.
         *
         * @retval true Punch 3 holes is enabled
         * @retval false Punch 3 holes is disabled
         */
        /**
         * @brief Updates the value of mPunch3Available.
         *
         * @param punch3Available Enable/Disable Punch 3 holes capability
         */
        var isPunch3Available = false
        /**
         * @brief Determines the Punch capability of the device for 4 holes.
         *
         * @retval true Punch 4 holes is enabled
         * @retval false Punch 4 holes is disabled
         */
        /**
         * @brief Updates the value of mPunch4Available.
         *
         * @param punch4Available Enable/Disable Punch 4 holes capability
         */
        var isPunch4Available = true
        /**
         * @brief Determines the TrayFaceDown capability.
         *
         * @retval true TrayFaceDown is enabled
         * @retval false TrayFaceDown is disabled
         */
        /**
         * @brief Updates the value of mTrayFaceDownAvailable.
         *
         * @param trayFaceDownAvailable Enable/Disable TrayFaceDown capability
         */
        var isTrayFaceDownAvailable = true
        /**
         * @brief Determines the TrayTop capability.
         *
         * @retval true TrayTop is enabled
         * @retval false TrayTop is disabled
         */
        /**
         * @brief Updates the value of mTrayTopAvailable.
         *
         * @param trayTopAvailable Enable/Disable TrayTop capability
         */
        var isTrayTopAvailable = true
        /**
         * @brief Determines the TrayStack capability.
         *
         * @retval true TrayStack is enabled
         * @retval false TrayStack is disabled
         */
        /**
         * @brief Updates the value of mTrayStackAvailable.
         *
         * @param trayStackAvailable Enable/Disable TrayStack capability
         */
        var isTrayStackAvailable = true
        /**
         * @brief Determines the External Feeder input tray capability of the device.
         *
         * @retval true External Feeder is enabled
         * @retval false External Feeder is disabled
         */
        /**
         * @brief Updates the value of mExternalFeederAvailable.
         *
         * @param externalFeederAvailable Enable/Disable Punch capability
         */
        var isExternalFeederAvailable = false
        /**
         * @brief Determines the Punch capability of the device.
         *
         * @retval true Punch is disabled
         * @retval false Punch is enabled
         */
        /**
         * @brief Updates the value of mPunch0Available.
         *
         * @param punch0Available Enable/Disable Punch capability
         */
        var isPunch0Available = false

        /**
         * @brief Determines the Punch capability of the device.
         *
         * @retval true Punch is enabled
         * @retval false Punch is disabled
         */
        val isPunchAvailable: Boolean
            get() = !isPunch0Available && (isPunch3Available || isPunch4Available)

        /**
         * @brief Saves the value of all class members to a Parcel.
         *
         * @param out Destination parcel where to save the data
         */
        fun writeToParcel(out: Parcel) {
            val config = booleanArrayOf(
                isLprAvailable,
                isRawAvailable,
                isBookletFinishingAvailable,
                isStaplerAvailable,
                isPunch3Available,
                isPunch4Available,
                isTrayFaceDownAvailable,
                isTrayTopAvailable,
                isTrayStackAvailable,
                isExternalFeederAvailable,
                isPunch0Available,
                isIppsAvailable
            )
            out.writeBooleanArray(config)
        }

        /**
         * @brief Retrieves the value of all the class members from a Parcel.
         *
         * @param in Source parcel where to retrieve the data
         */
        fun readFromParcel(`in`: Parcel) {
            val bConfig = booleanArrayOf(
                isLprAvailable,
                isRawAvailable,
                isBookletFinishingAvailable,
                isStaplerAvailable,
                isPunch3Available,
                isPunch4Available,
                isTrayFaceDownAvailable,
                isTrayTopAvailable,
                isTrayStackAvailable,
                isExternalFeederAvailable,
                isPunch0Available,
                isIppsAvailable
            )
            `in`.readBooleanArray(bConfig)
            config!!.isLprAvailable = bConfig[0]
            config!!.isRawAvailable = bConfig[1]
            config!!.isBookletFinishingAvailable = bConfig[2]
            config!!.isStaplerAvailable = bConfig[3]
            config!!.isPunch3Available = bConfig[4]
            config!!.isPunch4Available = bConfig[5]
            config!!.isTrayFaceDownAvailable = bConfig[6]
            config!!.isTrayTopAvailable = bConfig[7]
            config!!.isTrayStackAvailable = bConfig[8]
            config!!.isExternalFeederAvailable = bConfig[9]
            config!!.isPunch0Available = bConfig[10]
            config!!.isIppsAvailable = bConfig[11]
        }
    }

    companion object {
        //for the meantime, set the fallthrough printer type to IS since it is the originally
        //supported printer type
        private const val DEFAULT_PRINTER_TYPE = AppConstants.PRINTER_MODEL_IS
        @JvmField
        val CREATOR: Creator<Printer?> = object : Creator<Printer?> {
            override fun createFromParcel(`in`: Parcel): Printer {
                return Printer(`in`)
            }

            override fun newArray(size: Int): Array<Printer?> {
                return arrayOfNulls(size)
            }
        }
    }
}