package jp.co.riso.android.text;

import junit.framework.TestCase;

public class AlphaNumericFilterTest extends TestCase {

    public AlphaNumericFilterTest(String name) {
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
        AlphaNumericFilter filter = new AlphaNumericFilter();
        assertNotNull(filter);
    }
}
