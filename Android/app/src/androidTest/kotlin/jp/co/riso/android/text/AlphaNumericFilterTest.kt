package jp.co.riso.android.text

import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.widget.EditText
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase
import org.junit.Test

class AlphaNumericFilterTest {

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor() {
        val filter = AlphaNumericFilter()
        TestCase.assertNotNull(filter)
    }

    // ================================================================================
    // Tests - filter
    // ================================================================================
    @Test
    fun testFilter() {
        val editText = EditText(SmartDeviceApp.appContext!!)
        editText.filters = arrayOf(
            LengthFilter(8),
            AlphaNumericFilter()
        )
        editText.setText(INVALID_INPUT_TEXT)
        TestCase.assertEquals("", editText.text.toString())
        editText.setText(VALID_INPUT_TEXT)
        TestCase.assertEquals(VALID_INPUT_TEXT, editText.text.toString())
    }

    @Test
    fun testFilter_Kanji() {
        val editText = EditText(SmartDeviceApp.appContext!!)
        editText.filters = arrayOf<InputFilter>(
            AlphaNumericFilter()
        )
        editText.setText(KANJI_TEXT_ONLY)
        TestCase.assertEquals("", editText.text.toString())
        editText.setText(KANJI_WITH_NUMBERS)
        TestCase.assertEquals("12345", editText.text.toString())
        editText.setText(KANJI_WITH_LETTERS)
        TestCase.assertEquals("ABCDE", editText.text.toString())
        editText.setText(KANJI_WITH_ALPHANUM)
        TestCase.assertEquals("12345ABCDE", editText.text.toString())
    }

    companion object {
        private const val INVALID_INPUT_TEXT = "!"
        private const val VALID_INPUT_TEXT = "a"
        private const val KANJI_TEXT_ONLY = "この言葉は漢字です。"
        private const val KANJI_WITH_NUMBERS = "この言葉は漢字です。12345"
        private const val KANJI_WITH_LETTERS = "ABCDEこの言葉は漢字です。"
        private const val KANJI_WITH_ALPHANUM = "この言葉12345は漢字です。ABCDE"
    }
}