/*
 * Copyright (c) 2018 RISO, Inc. All rights reserved.
 *
 * PDFConverterManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import jp.co.riso.android.util.FileUtils;
import jp.co.riso.android.util.ImageUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.model.Pagination;
import jp.co.riso.smartprint.R;

/**
 * @class PDFConverterManager
 *
 * @brief Manager responsible for file conversion to PDF.
 */
public class PDFConverterManager {

    public static final int CONVERSION_OK = 0; ///< Conversion successful
    public static final int CONVERSION_FAILED = -1; ///< Conversion failed
    public static final int CONVERSION_UNSUPPORTED = -2; ///< Unsupported file format
    public static final int CONVERSION_EXCEED_TEXT_FILE_SIZE_LIMIT = -3; ///< Text file to be converted exceeds file size limit
    public static final int CONVERSION_SECURITY_ERROR = -4; ///< URI Permission limitation

    // Conversion Types
    public static final String CONVERSION_TEXT = "TEXT";
    public static final String CONVERSION_IMAGE = "IMAGE";
    public static final String CONVERSION_IMAGES = "IMAGES";

    // PostScript Point Values
    private int A4_WIDTH = 595;
    private int A4_HEIGHT = 842;
    private int MARGIN_SIZE = 72; ///< 1 inch

    private String mConversionFlag = null;
    private PDFConversionTask mPdfConversionTask = null;

    private WeakReference<PDFConverterManagerInterface> mInterfaceRef;

    private Uri mUri = null;
    private ClipData mClipData = null;
    private File mDestFile = null;
    private Context mContext;

    /**
     * @brief Creates a PDFConverterManager with an Interface class.
     *
     * @param context Application context.
     * @param pdfConverterManagerInterface Object which receives the events.
     */
    public PDFConverterManager(Context context, PDFConverterManagerInterface pdfConverterManagerInterface) {
        this.mContext = context;
        mInterfaceRef = new WeakReference<PDFConverterManagerInterface>(pdfConverterManagerInterface);
    }

    /**
     * @brief Sets the URI of image file to convert and sets conversion flag.
     *
     * @param data URI of image file.
     */
    public void setImageFile(Uri data) {
        mUri = data;
        this.mConversionFlag = CONVERSION_IMAGE;
    }

    /**
     * @brief Sets the Clipdata of image files to convert and sets conversion flag.
     *
     * @param data Clipdata of image files.
     */
    public void setImageFile(ClipData data) {
        mClipData = data;
        this.mConversionFlag = CONVERSION_IMAGES;
    }

    /**
     * @brief Sets the URI of text file to convert and sets conversion flag.
     *
     * @param data URI of text file.
     */
    public void setTextFile(Uri data) {
        mUri = data;
        this.mConversionFlag = CONVERSION_TEXT;
    }

