/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * CommunityNameFilter.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.text

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class SnmpCommunityNameFilter(obs: InvalidInputObserver) : InputFilter {
    private var _invalidInputObserver: InvalidInputObserver? = obs
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (!isValid(source)) {
                // because this also filters characters typed from the keyboard
                // show error only when there are more than 1 characters
                // behavior is same with iOS
                _invalidInputObserver?.onInvalidInput(source.length > 1)
            return ""
        }
        return null
    }

    private fun isValid(source: CharSequence?): Boolean {
        val matcher = validPattern.matcher(source!!)
        return matcher.matches()
    }

    /**
     * Observer interface for invalid input
     */
    interface InvalidInputObserver {
        /**
         * @brief Notify the observer about invalid input
         */
        /**
         * @param showError if observer should show an error
         */
        fun onInvalidInput(showError: Boolean)
    }

    companion object {
        private const val VALID_CHARACTERS = "^[a-zA-Z0-9,./¥:;@\\[\\\\\\]^_]*$"
        private val validPattern = Pattern.compile(VALID_CHARACTERS)
    }
}