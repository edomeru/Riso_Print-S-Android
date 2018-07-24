/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Printer.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;

/**
 * @class Printer
 * 
 * @brief Class representation of printer object
 */
public class Printer implements Parcelable {

    //for the meantime, set the fallthrough printer type to IS since it is the originally
    //supported printer type
    private static final String DEFAULT_PRINTER_TYPE = AppConstants.PRINTER_MODEL_IS;

    private String mName = null;
    private String mIpAddress = null;
    private int mId = PrinterManager.EMPTY_ID;
    private PortSetting mPortSetting = PortSetting.LPR;
    
    private Config mConfig = null;

    private String mPrinterType = null;

    // Flag to indicate if printer only defaulted to IS;
    // Currently used to disable listing printers in search
    // but can be used in future implementations for handling
    private boolean actualPrinterTypeInvalid = false;

    /**
     * @brief Printer port setting.
     */
    public enum PortSetting {
        LPR, ///< LPR port
        RAW ///< Raw port
    }
    
    /**
     * @brief Printer Constructor. <br>
     *
     * Create a printer instance
     * 
     * @param name Device Name
     * @param ipAddress IP address
     */
    public Printer(String name, String ipAddress) {
        mName = name;
        mIpAddress = ipAddress;
        mPortSetting = PortSetting.LPR;
        
        mConfig = new Config();
        initializePrinterType();
    }
    
    public static final Printer.Creator<Printer> CREATOR = new Parcelable.Creator<Printer>() {
        public Printer createFromParcel(Parcel in) {
            return new Printer(in);
        }
        
        public Printer[] newArray(int size) {
            return new Printer[size];
        }
    };
    
    /**
     * @brief Printer Constructor. <br>
     *
     * Create a Printer instance
     * 
     * @param in Parcel containing the printer information.
     */
    public Printer(Parcel in) {
        if (mConfig == null) {
            mConfig = new Config();
        }
        
        mName = in.readString();
        mIpAddress = in.readString();
        mId = in.readInt();
        switch(in.readInt()) {
            case 1:
                mPortSetting = PortSetting.RAW;
                break;
            default:
                mPortSetting = PortSetting.LPR;
                break;
        }
        mConfig.readFromParcel(in);
        initializePrinterType();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel out, int flags) {
        if (mConfig == null) {
            mConfig = new Config();
        }
        
        out.writeString(mName);
        out.writeString(mIpAddress);
        out.writeInt(mId);
        out.writeInt(mPortSetting.ordinal());
        mConfig.writeToParcel(out);
    }
    
    // ================================================================================
    // Getter/Setters
    // ================================================================================
    
    /**
     * @brief Obtain the Printer ID.
     * 
     * @return The printer ID (mId)
     */
    public int getId() {
        return mId;
    }
    
    /**
     * @brief Updates the value of printer ID.
     * 
     * @param id Printer ID
     */
    public void setId(int id) {
        this.mId = id;
    }
    
    /**
     * @brief Obtain the printer device name.
     * 
     * @return The name of the printer device
     */
    public String getName() {
        return mName;
    }
    
    /**
     * @brief Updates the value of printer device name.
     * 
     * @param name The name of the printer device
     */
    public void setName(String name) {
        this.mName = name;
    }
    
    /**
     * @brief Obtain the printer device IP address.
     * 
     * @return The printer device IP address
     */
    public String getIpAddress() {
        return mIpAddress;
    }
    
    /**
     * @brief Updates the value of printer device IP Address.
     * 
     * @param ipAddress The printer device IP address
     */
    public void setIpAddress(String ipAddress) {
        this.mIpAddress = ipAddress;
    }
    
    /**
     * @brief Obtain the printer device port setting.
     * 
     * @retval LPR LPR port setting 
     * @retval RAW Raw port setting
     */
    public PortSetting getPortSetting() {
        return mPortSetting;
    }
    
    /**
     * @brief Updates the value of printer device port setting.
     * 
     * @param portSetting The printer device port setting 
     */
    public void setPortSetting(PortSetting portSetting) {
        this.mPortSetting = portSetting;
    }
    
    /**
     * @brief Obtain the printer device configuration.
     * 
     * @return The printer device configuration
     */
    public Config getConfig() {
        return mConfig;
    }
    
    /**
     * @brief Updates the value of printer device configuration.
     * 
     * @param config The printer device configuration
     */
    public void setConfig(Config config) {
        mConfig = config;
    }

    /**
     * @brief Returns the printer's printer type .
     */
    public String getPrinterType(){
        return mPrinterType;
    }

    /**
     * @brief Returns if the printer model is not supported and is only set to the default printer model
     */
    public boolean isActualPrinterTypeInvalid() {
        return actualPrinterTypeInvalid;
    }

