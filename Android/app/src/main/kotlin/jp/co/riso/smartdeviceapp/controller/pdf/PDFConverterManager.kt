/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PDFConverterManager.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.pdf

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import jp.co.riso.android.util.FileUtils
import jp.co.riso.android.util.ImageUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.activity
import jp.co.riso.smartdeviceapp.common.BaseTask
import jp.co.riso.smartdeviceapp.model.Pagination
import jp.co.riso.smartprint.R
import java.io.*
import java.lang.ref.WeakReference

/**
 * @class PDFConverterManager
 *
 * @brief Manager responsible for file conversion to PDF.
 */
class PDFConverterManager(
    private val _context: Context,
    pdfConverterManagerInterface: PDFConverterManagerInterface?
) {

    private var _conversionFlag: String? = null
    private var _pdfConversionTask: PDFConversionTask? = null
    private val _interfaceRef: WeakReference<PDFConverterManagerInterface?>?
    private var _uri: Uri? = null
    private var _clipData: ClipData? = null

    /**
     * @brief Obtain the destination file.
     *
     * @return Destination file.
     */
    var destFile: File? = null
        private set

    /**
     * @brief Sets the URI of image file to convert and sets conversion flag.
     *
     * @param data URI of image file.
     */
    fun setImageFile(data: Uri?) {
        _uri = data
        _conversionFlag = CONVERSION_IMAGE
    }

    /**
     * @brief Sets the ClipData of image files to convert and sets conversion flag.
     *
     * @param data ClipData of image files.
     */
    fun setImageFile(data: ClipData?) {
        _clipData = data
        _conversionFlag = CONVERSION_IMAGES
    }

    /**
     * @brief Sets the URI of text file to convert and sets conversion flag.
     *
     * @param data URI of text file.
     */
    fun setTextFile(data: Uri?) {
        _uri = data
        _conversionFlag = CONVERSION_TEXT
    }

    /**
     * @brief Converts text file to PDF.
     */
    fun convertTextToPDF(): Int {
        try {
            // check if over file size limit (5MB)
            if (FileUtils.getFileSize(_context, _uri) > AppConstants.TEXT_FILE_SIZE_LIMIT) {
                return CONVERSION_EXCEED_TEXT_FILE_SIZE_LIMIT
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        }
        val rect = Rect(0, 0, A4_WIDTH, A4_HEIGHT)
        var fileString: String?
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.color = Color.BLACK
        var tempTxtFile: File? = null
        // Get text file content
        try {
            // check if it is needed to store file in temp file
            if (isUriAuthorityAnyOf(_uri, AppConstants.TXT_URI_AUTHORITIES)) {
                tempTxtFile = copyInputStreamToTempFile(_uri)
                _uri = Uri.fromFile(tempTxtFile)
            }
            val parcelFileDescriptor = _context.contentResolver.openFileDescriptor(
                _uri!!, "r"
            )
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val inputStream = InputStreamReader(FileInputStream(fileDescriptor))
            val reader = BufferedReader(inputStream)
            if (_pdfConversionTask!!.isCancelled) {
                inputStream.close()
                reader.close()
                parcelFileDescriptor.close()
                return 0
            }
            val stringBuilder = StringBuilder()
            while (reader.readLine().also { fileString = it } != null) {
                if (_pdfConversionTask!!.isCancelled) {
                    inputStream.close()
                    reader.close()
                    parcelFileDescriptor.close()
                    return 0
                }
                stringBuilder.append(fileString).append("\n")
            }
            inputStream.close()
            reader.close()
            parcelFileDescriptor.close()
            fileString = stringBuilder.toString()
            if (_pdfConversionTask!!.isCancelled) {
                return 0
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return CONVERSION_FILE_NOT_FOUND
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return CONVERSION_FAILED
        } catch (e: Exception) {
            e.printStackTrace()
            return CONVERSION_FAILED
        } finally {
            // delete temp text file if exists
            if (tempTxtFile != null) {
                try {
                    FileUtils.delete(tempTxtFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        val document = PdfDocument()
        try {
            // create pagination for text content
            val pagination = Pagination(
                    fileString!!, rect.width() - 2 * MARGIN_SIZE,
                    rect.height() - 2 * MARGIN_SIZE, textPaint, 1F, 1F, true)
            val pages = pagination.size()
            if (_pdfConversionTask!!.isCancelled) {
                return 0
            }
            var i = 0
            while (i < pagination.size()) {

                // draws text and checks if task is cancelled
                if (!drawTextToPdf(pagination[i].toString(), rect, document, textPaint, i + 1)) {
                    return 0
                }
                _interfaceRef!!.get()!!.onNotifyProgress(i + 1, pages, true)
                i += 1
            }
            createDestFile(_context)
            if (_pdfConversionTask!!.isCancelled) {
                return 0
            }
            document.writeTo(FileOutputStream(destFile))
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return CONVERSION_FAILED
        } catch (e: Exception) {
            e.printStackTrace()
            return CONVERSION_FAILED
        } finally {
            document.close()
        }
        return CONVERSION_OK
    }

    /**
     * @brief Converts image file to PDF.
     */
    fun convertImageToPDF(): Int {
        try {
            // Check first if image is supported
            if (!ImageUtils.isImageFileSupported(_context, _uri)) {
                return CONVERSION_UNSUPPORTED
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        }
        var rect = Rect(0, 0, A4_WIDTH, A4_HEIGHT)
        var bitmap: Bitmap?
        var tempImgFile: File? = null
        try {
            // check if it is needed to store file in temp file
            if (isUriAuthorityAnyOf(_uri, AppConstants.IMG_URI_AUTHORITIES)) {
                tempImgFile = copyInputStreamToTempFile(_uri)
                _uri = Uri.fromFile(tempImgFile)
            }
            bitmap = ImageUtils.getBitmapFromUri(_context, _uri)
            bitmap = bitmap?.let { ImageUtils.rotateImageIfRequired(_context, it, _uri) }
            if (bitmap == null) {
                return CONVERSION_FAILED
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return CONVERSION_FILE_NOT_FOUND
        } catch (e: Exception) {
            e.printStackTrace()
            return CONVERSION_FAILED
        } finally {
            // delete temp image file if exists
            if (tempImgFile != null) {
                try {
                    FileUtils.delete(tempImgFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        if (_pdfConversionTask!!.isCancelled) {
            return 0
        }

        // check if image is landscape
        val width = bitmap!!.width
        val height = bitmap.height
        if (width > height) {
            rect = Rect(0, 0, A4_HEIGHT, A4_WIDTH)
        }
        _interfaceRef!!.get()!!.onNotifyProgress(1, 1, false)
        val document = PdfDocument()
        if (_pdfConversionTask!!.isCancelled) {
            return 0
        }
        drawBitmapToPage(bitmap, rect, document, 1)
        if (_pdfConversionTask!!.isCancelled) {
            return 0
        }
        try {
            createDestFile(_context)
            if (_pdfConversionTask!!.isCancelled) {
                return 0
            }
            document.writeTo(FileOutputStream(destFile))
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        } catch (e: Exception) {
            e.printStackTrace()
            return CONVERSION_FAILED
        } finally {
            document.close()
        }
        return CONVERSION_OK
    }

    /**
     * @brief Converts image files to PDF.
     */
    fun convertImagesToPDF(): Int {
        try {
            // Check first if all files are supported image files
            if (!ImageUtils.isImageFileSupported(_context, _clipData)) {
                return CONVERSION_UNSUPPORTED
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        }
        var rect = Rect(0, 0, A4_WIDTH, A4_HEIGHT)
        var bitmap: Bitmap?
        val document = PdfDocument()
        var i = 0
        while (i < _clipData!!.itemCount) {
            _interfaceRef!!.get()!!.onNotifyProgress(i + 1, _clipData!!.itemCount, false)
            var tempImgFile: File? = null
            try {
                var uri = _clipData!!.getItemAt(i).uri
                // check if it is needed to store file in temp file
                if (isUriAuthorityAnyOf(uri, AppConstants.IMG_URI_AUTHORITIES)) {
                    tempImgFile = copyInputStreamToTempFile(uri)
                    uri = Uri.fromFile(tempImgFile)
                }
                bitmap = ImageUtils.getBitmapFromUri(_context, uri)
                bitmap = bitmap?.let { ImageUtils.rotateImageIfRequired(_context, it, uri) }
                if (bitmap == null) {
                    return CONVERSION_FAILED
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                return CONVERSION_SECURITY_ERROR
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return CONVERSION_FILE_NOT_FOUND
            } catch (e: Exception) {
                e.printStackTrace()
                return CONVERSION_FAILED
            } finally {
                // delete temp image file if exists
                if (tempImgFile != null) {
                    try {
                        FileUtils.delete(tempImgFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            if (_pdfConversionTask!!.isCancelled) {
                return 0
            }

            // check first image orientation
            if (i == 0) {
                // check if image is landscape (only the first image will be considered)
                val width = bitmap!!.width
                val height = bitmap.height
                if (width > height) {
                    rect = Rect(0, 0, A4_HEIGHT, A4_WIDTH)
                }
            }
            if (_pdfConversionTask!!.isCancelled) {
                document.close()
                return 0
            }
            drawBitmapToPage(bitmap, rect, document, i + 1)
            if (_pdfConversionTask!!.isCancelled) {
                document.close()
                return 0
            }
            i += 1
        }
        try {
            createDestFile(_context)
            if (_pdfConversionTask!!.isCancelled) {
                return 0
            }
            document.writeTo(FileOutputStream(destFile))
        } catch (e: SecurityException) {
            e.printStackTrace()
            return CONVERSION_SECURITY_ERROR
        } catch (e: Exception) {
            e.printStackTrace()
            return CONVERSION_FAILED
        } finally {
            document.close()
        }
        return CONVERSION_OK
    }

    /**
     * @brief Initializes conversion task asynchronously.
     */
    fun initializeAsync() {
        if (_pdfConversionTask != null) {
            _pdfConversionTask!!.cancel(true)
        }
        _pdfConversionTask = PDFConversionTask()
        _pdfConversionTask!!.execute(_conversionFlag)
    }

    /**
     * @brief Cancel conversion task.
     */
    fun cancel() {
        if (_pdfConversionTask != null) {
            _pdfConversionTask!!.cancel(true)
        }
    }

    /**
     * @brief Creates destination file of conversion.
     *
     * @param context Application context.
     */
    private fun createDestFile(context: Context) {
        destFile = File(context.cacheDir, AppConstants.CONST_TEMP_PDF_PATH)
        if (destFile!!.exists()) {
            destFile!!.delete()
        }
    }

    /**
     * @brief Draws bitmap to PDFDocument page
     *
     * @param bitmap Bitmap to be drawn
     * @param rect Rect of PDF page
     * @param document PdfDocument
     * @param pageNumber page number of PDF
     */
    private fun drawBitmapToPage(
        bitmap: Bitmap?,
        rect: Rect,
        document: PdfDocument,
        pageNumber: Int
    ) {
        var bitmap1 = bitmap
        bitmap1 =
            getScaledBitmap(bitmap1, rect.width() - 2 * MARGIN_SIZE, rect.height() - 2 * MARGIN_SIZE)
        if (_pdfConversionTask!!.isCancelled) {
            return
        }
        val pageInfo = PageInfo.Builder(rect.width(), rect.height(), pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawPaint(paint)
        paint.color = Color.BLUE
        if (_pdfConversionTask!!.isCancelled) {
            document.finishPage(page)
            return
        }

        // center image horizontally and vertically
        val leftMargin = (rect.width() - bitmap1.width) / 2
        val topMargin = (rect.height() - bitmap1.height) / 2
        canvas.drawBitmap(bitmap1, leftMargin.toFloat(), topMargin.toFloat(), null)
        bitmap1.recycle()
        document.finishPage(page)
    }

    /**
     * @brief Draws text to PDFDocument page
     *
     * @param pageText Text to be drawn
     * @param rect Rect of PDF page
     * @param document PdfDocument
     * @param textPaint paint to be used to draw text
     * @param pageNumber page number of PDF
     *
     * @return whether task has completed drawing text
     */
    @Throws(SecurityException::class, OutOfMemoryError::class)
    private fun drawTextToPdf(
        pageText: String,
        rect: Rect,
        document: PdfDocument,
        textPaint: TextPaint,
        pageNumber: Int
    ): Boolean {
        if (_pdfConversionTask!!.isCancelled) {
            return false
        }
        val pageInfo = PageInfo.Builder(rect.width(), rect.height(), pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // get text per line
        val builder = StaticLayout.Builder.obtain(
            pageText,
            0,
            pageText.length,
            textPaint,
            rect.width() - 2 * MARGIN_SIZE
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(true)
        val staticLayout = builder.build()
        val pageString = staticLayout.text.toString()
        if (_pdfConversionTask!!.isCancelled) {
            document.finishPage(page)
            return false
        }
        // draw text per line
        var j = 0
        while (j < staticLayout.lineCount) {
            val lineStart = staticLayout.getLineStart(j)
            val endStart = staticLayout.getLineEnd(j)
            val line = pageString.substring(lineStart, endStart)
            val marginTop =
                if (j == 0) MARGIN_SIZE else MARGIN_SIZE + staticLayout.getLineBottom(j - 1)
            // Android 13 New OS Support - Fix for RM#1268, extra character newline "\n" is printed in Android 13
            canvas.drawText(line.trim(), MARGIN_SIZE.toFloat(), marginTop.toFloat(), textPaint)
            j += 1
        }
        document.finishPage(page)
        return !_pdfConversionTask!!.isCancelled
    }

    /**
     * @brief Obtain scaled bitmap maintaining aspect ratio.
     *
     * @param bitmap Image bitmap.
     * @param maxWidth Maximum image width.
     * @param maxHeight Maximum image height.
     *
     * @return Scaled bitmap.
     */
    private fun getScaledBitmap(bitmap: Bitmap?, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratioBitmap = bitmap!!.width.toFloat() / bitmap.height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, false)
    }

    /**
     * @brief Create temporary file from input stream
     *
     * @param uri URI of input stream
     *
     * @return tempFile The temporary file created from the input stream
     */
    @Throws(IOException::class)
    private fun copyInputStreamToTempFile(uri: Uri?): File {
        val tempFile = File(_context.cacheDir, AppConstants.TEMP_COPY_FILENAME)
        val input = _context.contentResolver.openInputStream(uri!!)
        FileUtils.copy(input, tempFile)
        input?.close()
        return tempFile
    }

    /**
     * @brief Checks if URI authority is equal to any of listed authorities
     *
     * @param uri URI to check
     * @param authorities List of authorities to check against (can be 1)
     *
     * @return If URI authority is equal to any of the listed authorities
     */
    private fun isUriAuthorityAnyOf(uri: Uri?, authorities: Array<String>): Boolean {
        if (uri!!.authority == null) {
            return false
        }
        for (authority in authorities) {
            if (uri.authority == authority) {
                return true
            }
        }
        return false
    }
    // ================================================================================
    // Internal classes
    // ================================================================================
    /**
     * @class PDFConversionTask
     *
     * @brief Background task which converts files to PDF.
     * The PDF is saved to the sandbox.
     */
    private inner class PDFConversionTask : BaseTask<String?, Int?>() {
        override fun doInBackground(vararg params: String?): Int {
            val conversionType = params[0]
            var status = CONVERSION_FAILED
            when (conversionType) {
                CONVERSION_TEXT -> status = convertTextToPDF()
                CONVERSION_IMAGE -> status = convertImageToPDF()
                CONVERSION_IMAGES -> status = convertImagesToPDF()
            }
            return status
        }

        override fun onPreExecute() {
            if (_interfaceRef?.get() != null) {
                if (!_pdfConversionTask!!.isCancelled) {
                    _interfaceRef.get()!!
                        .onNotifyProgress(_context.resources.getString(R.string.ids_info_msg_initializing))
                }
            }
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            val activity = activity
            activity!!.runOnUiThread {
                if (_interfaceRef?.get() != null) {
                    if (!_pdfConversionTask!!.isCancelled) {
                        _interfaceRef.get()!!.onFileConverted(result!!)
                    }
                }
            }
        }
    }

    companion object {
        const val CONVERSION_OK = 0 ///< Conversion successful
        const val CONVERSION_FAILED = -1 ///< Conversion failed
        const val CONVERSION_UNSUPPORTED = -2 ///< Unsupported file format
        const val CONVERSION_EXCEED_TEXT_FILE_SIZE_LIMIT =
            -3 ///< Text file to be converted exceeds file size limit
        const val CONVERSION_SECURITY_ERROR = -4 ///< URI Permission limitation
        const val CONVERSION_FILE_NOT_FOUND = -5 ///< Not Found, No Internet

        // Conversion Types
        const val CONVERSION_TEXT = "TEXT"
        const val CONVERSION_IMAGE = "IMAGE"
        const val CONVERSION_IMAGES = "IMAGES"

        // PostScript Point Values
        const val A4_WIDTH = 595
        const val A4_HEIGHT = 842
        const val MARGIN_SIZE = 72 ///< 1 inch
    }

    /**
     * @brief Creates a PDFConverterManager with an Interface class.
     */
    init {
        _interfaceRef = WeakReference(pdfConverterManagerInterface)
    }
}