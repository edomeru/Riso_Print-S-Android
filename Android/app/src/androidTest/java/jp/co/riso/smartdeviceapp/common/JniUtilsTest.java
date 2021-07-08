package jp.co.riso.smartdeviceapp.common;

import junit.framework.TestCase;

public class JniUtilsTest extends TestCase {
    private final String[] IPv4_VALID_ADDRESS = {
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
    };
    
    private final String[] IP_INVALID_ADDRESS = {
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
    };
    
    public JniUtilsTest(String name) {
        super(name);
    }

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
        JniUtils jniUtils = new JniUtils();
        assertNotNull(jniUtils);
    }

    //================================================================================
    // Tests - validateIp
    //================================================================================
    
    public void testValidateIp_nullIp() {
        String ipAddress = JniUtils.validateIp(null);
        assertNull(ipAddress);
    }
    
    public void testValidateIp_validIp() {
        String ipAddress;
        
        for (int i = 0; i < IPv4_VALID_ADDRESS.length; i++) {
            ipAddress = JniUtils.validateIp(null);
            assertNull(ipAddress);
        }
    }
    
    public void testValidateIp_invalidIp() {
        String ipAddress;
        for (String ip_invalid_address : IP_INVALID_ADDRESS) {
            ipAddress = JniUtils.validateIp(ip_invalid_address);
            assertNull(ipAddress);
        }
    }
}
