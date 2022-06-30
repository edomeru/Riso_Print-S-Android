/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * FileUtils.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.util

import kotlin.Throws
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.provider.OpenableColumns
import java.io.*
import java.util.*

/**
 * @class FileUtils
 *
 * @brief Utility class for file operations
 */
object FileUtils {
    /**
     * @brief Copy a file.
     *
     * @param src Source file
     * @param dst Destination file
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(src: File?, dst: File?) {
        if (src == null || dst == null) {
            return
        }
        val `in`: InputStream = FileInputStream(src)
        val out: OutputStream = FileOutputStream(dst)

        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    }

    /**
     * @brief Copy a file from inputstream.
     *
     * @param in Source inputstream
     * @param dst Destination file
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(`in`: InputStream?, dst: File?) {
        if (`in` == null || dst == null) {
            return
        }
        val out: OutputStream = FileOutputStream(dst)

        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun delete(src: File) {
        src.delete()
    }

    /**
     * @brief Determines the mime type of a file uri
     *
     * @param context Application context
     * @param uri File uri
     * @return Mime type of file
     */
    @JvmStatic
    fun getMimeType(context: Context?, uri: Uri?): String {
        var mimeType: String? = null
        if (context != null && uri != null && uri.scheme != null) {
            mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val cr = context.contentResolver
                cr.getType(uri)
            } else {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
            }
        }
        return mimeType ?: ""
    }

    /**
     * @brief Gets the filename from uri with content scheme
     *
     * @param context Application context
     * @param uri File uri
     * @param isPdfFilename flag to convert file extension to pdf
     * @return Filename
     */
    @JvmStatic
    @Throws(SecurityException::class)
    fun getFileName(context: Context?, uri: Uri?, isPdfFilename: Boolean): String? {
        var result: String? = null
        if (context != null && uri != null && uri.scheme != null) {
            if (uri.scheme == "content") {
                try {
                    context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                        // Content resolver query can return a SecurityException
                        // E.g.: When there is a PDF from image/text in PrintPreview, then suddenly user Denied Storage from Settings
                        // PrintPreviewFragment will repeat conversion using the same Intent data, but this time the permission is not granted anymore
                        if (cursor != null && cursor.moveToFirst()) {
                            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (index >= 0) {
                                result = cursor.getString(index)
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    throw SecurityException()
                }
            }
            if (result == null && uri.path != null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result!!.substring(cut + 1)
                }
            }
            if (isPdfFilename && result != null) {
                // replace file extension with .pdf
                if (!result!!.endsWith(".pdf")) {
                    val cut = result!!.lastIndexOf('.')
                    if (cut != -1) {
                        val filename = result!!.substring(0, cut)
                        result = "$filename.pdf"
                    }
                }
            }
        }
        return result
    }

    /**
     * @brief Gets the file size from uri
     *
     * @param context Application context
     * @param uri File uri
     * @return file size
     */
    @JvmStatic
    fun getFileSize(context: Context?, uri: Uri?): Int {
        var filesize = 0
        if (context != null && uri != null && uri.scheme != null) {
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (columnIndex >= 0) {
                            filesize = cursor.getInt(columnIndex)
                        } else {
                            Logger.logError(
                                FileUtils::class.java,
                                "columnName:" + OpenableColumns.SIZE + " not found"
                            )
                        }
                    }
                }
            } else if (uri.path != null) {
                val file = File(uri.path!!)
                filesize = file.length().toInt()
            }
        }
        return filesize
    }
}