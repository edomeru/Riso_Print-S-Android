/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Printer.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Printer object
 */
public class Printer implements Parcelable {
    private String mName = null;
    private String mIpAddress = null;
    private int mId = PrinterManager.EMPTY_ID;
    private int mPortSetting = 0;
    
    private Config mConfig = null;
    
    /**
     * Printer Constructor
     * <p>
     * Create a printer instance
     * 
     * @param name
     *            Device Name
     * @param ipAddress
     *            IP address
     */
    public Printer(String name, String ipAddress) {
        mName = name;
        mIpAddress = ipAddress;
        mPortSetting = 0;
        
        mConfig = new Config();
    }
    
    /** {@inheritDoc} */
    public static final Printer.Creator<Printer> CREATOR = new Parcelable.Creator<Printer>() {
        /** {@inheritDoc} */
        public Printer createFromParcel(Parcel in) {
            return new Printer(in);
        }
        
        /** {@inheritDoc} */
        public Printer[] newArray(int size) {
            return new Printer[size];
        }
    };
    
    /**
     * Printer Constructor
     * <p>
     * Create a printer instance
     * 
     * @param in
     *            Parcel containing the printer information.
     */
    public Printer(Parcel in) {
        if (mConfig == null) {
            mConfig = new Config();
        }
        
        mName = in.readString();
        mIpAddress = in.readString();
        mId = in.readInt();
        mPortSetting = in.readInt();
        mConfig.readFromParcel(in);
    }
    
    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }
    
    /** {@inheritDoc} */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        if (mConfig == null) {
            mConfig = new Config();
        }
        
        out.writeString(mName);
        out.writeString(mIpAddress);
        out.writeInt(mId);
        out.writeInt(mPortSetting);
        mConfig.writeToParcel(out);
    }
    
    // ================================================================================
    // Getter/Setters
    // ================================================================================
    
    /**
     * @return the ID (mId)
     */
    public int getId() {
        return mId;
    }
    
    /**
     * updates the value of mId
     * 
     * @param id
     */
    public void setId(int id) {
        this.mId = id;
    }
    
    /**
     * @return the name (mName)
     */
    public String getName() {
        return mName;
    }
    
    /**
     * updates the value of mName
     * 
     * @param name
     */
    public void setName(String name) {
        this.mName = name;
    }
    
    /**
     * @return the IP address (mIpAddress)
     */
    public String getIpAddress() {
        return mIpAddress;
    }
    
    /**
     * updates the value of mIpAddress
     * 
     * @param ipAddress
     */
    public void setIpAddress(String ipAddress) {
        this.mIpAddress = ipAddress;
    }
    
    /**
     * @return the port setting (mPortSetting)
     */
    public int getPortSetting() {
        return mPortSetting;
    }
    
    /**
     * updates the value of mPortSetting
     * 
     * @param portSetting
     */
    public void setPortSetting(int portSetting) {
        this.mPortSetting = portSetting;
    }
    
    /**
     * @return the printer configuration (mConfig)
     */
    public Config getConfig() {
        return mConfig;
    }
    
    /**
     * updates the value of mConfig
     * 
     * @param config
     */
    public void setConfig(Config config) {
        mConfig = config;
    }

    /**
     * Gets the print settings corresponding to the Printer
     * 
     * @return Printer's print settings
     */
    // TODO: For update
    public PrintSettings getPrintSettings() {
        return new PrintSettings(getId());
    }
    
    // ================================================================================
    // Internal Class - Printer Config
    // ================================================================================
    
    /**
     * Printer Capabilities
     */
    public class Config {
        private boolean mLprAvailable;
        private boolean mRawAvailable;
        private boolean mBookletAvailable;
        private boolean mStaplerAvailable;
        private boolean mPunch3Available;
        private boolean mPunch4Available;
        private boolean mTrayFaceDownAvailable;
        private boolean mTrayTopAvailable;
        private boolean mTrayStackAvailable;
        
        /**
         * Config Constructor
         * <p>
         * Create a config instance.
         */
        public Config() {
            mLprAvailable = true;
            mRawAvailable = true;
            mBookletAvailable = true;
            mStaplerAvailable = true;
            mPunch3Available = false;
            mPunch4Available = true;
            mTrayFaceDownAvailable = true;
            mTrayTopAvailable = true;
            mTrayStackAvailable = true;
        }
        
        /**
         * @return the mLprAvailable
         */
        public boolean isLprAvailable() {
            return mLprAvailable;
        }
        
        /**
         * updates the value of mLprAvailable
         * 
         * @param lprAvailable
         */
        public void setLprAvailable(boolean lprAvailable) {
            this.mLprAvailable = lprAvailable;
        }
        
        /**
         * @return the mRawAvailable
         */
        public boolean isRawAvailable() {
            return mRawAvailable;
        }
        
        /**
         * updates the value of mRawAvailable
         * 
         * @param rawAvailable
         */
        public void setRawAvailable(boolean rawAvailable) {
            this.mRawAvailable = rawAvailable;
        }
        
        /**
         * @return the mBookletAvailable
         */
        public boolean isBookletAvailable() {
            return mBookletAvailable;
        }
        
        /**
         * updates the value of mBookletAvailable
         * 
         * @param bookletAvailable
         */
        public void setBookletAvailable(boolean bookletAvailable) {
            this.mBookletAvailable = bookletAvailable;
        }
        
        /**
         * @return the mStaplerAvailable
         */
        public boolean isStaplerAvailable() {
            return mStaplerAvailable;
        }
        
        /**
         * updates the value of mStaplerAvailable
         * 
         * @param staplerAvailable
         */
        public void setStaplerAvailable(boolean staplerAvailable) {
            this.mStaplerAvailable = staplerAvailable;
        }
        
        /**
         * @return Whether punch is available or not.
         */
        public boolean isPunchAvailable() {
            return mPunch3Available || mPunch4Available;
        }
        
        /**
         * @return the mPunch3Available
         */
        public boolean isPunch3Available() {
            return mPunch3Available;
        }
        
        /**
         * updates the value of mPunch4Available
         * 
         * @param punch3Available
         */
        public void setPunch3Available(boolean punch3Available) {
            this.mPunch3Available = punch3Available;
        }
        
        /**
         * @return the mPunch4Available
         */
        public boolean isPunch4Available() {
            return mPunch4Available;
        }
        
        /**
         * updates the value of mPunch4Available
         * 
         * @param punch4Available
         */
        public void setPunch4Available(boolean punch4Available) {
            this.mPunch4Available = punch4Available;
        }
        
        /**
         * @return the mTrayFaceDownAvailable
         */
        public boolean isTrayFaceDownAvailable() {
            return mTrayFaceDownAvailable;
        }
        
        /**
         * updates the value of mTrayFaceDownAvailable
         * 
         * @param trayFaceDownAvailable
         */
        public void setTrayFaceDownAvailable(boolean trayFaceDownAvailable) {
            this.mTrayFaceDownAvailable = trayFaceDownAvailable;
        }
        
        /**
         * @return the mTrayTopAvailable
         */
        public boolean isTrayTopAvailable() {
            return mTrayTopAvailable;
        }
        
        /**
         * updates the value of mTrayTopAvailable
         * 
         * @param trayTopAvailable
         */
        public void setTrayTopAvailable(boolean trayTopAvailable) {
            this.mTrayTopAvailable = trayTopAvailable;
        }
        
        /**
         * @return the mTrayStackAvailable
         */
        public boolean isTrayStackAvailable() {
            return mTrayStackAvailable;
        }
        
        /**
         * updates the value of mTrayStackAvailable
         * 
         * @param trayStackAvailable
         */
        public void setTrayStackAvailable(boolean trayStackAvailable) {
            this.mTrayStackAvailable = trayStackAvailable;
        }
        
        /**
         * saves the value of all class members to a Parcel
         * 
         * @param out
         */
        public void writeToParcel(Parcel out) {
            boolean[] config = new boolean[] { mLprAvailable, mRawAvailable, mBookletAvailable, mStaplerAvailable, mPunch3Available,
                    mPunch4Available, mTrayFaceDownAvailable, mTrayTopAvailable, mTrayStackAvailable };
            
            out.writeBooleanArray(config);
        }
        
        /**
         * retrieves the value of all the class members from a Parcel
         * 
         * @param in
         */
        public void readFromParcel(Parcel in) {
            boolean[] config = new boolean[] { mLprAvailable, mRawAvailable, mBookletAvailable, mStaplerAvailable, mPunch3Available,
                    mPunch4Available, mTrayFaceDownAvailable, mTrayTopAvailable, mTrayStackAvailable };
            
            in.readBooleanArray(config);
            mConfig.mLprAvailable = config[0];
            mConfig.mRawAvailable = config[1];
            mConfig.mBookletAvailable = config[2];
            mConfig.mStaplerAvailable = config[3];
            mConfig.mPunch3Available = config[4];
            mConfig.mPunch4Available = config[5];
            mConfig.mTrayFaceDownAvailable = config[6];
            mConfig.mTrayTopAvailable = config[7];
            mConfig.mTrayStackAvailable = config[8];
        }
    }
}
