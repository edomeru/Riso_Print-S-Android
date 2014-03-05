/*
 * Copyright (c) 2014 All rights reserved.
 *
 * AppUtils.java
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
    
    public static void renderBmpToBmp(Bitmap src, Bitmap dest, boolean color) {
        renderBmpToBmp(src, dest, color, new Rect(0, 0, dest.getWidth(), dest.getHeight()));
    }
    
    public static void renderBmpToBmp(Bitmap src, Bitmap dest, boolean color, Rect destRect) {
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        
        if (!color) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(filter);            
        }
        
        canvas.drawBitmap(src, new Rect(0, 0, src.getWidth(), src.getHeight()),
                destRect, paint);
    }
}