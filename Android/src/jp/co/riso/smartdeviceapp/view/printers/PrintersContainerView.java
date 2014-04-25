/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersContainerView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartdeviceapp.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * LinearLayout with custom states
 */
public class PrintersContainerView extends LinearLayout {
    private static final int[] STATE_DELETE = { R.attr.state_delete };
    private static final int[] STATE_DEFAULT = { R.attr.state_default };
    
    private boolean mIsDelete = false;
    private boolean mIsDefault = false;
    
    /**
     * Constructor
     * <p>
     * Instantiate LinearLayout with custom states
     * 
     * @param context
     */
    public PrintersContainerView(Context context) {
        super(context);
    }
    
    /**
     * Constructor
     * <p>
     * Instantiate LinearLayout with custom states
     * 
     * @param context
     * @param attrs
     */
    public PrintersContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * Constructor
     * <p>
     * Instantiate LinearLayout with custom states
     * 
     * @param context
     * @param attrs
     * @param defStyle
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
     * Set delete state
     * 
     * @param isDelete
     *            delete state
     */
    public void setDelete(boolean isDelete) {
        if (mIsDelete != isDelete) {
            mIsDelete = isDelete;
            refreshDrawableState();
        }
    }
    
    /**
     * @return delete state
     */    
    public boolean getDelete() {
        return mIsDelete;
    }
    
    /**
     * Set default state
     * 
     * @param isDefault
     *            default state
     */
    public void setDefault(boolean isDefault) {
        if (mIsDefault != isDefault) {
            mIsDefault = isDefault;
            refreshDrawableState();
        }
    }
    
    /**
     * @return default state
     */
    public boolean getDefault() {
        return mIsDefault;
    }
}
