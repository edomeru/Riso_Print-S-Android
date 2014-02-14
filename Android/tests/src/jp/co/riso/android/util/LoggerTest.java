package jp.co.riso.android.util;

import android.test.AndroidTestCase;

public class LoggerTest extends AndroidTestCase {
    
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
