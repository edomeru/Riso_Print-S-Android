package jp.co.riso.smartdeviceapp.controller;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Detects left and right swipes across a view.
 */
public class OnSwipeTouchListener implements OnTouchListener {
    
    private final GestureDetector gestureDetector;
    
    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }
    
    public void onSwipe() {
    }
    
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
    
    private final class GestureListener extends SimpleOnGestureListener {
        
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = Math.abs(e2.getX() - e1.getX());
            float y = Math.abs(e2.getY() - e1.getY());
            if (x > y && x > SWIPE_DISTANCE_THRESHOLD) {
                onSwipe();
                return true;
            }
            return false;
        }
    }
}