/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersContainerView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import jp.co.riso.smartdeviceapp.view.printers.PrintersContainerView
import jp.co.riso.smartprint.R

/**
 * @class PrintersContainerView
 *
 * @brief LinearLayout with custom states
 */
class PrintersContainerView : LinearLayout {
    private var mIsDelete = false
    private var mIsDefault = false

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate LinearLayout with custom states
     *
     * @param context Application context
     */
    constructor(context: Context?) : super(context) {}

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate LinearLayout with custom states
     *
     * @param context Application context
     * @param attrs Layout attributes
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate LinearLayout with custom states
     *
     * @param context Application context
     * @param attrs Layout attributes
     * @param defStyle Layout style
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (mIsDelete) {
            mergeDrawableStates(drawableState, STATE_DELETE)
        }
        if (mIsDefault) {
            mergeDrawableStates(drawableState, STATE_DEFAULT)
        }
        return drawableState
    }
    /**
     * @brief Get the delete state of the view.
     *
     * @retval true View state is delete state
     * @retval false View state is not delete state
     */
    /**
     * @brief Set delete state. <br></br>
     *
     * Sets and refreshes the drawable state of the view
     *
     * @param isDelete Delete state
     */
    var delete: Boolean
        get() = mIsDelete
        set(isDelete) {
            if (mIsDelete != isDelete) {
                mIsDelete = isDelete
                refreshDrawableState()
            }
        }
    /**
     * @brief Get the default state of the view.
     *
     * @retval true The view is set to default state.
     * @retval false The view is not set to default state.
     */
    /**
     * @brief Set default state. <br></br>
     *
     * Sets and refreshes the drawable state of the view.
     * The view is used for default printer view.
     *
     * @param isDefault Default state
     */
    var default: Boolean
        get() = mIsDefault
        set(isDefault) {
            if (mIsDefault != isDefault) {
                mIsDefault = isDefault
                refreshDrawableState()
            }
        }

    companion object {
        private val STATE_DELETE = intArrayOf(R.attr.state_delete)
        private val STATE_DEFAULT = intArrayOf(R.attr.state_default)
    }
}