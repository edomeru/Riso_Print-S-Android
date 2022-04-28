package jp.co.riso.android.util

import jp.co.riso.android.util.FileUtils.copy
import jp.co.riso.android.util.FileUtils.getFileName
import jp.co.riso.android.util.FileUtils.getFileSize
import jp.co.riso.android.util.FileUtils.getMimeType
import android.test.ActivityInstrumentationTestCase2
import jp.co.riso.smartdeviceapp.view.MainActivity
import kotlin.Throws
import jp.co.riso.android.util.FileUtilsTest
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import android.content.res.AssetManager
import android.net.Uri
import junit.framework.TestCase
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import java.lang.RuntimeException

class FileUtilsTest : ActivityInstrumentationTestCase2<MainActivity> {
    private var mSrcFile: File? = null
    private var mDstFile: File? = null

    constructor() : super(MainActivity::class.java) {}
    constructor(activityClass: Class<MainActivity?>?) : super(activityClass) {}

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    fun testConstructor() {
        val utils: FileUtils = jp.co.riso.android.util.FileUtils
        TestCase.assertNotNull(utils)
    }

    // ================================================================================
    // Tests - copy
    // ================================================================================
    fun testCopy_ValidParams() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            mSrcFile = File(pdfPath)
            if (mSrcFile == null) {
                TestCase.fail()
            }
            mDstFile = File(
                SmartDeviceApp.getAppContext().getExternalFilesDir("pdfs").toString() + "/"
                        + TEST_DST_FILE
            )
            if (mDstFile == null) {
                TestCase.fail()
            }
            copy(mSrcFile, mDstFile)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    fun testCopy_NullSrc() {
        try {
            copy(null as InputStream?, mDstFile)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    fun testCopy_NullDst() {
        try {
            copy(mSrcFile, null)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    // ================================================================================
    // Tests - get filename
    // ================================================================================
    fun testGetFilename() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            mSrcFile = File(pdfPath)
            if (mSrcFile == null) {
                TestCase.fail()
            }
            TestCase.assertEquals(
                getFileName(
                    SmartDeviceApp.getAppContext(),
                    Uri.fromFile(mSrcFile),
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
    fun testGetFileSize() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            mSrcFile = File(pdfPath)
            TestCase.assertEquals(
                getFileSize(
                    SmartDeviceApp.getAppContext(),
                    Uri.fromFile(mSrcFile)
                ).toLong(), mSrcFile!!.length()
            )
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    // ================================================================================
    // Tests - get mimetype
    // ================================================================================
    fun testGetMimeType() {
        try {
            val pdfPath = getAssetPath(TEST_SRC_FILE)
            mSrcFile = File(pdfPath)
            TestCase.assertEquals(
                getMimeType(
                    SmartDeviceApp.getAppContext(),
                    Uri.fromFile(mSrcFile)
                ), "application/pdf"
            )
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    // ================================================================================
    // Private
    // ================================================================================
    private fun getAssetPath(filename: String): String {
        val f = File(activity!!.cacheDir.toString() + "/" + filename)
        val assetManager = instrumentation.context.assets
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