/*
 * Copyright (c) 2014 All rights reserved.
 *
 * ImageUtil.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.android.util.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

public class ImageUtil {
    
    private ImageUtil() {
        // Avoid initialization
    }
    
    public static Bitmap flipImage(Bitmap bitmap) {
        Matrix flipMatrix = new Matrix();
        flipMatrix.preScale(-1.0f, 1.0f);
        Bitmap flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), flipMatrix, false);
        bitmap = flippedBitmap.copy(Bitmap.Config.ARGB_8888, true);
        flippedBitmap.recycle();
        return bitmap;
    }
    
    public static void applyGrayscale(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
    
    public static void drawBorder(Bitmap bitmap, int color, int thickness) {
        Canvas canvas = new Canvas(bitmap);
        
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(thickness);
        paint.setStyle(Paint.Style.STROKE);
        
        int edgeAdjust = thickness / 2;
        canvas.drawRect(edgeAdjust, edgeAdjust, bitmap.getWidth() - edgeAdjust, bitmap.getHeight() - edgeAdjust, paint);
    }
}
