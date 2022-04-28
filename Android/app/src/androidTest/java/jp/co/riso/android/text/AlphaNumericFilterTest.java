package jp.co.riso.android.text;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.text.InputFilter;
import android.widget.EditText;

public class AlphaNumericFilterTest extends  ActivityInstrumentationTestCase2<MainActivity> {
    private static final String INVALID_INPUT_TEXT = "!";
    private static final String VALID_INPUT_TEXT = "a";
    private static final String KANJI_TEXT_ONLY = "この言葉は漢字です。";
    private static final String KANJI_WITH_NUMBERS = "この言葉は漢字です。12345";
    private static final String KANJI_WITH_LETTERS = "ABCDEこの言葉は漢字です。";
    private static final String KANJI_WITH_ALPHANUM = "この言葉12345は漢字です。ABCDE";
    
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
        EditText editText = new EditText(SmartDeviceApp.Companion.getAppContext());
        editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(8),
                new AlphaNumericFilter()
        });
        editText.setText(INVALID_INPUT_TEXT);
        assertEquals("", editText.getText().toString());
        editText.setText(VALID_INPUT_TEXT);
        assertEquals(VALID_INPUT_TEXT, editText.getText().toString());
    }
    
    public void testFilter_Kanji() {
        EditText editText = new EditText(SmartDeviceApp.Companion.getAppContext());
        editText.setFilters(new InputFilter[] {
                new AlphaNumericFilter()
        });
        editText.setText(KANJI_TEXT_ONLY);
        assertEquals("", editText.getText().toString());
        editText.setText(KANJI_WITH_NUMBERS);
        assertEquals("12345", editText.getText().toString());
        editText.setText(KANJI_WITH_LETTERS);
        assertEquals("ABCDE", editText.getText().toString());
        editText.setText(KANJI_WITH_ALPHANUM);
        assertEquals("12345ABCDE", editText.getText().toString());
    }
    
}
