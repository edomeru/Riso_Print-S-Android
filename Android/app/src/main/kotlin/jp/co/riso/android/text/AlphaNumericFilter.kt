/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * AlphaNumericFilter.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.text

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

/**
 * @class AlphaNumericFilter
 *
 * @brief EditText filter for Alpha Numeric characters
 */
class AlphaNumericFilter : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        return if (!Pattern.matches("^[A-Za-z0-9]*$", source)) {
            source.toString().replace("[^A-Za-z0-9]".toRegex(), "")
        } else null
    }
}