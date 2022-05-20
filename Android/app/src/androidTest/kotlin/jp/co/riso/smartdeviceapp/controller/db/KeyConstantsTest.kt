package jp.co.riso.smartdeviceapp.controller.db

import junit.framework.TestCase.assertNotNull
import org.junit.Test

class KeyConstantsTest {

    @Test
    fun testConstructor() {
        val keyConstants = KeyConstants
        assertNotNull(keyConstants)
    }
}