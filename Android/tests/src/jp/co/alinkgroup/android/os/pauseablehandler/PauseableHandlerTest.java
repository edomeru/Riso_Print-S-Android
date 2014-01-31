package jp.co.alinkgroup.android.os.pauseablehandler;

import junit.framework.TestCase;

public class PauseableHandlerTest extends TestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor_NullCallback() {
        PauseableHandler handler = new PauseableHandler(null);
        assertNotNull(handler);
    }
}
