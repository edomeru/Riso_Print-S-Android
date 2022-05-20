/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * IpAddressFilter.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.text

import android.text.InputFilter
import android.text.Spanned
import java.util.HashSet

/**
 * @class IpAddressFilter
 *
 * @brief EditText filter for IP Address characters
 */
class IpAddressFilter : InputFilter {
    private val _charSet: MutableSet<Char>
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        for (i in start until end) {
            if (!_charSet.contains(source[i])) {
                return ""
            }
        }
        return null
    }

    init {
        val validChars = "1234567890abcdefABCDEF.:"
        _charSet = HashSet()
        for (element in validChars) {
            _charSet.add(element)
        }
    }
}