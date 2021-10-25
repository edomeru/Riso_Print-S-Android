/*
 * Copyright (c) 2018 RISO, Inc. All rights reserved.
 *
 * PDFConverterManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import jp.co.riso.android.util.FileUtils;
import jp.co.riso.android.util.ImageUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.BaseTask;
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
    public static final int CONVERSION_FILE_NOT_FOUND = -5; ///< Not Found, No Internet

    // Conversion Types
    public static final String CONVERSION_TEXT = "TEXT";
    public static final String CONVERSION_IMAGE = "IMAGE";
    public static final String CONVERSION_IMAGES = "IMAGES";

    // PostScript Point Values
    private final int A4_WIDTH = 595;
    private final int A4_HEIGHT = 842;
    private final int MARGIN_SIZE = 72; ///< 1 inch

    private String mConversionFlag = null;
    private PDFConversionTask mPdfConversionTask = null;

    private final WeakReference<PDFConverterManagerInterface> mInterfaceRef;

    private Uri mUri = null;
    private ClipData mClipData = null;
    private File mDestFile = null;
    private final Context mContext;

    /**
     * @brief Creates a PDFConverterManager with an Interface class.
     *
     * @param context Application context.
     * @param pdfConverterManagerInterface Object which receives the events.
     */
    public PDFConverterManager(Context context, PDFConverterManagerInterface pdfConverterManagerInterface) {
        this.mContext = context;
        mInterfaceRef = new WeakReference<>(pdfConverterManagerInterface);
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
     * @brief Sets the ClipData of image files to convert and sets conversion flag.
     *
     * @param data ClipData of image files.
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
        String fileString;

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);

        File tempTxtFile = null;
        // Get text file content
        try {
            // check if it is needed to store file in temp file
            if (isUriAuthorityAnyOf(mUri, AppConstants.TXT_URI_AUTHORITIES)) {
                tempTxtFile = copyInputStreamToTempFile(mUri);
                mUri = Uri.fromFile(tempTxtFile);
            }

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
                stringBuilder.append(fileString).append("\n");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return CONVERSION_FILE_NOT_FOUND;
        }  catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return CONVERSION_FAILED;
        } finally {
            // delete temp text file if exists
            if (tempTxtFile != null) {
                try {
                    FileUtils.delete(tempTxtFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        try {
            // Check first if image is supported
            if (!ImageUtils.isImageFileSupported(mContext, mUri)) {
                return CONVERSION_UNSUPPORTED;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        }

        Rect rect = new Rect(0, 0, A4_WIDTH, A4_HEIGHT);
        Bitmap bitmap;
        File tempImgFile = null;
        try {
            // check if it is needed to store file in temp file
            if (isUriAuthorityAnyOf(mUri, AppConstants.IMG_URI_AUTHORITIES)) {
                tempImgFile = copyInputStreamToTempFile(mUri);
                mUri = Uri.fromFile(tempImgFile);
            }
            bitmap = ImageUtils.getBitmapFromUri(mContext, mUri);
            bitmap = ImageUtils.rotateImageIfRequired(mContext, bitmap, mUri);
            if (bitmap == null) {
                return CONVERSION_FAILED;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return CONVERSION_SECURITY_ERROR;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return CONVERSION_FILE_NOT_FOUND;
        }  catch (Exception e) {
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
        Bitmap bitmap;

        PdfDocument document = new PdfDocument();
        for (int i=0;i<mClipData.getItemCount();i+=1) {
            mInterfaceRef.get().onNotifyProgress(i+1, mClipData.getItemCount(), false);

            File tempImgFile = null;
            try {
                Uri uri = mClipData.getItemAt(i).getUri();
                // check if it is needed to store file in temp file
                if (isUriAuthorityAnyOf(uri, AppConstants.IMG_URI_AUTHORITIES)) {
                    tempImgFile = copyInputStreamToTempFile(uri);
                    uri = Uri.fromFile(tempImgFile);
                }
                bitmap = ImageUtils.getBitmapFromUri(mContext, uri);
                bitmap = ImageUtils.rotateImageIfRequired(mContext, bitmap, uri);
                if (bitmap == null) {
                    return CONVERSION_FAILED;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                return CONVERSION_SECURITY_ERROR;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return CONVERSION_FILE_NOT_FOUND;
            }  catch (Exception e) {
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
        StaticLayout.Builder builder = StaticLayout.Builder.obtain(pageText, 0, pageText.length(), textPaint, rect.width() - 2 * MARGIN_SIZE)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1, 1)
            .setIncludePad(true);
        StaticLayout staticLayout = builder.build();

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

    /**
     * @brief Create temporary file from input stream
     *
     * @param uri URI of input stream
     *
     * @return tempFile The temporary file created from the input stream
     */
    private File copyInputStreamToTempFile(Uri uri) throws IOException {
        File tempFile = new File(mContext.getCacheDir(), AppConstants.TEMP_COPY_FILENAME);
        InputStream input = mContext.getContentResolver().openInputStream(uri);
        FileUtils.copy(input, tempFile);
        if (input != null) {
            input.close();
        }
        return tempFile;
    }

    /**
     * @brief Checks if URI authority is equal to any of listed authorities
     *
     * @param uri URI to check
     * @param authorities List of authorities to check against (can be 1)
     *
     * @return If URI authority is equal to any of the listed authorities
     */
    private boolean isUriAuthorityAnyOf(Uri uri, String[] authorities) {
        if (uri.getAuthority() == null) {
            return false;
        }
        for (String authority : authorities) {
            if (uri.getAuthority().equals(authority)) {
                return true;
            }
        }
        return false;
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
    private class PDFConversionTask extends BaseTask<String, Integer> {

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
            if (mInterfaceRef != null && mInterfaceRef.get() != null ) {
                if (!mPdfConversionTask.isCancelled()) {
                    mInterfaceRef.get().onNotifyProgress(mContext.getResources().getString(R.string.ids_info_msg_initializing));
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            final Integer mResult = result;
            final Activity activity = SmartDeviceApp.getActivity();

            activity.runOnUiThread((new Runnable() {
                @Override
                public void run() {
                    if (mInterfaceRef != null && mInterfaceRef.get() != null ) {
                        if (!mPdfConversionTask.isCancelled()) {
                            mInterfaceRef.get().onFileConverted(mResult);
                        }
                    }
                }
            }));
        }
    }
}
