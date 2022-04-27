package jp.co.riso.android.text

import android.text.InputFilter.LengthFilter
import android.widget.EditText
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase.*
import org.junit.Test

class CommunityNameFilterTest {
    private var _wasInvalid = false
    private var _errorSent = false

    // ================================================================================
    // Tests - filter
    // ================================================================================
    @Test
    fun testFilter() {
        val editText = EditText(SmartDeviceApp.getAppContext())
        editText.filters = arrayOf(
            LengthFilter(AppConstants.CONST_COMMUNITY_NAME_LIMIT),
            SnmpCommunityNameFilter { showError ->
                _wasInvalid = true
                _errorSent = showError
            }
        )
        editText.setText(VALID_INPUT_TEXT)
        assertEquals(editText.text.toString(), VALID_INPUT_TEXT)
        assertFalse(_wasInvalid)
        assertFalse(_errorSent)
        editText.setText(INVALID_INPUT_TEXT_SINGLE_CHAR)
        assertTrue(editText.text.toString().isEmpty())
        assertTrue(_wasInvalid)
        assertFalse(_errorSent)
        _wasInvalid = false
        editText.setText(INVALID_INPUT_TEXT)
        assertTrue(editText.text.toString().isEmpty())
        assertTrue(_wasInvalid)
        assertTrue(_errorSent)
    }

    companion object {
        private const val INVALID_INPUT_TEXT_SINGLE_CHAR = "!"
        private const val INVALID_INPUT_TEXT = "!+"
        private const val VALID_INPUT_TEXT = "\\"
    }
}