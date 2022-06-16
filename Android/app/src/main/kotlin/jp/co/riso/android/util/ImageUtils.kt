/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * ImageUtils.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.util

import android.content.ClipData
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import jp.co.riso.android.util.FileUtils.getMimeType
import jp.co.riso.smartdeviceapp.AppConstants
import java.io.IOException

/**
 * @class ImageUtils
 *
 * @brief Utility class for image operations
 */
object ImageUtils {
    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842

    /**
     * @brief Render Bitmap to Canvas.
     *
     * @param bmp Bitmap image
     * @param canvas Canvas where the image will be rendered
     * @param color Render colored image
     */
    @JvmStatic
    fun renderBmpToCanvas(bmp: Bitmap?, canvas: Canvas?, color: Boolean) {
        if (canvas == null) {
            return
        }
        renderBmpToCanvas(bmp, canvas, color, Rect(0, 0, canvas.width, canvas.height))
    }

    /**
     * @brief Render Bitmap to Canvas.
     *
     * @param bmp Bitmap image
     * @param canvas Canvas where the image will be rendered
     * @param color Render colored image
     * @param rect Rectangular coordinates
     */
    @JvmStatic
    fun renderBmpToCanvas(bmp: Bitmap?, canvas: Canvas?, color: Boolean, rect: Rect?) {
        if (bmp == null || rect == null) {
            return
        }
        val x = rect.centerX()
        val y = rect.centerY()
        val scaleX = rect.width() / bmp.width.toFloat()
        val scaleY = rect.height() / bmp.height.toFloat()
        renderBmpToCanvas(bmp, canvas, color, x, y, 0f, scaleX, scaleY)
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
    @JvmStatic
    fun renderBmpToCanvas(
        bmp: Bitmap?,
        canvas: Canvas?,
        color: Boolean,
        x: Int,
        y: Int,
        rotate: Float,
        scale: Float
    ) {
        renderBmpToCanvas(bmp, canvas, color, x, y, rotate, scale, scale)
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
    @JvmStatic
    fun renderBmpToCanvas(
        bmp: Bitmap?,
        canvas: Canvas?,
        color: Boolean,
        x: Int,
        y: Int,
        rotate: Float,
        scaleX: Float,
        scaleY: Float
    ) {
        if (bmp == null || canvas == null) {
            return
        }
        val paint = Paint()
        if (!color) {
            val cm = ColorMatrix()
            cm.setSaturation(0f)
            val filter = ColorMatrixColorFilter(cm)
            paint.colorFilter = filter
        }
        val mtx = Matrix()
        mtx.preTranslate(-(bmp.width shr 1).toFloat(), -(bmp.height shr 1).toFloat())
        mtx.preRotate(rotate, (bmp.width shr 1).toFloat(), (bmp.height shr 1).toFloat())
        mtx.postScale(scaleX, scaleY)
        mtx.postTranslate(x.toFloat(), y.toFloat())
        canvas.drawBitmap(bmp, mtx, paint)
    }

    /**
     * @brief Checks if all selected files are supported image files
     *
     * @param context Application context
     * @param items Selected files
     * @return true or false
     */
    @JvmStatic
    fun isImageFileSupported(context: Context?, items: ClipData?): Boolean {
        var valid = true
        if (items == null) {
            valid = false
        } else {
            var i = 0
            while (i < items.itemCount) {
                valid = isImageFileSupported(context, items.getItemAt(i).uri)
                if (!valid) {
                    break
                }
                i += 1
            }
        }
        return valid
    }

    /**
     * @brief Checks if image file is supported
     *
     * @param context Application context
     * @param uri File uri
     * @return true or false
     */
    @JvmStatic
    fun isImageFileSupported(context: Context?, uri: Uri?): Boolean {
        val imageTypes = listOf(*AppConstants.IMAGE_TYPES)
        val contentType = getMimeType(context, uri)
        return imageTypes.contains(contentType)
    }

    /**
     * @brief Gets bitmap from the given URI
     *
     * @param context Application context
     * @param uri File uri
     * @return bitmap
     */
    @JvmStatic
    @Throws(IOException::class, SecurityException::class)
    fun getBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(
            uri!!, "r"
        )
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val rect = Rect(0, 0, 0, 0)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var image = BitmapFactory.decodeFileDescriptor(fileDescriptor, rect, options)
        if (image == null) {
            options.inSampleSize = calculateInSampleSize(options)
            options.inJustDecodeBounds = false
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor, rect, options)
        }
        parcelFileDescriptor.close()
        return image
    }

    /**
     * @brief Obtain number of subsamples for image.
     *
     * @param options BitmapFactory options used in decoding
     *
     * @return number of subsamples.
     */
    // https://developer.android.com/topic/performance/graphics/load-bitmap#java
    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        val reqWidth = if (height > width) A4_WIDTH else A4_HEIGHT
        val reqHeight = if (height > width) A4_HEIGHT else A4_WIDTH
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight
                && halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    @JvmStatic
    @Throws(IOException::class, SecurityException::class)
    fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri?): Bitmap {
        val input = context.contentResolver.openInputStream(selectedImage!!)
        val ei = ExifInterface(
            input!!
        )
        var orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        if (orientation == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val orientationColumn = arrayOf(MediaStore.Images.Media.ORIENTATION)
                val cursor = context.contentResolver.query(
                    selectedImage,
                    orientationColumn,
                    null,
                    null,
                    null
                )
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(orientationColumn[0])
                        if (index != -1) {
                            orientation = cursor.getInt(index)
                        }
                    }
                    cursor.close()
                }
            }
        }
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}