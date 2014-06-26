/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SDALinearLayout.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * @class SDALinearLayout
 * @brief Subclass of LinearLayout class. Adds support for fraction animation
 */
public class SDALinearLayout extends LinearLayout {
    
    /**
     * @brief Constructs a new LinearLayout with a Context object
     * 
     * @param context a Context object used to access application assets 
     */
    public SDALinearLayout(Context context) {
        super(context);
    }
    
    /**
     * @brief Constructs a new LinearLayout with layout parameters.
     * 
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent 
     */
    public SDALinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * @brief Constructs a new LinearLayout with layout parameters and a default style.
     * 
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent 
     * @param defStyle the default style resource ID  
     */
    public SDALinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @brief Gets the current x position of the view in percentage with respect to view width
     * 
     * @return Return x position percentage
     */
    public float getXFraction() {
        int width = getWidth();
        return (width == 0) ? 0 : getX() / (float) width;
    }

    /**
     * @brief Sets the x position of the view
     * 
     * @param xFraction x position fraction (percentage of width)
     */
    public void setXFraction(float xFraction) {
        int width = getWidth();
        setX((width > 0) ? (xFraction * width) : 0);
    }
}