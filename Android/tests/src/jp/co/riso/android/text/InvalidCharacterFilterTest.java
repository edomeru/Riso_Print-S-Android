
package jp.co.riso.android.text;

import junit.framework.TestCase;

public class InvalidCharacterFilterTest extends TestCase {

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
        } catch (NullPointerException e)
        {
            fail();
        }
    }

    public void testConstructor_ValidSet() {
        InvalidCharacterFilter filter = new InvalidCharacterFilter("abcd");
        assertNotNull(filter);
    }
}
