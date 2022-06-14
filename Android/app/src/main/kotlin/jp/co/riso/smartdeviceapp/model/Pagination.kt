/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * Pagination.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model

import android.text.Layout
import android.text.TextPaint
import android.text.StaticLayout
import java.util.ArrayList

// https://stackoverflow.com/a/32096884
/**
 * @class Pagination
 *
 * @brief Class representation of pagination for PDF
 */
class Pagination(
    private val _text: CharSequence,
    private val _width: Int,
    private val _height: Int,
    private val _paint: TextPaint,
    private val _spacingMult: Float,
    private val _spacingAdd: Float,
    private val _includePad: Boolean
) {
    private val _pages: MutableList<CharSequence>

    /**
     * @brief Compute pagination
     */
    private fun layout() {
        val builder = StaticLayout.Builder.obtain(_text, 0, _text.length, _paint, _width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(_spacingAdd, _spacingMult)
            .setIncludePad(_includePad)
        val layout = builder.build()
        val lines = layout.lineCount
        val text = layout.text
        var startOffset = 0
        var height = _height
        for (i in 0 until lines) {
            if (height < layout.getLineBottom(i)) {
                // When the layout height has been exceeded
                addPage(text.subSequence(startOffset, layout.getLineStart(i)))
                startOffset = layout.getLineStart(i)
                height = layout.getLineTop(i) + _height
            }
            if (i == lines - 1) {
                // Put the rest of the text into the last page
                val remainingText = text.subSequence(startOffset, layout.getLineEnd(i))
                if (remainingText.toString().isNotEmpty()) {
                    addPage(remainingText)
                }
                return
            }
        }
    }

    /**
     * @brief Add page
     *
     * @param text remaining text
     */
    private fun addPage(text: CharSequence) {
        _pages.add(text)
    }

    /**
     * @brief Obtain number of pages
     *
     * @return Total number of pages
     */
    fun size(): Int {
        return _pages.size
    }

    /**
     * @brief Obtain string on a page
     *
     * @param index page number
     *
     * @return string content of page
     */
    operator fun get(index: Int): CharSequence? {
        return if (index >= 0 && index < _pages.size) _pages[index] else null
    }

    /**
     * @brief Pagination Constructor. <br></br>
     *
     * Create a pagination instance
     */
    init {
        _pages = ArrayList()
        layout()
    }
}