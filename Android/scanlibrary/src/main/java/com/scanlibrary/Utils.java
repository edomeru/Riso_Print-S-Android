package com.scanlibrary;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.exifinterface.media.ExifInterface;

/**
 * Created by jhansi on 05/04/15.
 */
public class Utils {

    private Utils() {

    }

    public static Uri getUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public static Bitmap getBitmap(Context context, Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        return bitmap;
    }

    /**
        aLINK edit: add util functions to rotate image if needed (same from ImageUtils in smartdeviceapp
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

    /**
     * aLINK edit: add chrome book checker to use for chrome book specific behavior
     *
     * @brief Check if device is chrome book
     *
     * @retval true Device is a chrome book
     * @retval false Device is a tablet or phone
     */
    public static boolean isChromeBook(Context context) {
        return context.getPackageManager().hasSystemFeature(ScanConstants.CHROME_BOOK);
    }
}