/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PortTextView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class PortTextView extends TextView {
    
    /**
     * Constructor
     * <p>
     * Instantiate TextView object with a custom font
     * 
     * @param context
     */
   public PortTextView(Context context) {
        super(context);
        this.setTypeface(SmartDeviceApp.getAppFont());
    }

    /**
     * Constructor
     * <p>
     * Instantiate TextView object with a custom font
     * 
     * @param context
     * @param attrs
     */
    public PortTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(SmartDeviceApp.getAppFont());
    }
    
    /**
     * Constructor
     * <p>
     * Instantiate TextView object with a custom font
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public PortTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTypeface(SmartDeviceApp.getAppFont());
    }
}