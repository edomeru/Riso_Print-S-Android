/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AlphaNumericFilter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.text;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * EditText filter for Alpha Numeric characters
 */
public class AlphaNumericFilter implements InputFilter {
    
    /** {@inheritDoc} */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        return source.toString().replaceAll("[^A-Za-z0-9]", "");
    }
    
}