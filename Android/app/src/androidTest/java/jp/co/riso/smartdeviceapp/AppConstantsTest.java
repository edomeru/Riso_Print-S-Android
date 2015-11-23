package jp.co.riso.smartdeviceapp;

import junit.framework.TestCase;

public class AppConstantsTest extends TestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //================================================================================
    // Tests - constructors
    //================================================================================
    
    public void testConstructor() {
        AppConstants appConstants = new AppConstants();
        assertNotNull(appConstants);
    }
}
