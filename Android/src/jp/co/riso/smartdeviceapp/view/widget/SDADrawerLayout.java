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

public class SDADrawerLayout extends DrawerLayout {
    
    private boolean mPreventInterceptTouches = false;

    /**
     * Constructor
     * <p>
     * Instantiate custom DrawerLayout
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SDADrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Constructor
     * <p>
     * Instantiate custom DrawerLayout
     * 
     * @param context
     * @param attrs
     */
    public SDADrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     * <p>
     * Instantiate custom DrawerLayout
     * 
     * @param context
     */
    public SDADrawerLayout(Context context) {
        super(context);
    }

    /** {@inheritDoc} */
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
     * Set prevent intercept touches
     * 
     * @param preventInterceptTouches
     *            Prevent touches
     */
    public void setPreventInterceptTouches(boolean preventInterceptTouches) {
        mPreventInterceptTouches = preventInterceptTouches;
    }
}

