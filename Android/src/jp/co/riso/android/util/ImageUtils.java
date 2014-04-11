/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * ImageUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public final class ImageUtils {
    
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color) {
        renderBmpToCanvas(bmp, canvas, color, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()));
    }
    
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color, Rect rect) {
        int x = rect.centerX();
        int y = rect.centerY();
        
        float scaleX = rect.width() / (float) bmp.getWidth();
        float scaleY = rect.height() / (float) bmp.getHeight();
        
        renderBmpToCanvas(bmp, canvas, color, x, y, 0, scaleX, scaleY);
    }
    
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color, int x, int y, float rotate, float scale) {
        renderBmpToCanvas(bmp, canvas, color, x, y, rotate, scale, scale);
    }
    
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color, int x, int y, float rotate, float scaleX, float scaleY) {
        Paint paint = new Paint();
        
        if (!color) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(filter);            
        }
        
        Matrix mtx = new Matrix();
        
        mtx.preTranslate(-(bmp.getWidth() >> 1), -(bmp.getHeight() >> 1));
        mtx.preRotate(rotate, bmp.getWidth() >> 1, bmp.getHeight() >> 1);
        mtx.postScale(scaleX, scaleY);
        mtx.postTranslate(x, y);
        
        canvas.drawBitmap(bmp, mtx, paint);
    }
}