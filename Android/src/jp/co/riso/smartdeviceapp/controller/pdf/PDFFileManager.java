/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PDFFileManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import jp.co.riso.android.util.FileUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.radaee.pdf.Document;
import com.radaee.pdf.Matrix;
import com.radaee.pdf.Page;

public class PDFFileManager {
    public static final String TAG = "PDFFileManager";
    
    public static final String KEY_NEW_PDF_DATA = "new_pdf_data";
    public static final String KEY_SANDBOX_PDF_NAME = "key_sandbox_pdf_name";
    
    public static final int PDF_OK = 0;
    public static final int PDF_ENCRYPTED = -1;
    public static final int PDF_PRINT_RESTRICTED = -2;
    public static final int PDF_OPEN_FAILED = -3;
    
    private static final int RADAEE_OK = 0;
    private static final int RADAEE_ENCRYPTED = -1;
    private static final int RADAEE_UNKNOWN_ENCRYPTION = -2;
    @SuppressWarnings("unused") // Radaee error with general error handling
    private static final int RADAEE_DAMAGED = -3;
    @SuppressWarnings("unused") // Radaee error with general error handling
    private static final int RADAEE_INVALID_PATH = -10;
    
    // For design consideration
    private static final boolean CONST_KEEP_DOCUMENT_CLOSED = false;
    
    private static final float CONST_RADAEE_DPI = 72.0f;
    private static final float CONST_INCHES_TO_MM = 25.4f;
    
    private Document mDocument;
    private String mPath;
    private String mFileName;
    private WeakReference<PDFFileManagerInterface> mInterfaceRef;
    
    private PDFInitTask mInitTask = null;
    
    private volatile boolean mIsInitialized;
    private volatile int mPageCount;
    
    /**
     * Constructor
     * 
     * @param pdfFileManagerInterface
     *            Object to send callbacks
     */
    public PDFFileManager(PDFFileManagerInterface pdfFileManagerInterface) {
        mDocument = new Document();
        mInterfaceRef = new WeakReference<PDFFileManagerInterface>(pdfFileManagerInterface);
        setPDF(null);
    }
    
    /**
     * Gets the current path of the pdf
     * 
     * @return Path from the file system.
     */
    public String getPath() {
        return mPath;
    }
    
    /**
     * Gets the current filename of the pdf
     * 
     * @return Filename of the PDF.
     */
    public String getFileName() {
        return mFileName;
    }
    
    /**
     * Sets the path of the PDF
     * 
     * @param path
     *            New path value
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
    
    public void setSandboxPDF() {
        setPDF(getSandboxPath());
        mFileName = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
        
        PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), false);
    }
    
    /**
     * Checks if PDF path is initialized
     * 
     * @return PDF path is initialized
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    /**
     * Gets the number of pages in the PDF
     * 
     * @return page count
     */
    public int getPageCount() {
        return mPageCount;
    }
    
    /**
     * Gets the page width
     * 
     * @param pageNo
     *            Page Index
     * 
     * @return page width in mm
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
        
        if (!mDocument.is_opened()) {
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
     * Gets the page height
     * 
     * @param pageNo
     *            Page Index
     * 
     * @return page height in mm
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
        if (!mDocument.is_opened()) {
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
     * Appends the sandbox path
     * 
     * @return PDF sandbox directory
     */
    public static final String getSandboxPath() {
        return SmartDeviceApp.getAppContext().getExternalFilesDir(AppConstants.CONST_PDF_DIR) 
                + "/" + AppConstants.CONST_TEMP_PDF_PATH;
    }
    
