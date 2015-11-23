package jp.co.riso.smartdeviceapp.controller.db;

import junit.framework.TestCase;

public class KeyConstantsTest extends TestCase {

    public KeyConstantsTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor() {
        KeyConstants keyConstants = new KeyConstants();
        assertNotNull(keyConstants);
    }
}
