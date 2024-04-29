/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintPreviewView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.preview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.util.LruCache
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import fi.harism.curl.CurlPage
import fi.harism.curl.CurlView
import fi.harism.curl.CurlView.PageProvider
import jp.co.riso.android.util.AppUtils
import jp.co.riso.android.util.ImageUtils
import jp.co.riso.android.util.Logger
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.common.BaseTask
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.*
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartprint.R
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.ceil
import jp.co.riso.gestureListener.GestureDetectorListenerWrapper

/**
 * @class PrintPreviewView
 * 
 * @brief A View that displays a PDF. Uses CurlView to present the PDF.
 */
class PrintPreviewView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(
    context!!, attrs, defStyle
), OnScaleGestureListener,
    GestureDetectorListenerWrapper, OnDoubleTapListener {
    private var _curlView: CurlView? = null
    private var _pdfManager: PDFFileManager? = null
    private val _pdfPageProvider = PDFPageProvider()
    private var _printSettings: PrintSettings? = null
    private var _bmpCache: LruCache<String, Bitmap>? = null

    /*
    private Bitmap mStapleBmp = null;
    private Bitmap mPunchBmp = null;
    */
    private var _marginLeft = 0f
    private var _marginRight = 0f
    private var _marginTop = 0f
    private var _marginBottom = 0f

    /*
    private boolean mShow3Punch = false;
    */
    private var _listener: PreviewControlsListener? = null

    // Zoom/pan related variables
    private var _scaleDetector: ScaleGestureDetector? = null
    private var _doubleTapDetector: GestureDetector? = null
    private var _zoomLevel = BASE_ZOOM_LEVEL
    private var _ptrIdx = INVALID_IDX
    private val _ptrDownPos = PointF()
    private val _ptrLastPos = PointF()

    /**
     * @brief Initializes the resources needed by the PrintPreviewView
     */
    @SuppressLint("NewApi") // Prevent quick scale on kitkat devices
    fun init() {
        if (!isInEditMode) {
            _printSettings = PrintSettings() // Should not be null
            _scaleDetector = ScaleGestureDetector(context, this)
            _scaleDetector!!.isQuickScaleEnabled = false
            _doubleTapDetector = GestureDetector(context, this)
            _doubleTapDetector!!.setOnDoubleTapListener(this)
            initializeCurlView()
            loadResources()
        }
    }

    /**
     * @brief Inform the curl view that the activity is paused.
     */
    fun onPause() {
        _curlView!!.onPause()
    }

    /**
     * @brief Inform the curl view that the activity is resumed.
     */
    fun onResume() {
        _curlView!!.onResume()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!isInEditMode) {
            // Re-adjust the margin of the CurlView
            fitCurlView(l, t, r, b)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    /**
     * @brief Process panning events when PrintPreviewView is zoomed.
     *
     * @param ev The event to be processed
     *
     * @retval true Event was processed
     * @retval false Event was not processed
     */
    private fun processTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount == 1) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> if (_ptrIdx == INVALID_IDX) {
                    // BTS 20071: For two-page mode, since panning bounds already handles the limit,
                    // no need to prevent panning when user touches gray area to align with iOS behavior
                    // and prevent issues with viewHit calculation.
                    if (_curlView!!.isViewHit(
                            ev.x,
                            ev.y
                        ) || _curlView!!.viewMode == CurlView.SHOW_TWO_PAGES
                    ) {
                        _ptrIdx = ev.actionIndex
                        _ptrDownPos[ev.x] = ev.y
                        _ptrLastPos[ev.x] = ev.y
                        return true
                    }
                }
                MotionEvent.ACTION_MOVE -> if (_ptrIdx == ev.actionIndex) {
                    _curlView!!.adjustPan(ev.x - _ptrLastPos.x, ev.y - _ptrLastPos.y)
                    _curlView!!.requestRender()
                    setPans(_curlView!!.panX, _curlView!!.panY)
                    _ptrLastPos[ev.x] = ev.y
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    _ptrIdx = -1
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        _scaleDetector!!.onTouchEvent(ev)
        if (!_scaleDetector!!.isInProgress && ev.pointerCount == 1) {
            return if (_zoomLevel == BASE_ZOOM_LEVEL) {
                _curlView!!.dispatchTouchEvent(ev)
            } else {
                if (_doubleTapDetector!!.onTouchEvent(ev)) {
                    true
                } else processTouchEvent(ev)
            }
        }
        val e = MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_CANCEL,
            ev.x, ev.y, 0
        )
        processTouchEvent(e)
        _curlView!!.dispatchTouchEvent(e)
        e.recycle()
        return true
    }
    // ================================================================================
    // Public methods
    // ================================================================================
    /**
     * @brief Load Bitmap resources such as staple and punch
     */
    private fun loadResources() {
        /*
        mStapleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_staple);
        mPunchBmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_punch);
        */
    }

    /**
     * @brief Free the native object associated with the Bitmap loaded by method loadResources()
     */
    fun freeResources() {
        /*
        mStapleBmp.recycle();
        mPunchBmp.recycle();
        */
    }

    /**
     * @brief Request that the curl view renderer render a frame.
     */
    fun refreshView() {
        _curlView!!.requestRender()
    }

    /**
     * @brief Sets the PDF Manager to be used by the view
     *
     * @param pdfManager PDF Manager instance
     */
    fun setPdfManager(pdfManager: PDFFileManager?) {
        _pdfManager = pdfManager
    }

    /**
     * @brief Print Settings to be used by the View.
     *
     * @param printSettings Print settings to be applied
     */
    fun setPrintSettings(printSettings: PrintSettings?) {
        if (printSettings == null || isPrintSettingsChanged(printSettings)) {
            _printSettings = printSettings
            reconfigureCurlView()
            displayValidPage()
            _curlView!!.requestLayout()
        }
    }

    /**
     * @brief Check if it is necessary to update view
     *
     * @param printSettings Print settings to be applied
     */
    private fun isPrintSettingsChanged(printSettings: PrintSettings?): Boolean {
        return _printSettings?.bookletFinish != printSettings?.bookletFinish ||
                _printSettings?.bookletLayout != printSettings?.bookletLayout ||
                _printSettings?.colorMode != printSettings?.colorMode ||
                _printSettings?.duplex != printSettings?.duplex ||
                _printSettings?.finishingSide != printSettings?.finishingSide ||
                _printSettings?.imposition != printSettings?.imposition ||
                _printSettings?.impositionOrder != printSettings?.impositionOrder ||
                _printSettings?.inputTray  != printSettings?.inputTray  ||
                _printSettings?.orientation  != printSettings?.orientation  ||
                _printSettings?.paperSize != printSettings?.paperSize ||
                _printSettings?.punch  != printSettings?.punch  ||
                _printSettings?.sort  != printSettings?.sort  ||
                _printSettings?.staple  != printSettings?.staple  ||
                _printSettings?.isBooklet != printSettings?.isBooklet ||
                _printSettings?.isScaleToFit != printSettings?.isScaleToFit
    }

    /**
     * @brief Sets the Bitmap Cache to be used to store PDF page bitmaps
     *
     * @param bmpCache BMP cache with allocated memory
     */
    fun setBmpCache(bmpCache: LruCache<String, Bitmap>?) {
        _bmpCache = bmpCache
    }

    /**
     * @brief Sets the default margins. Default is no margin
     */
    private fun setDefaultMargins() {
        setMarginLeftInMm(DEFAULT_MARGIN_IN_MM)
        setMarginRightInMm(DEFAULT_MARGIN_IN_MM)
        setMarginTopInMm(DEFAULT_MARGIN_IN_MM)
        setMarginBottomInMm(DEFAULT_MARGIN_IN_MM)
    }

    /**
     * @brief Sets the left margin
     *
     * @param marginLeft Left margin in mm
     */
    private fun setMarginLeftInMm(marginLeft: Float) {
        _marginLeft = marginLeft
    }

    /**
     * @brief Sets the right margin
     *
     * @param marginRight Right margin in mm
     */
    private fun setMarginRightInMm(marginRight: Float) {
        _marginRight = marginRight
    }

    /**
     * @brief Sets the top margin
     *
     * @param marginTop Top margin in mm
     */
    private fun setMarginTopInMm(marginTop: Float) {
        _marginTop = marginTop
    }

    /**
     * @brief Sets the bottom margin
     *
     * @param marginBottom Bottom margin in mm
     */
    private fun setMarginBottomInMm(marginBottom: Float) {
        _marginBottom = marginBottom
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
    fun setListener(listener: PreviewControlsListener?) {
        _listener = listener
    }

    /**
     * @brief Checks if last page curl is allowed
     *
     * @retval true Last page curl is allowed
     * @retval false Last page curl is not allowed
     */
    val allowLastPageCurl: Boolean
        get() = _curlView!!.allowLastPageCurl
    /**
     * @brief Gets the current page displayed.
     *
     * @return Current CurlView index
     */
    /**
     * @brief Sets the current page displayed.
     */
    var currentPage: Int
        get() = _curlView!!.currentIndex
        set(page) {
            if (page != currentPage) {
                _curlView!!.currentIndex = page
                setZoomLevel(_zoomLevel)
            }
        }// will depend on PDF and pagination, always false for now

    /**
     * @brief Get the face count. Face count depends on the PDF
     * page count and pagination.
     *
     * @note If booklet or duplex, dependent on faces per paper
     *
     * @return Number of faces
     */
    private val _faceCount: Int
        get() {
            if (_pdfManager == null) {
                return 0
            }

            // will depend on PDF and pagination, always false for now
            var count = _pdfManager!!.pageCount
            count = ceil(count / pagesPerSheet.toDouble()).toInt()
            if (_isTwoPageDisplayed) {
                count = AppUtils.getNextIntegerMultiple(count, _facesPerPaper)
                if (_printSettings!!.isBooklet) {
                    count = AppUtils.getNextIntegerMultiple(count, _facesPerPaper)
                }
            }
            return count
        }

    /**
     * @brief Get the number of pages. Dependent on the face count and CurlView display.
     *
     * @return Page count
     */
    val pageCount: Int
        get() {
            var count = _faceCount
            if (_isTwoPageDisplayed) {
                count = ceil((count / 2.0f).toDouble()).toInt()
            }
            return count
        }

    /**
     * @brief Gets the string that shows the current page.
     *
     * @return Current page string
     */
    val pageString: String
        get() = getPageString(currentPage)

    /**
     * @brief Gets the string of the supplied current face.
     *
     * @param currentFace Current Face to get the string from
     *
     * @return Current page string
     */
    fun getPageString(currentFace: Int): String {
        var currentFace1 = currentFace
        if (_isTwoPageDisplayed) {
            currentFace1 *= 2
        }
        if (_curlView!!.viewMode == CurlView.SHOW_ONE_PAGE || currentFace1 == 0) {
            currentFace1++
        }
        val faceCount = _faceCount
        val formatOnePageStatus = resources.getString(R.string.ids_lbl_page_displayed)
        return String.format(Locale.getDefault(), formatOnePageStatus, currentFace1, faceCount)
    }

    /**
     * @brief Checks if zoom is base zoom
     *
     * @retval true Zoom level is equal to BASE_ZOOM_LEVEL
     * @retval false Zoom level is not equal to BASE_ZOOM_LEVEL
     */
    private val _isBaseZoom: Boolean
        get() = _zoomLevel == BASE_ZOOM_LEVEL
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
    private fun getCurlViewDimensions(screenWidth: Int, screenHeight: Int): IntArray {
        // Compute margins based on the paper size in preview settings.
        val paperDisplaySize = _paperDisplaySize

        // Adjust display on screen.
        if (_curlView!!.viewMode == CurlView.SHOW_TWO_PAGES) {
            if (_curlView!!.bindPosition == CurlView.BIND_TOP
                || _curlView!!.bindPosition == CurlView.BIND_BOTTOM
            ) {
                // double the height if bind == top
                paperDisplaySize[1] *= 2.0f
            } else {
                // double the width if bind != top
                paperDisplaySize[0] *= 2.0f
            }
        }
        return AppUtils.getFitToAspectRatioSize(
            paperDisplaySize[0],
            paperDisplaySize[1],
            screenWidth,
            screenHeight
        )
    }

    /**
     * @brief Get paper dimensions
     *
     * @param screenWidth Screen width
     * @param screenHeight Screen height
     *
     * @return Paper width and height
     */
    private fun getPaperDimensions(screenWidth: Int, screenHeight: Int): IntArray {
        // Compute margins based on the paper size in preview settings.
        val paperDisplaySize = _paperDisplaySize
        return AppUtils.getFitToAspectRatioSize(
            paperDisplaySize[0],
            paperDisplaySize[1],
            screenWidth,
            screenHeight
        )
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
    private fun getCacheKey(index: Int, side: Int): String {
        val buffer = StringBuffer()
        buffer.append(_pdfManager!!.path)
        buffer.append(index)
        buffer.append(side)
        buffer.append(shouldDisplayColor())
        buffer.append(_printSettings!!.isScaleToFit)
        if (!AppConstants.USE_PDF_ORIENTATION) {
            buffer.append(_printSettings!!.orientation.ordinal)
        }
        buffer.append(_printSettings!!.paperSize.ordinal)
        buffer.append(_printSettings!!.duplex.ordinal)
        buffer.append(_printSettings!!.imposition.ordinal)
        buffer.append(_printSettings!!.impositionOrder.ordinal)
        buffer.append(_printSettings!!.finishingSide.ordinal)
        /*
        buffer.append(mPrintSettings.getStaple().ordinal());
        buffer.append(mPrintSettings.getPunch().getCount(mShow3Punch));
        */buffer.append(_printSettings!!.isBooklet)
        buffer.append(_printSettings!!.bookletFinish.ordinal)
        buffer.append(_printSettings!!.bookletLayout.ordinal)
        return buffer.toString()
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
    private fun getBitmapsFromCacheForPage(index: Int): Array<Bitmap?> {
        var front: Bitmap? = null
        var back: Bitmap? = null
        if (_bmpCache != null) {
            front = _bmpCache!![getCacheKey(index, CurlPage.SIDE_FRONT)]
            back = _bmpCache!![getCacheKey(index, CurlPage.SIDE_BACK)]
        }
        if (front != null && front.isRecycled) {
            front = null
        }
        if (back != null && back.isRecycled) {
            back = null
        }
        return arrayOf(front, back)
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
    private fun convertDimension(dimension: Float, bmpWidth: Int, bmpHeight: Int): Int {
        val smallerDimension = bmpWidth.coerceAtMost(bmpHeight).toFloat()
        val paperSize = _paperDisplaySize
        val smallerPaperSize = paperSize[0].coerceAtMost(paperSize[1])
        return (dimension / smallerPaperSize * smallerDimension).toInt()
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
    private fun shouldDisplayLandscape(): Boolean {
        var flipToLandscape: Boolean
        flipToLandscape = if (AppConstants.USE_PDF_ORIENTATION) {
            _pdfManager!!.isPDFLandscape
        } else {
            _printSettings!!.orientation == Orientation.LANDSCAPE
        }
        if (!_printSettings!!.isBooklet) {
            if (_printSettings!!.imposition.isFlipLandscape) {
                flipToLandscape = !flipToLandscape
            }
        }
        return flipToLandscape
    }

    /**
     * @brief Get the paper size. Dependent on the printsettings.
     *
     * @return Paper display width and height
     */
    private val _paperDisplaySize: FloatArray
        get() {
            var width = _printSettings!!.paperSize.width
            var height = _printSettings!!.paperSize.height
            if (_printSettings!!.isBooklet) {
                width = _printSettings!!.paperSize.height / 2
                height = _printSettings!!.paperSize.width
            }
            return if (shouldDisplayLandscape()) {
                floatArrayOf(height, width)
            } else floatArrayOf(width, height)
        }

    /**
     * @brief Gets the number of pages displayed in one sheet.
     *
     * @return Pages per sheet
     */
    val pagesPerSheet: Int
        get() = if (_printSettings!!.isBooklet) {
            1
        } else _printSettings!!.imposition.perPage

    /**
     * @brief Gets the number of faces in one paper.
     *
     * @note If booklet, 4 faces can be displayed in one paper.
     *
     * @return Number of faces on a paper.
     */
    private val _facesPerPaper: Int
        get() = if (_printSettings!!.isBooklet) {
            4
        } else 2

    /**
     * @brief Gets the number of columns in one sheet.
     *
     * @return Number of columns
     */
    val colsPerSheet: Int
        get() {
            if (_printSettings!!.isBooklet) {
                return 1
            }
            if (AppConstants.USE_PDF_ORIENTATION) {
                if (_pdfManager!!.isPDFLandscape) {
                    return _printSettings!!.imposition.rows
                }
            } else {
                if (_printSettings!!.orientation == Orientation.LANDSCAPE) {
                    return _printSettings!!.imposition.rows
                }
            }
            return _printSettings!!.imposition.cols
        }

    /**
     * @brief Gets the number of rows in one sheet.
     *
     * @return Number of rows
     */
    val rowsPerSheet: Int
        get() {
            if (_printSettings!!.isBooklet) {
                return 1
            }
            if (AppConstants.USE_PDF_ORIENTATION) {
                if (_pdfManager!!.isPDFLandscape) {
                    return _printSettings!!.imposition.cols
                }
            } else {
                if (_printSettings!!.orientation == Orientation.LANDSCAPE) {
                    return _printSettings!!.imposition.cols
                }
            }
            return _printSettings!!.imposition.rows
        }

    /**
     * @brief Sets the display mode of the CurlView.
     */
    private fun setupCurlPageView() {
        var twoPage = false
        var allowLastPageCurl = false
        if (_faceCount > 1 && _isTwoPageDisplayed) {
            twoPage = true
            if (_faceCount % 2 == 0) {
                allowLastPageCurl = true
            }
        }
        if (twoPage) {
            _curlView!!.viewMode = CurlView.SHOW_TWO_PAGES
            _curlView!!.setRenderLeftPage(true)
        } else {
            _curlView!!.viewMode = CurlView.SHOW_ONE_PAGE
            _curlView!!.setRenderLeftPage(false)
        }
        _curlView!!.allowLastPageCurl = allowLastPageCurl
    }

    /**
     * @brief Check if two page display is enabled
     *
     * @retval true Two page display is enabled
     * @retval false Two page display is disabled
     */
    private val _isTwoPageDisplayed: Boolean
        get() = if (_printSettings!!.isBooklet) {
            true
        } else _printSettings!!.duplex != Duplex.OFF

    /**
     * @brief Sets the bind position of the CurlView
     */
    private fun setupCurlBind() {
        var bindPosition = CurlView.BIND_LEFT
        if (_printSettings!!.isBooklet) {
            if (!shouldDisplayLandscape()) {
                if (_printSettings!!.bookletLayout == BookletLayout.REVERSE) {
                    bindPosition = CurlView.BIND_RIGHT
                }
            } else {
                bindPosition = CurlView.BIND_TOP
                if (_printSettings!!.bookletLayout == BookletLayout.REVERSE) {
                    bindPosition = CurlView.BIND_BOTTOM
                }
            }
        } else {
            when (_printSettings?.finishingSide) {
                FinishingSide.LEFT -> {}
                FinishingSide.RIGHT -> bindPosition = CurlView.BIND_RIGHT
                FinishingSide.TOP -> bindPosition = CurlView.BIND_TOP
                else -> {}
            }
        }
        _curlView!!.bindPosition = bindPosition
    }

    /**
     * @brief Check if colored display is enabled
     *
     * @retval true Colored
     * @retval false Grayscale
     */
    private fun shouldDisplayColor(): Boolean {
        return _printSettings!!.colorMode != ColorMode.MONOCHROME
    }

    /**
     * @brief Check if display vertical flip is enabled
     *
     * @retval true Flipped vertically
     * @retval false Not flipped
     */
    private val _isVerticalFlip: Boolean
        get() {
            val verticalFlip: Boolean = when {
                _printSettings!!.isBooklet -> {
                    shouldDisplayLandscape()
                }
                shouldDisplayLandscape() -> {
                    _printSettings!!.duplex == Duplex.LONG_EDGE
                }
                else -> {
                    _printSettings!!.duplex == Duplex.SHORT_EDGE
                }
            }
            return verticalFlip
        }
    // ================================================================================
    // View-related methods
    // ================================================================================
    /**
     * @brief Initializes the CurlView display
     */
    private fun initializeCurlView() {
        _curlView = CurlView(context)
        _curlView!!.setMargins(0f, 0f, 0f, 0f)
        _curlView!!.setPageProvider(_pdfPageProvider)
        _curlView!!.bindPosition = CurlView.BIND_LEFT
        _curlView!!.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_light_2))
        reconfigureCurlView()
        if (!isInEditMode) {
            val percentage =
                resources.getFraction(R.fraction.preview_view_drop_shadow_percentage, 1, 1)
            _curlView!!.setDropShadowSize(percentage)
        }
        addView(_curlView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /**
     * @brief Re-configures the CurlView settings
     */
    fun reconfigureCurlView() {
        setupCurlPageView()
        setupCurlBind()
        setDefaultMargins()
    }

    /**
     * @brief Display the correct page to prevent out of bounds.
     */
    private fun displayValidPage() {
        if (_pdfPageProvider.pageCount > 0) {
            if (_curlView!!.viewMode == CurlView.SHOW_TWO_PAGES) {
                if (currentPage > _pdfPageProvider.pageCount) {
                    // set current page to last page if it is past the computed total page number
                    currentPage = _pdfPageProvider.pageCount
                }
            } else {
                if (currentPage + 1 > _pdfPageProvider.pageCount) {
                    // set current page to last page if it is past the computed total page number
                    currentPage = _pdfPageProvider.pageCount - 1
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
    private fun fitCurlView(l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val h = b - t
        val marginSize = resources.getDimensionPixelSize(R.dimen.preview_view_margin)
        var pageControlSize = 0
        if (_listener != null) {
            pageControlSize = _listener!!.controlsHeight
        }
        val newDimensions =
            getCurlViewDimensions(w - marginSize * 2, h - marginSize * 2 - pageControlSize)
        var lrMargin = (w - newDimensions[0]) / (w * 2.0f)
        var tbMargin = (h - pageControlSize - newDimensions[1]) / (h * 2.0f)
        lrMargin += marginSize / w.toFloat()
        tbMargin += marginSize / h.toFloat()
        _curlView!!.setMargins(
            lrMargin,
            tbMargin,
            lrMargin,
            tbMargin + pageControlSize / h.toFloat()
        )
    }

    /**
     * @brief Set the current panning of the Preview
     *
     * @param panX Horizontal Pan
     * @param panY Vertical Pan
     */
    fun setPans(panX: Float, panY: Float) {
        if (_listener != null) {
            _listener!!.panChanged(panX, panY)
        }
        _curlView!!.setPans(panX, panY)
        _curlView!!.requestRender()
    }

    /**
     * @brief Zooms-in the currently displayed Preview
     */
    fun zoomIn() {
        setZoomLevel(_zoomLevel + 1.0f)
    }

    /**
     * @brief Zooms-out the currently displayed Preview
     */
    fun zoomOut() {
        setZoomLevel(_zoomLevel - 1.0f)
    }

    /**
     * @brief Set the zoom level of the Preview
     *
     * @param zoomLevel New zoom level.
     */
    fun setZoomLevel(zoomLevel: Float) {
        _zoomLevel = zoomLevel
        if (_zoomLevel <= BASE_ZOOM_LEVEL) {
            _zoomLevel = BASE_ZOOM_LEVEL
        }
        if (_zoomLevel >= MAX_ZOOM_LEVEL) {
            _zoomLevel = MAX_ZOOM_LEVEL
        }
        if (_listener != null) {
            _listener!!.zoomLevelChanged(_zoomLevel)
            _listener!!.setControlsEnabled(_isBaseZoom)
        }
        _curlView!!.setZoomLevel(_zoomLevel)
        _curlView!!.requestRender()
    }

    /**
     * @brief Animates a zoom to a specified zoom level.
     *
     * @note Animation Speed: 0.25 seconds
     *
     * @param zoomLevel Target zoom level.
     */
    private fun animateZoomTo(zoomLevel: Float) {
        val duration = 250f
        val currentTime = System.currentTimeMillis()
        val initialZoom = _zoomLevel
        val targetActivity = context as Activity
        val thread = Thread {
            while (System.currentTimeMillis() - currentTime < duration) {
                val percentage = (System.currentTimeMillis() - currentTime) / duration
                _curlView!!.setZoomLevel(initialZoom + (zoomLevel - initialZoom) * percentage)
                _curlView!!.requestRender()
                Thread.yield()
            }
            targetActivity.runOnUiThread { setZoomLevel(zoomLevel) }
            //setZoomLevel(targetZoom);
        }
        thread.start()
    }

    // ================================================================================
    // INTERFACE - OnDoubleTapListener
    // ================================================================================
    override fun onDoubleTap(e: MotionEvent): Boolean {
        animateZoomTo(BASE_ZOOM_LEVEL)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return false
    }

    /* Android 13 New OS Support - Move interface to its Java wrapper GestureDetectorListenerWrapper
    // ================================================================================
    // INTERFACE - OnGestureListener
    // ================================================================================
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }*/

    // ================================================================================
    // INTERFACE - OnScaleGestureListener
    // ================================================================================
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        setZoomLevel(_zoomLevel * detector.scaleFactor)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        // Return true to begin scale
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {}
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class PDFPageProvider
     *
     * @brief Provider class for the PrintPreviewView
     */
    private inner class PDFPageProvider : PageProvider {
        override fun getPageCount(): Int {
            var count = _faceCount
            if (_isTwoPageDisplayed) {
                count = ceil((count / 2.0f).toDouble()).toInt()
            }
            return count
        }

        override fun updatePage(page: CurlPage, width: Int, height: Int, index: Int) {
            page.setColor(ContextCompat.getColor(context, R.color.bg_paper), CurlPage.SIDE_FRONT)
            page.setColor(ContextCompat.getColor(context, R.color.bg_paper), CurlPage.SIDE_BACK)
            val isFromCache = true
            val cachedPages: Array<Bitmap?>
            if (isFromCache && _curlView!!.mUseCachedBitmaps) {
                cachedPages = getBitmapsFromCacheForPage(index)
            } else {
                _curlView!!.mUseCachedBitmaps = true
                val task = PDFRenderTask(page, width, height, index, page.createNewHandler())
                cachedPages = task.renderBitmaps
            }
            if (cachedPages[0] == null || cachedPages[1] == null) {
                val front = Bitmap.createBitmap(SMALL_BMP_SIZE, SMALL_BMP_SIZE, BMP_CONFIG_TEXTURE)
                val back = Bitmap.createBitmap(SMALL_BMP_SIZE, SMALL_BMP_SIZE, BMP_CONFIG_TEXTURE)
                front.eraseColor(Color.WHITE)
                back.eraseColor(Color.WHITE)
                page.setTexture(front, CurlPage.SIDE_FRONT)
                page.setTexture(back, CurlPage.SIDE_BACK)
                PDFRenderTask(page, width, height, index, page.createNewHandler()).execute()
            } else {
                page.setTexture(Bitmap.createBitmap(cachedPages[0]!!), CurlPage.SIDE_FRONT)
                page.setTexture(Bitmap.createBitmap(cachedPages[1]!!), CurlPage.SIDE_BACK)
            }
        }

        override fun indexChanged(index: Int) {
            if (_listener != null) {
                _listener!!.onIndexChanged(index)
            }
        }
    }

    /**
     * @class PDFRenderTask
     *
     * @brief Background task which performs rendering of PDF pages.
     */
    private inner class PDFRenderTask(
        page: CurlPage?,
        width: Int,
        height: Int,
        index: Int,
        handler: Any?
    ) : BaseTask<Void?, Void?>() {

        private val _curlPageRef: WeakReference<CurlPage?> = WeakReference(page)
        private val _handlerRef: WeakReference<Any?> = WeakReference(handler)
        private val _width: Int = width
        private val _height: Int = height
        private val _index: Int = index
        private lateinit var _renderBmps: Array<Bitmap?>

        override fun doInBackground(vararg params: Void?): Void? {
            try {
                Thread.sleep(SLEEP_DELAY.toLong())
            } catch (e: InterruptedException) {
                Logger.logWarn(PrintPreviewView::class.java, "Thread exception received")
            }
            if (_handlerRef.get() == null || _curlPageRef.get() == null) {
                Logger.logWarn(PrintPreviewView::class.java, "Cancelled process")
                return null
            }
            _renderBmps = renderBitmaps
            return null
        }

        override fun onPostExecute(result: Void?) {
            if (_handlerRef.get() != null && _curlPageRef.get() != null) {
                _curlPageRef.get()!!.reset()
                _curlPageRef.get()!!
                    .setTexture(Bitmap.createBitmap(_renderBmps[0]!!), CurlPage.SIDE_FRONT)
                _curlPageRef.get()!!
                    .setTexture(Bitmap.createBitmap(_renderBmps[1]!!), CurlPage.SIDE_BACK)
                _curlView!!.requestRender()
            } else {
                Logger.logWarn(PrintPreviewView::class.java, "Will recycle")
            }
        }

        /**
         * @brief Draw crease to the canvas
         *
         * @param canvas Canvas to draw the crease onto.
         */
        private fun drawCrease(canvas: Canvas) {
            val paint = Paint()
            paint.strokeWidth = CREASE_SIZE
            var fromX = 0
            var fromY = 0
            var toX = canvas.width - 1
            var toY = canvas.height - 1
            when (_curlView!!.bindPosition) {
                CurlView.BIND_LEFT -> toX = fromX
                CurlView.BIND_RIGHT -> fromX = toX
                CurlView.BIND_TOP -> toY = fromY
                CurlView.BIND_BOTTOM -> fromY = toY
            }
            paint.style = Paint.Style.STROKE
            paint.color = ContextCompat.getColor(context, R.color.theme_light_1)
            canvas.drawLine(fromX.toFloat(), fromY.toFloat(), toX.toFloat(), toY.toFloat(), paint)
            paint.style = Paint.Style.STROKE
            paint.color = ContextCompat.getColor(context, R.color.theme_light_4)
            paint.pathEffect = DashPathEffect(
                floatArrayOf(
                    CREASE_DARK_LENGTH,
                    CREASE_WHITE_LENGTH
                ), 0F
            )
            canvas.drawLine(fromX.toFloat(), fromY.toFloat(), toX.toFloat(), toY.toFloat(), paint)
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
        private fun getBeginPositions(
            paperWidth: Int,
            paperHeight: Int,
            pageWidth: Int,
            pageHeight: Int
        ): IntArray {
            var beginX = 0
            var beginY = 0
            when (_curlView!!.bindPosition) {
                CurlView.BIND_LEFT -> beginX = paperWidth - colsPerSheet * pageWidth
                CurlView.BIND_TOP -> beginY = paperHeight - rowsPerSheet * pageHeight
            }
            return intArrayOf(beginX, beginY)
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
        private fun drawPDFPagesOnBitmap(
            bmp: Bitmap,
            beginIndex: Int,
            drawCrease: Boolean,
            flipX: Boolean,
            flipY: Boolean
        ) {
            // get page then draw in bitmap
            val canvas = Canvas(bmp)

            //int pagesDisplayed = getPagesPerSheet() * (get_facesPerPaper() / 2);
            var paperWidth = bmp.width
            var paperHeight = bmp.height

            // adjust paperWidth and paperHeight based on margins
            paperWidth -= convertDimension(_marginLeft, bmp.width, bmp.height)
            paperWidth -= convertDimension(_marginRight, bmp.width, bmp.height)
            paperHeight -= convertDimension(_marginTop, bmp.width, bmp.height)
            paperHeight -= convertDimension(_marginBottom, bmp.width, bmp.height)
            val pdfPageWidth = paperWidth / colsPerSheet
            val pdfPageHeight = paperHeight / rowsPerSheet
            val beginPos = getBeginPositions(paperWidth, paperHeight, pdfPageWidth, pdfPageHeight)

            // adjust beginX and beginY based on margins
            beginPos[0] += convertDimension(_marginLeft, bmp.width, bmp.height)
            beginPos[1] += convertDimension(_marginTop, bmp.width, bmp.height)
            var leftToRight = true
            var topToBottom = true
            var horizontalFlow = true
            if (!_printSettings!!.isBooklet) {
                leftToRight = _printSettings!!.impositionOrder.isLeftToRight
                topToBottom = _printSettings!!.impositionOrder.isTopToBottom
                horizontalFlow = _printSettings!!.impositionOrder.isHorizontalFlow

                // Flip when rendering back page.
                if (flipX) {
                    leftToRight = !leftToRight
                }
                if (flipY) {
                    topToBottom = !topToBottom
                }
            }

            // start from right
            if (!leftToRight) {
                val actualWidthPixels = colsPerSheet * pdfPageWidth
                beginPos[0] = beginPos[0] + actualWidthPixels - pdfPageWidth
            }
            if (!topToBottom) {
                val actualHeightPixels = rowsPerSheet * pdfPageHeight
                beginPos[1] = beginPos[1] + actualHeightPixels - pdfPageHeight
            }
            var curX = beginPos[0]
            var curY = beginPos[1]
            for (i in 0 until pagesPerSheet) {
                if (_handlerRef.get() == null || _curlPageRef.get() == null) {
                    Logger.logWarn(PrintPreviewView::class.java, "Cancelled process")
                    return
                }
                val left = curX
                val top = curY
                val right = curX + pdfPageWidth
                val bottom = curY + pdfPageHeight
                var addX = pdfPageWidth.toFloat()
                if (!leftToRight) {
                    addX = -pdfPageWidth.toFloat()
                }
                var addY = pdfPageHeight.toFloat()
                if (!topToBottom) {
                    addY = -pdfPageHeight.toFloat()
                }
                if (horizontalFlow) {
                    curX += addX.toInt()
                    if (i % colsPerSheet == colsPerSheet - 1) {
                        curX = beginPos[0]
                        curY += addY.toInt()
                    }
                } else {
                    curY += addY.toInt()
                    if (i % rowsPerSheet == rowsPerSheet - 1) {
                        curY = beginPos[1]
                        curX += addX.toInt()
                    }
                }
                val scale = 1.0f / pagesPerSheet
                val curIndex = i + beginIndex
                val page = _pdfManager!!.getPageBitmap(curIndex, scale, flipX, flipY)
                if (page != null) {
                    var dim = intArrayOf(
                        convertDimension(
                            _pdfManager!!.getPageWidth(curIndex),
                            canvas.width,
                            canvas.height
                        ),
                        convertDimension(
                            _pdfManager!!.getPageHeight(curIndex),
                            canvas.width,
                            canvas.height
                        )
                    )

                    // Ver.2.0.4.7 Start
                    // approximate calculation doesn't need. When Nup is on, scale to fit.(2017.01.13 RISO Saito)
                    //dim[0] = (int) Math.sqrt((dim[0] * dim[0] / (float)pagesDisplayed));
                    //dim[1] = (int) Math.sqrt((dim[1] * dim[1] / (float)pagesDisplayed));
                    // Ver.2.0.4.7 End
                    var x = left
                    var y = top
                    var rotate = 0.0f
                    val shouldRotate =
                        right - left > bottom - top != _pdfManager!!.getPageWidth(curIndex) > _pdfManager!!.getPageHeight(
                            curIndex
                        )
                    if (shouldRotate) {
                        y = bottom
                        rotate = -90.0f
                        if (flipX) {
                            rotate += 180.0f
                        }
                        if (flipY) {
                            rotate += 180.0f
                        }
                    }
                    var width = right - left
                    var height = bottom - top
                    if (shouldRotate) {
                        width = bottom - top
                        height = right - left
                    }

                    // Ver.2.0.4.7 Start
                    // If images are protruded, scale to fit.(when 2up or 4up) (2017.01.13 RISO Saito)
                    //if (mPrintSettings.isScaleToFit() ) {
                    //if (mPrintSettings.isScaleToFit() || ( getPagesPerSheet() != 1 && dim[0] > width ) || ( getPagesPerSheet() != 1 && dim[1] > height)) {
                    // When Nup or Booklet is on, scale to fit.(2017.01.16 RISO Saito)
                    if (_printSettings!!.isScaleToFit || pagesPerSheet != 1 || _printSettings!!.isBooklet) {
                        // Ver.2.0.4.7 End
                        dim = AppUtils.getFitToAspectRatioSize(
                            _pdfManager!!.getPageWidth(curIndex),
                            _pdfManager!!.getPageHeight(curIndex),
                            width,
                            height
                        )
                        // For Fit-XY
                        //dim[0] = width;
                        //dim[1] = height;
                        if (shouldRotate) {
                            x += (height - dim[1]) / 2
                            y -= (width - dim[0]) / 2
                        } else {
                            x += (width - dim[0]) / 2
                            y += (height - dim[1]) / 2
                        }
                    } else {
                        if (shouldRotate) {
                            // Adjust x and y position when flipped
                            if (flipX) {
                                x -= dim[1] - height
                            }
                            if (flipY) {
                                y += dim[0] - width
                            }
                        } else {
                            // Adjust x and y position when flipped
                            if (flipX) {
                                x -= dim[0] - width
                            }
                            if (flipY) {
                                y -= dim[1] - height
                            }
                        }
                    }
                    canvas.save()
                    canvas.clipRect(left, top, right + 1, bottom + 1)

                    /*
                    Rect destRect = new Rect(x, y, x + dim[0], y + dim[1]);
                    ImageUtils.renderBmpToCanvas(page, canvas, shouldDisplayColor(), x, y, destRect);
                    */
                    // Adjust x and y for rotation purposes
                    if (shouldRotate) {
                        x += dim[1] / 2
                        y -= dim[0] / 2
                    } else {
                        x += dim[0] / 2
                        y += dim[1] / 2
                    }
                    val drawScale = dim[0] / page.width.toFloat()
                    ImageUtils.renderBmpToCanvas(
                        page,
                        canvas,
                        shouldDisplayColor(),
                        x,
                        y,
                        rotate,
                        drawScale
                    )
                    canvas.restore()
                    page.recycle()
                }

                // Draw staple and punch images
                if (drawCrease) {
                    drawCrease(canvas)
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
        val renderBitmaps: Array<Bitmap?>
            get() {
                val dim = getPaperDimensions(_width, _height)
                val front = Bitmap.createBitmap(dim[0], dim[1], BMP_CONFIG_TEXTURE)
                val back = Bitmap.createBitmap(dim[0], dim[1], BMP_CONFIG_TEXTURE)
                front.eraseColor(Color.WHITE)
                back.eraseColor(Color.WHITE)
                var pagePerScreen = 1
                if (_curlView!!.viewMode == CurlView.SHOW_TWO_PAGES) {
                    pagePerScreen = 2
                }
                pagePerScreen *= pagesPerSheet
                val frontIndex = _index * pagePerScreen
                var drawCrease = _index != 0
                drawCrease = drawCrease && _curlView!!.viewMode == CurlView.SHOW_TWO_PAGES
                drawPDFPagesOnBitmap(front, frontIndex, drawCrease, flipX = false, flipY = false)
                if (_curlView!!.viewMode == CurlView.SHOW_TWO_PAGES) {
                    val backIndex = _index * pagePerScreen + pagesPerSheet
                    val verticalFlip = _isVerticalFlip
                    drawPDFPagesOnBitmap(back, backIndex, false, !verticalFlip, verticalFlip)
                }
                if (_bmpCache != null) {
                    _bmpCache!!.put(getCacheKey(_index, CurlPage.SIDE_FRONT), front)
                    _bmpCache!!.put(getCacheKey(_index, CurlPage.SIDE_BACK), back)
                }
                return arrayOf(front, back)
            }

    }

    companion object {
        private const val DEFAULT_MARGIN_IN_MM = 0f
        private const val CREASE_SIZE = 1.0f
        private const val CREASE_DARK_LENGTH = 8.0f
        private const val CREASE_WHITE_LENGTH = 4.0f

        /*
    private static final float PUNCH_DIAMETER_IN_MM = 8;
    private static final float PUNCH_POS_SIDE_IN_MM = 8;
    
    private static final float STAPLE_LENGTH_IN_MM = 12;
    
    private static final float STAPLE_POS_CORNER_IN_MM = 6;
    private static final float STAPLE_POS_SIDE_IN_MM = 4;
    */
        /// Base and minimum zoom level of the preview
        const val BASE_ZOOM_LEVEL = 1.0f

        /// Maximum zoom level of the preview
        const val MAX_ZOOM_LEVEL = 10.0f
        private const val INVALID_IDX = -1
        private const val SLEEP_DELAY = 100
        private const val SMALL_BMP_SIZE = 64
        private val BMP_CONFIG_TEXTURE = Bitmap.Config.RGB_565
    }
    /**
     * @brief Constructs a new PrintPreviewView with layout parameters and a default style.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     * @param defStyle The default style resource ID
     */
    /**
     * @brief Constructs a new PrintPreviewView with layout parameters.
     *
     * @param context A Context object used to access application assets
     * @param attrs An AttributeSet passed to our parent
     */
    /**
     * @brief Constructs a new PrintPreviewView with a Context object
     *
     * @param context A Context object used to access application assets
     */
    init {
        init()
    }
}