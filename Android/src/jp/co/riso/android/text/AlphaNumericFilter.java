/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AlphaNumericFilter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.text;

import java.util.regex.Pattern;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * @class AlphaNumericFilter
 * 
 * @brief EditText filter for Alpha Numeric characters
 */
public class AlphaNumericFilter implements InputFilter {
    
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (!Pattern.matches("^[A-Za-z0-9]*$", source)) {
            return source.toString().replaceAll("[^A-Za-z0-9]", "");
        }
        return null;
    }
    
}