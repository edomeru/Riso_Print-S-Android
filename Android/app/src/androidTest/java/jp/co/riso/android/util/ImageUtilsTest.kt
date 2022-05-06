package jp.co.riso.android.util

import jp.co.riso.android.util.ImageUtils.renderBmpToCanvas
import android.test.ActivityInstrumentationTestCase2
import jp.co.riso.smartdeviceapp.view.MainActivity
import kotlin.Throws
import jp.co.riso.android.util.ImageUtils
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import junit.framework.TestCase
import java.lang.Exception
import java.lang.NullPointerException

class ImageUtilsTest : ActivityInstrumentationTestCase2<MainActivity> {
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
        val utils = ImageUtils
        TestCase.assertNotNull(utils)
    }

    // ================================================================================
    // Tests - renderBmpToCanvas
    // ================================================================================
    fun testRenderBmpToCanvas_ValidParameters() {
        try {
            val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val rect = Rect(0, 0, 0, 0)
            val canvas = Canvas(bmp)
            renderBmpToCanvas(bmp, canvas, false)
            renderBmpToCanvas(bmp, canvas, false, rect)
            renderBmpToCanvas(bmp, canvas, false, 0, 0, 0f, 1f)
            renderBmpToCanvas(bmp, canvas, false, 0, 0, 0f, 1f, 1f)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }

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

    fun testRenderBmpToCanvas_NullRect() {
        try {
            val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            renderBmpToCanvas(bmp, canvas, false, null)
        } catch (e: Exception) {
            TestCase.fail()
        }
    }
}