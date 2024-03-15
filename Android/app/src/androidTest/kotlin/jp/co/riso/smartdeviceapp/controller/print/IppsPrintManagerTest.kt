package jp.co.riso.smartdeviceapp.controller.print

import jp.co.riso.smartdeviceapp.common.DirectPrintManager
import junit.framework.TestCase
import org.junit.Test

class IppsPrintManagerTest: TestCase() {
    private var _directPrint: DirectPrintManager? = null
    private var _manager: IppsPrintManager? = null
    private val PAGE_COUNT = 1
    private val IP_ADDRESS = "192.168.1.200"
    private val FILE_NAME = "Test.pdf"
    private val JOB_NAME = "Test Job"

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        _directPrint = DirectPrintManager()
        _manager = IppsPrintManager(_directPrint!!, PAGE_COUNT, IP_ADDRESS, FILE_NAME, JOB_NAME)
    }

    // ================================================================================
    // Tests - cancel
    // ================================================================================
    @Test
    fun testCancel_AfterPrint() {
        _manager?.print()
        _manager?.cancel()
    }

    @Test
    fun testCancel_WithoutPrint() {
        _manager?.cancel()
    }
}