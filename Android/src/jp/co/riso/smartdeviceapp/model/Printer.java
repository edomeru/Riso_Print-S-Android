/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Printer.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;


public class Printer {
    private int mId;
    private String mName;
    private String mIpAddress;
    private int mPortSetting;
    
    private Config mConfig;
    private boolean mOnline;
    
    public Printer(String name, String ipAddress) {
        mName = name;
        mIpAddress = ipAddress;
        mPortSetting = 0;
        
        mConfig = new Config();
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
     * @return the online status (mOnline)
     */
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
    }
}
