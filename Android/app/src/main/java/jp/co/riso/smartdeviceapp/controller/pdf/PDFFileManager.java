/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PDFFileManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import androidx.preference.PreferenceManager;

import com.radaee.pdf.Document;
import com.radaee.pdf.Matrix;
import com.radaee.pdf.Page;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import jp.co.riso.android.util.FileUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.BaseTask;

import android.system.ErrnoException;
import static android.system.OsConstants.ENOSPC;

/**
 * @class PDFFileManager
 * 
 * @brief Wrapper class for Radaee PDFViewer.
 * Contains operations to open/close PDF (asynchronously), get page bitmap, and get pdf information
 */
public class PDFFileManager {
    
    public static final int PDF_OK = 0; ///< Open successful
    public static final int PDF_ENCRYPTED = -1; ///< PDF is encrypted
    public static final int PDF_PRINT_RESTRICTED = -2; ///< Printing is restricted
    public static final int PDF_OPEN_FAILED = -3; ///< PDF open failed
    public static final int PDF_CANCELLED = -4; ///< PDF open was cancelled
    public static final int PDF_NOT_ENOUGH_FREE_SPACE = -5;
    
    protected static final String KEY_NEW_PDF_DATA = "new_pdf_data";
    protected static final String KEY_SANDBOX_PDF_NAME = "key_sandbox_pdf_name";
    
    private static final int RADAEE_OK = 0; ///< PDFViewer: PDF is opened successfully
    private static final int RADAEE_ENCRYPTED = -1; ///< PDFViewer: Cannot open encrypted PDF
    private static final int RADAEE_UNKNOWN_ENCRYPTION = -2; ///< PDFViewer: Unknown Encryption
    @SuppressWarnings("unused") // Radaee error (Handled on general error handling)
    private static final int RADAEE_DAMAGED = -3; ///< PDFViewer: PDF is Damaged
    @SuppressWarnings("unused") // Radaee error (Handled on general error handling)
    private static final int RADAEE_INVALID_PATH = -10; ///< PDFViewer: Invalid PDF Path
    
    /// Should keep the document closed after every access
    private static final boolean CONST_KEEP_DOCUMENT_CLOSED = false;
    
    private static final float CONST_RADAEE_DPI = 72.0f; ///< PDFViewer: resolution of the PDF
    private static final float CONST_INCHES_TO_MM = 25.4f; ///< mm per inches
    
    private volatile String mPath;
    private Document mDocument;
    private String mFileName;
    private WeakReference<PDFFileManagerInterface> mInterfaceRef;
    
    private PDFInitTask mInitTask = null;
    
    private volatile boolean mIsInitialized;
    private volatile int mPageCount;
    
    private InputStream contentInputStream = null;
    
    /**
     * @brief Creates a PDFFileManager with an Interface class
     * 
     * @param pdfFileManagerInterface Object which receives the events.
     */
    public PDFFileManager(PDFFileManagerInterface pdfFileManagerInterface) {
        mDocument = new Document();
        mInterfaceRef = new WeakReference<PDFFileManagerInterface>(pdfFileManagerInterface);
        setPDF(null);
    }
    
    /**
     * @brief Gets the current path of the PDF
     * 
     * @return Path from the file system.
     */
    public String getPath() {
        return mPath;
    }
    
    /**
     * @brief Gets the current filename of the PDF
     * 
     * @return Filename of the PDF (with extension)
     */
    public String getFileName() {
        return mFileName;
    }
    