    /**
     * Checks whether a new PDF is available through open-in
     * 
     * @param context
     *            Application instance context
     * @return A new PDF is available
     */
    public static boolean hasNewPDFData(Context context) {
        if (context == null) {
            return false;
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(KEY_NEW_PDF_DATA, false);
    }
    
    /**
     * @param context
     *            Application instance context
     * @param newData
     *            New value for has PDF Flag
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
    
    public static String getSandboxPDFName(Context context) {
        if (context == null) {
            return null;
        }
        
        // Notify PDF File Data that there is a new PDF
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_SANDBOX_PDF_NAME, null);
    }
    
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
     * Initializes the set PDF path asynchronously. <br>
     * Performs the following: <br>
     * 1. Save to file <br>
     * 2. Test if file is openable. <br>
     * 3. Returns status via PDFFileManagerInterface
     */
    public void initializeAsync() {
        mIsInitialized = false;
        mPageCount = 0;
        
        // Cancel any previous initialize task
        if (mInitTask != null) {
            mInitTask.cancel(true);
        }
        
        mInitTask = new PDFInitTask();
        mInitTask.execute();
    }
    
    /**
     * Gets the Bitmap of the page
     * 
     * @param pageNo
     *            PDF page of the requested page
     * 
     * @return Bitmap of the page
     */
    public synchronized Bitmap getPageBitmap(int pageNo) {
        return getPageBitmap(pageNo, 1.0f, false, false);
    }
    
    /**
     * Gets the Bitmap of the page
     * 
     * @param pageNo
     *            PDF page of the requested page
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
        } else {
            // TODO: re-check: For temporary fix of bug
            // mDocument.Close(); // This will clear the buffer
            // mDocument.Open(mSandboxPath, null);
        }

        // Make sure document is opened
        if (!mDocument.is_opened()) {
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
    
    // ================================================================================
    // Protected methods
    // ================================================================================

    /**
     * Opens the document
     * 
     * @return status of open <br>
     *         PDF_OK = 0; <br>
     *         PDF_ENCRYPTED = -1; <br>
     *         PDF_UNKNOWN_ENCRYPTION = -2; <br>
     *         PDF_DAMAGED = -3; <br>
     *         PDF_INVALID_PATH = -10;
     */
    protected synchronized int openDocument() {
        return openDocument(mPath);
    }
    
    /**
     * Opens the document
     * 
     * @param path
     *            Opens the document. Sets the status of the class to initialized
     * 
     * @return status of open <br>
     *         PDF_OK = 0; <br>
     *         PDF_ENCRYPTED = -1; <br>
     *         PDF_UNKNOWN_ENCRYPTION = -2; <br>
     *         PDF_DAMAGED = -3; <br>
     *         PDF_INVALID_PATH = -10;
     */
    protected synchronized int openDocument(String path) {
        if (mDocument.is_opened()) {
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
     * Checks whether the document can be opened
     * 
     * @param path
     *            Path of the document to be opened
     * 
     * @return status of open <br>
     *         PDF_OK = 0; <br>
     *         PDF_ENCRYPTED = -1; <br>
     *         PDF_UNKNOWN_ENCRYPTION = -2; <br>
     *         PDF_DAMAGED = -3; <br>
     *         PDF_INVALID_PATH = -10;
     */
    protected synchronized int testDocument(String path) {
        if (path == null || path.length() == 0) {
            return PDF_OPEN_FAILED;
        }

        Document document = new Document();
        int status = document.Open(path, null);
        
        switch (status) {
            case RADAEE_OK:
                int permission = mDocument.GetPermission();
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
     * Closes the document
     */
    protected synchronized void closeDocument() {
        if (mDocument.is_opened()) {
            mDocument.Close();
        }
    }
    
    // ================================================================================
    // Internal classes
    // ================================================================================
    
    class PDFInitTask extends AsyncTask<Void, Void, Integer> {
        
        /**
         * Save the document in sandbox and test if openable
         */
        @Override
        protected Integer doInBackground(Void... params) {
            int status = testDocument(mPath);
            
            if (status == PDF_OK) {
                File file = new File(mPath);
                String fileName = file.getName();
                String sandboxPath = PDFFileManager.getSandboxPath();
                
                if (PDFFileManager.hasNewPDFData(SmartDeviceApp.getAppContext())) {
                    PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), false);
                    
                    try {
                        if (!mPath.equals(sandboxPath)) {
                            FileUtils.copy(file, new File(sandboxPath));
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
        
        /**
         * Delegate the result
         */
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            
            if (mInterfaceRef != null && mInterfaceRef.get() != null) {
                mInterfaceRef.get().onFileInitialized(result);
            }
        }
    }
}
