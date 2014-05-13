package jp.co.riso.android.text;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.text.InputFilter;
import android.widget.EditText;

public class AlphaNumericFilterTest extends  ActivityInstrumentationTestCase2<MainActivity> {
    private static final String INVALID_INPUT_TEXT = "!";
    private static final String VALID_INPUT_TEXT = "a";
    
    public AlphaNumericFilterTest() {
        super(MainActivity.class);
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
        AlphaNumericFilter filter = new AlphaNumericFilter();
        assertNotNull(filter);
    }
    
    // ================================================================================
    // Tests - filter
    // ================================================================================

    public void testFilter() {
        EditText editText = new EditText(SmartDeviceApp.getAppContext());
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(8),
                new AlphaNumericFilter()
        });
        editText.setText(INVALID_INPUT_TEXT);
        editText.setText(VALID_INPUT_TEXT);

    }
    
}
