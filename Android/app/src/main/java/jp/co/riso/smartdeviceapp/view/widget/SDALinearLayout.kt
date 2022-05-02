/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SDALinearLayout.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * @class SDALinearLayout
 *
 * @brief Subclass of LinearLayout class. Adds support for fraction animation
 */
class SDALinearLayout : LinearLayout {
    /**
     * @brief Constructs a new LinearLayout with a Context object
     *
     * @param context A Context object used to access application assets
     */
    constructor(context: Context?) : super(context) {}

    /**
     * @brief Constructs a new LinearLayout with layout parameters.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    /**
     * @brief Constructs a new LinearLayout with layout parameters and a default style.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     * @param defStyle The default style resource ID
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }
    /**
     * @brief Gets the current x position of the view in percentage with respect to view width
     *
     * @return Return x position percentage
     */
    /**
     * @brief Sets the x position of the view
     *
     * @param xFraction x position fraction (percentage of width)
     */
    var xFraction: Float
        get() {
            val width = width
            return if (width == 0) 0F else x / width.toFloat()
        }
        set(xFraction) {
            val width = width
            setX(if (width > 0) xFraction * width else 0F)
        }
}