    /**
     * @brief Initializes the printer's printer type based on the printer's model (name) .
     */
    private void initializePrinterType(){
        if(mName == null || mName.isEmpty()){
            mPrinterType =  DEFAULT_PRINTER_TYPE;
            actualPrinterTypeInvalid = true;
            return;
        }

       final String[] IS_Printers = {"RISO IS1000C-J", "RISO IS1000C-G", "RISO IS950C-G"} ;

        for(String printerModel : IS_Printers){
            if(printerModel.equalsIgnoreCase(mName)){
                mPrinterType = AppConstants.PRINTER_MODEL_IS;
                return ;
            }
        }

        if(mName.contains(AppConstants.PRINTER_MODEL_FW)){
            mPrinterType = AppConstants.PRINTER_MODEL_FW;
            return;
        }

        if(mName.contains(AppConstants.PRINTER_MODEL_GD)){
            mPrinterType = AppConstants.PRINTER_MODEL_GD;
            return;
        }

        if(mName.contains(AppConstants.PRINTER_MODEL_RAG)){
            mPrinterType = AppConstants.PRINTER_MODEL_RAG;
            return;
        }

        mPrinterType =  AppConstants.PRINTER_MODEL_IS;
        actualPrinterTypeInvalid = true;
    }

    // ================================================================================
    // Internal Class - Printer Config
    // ================================================================================
    
    /**
     * @class Config
     * 
     * @brief Configuration class for printer capabilities
     */
    public class Config {
        private boolean mLprAvailable;
        private boolean mRawAvailable;
        private boolean mBookletFinishingAvailable;
        private boolean mStaplerAvailable;
        private boolean mPunch3Available;
        private boolean mPunch4Available;
        private boolean mTrayFaceDownAvailable;
        private boolean mTrayTopAvailable;
        private boolean mTrayStackAvailable;
        private boolean mExternalFeederAvailable;
        private boolean mPunch0Available;

        /**
         * @brief Config Constructor.
         */
        public Config() {
            mLprAvailable = true;
            mRawAvailable = true;
            mBookletFinishingAvailable = true;
            mStaplerAvailable = true;
            mPunch3Available = false;
            mPunch4Available = true;
            mTrayFaceDownAvailable = true;
            mTrayTopAvailable = true;
            mTrayStackAvailable = true;
            mExternalFeederAvailable = false;
            mPunch0Available = true;
        }
        
        /**
         * @brief Determines LPR capability for printing.
         * 
         * @retval true LPR is enabled
         * @retval false LPR is disabled
         */
        public boolean isLprAvailable() {
            return mLprAvailable;
        }
        
        /**
         * @brief Updates the value of mLprAvailable.
         * 
         * @param lprAvailable Enable/Disable LPR capability
         */
        public void setLprAvailable(boolean lprAvailable) {
            this.mLprAvailable = lprAvailable;
        }
        
        /**
         * @brief Determines the Raw capability for printing.
         * 
         * @retval true Raw is enabled
         * @retval false Raw is disabled
         */
        public boolean isRawAvailable() {
            return mRawAvailable;
        }
        
        /**
         * @brief Updates the value of mRawAvailable.
         * 
         * @param rawAvailable Enable/Disable Raw print capability
         */
        public void setRawAvailable(boolean rawAvailable) {
            this.mRawAvailable = rawAvailable;
        }
        
        /**
         * @brief Determines the Booklet finishing capability of the device.
         * 
         * @retval true Booklet Finishing is enabled
         * @retval false Booklet Finishing is disabled 
         */
        public boolean isBookletFinishingAvailable() {
            return mBookletFinishingAvailable;
        }
        
        /**
         * @brief Updates the value of mBookletFinishingAvailable.
         * 
         * @param bookletFinishingAvailable Enable/Disable Booklet Finishing
         */
        public void setBookletFinishingAvailable(boolean bookletFinishingAvailable) {
            this.mBookletFinishingAvailable = bookletFinishingAvailable;
        }
        
        /**
         * @brief Determines the Staple capability of the device.
         * 
         * @retval true Staple is enabled
         * @retval false Staple is disabled
         */
        public boolean isStaplerAvailable() {
            return mStaplerAvailable;
        }
        
        /**
         * @brief Updates the value of mStaplerAvailable.
         * 
         * @param staplerAvailable Enable/Disable Staple capability
         */
        public void setStaplerAvailable(boolean staplerAvailable) {
            this.mStaplerAvailable = staplerAvailable;
        }
        
        /**
         * @brief Determines the Punch capability of the device.
         * 
         * @retval true Punch is enabled
         * @retval false Punch is disabled
         */
        public boolean isPunchAvailable() {
            return mPunch0Available && (mPunch3Available || mPunch4Available);
        }
        
        /**
         * @brief Determines the Punch capability of the device for 3 holes.
         * 
         * @retval true Punch 3 holes is enabled
         * @retval false Punch 3 holes is disabled
         */
        public boolean isPunch3Available() {
            return mPunch3Available;
        }
        
        /**
         * @brief Updates the value of mPunch3Available.
         * 
         * @param punch3Available Enable/Disable Punch 3 holes capability
         */
        public void setPunch3Available(boolean punch3Available) {
            this.mPunch3Available = punch3Available;
        }
        
