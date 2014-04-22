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

import jp.co.riso.android.util.ImageUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletFinish;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletLayout;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ColorMode;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Orientation;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Staple;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.FrameLayout;
import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;

public class PrintPreviewView extends FrameLayout implements OnScaleGestureListener {
    public static final String TAG = "PrintPreviewView";
    
    private static final float DEFAULT_MARGIN_IN_MM = 0;
    
    private static final float CREASE_SIZE = 1.0f;
    private static final float CREASE_DARK_LENGTH = 8.0f;
    private static final float CREASE_WHITE_LENGTH = 4.0f;
    
    private static final float PUNCH_DIAMETER_IN_MM = 12;
    private static final float PUNCH_POS_SIDE_IN_MM = 8;
    
    private static final float STAPLE_LENGTH_IN_MM = 12;
    
    private static final float STAPLE_POS_CORNER_IN_MM = 6;
    private static final float STAPLE_POS_SIDE_IN_MM = 4;

    public static final float BASE_ZOOM_LEVEL = 1.0f;
    public static final float MAX_ZOOM_LEVEL = 4.0f;
    
    private static final int INVALID_IDX = -1;
    
    private static final int SLEEP_DELAY = 128;
    private static final int SMALL_BMP_SIZE = 64;
    private static final Bitmap.Config BMP_CONFIG_TEXTURE = Config.RGB_565;
    
    private CurlView mCurlView = null;
    private PDFFileManager mPdfManager = null;
    private PDFPageProvider mPdfPageProvider = new PDFPageProvider();
    private PrintSettings mPrintSettings = null;
    private LruCache<String, Bitmap> mBmpCache = null;
    
    private Bitmap mStapleBmp = null;
    private Bitmap mPunchBmp = null;
    
    private float mMarginLeft = 0;
    private float mMarginRight = 0;
    private float mMarginTop = 0;
    private float mMarginBottom = 0;
    
    private PreviewControlsListener mListener = null;
    
    // Zoom/pan related variables
    
    private ScaleGestureDetector mScaleDetector;
    private float mZoomLevel = BASE_ZOOM_LEVEL;
    
    private int mPtrIdx = INVALID_IDX;
    private PointF mPtrDownPos = new PointF();
    private PointF mPtrLastPos = new PointF();
    
    /**
     * Constructor
     * 
     * @param context
     *            Context
     * @param attrs
     *            Attributes
     * @param defStyle
     *            Default style
     */
    public PrintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * Constructor
     * 
     * @param context
     *            Context
     * @param attrs
     *            Attributes
     */
    public PrintPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * Constructor
     * 
     * @param context
     *            Context
     */
    public PrintPreviewView(Context context) {
        super(context);
        init();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        if (!isInEditMode()) {
            // Re-adjust the margin of the CurlView
            fitCurlView(l, t, r, b);
        }
    }
    
    /**
     * Initialize PrintPreviewView
     */
    public void init() {
        if (!isInEditMode()) {
            mPrintSettings = new PrintSettings(); // Should not be null
            mScaleDetector = new ScaleGestureDetector(getContext(), this);
            
            initializeCurlView();
            loadResources();
        }
    }
    
    /**
     * Inform the curl view that the activity is paused. 
     */
    public void onPause() {
        mCurlView.onPause();
    }
    
