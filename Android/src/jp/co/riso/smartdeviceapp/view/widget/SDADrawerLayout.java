package jp.co.riso.smartdeviceapp.view.widget;

import jp.co.riso.smartdeviceapp.R;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

public class SDADrawerLayout extends DrawerLayout {

    public SDADrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SDADrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SDADrawerLayout(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getContext().getResources().getBoolean(R.bool.is_tablet) && isDrawerOpen(Gravity.RIGHT)) {
            return false;
        }
        
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
}

