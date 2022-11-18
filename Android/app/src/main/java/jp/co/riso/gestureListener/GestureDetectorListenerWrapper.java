package jp.co.riso.gestureListener;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/* Android 13 New OS Support - Create Java wrapper for GestureDetector.OnGestureListener
   to avoid crashing in API 33 + Kotlin
 */

public interface GestureDetectorListenerWrapper extends GestureDetector.OnGestureListener {

    @Override
    default boolean onDown(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    default void onShowPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    default boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    default boolean onScroll(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    default void onLongPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    default boolean onFling(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}

