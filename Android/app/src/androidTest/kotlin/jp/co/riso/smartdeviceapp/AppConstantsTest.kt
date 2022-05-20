package jp.co.riso.smartdeviceapp

import junit.framework.TestCase.assertNotNull
import org.junit.Test

class AppConstantsTest {

    //================================================================================
    // Tests - constructors
    //================================================================================
    @Test
    fun testConstructor() {
        val appConstants = AppConstants
        assertNotNull(appConstants)
    }
}