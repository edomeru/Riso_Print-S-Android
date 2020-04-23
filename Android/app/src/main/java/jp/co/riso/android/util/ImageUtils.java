/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * ImageUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import androidx.exifinterface.media.ExifInterface;

import jp.co.riso.smartdeviceapp.AppConstants;

/**
 * @class ImageUtils
 * 
 * @brief Utility class for image operations
 */
public final class ImageUtils {
    
    /**
     * @brief Render Bitmap to Canvas.
     * 
     * @param bmp Bitmap image
     * @param canvas Canvas where the image will be rendered
     * @param color Render colored image
     */
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color) {
        if(canvas == null) {
            return;
        }
        renderBmpToCanvas(bmp, canvas, color, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()));
    }
    
    /**
     * @brief Render Bitmap to Canvas.
     * 
     * @param bmp Bitmap image
     * @param canvas Canvas where the image will be rendered
     * @param color Render colored image
     * @param rect Rectangular coordinates
     */
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color, Rect rect) {
        if (bmp == null || rect == null) {
            return;
        }
        int x = rect.centerX();
        int y = rect.centerY();
        
        float scaleX = rect.width() / (float) bmp.getWidth();
        float scaleY = rect.height() / (float) bmp.getHeight();
        
        renderBmpToCanvas(bmp, canvas, color, x, y, 0, scaleX, scaleY);
    }
    
    /**
     * @brief Render Bitmap to Canvas.
     * 
     * @param bmp Bitmap image
     * @param canvas Canvas where the image will be rendered
     * @param color Render colored image
     * @param x x-coordinates
     * @param y y-coordinates
     * @param rotate Image rotation
     * @param scale Scale
     */
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color, int x, int y, float rotate, float scale) {
        renderBmpToCanvas(bmp, canvas, color, x, y, rotate, scale, scale);
    }
    
    /**
     * @brief Render Bitmap to Canvas.
     * 
     * @param bmp Bitmap image
     * @param canvas Canvas where the image will be rendered
     * @param color Render colored image
     * @param x x-coordinates
     * @param y y-coordinates
     * @param rotate Image rotation
     * @param scaleX Scale of x-axis
     * @param scaleY Scale of y-axis
     */
    public static void renderBmpToCanvas(Bitmap bmp, Canvas canvas, boolean color, int x, int y, float rotate, float scaleX, float scaleY) {
        if (bmp == null || canvas == null) {
            return;
        }
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

    /**
     * @brief Checks if all selected files are supported image files
     *
     * @param context Application context
     * @param items Selected files
     * @return true or false
     */
    public static boolean isImageFileSupported(Context context, ClipData items) {
        boolean valid = true;
        if (items == null || items.getItemCount() < 1) {
            valid = false;
        } else {
            for (int i=0;i<items.getItemCount();i+=1) {
                valid = ImageUtils.isImageFileSupported(context, items.getItemAt(i).getUri());
                if (!valid) {
                    break;
                }
            }
        }
        return valid;
    }

    /**
     * @brief Checks if image file is supported
     *
     * @param context Application context
     * @param uri File uri
     * @return true or false
     */
    public static boolean isImageFileSupported(Context context, Uri uri) {
        List<String> imageTypes = Arrays.asList(AppConstants.IMAGE_TYPES);
        String contentType = FileUtils.getMimeType(context, uri);

        return contentType != null && imageTypes.indexOf(contentType) != -1;
    }


    /**
     * @brief Gets bitmap from the given URI
     *
     * @param context Application context
     * @param uri File uri
     * @return bitmap
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException, SecurityException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        Rect rect = new Rect(0, 0, 0, 0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, rect, options);
        if (image == null) {
            options.inSampleSize = calculateInSampleSize(options);
            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor, rect, options);
        }

        parcelFileDescriptor.close();
        return image;
    }

    /**
     * @brief Obtain number of subsamples for image.
     *
     * @param options BitmapFactory options used in decoding
     *
     * @return number of subsamples.
     */
    // https://developer.android.com/topic/performance/graphics/load-bitmap#java
    private static int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

//        int reqWidth = height > width ? EditPhotoActivity.A4_WIDTH : EditPhotoActivity.A4_HEIGHT;
//        int reqHeight = height > width ? EditPhotoActivity.A4_HEIGHT : EditPhotoActivity.A4_WIDTH;

        int A4_WIDTH = 595, A4_HEIGHT = 842;
        int reqWidth = height > width ? A4_WIDTH : A4_HEIGHT;
        int reqHeight = height > width ? A4_HEIGHT : A4_WIDTH;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException, SecurityException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei = new ExifInterface(input);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        if (orientation == 0) {
            String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
            Cursor cursor = context.getContentResolver().query(selectedImage, orientationColumn, null, null, null);

            orientation = -1;
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(orientationColumn[0]);
                if (index != -1) {
                    orientation = cursor.getInt(index);
                }
            }
        }

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}