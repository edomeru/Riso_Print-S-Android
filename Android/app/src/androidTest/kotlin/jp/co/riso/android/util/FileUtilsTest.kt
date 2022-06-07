package jp.co.riso.android.util

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.FileUtils.copy
import jp.co.riso.android.util.FileUtils.delete
import jp.co.riso.android.util.FileUtils.getFileName
import jp.co.riso.android.util.FileUtils.getFileSize
import jp.co.riso.android.util.FileUtils.getMimeType
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class FileUtilsTest : BaseActivityTestUtil() {
    private var _srcFile: File? = null
    private var _srcInputStream: InputStream? = null
    private var _dstFile: File? = null
    
    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor() {
        val utils = FileUtils
        TestCase.assertNotNull(utils)
    }

    // ================================================================================
    // Tests - copy
    // ================================================================================
    @Test
    fun testCopy_ValidParams() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            _srcFile = File(pdfPath)
            if (_srcFile == null) {
                TestCase.fail()
            }
            _dstFile = File(
                SmartDeviceApp.appContext!!.getExternalFilesDir("pdfs").toString() + "/"
                        + TEST_DST_FILE
            )
            if (_dstFile == null) {
                TestCase.fail()
            }
            copy(_srcFile, _dstFile)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testCopy_ValidParams2() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            _srcInputStream = FileInputStream(pdfPath)
            if (_srcInputStream == null) {
                TestCase.fail()
            }
            _dstFile = File(
                SmartDeviceApp.appContext!!.getExternalFilesDir("pdfs").toString() + "/"
                        + TEST_DST_FILE
            )
            if (_dstFile == null) {
                TestCase.fail()
            }
            copy(_srcInputStream, _dstFile)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testCopy_NullSrc() {
        try {
            _dstFile = File(
                SmartDeviceApp.appContext!!.getExternalFilesDir("pdfs").toString() + "/"
                        + TEST_DST_FILE
            )
            copy(_srcInputStream, _dstFile)
            copy(_srcFile, _dstFile)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testCopy_NullDst() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            _srcInputStream = FileInputStream(pdfPath)
            _srcFile = File(pdfPath)

            copy(_srcInputStream, _dstFile)
            copy(_srcFile, _dstFile)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testCopy_NullParameters() {
        try {
            copy(_srcInputStream, _dstFile)
            copy(_srcFile, _dstFile)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    // ================================================================================
    // Tests - get filename
    // ================================================================================
    @Test
    fun testGetFilename() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            _srcFile = File(pdfPath)
            if (_srcFile == null) {
                TestCase.fail()
            }
            TestCase.assertEquals(
                getFileName(
                    SmartDeviceApp.appContext,
                    Uri.fromFile(_srcFile),
                    true
                ), TEST_SRC_FILE
            )
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    // ================================================================================
    // Tests - get filesize
    // ================================================================================
    @Test
    fun testGetFileSize() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            _srcFile = File(pdfPath)
            TestCase.assertEquals(
                getFileSize(
                    SmartDeviceApp.appContext,
                    Uri.fromFile(_srcFile)
                ).toLong(), _srcFile!!.length()
            )
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    // ================================================================================
    // Tests - get mimetype
    // ================================================================================
    @Test
    fun testGetMimeType() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            _srcFile = File(pdfPath)
            TestCase.assertEquals(
                getMimeType(
                    SmartDeviceApp.appContext,
                    Uri.fromFile(_srcFile)
                ), "application/pdf"
            )
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testDelete() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            _srcFile = File(pdfPath)

            delete(_srcFile!!)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    // ================================================================================
    // Private
    // ================================================================================
    private fun getAssetPath(filename: String): String {
        val f = File(mainActivity!!.cacheDir.toString() + "/" + filename)
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        try {
            val `is` = assetManager.open(filename)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val fos = FileOutputStream(f)
            fos.write(buffer)
            fos.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return f.path
    }

    companion object {
        private const val TEST_SRC_FILE = "PDF-squarish.pdf"
        private const val TEST_DST_FILE = "sample_dst.pdf"
    }
}