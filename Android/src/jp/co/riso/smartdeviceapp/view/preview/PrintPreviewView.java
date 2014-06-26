/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintPreviewView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.preview;

import java.lang.ref.WeakReference;
import java.util.Locale;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.ImageUtils;
import jp.co.riso.android.util.Logger;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletLayout;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ColorMode;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Orientation;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.FrameLayout;
import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;

/**
 * @class PrintPreviewView
 * 
 * @brief A View that displays a PDF. Uses CurlView to present the PDF.
 */
public class PrintPreviewView extends FrameLayout implements OnScaleGestureListener, OnGestureListener, OnDoubleTapListener {
    
    private static final float DEFAULT_MARGIN_IN_MM = 0;
    
    private static final float CREASE_SIZE = 1.0f;
    private static final float CREASE_DARK_LENGTH = 8.0f;
    private static final float CREASE_WHITE_LENGTH = 4.0f;
    
    /*
    private static final float PUNCH_DIAMETER_IN_MM = 8;
    private static final float PUNCH_POS_SIDE_IN_MM = 8;
    
    private static final float STAPLE_LENGTH_IN_MM = 12;
    
    private static final float STAPLE_POS_CORNER_IN_MM = 6;
    private static final float STAPLE_POS_SIDE_IN_MM = 4;
    */
    
    /// Base and minimum zoom level of the preview
    public static final float BASE_ZOOM_LEVEL = 1.0f;
    /// Maximum zoom level of the preview
    public static final float MAX_ZOOM_LEVEL = 4.0f;
    
    private static final int INVALID_IDX = -1;
    
    private static final int SLEEP_DELAY = 100;
    private static final int SMALL_BMP_SIZE = 64;
    private static final Bitmap.Config BMP_CONFIG_TEXTURE = Config.RGB_565;
    
    private CurlView mCurlView = null;
    private PDFFileManager mPdfManager = null;
    private PDFPageProvider mPdfPageProvider = new PDFPageProvider();
    private PrintSettings mPrintSettings = null;
    private LruCache<String, Bitmap> mBmpCache = null;
    
    /*
    private Bitmap mStapleBmp = null;
    private Bitmap mPunchBmp = null;
    */
    
    private float mMarginLeft = 0;
    private float mMarginRight = 0;
    private float mMarginTop = 0;
    private float mMarginBottom = 0;
    
    /*
    private boolean mShow3Punch = false;
    */
    
    private PreviewControlsListener mListener = null;
    
    // Zoom/pan related variables
    
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mDoubleTapDetector;
    private float mZoomLevel = BASE_ZOOM_LEVEL;
    
    private int mPtrIdx = INVALID_IDX;
    private PointF mPtrDownPos = new PointF();
    private PointF mPtrLastPos = new PointF();
    
    /**
     * @brief Constructs a new PrintPreviewView with a Context object
     * 
     * @param context A Context object used to access application assets 
     */
    public PrintPreviewView(Context context) {
        this(context, null);
    }
    
