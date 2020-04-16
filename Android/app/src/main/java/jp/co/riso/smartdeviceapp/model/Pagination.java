/*
 * Copyright (c) 2018 RISO, Inc. All rights reserved.
 *
 * Pagination.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

// https://stackoverflow.com/a/32096884
/**
 * @class Pagination
 *
 * @brief Class representation of pagination for PDF
 */
public class Pagination {
    private final boolean mIncludePad;
    private final int mWidth;
    private final int mHeight;
    private final float mSpacingMult;
    private final float mSpacingAdd;
    private final CharSequence mText;
    private final TextPaint mPaint;
    private final List<CharSequence> mPages;

    /**
     * @brief Pagination Constructor. <br>
     *
     * Create a pagination instance
     *
     * @param text string content
     * @param pageW page width with margin
     * @param pageH page height with margin
     * @param paint text paint
     * @param spacingMult spacing multiplier
     * @param spacingAdd spacing addition
     * @param includePad include padding
     */
    public Pagination(CharSequence text, int pageW, int pageH, TextPaint paint, float spacingMult, float spacingAdd, boolean includePad) {
        this.mText = text;
        this.mWidth = pageW;
        this.mHeight = pageH;
        this.mPaint = paint;
        this.mSpacingMult = spacingMult;
        this.mSpacingAdd = spacingAdd;
        this.mIncludePad = includePad;
        this.mPages = new ArrayList<CharSequence>();

        layout();
    }

    /**
     * @brief Compute pagination
     */
    private void layout() {
        final StaticLayout layout = new StaticLayout(mText, mPaint, mWidth, Layout.Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, mIncludePad);

        final int lines = layout.getLineCount();
        final CharSequence text = layout.getText();
        int startOffset = 0;
        int height = mHeight;

        for (int i = 0; i < lines; i++) {
            if (height < layout.getLineBottom(i)) {
                // When the layout height has been exceeded
                addPage(text.subSequence(startOffset, layout.getLineStart(i)));
                startOffset = layout.getLineStart(i);
                height = layout.getLineTop(i) + mHeight;
            }

            if (i == lines - 1) {
                // Put the rest of the text into the last page
                CharSequence remainingText = text.subSequence(startOffset, layout.getLineEnd(i));
                if (!remainingText.toString().isEmpty()) {
                    addPage(remainingText);
                }
                return;
            }
        }
    }

    /**
     * @brief Add page
     *
     * @param text remaining text
     */
    private void addPage(CharSequence text) {
        mPages.add(text);
    }

    /**
     * @brief Obtain number of pages
     *
     * @return Total number of pages
     */
    public int size() {
        return mPages.size();
    }

    /**
     * @brief Obtain string on a page
     *
     * @param index page number
     *
     * @return string content of page
     */
    public CharSequence get(int index) {
        return (index >= 0 && index < mPages.size()) ? mPages.get(index) : null;
    }
}
