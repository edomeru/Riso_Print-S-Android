package jp.co.riso.android.util

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import jp.co.riso.android.util.ImageUtils.renderBmpToCanvas
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.Test

class ImageUtilsTest : BaseActivityTestUtil() {
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
    fun testImageFileSupported_ItemsNullClipData() {
        val items: ClipData? = null
        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, items)
        TestCase.assertFalse(ret)
    }

    @Test
    fun testImageFileSupported_ValidParameters() {
        val testFiles = ClipData.newUri(SmartDeviceApp.appContext!!.contentResolver,
            IMG_JPG,
            getUriFromPath(IMG_JPG))
        testFiles.addItem(ClipData.Item(getUriFromPath(IMG_JPG)))

        val intent = Intent()
        intent.clipData = testFiles

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, intent.clipData)
        TestCase.assertTrue(ret)
    }

    @Test
    fun testImageFileSupported_UnsupportedImage() {
        val testFiles = ClipData.newUri(SmartDeviceApp.appContext!!.contentResolver,
            IMG_JPG,
            getUriFromPath(IMG_JPG))
        testFiles.addItem(ClipData.Item(getUriFromPath(IMG_ERR_UNSUPPORTED)))

        val intent = Intent()
        intent.clipData = testFiles

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, intent.clipData)
        TestCase.assertFalse(ret)
    }

    @Test
    fun testImageFileSupported_ValidUri() {
        val testUri = getUriFromPath(IMG_JPG)

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, testUri)
        TestCase.assertTrue(ret)
    }

    @Test
    fun testGetBitmapFromUri_ValidPathLandscape() {
        val testUri = getUriFromPath(IMG_LANDSCAPE)

        val ret = ImageUtils.getBitmapFromUri(SmartDeviceApp.appContext!!, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testGetBitmapFromUri_ValidPathPortrait() {
        val testUri = getUriFromPath(IMG_PORTRAIT)

        val ret = ImageUtils.getBitmapFromUri(SmartDeviceApp.appContext!!, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequired_ValidParam() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = getUriFromPath(IMG_PNG)

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequired_Rotate90() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = getUriFromPath(IMG_JPG_90)

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequired_Rotate180() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = getUriFromPath(IMG_JPG_180)

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequired_Rotate270() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = getUriFromPath(IMG_JPG_270)

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }
}