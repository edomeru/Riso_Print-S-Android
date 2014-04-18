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

public class Printer implements Parcelable {
    private PrintSettings mPrintSettings = null;
    private String mName = null;
    private String mIpAddress = null;
    private int mId = PrinterManager.EMPTY_ID;
    private int mPortSetting = 0;
    
    private Config mConfig = null;
    
    public Printer(String name, String ipAddress, PrintSettings printSettings) {
        mName = name;
        mIpAddress = ipAddress;
        mPortSetting = 0;
        
        mConfig = new Config();
        
        if (printSettings == null) {
            mPrintSettings = new PrintSettings();
        } else {
            mPrintSettings = new PrintSettings(printSettings);
        }
    }
    
    public Printer(String name, String ipAddress, boolean isDefault, PrintSettings printSettings) {
        this(name, ipAddress, printSettings);
    }
    
    public static final Printer.Creator<Printer> CREATOR = new Parcelable.Creator<Printer>() {
        public Printer createFromParcel(Parcel in) {
            return new Printer(in);
        }
        
        public Printer[] newArray(int size) {
            return new Printer[size];
        }
    };
    
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
     * @return the print settings (mPrintSettings)
     */
    public PrintSettings getPrintSettings() {
        return mPrintSettings;
    }
    
    /**
     * updates the value of mPrintSettings
     * 
     * @param printSettings
     */
    public void setPrintSettings(PrintSettings printSettings) {
        this.mPrintSettings = new PrintSettings(printSettings);
    }
    
    // ================================================================================
    // Internal Class - Printer Config
    // ================================================================================
    
    public class Config {
        public boolean mLprAvailable;
        public boolean mRawAvailable;
        public boolean mBookletAvailable;
        public boolean mStaplerAvailable;
        public boolean mPunch4Available;
        public boolean mTrayFaceDownAvailable;
        public boolean mTrayAutoStackAvailable;
        public boolean mTrayTopAvailable;
        public boolean mTrayStackAvailable;
        
        public Config() {
            mBookletAvailable = true;
            mStaplerAvailable = true;
            mPunch4Available = true;
            mTrayFaceDownAvailable = true;
            mTrayAutoStackAvailable = true;
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
         * @return the mTrayAutoStackAvailable
         */
        public boolean isTrayAutoStackAvailable() {
            return mTrayAutoStackAvailable;
        }
        
        /**
         * updates the value of mTrayAutoStackAvailable
         * 
         * @param trayAutoStackAvailable
         */
        public void setTrayAutoStackAvailable(boolean trayAutoStackAvailable) {
            this.mTrayAutoStackAvailable = trayAutoStackAvailable;
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
        
        public void writeToParcel(Parcel out) {
            boolean[] config = new boolean[] { mLprAvailable, mRawAvailable, mBookletAvailable, mStaplerAvailable, mPunch4Available, mTrayFaceDownAvailable,
                    mTrayAutoStackAvailable, mTrayTopAvailable, mTrayStackAvailable };
            
            out.writeBooleanArray(config);
        }
        
        public void readFromParcel(Parcel in) {
            boolean[] config = new boolean[] { mLprAvailable, mRawAvailable, mBookletAvailable, mStaplerAvailable, mPunch4Available, mTrayFaceDownAvailable,
                    mTrayAutoStackAvailable, mTrayTopAvailable, mTrayStackAvailable };
            
            in.readBooleanArray(config);
            mConfig.mLprAvailable = config[0];
            mConfig.mRawAvailable = config[1];
            mConfig.mBookletAvailable = config[2];
            mConfig.mStaplerAvailable = config[3];
            mConfig.mPunch4Available = config[4];
            mConfig.mTrayFaceDownAvailable = config[5];
            mConfig.mTrayAutoStackAvailable = config[6];
            mConfig.mTrayTopAvailable = config[7];
            mConfig.mTrayStackAvailable = config[8];
        }
    }
}