    /**
     * @brief Constructs a new PrintPreviewView with layout parameters.
     * 
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent 
     */
    public PrintPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * @brief Constructs a new PrintPreviewView with layout parameters and a default style.
     * 
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent 
     * @param defStyle The default style resource ID  
     */
    public PrintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * @brief Initializes the resources needed by the PrintPreviewView
     */
    @SuppressLint("NewApi") // Prevent quick scale on kitkat devices
    public void init() {
        if (!isInEditMode()) {
            mPrintSettings = new PrintSettings(); // Should not be null
            mScaleDetector = new ScaleGestureDetector(getContext(), this);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mScaleDetector.setQuickScaleEnabled(false);
            }
            mDoubleTapDetector = new GestureDetector(getContext(), this);
            mDoubleTapDetector.setOnDoubleTapListener(this);
            
            initializeCurlView();
            loadResources();
        }
    }

    /**
     * @brief Inform the curl view that the activity is paused. 
     */
    public void onPause() {
        mCurlView.onPause();
    }

    /**
     * @brief Inform the curl view that the activity is resumed. 
     */
    public void onResume() {
        mCurlView.onResume();
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        if (!isInEditMode()) {
            // Re-adjust the margin of the CurlView
            fitCurlView(l, t, r, b);
        }
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /**
     * @brief Process panning events when PrintPreviewView is zoomed.
     *
     * @param ev The event to be processed
     * 
     * @retval true Event was processed
     * @retval false Event was not processed
     */
    public boolean processTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() == 1) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mPtrIdx == INVALID_IDX) {
                        if (mCurlView.isViewHit(ev.getX(), ev.getY())) {
                            mPtrIdx = ev.getActionIndex();
                            mPtrDownPos.set(ev.getX(), ev.getY());
                            mPtrLastPos.set(ev.getX(), ev.getY());
                            return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mPtrIdx == ev.getActionIndex()) {
                        mCurlView.adjustPan(ev.getX() - mPtrLastPos.x, ev.getY() - mPtrLastPos.y);
                        mCurlView.requestRender();
                        
                        setPans(mCurlView.getPanX(), mCurlView.getPanY());
                        
                        mPtrLastPos.set(ev.getX(), ev.getY());
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mPtrIdx = -1;
                    return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        
        if (!mScaleDetector.isInProgress() && ev.getPointerCount() == 1) {
            if (mZoomLevel == BASE_ZOOM_LEVEL) {
                return mCurlView.dispatchTouchEvent(ev);
            } else {
                if (mDoubleTapDetector.onTouchEvent(ev)) {
                    return true;
                }
                return processTouchEvent(ev);
            }
        }
        
        MotionEvent e = MotionEvent.obtain( SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), 
                MotionEvent.ACTION_CANCEL, 
                ev.getX(), ev.getY(), 0);
        
        processTouchEvent(e);
        mCurlView.dispatchTouchEvent(e);
        
        e.recycle();
        
        return true;
    }
    
    // ================================================================================
    // Public methods
    // ================================================================================

    /**
     * @brief Load Bitmap resources such as staple and punch
     */
    public void loadResources() {
        /*
        mStapleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_staple);
        mPunchBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_punch);
        */
    }

    /**
     * @brief Free the native object associated with the Bitmap loaded by method loadResources()
     */
    public void freeResources() {
        /*
        mStapleBmp.recycle();
        mPunchBmp.recycle();
        */
    }

    /**
     * @brief Request that the curl view renderer render a frame.
     */
    public void refreshView() {
        mCurlView.requestRender(); 
    }

    /**
     * @brief Sets the PDF Manager to be used by the view
     * 
     * @param pdfManager PDF Manager instance
     */
    public void setPdfManager(PDFFileManager pdfManager) {
        mPdfManager = pdfManager;
    }

    /**
     * @brief Print Settings to be used by the View.
     * 
     * @param printSettings Print settings to be applied
     */
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = printSettings;

        reconfigureCurlView();
        displayValidPage();
        mCurlView.requestLayout();
    }
    
    /**
     * @brief Sets the Bitmap Cache to be used to store PDF page bitmaps
     * 
     * @param bmpCache BMP cache with allocated memory
     */
    public void setBmpCache(LruCache<String, Bitmap> bmpCache) {
        mBmpCache = bmpCache;
    }
    
    /**
     * @brief Sets the default margins. Default is no margin
     */
    public void setDefaultMargins() {
        setMarginLeftInMm(DEFAULT_MARGIN_IN_MM);
        setMarginRightInMm(DEFAULT_MARGIN_IN_MM);
        setMarginTopInMm(DEFAULT_MARGIN_IN_MM);
        setMarginBottomInMm(DEFAULT_MARGIN_IN_MM);
    }
    
    /**
     * @brief Sets the left margin
     * 
     * @param marginLeft Left margin in mm
     */
    public void setMarginLeftInMm(float marginLeft) {
        mMarginLeft = marginLeft;
    }
    
    /**
     * @brief Sets the right margin
     * 
     * @param marginRight Right margin in mm
     */
    public void setMarginRightInMm(float marginRight) {
        mMarginRight = marginRight;
    }
    
    /**
     * @brief Sets the top margin
     * 
     * @param marginTop Top margin in mm
     */
    public void setMarginTopInMm(float marginTop) {
        mMarginTop = marginTop;
    }

    /**
     * @brief Sets the bottom margin
     * 
     * @param marginBottom Bottom margin in mm
     */
    public void setMarginBottomInMm(float marginBottom) {
        mMarginBottom = marginBottom;
    }

    /*
     * @brief Set the print preview to show 3 punch instead of 4 punch
     * 
     * @param show3Punch Should show 3 holes or not
     */
    // public void setShow3Punch(boolean show3Punch) {
    //     mShow3Punch = show3Punch;
    // }
    
    /**
     * @brief Set the Preview Controls Listener
     * 
     * @param listener Preview Controls listener
     */
    public void setListener(PreviewControlsListener listener) {
        mListener = listener;
    }
    
    /**
     * @brief Checks if last page curl is allowed
     * 
     * @retval true Last page curl is allowed
     * @retval false Last page curl is not allowed
     */
    public boolean getAllowLastPageCurl() {
        return mCurlView.getAllowLastPageCurl();
    }
    
    /**
     * @brief Gets the current page displayed.
     * 
     * @return Current CurlView index
     */
    public int getCurrentPage() {
        return mCurlView.getCurrentIndex();
    }
    
    /**
     * @brief Sets the current page displayed.
     * 
     * @param page New CurlView index
     */
    public void setCurrentPage(int page) {
        mCurlView.setCurrentIndex(page);
    }
    
    /**
     * @brief Get the face count. Face count depends on the PDF
     * page count and pagination.
     * 
     * @note If booklet or duplex, dependent on faces per paper
     * 
     * @return Number of faces
     */
    public int getFaceCount() {
        if (mPdfManager == null) {
            return 0;
        }
        
        // will depend on PDF and pagination, always false for now
        int count = mPdfManager.getPageCount();
        count = (int) Math.ceil(count / (double) getPagesPerSheet());

        if (isTwoPageDisplayed()) {
            count = AppUtils.getNextIntegerMultiple(count, getFacesPerPaper());
            
            if (mPrintSettings.isBooklet()) {
                count = AppUtils.getNextIntegerMultiple(count, getFacesPerPaper());
            }
        }
        
        return count;
    }
    
    /**
     * @brief Get the number of pages. Dependent on the face count and CurlView display.
     * 
     * @return Page count
     */
    public int getPageCount() {
        int count = PrintPreviewView.this.getFaceCount();
        if (isTwoPageDisplayed()) {
            count = (int) Math.ceil(count / 2.0f);
        }
        return count;
    }
    
    /**
     * @brief Gets the string that shows the current page.
     * 
     * @return Current page string
     */
    public String getPageString() {
        return getPageString(getCurrentPage());
    }
    
    /**
     * @brief Gets the string of the supplied current face.
     * 
     * @param currentFace Current Face to get the string from
     * 
     * @return Current page string
     */
    public String getPageString(int currentFace) {
        if (isTwoPageDisplayed()) {
            currentFace *= 2;
        }
        if (mCurlView.getViewMode() == CurlView.SHOW_ONE_PAGE || currentFace == 0)  {
            currentFace++;
        }
        
        int faceCount = getFaceCount();

        final String FORMAT_ONE_PAGE_STATUS = getResources().getString(R.string.ids_lbl_page_displayed);
        return String.format(Locale.getDefault(), FORMAT_ONE_PAGE_STATUS, currentFace, faceCount);
    }
    
    /**
     * @brief Checks if zoom is base zoom
     * 
     * @retval true Zoom level is equal to BASE_ZOOM_LEVEL
     * @retval false Zoom level is not equal to BASE_ZOOM_LEVEL
     */
    public boolean isBaseZoom() {
        return mZoomLevel == BASE_ZOOM_LEVEL;
    }
    
    // ================================================================================
    // Protected methods
    // ================================================================================
    
    /**
     * @brief Get curl view dimensions
     * 
     * @param screenWidth Screen width
     * @param screenHeight Screen height
     * 
     * @return CurlView width and height
     */
    protected int[] getCurlViewDimensions(int screenWidth, int screenHeight) {
        // Compute margins based on the paper size in preview settings.
        float paperDisplaySize[] = getPaperDisplaySize();
        
        // Adjust display on screen.
        if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
            if (mCurlView.getBindPosition() == CurlView.BIND_TOP
                    || mCurlView.getBindPosition() == CurlView.BIND_BOTTOM) {
                // double the height if bind == top
                paperDisplaySize[1] *= 2.0f;
            } else {
                // double the width if bind != top
                paperDisplaySize[0] *= 2.0f;
            }
        }
        
        return AppUtils.getFitToAspectRatioSize(paperDisplaySize[0], paperDisplaySize[1], screenWidth, screenHeight);
    }
    
    /**
     * @brief Get paper dimensions
     * 
     * @param screenWidth Screen width
     * @param screenHeight Screen height
     * 
     * @return Paper width and height
     */
    protected int[] getPaperDimensions(int screenWidth, int screenHeight) {
        // Compute margins based on the paper size in preview settings.
        float paperDisplaySize[] = getPaperDisplaySize();
        
        return AppUtils.getFitToAspectRatioSize(paperDisplaySize[0], paperDisplaySize[1], screenWidth, screenHeight);
    }
    
    /**
     * @brief Get the cache key of the page bitmap for the BMPCache.
     * The cache key is dependent on the print settings that has effect
     * on the preview.
     * 
     * @param index Sheet index
     * @param side Front or back
     * 
     * @return Cache String
     */
    protected String getCacheKey(int index, int side) {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(mPdfManager.getPath());
        buffer.append(index);
        buffer.append(side);
        
        buffer.append(shouldDisplayColor());
        buffer.append(mPrintSettings.isScaleToFit());
        if (!AppConstants.USE_PDF_ORIENTATION) {
            buffer.append(mPrintSettings.getOrientation().ordinal());
        }
        buffer.append(mPrintSettings.getPaperSize().ordinal());
        
        buffer.append(mPrintSettings.getDuplex().ordinal());
        buffer.append(mPrintSettings.getImposition().ordinal());
        buffer.append(mPrintSettings.getImpositionOrder().ordinal());
        
        buffer.append(mPrintSettings.getFinishingSide().ordinal());
        /*
        buffer.append(mPrintSettings.getStaple().ordinal());
        buffer.append(mPrintSettings.getPunch().getCount(mShow3Punch));
        */
        
        buffer.append(mPrintSettings.isBooklet());
        buffer.append(mPrintSettings.getBookletFinish().ordinal());
        buffer.append(mPrintSettings.getBookletLayout().ordinal());
        
        return buffer.toString();
    }
    
    /**
     * @brief Get the bitmaps of the page from the Bitmap
     * 
     * @note Will not return a null array, only the Bitmaps will be null.
     * 
     * @param index Sheet index
     * 
     * @return Bitmaps from cache
     */
    protected Bitmap[] getBitmapsFromCacheForPage(int index) {
        Bitmap front = null;
        Bitmap back = null;
        
        if (mBmpCache != null) {
            front = mBmpCache.get(getCacheKey(index, CurlPage.SIDE_FRONT));
            back = mBmpCache.get(getCacheKey(index, CurlPage.SIDE_BACK));
        }
        
        if (front != null && front.isRecycled()) {
            front = null;
        }
        
        if (back != null && back.isRecycled()) {
            back = null;
        }
        
        return new Bitmap[] { front, back };
    }
    
    /**
     * @brief Convert the dimension (in mm) to pixel size. The conversion will be dependent
     * on the Bitmap dimensions.
     * 
     * @param dimension Unit of measure in mm.
     * @param bmpWidth Paper width in pixels
     * @param bmpHeight Paper height in pixels.
     * 
     * @return Dimension in pixels.
     */
    protected int convertDimension(float dimension, int bmpWidth, int bmpHeight) {
        float smallerDimension = Math.min(bmpWidth, bmpHeight);
        float[] paperSize = getPaperDisplaySize();
        float smallerPaperSize = Math.min(paperSize[0], paperSize[1]);
        
        return (int)((dimension / smallerPaperSize) * smallerDimension);
    }
    
    // ================================================================================
    // Preview Related functions
    // ================================================================================
    
    /**
     * @brief Check if should display landscape
     * 
     * @retval true Should display landscape
     * @retval false Should display portrait
     */
    private boolean shouldDisplayLandscape() {
        boolean flipToLandscape = false;
        
        if (AppConstants.USE_PDF_ORIENTATION) {
            flipToLandscape = mPdfManager.isPDFLandscape();
        } else {
            flipToLandscape = (mPrintSettings.getOrientation() == Orientation.LANDSCAPE);
        }
        
        if (!mPrintSettings.isBooklet()) {
            if (mPrintSettings.getImposition().isFlipLandscape()) {
                flipToLandscape = !flipToLandscape;
            }
        }
        
        return flipToLandscape;
    }
    
    /**
     * @brief Get the paper size. Dependent on the printsettings.
     * 
     * @return Paper display width and height
     */
    private float[] getPaperDisplaySize() {
        float width = mPrintSettings.getPaperSize().getWidth();
        float height = mPrintSettings.getPaperSize().getHeight();

        if (mPrintSettings.isBooklet()) {
            width = mPrintSettings.getPaperSize().getHeight() / 2;
            height = mPrintSettings.getPaperSize().getWidth();
        } 

        if (shouldDisplayLandscape()) {
            return (new float[] {height, width});
        }

        return (new float[] {width, height});
    }
    
    /**
     * @brief Gets the number of pages displayed in one sheet.
     * 
     * @return Pages per sheet
     */
    public int getPagesPerSheet() {
        if (mPrintSettings.isBooklet()) {
            return 1;
        }
        
        return mPrintSettings.getImposition().getPerPage();
    }
    
    /**
     * @brief Gets the number of faces in one paper.
     * 
     * @note If booklet, 4 faces can be displayed in one paper.
     * 
     * @return Number of faces on a paper.
     */
    public int getFacesPerPaper() {
        if (mPrintSettings.isBooklet()) {
            return 4;
        }
        
        return 2;
    }
    
    /**
     * @brief Gets the number of columns in one sheet.
     * 
     * @return Number of columns
     */
    public int getColsPerSheet() {
        if (mPrintSettings.isBooklet()) {
            return 1;
        }
        
        if (AppConstants.USE_PDF_ORIENTATION) {
            if (mPdfManager.isPDFLandscape()) {
                return mPrintSettings.getImposition().getRows();
            }
        } else {
            if (mPrintSettings.getOrientation() == Orientation.LANDSCAPE) {
                return mPrintSettings.getImposition().getRows();
            }
        }
        
        return mPrintSettings.getImposition().getCols();
    }
    
    /**
     * @brief Gets the number of rows in one sheet.
     * 
     * @return Number of rows
     */
    public int getRowsPerSheet() {
        if (mPrintSettings.isBooklet()) {
            return 1;
        }

        if (AppConstants.USE_PDF_ORIENTATION) {
            if (mPdfManager.isPDFLandscape()) {
                return mPrintSettings.getImposition().getCols();
            }
        } else {
            if (mPrintSettings.getOrientation() == Orientation.LANDSCAPE) {
                return mPrintSettings.getImposition().getCols();
            }
        }
        
        return mPrintSettings.getImposition().getRows();
    }
    
    /**
     * @brief Sets the display mode of the CurlView.
     */
    private void setupCurlPageView() {
        boolean twoPage = false;
        boolean allowLastPageCurl = false;
        
        if (getFaceCount() > 1 && isTwoPageDisplayed()) {
            twoPage = true;
            if (getFaceCount() % 2 == 0) {
                allowLastPageCurl = true;
            }
        }
        
        if (twoPage) {
            mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
            mCurlView.setRenderLeftPage(true);
        } else {
            mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
            mCurlView.setRenderLeftPage(false);
        }
        
        mCurlView.setAllowLastPageCurl(allowLastPageCurl);
    }
    
    /**
     * @brief Check if two page display is enabled
     * 
     * @retval true Two page display is enabled
     * @retval false Two page display is disabled
     */
    private boolean isTwoPageDisplayed() {
        if (mPrintSettings.isBooklet()) {
            return true;
        }
        
        return (mPrintSettings.getDuplex() != Duplex.OFF);
    }
    
    /**
     * @brief Sets the bind position of the CurlView
     */
    private void setupCurlBind() {
        int bindPosition = CurlView.BIND_LEFT;
        
        if (mPrintSettings.isBooklet()) {
            if (!shouldDisplayLandscape()) {
                bindPosition = CurlView.BIND_LEFT;
                
                if (mPrintSettings.getBookletLayout() == BookletLayout.REVERSE) {
                    bindPosition = CurlView.BIND_RIGHT;
                }
            } else {
                bindPosition = CurlView.BIND_TOP;
                
                if (mPrintSettings.getBookletLayout() == BookletLayout.REVERSE) {
                    bindPosition = CurlView.BIND_BOTTOM;
                }
            }
        } else {
            switch (mPrintSettings.getFinishingSide()) {
                case LEFT:
                    bindPosition = CurlView.BIND_LEFT;
                    break;
                case RIGHT:
                    bindPosition = CurlView.BIND_RIGHT;
                    break;
                case TOP:
                    bindPosition = CurlView.BIND_TOP;
                    break;
            }
        }
        
        mCurlView.setBindPosition(bindPosition);
    }
    
    /**
     * @brief Check if colored display is enabled
     * 
     * @retval true Colored
     * @retval false Grayscale
     */
    private boolean shouldDisplayColor() {
        return (mPrintSettings.getColorMode() != ColorMode.MONOCHROME);
    }

    /**
     * @brief Check if display vertical flip is enabled
     * 
     * @retval true Flipped vertically
     * @retval false Not flipped
     */
    private boolean isVerticalFlip() {
        boolean verticalFlip = true;
        
        if (mPrintSettings.isBooklet()) {
            verticalFlip = shouldDisplayLandscape();
        } else if (shouldDisplayLandscape()) {
            verticalFlip = (mPrintSettings.getDuplex() == Duplex.LONG_EDGE);
        } else {
            verticalFlip = (mPrintSettings.getDuplex() == Duplex.SHORT_EDGE);
        }
        
        return verticalFlip;
    }
    
    // ================================================================================
    // View-related methods
    // ================================================================================
    
    /**
     * @brief Initializes the CurlView display
     */
    private void initializeCurlView() {
        mCurlView = new CurlView(getContext());
        mCurlView.setMargins(0, 0, 0, 0);
        mCurlView.setPageProvider(mPdfPageProvider);
        mCurlView.setBindPosition(CurlView.BIND_LEFT);
        mCurlView.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
        
        reconfigureCurlView();
        
        if (!isInEditMode()) {
            float percentage = getResources().getFraction(R.dimen.preview_view_drop_shadow_percentage, 1, 1);
            mCurlView.setDropShadowSize(percentage);
        }
        
        addView(mCurlView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
    
    /**
     * @brief Re-configures the CurlView settings
     */
    public void reconfigureCurlView() {
        setupCurlPageView();
        setupCurlBind();
        setDefaultMargins();
    }
    
    /**
     * @brief Display the correct page to prevent out of bounds.
     */
    public void displayValidPage() {
        if (mPdfPageProvider.getPageCount() > 0) {
            if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
                if (getCurrentPage() > mPdfPageProvider.getPageCount()) {
                    setCurrentPage(0);
                }
            } else {
                if (getCurrentPage() + 1 > mPdfPageProvider.getPageCount()) {
                    setCurrentPage(0);
                }
            }
        }
    }
    
    /**
     * @brief Layout the curl view to fit screen
     * 
     * @param l Left bounds
     * @param t Top bounds
     * @param r Right bounds
     * @param b Bottom bounds
     */
    private void fitCurlView(int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        
        int marginSize = getResources().getDimensionPixelSize(R.dimen.preview_view_margin);
        int pageControlSize = 0;
        
        if (mListener != null) {
            pageControlSize = mListener.getControlsHeight();
        }

        int newDimensions[] = getCurlViewDimensions(w - (marginSize * 2), h - (marginSize * 2) - pageControlSize);
        
        float lrMargin = ((w - newDimensions[0]) / (w * 2.0f));
        float tbMargin = (((h - pageControlSize) - newDimensions[1]) / (h * 2.0f));
        
        lrMargin += (marginSize / (float) w);
        tbMargin += (marginSize / (float) h);

        mCurlView.setMargins(lrMargin, tbMargin, lrMargin, tbMargin + (pageControlSize / (float) h));
    }
    
    /**
     * @brief Set the current panning of the Preview
     * 
     * @param panX Horizontal Pan
     * @param panY Vertical Pan
     */
    public void setPans(float panX, float panY) {
        if (mListener != null) {
            mListener.panChanged(panX, panY);
        }
        
        mCurlView.setPans(panX, panY);
        mCurlView.requestRender();
    }
    
    /**
     * @brief Set the zoom level of the Preview
     * 
     * @param zoomLevel New zoom level.
     */
    public void setZoomLevel(float zoomLevel) {
        mZoomLevel  = zoomLevel;
        if (mZoomLevel <= BASE_ZOOM_LEVEL) {
            mZoomLevel = BASE_ZOOM_LEVEL;
        }
        if (mZoomLevel >= MAX_ZOOM_LEVEL) {
            mZoomLevel = MAX_ZOOM_LEVEL;
        }
        
        if (mListener != null) {
            mListener.zoomLevelChanged(mZoomLevel);
            mListener.setControlsEnabled(isBaseZoom());
        }
        mCurlView.setZoomLevel(mZoomLevel);
        mCurlView.requestRender();
    }
    
    /**
     * @brief Animates a zoom to a specified zoom level.
     * 
     * @note Animation Speed: 0.25 seconds
     * 
     * @param zoomLevel Target zoom level.
     */
    private void animateZoomTo(float zoomLevel) {
        final float duration = 250;
        final long currentTime = System.currentTimeMillis();
        final float initialZoom = mZoomLevel;
        final float targetZoom = zoomLevel;
        final Activity targetActivity = (Activity) getContext();

        Thread thread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (System.currentTimeMillis() - currentTime < duration) {
                    float percentage = (System.currentTimeMillis() - currentTime) / duration;
                    
                    mCurlView.setZoomLevel(initialZoom + ((targetZoom - initialZoom) * percentage));
                    mCurlView.requestRender();
                    
                    Thread.yield();
                }
                
                if (targetActivity != null) {
                    targetActivity.runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            setZoomLevel(targetZoom);
                        }
                    });
                };
                //setZoomLevel(targetZoom);
            }
        });
        thread.start();
    }
    
    // ================================================================================
    // INTERFACE - OnDoubleTapListener
    // ================================================================================
    
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        animateZoomTo(BASE_ZOOM_LEVEL);
        return true;
    }
    
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
    
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }
    
    // ================================================================================
    // INTERFACE - OnGestureListener
    // ================================================================================
    
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
    
    @Override
    public void onLongPress(MotionEvent e) {
        
    }
    
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }
    
    @Override
    public void onShowPress(MotionEvent e) {
    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    
    // ================================================================================
    // INTERFACE - OnScaleGestureListener
    // ================================================================================
    
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        setZoomLevel(mZoomLevel * detector.getScaleFactor());
        return true;
    }
    
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // Return true to begin scale
        return true;
    }
    
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @class PDFPageProvider
     * 
     * @brief Provider class for the PrintPreviewView
     */
    private class PDFPageProvider implements CurlView.PageProvider {
        
        @Override
        public int getPageCount() {
            return PrintPreviewView.this.getPageCount();
        }
        
        @Override
        public void updatePage(CurlPage page, int width, int height, int index) {
            page.setColor(getResources().getColor(R.color.bg_paper), CurlPage.SIDE_FRONT);
            page.setColor(getResources().getColor(R.color.bg_paper), CurlPage.SIDE_BACK);
            
            boolean GET_FROM_CACHE = true;
            
            Bitmap cachedPages[] = null;
            if (GET_FROM_CACHE) {
                cachedPages = getBitmapsFromCacheForPage(index);
            } else {
                PDFRenderTask task = new PDFRenderTask(page, width, height, index, page.createNewHandler());
                cachedPages = task.getRenderBitmaps();
            }
            
            if (cachedPages[0] == null || cachedPages[1] == null) {
                Bitmap front = Bitmap.createBitmap(SMALL_BMP_SIZE, SMALL_BMP_SIZE, BMP_CONFIG_TEXTURE);
                Bitmap back = Bitmap.createBitmap(SMALL_BMP_SIZE, SMALL_BMP_SIZE, BMP_CONFIG_TEXTURE);
                front.eraseColor(Color.WHITE);
                back.eraseColor(Color.WHITE);
                page.setTexture(front, CurlPage.SIDE_FRONT);
                page.setTexture(back, CurlPage.SIDE_BACK);
                
                new PDFRenderTask(page, width, height, index, page.createNewHandler()).execute();
            } else {
                page.setTexture(Bitmap.createBitmap(cachedPages[0]), CurlPage.SIDE_FRONT);
                page.setTexture(Bitmap.createBitmap(cachedPages[1]), CurlPage.SIDE_BACK);
            }
        }
        
        @Override
        public void indexChanged(int index) {
            if (mListener != null) {
                mListener.onIndexChanged(index);
            }
        }
    }

    /**
     * @class PDFRenderTask
     * 
     * @brief Background task which performs rendering of PDF pages.
     */
    private class PDFRenderTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<CurlPage> mCurlPageRef;
        private WeakReference<Object> mHandlerRef;
        private int mWidth;
        private int mHeight;
        private int mIndex;
        private Bitmap mRenderBmps[];
        
        /**
         * @brief Initializes a new PDF Render task with the supplied parameters
         * 
         * @param page Curl page instance to set the textures to.
         * @param width Width of the view
         * @param height Height of the view
         * @param index Current index
         * @param handler Handler object which determines whether the page is valid or not.
         */
        public PDFRenderTask(CurlPage page, int width, int height, int index, Object handler) {
            mCurlPageRef = new WeakReference<CurlPage>(page);
            mHandlerRef = new WeakReference<Object>(handler);
            mWidth = width;
            mHeight = height;
            mIndex = index;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(SLEEP_DELAY);
            } catch (InterruptedException e) {
                Logger.logWarn(PrintPreviewView.class, "Thread exception received");
            }
            
            if (mHandlerRef.get() == null || mCurlPageRef.get() == null) {
                Logger.logWarn(PrintPreviewView.class, "Cancelled process");
                return null;
            }
            mRenderBmps = getRenderBitmaps();
            return null;
        }
        
        @Override
        protected void onPostExecute(Void param) {
            if (mHandlerRef.get() != null && mCurlPageRef.get() != null) {
                mCurlPageRef.get().reset();
                mCurlPageRef.get().setTexture(Bitmap.createBitmap(mRenderBmps[0]), CurlPage.SIDE_FRONT);
                mCurlPageRef.get().setTexture(Bitmap.createBitmap(mRenderBmps[1]), CurlPage.SIDE_BACK);
                mCurlView.requestRender();
            } else {
                Logger.logWarn(PrintPreviewView.class, "Will recycle");
            }
        }
        
        /**
         * @brief Draw crease to the canvas
         * 
         * @param canvas Canvas to draw the crease onto.
         */
        private void drawCrease(Canvas canvas) {
            Paint paint = new Paint();
            paint.setStrokeWidth(CREASE_SIZE);

            int fromX = 0;
            int fromY = 0;
            int toX = canvas.getWidth() - 1;
            int toY = canvas.getHeight() - 1;
            
            switch (mCurlView.getBindPosition()) {
                case CurlView.BIND_LEFT:
                    toX = fromX;
                    break;
                case CurlView.BIND_RIGHT:
                    fromX = toX;
                    break;
                case CurlView.BIND_TOP:
                    toY = fromY;
                    break;
                case CurlView.BIND_BOTTOM:
                    fromY = toY;
                    break;
            }
            
            paint.setStyle(Style.STROKE);
            paint.setColor(getResources().getColor(R.color.theme_light_1));
            canvas.drawLine(fromX, fromY, toX, toY, paint);
            
            paint.setStyle(Style.STROKE);
            paint.setColor(getResources().getColor(R.color.theme_light_4));
            paint.setPathEffect(new DashPathEffect(new float[] {CREASE_DARK_LENGTH, CREASE_WHITE_LENGTH}, 0));
            canvas.drawLine(fromX, fromY, toX, toY, paint);
        }

        
        /*
         * @brief Draw staple images to the canvas
         * 
         * @param canvas Canvas to draw the staple images onto.
         */
        /*
        private void drawStapleImages(Canvas canvas) {
            int stapleLength = convertDimension(STAPLE_LENGTH_IN_MM, canvas.getWidth(), canvas.getHeight());
            float scale = stapleLength / (float) mStapleBmp.getWidth();
            
            int count = mPrintSettings.getStaple().getCount();
            
            if (mPrintSettings.isBooklet()) {
                if (mPrintSettings.getBookletFinish() == BookletFinish.FOLD_AND_STAPLE) {
                    count = 2;
                }
            }
            
            // CORNER
            if (count == 1) {
                int staplePos = convertDimension(STAPLE_POS_CORNER_IN_MM, canvas.getWidth(), canvas.getHeight());
                
                int x = staplePos;
                int y = staplePos;
                float rotate = -45.0f;

                if (mPrintSettings.getStaple() == Staple.ONE_UR || 
                        mCurlView.getBindPosition() == CurlView.BIND_RIGHT) {
                    x = canvas.getWidth() - staplePos;
                    rotate = -rotate;
                }
                
                ImageUtils.renderBmpToCanvas(mStapleBmp, canvas, shouldDisplayColor(), x, y, rotate, scale);                
            } else {
                int staplePos = convertDimension(STAPLE_POS_SIDE_IN_MM, canvas.getWidth(), canvas.getHeight());

                if (mPrintSettings.isBooklet()) {
                    if (mPrintSettings.getBookletFinish() == BookletFinish.FOLD_AND_STAPLE) {
                        staplePos = convertDimension(0, canvas.getWidth(), canvas.getHeight());
                    }
                }
                
                for (int i = 0; i < count; i++) {
                    int x = staplePos;
                    int y = staplePos;
                    float rotate = 90.0f;
                    
                    if (mCurlView.getBindPosition() == CurlView.BIND_LEFT) {
                        y = (canvas.getHeight() * (i + 1)) / (count + 1);
                    } else if (mCurlView.getBindPosition() == CurlView.BIND_RIGHT) {
                        x = (canvas.getWidth() - staplePos);
                        y = (canvas.getHeight() * (i + 1)) / (count + 1);
                        rotate = -rotate;
                        
                        // do not rotate when booklet to show a "continuous" staple image
                        if (mPrintSettings.isBooklet()) {
                            if (mPrintSettings.getBookletFinish() == BookletFinish.FOLD_AND_STAPLE) {
                                rotate += 180.0f;
                            }
                        }
                    } else if (mCurlView.getBindPosition() == CurlView.BIND_TOP) {
                        x = (canvas.getWidth() * (i + 1)) / (count + 1);
                        rotate = 0.0f;
                    } else if (mCurlView.getBindPosition() == CurlView.BIND_BOTTOM) {
                        x = (canvas.getWidth() * (i + 1)) / (count + 1);
                        y = (canvas.getHeight() - staplePos);
                        rotate = 0.0f;
                        
                        // do not rotate when booklet to show a "continuous" staple image
                        if (mPrintSettings.isBooklet()) {
                            if (mPrintSettings.getBookletFinish() == BookletFinish.FOLD_AND_STAPLE) {
                                rotate += 180.0f;
                            }
                        }
                    }
                    
                    ImageUtils.renderBmpToCanvas(mStapleBmp, canvas, shouldDisplayColor(), x, y, rotate, scale);
                }
            }
        }
        */

        /*
         * @brief Draw punch images to the canvas
         * 
         * @param canvas Canvas to draw the punch images onto.
         */
        /*
        private void drawPunchImages(Canvas canvas) {
            int punchDiameter = convertDimension(PUNCH_DIAMETER_IN_MM, canvas.getWidth(), canvas.getHeight());
            float scale = punchDiameter / (float) mPunchBmp.getWidth();

            int count = mPrintSettings.getPunch().getCount(mShow3Punch);
            int punchPos = convertDimension(PUNCH_POS_SIDE_IN_MM, canvas.getWidth(), canvas.getHeight());
            
            for (int i = 0; i < count; i++) {
                int x = punchPos;
                int y = punchPos;
                
                if (mCurlView.getBindPosition() == CurlView.BIND_LEFT) {
                    y = (canvas.getHeight() * (i + 1)) / (count + 1);
                } else if (mCurlView.getBindPosition() == CurlView.BIND_RIGHT) {
                    x = (canvas.getWidth() - punchPos);
                    y = (canvas.getHeight() * (i + 1)) / (count + 1);
                } else if (mCurlView.getBindPosition() == CurlView.BIND_TOP) {
                    x = (canvas.getWidth() * (i + 1)) / (count + 1);
                } else if (mCurlView.getBindPosition() == CurlView.BIND_BOTTOM) {
                    x = (canvas.getWidth() * (i + 1)) / (count + 1);
                    y = (canvas.getHeight() - punchPos);
                }
                
                ImageUtils.renderBmpToCanvas(mPunchBmp, canvas, shouldDisplayColor(), x, y, 0, scale);
            }
        }
        */
        
        /**
         * @brief Get the begin positions of a paper
         * 
         * @param paperWidth Paper width
         * @param paperHeight Paper height
         * @param pageWidth Page width
         * @param pageHeight Page height
         * 
         * @return Beginning position (x and y)
         */
        private int[] getBeginPositions(int paperWidth, int paperHeight, int pageWidth, int pageHeight) {
            int beginX = 0;
            int beginY = 0;
            
            switch (mCurlView.getBindPosition()) {
                case CurlView.BIND_LEFT:
                    beginX = paperWidth - (getColsPerSheet() * pageWidth);
                    break;
                case CurlView.BIND_TOP:
                    beginY = paperHeight - (getRowsPerSheet() * pageHeight);
                    break;
            }
            
            return (new int[] {beginX, beginY});
        }
        
        /**
         * @brief Draw PDF pages on bitmap
         * 
         * @param bmp Bitmap object to contain the PDF pages
         * @param beginIndex Start index
         * @param drawCrease Should draw the crease
         * @param flipX Should flip page horizontally
         * @param flipY Should flip page vertically
         */
        private void drawPDFPagesOnBitmap(Bitmap bmp, int beginIndex, boolean drawCrease, boolean flipX, boolean flipY) {
            // get page then draw in bitmap
            Canvas canvas = new Canvas(bmp);
            
            int pagesDisplayed = getPagesPerSheet() * (getFacesPerPaper() / 2);
            
            int paperWidth = bmp.getWidth();
            int paperHeight = bmp.getHeight();
            
            // adjust paperWidth and paperHeight based on margins
            paperWidth -= convertDimension(mMarginLeft, bmp.getWidth(), bmp.getHeight());
            paperWidth -= convertDimension(mMarginRight, bmp.getWidth(), bmp.getHeight());
            
            paperHeight -= convertDimension(mMarginTop, bmp.getWidth(), bmp.getHeight());
            paperHeight -= convertDimension(mMarginBottom, bmp.getWidth(), bmp.getHeight());
            
            int pdfPageWidth = paperWidth / getColsPerSheet();
            int pdfPageHeight = paperHeight / getRowsPerSheet();
            
            int beginPos[] = getBeginPositions(paperWidth, paperHeight, pdfPageWidth, pdfPageHeight);
            
            // adjust beginX and beginY based on margins
            beginPos[0] += convertDimension(mMarginLeft, bmp.getWidth(), bmp.getHeight());
            beginPos[1] += convertDimension(mMarginTop, bmp.getWidth(), bmp.getHeight());
            
            boolean leftToRight = true;
            boolean topToBottom = true;
            boolean horizontalFlow = true;
            
            if (!mPrintSettings.isBooklet()) {
                leftToRight = mPrintSettings.getImpositionOrder().isLeftToRight();
                topToBottom = mPrintSettings.getImpositionOrder().isTopToBottom();
                horizontalFlow = mPrintSettings.getImpositionOrder().isHorizontalFlow();
            }
            
            // start from right
            if (!leftToRight) {
                int acualWidthPixels = (getColsPerSheet() * pdfPageWidth);
                beginPos[0] = beginPos[0] + acualWidthPixels - pdfPageWidth;
            }
            
            if (!topToBottom) {
                int acualHeightPixels = (getRowsPerSheet() * pdfPageHeight);
                beginPos[1] = beginPos[1] + acualHeightPixels - pdfPageHeight;
            }
            
            
            int curX = beginPos[0];
            int curY = beginPos[1];
            
            for (int i = 0; i < getPagesPerSheet(); i++) {
                if (mHandlerRef.get() == null || mCurlPageRef.get() == null) {
                    Logger.logWarn(PrintPreviewView.class, "Cancelled process");
                    return;
                }
                
                int left = curX;
                int top = curY;
                int right = curX + pdfPageWidth;
                int bottom = curY + pdfPageHeight;
                
                float addX = pdfPageWidth;
                if (!leftToRight) {
                    addX = -pdfPageWidth;
                }
                float addY = pdfPageHeight;
                if (!topToBottom) {
                    addY = -pdfPageHeight;
                }
                
                if (horizontalFlow) {
                    curX += addX;
                    if (i % getColsPerSheet() == getColsPerSheet() - 1) {
                        curX = beginPos[0];
                        curY += addY;
                    }
                } else {
                    curY += addY;
                    if (i % getRowsPerSheet() == getRowsPerSheet() - 1) {
                        curY = beginPos[1];
                        curX += addX;
                    }
                }
                
                float scale = 1.0f / getPagesPerSheet();
                int curIndex = i + beginIndex;
                Bitmap page = mPdfManager.getPageBitmap(curIndex, scale, flipX, flipY);
                
                if (page != null) {
                    int dim[] = new int[] {
                            convertDimension(mPdfManager.getPageWidth(curIndex), canvas.getWidth(), canvas.getHeight()),
                            convertDimension(mPdfManager.getPageHeight(curIndex), canvas.getWidth(), canvas.getHeight())
                    };
                    
                    dim[0] = (int) Math.sqrt((dim[0] * dim[0] / (float)pagesDisplayed));
                    dim[1] = (int) Math.sqrt((dim[1] * dim[1] / (float)pagesDisplayed));
                    
                    int x = left;
                    int y = top;
                    float rotate = 0.0f;
                    
                    boolean shouldRotate = ((right - left) > (bottom - top)) != (mPdfManager.getPageWidth(curIndex) > mPdfManager.getPageHeight(curIndex));
                    
                    if (shouldRotate) {
                        x = left;
                        y = bottom;
                        rotate = -90.0f;
                        if (flipX) {
                            rotate += 180.0f;
                        }
                        if (flipY) {
                            rotate += 180.0f;
                        }
                    }
                    
                    int width = right - left;
                    int height = bottom - top;
                    if (shouldRotate) {
                        width = bottom - top;
                        height = right - left;
                    }
                    
                    if (mPrintSettings.isScaleToFit()) {
                        dim = AppUtils.getFitToAspectRatioSize(mPdfManager.getPageWidth(curIndex), mPdfManager.getPageHeight(curIndex), width, height);
                        // For Fit-XY
                        //dim[0] = width;
                        //dim[1] = height;
                        if (shouldRotate) {
                            x += ((height - dim[1]) / 2);
                            y -= ((width - dim[0]) / 2);
                        } else {
                            x += ((width - dim[0]) / 2);
                            y += ((height - dim[1]) / 2);
                        }
                    } else {
                        if (shouldRotate) {
                            // Adjust x and y position when flipped
                            if (flipX) {
                                x -= (dim[1] - height);
                            }
                            if (flipY) {
                                y += (dim[0] - width);
                            }
                        } else {
                            // Adjust x and y position when flipped
                            if (flipX) {
                                x -= (dim[0] - width);
                            }
                            if (flipY) {
                                y -= (dim[1] - height);
                            }
                        }
                    }
                    
                    canvas.save(Canvas.CLIP_SAVE_FLAG);
                    canvas.clipRect(left, top, right + 1, bottom + 1);
                    
                    /*
                    Rect destRect = new Rect(x, y, x + dim[0], y + dim[1]);
                    ImageUtils.renderBmpToCanvas(page, canvas, shouldDisplayColor(), x, y, destRect);
                    */
                    // Adjust x and y for rotation purposes
                    if (shouldRotate) {
                        x += (dim[1] / 2);
                        y -= (dim[0] / 2);
                    } else {
                        x += (dim[0] / 2);
                        y += (dim[1] / 2);
                    }
                    float drawScale = dim[0] / (float)page.getWidth();
                    ImageUtils.renderBmpToCanvas(page, canvas, shouldDisplayColor(), x, y, rotate, drawScale);
                    
                    canvas.restore();
                    
                    page.recycle();
                }
                
                // Draw staple and punch images
                if (drawCrease) {
                    drawCrease(canvas);
                }
                /*
                drawStapleImages(canvas);
                drawPunchImages(canvas);
                */
            }
        }
        
        /**
         * @brief Gets the Front and back bitmaps.
         * 
         * @note The bitmaps are saved in the cache.
         * 
         * @return Front and back bitmaps
         */
        private Bitmap[] getRenderBitmaps() {
            int dim[] = getPaperDimensions(mWidth, mHeight);
            
            Bitmap front = Bitmap.createBitmap(dim[0], dim[1], BMP_CONFIG_TEXTURE);
            Bitmap back = Bitmap.createBitmap(dim[0], dim[1], BMP_CONFIG_TEXTURE);
            front.eraseColor(Color.WHITE);
            back.eraseColor(Color.WHITE);
            int pagePerScreen = 1;
            if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
                pagePerScreen = 2;
            }
            
            pagePerScreen *= getPagesPerSheet();
            
            int frontIndex = (mIndex * pagePerScreen);
            
            boolean drawCrease = (mIndex != 0);
            drawCrease = drawCrease && (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES);
            drawPDFPagesOnBitmap(front, frontIndex, drawCrease, false, false);
            
            if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
                int backIndex = (mIndex * pagePerScreen) + getPagesPerSheet();
                
                boolean verticalFlip = isVerticalFlip();
                drawPDFPagesOnBitmap(back, backIndex, false, !verticalFlip, verticalFlip);
            }
            
            if (mBmpCache != null) {
                mBmpCache.put(getCacheKey(mIndex, CurlPage.SIDE_FRONT), front);
                mBmpCache.put(getCacheKey(mIndex, CurlPage.SIDE_BACK), back);
            }
            
            return new Bitmap[] { front, back };
        }
    }
}
 