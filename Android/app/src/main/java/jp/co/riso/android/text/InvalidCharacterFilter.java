/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * InvalidCharacterFilter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.text;

import java.util.HashSet;
import java.util.Set;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * @class InvalidCharacterFilter
 * 
 * @brief EditText filter for Invalid characters
 */
public class InvalidCharacterFilter implements InputFilter {
    private Set<Character> mCharSet;
    
    /**
     * @brief Constructor for invalid characters input filter
     * 
     * @param invalidChars Invalid characters in string format
     */
    public InvalidCharacterFilter(String invalidChars) {
        mCharSet = new HashSet<Character>();
        if (invalidChars == null) {
            return;
        }
        for (int i = 0; i < invalidChars.length(); i++) {
            mCharSet.add(invalidChars.charAt(i));
        }
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) { 
            if (mCharSet.contains(source.charAt(i))) {
                return ""; 
            } 
        }
        return null; 
    }
}