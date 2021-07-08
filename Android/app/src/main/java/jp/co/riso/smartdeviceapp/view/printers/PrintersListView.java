/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersListView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * @class PrintersListView
 * 
 * @brief ListView for Printers Screen having custom states.
 */
public class PrintersListView extends ListView implements Callback {
    private static final int SWIPE_THRESHOLD = 50;
    private static final int MSG_START_DELETE_MODE = 0x1;
    private boolean mDeleteMode = false;
    private View mDeleteView = null;
    private Point mDownPoint = null;
    private DisplayDeleteAnimation mDeleteAnimation = null;
    private Handler mHandler = null;
    
    /**
     * @brief Constructor. <br>
     *
     * Instantiate Printers Screen ListView
     * 
     * @param context Application context
     */
    public PrintersListView(Context context) {
        super(context);
        
        init();
    }
    
    /**
     * @brief Constructor. <br>
     *
     * Instantiate Printers Screen ListView
     * 
     * @param context Application context
     * @param attrs layout attributes
     */
    public PrintersListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init();
    }
    
    /**
     * @brief Constructor. <br>
     *
     * Instantiate Printers Screen ListView
     * 
     * @param context Application context
     * @param attrs Layout attributes
     * @param defStyle Layout style
     */
    public PrintersListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int coords[] = new int[2];
        
        if (mDeleteMode) {
            View deleteButton = mDeleteView.findViewById(R.id.btn_delete);
            
            if (deleteButton != null) {
                deleteButton.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1], coords[0] + deleteButton.getWidth(), coords[1] + deleteButton.getHeight());
                // Delete button is pressed
                if (rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
                        endDeleteMode();
                        // Process Dialog box
                        super.onInterceptTouchEvent(ev);
                        // Reset delete mode to true
                        mDeleteMode = true;
                        return false;
                    }
                }
            }
            // intercept and clear delete button if ACTION_DOWN on different item
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mDeleteView.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1], coords[0] + mDeleteView.getWidth(), coords[1] + mDeleteView.getHeight());
                if (!rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    endDeleteMode(mDeleteView);
                    return true;
                }
            }

            boolean swipe = processSwipe(ev);
            
            if (swipe) {
                return true;
            }
            
            // intercept and clear delete button if ACTION_UP on same item
            if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
                mDeleteView.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1], coords[0] + mDeleteView.getWidth(), coords[1] + mDeleteView.getHeight());
                if (rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    endDeleteMode(mDeleteView);
                    return true;
                }
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                endDeleteMode(mDeleteView);
            }
            return super.onInterceptTouchEvent(ev);
        } else {
            boolean swipe = processSwipe(ev);
            
            if (swipe) {
                return true;
            } else {
                return super.onInterceptTouchEvent(ev);
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
    public void onRestoreInstanceState(Parcelable state, int index) {
        super.onRestoreInstanceState(state);
        if (index != PrinterManager.EMPTY_ID) {
            Message newMessage = Message.obtain(mHandler, MSG_START_DELETE_MODE);
            newMessage.arg1 = index;
            mHandler.sendMessage(newMessage);
        }
    }
    
    /**
     * @brief Get the delete view index.
     * 
     * @return Delete view index
     * @retval EMPTY_ID No delete item 
     */
    public int getDeleteItemPosition() {
        if (!mDeleteMode) {
            return PrinterManager.EMPTY_ID;
        }
        if (mDeleteView != null) {
            return indexOfChild(mDeleteView);
        } else {
            return PrinterManager.EMPTY_ID;
        }
    }
    
    /**
     * @brief Reset delete view.
     * 
     * @param animate Animate delete button
     */
    public void resetDeleteView(boolean animate) {
        if (mDeleteView != null) {
            ((PrinterArrayAdapter) getAdapter()).setPrinterRow(mDeleteView);
            ((PrintersContainerView) mDeleteView).setDelete(false);
            mDeleteAnimation.endDeleteMode(mDeleteView, animate, R.id.btn_delete, R.id.img_disclosure);
            mDeleteMode = false;
            mDeleteView = null;
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * @brief Initialize PrintersListView.
     */
    private void init() {
        mDeleteAnimation = new DisplayDeleteAnimation();
        mHandler = new Handler(Looper.myLooper(), this);
    }
    
    /**
     * @brief Checks if swipe was performed.
     * 
     * @param ev Event object
     * 
     * @retval true Action is swipe to left
     * @retval false Action is not swipe to left
     */
    private boolean checkSwipe(MotionEvent ev) {
        
        int coords[] = new int[2];
        boolean dragged = (mDownPoint.x - ev.getRawX()) > SWIPE_THRESHOLD;
        boolean contains1;
        boolean contains2;
        // check self, if valid swipe don't redisplay nor remove delete button
        if (mDeleteMode) {
            mDeleteView.getLocationOnScreen(coords);
            
            Rect rect = new Rect(coords[0], coords[1], coords[0] + mDeleteView.getWidth(), coords[1] + mDeleteView.getHeight());
            contains1 = rect.contains(mDownPoint.x, mDownPoint.y);
            contains2 = rect.contains((int) ev.getRawX(), (int) ev.getRawY());
            
            return (contains1 && contains2 && dragged);
        }
        
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            
            if (view != null) {
                view.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1], coords[0] + view.getWidth(), coords[1] + view.getHeight());
                contains1 = rect.contains(mDownPoint.x, mDownPoint.y);
                contains2 = rect.contains((int) ev.getRawX(), (int) ev.getRawY());
                
                if (contains1 && contains2 && dragged) {
                    startDeleteMode(view);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * @brief Process swipe event.
     * 
     * @param ev Event object
     * 
     * @retval true Action is swipe to left
     * @retval false Action is not swipe to left
     */
    private boolean processSwipe(MotionEvent ev) {
        boolean ret = false;
        int action = ev.getActionMasked();
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPoint = new Point((int) ev.getRawX(), (int) ev.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                ret = checkSwipe(ev);
                break;
        }
        
        return ret;
    }
    
    /**
     * @brief Start delete mode.
     * 
     * @param view View to set as delete view
     */
    private void startDeleteMode(View view) {
        if (!mDeleteMode) {
            mDeleteView = view;
            mDeleteAnimation.beginDeleteModeOnView(view, true, R.id.btn_delete, R.id.img_disclosure);
            ((PrinterArrayAdapter) getAdapter()).setPrinterRowToDelete(mDeleteView);
            mDeleteMode = true;
        }
    }
    
    /**
     * @brief End delete mode.
     * 
     * @param view Delete view
     */
    private void endDeleteMode(View view) {
        if (mDeleteMode) {
            ((PrinterArrayAdapter) getAdapter()).setPrinterRow(view);
            PrintersContainerView printerItem = (PrintersContainerView) view.findViewById(R.id.btn_delete).getTag();
            printerItem.setDelete(false);
            mDeleteAnimation.endDeleteMode(view, true, R.id.btn_delete, R.id.img_disclosure);
            mDeleteMode = false;
        }
    }
    
    /**
     * @brief End delete mode.
     */
    private void endDeleteMode() {
        mDeleteMode = false;
    }

    // ================================================================================
    // Interface - Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START_DELETE_MODE:
                View view = getChildAt(msg.arg1);
                if (view != null) {
                    startDeleteMode(view);
                }
                return true;
        }
        return false;
    }    
}
