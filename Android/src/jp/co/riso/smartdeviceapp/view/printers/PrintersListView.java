package jp.co.riso.smartdeviceapp.view.printers;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class PrintersListView extends ListView {
    private boolean mDeleteMode = false;
    private View mDeleteView = null;
    private Point mDownPoint = null;
    private DisplayDeleteAnimation mDeleteAnimation = null;
    
    public PrintersListView(Context context) {
        super(context);
        
        init();
    }
    
    public PrintersListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init();
    }
    
    public PrintersListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        
        if (mDeleteMode) {
            if (mDeleteView != null) {
                View deleteButton = mDeleteView.findViewById(R.id.btn_delete);
                if (deleteButton != null) {
                    
                    int coords[] = new int[2];
                    deleteButton.getLocationOnScreen(coords);
                    
                    Rect rect = new Rect(coords[0], coords[1] - deleteButton.getHeight(), coords[0] + deleteButton.getWidth(), coords[1]);
                    
                    // Intercept only if touched item is not the delete button
                    if (rect.contains((int) ev.getX(), (int) ev.getY())) {
                        endDeleteMode(mDeleteView);
                        return super.onInterceptTouchEvent(ev);
                    }
                }
            }
            endDeleteMode(mDeleteView);
            return true;
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
    // Private Methods
    // ================================================================================
    
    private void init() {
        mDeleteAnimation = new DisplayDeleteAnimation();
    }
    
    private boolean checkSwipe(MotionEvent ev) {
        int coords[] = new int[2];
        
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            
            if (view != null) {
                view.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1] - view.getHeight(), coords[0] + view.getWidth(), coords[1]);
                
                boolean contains1 = rect.contains(mDownPoint.x, mDownPoint.y);
                boolean contains2 = rect.contains((int) ev.getX(), (int) ev.getY());
                boolean dragged = Math.abs(mDownPoint.x - ev.getX()) > 48;
                boolean swipeLeft = mDownPoint.x - ev.getX() > 0;
                if (contains1 && contains2 && dragged && swipeLeft) {
                    startDeleteMode(view);
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean processSwipe(MotionEvent ev) {
        boolean ret = false;
        int action = ev.getAction();
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownPoint = new Point((int) ev.getX(), (int) ev.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                ret = checkSwipe(ev);
                break;
        }
        return ret;
    }
    
    private void startDeleteMode(View view) {
        mDeleteView = view;
        mDeleteAnimation.beginDeleteModeOnView(view, true, R.id.btn_delete);
        ((PrinterArrayAdapter) getAdapter()).setPrinterRowToDelete(mDeleteView);
        mDeleteMode = true;
    }
    
    private void endDeleteMode(View view) {
        ((PrinterArrayAdapter) getAdapter()).setPrinterRow(view);
        mDeleteAnimation.endDeleteMode(view, true, R.id.btn_delete);
        mDeleteMode = false;
    }
}
