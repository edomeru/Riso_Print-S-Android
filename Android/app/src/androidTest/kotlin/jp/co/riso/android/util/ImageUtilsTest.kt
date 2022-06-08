package jp.co.riso.android.util

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import jp.co.riso.android.util.ImageUtils.renderBmpToCanvas
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.Test
import java.io.File

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
    fun testImageFileSupportedItemsNullClipData() {
        val items: ClipData? = null
        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, items)
        TestCase.assertFalse(ret)
    }

    @Test
    fun testImageFileSupportedValidParameters() {
        val testFiles = ClipData.newUri(SmartDeviceApp.appContext!!.contentResolver,
            getPath(IMG_JPG),
            Uri.fromFile(File(getPath(IMG_JPG))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath(IMG_PNG)))))

        val intent = Intent()
        intent.clipData = testFiles

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, intent.clipData)
        TestCase.assertTrue(ret)
    }

    @Test
    fun testImageFileSupportedUnsupportedImage() {
        val testFiles = ClipData.newUri(SmartDeviceApp.appContext!!.contentResolver,
            getPath(IMG_JPG),
            Uri.fromFile(File(getPath(IMG_JPG))))
        testFiles.addItem(ClipData.Item(Uri.fromFile(File(getPath(IMG_HEIC)))))

        val intent = Intent()
        intent.clipData = testFiles

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, intent.clipData)
        TestCase.assertFalse(ret)
    }

    @Test
    fun testImageFileSupportedValidUri() {
        val testUri = Uri.fromFile(File(getPath(IMG_JPG)))

        val ret = ImageUtils.isImageFileSupported(SmartDeviceApp.appContext, testUri)
        TestCase.assertTrue(ret)
    }

    @Test
    fun testGetBitmapFromUriValidPathLandscape() {
        val testUri = Uri.fromFile(File(getPath(IMG_JPG)))

        val ret = ImageUtils.getBitmapFromUri(SmartDeviceApp.appContext!!, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testGetBitmapFromUriValidPathPortrait() {
        val testUri = Uri.fromFile(File(getPath(IMG_PNG)))

        val ret = ImageUtils.getBitmapFromUri(SmartDeviceApp.appContext!!, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredValidParam() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath(IMG_PNG)))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredRotate90() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath(IMG_JPG_90)))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredRotate180() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath(IMG_JPG_180)))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }

    @Test
    fun testRotateImageIfRequiredRotate270() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val testUri = Uri.fromFile(File(getPath(IMG_JPG_270)))

        val ret = ImageUtils.rotateImageIfRequired(SmartDeviceApp.appContext!!, bmp, testUri)
        TestCase.assertNotNull(ret)
    }
}