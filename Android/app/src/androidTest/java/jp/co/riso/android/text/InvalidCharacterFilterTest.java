
package jp.co.riso.android.text;

import android.text.InputFilter;
import android.widget.EditText;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import junit.framework.TestCase;

public class InvalidCharacterFilterTest extends TestCase {
    private static final String INVALID_INPUT_TEXT = "!";
    private static final String VALID_INPUT_TEXT = "a";

    public InvalidCharacterFilterTest(String name) {
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

    public void testConstructor_NullSet() {
        try {
            new InvalidCharacterFilter(null);
        } catch (NullPointerException e) {
            fail();
        }
    }

    public void testConstructor_ValidSet() {
        InvalidCharacterFilter filter = new InvalidCharacterFilter("abcd");
        assertNotNull(filter);
    }
    
    // ================================================================================
    // Tests - filter
    // ================================================================================

    public void testFilter() {
        EditText editText = new EditText(SmartDeviceApp.getAppContext());
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(8),
                new InvalidCharacterFilter("abcd")
        });
        editText.setText(INVALID_INPUT_TEXT);
        editText.setText(VALID_INPUT_TEXT);
    }
}
