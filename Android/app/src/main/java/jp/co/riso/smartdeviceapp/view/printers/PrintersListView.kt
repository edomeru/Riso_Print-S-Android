/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersListView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
//import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter.setPrinterRow
//import jp.co.riso.smartdeviceapp.view.printers.PrintersContainerView.delete
//import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation.endDeleteMode
//import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation.beginDeleteModeOnView
//import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter.setPrinterRowToDelete
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation
import android.view.MotionEvent
import jp.co.riso.smartprint.R
import android.os.Parcelable
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.view.printers.PrintersListView
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter
import jp.co.riso.smartdeviceapp.view.printers.PrintersContainerView
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.ListView

/**
 * @class PrintersListView
 *
 * @brief ListView for Printers Screen having custom states.
 */
class PrintersListView : ListView, Handler.Callback {
    private var mDeleteMode = false
    private var mDeleteView: View? = null
    private var mDownPoint: Point? = null
    private var mDeleteAnimation: DisplayDeleteAnimation? = null
    private var mHandler: Handler? = null

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
        return if (mDeleteMode) {
            val deleteButton = mDeleteView!!.findViewById<View>(R.id.btn_delete)
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
                        mDeleteMode = true
                        return false
                    }
                }
            }
            // intercept and clear delete button if ACTION_DOWN on different item
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                mDeleteView!!.getLocationOnScreen(coords)
                val rect = Rect(
                    coords[0],
                    coords[1],
                    coords[0] + mDeleteView!!.width,
                    coords[1] + mDeleteView!!.height
                )
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    endDeleteMode(mDeleteView)
                    return true
                }
            }
            val swipe = processSwipe(ev)
            if (swipe) {
                return true
            }

            // intercept and clear delete button if ACTION_UP on same item
            if (ev.actionMasked == MotionEvent.ACTION_UP) {
                mDeleteView!!.getLocationOnScreen(coords)
                val rect = Rect(
                    coords[0],
                    coords[1],
                    coords[0] + mDeleteView!!.width,
                    coords[1] + mDeleteView!!.height
                )
                if (rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    endDeleteMode(mDeleteView)
                    return true
                }
            }
            if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
                endDeleteMode(mDeleteView)
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
            val newMessage = Message.obtain(mHandler, MSG_START_DELETE_MODE)
            newMessage.arg1 = index
            mHandler!!.sendMessage(newMessage)
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
            if (!mDeleteMode) {
                return PrinterManager.EMPTY_ID
            }
            return if (mDeleteView != null) {
                indexOfChild(mDeleteView)
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
        if (mDeleteView != null) {
            (adapter as PrinterArrayAdapter).setPrinterRow(mDeleteView!!)
            (mDeleteView as PrintersContainerView).delete = false
            mDeleteAnimation!!.endDeleteMode(
                mDeleteView as PrintersContainerView,
                animate,
                R.id.btn_delete,
                R.id.img_disclosure
            )
            mDeleteMode = false
            mDeleteView = null
        }
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Initialize PrintersListView.
     */
    private fun init() {
        mDeleteAnimation = DisplayDeleteAnimation()
        mHandler = Handler(Looper.myLooper()!!, this)
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
        val dragged = mDownPoint!!.x - ev.rawX > SWIPE_THRESHOLD
        var contains1: Boolean
        var contains2: Boolean
        // check self, if valid swipe don't redisplay nor remove delete button
        if (mDeleteMode) {
            mDeleteView!!.getLocationOnScreen(coords)
            val rect = Rect(
                coords[0],
                coords[1],
                coords[0] + mDeleteView!!.width,
                coords[1] + mDeleteView!!.height
            )
            contains1 = rect.contains(mDownPoint!!.x, mDownPoint!!.y)
            contains2 = rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
            return contains1 && contains2 && dragged
        }
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view != null) {
                view.getLocationOnScreen(coords)
                val rect =
                    Rect(coords[0], coords[1], coords[0] + view.width, coords[1] + view.height)
                contains1 = rect.contains(mDownPoint!!.x, mDownPoint!!.y)
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
        val action = ev.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> mDownPoint = Point(
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
        if (!mDeleteMode) {
            mDeleteView = view
            mDeleteAnimation!!.beginDeleteModeOnView(
                view,
                true,
                R.id.btn_delete,
                R.id.img_disclosure
            )
            (adapter as PrinterArrayAdapter).setPrinterRowToDelete(mDeleteView)
            mDeleteMode = true
        }
    }

    /**
     * @brief End delete mode.
     *
     * @param view Delete view
     */
    private fun endDeleteMode(view: View?) {
        if (mDeleteMode) {
            (adapter as PrinterArrayAdapter).setPrinterRow(view!!)
            val printerItem = view.findViewById<View>(R.id.btn_delete).tag as PrintersContainerView
            printerItem.delete = false
            mDeleteAnimation!!.endDeleteMode(view, true, R.id.btn_delete, R.id.img_disclosure)
            mDeleteMode = false
        }
    }

    /**
     * @brief End delete mode.
     */
    private fun endDeleteMode() {
        mDeleteMode = false
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