    /**
     * @brief Sets the current filename of the PDF and saves to preferences
     *
     * @return Filename of the PDF (with extension)
     */
    public void setFileName(String filename) {
        mFileName = filename;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PDFFileManager.KEY_SANDBOX_PDF_NAME, filename);
        edit.apply();
    }

    /**
     * @brief Sets the PDF to be processed.
     * 
     * @param path Path of the PDF to be opened.
     */
    public void setPDF(String path) {
        mIsInitialized = false;
        mPageCount = 0;
        
        if (path == null) {
            mPath = null;
            mFileName = null;
            
            return;
        }
        
        File file = new File(path);
        
        mPath = path;
        mFileName = file.getName();
    }    
    
    /**
     * @brief Sets the PDF in the sandbox as the PDF to be processed.
     */
    public void setSandboxPDF() {
        setPDF(getSandboxPath());
        mFileName = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
        
        PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), true);
    }
    
    /**
     * @brief Checks if the PDF is already initialized
     * 
     * @retval true PDF manager is initialized
     * @retval true Initialization is not yet completed
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    /**
     * @brief Gets the number of pages in the PDF.
     * 
     * @return Page count
     * @retval 0 If not initialized.
     */
    public int getPageCount() {
        return mPageCount;
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
    public float getPageWidth(int pageNo) {
        if (!isInitialized()) {
            return 0;
        }
        
        if (pageNo < 0 || pageNo >= getPageCount()) {
            return 0;
        }
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(mPath, null);
        }
        
        if (!mDocument.IsOpened()) {
            mDocument.Open(mPath, null);
        }
        
        // Make sure document is opened
        float width = mDocument.GetPageWidth(pageNo) / CONST_RADAEE_DPI;
        width *= CONST_INCHES_TO_MM;
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Close();
        }
        
        return width;
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
    public float getPageHeight(int pageNo) {
        if (!isInitialized()) {
            return 0;
        }
        
        if (pageNo < 0 || pageNo >= getPageCount()) {
            return 0;
        }
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(mPath, null);
        }
        
        // Make sure document is opened
        if (!mDocument.IsOpened()) {
            mDocument.Open(mPath, null);
        }
        
        float height = mDocument.GetPageHeight(pageNo) / CONST_RADAEE_DPI;
        height *= CONST_INCHES_TO_MM;
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Close();
        }
        
        return height;
    }
    
    /**
     * @brief Checks whether the PDF loaded is landscape.
     * 
     * @note Orientation is based on the first page
     * 
     * @retval true PDF is landscape
     * @retval false PDF is portrait, square.
     */
    public boolean isPDFLandscape() {
        float width = getPageWidth(0);
        float height = getPageHeight(0);
        
        // if width == height, it is considered portrait
        return (width > height);
    }
    
    /**
     * @brief Gets the sandbox path to store the PDF
     * 
     * @return PDF path from the sandbox
     */
    public static final String getSandboxPath() {
        return SmartDeviceApp.getAppContext().getExternalFilesDir(AppConstants.CONST_PDF_DIR) + "/" + AppConstants.CONST_TEMP_PDF_PATH;
    }
    
    /**
     * @brief Checks whether a new PDF is available from open-in
     * 
     * @param context Application instance context
     * 
     * @retval true A PDF is available from open-in
     * @retval false No PDF is available from open-in
     */
    public synchronized static boolean hasNewPDFData(Context context) {
        if (context == null) {
            return false;
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(KEY_NEW_PDF_DATA, false);
    }
    
    /**
     * @brief Sets the path of a new PDF data.
     * 
     * @param context Application instance context
     * @param newData New PDF data is launched through Open-in
     */
    public static void setHasNewPDFData(Context context, boolean newData) {
        if (context == null) {
            return;
        }
        
        // Notify PDF File Data that there is a new PDF
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        
        edit.putBoolean(PDFFileManager.KEY_NEW_PDF_DATA, newData);
        if (newData) {
            edit.remove(PDFFileManager.KEY_SANDBOX_PDF_NAME);
        }
        edit.apply();
    }
    
    /**
     * @brief Clears the PDF saved in the sandbox
     * 
     * @param context Application instance context
     */
    public static void clearSandboxPDFName(Context context) {
        if (context == null) {
            return;
        }
        
        // Notify PDF File Data that there is a new PDF
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(PDFFileManager.KEY_SANDBOX_PDF_NAME);
        edit.apply();
    }
    
    /**
     * @brief Gets the filename of the PDF saved in the sandbox
     * 
     * @param context Application instance context
     * 
     * @return Filename of the PDF in the sandbox
     * @retval null If no PDF is in the sandbox
     */
    public static String getSandboxPDFName(Context context) {
        if (context == null) {
            return null;
        }
        
        // Notify PDF File Data that there is a new PDF
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_SANDBOX_PDF_NAME, null);
    }
    
    /**
     * @brief Sets the filename of the PDF saved in the sandbox
     * 
     * @param context Application instance context
     * @param fileName Filename to be saved
     */
    public static void setSandboxPDF(Context context, String fileName) {
        if (context == null) {
            return;
        }
        
        // Notify PDF File Data that there is a new PDF
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        if (fileName == null) {
            edit.remove(PDFFileManager.KEY_SANDBOX_PDF_NAME);
        } else {
            edit.putString(PDFFileManager.KEY_SANDBOX_PDF_NAME, fileName);
        }
        edit.apply();
    }
    
    /**
     * @brief Initializes the set PDF path asynchronously.
     * 
     *        Performs the following:
     *        <ol>
     *        <li>Save to file</li>
     *        <li>Test if file is open-able.</li>
     *        <li>Returns status via PDFFileManagerInterface</li>
     *        </ol>
     */
    public void initializeAsync(InputStream is) {
        mIsInitialized = false;
        mPageCount = 0;
        
        // Cancel any previous initialize task
        if (mInitTask != null) {
            mInitTask.cancel(true);
        }
        if (is != null){
            contentInputStream = is;
            this.setPDF(null);
        } else {
            contentInputStream = null;            
        }
        
        mInitTask = new PDFInitTask();
        mInitTask.execute(mPath);
        
    }
    
    /**
     * @brief Gets the page bitmap
     * 
     * @param pageNo Page Index
     * 
     * @return Bitmap of the page
     */
    public synchronized Bitmap getPageBitmap(int pageNo) {
        return getPageBitmap(pageNo, 1.0f, false, false);
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
    public synchronized Bitmap getPageBitmap(int pageNo, float scale, boolean flipX, boolean flipY) {
        if (!isInitialized()) {
            return null;
        }
        
        if (pageNo < 0 || pageNo >= getPageCount()) {
            return null;
        }
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(mPath, null);
        } // else {
            // TODO: re-check: For temporary fix of bug
            // mDocument.Close(); // This will clear the buffer
            // mDocument.Open(mSandboxPath, null);
        // }
        
        // Make sure document is opened
        if (!mDocument.IsOpened()) {
            mDocument.Open(mPath, null);
        }
        
        Page page = mDocument.GetPage(pageNo);
        
        if (page == null) {
            return null;
        }
        
        float pageWidth = mDocument.GetPageWidth(pageNo) * scale;
        float pageHeight = mDocument.GetPageHeight(pageNo) * scale;
        
        float x0 = 0;
        float y0 = pageHeight;
        
        if (flipX) {
            x0 = pageWidth;
        }
        if (flipY) {
            y0 = 0;
        }
        
        float scaleX = scale;
        float scaleY = -scale;
        
        if (flipX) {
            scaleX = -scaleX;
        }
        if (flipY) {
            scaleY = -scaleY;
        }
        
        Bitmap bitmap = Bitmap.createBitmap((int) pageWidth, (int) pageHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        
        Matrix mat = new Matrix(scaleX, scaleY, x0, y0);
        
        if (!page.RenderToBmp(bitmap, mat)) {
            bitmap.recycle();
            bitmap = null;
        }
        
        mat.Destroy();
        page.Close();
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Close();
        }
        
        return bitmap;
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
    public static Uri createTemporaryPdfFromContentUri(Context context, Uri contentUri) {
        if (contentUri != null) {
            FileOutputStream output = null;
            InputStream contentInputStream = null;

            try {
                contentInputStream = context.getContentResolver().openInputStream(contentUri);

                File file = new File(context.getCacheDir(), AppConstants.CONST_TEMP_PDF_PATH);
                if (file.exists()) {
                    file.delete();
                }

                output = new FileOutputStream(file);
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len;
                while ((len = contentInputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }

                return Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Separate try-catch to allow closing of "output" variable if contentInputStream throws an exception when closing
                try {
                    if (contentInputStream != null) {
                        contentInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return contentUri;
    }

    /**
     * Cleans up the temporary PDF file created for non-"file" intent URIs
     *
     * @param context
     */
    public static void cleanupCachedPdf(Context context) {
        File file = new File(context.getCacheDir(), AppConstants.CONST_TEMP_PDF_PATH);
        if (file.exists()) {
            file.delete();
        }
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
    protected synchronized int openDocument() {
        return openDocument(mPath);
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
    protected synchronized int openDocument(String path) {
        if (mDocument.IsOpened()) {
            closeDocument();
        }
        
        if (path == null || path.length() == 0) {
            return PDF_OPEN_FAILED;
        }
        
        int status = mDocument.Open(path, null);
        
        switch (status) {
            case RADAEE_OK:
                int permission = mDocument.GetPermission();
                // check if (permission != 0) means that license is not standard. if standard license, just display.
                if (permission != 0) {
                    if ((permission & 0x4) == 0) {
                        mDocument.Close();
                        return PDF_PRINT_RESTRICTED;
                    }
                }
                
                mPageCount = mDocument.GetPageCount();
                mIsInitialized = true;
                
                return PDF_OK;
            case RADAEE_ENCRYPTED:
            case RADAEE_UNKNOWN_ENCRYPTION:
                return PDF_ENCRYPTED;
            default:
                return PDF_OPEN_FAILED;
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
    protected synchronized int testDocument(String path) {
        if (path == null || path.length() == 0) {
            return PDF_OPEN_FAILED;
        }
        
        Document document = new Document();
        int status = document.Open(path, null);
        
        switch (status) {
            case RADAEE_OK:
                int permission = document.GetPermission();
                document.Close();
                
                // check if (permission != 0) means that license is not standard. if standard license, just display.
                if (permission != 0) {
                    if ((permission & 0x4) == 0) {
                        return PDF_PRINT_RESTRICTED;
                    }
                }
                
                return PDF_OK;
            case RADAEE_ENCRYPTED:
            case RADAEE_UNKNOWN_ENCRYPTION:
                return PDF_ENCRYPTED;
            default:
                return PDF_OPEN_FAILED;
        }
    }
    
    /**
     * @brief Closes the document
     */
    protected synchronized void closeDocument() {
        if (mDocument.IsOpened()) {
            mDocument.Close();
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
    private class PDFInitTask extends BaseTask<String, Integer> {
        
        String sandboxPath = PDFFileManager.getSandboxPath();
        String inputFile = null;
        
        @Override
        protected Integer doInBackground(String... params) {
            if (contentInputStream != null) {
                // do file copy in background
                File destFile = new File(sandboxPath);
                try {
                    FileUtils.copy(contentInputStream, destFile);
                } catch (IOException e) {
                    e.printStackTrace();

                    // RM 784 Fix: Flag "No space left in device" exception -- start
                    int errNo = ((ErrnoException)e.getCause()).errno;
                    if (errNo == ENOSPC) {
                        return PDF_NOT_ENOUGH_FREE_SPACE;
                    }
                    // RM 784 Fix -- end
                }
            }
            
            if (inputFile == null){
                inputFile = params[0];
            }
            
            int status = testDocument(inputFile);
            
            if (isCancelled() || (mPath != null && !mPath.equals(inputFile))) {
                return PDF_CANCELLED;
            }
            if (status == PDF_OK) {
                File file = new File(mPath);
                String fileName = file.getName();
                
                if (PDFFileManager.hasNewPDFData(SmartDeviceApp.getAppContext())) {
                    PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());
                    
                    try {
                        if (!mPath.equals(sandboxPath)) {
                            //check if the sandbox can still accommodate the PDF
                            File destFile = new File(sandboxPath);
                            destFile.createNewFile();
                            long srcSize = file.length();
                            long destSize = destFile.getUsableSpace() - AppConstants.CONST_FREE_SPACE_BUFFER;
                            if (srcSize > destSize){
                                return PDF_NOT_ENOUGH_FREE_SPACE;
                            } else {                            
                                FileUtils.copy(file, destFile);
                            }
                        }
                        
                        PDFFileManager.setSandboxPDF(SmartDeviceApp.getAppContext(), fileName);
                    } catch (IOException e) {
                        return PDF_OPEN_FAILED;
                    }
                }
                
                mPath = sandboxPath;
                openDocument(sandboxPath);
            }
            
            return status;
        }
        
        @Override
        protected void onPreExecute() {
            if (contentInputStream != null){
                File destFile = new File(sandboxPath);
                try {
                    destFile.createNewFile();
                    inputFile = destFile.getPath();
                    
                    setPDF(destFile.getPath());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            final Integer mResult = result;
            
            if (result != PDF_CANCELLED) {
                if (result != PDF_OK) {
                    setPDF(null);
                }

                final Activity activity = SmartDeviceApp.getActivity();
                activity.runOnUiThread((new Runnable() {
                    @Override
                    public void run() {
                        if (mInterfaceRef != null && mInterfaceRef.get() != null) {
                            mInterfaceRef.get().onFileInitialized(mResult);
                        }
                    }
               }));
            }
        }
    }
 
}
