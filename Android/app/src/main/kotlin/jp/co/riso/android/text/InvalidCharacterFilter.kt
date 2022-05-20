/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * InvalidCharacterFilter.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.text

import android.text.InputFilter
import android.text.Spanned
import java.util.HashSet

/**
 * @class InvalidCharacterFilter
 *
 * @brief EditText filter for Invalid characters
 */
class InvalidCharacterFilter(invalidChars: String?) : InputFilter {
    private val _charSet: MutableSet<Char>
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        for (i in start until end) {
            if (_charSet.contains(source[i])) {
                return ""
            }
        }
        return ""
    }

    /**
     * @brief Constructor for invalid characters input filter
     */
    init {
        _charSet = HashSet()
        invalidChars?.let {
            for (element in invalidChars) {
                _charSet.add(element)
            }
        }
    }
}