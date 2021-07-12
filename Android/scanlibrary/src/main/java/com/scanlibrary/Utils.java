package com.scanlibrary;

// aLINK edit - Start
// MediaStore.Images.Media.insertImage(ContentResolver cr, Bitmap src, String title, String desc)
// was deprecated in API level 29. Use MediaColumns.IS_PENDING instead.
import android.content.ContentValues;
// aLINK edit - End
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
// aLINK edit - Start
// MediaStore.Images.Media.getBitmap(ContentResolver cr, Uri uri) was deprecated in API level 29.
// Use ImageDecoder.createSource(ContentResolver cr,  Uri uri) instead
import android.graphics.ImageDecoder;
// aLINK edit - End
import android.graphics.Matrix;
import android.net.Uri;
// aLINK edit - Start
// ImageDecoder.createSource(ContentResolver cr,  Uri uri) was added in API level 28
// Check the build version and use MediaStore.Images.Media.getBitmap(ContentResolver cr, Uri uri)
// for older build versions.
import android.os.Build;
// aLINK edit - End
import android.provider.MediaStore;

// aLINK edit - Start
// MediaStore.Images.Media.insertImage(ContentResolver cr, Bitmap src, String title, String desc)
// was deprecated in API level 29. Use MediaColumns.IS_PENDING instead.
import java.io.FileNotFoundException;
// aLINK edit - End
import java.io.IOException;
import java.io.InputStream;
// aLINK edit - Start
// MediaStore.Images.Media.insertImage(ContentResolver cr, Bitmap src, String title, String desc)
// was deprecated in API level 29. Use MediaColumns.IS_PENDING instead.
import java.io.OutputStream;
// aLINK edit - End

import androidx.exifinterface.media.ExifInterface;

/**
 * Created by jhansi on 05/04/15.
 */
public class Utils {

    private Utils() {

    }

    // aLINK edit - Start
    // MediaStore.Images.Media.insertImage(ContentResolver cr, Bitmap src, String title,
    // String desc) was deprecated in API level 29. Use MediaColumns.IS_PENDING instead.
    // Reference:
    // https://github.com/coroutineDispatcher/pocket_treasure/blob/master/gallery_module/src/main/java/com/sxhardha/gallery_module/image/FullImageFragment.kt
    public static Uri getUri(Context context, Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, ScanConstants.IMAGE_RELATIVE_PATH);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues);
        try {
            OutputStream stream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            uri = null;
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
            }
        }
        return uri;
    }
    // aLINK edit - End

    @SuppressWarnings("deprecation")
    public static Bitmap getBitmap(Context context, Uri uri) throws IOException {
        Bitmap bitmap;
        // aLINK edit - Start
        // MediaStore.Images.Media.getBitmap(ContentResolver cr, Uri uri) was deprecated in
        // API level 29. Use ImageDecoder.createSource(ContentResolver cr,  Uri uri) instead.
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } else {
            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
            bitmap = ImageDecoder.decodeBitmap(source);
            bitmap = bitmap.copy(ScanConstants.BITMAP_CONFIG, true);
        }
        // aLINK edit - End
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