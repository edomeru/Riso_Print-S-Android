package jp.co.riso.android.util;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.test.ActivityInstrumentationTestCase2;

public class MemoryUtilsTest extends ActivityInstrumentationTestCase2<MainActivity> {
    
    public MemoryUtilsTest() {
        super(MainActivity.class);
    }
    
    public MemoryUtilsTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //================================================================================
    // Test getCacheSizeBasedOnMemoryClass
    //================================================================================

    public void testGetCacheSizeBasedOnMemoryClass_Valid() {
        int memoryClass = MemoryUtils.getCacheSizeBasedOnMemoryClass(getActivity());
        
        assertTrue(memoryClass > 0);
    }

    public void testGetCacheSizeBasedOnMemoryClass_Invalid() {
        int memoryClass = MemoryUtils.getCacheSizeBasedOnMemoryClass(null);
        
        assertTrue(memoryClass == 0);
    }

    
    //================================================================================
    // Test getAvailableMemory
    //================================================================================

    public void testGetAvailableMemory_Valid() {
        float availableMemory = MemoryUtils.getAvailableMemory(getActivity());
        
        assertTrue(availableMemory > 0);
    }

    public void testGetAvailableMemory_Invalid() {
        float availableMemory = MemoryUtils.getAvailableMemory(null);
        
        assertTrue(Float.isNaN(availableMemory));
    }
    
    
}
