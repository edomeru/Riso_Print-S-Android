/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SDADrawerLayout.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.widget;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartprint.R;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

/**
 * @class SDADrawerLayout
 * 
 * @brief Subclass of DrawerLayout class.
 * 
 * Adds following functionalities:
 * <ol>
 * <li> Prevent touch intercept of the main view </li>
 * <li> Prevent dragging from drawer views </li>
 * </ol> 
 */
public class SDADrawerLayout extends DrawerLayout {
    
    private boolean mPreventInterceptTouches = false;
    
    /**
     * @brief Constructs a new DrawerLayout with a Context object
     * 
     * @param context A Context object used to access application assets 
     */
    public SDADrawerLayout(Context context) {
        super(context);
    }
    
    /**
     * @brief Constructs a new DrawerLayout with layout parameters.
     * 
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent 
     */
    public SDADrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * @brief Constructs a new DrawerLayout with layout parameters and a default style.
     * 
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent 
     * @param defStyle The default style resource ID  
     */
    public SDADrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mPreventInterceptTouches) {
            return false;
        }
        if (isDrawerOpen(Gravity.LEFT)) {
            View leftView = findViewById(R.id.leftLayout);
            if (leftView != null) {
                if (AppUtils.checkViewHitTest(leftView, (int) ev.getRawX(), (int) ev.getRawY())) {
                    return false;
                }
            }
        }        
        if (isDrawerOpen(Gravity.RIGHT)) {
            View rightView = findViewById(R.id.rightLayout);
            if (rightView != null) {
                if (AppUtils.checkViewHitTest(rightView, (int) ev.getRawX(), (int) ev.getRawY())) {
                    return false;
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
    
    /**
     * @brief Enable the prevention of touch intercept during onInterceptTouchEvent
     * 
     * @param preventInterceptTouches DrawerLayout should prevent touch interception
     */
    public void setPreventInterceptTouches(boolean preventInterceptTouches) {
        mPreventInterceptTouches = preventInterceptTouches;
    }
}

