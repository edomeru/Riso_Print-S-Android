/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * SDADrawerLayout.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.drawerlayout.widget.DrawerLayout
import android.view.MotionEvent
import android.view.Gravity
import android.view.View
import jp.co.riso.smartprint.R
import jp.co.riso.android.util.AppUtils

/**
 * @class SDADrawerLayout
 * 
 * @brief Subclass of DrawerLayout class.
 * 
 * Adds following functionalities:
 *
 *  1.  Prevent touch intercept of the main view
 *  2.  Prevent dragging from drawer views
 *
 */
class SDADrawerLayout : DrawerLayout {
    private var _preventInterceptTouches = false

    /**
     * @brief Constructs a new DrawerLayout with a Context object
     *
     * @param context A Context object used to access application assets
     */
    constructor(context: Context?) : super(context!!) {}

    /**
     * @brief Constructs a new DrawerLayout with layout parameters.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    /**
     * @brief Constructs a new DrawerLayout with layout parameters and a default style.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     * @param defStyle The default style resource ID
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (_preventInterceptTouches) {
            return false
        }
        if (isDrawerOpen(Gravity.LEFT)) {
            val leftView = findViewById<View>(R.id.leftLayout)
            if (leftView != null) {
                if (AppUtils.checkViewHitTest(leftView, ev.rawX.toInt(), ev.rawY.toInt())) {
                    return false
                }
            }
        }
        if (isDrawerOpen(Gravity.RIGHT)) {
            val rightView = findViewById<View>(R.id.rightLayout)
            if (rightView != null) {
                if (AppUtils.checkViewHitTest(rightView, ev.rawX.toInt(), ev.rawY.toInt())) {
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}