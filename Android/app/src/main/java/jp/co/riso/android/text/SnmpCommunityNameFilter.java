/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * CommunityNameFilter.java
 * Tamago Clicker
 * Created by: a-LINK Group
 */

package jp.co.riso.android.text;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnmpCommunityNameFilter implements InputFilter {

    private static final String VALID_CHARACTERS = "^[a-zA-Z0-9,./¥:;@\\[\\\\\\]\\^_]*$";
    private static final Pattern validPattern = Pattern.compile(VALID_CHARACTERS);
    private ErrorSender mErrorSender = null;

    public SnmpCommunityNameFilter(ErrorSender sender) {
        mErrorSender = sender;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (!isValid(source)) {
            if (mErrorSender != null) {
                mErrorSender.send();
            }
            return "";
        }

        return null;
    }

    public boolean isValid(CharSequence source) {
        Matcher matcher = validPattern.matcher(source);
        return matcher.matches();
    }

    public interface ErrorSender {
        void send();
    }
}