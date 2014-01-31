package jp.co.alinkgroup.android.util;

import junit.framework.TestCase;

public class LoggerTest extends TestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testConstructor() {
        Logger logger = new Logger();
        assertNotNull(logger);
    }
}
