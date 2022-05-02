/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PDFFileManager.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.system.ErrnoException
import android.system.OsConstants
import androidx.preference.PreferenceManager
import com.radaee.pdf.Document
import com.radaee.pdf.Matrix
import jp.co.riso.android.util.FileUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.common.BaseTask
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference

/**
 * @class PDFFileManager
 *
 * @brief Wrapper class for Radaee PDFViewer.
 * Contains operations to open/close PDF (asynchronously), get page bitmap, and get pdf information
 */
class PDFFileManager(pdfFileManagerInterface: PDFFileManagerInterface?) {
    /**
     * @brief Gets the current path of the PDF
     *
     * @return Path from the file system.
     */
    @Volatile
    var path: String? = null
        private set
    private val mDocument: Document = Document()
    private var mFileName: String? = null
    private val mInterfaceRef: WeakReference<PDFFileManagerInterface?>?
    private var mInitTask: PDFInitTask? = null

    /**
     * @brief Checks if the PDF is already initialized
     *
     * @retval true PDF manager is initialized
     * @retval true Initialization is not yet completed
     */
    @Volatile
    var isInitialized = false
        private set

    /**
     * @brief Gets the number of pages in the PDF.
     *
     * @return Page count
     * @retval 0 If not initialized.
     */
    @Volatile
    var pageCount = 0
        private set
    private var contentInputStream: InputStream? = null
    /**
     * @brief Gets the current filename of the PDF
     *
     * @return Filename of the PDF (with extension)
     */
    /**
     * @brief Sets the current filename of the PDF and saves to preferences
     *
     * @return Filename of the PDF (with extension)
     */
    var fileName: String?
        get() = mFileName
        set(filename) {
            mFileName = filename
            val prefs =
                PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
            val edit = prefs.edit()
            edit.putString(KEY_SANDBOX_PDF_NAME, filename)
            edit.apply()
        }

    /**
     * @brief Sets the PDF to be processed.
     *
     * @param path Path of the PDF to be opened.
     */
    fun setPDF(path: String?) {
        this.isInitialized = false
        pageCount = 0
        if (path == null) {
            this.path = null
            mFileName = null
            return
        }
        val file = File(path)
        this.path = path
        mFileName = file.name
    }

    /**
     * @brief Sets the PDF in the sandbox as the PDF to be processed.
     */
    fun setSandboxPDF() {
        setPDF(sandboxPath)
        mFileName = getSandboxPDFName(SmartDeviceApp.appContext)
        setHasNewPDFData(SmartDeviceApp.appContext, true)
    }

    /**
     * @brief Gets the width of a specific page in the PDF
     *
     * @note Radaee returns 72 dpi.
     *
     * @param pageNo Page Index
     *
     * @return Page width in mm
     */
    fun getPageWidth(pageNo: Int): Float {
        if (!isInitialized) {
            return 0F
        }
        if (pageNo < 0 || pageNo >= pageCount) {
            return 0F
        }
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(path, null)
        }
        if (!mDocument.IsOpened()) {
            mDocument.Open(path, null)
        }

