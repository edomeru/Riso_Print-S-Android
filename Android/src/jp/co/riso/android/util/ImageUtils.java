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
import android.graphics.Paint;
import android.graphics.Rect;

public final class ImageUtils {
    
    public static void renderBmpToBmp(Bitmap src, Bitmap dest) {
        renderBmpToBmp(src, dest, new Rect(0, 0, src.getWidth(), src.getHeight()),
                new Rect(0, 0, dest.getWidth(), dest.getHeight()));
    }
    
    public static void renderBmpToBmp(Bitmap src, Bitmap dest, Rect srcRect, Rect destRect) {
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();

        canvas.drawBitmap(src, new Rect(0, 0, src.getWidth(), src.getHeight()),
                new Rect(0, 0, dest.getWidth(), dest.getHeight()), paint);
    }
}