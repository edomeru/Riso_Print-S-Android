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
 * @class DisplayDeleteAnimation
 * 
 * @brief Class for displaying a view (e.g. delete button) using TranslateAnimation and
 * hiding other views (optional).
 */
public class DisplayDeleteAnimation {
    private static final int ANIM_DURATION = 250;
    
    /**
     * @brief This method displays a view (e.g. delete button) using TranslateAnimation 
     * if animation is enabled and hides other views.
     * 
     * @param view The row layout
     * @param animate Determines if animation will be used
     * @param deleteId The id of the view to be displayed (e.g. delete button)
     * @param ids The ids of the views to be hidden (optional)
     */
    public void beginDeleteModeOnView(View view, boolean animate, int deleteId, int... ids) {
        
        View deleteButton = view.findViewById(deleteId);
        
        for (int i = 0; i < ids.length; i++) {
            View viewToHide = view.findViewById(ids[i]);
            if (viewToHide != null) {
                viewToHide.setVisibility(View.GONE);
            }
        }
        
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
    }
    
    /**
     * @brief This method hides a view (e.g. delete button) using TranslateAnimation and displays
     * other views using AlphaAnimation if animation is enabled.
     * 
     * @param view The row layout
     * @param animate Determines if animation will be used
     * @param deleteId The id of the view to be hidden (e.g. delete button)
     * @param ids The ids of the views to be displayed (optional)
     */
    public void endDeleteMode(View view, boolean animate, int deleteId, int... ids) {
        
        View deleteButton = view.findViewById(deleteId);
        
        if (deleteButton != null) {
            if (animate) {
                TranslateAnimation translate = new TranslateAnimation(0, deleteButton.getWidth(), 0, 0);
                translate.setDuration(ANIM_DURATION);
                
                HideOnFadeAnimationListener listener = new HideOnFadeAnimationListener();
                listener.setRowView(view);
                listener.setView(deleteButton);
                listener.setIds(ids);
                
                translate.setAnimationListener(listener);
                
                if (deleteButton.getAnimation() != null) {
                    deleteButton.getAnimation().setAnimationListener(null);
                    deleteButton.getAnimation().cancel();
                }
                deleteButton.clearAnimation();
                deleteButton.startAnimation(translate);
                
            } else {
                deleteButton.setVisibility(View.GONE);
                
                for (int i = 0; i < ids.length; i++) {
                    View viewToHide = view.findViewById(ids[i]);
                    if (viewToHide != null) {
                        viewToHide.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @class HideOnFadeAnimationListener
     * 
     * @brief Listener class that hides the view and displays other views after animation end
     */
    public static class HideOnFadeAnimationListener implements Animation.AnimationListener {
        private WeakReference<View> mRowViewReference = null;
        private WeakReference<View> mViewReference = null;
        private int[] mIdsReference = null;
        
        /**
         * @brief Set delete view
         * 
         * @param view Delete button
         */
        public void setView(View view) {
            mViewReference = new WeakReference<>(view);
        }
        
        /**
         * @brief Set Row view
         * 
         * @param view Row view or parent view of the delete button
         */
        public void setRowView(View view) {
            mRowViewReference = new WeakReference<>(view);
        }

        /**
         * @brief Set other resource ID that needs to be redisplayed
         * 
         * @param ids Array of resource IDs that needs to be redisplayed
         */
        public void setIds(int[] ids) {
            mIdsReference = ids;            
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mViewReference != null) {
                final View view = mViewReference.get();
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
            for (int i = 0; i < mIdsReference.length; i++) {
                View viewToHide = mRowViewReference.get().findViewById(mIdsReference[i]);
                if (viewToHide != null) {
                    viewToHide.setVisibility(View.VISIBLE);
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
        
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
        
        @Override
        public void onAnimationStart(Animation animation) {
        }
    }
}
