
package jp.co.riso.android.text;

import android.text.InputFilter;
import android.widget.EditText;

import junit.framework.TestCase;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

public class CommunityNameFilterTest extends TestCase {
    private static final String INVALID_INPUT_TEXT = "!";
    private static final String VALID_INPUT_TEXT = "\\";
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

    private void sendError() {
        errorSent = true;
    }
    
    // ================================================================================
    // Tests - filter
    // ================================================================================

    public void testFilter() {

        EditText editText = new EditText(SmartDeviceApp.getAppContext());
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(AppConstants.CONST_COMMUNITY_NAME_LIMIT),
                new SnmpCommunityNameFilter(new SnmpCommunityNameFilter.ErrorSender() {
                    @Override
                    public void send() {
                        sendError();
                    }
                })
        });
        editText.setText(INVALID_INPUT_TEXT);
        assertTrue(editText.getText().toString().isEmpty());
        assertTrue(errorSent);

        errorSent = false;
        editText.setText(VALID_INPUT_TEXT);
        assertTrue(editText.getText().toString().equals(VALID_INPUT_TEXT));
        assertFalse(errorSent);
    }
}
