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

public class Printer implements Parcelable {
    private int mId;
    private String mName;
    private String mIpAddress;
    private int mPortSetting;
    private boolean mLpr;
    private boolean mRaw;
    private boolean mPagination;
    private boolean mDuplex;
    private boolean mBookletBinding;
    private boolean mStaple;
    private boolean mBind;
    
    public Printer(String name, String ipAddress, boolean isDefault, PrintSettings printSettings) {
        super();
        mName = name;
        mIpAddress = ipAddress;
        mPortSetting = 0;
        mLpr = true;
        mRaw = true;
        mPagination = true;
        mDuplex = true;
        mBookletBinding = true;
        mStaple = true;
        mBind = true;
        
    }
    
    public Printer(Parcel source) {
        if (source != null) {
            mName = source.readString();
            mIpAddress = source.readString();
        }
    }
    
    public static final Parcelable.Creator<Printer> CREATOR = new Parcelable.Creator<Printer>() {
        @Override
        public Printer createFromParcel(Parcel source) {
            return new Printer(source);
        }
        
        @Override
        public Printer[] newArray(int size) {
            return new Printer[size];
        }
    };
    
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
    
    public boolean getLpr() {
        return mLpr;
    }
    
    public void setLpr(boolean lpr) {
        this.mLpr = lpr;
    }
    
    public boolean getRaw() {
        return mRaw;
    }
    
    public void setRaw(boolean raw) {
        this.mRaw = raw;
    }
    
    public boolean getPagination() {
        return mPagination;
    }
    
    public void setPagination(boolean pagination) {
        this.mPagination = pagination;
    }
    
    public boolean getDuplex() {
        return mDuplex;
    }
    
    public void setDuplex(boolean duplex) {
        this.mDuplex = duplex;
    }
    
    public boolean getBookletBinding() {
        return mBookletBinding;
    }
    
    public void setBookletBinding(boolean bookletBinding) {
        this.mBookletBinding = bookletBinding;
    }
    
    public boolean getStaple() {
        return mStaple;
    }
    
    public void setStaple(boolean staple) {
        this.mStaple = staple;
    }
    
    public boolean getBind() {
        return mBind;
    }
    
    public void setBind(boolean bind) {
        this.mBind = bind;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        return;
    }
}
