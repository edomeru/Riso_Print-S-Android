/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DisplayDeleteAnimation.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.anim;

import java.lang.ref.WeakReference;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Class for displaying a view (e.g. delete button) using TranslateAnimation and
 * hiding other views using AlphaAnimation.
 */
public class DisplayDeleteAnimation {
    private static final int ANIM_DURATION = 250;
    
    /**
     * Returns a View object that represents the item layout for deletion.
     * <p>
     * This method displays a view (e.g. delete button) using TranslateAnimation and hides
     * other views using AlphaAnimation if animation is enabled.
     * 
     * @param view
     *            the row layout
     * @param animate
     *            determines if animation will be used
     * @param deleteId
     *            the id of the view to be displayed (e.g. delete button)
     * @param ids
     *            the ids of the views to be hidden (optional)
     */
    public void beginDeleteModeOnView(View view, boolean animate, int deleteId, int... ids) {
        
        View deleteButton = view.findViewById(deleteId);
        
        if (deleteButton != null) {
            deleteButton.setVisibility(View.VISIBLE);
            if (animate) {
                TranslateAnimation translate = new TranslateAnimation(deleteButton.getWidth(), 0, 0, 0);
                translate.setDuration(ANIM_DURATION);
                
                if (deleteButton.getAnimation() != null) {
                    deleteButton.getAnimation().setAnimationListener(null);
                    deleteButton.getAnimation().cancel();
                }
                deleteButton.clearAnimation();
                deleteButton.startAnimation(translate);
            }
        }
        
        for (int i = 0; i < ids.length; i++) {
            View viewToHide = view.findViewById(ids[i]);
            if (viewToHide != null) {
                if (animate) {
                    AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
                    alpha.setDuration(ANIM_DURATION);
                    
                    HideOnFadeAnimationListener listener = new HideOnFadeAnimationListener();
                    listener.setView(viewToHide);
                    
                    alpha.setAnimationListener(listener);
                    
                    if (viewToHide.getAnimation() != null) {
                        viewToHide.getAnimation().setAnimationListener(null);
                        viewToHide.getAnimation().cancel();
                    }
                    viewToHide.clearAnimation();
                    viewToHide.startAnimation(alpha);
                } else {
                    viewToHide.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
    
    /**
     * This method hides a view (e.g. delete button) using TranslateAnimation and displays
     * other views using AlphaAnimation if animation is enabled.
     * 
     * @param view
     *            the row layout
     * @param animate
     *            determines if animation will be used
     * @param deleteId
     *            the id of the view to be hidden (e.g. delete button)
     * @param ids
     *            the ids of the views to be displayed (optional)
     */
    public void endDeleteMode(View view, boolean animate, int deleteId, int... ids) {
        
        View deleteButton = view.findViewById(deleteId);
        
        if (deleteButton != null) {
            if (animate) {
                TranslateAnimation translate = new TranslateAnimation(0, deleteButton.getWidth(), 0, 0);
                translate.setDuration(ANIM_DURATION);
                
                HideOnFadeAnimationListener listener = new HideOnFadeAnimationListener();
                listener.setView(deleteButton);
                
                translate.setAnimationListener(listener);
                
                if (deleteButton.getAnimation() != null) {
                    deleteButton.getAnimation().setAnimationListener(null);
                    deleteButton.getAnimation().cancel();
                }
                deleteButton.clearAnimation();
                deleteButton.startAnimation(translate);
                
            } else {
                deleteButton.setVisibility(View.INVISIBLE);
            }
        }
        
        for (int i = 0; i < ids.length; i++) {
            View viewToHide = view.findViewById(ids[i]);
            if (viewToHide != null) {
                viewToHide.setVisibility(View.VISIBLE);
                if (animate) {
                    AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
                    alpha.setDuration(ANIM_DURATION);
                    
                    if (viewToHide.getAnimation() != null) {
                        viewToHide.getAnimation().setAnimationListener(null);
                        viewToHide.getAnimation().cancel();
                    }
                    viewToHide.clearAnimation();
                    viewToHide.startAnimation(alpha);
                }
            }
        }
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    public class HideOnFadeAnimationListener implements Animation.AnimationListener {
        private WeakReference<View> mViewReference = null;
        
        /**
         * Set delete view
         * 
         * @param view
         */
        public void setView(View view) {
            mViewReference = new WeakReference<View>(view);
        }
        
        /** {@inheritDoc} */
        @Override
        public void onAnimationEnd(Animation animation) {
            if (mViewReference != null) {
                final View view = mViewReference.get();
                if (view != null) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
        }
        
        /** {@inheritDoc} */
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
        
        /** {@inheritDoc} */
        @Override
        public void onAnimationStart(Animation animation) {
        }
    }
}
