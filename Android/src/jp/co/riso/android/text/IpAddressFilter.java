/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * IpAddressFilter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.text;

import java.util.HashSet;
import java.util.Set;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * EditText filter for IP Address characters
 */
public class IpAddressFilter implements InputFilter {
    private Set<Character> mCharSet;
    
    /**
     * Constructor
     */
    public IpAddressFilter() {
        String validChars = "1234567890abcdefABCDEF.:";
        mCharSet = new HashSet<Character>();
        for (int i = 0; i < validChars.length(); i++) {
            mCharSet.add(validChars.charAt(i));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            if (!mCharSet.contains(source.charAt(i))) {
                return "";
            }
        }
        return null;
    }
}