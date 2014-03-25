/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SDADrawerLayout.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.widget;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SDADrawerLayout extends DrawerLayout {
    
    private boolean mPreventInterceptTouches;

    public SDADrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SDADrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SDADrawerLayout(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mPreventInterceptTouches) {
            return false;
        }
        
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
    
    public void setPreventInterceptTouches(boolean preventInterceptTouches) {
        mPreventInterceptTouches = preventInterceptTouches;
    }
}

