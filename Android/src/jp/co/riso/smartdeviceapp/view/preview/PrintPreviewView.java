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
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ColorMode;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Imposition;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Orientation;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Staple;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;

public class PrintPreviewView extends FrameLayout implements OnSeekBarChangeListener, OnScaleGestureListener {
    public static final String TAG = "PrintPreviewView";
    
    private static final float DEFAULT_MARGIN_IN_MM = 0;
    
    private static final float PUNCH_DIAMETER_IN_MM = 12;
    private static final float PUNCH_POS_SIDE_IN_MM = 8;
    
    private static final float STAPLE_LENGTH_IN_MM = 12;
    
    private static final float STAPLE_POS_CORNER_IN_MM = 6;
    private static final float STAPLE_POS_SIDE_IN_MM = 4;

    private static final float BASE_ZOOM_LEVEL = 1.0f;
    private static final float MAX_ZOOM_LEVEL = 4.0f;
    
    private static final int INVALID_IDX = -1;
    
    private static final int SLEEP_DELAY = 128;
    private static final int SMALL_BMP_SIZE = 64;
    private static final Bitmap.Config BMP_CONFIG_TEXTURE = Config.RGB_565;
    
    private CurlView mCurlView;
    private PDFFileManager mPdfManager = null;
    private PDFPageProvider mPdfPageProvider = new PDFPageProvider();
    private PrintSettings mPrintSettings = new PrintSettings(); // Should not be null
    private LruCache<String, Bitmap> mBmpCache = null;
    
    private LinearLayout mPageControlLayout;
    private SeekBar mSeekBar;
    private TextView mPageLabel;
    private Bitmap mStapleBmp;
    private Bitmap mPunchBmp;
    
    private float mMarginLeft = 0;
    private float mMarginRight = 0;
    private float mMarginTop = 0;
    private float mMarginBottom = 0;
    
    // Zoom/pan related variables
    
    private ScaleGestureDetector mScaleDetector;
    private float mZoomLevel = BASE_ZOOM_LEVEL;
    
    private int mPtrIdx = INVALID_IDX;
    private PointF mPtrDownPos = new PointF();
    private PointF mPtrLastPos = new PointF();
    
    public PrintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    public PrintPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public PrintPreviewView(Context context) {
        super(context);

        init();
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        // Re-adjust the margin of the CurlView
        fitCurlView(l, t, r, b);
    }
    
    public void init() {
        
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        
        initializeCurlView();
        initializePageControls();
        loadResources();
    }
    
    public void onPause() {
        mCurlView.onPause();
    }
    
    public void onResume() {
        mCurlView.onResume();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    
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
                        mCurlView.requestLayout();
                        
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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        
        if (!mScaleDetector.isInProgress()) {
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
    
    public void loadResources() {
        mStapleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_staple);
        mPunchBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_punch);
    }
    
    public void freeResources() {
        mStapleBmp.recycle();
        mPunchBmp.recycle();
    }
    
    public void refreshView() {
        invalidate();
        
        updateSeekBar();
        updatePageLabel();
    }
    
    public void setPdfManager(PDFFileManager pdfManager) {
        mPdfManager = pdfManager;
        
        updateSeekBar();
        updatePageLabel();
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = printSettings;
        
        if (getCurrentPage() > mPdfPageProvider.getPageCount()) {
            setCurrentPage(mPdfPageProvider.getPageCount());
        }
        
        setupCurlPageView();
        setupCurlBind();
        requestLayout();
    }
    
    public void setBmpCache(LruCache<String, Bitmap> bmpCache) {
        mBmpCache = bmpCache;
    }
    
    public int getCurrentPage() {
        return mCurlView.getCurrentIndex();
    }
    
    public void setCurrentPage(int page) {
        mCurlView.setCurrentIndex(page);
    }
    
    public void setDefaultMargins() {
        setMarginLeftInMm(DEFAULT_MARGIN_IN_MM);
        setMarginRightInMm(DEFAULT_MARGIN_IN_MM);
        setMarginTopInMm(DEFAULT_MARGIN_IN_MM);
        setMarginBottomInMm(DEFAULT_MARGIN_IN_MM);
    }
    
    public void setMarginLeftInMm(float marginLeft) {
        mMarginLeft = marginLeft;
    }
    
    public void setMarginRightInMm(float marginRight) {
        mMarginRight = marginRight;
    }
    
    public void setMarginTopInMm(float marginTop) {
        mMarginTop = marginTop;
    }
    
    public void setMarginBottomInMm(float marginBottom) {
        mMarginBottom = marginBottom;
    }
    
    // ================================================================================
    // Protected methods
    // ================================================================================
    
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
    
