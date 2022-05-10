/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintersContainerView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import jp.co.riso.smartprint.R

/**
 * @class PrintersContainerView
 *
 * @brief LinearLayout with custom states
 */
class PrintersContainerView : LinearLayout {
    private var _isDelete = false
    private var _isDefault = false

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate LinearLayout with custom states
     *
     * @param context Application context
     */
    constructor(context: Context?) : super(context)

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate LinearLayout with custom states
     *
     * @param context Application context
     * @param attrs Layout attributes
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

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
    )

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (_isDelete) {
            mergeDrawableStates(drawableState, STATE_DELETE)
        }
        if (_isDefault) {
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
     * @param _isDelete Delete state
     */
    var delete: Boolean
        get() = _isDelete
        set(isDelete) {
            if (_isDelete != isDelete) {
                _isDelete = isDelete
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
     * @param _isDefault Default state
     */
    var default: Boolean
        get() = _isDefault
        set(isDefault) {
            if (_isDefault != isDefault) {
                _isDefault = isDefault
                refreshDrawableState()
            }
        }

    companion object {
        private val STATE_DELETE = intArrayOf(R.attr.state_delete)
        private val STATE_DEFAULT = intArrayOf(R.attr.state_default)
    }
}