        // Make sure document is opened
        var width = mDocument.GetPageWidth(pageNo) / CONST_RADAEE_DPI
        width *= CONST_INCHES_TO_MM
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Close()
        }
        return width
    }

    /**
     * @brief Gets the height of a specific page in the PDF
     *
     * @note Radaee returns 72 dpi.
     *
     * @param pageNo Page Index
     *
     * @return Page width in mm
     */
    fun getPageHeight(pageNo: Int): Float {
        if (!isInitialized) {
            return 0F
        }
        if (pageNo < 0 || pageNo >= pageCount) {
            return 0F
        }
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(path, null)
        }

        // Make sure document is opened
        if (!mDocument.IsOpened()) {
            mDocument.Open(path, null)
        }
        var height = mDocument.GetPageHeight(pageNo) / CONST_RADAEE_DPI
        height *= CONST_INCHES_TO_MM
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Close()
        }
        return height
    }// if width == height, it is considered portrait

    /**
     * @brief Checks whether the PDF loaded is landscape.
     *
     * @note Orientation is based on the first page
     *
     * @retval true PDF is landscape
     * @retval false PDF is portrait, square.
     */
    val isPDFLandscape: Boolean
        get() {
            val width = getPageWidth(0)
            val height = getPageHeight(0)

            // if width == height, it is considered portrait
            return width > height
        }

    /**
     * @brief Initializes the set PDF path asynchronously.
     *
     * Performs the following:
     *
     *  1. Save to file
     *  1. Test if file is open-able.
     *  1. Returns status via PDFFileManagerInterface
     *
     */
    fun initializeAsync(`is`: InputStream?) {
        this.isInitialized = false
        pageCount = 0

        // Cancel any previous initialize task
        if (mInitTask != null) {
            mInitTask!!.cancel(true)
        }
        if (`is` != null) {
            contentInputStream = `is`
            setPDF(null)
        } else {
            contentInputStream = null
        }
        mInitTask = PDFInitTask()
        mInitTask!!.execute(path)
    }

    /**
     * @brief Gets the page bitmap
     *
     * @param pageNo Page Index
     *
     * @return Bitmap of the page
     */
    @Synchronized
    fun getPageBitmap(pageNo: Int): Bitmap? {
        return getPageBitmap(pageNo, 1.0f, flipX = false, flipY = false)
    }

    /**
     * @brief Gets the page bitmap
     *
     * @param pageNo Page Index
     * @param scale X and Y scale of the page
     * @param flipX Flip the page horizontally
     * @param flipY Flip the page vertically
     *
     * @return Bitmap of the page
     */
    @Synchronized
    fun getPageBitmap(pageNo: Int, scale: Float, flipX: Boolean, flipY: Boolean): Bitmap? {
        if (!isInitialized) {
            return null
        }
        if (pageNo < 0 || pageNo >= pageCount) {
            return null
        }
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(path, null)
        } // else {
        // TODO: re-check: For temporary fix of bug
        // mDocument.Close(); // This will clear the buffer
        // mDocument.Open(mSandboxPath, null);
        // }

        // Make sure document is opened
        if (!mDocument.IsOpened()) {
            mDocument.Open(path, null)
        }
        val page = mDocument.GetPage(pageNo) ?: return null
        val pageWidth = mDocument.GetPageWidth(pageNo) * scale
        val pageHeight = mDocument.GetPageHeight(pageNo) * scale
        var x0 = 0f
        var y0 = pageHeight
        if (flipX) {
            x0 = pageWidth
        }
        if (flipY) {
            y0 = 0f
        }
        var scaleX = scale
        var scaleY = -scale
        if (flipX) {
            scaleX = -scaleX
        }
        if (flipY) {
            scaleY = -scaleY
        }
        var bitmap =
            Bitmap.createBitmap(pageWidth.toInt(), pageHeight.toInt(), Bitmap.Config.ARGB_8888)
        bitmap!!.eraseColor(Color.WHITE)
        val mat = Matrix(scaleX, scaleY, x0, y0)
        if (!page.RenderToBmp(bitmap, mat)) {
            bitmap.recycle()
            bitmap = null
        }
        mat.Destroy()
        page.Close()
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Close()
        }
        return bitmap
    }
    // ================================================================================
    // Protected methods
    // ================================================================================
    /**
     * @brief Opens the PDF
     *
     * @return Status of open PDF open operation
     * @retval PDF_OK(0)
     * @retval PDF_ENCRYPTED(-1)
     * @retval PDF_UNKNOWN_ENCRYPTION(-2)
     * @retval PDF_DAMAGED(-3)
     * @retval PDF_INVALID_PATH(-10)
     */
    @Synchronized
    fun openDocument(): Int {
        return openDocument(path)
    }

    /**
     * @brief Opens the PDF. Sets the status of the class to initialized
     *
     * @param path Path to the PDF to be opened
     *
     * @return Status of open document operation
     * @retval PDF_OK(0)
     * @retval PDF_ENCRYPTED(-1)
     * @retval PDF_UNKNOWN_ENCRYPTION(-2)
     * @retval PDF_DAMAGED(-3)
     * @retval PDF_INVALID_PATH(-10)
     */
    @Synchronized
    fun openDocument(path: String?): Int {
        if (mDocument.IsOpened()) {
            closeDocument()
        }
        if (path == null || path.isEmpty()) {
            return PDF_OPEN_FAILED
        }
        return when (mDocument.Open(path, null)) {
            RADAEE_OK -> {
                val permission = mDocument.GetPermission()
                // check if (permission != 0) means that license is not standard. if standard license, just display.
                if (permission != 0) {
                    if (permission and 0x4 == 0) {
                        mDocument.Close()
                        return PDF_PRINT_RESTRICTED
                    }
                }
                pageCount = mDocument.GetPageCount()
                this.isInitialized = true
                PDF_OK
            }
            RADAEE_ENCRYPTED, RADAEE_UNKNOWN_ENCRYPTION -> PDF_ENCRYPTED
            else -> PDF_OPEN_FAILED
        }
    }

    /**
     * @brief Tests whether the PDF can be opened or not.
     *
     * @param path Path to the PDF to be opened
     *
     * @return Status of open document operation
     * @retval PDF_OK(0)
     * @retval PDF_ENCRYPTED(-1)
     * @retval PDF_UNKNOWN_ENCRYPTION(-2)
     * @retval PDF_DAMAGED(-3)
     * @retval PDF_INVALID_PATH(-10)
     */
    @Synchronized
    fun testDocument(path: String?): Int {
        if (path == null || path.isEmpty()) {
            return PDF_OPEN_FAILED
        }
        val document = Document()
        return when (document.Open(path, null)) {
            RADAEE_OK -> {
                val permission = document.GetPermission()
                document.Close()

                // check if (permission != 0) means that license is not standard. if standard license, just display.
                if (permission != 0) {
                    if (permission and 0x4 == 0) {
                        return PDF_PRINT_RESTRICTED
                    }
                }
                PDF_OK
            }
            RADAEE_ENCRYPTED, RADAEE_UNKNOWN_ENCRYPTION -> PDF_ENCRYPTED
            else -> PDF_OPEN_FAILED
        }
    }

    /**
     * @brief Closes the document
     */
    @Synchronized
    fun closeDocument() {
        if (mDocument.IsOpened()) {
            mDocument.Close()
        }
    }
    // ================================================================================
    // Internal classes
    // ================================================================================
    /**
     * @class PDFInitTask
     *
     * @brief Background task which initialized the PDF.
     * The PDF is copied to the sandbox.
     */
    private inner class PDFInitTask : BaseTask<String?, Int?>() {
        var sandboxPath = PDFFileManager.sandboxPath
        var inputFile: String? = null

        override fun doInBackground(vararg params: String?): Int {
            if (contentInputStream != null) {
                // do file copy in background
                val destFile = File(sandboxPath)
                try {
                    FileUtils.copy(contentInputStream, destFile)
                } catch (e: IOException) {
                    e.printStackTrace()

                    // RM 784 Fix: Flag "No space left in device" exception -- start
                    val errNo = (e.cause as ErrnoException?)!!.errno
                    if (errNo == OsConstants.ENOSPC) {
                        return PDF_NOT_ENOUGH_FREE_SPACE
                    }
                    // RM 784 Fix -- end
                }
            }
            if (inputFile == null) {
                inputFile = params[0]
            }
            val status = testDocument(inputFile)
            if (isCancelled || path != null && path != inputFile) {
                return PDF_CANCELLED
            }
            if (status == PDF_OK) {
                val file = path?.let { File(it) }
                val fileName = file?.name
                if (hasNewPDFData(SmartDeviceApp.appContext)) {
                    clearSandboxPDFName(SmartDeviceApp.appContext)
                    try {
                        if (path != sandboxPath) {
                            //check if the sandbox can still accommodate the PDF
                            val destFile = File(sandboxPath)
                            destFile.createNewFile()
                            val srcSize = file!!.length()
                            val destSize =
                                destFile.usableSpace - AppConstants.CONST_FREE_SPACE_BUFFER
                            if (srcSize > destSize) {
                                return PDF_NOT_ENOUGH_FREE_SPACE
                            } else {
                                FileUtils.copy(file, destFile)
                            }
                        }
                        setSandboxPDF(SmartDeviceApp.appContext, fileName)
                    } catch (e: IOException) {
                        return PDF_OPEN_FAILED
                    }
                }
                path = sandboxPath
                openDocument(sandboxPath)
            }
            return status
        }

        override fun onPreExecute() {
            if (contentInputStream != null) {
                val destFile = File(sandboxPath)
                try {
                    destFile.createNewFile()
                    inputFile = destFile.path
                    setPDF(destFile.path)
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (result != PDF_CANCELLED) {
                if (result != PDF_OK) {
                    setPDF(null)
                }
                val activity = SmartDeviceApp.activity!!
                activity.runOnUiThread {
                    if (mInterfaceRef?.get() != null) {
                        mInterfaceRef.get()!!.onFileInitialized(result!!)
                    }
                }
            }
        }
    }

    companion object {
        const val PDF_OK = 0 ///< Open successful
        const val PDF_ENCRYPTED = -1 ///< PDF is encrypted
        const val PDF_PRINT_RESTRICTED = -2 ///< Printing is restricted
        const val PDF_OPEN_FAILED = -3 ///< PDF open failed
        const val PDF_CANCELLED = -4 ///< PDF open was cancelled
        const val PDF_NOT_ENOUGH_FREE_SPACE = -5
        private const val KEY_NEW_PDF_DATA = "new_pdf_data"
        const val KEY_SANDBOX_PDF_NAME = "key_sandbox_pdf_name"
        private const val RADAEE_OK = 0 ///< PDFViewer: PDF is opened successfully
        private const val RADAEE_ENCRYPTED = -1 ///< PDFViewer: Cannot open encrypted PDF
        private const val RADAEE_UNKNOWN_ENCRYPTION = -2 ///< PDFViewer: Unknown Encryption

        // Radaee error (Handled on general error handling)
        //private const val RADAEE_DAMAGED = -3 ///< PDFViewer: PDF is Damaged
        //private const val RADAEE_INVALID_PATH = -10 ///< PDFViewer: Invalid PDF Path

        /// Should keep the document closed after every access
        private const val CONST_KEEP_DOCUMENT_CLOSED = false
        private const val CONST_RADAEE_DPI = 72.0f ///< PDFViewer: resolution of the PDF
        private const val CONST_INCHES_TO_MM = 25.4f ///< mm per inches

        /**
         * @brief Gets the sandbox path to store the PDF
         *
         * @return PDF path from the sandbox
         */
        @JvmStatic
        val sandboxPath: String
            get() = SmartDeviceApp.appContext?.getExternalFilesDir(AppConstants.CONST_PDF_DIR)
                .toString() + "/" + AppConstants.CONST_TEMP_PDF_PATH

        /**
         * @brief Checks whether a new PDF is available from open-in
         *
         * @param context Application instance context
         *
         * @retval true A PDF is available from open-in
         * @retval false No PDF is available from open-in
         */
        @JvmStatic
        @Synchronized
        fun hasNewPDFData(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean(KEY_NEW_PDF_DATA, false)
        }

        /**
         * @brief Sets the path of a new PDF data.
         *
         * @param context Application instance context
         * @param newData New PDF data is launched through Open-in
         */
        @JvmStatic
        fun setHasNewPDFData(context: Context?, newData: Boolean) {
            if (context == null) {
                return
            }

            // Notify PDF File Data that there is a new PDF
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = prefs.edit()
            edit.putBoolean(KEY_NEW_PDF_DATA, newData)
            if (newData) {
                edit.remove(KEY_SANDBOX_PDF_NAME)
            }
            edit.apply()
        }

        /**
         * @brief Clears the PDF saved in the sandbox
         *
         * @param context Application instance context
         */
        @JvmStatic
        fun clearSandboxPDFName(context: Context?) {
            if (context == null) {
                return
            }

            // Notify PDF File Data that there is a new PDF
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = prefs.edit()
            edit.remove(KEY_SANDBOX_PDF_NAME)
            edit.apply()
        }

        /**
         * @brief Gets the filename of the PDF saved in the sandbox
         *
         * @param context Application instance context
         *
         * @return Filename of the PDF in the sandbox
         * @retval null If no PDF is in the sandbox
         */
        @JvmStatic
        fun getSandboxPDFName(context: Context?): String? {
            if (context == null) {
                return null
            }

            // Notify PDF File Data that there is a new PDF
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(KEY_SANDBOX_PDF_NAME, null)
        }

        /**
         * @brief Sets the filename of the PDF saved in the sandbox
         *
         * @param context Application instance context
         * @param fileName Filename to be saved
         */
        @JvmStatic
        fun setSandboxPDF(context: Context?, fileName: String?) {
            if (context == null) {
                return
            }

            // Notify PDF File Data that there is a new PDF
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = prefs.edit()
            if (fileName == null) {
                edit.remove(KEY_SANDBOX_PDF_NAME)
            } else {
                edit.putString(KEY_SANDBOX_PDF_NAME, fileName)
            }
            edit.apply()
        }

        /**
         * Creates a temporary PDF file for content URIs (this does not
         * require storage permission since it is within the app)
         *
         * This handles issues with some content URIs that only allow
         * the handler activity to access it. (such as Gmail)
         *
         * @param context
         */
        @JvmStatic
        fun createTemporaryPdfFromContentUri(context: Context, contentUri: Uri?): Uri? {
            if (contentUri != null) {
                var output: FileOutputStream? = null
                var contentInputStream: InputStream? = null
                try {
                    contentInputStream = context.contentResolver.openInputStream(contentUri)
                    val file = File(context.cacheDir, AppConstants.CONST_TEMP_PDF_PATH)
                    if (file.exists()) {
                        file.delete()
                    }
                    output = FileOutputStream(file)
                    val bufferSize = 1024
                    val buffer = ByteArray(bufferSize)
                    var len: Int
                    while (contentInputStream!!.read(buffer).also { len = it } != -1) {
                        output.write(buffer, 0, len)
                    }
                    return Uri.fromFile(file)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    // Separate try-catch to allow closing of "output" variable if contentInputStream throws an exception when closing
                    try {
                        contentInputStream?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    try {
                        output?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return contentUri
        }

        /**
         * Cleans up the temporary PDF file created for non-"file" intent URIs
         *
         * @param context
         */
        @JvmStatic
        fun cleanupCachedPdf(context: Context) {
            val file = File(context.cacheDir, AppConstants.CONST_TEMP_PDF_PATH)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    /**
     * @brief Creates a PDFFileManager with an Interface class
     */
    init {
        mInterfaceRef = WeakReference(pdfFileManagerInterface)
        setPDF(null)
    }
}