    protected int[] getScreenDimensions(int screenWidth, int screenHeight) {
        // Compute margins based on the paper size in preview settings.
        float paperWidth = getPaperDisplayWidth();
        float paperHeight = getPaperDisplayHeight();
        
        if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
            if (mCurlView.getBindPosition() == CurlView.BIND_TOP) {
                paperHeight *= 2.0f;
            } else {
                paperWidth *= 2.0f;
            }
        }
        
        return getFitToAspectRatioSize(paperWidth, paperHeight, screenWidth, screenHeight);
    }
    
    protected int[] getPaperDimensions(int screenWidth, int screenHeight) {
        // Compute margins based on the paper size in preview settings.
        float paperWidth = getPaperDisplayWidth();
        float paperHeight = getPaperDisplayHeight();
        
        return getFitToAspectRatioSize(paperWidth, paperHeight, screenWidth, screenHeight);
    }
    
    protected String getCacheKey(int index, int side) {
        // path; page; side; paper size; duplex; imposition; color; landscape; staple; punch
        StringBuffer buffer = new StringBuffer();
        buffer.append(mPdfManager.getPath());
        buffer.append(index);
        buffer.append(side);
        buffer.append(mPrintSettings.getPaperSize().ordinal());
        buffer.append(mPrintSettings.getImposition().ordinal());
        buffer.append(mPrintSettings.getDuplex().ordinal());
        buffer.append(mPrintSettings.isScaleToFit());
        buffer.append(shouldDisplayColor());
        buffer.append(shouldDisplayLandscape());
        buffer.append(mPrintSettings.getStaple().ordinal());
        buffer.append(mPrintSettings.getPunch().ordinal());
        
        return buffer.toString();
    }
    
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
    
    protected int convertDimension(float dimension, int bmpWidth) {
        return (int)((dimension / mPrintSettings.getPaperSize().getWidth()) * bmpWidth);
    }
    
    // ================================================================================
    // PDF page methods
    // ================================================================================
    
    private int getPageCount() {
        if (mPdfManager == null) {
            return 0;
        }
        
        // will depend on PDF and pagination, always false for now
        int count = mPdfManager.getPageCount();
        
        if (isTwoPageDisplayed()) {
            count = (int) Math.ceil(count / 2.0f);
        }
        
        count = (int) Math.ceil(count / (double) mPrintSettings.getImposition().getPerPage());
        
        return count;
    }
    
    private boolean shouldDisplayLandscape() {
        if (mPdfManager == null) {
            return false;
        }
        
        boolean flipToLandscape = mPrintSettings.getOrientation() == Orientation.LANDSCAPE;
        
        /*
        if (mPrintSettings.isBooklet()) {
            flipToLandscape = !flipToLandscape;
        }
        
        if (mPrintSettings.getImposition().isFlipLandscape()) {
            flipToLandscape = !flipToLandscape;
        }
        */
        
        return flipToLandscape;
    }
    
    private float getPaperDisplayWidth() {
        float width = mPrintSettings.getPaperSize().getWidth();
        
        if (shouldDisplayLandscape()) {
            width = mPrintSettings.getPaperSize().getHeight();
        }
        
        return width;
    }
    
    private float getPaperDisplayHeight() {
        float height = mPrintSettings.getPaperSize().getHeight();
        
        if (shouldDisplayLandscape()) {
            height = mPrintSettings.getPaperSize().getWidth();
        }
        
        return height;
    }
    
    private void setupCurlPageView() {
        boolean twoPage = false;
        boolean allowLastPageCurl = false;
        
        if (isTwoPageDisplayed()) {
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
    
    private void setupCurlBind() {
        int bindPosition = CurlView.BIND_LEFT;
        
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
        
        mCurlView.setBindPosition(bindPosition);
    }
    
    private boolean shouldDisplayColor() {
        return (mPrintSettings.getColorMode() != ColorMode.MONOCHROME);
    }
    
    private boolean isTwoPageDisplayed() {
        return (mPrintSettings.getDuplex() != Duplex.OFF);
    }
    
    private boolean isVerticalFlip() {
        boolean verticalFlip = true;
        
        if (shouldDisplayLandscape()) {
            verticalFlip = (mPrintSettings.getDuplex() == Duplex.LONG_EDGE);
        } else {
            verticalFlip = (mPrintSettings.getDuplex() == Duplex.SHORT_EDGE);
        }
        
        return verticalFlip;
    }
    
    // ================================================================================
    // View-related methods
    // ================================================================================
    
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
    
    private void initializePageControls() {
        mSeekBar = new SeekBar(getContext());
        mSeekBar.setOnSeekBarChangeListener(this);
        
        mPageLabel = new TextView(getContext());
        mPageLabel.setTextColor(getResources().getColor(R.color.theme_dark_1));
        mPageLabel.setGravity(Gravity.CENTER);
        
        updateSeekBar();
        updatePageLabel();
        
        mPageControlLayout = new LinearLayout(getContext());
        
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        
        if (isLandscape && !isTablet) {
            mPageControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            
            int width = getResources().getDimensionPixelSize(R.dimen.preview_controls_text_width);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
            params.weight = 0.0f;
            params.gravity = Gravity.CENTER_VERTICAL;
            mPageControlLayout.addView(mPageLabel, params);
            
            params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.weight = 1.0f;
            mPageControlLayout.addView(mSeekBar, params);
        } else {
            mPageControlLayout.setOrientation(LinearLayout.VERTICAL);
            mPageControlLayout.addView(mSeekBar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            mPageControlLayout.addView(mPageLabel, params);
        }
        
        FrameLayout.LayoutParams curlViewParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        curlViewParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.preview_controls_margin_side);
        curlViewParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.preview_controls_margin_side);
        curlViewParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.preview_controls_margin_bottom);
        curlViewParams.gravity = Gravity.BOTTOM;
        addView(mPageControlLayout, curlViewParams);
    }
    
    private void updateSeekBar() {
        int currentPage = getCurrentPage();
        int pageCount = mPdfPageProvider.getPageCount();
        
        if (!mCurlView.getAllowLastPageCurl()) {
            pageCount--;
        }
        
        mSeekBar.setMax(0);
        mSeekBar.setMax(pageCount);
        mSeekBar.setProgress(currentPage);
    }
    
    private void updateSeekBarProgress(int currentPage) {
        mSeekBar.setProgress(currentPage);
    }
    
    private void updatePageLabel() {
        final String FORMAT_ONE_PAGE_STATUS = "PAGE %d / %d";
        final String FORMAT_TWO_PAGE_STATUS = "PAGE %d-%d / %d";
        
        int currentPage = getCurrentPage();
        int pageCount = mPdfPageProvider.getPageCount();
        
        if (mCurlView.getViewMode() == CurlView.SHOW_ONE_PAGE || getCurrentPage() == 0) {
            mPageLabel.setText(String.format(Locale.getDefault(), FORMAT_ONE_PAGE_STATUS, currentPage + 1, pageCount));
        } else if (getCurrentPage() == mPdfPageProvider.getPageCount()) {
            mPageLabel.setText(String.format(Locale.getDefault(), FORMAT_ONE_PAGE_STATUS, currentPage, pageCount));
        } else {
            mPageLabel.setText(String.format(Locale.getDefault(), FORMAT_TWO_PAGE_STATUS, currentPage, currentPage + 1, pageCount));
        }
    }
    
    private void setPageControlsDisplay(boolean visible) {
        if (visible) {
            mSeekBar.setVisibility(View.VISIBLE);
            mPageLabel.setVisibility(View.VISIBLE);
        } else {
            mSeekBar.setVisibility(View.GONE);
            mPageLabel.setVisibility(View.GONE);
        }
    }
    
    private void fitCurlView(int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        
        int marginSize = getResources().getDimensionPixelSize(R.dimen.preview_view_margin);
        
        MarginLayoutParams params = (MarginLayoutParams) mPageControlLayout.getLayoutParams();
        int pageControlSize = mPageControlLayout.getHeight() + params.bottomMargin;
        
        int newDimensions[] = getScreenDimensions(w - (marginSize * 2), h - (marginSize * 2) - pageControlSize);
        
        float lrMargin = ((w - newDimensions[0]) / (w * 2.0f));
        float tbMargin = (((h - pageControlSize) - newDimensions[1]) / (h * 2.0f));
        
        lrMargin += (marginSize / (float) w);
        tbMargin += (marginSize / (float) h);
        
        /*
        mZoomLevel = 4.0f;
        
        int zoomedInDimensions[] = newDimensions.clone();
        zoomedInDimensions[0] *= mZoomLevel;
        zoomedInDimensions[1] *= mZoomLevel;
        
        float marginAdjustX = ((mZoomLevel * newDimensions[0]) - newDimensions[0]) / (float)w;
        float marginAdjustY = ((mZoomLevel * newDimensions[1]) - newDimensions[1]) / (float)h;
        
        lrMargin -= marginAdjustX;
        tbMargin -= marginAdjustY;
        */
        
        mCurlView.setMargins(lrMargin, tbMargin, lrMargin, tbMargin + (pageControlSize / (float) h));
    }
    
    // ================================================================================
    // INTERFACE - OnSeekBarChangeListener
    // ================================================================================
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mCurlView.setCurrentIndex(progress);
            updatePageLabel();
        }
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    
    // ================================================================================
    // INTERFACE - OnScaleGestureListener
    // ================================================================================
    
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mZoomLevel  = mZoomLevel * detector.getScaleFactor();
        if (mZoomLevel <= BASE_ZOOM_LEVEL) {
            mZoomLevel = BASE_ZOOM_LEVEL;
        }
        if (mZoomLevel >= MAX_ZOOM_LEVEL) {
            mZoomLevel = MAX_ZOOM_LEVEL;
        }

        setPageControlsDisplay(mZoomLevel == BASE_ZOOM_LEVEL);
        mCurlView.setZoomLevel(mZoomLevel);
        mCurlView.requestLayout();
        
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
        
        @Override
        public void indexChanged(int index) {
            updateSeekBarProgress(index);
            
            /*
            ((android.app.Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    updatePageLabel();
                }
            });
            */
        }
    }
    
    private class PDFRenderTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<CurlPage> mCurlPageRef;
        private WeakReference<Object> mHandlerRef;
        private int mWidth;
        private int mHeight;
        private int mIndex;
        private Bitmap mRenderBmps[];
        
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
                Log.w(TAG, "Thread exception received");
            }
            
            if (mHandlerRef.get() == null || mCurlPageRef.get() == null) {
                Log.w(TAG, "Cancelled process");
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
                Log.w(TAG, "Will recycle");
            }
        }
        
        private void drawStapleImages(Canvas canvas) {
            int stapleLength = convertDimension(STAPLE_LENGTH_IN_MM, canvas.getWidth());
            float scale = stapleLength / (float) mStapleBmp.getWidth();
            
            int count = mPrintSettings.getStaple().getCount();
            
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
                    } else if (mCurlView.getBindPosition() == CurlView.BIND_TOP) {
                        x = (canvas.getWidth() * (i + 1)) / (count + 1);
                        rotate = 0.0f;
                    }
                    
                    ImageUtils.renderBmpToCanvas(mStapleBmp, canvas, shouldDisplayColor(), x, y, rotate, scale);
                }
            }
        }
        
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
        
        private void drawPDFPagesOnBitmap(Bitmap bmp, int beginIndex, boolean flipX, boolean flipY) {
            // get page then draw in bitmap
            Canvas canvas = new Canvas(bmp);
            
            Imposition pagination = mPrintSettings.getImposition();
            
            int paperWidth = bmp.getWidth();
            int paperHeight = bmp.getHeight();
            
            // adjust paperWidth and paperHeight based on margins
            paperWidth -= convertDimension(mMarginLeft, bmp.getWidth());
            paperWidth -= convertDimension(mMarginRight, bmp.getWidth());
            
            paperHeight -= convertDimension(mMarginTop, bmp.getWidth());
            paperHeight -= convertDimension(mMarginBottom, bmp.getWidth());
            
            int pdfPageWidth = paperWidth / pagination.getCols();
            int pdfPageHeight = paperHeight / pagination.getRows();
            
            int beginX = 0;
            int beginY = 0;
            
            switch (mCurlView.getBindPosition()) {
                case CurlView.BIND_LEFT:
                    beginX = paperWidth - (pagination.getCols() * pdfPageWidth);
                    break;
                case CurlView.BIND_TOP:
                    beginY = paperHeight - (pagination.getRows() * pdfPageHeight);
                    break;
            }
            
            // adjust beginX and beginY based on margins
            beginX += convertDimension(mMarginLeft, bmp.getWidth());
            beginY += convertDimension(mMarginTop, bmp.getWidth());
            
            int curX = beginX;
            int curY = beginY;
            
            for (int i = 0; i < pagination.getPerPage(); i++) {
                if (mHandlerRef.get() == null || mCurlPageRef.get() == null) {
                    Log.w(TAG, "Cancelled process");
                    return;
                }
                
                int left = curX;
                int top = curY;
                int right = curX + pdfPageWidth;
                int bottom = curY + pdfPageHeight;
                
                // Left to right
                curX += pdfPageWidth;
                if (i % pagination.getCols() == pagination.getCols() - 1) {
                    curX = beginX;
                    curY += pdfPageHeight;
                }
                
                float scale = 1.0f / pagination.getPerPage();
                
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
                drawStapleImages(canvas);
                drawPunchImages(canvas);
            }
        }
        
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
            
            pagePerScreen *= mPrintSettings.getImposition().getPerPage();
            
            int frontIndex = (mIndex * pagePerScreen);
            drawPDFPagesOnBitmap(front, frontIndex, false, false);
            
            if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
                int backIndex = (mIndex * pagePerScreen) + mPrintSettings.getImposition().getPerPage();
                
                boolean verticalFlip = isVerticalFlip();
                drawPDFPagesOnBitmap(back, backIndex, !verticalFlip, verticalFlip);
            }
            
            if (mBmpCache != null) {
                mBmpCache.put(getCacheKey(mIndex, CurlPage.SIDE_FRONT), front);
                mBmpCache.put(getCacheKey(mIndex, CurlPage.SIDE_BACK), back);
            }
            
            return new Bitmap[] { front, back };
        }
    }
}
 