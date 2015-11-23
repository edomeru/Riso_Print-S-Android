/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersContainerView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartprint.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * @class PrintersContainerView
 * 
 * @brief LinearLayout with custom states
 */
public class PrintersContainerView extends LinearLayout {
    private static final int[] STATE_DELETE = { R.attr.state_delete };
    private static final int[] STATE_DEFAULT = { R.attr.state_default };
    
    private boolean mIsDelete = false;
    private boolean mIsDefault = false;
    
    /**
     * @brief Constructor. <br>
     *
     * Instantiate LinearLayout with custom states
     * 
     * @param context Application context
     */
    public PrintersContainerView(Context context) {
        super(context);
    }
    
    /**
     * @brief Constructor. <br>
     *
     * Instantiate LinearLayout with custom states
     * 
     * @param context Application context
     * @param attrs Layout attributes
     */
    public PrintersContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * @brief Constructor. <br>
     * 
     * Instantiate LinearLayout with custom states
     * 
     * @param context Application context
     * @param attrs Layout attributes
     * @param defStyle Layout style
     */
    public PrintersContainerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
        
        if (mIsDelete) {
            mergeDrawableStates(drawableState, STATE_DELETE);
            
        }
        if (mIsDefault) {
            mergeDrawableStates(drawableState, STATE_DEFAULT);
            
        }
        return drawableState;
    }
    
    /**
     * @brief Set delete state. <br>
     * 
     * Sets and refreshes the drawable state of the view
     * 
     * @param isDelete Delete state
     */
    public void setDelete(boolean isDelete) {
        if (mIsDelete != isDelete) {
            mIsDelete = isDelete;
            refreshDrawableState();
        }
    }
    
    /**
     * @brief Get the delete state of the view.
     * 
     * @retval true View state is delete state
     * @retval false View state is not delete state
     */    
    public boolean getDelete() {
        return mIsDelete;
    }
    
    /**
     * @brief Set default state. <br>
     * 
     * Sets and refreshes the drawable state of the view.
     * The view is used for default printer view.
     * 
     * @param isDefault Default state
     */
    public void setDefault(boolean isDefault) {
        if (mIsDefault != isDefault) {
            mIsDefault = isDefault;
            refreshDrawableState();
        }
    }
    
    /**
     * @brief Get the default state of the view.
     * 
     * @retval true The view is set to default state.
     * @retval false The view is not set to default state.
     */
    public boolean getDefault() {
        return mIsDefault;
    }
}
