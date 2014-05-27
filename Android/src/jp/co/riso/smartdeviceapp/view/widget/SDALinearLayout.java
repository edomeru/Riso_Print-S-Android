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

public class SDALinearLayout extends LinearLayout {

    /**
     * Constructor
     * <p>
     * Instantiate custom LinearLayout
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SDALinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Constructor
     * <p>
     * Instantiate custom LinearLayout
     * 
     * @param context
     * @param attrs
     */
    public SDALinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     * <p>
     * Instantiate custom LinearLayout
     * 
     * @param context
     */
    public SDALinearLayout(Context context) {
        super(context);
    }

    /**
     * Gets a fraction of the visual x position of this view in pixels
     * 
     * @return Return x-position
     */
    public float getXFraction() {
        int width = getWidth();
        return (width == 0) ? 0 : getX() / (float) width;
    }

    /**
     * Sets the visual x position of this view in pixels
     * 
     * @param xFraction
     */
    public void setXFraction(float xFraction) {
        int width = getWidth();
        setX((width > 0) ? (xFraction * width) : 0);
    }
}