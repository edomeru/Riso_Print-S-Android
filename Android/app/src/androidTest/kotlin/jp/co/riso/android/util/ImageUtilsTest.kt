package jp.co.riso.android.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.provider.OpenableColumns
import android.test.ProviderTestCase2
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.FileUtils.getFileName
import jp.co.riso.android.util.FileUtilsContentProviderTest.Companion.TEST_AUTHORITY
import jp.co.riso.android.util.FileUtilsContentProviderTest.TestContentProvider
import jp.co.riso.android.util.ImageUtils.renderBmpToCanvas
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartdeviceapp.view.MainActivity
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ImageUtilsTest : BaseActivityTestUtil()
{

//    private val TEST_FILENAME_PDF = "testFileName.pdf"
//    private val TEST_FILENAME_NON_PDF = "testFileName.png"
//    private val TEST_DISPLAYNAME_PDF = "testDisplayName.pdf"
//    private val TEST_DISPLAYNAME_NON_PDF = "testDisplayName.png"
//    private val TEST_FILESIZE = 999
//    private val TEST_CONTENT_PREFIX = "content://"
//    private val TEST_URI_PDF = "$TEST_CONTENT_PREFIX${TEST_AUTHORITY}/$TEST_FILENAME_PDF"
//    private val TEST_URI_NON_PDF =
//        "$TEST_CONTENT_PREFIX${TEST_AUTHORITY}/$TEST_FILENAME_NON_PDF"
//    private val INVALID_COLUMNS = arrayOf("testColumn")
//    private val INVALID_DATA = arrayOf("testData")
//    private val VALID_COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
//    private val VALID_DATA_PDF = arrayOf(TEST_DISPLAYNAME_PDF, TEST_FILESIZE.toString())
//    private val VALID_DATA_NON_PDF = arrayOf(TEST_DISPLAYNAME_NON_PDF, TEST_FILESIZE.toString())
//    private var _testContext: Context? = null
//    private var _testUri: Uri? = null
//    private var _testCursor: MatrixCursor? = null

//    @Deprecated("Deprecated in Java")
//    @Before
//    @Throws(Exception::class)
//    override fun setUp() {
//        super.setUp()
//        _testContext = this.mockContext
//    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor() {
        val utils = ImageUtils
        TestCase.assertNotNull(utils)
    }

    // ================================================================================
    // Tests - renderBmpToCanvas
    // ================================================================================
    @Test
    fun testRenderBmpToCanvas_ValidParameters() {
        try {
            val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val rect = Rect(0, 0, 0, 0)
            val canvas = Canvas(bmp)
            renderBmpToCanvas(bmp, canvas, false)
            renderBmpToCanvas(bmp, canvas, false, rect)
            renderBmpToCanvas(bmp, canvas, false, 0, 0, 0f, 1f)
            renderBmpToCanvas(bmp, canvas, false, 0, 0, 0f, 1f, 1f)
            renderBmpToCanvas(bmp, canvas, true, 0, 0, 0f, 1f, 1f)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testRenderBmpToCanvas_NullBitmap() {
        try {
            val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val rect = Rect(0, 0, 0, 0)
            val canvas = Canvas(bmp)
            renderBmpToCanvas(null, canvas, false)
            renderBmpToCanvas(null, canvas, false, rect)
            renderBmpToCanvas(null, canvas, false, 0, 0, 0f, 1f)
            renderBmpToCanvas(null, canvas, false, 0, 0, 0f, 1f, 1f)
        } catch (e: NullPointerException) {
            TestCase.fail()
        }
    }

    @Test
    fun testRenderBmpToCanvas_NullCanvas() {
        try {
            val bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            val rect = Rect(0, 0, 0, 0)
            renderBmpToCanvas(bmp, null, false)
            renderBmpToCanvas(bmp, null, false, rect)
            renderBmpToCanvas(bmp, null, false, 0, 0, 0f, 1f)
            renderBmpToCanvas(bmp, null, false, 0, 0, 0f, 1f, 1f)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testRenderBmpToCanvas_NullRect() {
        try {
            val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            renderBmpToCanvas(bmp, canvas, false, null)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

    @Test
    fun testImageFileSupportedItemsNullClipData() {
        val items: ClipData? = null
        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, items)
        TestCase.assertFalse(ret)
    }

    @Test
    fun testImageFileSupportedValidParameters() {
        val testFiles = ClipData.newUri(SmartDeviceApp.appContext!!.contentResolver,
            getPath("Universe.jpg"),
            Uri.fromFile(File(getPath("Universe.jpg"))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath("Fairy.png")))))

        val intent = Intent()
        intent.clipData = testFiles

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, intent.clipData)
        TestCase.assertTrue(ret)
    }

    @Test
    fun testImageFileSupportedUnsupportedImage() {
        val testFiles = ClipData.newUri(SmartDeviceApp.appContext!!.contentResolver,
            getPath("Universe.jpg"),
            Uri.fromFile(File(getPath("Universe.jpg"))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath("autumn.heic")))))

        val intent = Intent()
        intent.clipData = testFiles

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, intent.clipData)
        TestCase.assertFalse(ret)
    }

    @Test
    fun testImageFileSupportedValidUri() {
        val testUri = Uri.fromFile(File(getPath("Universe.jpg")))

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, testUri)
        TestCase.assertTrue(ret)
    }

    @Test
    fun testGetBitmapFromUriValidPathLandscape() {
        val testUri = Uri.fromFile(File(getPath("Universe.jpg")))

        val ret = ImageUtils.getBitmapFromUri(SmartDeviceApp.appContext!!, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testGetBitmapFromUriValidPathPortrait() {
        val testUri = Uri.fromFile(File(getPath("Fairy.png")))

        val ret = ImageUtils.getBitmapFromUri(SmartDeviceApp.appContext!!, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredValidParam() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath("Fairy.png")))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredRotate90() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath("Universe_rotate90.jpg")))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredRotate180() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath("Universe_rotate180.jpg")))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredRotate270() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath("Universe_rotate270.jpg")))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }
}