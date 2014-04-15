/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Printer.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;

public class Printer {
    private PrintSettings mPrintSettings = null;
    private int mId;
    private String mName;
    private String mIpAddress;
    private int mPortSetting;
    
    private Config mConfig;
    private boolean mOnline;
    
    public Printer(String name, String ipAddress, PrintSettings printSettings) {
        super();
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
    
    // ================================================================================
    // Getter/Setters
    // ================================================================================
    
    public int getId() {
        return mId;
    }
    
    public void setId(int id) {
        this.mId = id;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setName(String name) {
        this.mName = name;
    }
    
    public String getIpAddress() {
        return mIpAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.mIpAddress = ipAddress;
    }
    
    public int getPortSetting() {
        return mPortSetting;
    }
    
    public void setPortSetting(int portSetting) {
        this.mPortSetting = portSetting;
    }
    
    public Config getConfig() {
        return mConfig;
    }
    
    public void setConfig(Config config) {
        mConfig = config;
    }
    
    public PrintSettings getPrintSettings() {
        return mPrintSettings;
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        this.mPrintSettings = new PrintSettings(printSettings);
    }
    
    public boolean getOnlineStatus() {
        return mOnline;
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
        
        public boolean isLprAvailable() {
            return mLprAvailable;
        }
        
        public void setLprAvailable(boolean lprAvailable) {
            this.mLprAvailable = lprAvailable;
        }
        
        public boolean isRawAvailable() {
            return mRawAvailable;
        }
        
        public void setRawAvailable(boolean rawAvailable) {
            this.mRawAvailable = rawAvailable;
        }
        
        public boolean isBookletAvailable() {
            return mBookletAvailable;
        }
        
        public void setBookletAvailable(boolean bookletAvailable) {
            this.mBookletAvailable = bookletAvailable;
        }
        
        public boolean isStaplerAvailable() {
            return mStaplerAvailable;
        }
        
        public void setStaplerAvailable(boolean staplerAvailable) {
            this.mStaplerAvailable = staplerAvailable;
        }
        
        public boolean isPunch4Available() {
            return mPunch4Available;
        }
        
        public void setPunch4Available(boolean punch4Available) {
            this.mPunch4Available = punch4Available;
        }
        
        public boolean isTrayFaceDownAvailable() {
            return mTrayFaceDownAvailable;
        }
        
        public void setTrayFaceDownAvailable(boolean trayFaceDownAvailable) {
            this.mTrayFaceDownAvailable = trayFaceDownAvailable;
        }
        
        public boolean isTrayAutoStackAvailable() {
            return mTrayAutoStackAvailable;
        }
        
        public void setTrayAutoStackAvailable(boolean trayAutoStackAvailable) {
            this.mTrayAutoStackAvailable = trayAutoStackAvailable;
        }
        
        public boolean isTrayTopAvailable() {
            return mTrayTopAvailable;
        }
        
        public void setTrayTopAvailable(boolean trayTopAvailable) {
            this.mTrayTopAvailable = trayTopAvailable;
        }
        
        public boolean isTrayStackAvailable() {
            return mTrayStackAvailable;
        }
        
        public void setTrayStackAvailable(boolean trayStackAvailable) {
            this.mTrayStackAvailable = trayStackAvailable;
        }
    }
}
