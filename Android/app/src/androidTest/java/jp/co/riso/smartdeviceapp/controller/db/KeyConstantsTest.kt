package jp.co.riso.smartdeviceapp.controller.db

import kotlin.Throws
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants
import junit.framework.TestCase
import java.lang.Exception

class KeyConstantsTest(name: String?) : TestCase(name) {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    fun testConstructor() {
        val keyConstants = KeyConstants
        assertNotNull(keyConstants)
    }
}