package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class PortTextView extends TextView {
    public PortTextView(Context context) {
        super(context);
        this.setTypeface(SmartDeviceApp.getAppFont());
    }
    
    public PortTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(SmartDeviceApp.getAppFont());
    }
    
    public PortTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTypeface(SmartDeviceApp.getAppFont());
    }
    
}