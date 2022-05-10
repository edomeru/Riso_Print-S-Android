/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * DisplayDeleteAnimation.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.anim

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import java.lang.ref.WeakReference

/**
 * @class DisplayDeleteAnimation
 *
 * @brief Class for displaying a view (e.g. delete button) using TranslateAnimation and
 * hiding other views (optional).
 */
class DisplayDeleteAnimation {
    /**
     * @brief This method displays a view (e.g. delete button) using TranslateAnimation
     * if animation is enabled and hides other views.
     *
     * @param view The row layout
     * @param animate Determines if animation will be used
     * @param deleteId The id of the view to be displayed (e.g. delete button)
     * @param ids The ids of the views to be hidden (optional)
     */
    fun beginDeleteModeOnView(view: View, animate: Boolean, deleteId: Int, vararg ids: Int) {
        val deleteButton = view.findViewById<View>(deleteId)
        for (id in ids) {
            val viewToHide = view.findViewById<View>(id)
            if (viewToHide != null) {
                viewToHide.visibility = View.GONE
            }
        }
        if (deleteButton != null) {
            deleteButton.visibility = View.VISIBLE
            if (animate) {
                val translate = TranslateAnimation(
                    deleteButton.width.toFloat(), 0F, 0F, 0F
                )
                translate.duration = ANIM_DURATION.toLong()
                if (deleteButton.animation != null) {
                    deleteButton.animation.setAnimationListener(null)
                    deleteButton.animation.cancel()
                }
                deleteButton.clearAnimation()
                deleteButton.startAnimation(translate)
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
    fun endDeleteMode(view: View, animate: Boolean, deleteId: Int, vararg ids: Int) {
        val deleteButton = view.findViewById<View>(deleteId)
        if (deleteButton != null) {
            if (animate) {
                val translate = TranslateAnimation(
                    0F, deleteButton.width
                        .toFloat(), 0F, 0F
                )
                translate.duration = ANIM_DURATION.toLong()
                val listener = HideOnFadeAnimationListener()
                listener.setRowView(view)
                listener.setView(deleteButton)
                listener.setIds(ids)
                translate.setAnimationListener(listener)
                if (deleteButton.animation != null) {
                    deleteButton.animation.setAnimationListener(null)
                    deleteButton.animation.cancel()
                }
                deleteButton.clearAnimation()
                deleteButton.startAnimation(translate)
            } else {
                deleteButton.visibility = View.GONE
                for (id in ids) {
                    val viewToHide = view.findViewById<View>(id)
                    if (viewToHide != null) {
                        viewToHide.visibility = View.VISIBLE
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
    inner class HideOnFadeAnimationListener : AnimationListener {
        private var _rowViewReference: WeakReference<View>? = null
        private var _viewReference: WeakReference<View>? = null
        private var _idsReference: IntArray? = null

        /**
         * @brief Set delete view
         *
         * @param view Delete button
         */
        fun setView(view: View) {
            _viewReference = WeakReference(view)
        }

        /**
         * @brief Set Row view
         *
         * @param view Row view or parent view of the delete button
         */
        fun setRowView(view: View) {
            _rowViewReference = WeakReference(view)
        }

        /**
         * @brief Set other resource ID that needs to be redisplayed
         *
         * @param ids Array of resource IDs that needs to be redisplayed
         */
        fun setIds(ids: IntArray?) {
            _idsReference = ids
        }

        override fun onAnimationEnd(animation: Animation) {
            if (_viewReference != null) {
                val view = _viewReference!!.get()
                if (view != null) {
                    view.visibility = View.GONE
                }
            }
            for (j in _idsReference!!) {
                val viewToHide = _rowViewReference!!.get()!!.findViewById<View>(j)
                if (viewToHide != null) {
                    viewToHide.visibility = View.VISIBLE
                    val alpha = AlphaAnimation(0.0f, 1.0f)
                    alpha.duration = ANIM_DURATION.toLong()
                    if (viewToHide.animation != null) {
                        viewToHide.animation.setAnimationListener(null)
                        viewToHide.animation.cancel()
                    }
                    viewToHide.clearAnimation()
                    viewToHide.startAnimation(alpha)
                }
            }
        }

        override fun onAnimationRepeat(animation: Animation) {}
        override fun onAnimationStart(animation: Animation) {}
    }

    companion object {
        private const val ANIM_DURATION = 250
    }
}