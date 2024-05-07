package jp.co.riso.smartdeviceapp.model

import jp.co.riso.smartdeviceapp.MockTestUtil
import org.junit.Assert
import org.junit.Test

class ContentPrintFileTest {
    @Test
    fun testContentPrintFile_Init() {
        val fileId = TEST_FILE_ID
        val filename = TEST_FILE_NAME
        val printSettings = ContentPrintPrintSettings()
        val contentPrintFile = ContentPrintFile(fileId, filename, printSettings)
        Assert.assertNotNull(contentPrintFile)
        Assert.assertNull(contentPrintFile.thumbnailImagePath)
    }

    @Test
    fun testContentPrintFile_InitNull() {
        val fileId = TEST_FILE_ID
        val filename = null
        val printSettings = null
        val contentPrintFile = ContentPrintFile(fileId, filename, printSettings)
        Assert.assertNotNull(contentPrintFile)
        Assert.assertEquals(contentPrintFile.fileId, TEST_FILE_ID)
        Assert.assertNull(contentPrintFile.filename)
        Assert.assertNull(contentPrintFile.printSettings)
        Assert.assertNull(contentPrintFile.thumbnailImagePath)
    }

    @Test
    fun testContentPrintFile_UpdateValues() {
        val fileId = TEST_FILE_ID
        val filename = null
        val printSettings = null
        val contentPrintFile = ContentPrintFile(fileId, filename, printSettings)
        contentPrintFile.fileId = 1000
        contentPrintFile.filename = TEST_FILE_NAME
        contentPrintFile.printSettings = ContentPrintPrintSettings()
        Assert.assertNotNull(contentPrintFile)
        Assert.assertEquals(contentPrintFile.fileId, 1000)
        Assert.assertNotNull(contentPrintFile.filename)
        Assert.assertNotNull(contentPrintFile.printSettings)
        Assert.assertNull(contentPrintFile.thumbnailImagePath)
    }

    @Test
    fun testContentPrintFile_ThumbnailImagePath() {
        val fileId = TEST_FILE_ID
        val filename = TEST_FILE_NAME
        val printSettings = ContentPrintPrintSettings()
        val contentPrintFile = ContentPrintFile(fileId, filename, printSettings)
        contentPrintFile.thumbnailImagePath = MockTestUtil.cacheDir().absolutePath
        Assert.assertNotNull(contentPrintFile)
        Assert.assertNotNull(contentPrintFile.thumbnailImagePath)
    }

    companion object {
        private const val TEST_FILE_ID = 1
        private const val TEST_FILE_NAME = "test.pdf"
    }
}