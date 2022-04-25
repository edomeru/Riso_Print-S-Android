
package jp.co.riso.android.text;

import android.text.InputFilter;
import android.widget.EditText;

import junit.framework.TestCase;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

public class CommunityNameFilterTest extends TestCase {
    private static final String INVALID_INPUT_TEXT_SINGLE_CHAR = "!";
    private static final String INVALID_INPUT_TEXT = "!+";
    private static final String VALID_INPUT_TEXT = "\\";
    public boolean wasInvalid = false;
    public boolean errorSent = false;

    public CommunityNameFilterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    // ================================================================================
    // Tests - filter
    // ================================================================================

    public void testFilter() {

        EditText editText = new EditText(SmartDeviceApp.Companion.getAppContext());
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(AppConstants.CONST_COMMUNITY_NAME_LIMIT),
                new SnmpCommunityNameFilter(new SnmpCommunityNameFilter.InvalidInputObserver() {
                    @Override
                    public void onInvalidInput(boolean showError) {
                        wasInvalid = true;
                        errorSent = showError;
                    }
                })
        });

        editText.setText(VALID_INPUT_TEXT);
        assertEquals(editText.getText().toString(), VALID_INPUT_TEXT);
        assertFalse(wasInvalid);
        assertFalse(errorSent);

        editText.setText(INVALID_INPUT_TEXT_SINGLE_CHAR);
        assertTrue(editText.getText().toString().isEmpty());
        assertTrue(wasInvalid);
        assertFalse(errorSent);

        wasInvalid = false;
        editText.setText(INVALID_INPUT_TEXT);
        assertTrue(editText.getText().toString().isEmpty());
        assertTrue(wasInvalid);
        assertTrue(errorSent);
    }
}
