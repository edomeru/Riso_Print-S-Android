package jp.co.riso.android.text

import android.text.InputFilter.LengthFilter
import android.widget.EditText
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.fail
import org.junit.Test

class InvalidCharacterFilterTest {

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor_NullSet() {
        try {
            InvalidCharacterFilter(null)
        } catch (e: NullPointerException) {
            fail()
        }
    }

    @Test
    fun testConstructor_ValidSet() {
        val filter = InvalidCharacterFilter("abcd")
        assertNotNull(filter)
    }

    // ================================================================================
    // Tests - filter
    // ================================================================================
    @Test
    fun testFilter() {
        val editText = EditText(SmartDeviceApp.getAppContext())
        editText.filters = arrayOf(
            LengthFilter(8),
            InvalidCharacterFilter("abcd")
        )
        editText.setText(INVALID_INPUT_TEXT)
        editText.setText(VALID_INPUT_TEXT)
    }

    companion object {
        private const val INVALID_INPUT_TEXT = "!"
        private const val VALID_INPUT_TEXT = "a"
    }
}