package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartdeviceapp.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PrintersContainerView extends LinearLayout {
    private static final int[] STATE_DELETE = { R.attr.state_delete };
    private static final int[] STATE_DEFAULT = { R.attr.state_default };
    
    private boolean mIsDelete = false;
    private boolean mIsDefault = false;
    
    public PrintersContainerView(Context context) {
        super(context);
    }
    
    public PrintersContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
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
    
    public void setDelete(boolean isDelete) {
        if (mIsDelete != isDelete) {
            mIsDelete = isDelete;
            refreshDrawableState();
        }
    }
    
    public boolean getDelete() {
        return mIsDelete;
    }
    
    public void setDefault(boolean isDefault) {
        if (mIsDefault != isDefault) {
            mIsDefault = isDefault;
            refreshDrawableState();
        }
    }
    
    public boolean getDefault() {
        return mIsDefault;
    }
}