        /**
         * @brief Determines the Punch capability of the device for 4 holes.
         *  
         * @retval true Punch 4 holes is enabled
         * @retval false Punch 4 holes is disabled
         */
        public boolean isPunch4Available() {
            return mPunch4Available;
        }
        
        /**
         * @brief Updates the value of mPunch4Available.
         * 
         * @param punch4Available Enable/Disable Punch 4 holes capability
         */
        public void setPunch4Available(boolean punch4Available) {
            this.mPunch4Available = punch4Available;
        }
        
        /**
         * @brief Determines the TrayFaceDown capability.
         * 
         * @retval true TrayFaceDown is enabled
         * @retval false TrayFaceDown is disabled
         */
        public boolean isTrayFaceDownAvailable() {
            return mTrayFaceDownAvailable;
        }
        
        /**
         * @brief Updates the value of mTrayFaceDownAvailable.
         * 
         * @param trayFaceDownAvailable Enable/Disable TrayFaceDown capability
         */
        public void setTrayFaceDownAvailable(boolean trayFaceDownAvailable) {
            this.mTrayFaceDownAvailable = trayFaceDownAvailable;
        }
        
        /**
         * @brief Determines the TrayTop capability.
         * 
         * @retval true TrayTop is enabled
         * @retval false TrayTop is disabled
         */
        public boolean isTrayTopAvailable() {
            return mTrayTopAvailable;
        }
        
        /**
         * @brief Updates the value of mTrayTopAvailable.
         * 
         * @param trayTopAvailable Enable/Disable TrayTop capability
         */
        public void setTrayTopAvailable(boolean trayTopAvailable) {
            this.mTrayTopAvailable = trayTopAvailable;
        }
        
        /**
         * @brief Determines the TrayStack capability.
         * 
         * @retval true TrayStack is enabled
         * @retval false TrayStack is disabled
         */
        public boolean isTrayStackAvailable() {
            return mTrayStackAvailable;
        }
        
        /**
         * @brief Updates the value of mTrayStackAvailable.
         * 
         * @param trayStackAvailable Enable/Disable TrayStack capability
         */
        public void setTrayStackAvailable(boolean trayStackAvailable) {
            this.mTrayStackAvailable = trayStackAvailable;
        }

        /**
         * @brief Determines the External Feeder input tray capability of the device.
         *
         * @retval true External Feeder is enabled
         * @retval false External Feeder is disabled
         */
        public boolean isExternalFeederAvailable() {
            return mExternalFeederAvailable;
        }

        /**
         * @brief Updates the value of mExternalFeederAvailable.
         *
         * @param externalFeederAvailable Enable/Disable Punch capability
         */
        public void setExternalFeederAvailable(boolean externalFeederAvailable) {
            this.mExternalFeederAvailable = externalFeederAvailable;
        }

        /**
         * @brief Determines the Punch capability of the device.
         *
         * @retval true Punch is enabled
         * @retval false Punch is disabled
         */
        public boolean isPunch0Available() {
            return mPunch0Available;
        }

        /**
         * @brief Updates the value of mPunch0Available.
         *
         * @param punch0Available Enable/Disable Punch capability
         */
        public void setPunch0Available(boolean punch0Available) {
            this.mPunch0Available = punch0Available;
        }

        /**
         * @brief Saves the value of all class members to a Parcel.
         * 
         * @param out Destination parcel where to save the data
         */
        public void writeToParcel(Parcel out) {
            boolean[] config = new boolean[] { mLprAvailable, mRawAvailable, mBookletFinishingAvailable, mStaplerAvailable, mPunch3Available,
                    mPunch4Available, mTrayFaceDownAvailable, mTrayTopAvailable, mTrayStackAvailable,
                    mExternalFeederAvailable, mPunch0Available };
            
            out.writeBooleanArray(config);
        }
        
        /**
         * @brief Retrieves the value of all the class members from a Parcel.
         * 
         * @param in Source parcel where to retrieve the data
         */
        public void readFromParcel(Parcel in) {
            boolean[] config = new boolean[] { mLprAvailable, mRawAvailable, mBookletFinishingAvailable, mStaplerAvailable, mPunch3Available,
                    mPunch4Available, mTrayFaceDownAvailable, mTrayTopAvailable, mTrayStackAvailable,
                    mExternalFeederAvailable, mPunch0Available };
            
            in.readBooleanArray(config);
            mConfig.mLprAvailable = config[0];
            mConfig.mRawAvailable = config[1];
            mConfig.mBookletFinishingAvailable = config[2];
            mConfig.mStaplerAvailable = config[3];
            mConfig.mPunch3Available = config[4];
            mConfig.mPunch4Available = config[5];
            mConfig.mTrayFaceDownAvailable = config[6];
            mConfig.mTrayTopAvailable = config[7];
            mConfig.mTrayStackAvailable = config[8];
            mConfig.mExternalFeederAvailable = config[9];
            mConfig.mPunch0Available = config[10];
        }
    }
}