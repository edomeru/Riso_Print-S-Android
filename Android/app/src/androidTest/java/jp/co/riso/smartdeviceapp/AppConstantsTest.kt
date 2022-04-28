package jp.co.riso.smartdeviceapp

import kotlin.Throws
import jp.co.riso.smartdeviceapp.AppConstants
import junit.framework.TestCase
import java.lang.Exception

class AppConstantsTest : TestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    //================================================================================
    // Tests - constructors
    //================================================================================
    fun testConstructor() {
        val appConstants = AppConstants
        assertNotNull(appConstants)
    }
}