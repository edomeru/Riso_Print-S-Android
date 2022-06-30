package jp.co.riso.smartdeviceapp.common

import jp.co.riso.smartdeviceapp.common.JniUtils.validateIp
import jp.co.riso.smartdeviceapp.common.JniUtils.validateIpAddress
import junit.framework.TestCase

class JniUtilsTest(name: String?) : TestCase(name) {
    private val IPv4_VALID_ADDRESS = arrayOf(
        "0.0.0.0",
        "224.0.0.1",
        "192.168.0.1",
        "255.255.255.255",
        "0.0.0.0",
        "0.0.0.0",
        "224.0.0.1",
        "192.168.0.1",
        "255.255.255.255",
        "255.255.255.255",
        "1:2:3:4:5:6:7:8",
        "1:2:3:4:5:6:7::",
        "1:2:3:4:5:6::8",
        "1:2:3:4:5::7:8",
        "1:2:3:4::6:7:8",
        "1:2:3::5:6:7:8",
        "1:2::4:5:6:7:8",
        "1::3:4:5:6:7:8",
        "::2:3:4:5:6:7:8",
        "1:2:3:4:5::8",
        "1:2:3:4::7:8",
        "1:2:3::6:7:8",
        "1:2::5:6:7:8",
        "1::4:5:6:7:8",
        "1:2:3:4::8",
        "1:2:3::7:8",
        "1:2::6:7:8",
        "1::5:6:7:8",
        "::4:5:6:7:8",
        "1:2:3::8",
        "1:2::7:8",
        "1::6:7:8",
        "1:2::8",
        "1::7:8",
        "1::8",
        "::8",
        "::",
        "a::",
        "a::",
        "::f",
        "::f"
    )
    private val IP_INVALID_ADDRESS = arrayOf(
        "0.1.2.3.4",
        "x.x.x.x",
        "0.1.2.",
        "0.1.2",
        "0.1.",
        "0.1",
        "0.",
        "0",
        "z:2:3:4:5:6:7:8",
        "z:2:3:4:5:6:7::",
        "z:2:3:4:5:6::8",
        "z:2:3:4:5::7:8",
        "z:2:3:4::6:7:8",
        "z:2:3::5:6:7:8",
        "z:2::4:5:6:7:8",
        "z::3:4:5:6:7:8",
        "::z:3:4:5:6:7:8",
        "z:2:3:4:5::8",
        "z:2:3:4::7:8",
        "z:2:3::6:7:8",
        "z:2::5:6:7:8",
        "z::4:5:6:7:8",
        "z:2:3:4::8",
        "z:2:3::7:8",
        "z:2::6:7:8",
        "z::5:6:7:8",
        "::z:5:6:7:8",
        "z:2:3::8",
        "z:2::7:8",
        "z::6:7:8",
        "z:2::8",
        "z::7:8",
        "z::8",
        "::z"
    )

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
        val jniUtils = JniUtils
        assertNotNull(jniUtils)
    }

    //================================================================================
    // Tests - validateIp
    //================================================================================
    fun testValidateIp_nullIp() {
        val ipAddress = validateIp(null)
        assertNull(ipAddress)
    }

    fun testValidateIp_validIp() {
        var ipAddress: String?
        for (i in IPv4_VALID_ADDRESS.indices) {
            ipAddress = validateIp(null)
            assertNull(ipAddress)
        }
    }

    fun testValidateIp_invalidIp() {
        var ipAddress: String?
        for (ip_invalid_address in IP_INVALID_ADDRESS) {
            ipAddress = validateIp(ip_invalid_address)
            assertNull(ipAddress)
        }
    }

    fun testValidateIpAddress() {
        var ipAddress: String?
        for (i in IPv4_VALID_ADDRESS.indices) {
            ipAddress = validateIpAddress(null)
            assertNull(ipAddress)
        }
    }
}