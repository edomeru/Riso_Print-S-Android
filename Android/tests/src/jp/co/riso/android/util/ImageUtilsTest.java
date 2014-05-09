
package jp.co.riso.android.util;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.test.ActivityInstrumentationTestCase2;

public class ImageUtilsTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public ImageUtilsTest() {
        super(MainActivity.class);
    }

    public ImageUtilsTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================

    public void testConstructor() {
        ImageUtils utils = new ImageUtils();
        assertNotNull(utils);
    }

    // ================================================================================
    // Tests - renderBmpToCanvas
    // ================================================================================

    public void testRenderBmpToCanvas_ValidParameters() {
        try {
            Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

            Rect rect = new Rect(0, 0, 0, 0);
            Canvas canvas = new Canvas(bmp);
            ImageUtils.renderBmpToCanvas(bmp, canvas, false);
            ImageUtils.renderBmpToCanvas(bmp, canvas, false, rect);
            ImageUtils.renderBmpToCanvas(bmp, canvas, false, 0, 0, 0, 1);
            ImageUtils.renderBmpToCanvas(bmp, canvas, false, 0, 0, 0, 1, 1);
        } catch (Exception e) {
            fail();
        }
    }

    public void testRenderBmpToCanvas_NullBitmap() {
        try {
            Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

            Rect rect = new Rect(0, 0, 0, 0);
            Canvas canvas = new Canvas(bmp);
            ImageUtils.renderBmpToCanvas(null, canvas, false);
            ImageUtils.renderBmpToCanvas(null, canvas, false, rect);
            ImageUtils.renderBmpToCanvas(null, canvas, false, 0, 0, 0, 1);
            ImageUtils.renderBmpToCanvas(null, canvas, false, 0, 0, 0, 1, 1);
        } catch (NullPointerException e) {
            fail();
        }
    }

    public void testRenderBmpToCanvas_NullCanvas() {
        try {
            Bitmap bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);

            Rect rect = new Rect(0, 0, 0, 0);
            ImageUtils.renderBmpToCanvas(bmp, null, false);
            ImageUtils.renderBmpToCanvas(bmp, null, false, rect);
            ImageUtils.renderBmpToCanvas(bmp, null, false, 0, 0, 0, 1);
            ImageUtils.renderBmpToCanvas(bmp, null, false, 0, 0, 0, 1, 1);
        } catch (Exception e) {
            fail();
        }
    }
    
    public void testRenderBmpToCanvas_NullRect() {
        try {
            Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            ImageUtils.renderBmpToCanvas(bmp, canvas, false, null);
        } catch (Exception e) {
            fail();
        }
    }
}