    /**
     * Inform the curl view that the activity is resumed. 
     */
    public void onResume() {
        mCurlView.onResume();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    
    /**
     * Process touch event
     */
    public void processTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() == 1) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mPtrIdx == INVALID_IDX) {
                        mPtrIdx = ev.getActionIndex();
                        mPtrDownPos.set(ev.getX(), ev.getY());
                        mPtrLastPos.set(ev.getX(), ev.getY());
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mPtrIdx == ev.getActionIndex()) {
                        mCurlView.adjustPan(ev.getX() - mPtrLastPos.x, ev.getY() - mPtrLastPos.y);
                        mCurlView.requestRender();
                        
                        mPtrLastPos.set(ev.getX(), ev.getY());
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mPtrIdx = -1;
                    break;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        
        if (!mScaleDetector.isInProgress() && ev.getPointerCount() == 1) {
            if (mZoomLevel == BASE_ZOOM_LEVEL) {
                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).dispatchTouchEvent(ev);
                }                
            } else {
                processTouchEvent(ev);
            }
        } else {
            MotionEvent e = MotionEvent.obtain( SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), 
                    MotionEvent.ACTION_CANCEL, 
                    ev.getX(), ev.getY(), 0);
            
            processTouchEvent(e);
            
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).dispatchTouchEvent(e);
            }
            
            e.recycle();
        }
        
        return true;
    }
    
    // ================================================================================
    // Public methods
    // ================================================================================
    
    /**
     * Load Bitmap resources such as staple and punch
     */
    public void loadResources() {
        mStapleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_staple);
        mPunchBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_punch);
    }
    
    /**
     * Free the native object associated with the Bitmap loaded by method loadResources()
     */
    public void freeResources() {
        mStapleBmp.recycle();
        mPunchBmp.recycle();
    }
    
    /**
     * Request that the curl view renderer render a frame.
     */
    public void refreshView() {
        mCurlView.requestRender(); 
    }
    
    /**
     * Sets the PDF Manager
     * 
     * @param pdfManager
     *            PDF Manager
     */
    public void setPdfManager(PDFFileManager pdfManager) {
        mPdfManager = pdfManager;
    }
    
    /**
     * Set the Print Settings
     * 
     * @param printSettings
     *            Print Settings
     */
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = printSettings;
        
        if (getCurrentPage() > mPdfPageProvider.getPageCount()) {
            setCurrentPage(mPdfPageProvider.getPageCount());
        }
        
        setupCurlPageView();
        setupCurlBind();
        mCurlView.requestLayout();
    }
    
    /**
     * Set the Bitmap Cache
     * 
     * @param bmpCache
     *            Bitmap cache
     */
    public void setBmpCache(LruCache<String, Bitmap> bmpCache) {
        mBmpCache = bmpCache;
    }
    
    /**
     * Set the default margins
     */
    public void setDefaultMargins() {
        setMarginLeftInMm(DEFAULT_MARGIN_IN_MM);
        setMarginRightInMm(DEFAULT_MARGIN_IN_MM);
        setMarginTopInMm(DEFAULT_MARGIN_IN_MM);
        setMarginBottomInMm(DEFAULT_MARGIN_IN_MM);
    }
    
    /**
     * Set the Left Margin
     * 
     * @param marginLeft
     *            Left margin
     */
    public void setMarginLeftInMm(float marginLeft) {
        mMarginLeft = marginLeft;
    }
    
    /**
     * Set the Right Margin
     * 
     * @param marginRight
     *            Right margin
     */
    public void setMarginRightInMm(float marginRight) {
        mMarginRight = marginRight;
    }
    
    /**
     * Set the Top Margin
     * 
     * @param marginTop
     *            Top margin
     */
    public void setMarginTopInMm(float marginTop) {
        mMarginTop = marginTop;
    }
    
    /**
     * Set the Bottom Margin
     * 
     * @param marginBottom
     *            Bottom margin
     */
    public void setMarginBottomInMm(float marginBottom) {
        mMarginBottom = marginBottom;
    }
    
    /**
     * Set the Preview Controls Listener
     * 
     * @param listener
     *            Preview Controls listener
     */
    public void setListener(PreviewControlsListener listener) {
        mListener = listener;
    }
    
    /**
     * Checks if last page curl is allowed
     */
    public boolean getAllowLastPageCurl() {
        return mCurlView.getAllowLastPageCurl();
    }
    
    /**
     * @return current curl page index
     */
    public int getCurrentPage() {
        return mCurlView.getCurrentIndex();
    }
    
    /**
     * Set current curl page index
     * 
     * @param page
     *            page index
     */
    public void setCurrentPage(int page) {
        mCurlView.setCurrentIndex(page);
    }
    
    /**
     * @return page count
     */
    public int getPageCount() {
        if (mPdfManager == null) {
            return 0;
        }
        
        // will depend on PDF and pagination, always false for now
        int count = mPdfManager.getPageCount();
        
        if (isTwoPageDisplayed()) {
            count = (int) Math.ceil(count / 2.0f);
        }
        
        count = (int) Math.ceil(count / (double) getPagesPerSheet());
        
        if (mPrintSettings.isBooklet()) {
            int modulo = count % 2;
            if (count > 0) {
                count = count + 2 - modulo;
            }
        }
        
        return count;
    }
    
    /**
     * @return page string
     */
    public String getPageString() {
        final String FORMAT_ONE_PAGE_STATUS = "PAGE %d / %d";
        final String FORMAT_TWO_PAGE_STATUS = "PAGE %d-%d / %d";

        int currentPage = getCurrentPage();
        int pageCount = mPdfPageProvider.getPageCount();
        
        if (mCurlView.getViewMode() == CurlView.SHOW_ONE_PAGE || getCurrentPage() == 0) {
            return String.format(Locale.getDefault(), FORMAT_ONE_PAGE_STATUS, currentPage + 1, pageCount);
        } else if (getCurrentPage() == mPdfPageProvider.getPageCount()) {
            return String.format(Locale.getDefault(), FORMAT_ONE_PAGE_STATUS, currentPage, pageCount);
        } else {
            return String.format(Locale.getDefault(), FORMAT_TWO_PAGE_STATUS, currentPage, currentPage + 1, pageCount);
        }
    }
    
    /**
     * Checks if zoom is base zoom
     */
    public boolean isBaseZoom() {
        return mZoomLevel == BASE_ZOOM_LEVEL;
    }
    
    // ================================================================================
    // Protected methods
    // ================================================================================
    
    /**
     * Get fit to aspect ratio size
     * 
     * @param srcWidth
     *            Source width
     * @param srcHeight
     *            Source height
     * @param destWidth
     *            Destination width
     * @param destHeight
     *            Destination height
     * @return New width and height
     */
    protected int[] getFitToAspectRatioSize(float srcWidth, float srcHeight, int destWidth, int destHeight) {
        float ratioSrc = srcWidth / srcHeight;
        float ratioDest = (float) destWidth / destHeight;
        
        int newWidth = 0;
        int newHeight = 0;
        
        if (ratioDest > ratioSrc) {
            newHeight = destHeight;
            newWidth = (int) (destHeight * ratioSrc);
            
        } else {
            newWidth = destWidth;
            newHeight = (int) (destWidth / ratioSrc);
        }
        
        return new int[] { newWidth, newHeight };
    }
    
    /**
     * Get curl view dimensions
     * 
     * @param screenWidth
     *            Screen width
     * @param screenHeight
     *            Screen height
     * @return curl view width and height
     */
    protected int[] getCurlViewDimensions(int screenWidth, int screenHeight) {
        // Compute margins based on the paper size in preview settings.
        float paperDisplaySize[] = getPaperDisplaySize();
        
        // Adjust display on screen.
        if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
            if (mCurlView.getBindPosition() == CurlView.BIND_TOP) {
                // double the height if bind == top
                paperDisplaySize[1] *= 2.0f;
            } else {
                // double the width if bind != top
                paperDisplaySize[0] *= 2.0f;
            }
        }
        
        return getFitToAspectRatioSize(paperDisplaySize[0], paperDisplaySize[1], screenWidth, screenHeight);
    }
    
    /**
     * Get paper dimensions
     * 
     * @param screenWidth
     *            Screen width
     * @param screenHeight
     *            Screen height
     * @return paper width and height
     */
    protected int[] getPaperDimensions(int screenWidth, int screenHeight) {
        // Compute margins based on the paper size in preview settings.
        float paperDisplaySize[] = getPaperDisplaySize();
        
        return getFitToAspectRatioSize(paperDisplaySize[0], paperDisplaySize[1], screenWidth, screenHeight);
    }
    
    /**
     * @return cache key
     */
    protected String getCacheKey(int index, int side) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(mPdfManager.getPath());
        buffer.append(index);
        buffer.append(side);
        
        buffer.append(shouldDisplayColor());
        buffer.append(mPrintSettings.isScaleToFit());
        buffer.append(mPrintSettings.getOrientation().ordinal());
        buffer.append(mPrintSettings.getPaperSize().ordinal());
        
        buffer.append(mPrintSettings.getDuplex().ordinal());
        buffer.append(mPrintSettings.getImposition().ordinal());
        buffer.append(mPrintSettings.getImpositionOrder().ordinal());
        
        buffer.append(mPrintSettings.getFinishingSide().ordinal());
        buffer.append(mPrintSettings.getStaple().ordinal());
        buffer.append(mPrintSettings.getPunch().ordinal());
        
        buffer.append(mPrintSettings.isBooklet());
        buffer.append(mPrintSettings.getBookletFinish().ordinal());
        buffer.append(mPrintSettings.getBookletLayout().ordinal());
        
        return buffer.toString();
    }
    
    /**
     * @return bitmaps from cache
     */
    protected Bitmap[] getBitmapsFromCacheForPage(int index, int width, int height) {
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
     * @return converted dimension
     */
    protected int convertDimension(float dimension, int bmpWidth) {
        return (int)((dimension / mPrintSettings.getPaperSize().getWidth()) * bmpWidth);
    }
    
    // ================================================================================
    // Preview Related functions
    // ================================================================================
    
    /**
     * Check if should display landscape
     */
    private boolean shouldDisplayLandscape() {
        boolean flipToLandscape = (mPrintSettings.getOrientation() == Orientation.LANDSCAPE);
        
        if (!mPrintSettings.isBooklet()) {
            if (mPrintSettings.getImposition().isFlipLandscape()) {
                flipToLandscape = !flipToLandscape;
            }
        }
        
        return flipToLandscape;
    }
    
    /**
     * @return paper display width and height
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
     * @return pages per sheet
     */
    public int getPagesPerSheet() {
        if (mPrintSettings.isBooklet()) {
            return 1;
        }
        
        return mPrintSettings.getImposition().getPerPage();
    }
    
    /**
     * @return columns per sheet
     */
    public int getColsPerSheet() {
        if (mPrintSettings.isBooklet()) {
            return 1;
        }
        
        if (mPrintSettings.getOrientation() == Orientation.LANDSCAPE) {
            return mPrintSettings.getImposition().getRows();
        }
        
        return mPrintSettings.getImposition().getCols();
    }
    
    /**
     * @return rows per sheet
     */
    public int getRowsPerSheet() {
        if (mPrintSettings.isBooklet()) {
            return 1;
        }
        
        if (mPrintSettings.getOrientation() == Orientation.LANDSCAPE) {
            return mPrintSettings.getImposition().getCols();
        }
        
        return mPrintSettings.getImposition().getRows();
    }
    
    /**
     * Setup curl page view
     */
    private void setupCurlPageView() {
        boolean twoPage = false;
        boolean allowLastPageCurl = false;
        
        if (getPageCount() > 1 && isTwoPageDisplayed()) {
            twoPage = true;
            
            if (getCurrentPage() % 2 == 0) {
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
     * Check if two page display is enabled
     */
    private boolean isTwoPageDisplayed() {
        if (mPrintSettings.isBooklet()) {
            return true;
        }
        
        return (mPrintSettings.getDuplex() != Duplex.OFF);
    }
    
    /**
     * Setup curl bind
     */
    private void setupCurlBind() {
        int bindPosition = CurlView.BIND_LEFT;
        
        if (mPrintSettings.isBooklet()) {
            bindPosition = CurlView.BIND_LEFT;
            
            if (shouldDisplayLandscape()) {
                bindPosition = CurlView.BIND_TOP;
            } else if (mPrintSettings.getBookletLayout() == BookletLayout.R_L) {
                bindPosition = CurlView.BIND_RIGHT;
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
     * Check if colored display is enabled
     */
    private boolean shouldDisplayColor() {
        return (mPrintSettings.getColorMode() != ColorMode.MONOCHROME);
    }
    
    /**
     * Check if display vertical flip is enabled
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
     * Initialize curl view
     */
    private void initializeCurlView() {
        mCurlView = new CurlView(getContext());
        mCurlView.setMargins(0, 0, 0, 0);
        mCurlView.setPageProvider(mPdfPageProvider);
        mCurlView.setBindPosition(CurlView.BIND_LEFT);
        mCurlView.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
        
        setupCurlPageView();
        setupCurlBind();
        setDefaultMargins();
        
        if (!isInEditMode()) {
            float percentage = getResources().getFraction(R.dimen.preview_view_drop_shadow_percentage, 1, 1);
            mCurlView.setDropShadowSize(percentage);
        }
        
        addView(mCurlView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
    
    /**
     * Layout curl view to fit screen
     * 
     * @param l
     *            left
     * @param t
     *            top
     * @param r
     *            right
     * @param b
     *            bottom
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
    
    // ================================================================================
    // INTERFACE - OnScaleGestureListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mZoomLevel  = mZoomLevel * detector.getScaleFactor();
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
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // Return true to begin scale
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class PDFPageProvider implements CurlView.PageProvider {
        
        /** {@inheritDoc} */
        @Override
        public int getPageCount() {
            return PrintPreviewView.this.getPageCount();
        }
        
        /** {@inheritDoc} */
        @Override
        public void updatePage(CurlPage page, int width, int height, int index) {
            page.setColor(getResources().getColor(R.color.bg_paper), CurlPage.SIDE_FRONT);
            page.setColor(getResources().getColor(R.color.bg_paper), CurlPage.SIDE_BACK);
            
            boolean GET_FROM_CACHE = true;
            
            Bitmap cachedPages[] = null;
            if (GET_FROM_CACHE) {
                cachedPages = getBitmapsFromCacheForPage(index, width, height);
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
        
        /** {@inheritDoc} */
        @Override
        public void indexChanged(int index) {
            if (mListener != null) {
                mListener.onIndexChanged(index);
            }
        }
    }
    
    private class PDFRenderTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<CurlPage> mCurlPageRef;
        private WeakReference<Object> mHandlerRef;
        private int mWidth;
        private int mHeight;
        private int mIndex;
        private Bitmap mRenderBmps[];
        
        /**
         * Constructor
         * 
         * @param page
         *            curl page
         * @param width
         *            width
         * @param height
         *            height
         * @param index
         *            index
         * @param handler
         *            handler
         */
        public PDFRenderTask(CurlPage page, int width, int height, int index, Object handler) {
            mCurlPageRef = new WeakReference<CurlPage>(page);
            mHandlerRef = new WeakReference<Object>(handler);
            mWidth = width;
            mHeight = height;
            mIndex = index;
        }
        
        /** {@inheritDoc} */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(SLEEP_DELAY);
            } catch (InterruptedException e) {
                Log.w(TAG, "Thread exception received");
            }
            
            if (mHandlerRef.get() == null || mCurlPageRef.get() == null) {
                Log.w(TAG, "Cancelled process");
                return null;
            }
            mRenderBmps = getRenderBitmaps();
            return null;
        }
        
        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(Void param) {
            if (mHandlerRef.get() != null && mCurlPageRef.get() != null) {
                mCurlPageRef.get().reset();
                mCurlPageRef.get().setTexture(Bitmap.createBitmap(mRenderBmps[0]), CurlPage.SIDE_FRONT);
                mCurlPageRef.get().setTexture(Bitmap.createBitmap(mRenderBmps[1]), CurlPage.SIDE_BACK);
                mCurlView.requestRender();
            } else {
                Log.w(TAG, "Will recycle");
            }
        }
        
        /**
         * Draw crease to the canvas
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
            }
            
            paint.setStyle(Style.STROKE);
            paint.setColor(getResources().getColor(R.color.theme_light_1));
            canvas.drawLine(fromX, fromY, toX, toY, paint);
            
            paint.setStyle(Style.STROKE);
            paint.setColor(getResources().getColor(R.color.theme_light_4));
            paint.setPathEffect(new DashPathEffect(new float[] {CREASE_DARK_LENGTH, CREASE_WHITE_LENGTH}, 0));
            canvas.drawLine(fromX, fromY, toX, toY, paint);
        }

        /**
         * Draw staple images to the canvas
         */
        private void drawStapleImages(Canvas canvas) {
            int stapleLength = convertDimension(STAPLE_LENGTH_IN_MM, canvas.getWidth());
            float scale = stapleLength / (float) mStapleBmp.getWidth();
            
            int count = mPrintSettings.getStaple().getCount();
            
            if (mPrintSettings.isBooklet()) {
                if (mPrintSettings.getBookletFinish() == BookletFinish.FOLD_AND_STAPLE) {
                    count = 2;
                }
            }
            
            // CORNER
            if (count == 1) {
                int staplePos = convertDimension(STAPLE_POS_CORNER_IN_MM, canvas.getWidth());
                
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
                int staplePos = convertDimension(STAPLE_POS_SIDE_IN_MM, canvas.getWidth());

                if (mPrintSettings.isBooklet()) {
                    if (mPrintSettings.getBookletFinish() == BookletFinish.FOLD_AND_STAPLE) {
                        staplePos = convertDimension(0, canvas.getWidth());
                    }
                }
                
                for (int i = 0; i < count; i++) {
                    int x = staplePos;
                    int y = staplePos;
                    float rotate = -90.0f;
                    
                    if (mCurlView.getBindPosition() == CurlView.BIND_LEFT) {
                        y = (canvas.getHeight() * (i + 1)) / (count + 1);
                    } else if (mCurlView.getBindPosition() == CurlView.BIND_RIGHT) {
                        x = (canvas.getWidth() - staplePos);
                        y = (canvas.getHeight() * (i + 1)) / (count + 1);
                        rotate = -rotate;
                        
                        // do not rotate when booklet to show a "continuous" staple image
                        if (mPrintSettings.isBooklet()) {
                            if (mPrintSettings.getBookletFinish() == BookletFinish.FOLD_AND_STAPLE) {
                                rotate = -rotate;
                            }
                        }
                    } else if (mCurlView.getBindPosition() == CurlView.BIND_TOP) {
                        x = (canvas.getWidth() * (i + 1)) / (count + 1);
                        rotate = 0.0f;
                    }
                    
                    ImageUtils.renderBmpToCanvas(mStapleBmp, canvas, shouldDisplayColor(), x, y, rotate, scale);
                }
            }
        }
        
        /**
         * Draw punch images to the canvas
         */
        private void drawPunchImages(Canvas canvas) {
            int punchDiameter = convertDimension(PUNCH_DIAMETER_IN_MM, canvas.getWidth());
            float scale = punchDiameter / (float) mPunchBmp.getWidth();

            int count = mPrintSettings.getPunch().getCount();
            int punchPos = convertDimension(PUNCH_POS_SIDE_IN_MM, canvas.getWidth());
            
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
                }
                
                ImageUtils.renderBmpToCanvas(mPunchBmp, canvas, shouldDisplayColor(), x, y, 0, scale);
            }
        }
        
        /**
         * @return x-axis and y-axis beginning position
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
         * Draw PDF pages on bitmap
         */
        private void drawPDFPagesOnBitmap(Bitmap bmp, int beginIndex, boolean drawCrease, boolean flipX, boolean flipY) {
            // get page then draw in bitmap
            Canvas canvas = new Canvas(bmp);
            
            int paperWidth = bmp.getWidth();
            int paperHeight = bmp.getHeight();
            
            // adjust paperWidth and paperHeight based on margins
            paperWidth -= convertDimension(mMarginLeft, bmp.getWidth());
            paperWidth -= convertDimension(mMarginRight, bmp.getWidth());
            
            paperHeight -= convertDimension(mMarginTop, bmp.getWidth());
            paperHeight -= convertDimension(mMarginBottom, bmp.getWidth());
            
            int pdfPageWidth = paperWidth / getColsPerSheet();
            int pdfPageHeight = paperHeight / getRowsPerSheet();
            
            int beginPos[] = getBeginPositions(paperWidth, paperHeight, pdfPageWidth, pdfPageHeight);
            
            // adjust beginX and beginY based on margins
            beginPos[0] += convertDimension(mMarginLeft, bmp.getWidth());
            beginPos[1] += convertDimension(mMarginTop, bmp.getWidth());
            
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
                    Log.w(TAG, "Cancelled process");
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
                        curY = beginPos[0];
                        curX += addX;
                    }
                }
                
                float scale = 1.0f / getPagesPerSheet();
                
                Bitmap page = mPdfManager.getPageBitmap(i + beginIndex, scale, flipX, flipY);
                
                if (page != null) {
                    int dim[] = getFitToAspectRatioSize(mPdfManager.getPageWidth(), mPdfManager.getPageHeight(), right - left, bottom - top);
                    if (mPrintSettings.isScaleToFit()) {
                        dim[0] = right - left;
                        dim[1] = bottom - top;
                    }
                    
                    int x = left + ((right - left) - dim[0]) / 2;
                    int y = top + ((bottom - top) - dim[1]) / 2;
                    
                    Rect destRect = new Rect(x, y, x + dim[0], y + dim[1]);
                    ImageUtils.renderBmpToCanvas(page, canvas, shouldDisplayColor(), destRect);
                    
                    page.recycle();
                }
                
                // Draw staple and punch images
                if (drawCrease) {
                    drawCrease(canvas);
                }
                drawStapleImages(canvas);
                drawPunchImages(canvas);
            }
        }
        
        /**
         * @return Bitmaps
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
 