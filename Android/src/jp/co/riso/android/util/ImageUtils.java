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
import android.graphics.Paint;
import android.graphics.Rect;

public final class ImageUtils {
    
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color) {
        renderBmpToCanvas(bmp, canvas, color, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()));
    }
    
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color, Rect rect) {
        Paint paint = new Paint();
        
        if (!color) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(filter);            
        }
        
        canvas.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()),
                rect, paint);
    }
}