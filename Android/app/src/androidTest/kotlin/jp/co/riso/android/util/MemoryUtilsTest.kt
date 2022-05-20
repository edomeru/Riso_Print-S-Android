package jp.co.riso.android.util

import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.Test

class MemoryUtilsTest : BaseActivityTestUtil() {

    //================================================================================
    // Test getCacheSizeBasedOnMemoryClass
    //================================================================================
    @Test
    fun testGetCacheSizeBasedOnMemoryClass_Valid() {
        val memoryClass = MemoryUtils.getCacheSizeBasedOnMemoryClass(mainActivity)
        TestCase.assertTrue(memoryClass > 0)
    }

    @Test
    fun testGetCacheSizeBasedOnMemoryClass_Invalid() {
        val memoryClass = MemoryUtils.getCacheSizeBasedOnMemoryClass(null)
        TestCase.assertTrue(memoryClass == 0)
    }

    //================================================================================
    // Test getAvailableMemory
    //================================================================================
    @Test
    fun testGetAvailableMemory_Valid() {
        val availableMemory = MemoryUtils.getAvailableMemory(mainActivity)
        TestCase.assertTrue(availableMemory > 0)
    }

    @Test
    fun testGetAvailableMemory_Invalid() {
        val availableMemory = MemoryUtils.getAvailableMemory(null)
        TestCase.assertTrue(java.lang.Float.isNaN(availableMemory))
    }
}