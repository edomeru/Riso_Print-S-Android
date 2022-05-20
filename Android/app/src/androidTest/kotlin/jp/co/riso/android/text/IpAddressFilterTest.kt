package jp.co.riso.android.text

import android.text.InputFilter.LengthFilter
import android.widget.EditText
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase.*
import org.junit.Test

class IpAddressFilterTest {

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor() {
        try {
            assertNotNull(IpAddressFilter())
        } catch (e: NullPointerException) {
            fail()
        }
    }

    // ================================================================================
    // Tests - filter
    // ================================================================================
    @Test
    fun testFilter_Invalid() {
        val editText = EditText(SmartDeviceApp.appContext!!)
        editText.filters = arrayOf(
            LengthFilter(8),
            IpAddressFilter()
        )
        editText.setText(INVALID_INPUT_TEXT)
        assertTrue(editText.text.toString().isEmpty())
    }

    @Test
    fun testFilter_Valid() {
        val editText = EditText(SmartDeviceApp.appContext!!)
        editText.filters = arrayOf(
            LengthFilter(8),
            IpAddressFilter()
        )
        editText.setText(VALID_INPUT_TEXT)
        assertTrue(VALID_INPUT_TEXT.contentEquals(editText.text))
    }

    companion object {
        private const val INVALID_INPUT_TEXT = "!"
        private const val VALID_INPUT_TEXT = "a"
    }
}