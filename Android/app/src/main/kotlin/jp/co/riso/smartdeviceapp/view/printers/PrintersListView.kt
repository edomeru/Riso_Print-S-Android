/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintersListView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ListView
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation
import jp.co.riso.smartprint.R

/**
 * @class PrintersListView
 *
 * @brief ListView for Printers Screen having custom states.
 */
class PrintersListView : ListView, Handler.Callback {
    private var _deleteMode = false
    private var _deleteView: View? = null
    private var _downPoint: Point? = null
    private var _deleteAnimation: DisplayDeleteAnimation? = null
    private var _handler: Handler? = null

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate Printers Screen ListView
     *
     * @param context Application context
     */
    constructor(context: Context?) : super(context) {
        init()
    }

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate Printers Screen ListView
     *
     * @param context Application context
     * @param attrs layout attributes
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate Printers Screen ListView
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
        init()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val coords = IntArray(2)
        return if (_deleteMode) {
            val deleteButton = _deleteView!!.findViewById<View>(R.id.btn_delete)
            if (deleteButton != null) {
                deleteButton.getLocationOnScreen(coords)
                val rect = Rect(
                    coords[0],
                    coords[1],
                    coords[0] + deleteButton.width,
                    coords[1] + deleteButton.height
                )
                // Delete button is pressed
                if (rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    if (ev.actionMasked == MotionEvent.ACTION_UP) {
                        endDeleteMode()
                        // Process Dialog box
                        super.onInterceptTouchEvent(ev)
                        // Reset delete mode to true
                        _deleteMode = true
                        return false
                    }
                }
            }
            // intercept and clear delete button if ACTION_DOWN on different item
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                _deleteView!!.getLocationOnScreen(coords)
                val rect = Rect(
                    coords[0],
                    coords[1],
                    coords[0] + _deleteView!!.width,
                    coords[1] + _deleteView!!.height
                )
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    endDeleteMode(_deleteView)
                    return true
                }
            }
            val swipe = processSwipe(ev)
            if (swipe) {
                return true
            }

            // intercept and clear delete button if ACTION_UP on same item
            if (ev.actionMasked == MotionEvent.ACTION_UP) {
                _deleteView!!.getLocationOnScreen(coords)
                val rect = Rect(
                    coords[0],
                    coords[1],
                    coords[0] + _deleteView!!.width,
                    coords[1] + _deleteView!!.height
                )
                if (rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    endDeleteMode(_deleteView)
                    return true
                }
            }
            if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
                endDeleteMode(_deleteView)
            }
            super.onInterceptTouchEvent(ev)
        } else {
            val swipe = processSwipe(ev)
            if (swipe) {
                true
            } else {
                super.onInterceptTouchEvent(ev)
            }
        }
    }
    // ================================================================================
    // Public Methods
    // ================================================================================
    /**
     * @brief Restore the ListView's previous state.
     *
     * @param state ListView state
     * @param index Delete view index
     */
    fun onRestoreInstanceState(state: Parcelable?, index: Int) {
        super.onRestoreInstanceState(state)
        if (index != PrinterManager.EMPTY_ID) {
            val newMessage = Message.obtain(_handler, MSG_START_DELETE_MODE)
            newMessage.arg1 = index
            _handler!!.sendMessage(newMessage)
        }
    }

    /**
     * @brief Get the delete view index.
     *
     * @return Delete view index
     * @retval EMPTY_ID No delete item
     */
    val deleteItemPosition: Int
        get() {
            if (!_deleteMode) {
                return PrinterManager.EMPTY_ID
            }
            return if (_deleteView != null) {
                indexOfChild(_deleteView)
            } else {
                PrinterManager.EMPTY_ID
            }
        }

    /**
     * @brief Reset delete view.
     *
     * @param animate Animate delete button
     */
    fun resetDeleteView(animate: Boolean) {
        if (_deleteView != null) {
            (adapter as PrinterArrayAdapter?)!!.setPrinterRow(_deleteView!!)
            (_deleteView as PrintersContainerView?)!!.delete = false
            _deleteAnimation!!.endDeleteMode(
                (_deleteView as PrintersContainerView?)!!,
                animate,
                R.id.btn_delete,
                R.id.img_disclosure
            )
            _deleteMode = false
            _deleteView = null
        }
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Initialize PrintersListView.
     */
    private fun init() {
        _deleteAnimation = DisplayDeleteAnimation()
        _handler = Handler(Looper.myLooper()!!, this)
    }

    /**
     * @brief Checks if swipe was performed.
     *
     * @param ev Event object
     *
     * @retval true Action is swipe to left
     * @retval false Action is not swipe to left
     */
    private fun checkSwipe(ev: MotionEvent): Boolean {
        val coords = IntArray(2)
        val dragged = _downPoint!!.x - ev.rawX > SWIPE_THRESHOLD
        var contains1: Boolean
        var contains2: Boolean
        // check self, if valid swipe don't redisplay nor remove delete button
        if (_deleteMode) {
            _deleteView!!.getLocationOnScreen(coords)
            val rect = Rect(
                coords[0],
                coords[1],
                coords[0] + _deleteView!!.width,
                coords[1] + _deleteView!!.height
            )
            contains1 = rect.contains(_downPoint!!.x, _downPoint!!.y)
            contains2 = rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
            return contains1 && contains2 && dragged
        }
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view != null) {
                view.getLocationOnScreen(coords)
                val rect =
                    Rect(coords[0], coords[1], coords[0] + view.width, coords[1] + view.height)
                contains1 = rect.contains(_downPoint!!.x, _downPoint!!.y)
                contains2 = rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
                if (contains1 && contains2 && dragged) {
                    startDeleteMode(view)
                    return true
                }
            }
        }
        return false
    }

    /**
     * @brief Process swipe event.
     *
     * @param ev Event object
     *
     * @retval true Action is swipe to left
     * @retval false Action is not swipe to left
     */
    private fun processSwipe(ev: MotionEvent): Boolean {
        var ret = false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> _downPoint = Point(
                ev.rawX.toInt(), ev.rawY.toInt()
            )
            MotionEvent.ACTION_MOVE -> ret = checkSwipe(ev)
        }
        return ret
    }

    /**
     * @brief Start delete mode.
     *
     * @param view View to set as delete view
     */
    private fun startDeleteMode(view: View) {
        if (!_deleteMode) {
            _deleteView = view
            _deleteAnimation!!.beginDeleteModeOnView(
                view,
                true,
                R.id.btn_delete,
                R.id.img_disclosure
            )
            (adapter as PrinterArrayAdapter?)!!.setPrinterRowToDelete(_deleteView)
            _deleteMode = true
        }
    }

    /**
     * @brief End delete mode.
     *
     * @param view Delete view
     */
    private fun endDeleteMode(view: View?) {
        if (_deleteMode) {
            (adapter as PrinterArrayAdapter?)!!.setPrinterRow(view!!)
            val printerItem = view.findViewById<View>(R.id.btn_delete).tag as PrintersContainerView?
            printerItem!!.delete = false
            _deleteAnimation!!.endDeleteMode(view, true, R.id.btn_delete, R.id.img_disclosure)
            _deleteMode = false
        }
    }

    /**
     * @brief End delete mode.
     */
    private fun endDeleteMode() {
        _deleteMode = false
    }

    // ================================================================================
    // Interface - Callback
    // ================================================================================
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_START_DELETE_MODE -> {
                val view = getChildAt(msg.arg1)
                view?.let { startDeleteMode(it) }
                return true
            }
        }
        return false
    }

    companion object {
        private const val SWIPE_THRESHOLD = 50
        private const val MSG_START_DELETE_MODE = 0x1
    }
}