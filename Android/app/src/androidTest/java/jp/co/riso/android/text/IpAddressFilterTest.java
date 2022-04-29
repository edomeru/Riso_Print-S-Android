
package jp.co.riso.android.text;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import junit.framework.TestCase;
import android.text.InputFilter;
import android.widget.EditText;

public class IpAddressFilterTest extends TestCase {
    private static final String INVALID_INPUT_TEXT = "!";
    private static final String VALID_INPUT_TEXT = "a";

    public IpAddressFilterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================

    public void testConstructor() {
        try {
            assertNotNull(new IpAddressFilter());
        } catch (NullPointerException e) {
            fail();
        }
    }

    // ================================================================================
    // Tests - filter
    // ================================================================================

    public void testFilter_Invalid() {
        EditText editText = new EditText(SmartDeviceApp.Companion.getAppContext());
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(8),
                new IpAddressFilter()
        });
        editText.setText(INVALID_INPUT_TEXT);
        assertTrue(editText.getText().toString().isEmpty());

    }

    public void testFilter_Valid() {
        EditText editText = new EditText(SmartDeviceApp.Companion.getAppContext());
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(8),
                new IpAddressFilter()
        });
        editText.setText(VALID_INPUT_TEXT);
        assertTrue(VALID_INPUT_TEXT.contentEquals(editText.getText()));
    }
}