    /**
     * @brief Converts text file to PDF.
     */
    public int convertTextToPDF() {
        try {
            // check if over file size limit (5MB)
            if (FileUtils.getFileSize(mContext, mUri) > AppConstants.TEXT_FILE_SIZE_LIMIT) {
                return CONVERSION_EXCEED_TEXT_FILE_SIZE_LIMIT;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        }

        Rect rect = new Rect(0, 0, A4_WIDTH, A4_HEIGHT);
        String fileString = "";

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);

        // Get text file content
        try {
            ParcelFileDescriptor parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(mUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            InputStreamReader inputStream = new InputStreamReader(new FileInputStream(fileDescriptor));
            BufferedReader reader = new BufferedReader(inputStream);

            if (mPdfConversionTask.isCancelled()) {
                inputStream.close();
                reader.close();
                parcelFileDescriptor.close();
                return 0;
            }

            StringBuilder stringBuilder = new StringBuilder();
            while((fileString = reader.readLine()) != null) {
                if (mPdfConversionTask.isCancelled()) {
                    inputStream.close();
                    reader.close();
                    parcelFileDescriptor.close();
                    return 0;
                }
                stringBuilder.append(fileString + "\n");
            }
            inputStream.close();
            reader.close();
            parcelFileDescriptor.close();
            fileString = stringBuilder.toString();

            if (mPdfConversionTask.isCancelled()) {
                return 0;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        }  catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return CONVERSION_FAILED;
        }

        PdfDocument document = new PdfDocument();

        try {
            // create pagination for text content
            Pagination pagination = new Pagination(fileString, rect.width() - 2 * MARGIN_SIZE,
                    rect.height() - 2 * MARGIN_SIZE, textPaint, 1, 1, true);
            int pages = pagination.size();

            if (mPdfConversionTask.isCancelled()) {
                return 0;
            }

            for (int i = 0; i < pagination.size(); i += 1) {
                // draws text and checks if task is cancelled
                if (!drawTextToPdf(pagination.get(i).toString(), rect, document, textPaint, i+1)) {
                    return 0;
                }

                mInterfaceRef.get().onNotifyProgress(i+1, pages, true);
            }

            this.createDestFile(mContext);

            if (mPdfConversionTask.isCancelled()) {
                return 0;
            }

            document.writeTo(new FileOutputStream(getDestFile()));
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return CONVERSION_FAILED;
        } finally {
            document.close();
        }

        return CONVERSION_OK;
    }

    /**
     * @brief Converts image file to PDF.
     */
    public int convertImageToPDF() {
        Rect rect = new Rect(0, 0, A4_WIDTH, A4_HEIGHT);
        Bitmap bitmap = null;
        File tempImgFile = null;
        try {
            // check if file from photo picker google drive (account-specified)
            // save a copy of image file
//            if (mUri.getAuthority() != null && mUri.getAuthority().equals(AppConstants.GOOGLE_DRIVE_URI_AUTHORITY)) {
//                tempImgFile = new File(mContext.getCacheDir(), AppConstants.TEMP_IMG_FILENAME);
//                InputStream input = mContext.getContentResolver().openInputStream(mUri);
//                FileUtils.copy(input, tempImgFile);
//                if (input != null) {
//                    input.close();
//                }
//                mUri = Uri.fromFile(tempImgFile);
//            }
            bitmap = ImageUtils.getBitmapFromUri(mContext, mUri);
            bitmap = ImageUtils.rotateImageIfRequired(mContext, bitmap, mUri);
            if (bitmap == null) {
                return CONVERSION_FAILED;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return CONVERSION_FAILED;
        } finally {
            // delete temp image file if exists
            if (tempImgFile != null) {
                try {
                    FileUtils.delete(tempImgFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mPdfConversionTask.isCancelled()) {
            return 0;
        }

        // check if image is landscape
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > height) {
            rect = new Rect(0, 0, A4_HEIGHT, A4_WIDTH);
        }

        mInterfaceRef.get().onNotifyProgress(1, 1, false);
        PdfDocument document = new PdfDocument();

        if (mPdfConversionTask.isCancelled()) {
            return 0;
        }

        drawBitmapToPage(bitmap, rect, document, 1);

        if (mPdfConversionTask.isCancelled()) {
            return 0;
        }

        try {
            this.createDestFile(mContext);

            if (mPdfConversionTask.isCancelled()) {
                return 0;
            }

            document.writeTo(new FileOutputStream(getDestFile()));
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return CONVERSION_FAILED;
        } finally {
            document.close();
        }
        return CONVERSION_OK;
    }

    /**
     * @brief Converts image files to PDF.
     */
    public int convertImagesToPDF() {
        try {
            // Check first if all files are supported image files
            if (!ImageUtils.isImageFileSupported(mContext, mClipData)) {
                return CONVERSION_UNSUPPORTED;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        }

        Rect rect = new Rect(0, 0, A4_WIDTH, A4_HEIGHT);
        Bitmap bitmap = null;

        PdfDocument document = new PdfDocument();
        for (int i=0;i<mClipData.getItemCount();i+=1) {
            mInterfaceRef.get().onNotifyProgress(i+1, mClipData.getItemCount(), false);

            File tempImgFile = null;
            try {
                Uri uri = mClipData.getItemAt(i).getUri();
//                // check if file from photo picker google drive (account-specified)
//                // save a copy of image file
//                if (uri.getAuthority() != null && uri.getAuthority().equals(AppConstants.GOOGLE_DRIVE_URI_AUTHORITY)) {
//                    tempImgFile = new File(mContext.getCacheDir(), AppConstants.TEMP_IMG_FILENAME);
//                    InputStream input = mContext.getContentResolver().openInputStream(uri);
//                    FileUtils.copy(input, tempImgFile);
//                    if (input != null) {
//                        input.close();
//                    }
//                    uri = Uri.fromFile(tempImgFile);
//                }
                bitmap = ImageUtils.getBitmapFromUri(mContext, uri);
                bitmap = ImageUtils.rotateImageIfRequired(mContext, bitmap, uri);
                if (bitmap == null) {
                    return CONVERSION_FAILED;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                return CONVERSION_SECURITY_ERROR;
            } catch (Exception e) {
                e.printStackTrace();
                return CONVERSION_FAILED;
            } finally {
                // delete temp image file if exists
                if (tempImgFile != null) {
                    try {
                        FileUtils.delete(tempImgFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (mPdfConversionTask.isCancelled()) {
                return 0;
            }

            // check first image orientation
            if (i == 0) {
                // check if image is landscape (only the first image will be considered)
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width > height) {
                    rect = new Rect(0, 0, A4_HEIGHT, A4_WIDTH);
                }
            }
            if (mPdfConversionTask.isCancelled()) {
                document.close();
                return 0;
            }

            drawBitmapToPage(bitmap, rect, document, i+1);

            if (mPdfConversionTask.isCancelled()) {
                document.close();
                return 0;
            }
        }

        try {
            this.createDestFile(mContext);

            if (mPdfConversionTask.isCancelled()) {
                return 0;
            }

            document.writeTo(new FileOutputStream(getDestFile()));
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return CONVERSION_FAILED;
        } finally {
            document.close();
        }
        return CONVERSION_OK;
    }

    /**
     * @brief Initializes conversion task asynchronously.
     */
    public void initializeAsync() {
        if (mPdfConversionTask != null) {
            mPdfConversionTask.cancel(true);
        }
        mPdfConversionTask = new PDFConversionTask();
        mPdfConversionTask.execute(mConversionFlag);
    }

    /**
     * @brief Cancel conversion task.
     */
    public void cancel() {
        if (mPdfConversionTask != null) {
            mPdfConversionTask.cancel(true);
        }
    }

    /**
     * @brief Creates destination file of conversion.
     *
     * @param context Application context.
     */
    private void createDestFile(Context context) {
        mDestFile = new File(context.getCacheDir(), AppConstants.CONST_TEMP_PDF_PATH);
        if (mDestFile.exists()) {
            mDestFile.delete();
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
    private void drawBitmapToPage(Bitmap bitmap, Rect rect, PdfDocument document, int pageNumber) {
        bitmap = getScaledBitmap(bitmap, rect.width() - 2 * MARGIN_SIZE, rect.height() - 2 * MARGIN_SIZE);

        if (mPdfConversionTask.isCancelled()) {
            return;
        }

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(rect.width(), rect.height(), pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLUE);

        if (mPdfConversionTask.isCancelled()) {
            document.finishPage(page);
            return;
        }

        // center image horizontally and vertically
        int leftMargin = (rect.width() - bitmap.getWidth()) / 2;
        int topMargin = (rect.height() - bitmap.getHeight()) / 2;
        canvas.drawBitmap(bitmap, leftMargin, topMargin, null);
        bitmap.recycle();
        bitmap = null;

        document.finishPage(page);
    }

    /**
     * @brief Obtain the destination file.
     *
     * @return Destination file.
     */
    public File getDestFile() {
        return mDestFile;
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
    private boolean drawTextToPdf(String pageText, Rect rect, PdfDocument document, TextPaint textPaint, int pageNumber) throws SecurityException, OutOfMemoryError {
        if (mPdfConversionTask.isCancelled()) {
            return false;
        }

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(rect.width(), rect.height(), pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // get text per line
        StaticLayout staticLayout = new StaticLayout(pageText, textPaint,
                rect.width() - 2 * MARGIN_SIZE, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);

        String pageString = staticLayout.getText().toString();

        if (mPdfConversionTask.isCancelled()) {
            document.finishPage(page);
            return false;
        }
        // draw text per line
        for (int j = 0; j < staticLayout.getLineCount(); j += 1) {
            int lineStart = staticLayout.getLineStart(j);
            int endStart = staticLayout.getLineEnd(j);
            String line = pageString.substring(lineStart, endStart);
            int marginTop = j == 0 ? MARGIN_SIZE : MARGIN_SIZE + staticLayout.getLineBottom(j - 1);
            canvas.drawText(line, MARGIN_SIZE, marginTop, textPaint);
        }
        document.finishPage(page);

        return !mPdfConversionTask.isCancelled();
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
    private Bitmap getScaledBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        float ratioBitmap = (float) bitmap.getWidth() / (float) bitmap.getHeight();
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, false);
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
    private class PDFConversionTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String conversionType = params[0];
            int status = CONVERSION_FAILED;

            switch (conversionType) {
                case CONVERSION_TEXT:
                    status = convertTextToPDF();
                    break;
                case CONVERSION_IMAGE:
                    status = convertImageToPDF();
                    break;
                case CONVERSION_IMAGES:
                    status = convertImagesToPDF();
                    break;
            }

            return status;
        }

        @Override
        protected void onPreExecute() {
            mInterfaceRef.get().onNotifyProgress(mContext.getResources().getString(R.string.ids_info_msg_initializing));
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (mInterfaceRef != null && mInterfaceRef.get() != null) {
                mInterfaceRef.get().onFileConverted(result);
            }
        }
    }
}
