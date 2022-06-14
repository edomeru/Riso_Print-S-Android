/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintJobsView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.jobs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.smartdeviceapp.model.PrintJob
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsLayoutListener
import jp.co.riso.smartprint.R
import java.lang.ref.WeakReference

/**
 * @class PrintJobsView
 *
 * @brief Custom view for the Print Job History view
 */
class PrintJobsView : LinearLayout, PrintJobsLayoutListener {
    private var _listenerRef: WeakReference<PrintJobsViewListener?>? = null
    private var _printJobs: MutableList<PrintJob> = ArrayList()
    private var _printers: MutableList<Printer> = ArrayList()
    private val _collapsedPrinters: MutableList<Printer> = ArrayList()
    private val _columns: MutableList<LinearLayout?> = ArrayList()
    private val _printGroupViews: MutableList<PrintJobsGroupView?> = ArrayList()
    private var _printGroupWithDelete: PrintJobsGroupView? = null
    private var _deleteAnimation: DisplayDeleteAnimation? = null
    private var _groupListener: PrintJobsGroupListener? = null
    private var _printJobToDelete: PrintJob? = null
    private var _printerToDelete: Printer? = null
    private var _runnable: Runnable? = null
    private var _groupViewCtr = 0
    private var _initialFlag = false
    override var isDeleteMode = false
        private set
    private var _deleteView: View? = null
    private var _downPoint: Point? = null
    private lateinit var _columnsHeight: IntArray
    private var _thread: ViewCreationThread? = null

    /**
     * @brief Default Constructor
     *
     * @param context Activity context
     * @param attrs AttributeSet
     * @param defStyle Default Style
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    /**
     * @brief Default Constructor
     *
     * @param context Activity context
     * @param attrs AttributeSet
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * @brief Default Constructor
     *
     * @param context Activity context
     */
    constructor(context: Context?) : super(context) {
        init()
    }

    /**
     * @brief Sets Data for PrintJobsView and resets view according to this data.
     *
     * @param printJobs List of print jobs
     * @param printers List of printer objects
     * @param groupListener PrintJobsGroupListener
     * @param viewListener PrintJobsViewListener
     */
    fun setData(
        printJobs: List<PrintJob>?,
        printers: List<Printer>?,
        groupListener: PrintJobsGroupListener?,
        viewListener: PrintJobsViewListener?
    ) {
        _printJobs = ArrayList(printJobs!!)
        _printers = ArrayList(printers!!)
        _groupListener = groupListener
        _listenerRef = WeakReference(viewListener)
        reset()
    }

    /**
     * @brief Begins delete mode, i.e. displays the delete button.
     *
     * @param pj Print jobs group view containing the delete button to be displayed
     * @param view Job row layout view containing the delete button to be displayed
     * @param animate If true, delete button is displayed with animation
     */
    private fun beginDelete(pj: PrintJobsGroupView?, view: View?, animate: Boolean) {
        if (!isDeleteMode) {
            isDeleteMode = true
            _deleteAnimation!!.beginDeleteModeOnView(
                view!!,
                animate,
                R.id.printJobDeleteBtn,
                R.id.printJobDate
            )
            _deleteView = view
            pj!!.setDeleteButton(view)
            _printGroupWithDelete = pj
        }
    }

    /**
     * @brief Ends delete mode, i.e. hides the delete button.
     *
     * @param animate If true, delete button is hidden with animation
     */
    fun endDelete(animate: Boolean) {
        if (isDeleteMode) {
            isDeleteMode = false
            _deleteAnimation!!.endDeleteMode(
                _deleteView!!,
                animate,
                R.id.printJobDeleteBtn,
                R.id.printJobDate
            )
            _deleteView = null
            _printGroupWithDelete!!.clearDeleteButton()
        }
    }

    /**
     * @brief Resets the PrintJobsView.
     */
    fun reset() {
        isDeleteMode = false
        _groupViewCtr = 0
        _printGroupViews.clear()
        _initialFlag = true
        removeAllViews()
        _columns.clear()
        groupPrintJobs()
    }

    /**
     * @brief Sets print job to be deleted.
     *
     * @param job PrintJob to be deleted
     */
    fun setJobToDelete(job: PrintJob?) {
        _printJobToDelete = job
    }

    /**
     * @brief Set print job group to be deleted.
     *
     * @param printer Printer of the job group to be deleted
     */
    fun setPrinterToDelete(printer: Printer?) {
        _printerToDelete = printer
    }

    /**
     * @brief Set collapse state of the print job group.
     *
     * @param printer Printer object of the print job group to be collapsed/expanded
     * @param isCollapsed Collapse state
     */
    fun setCollapsedPrinters(printer: Printer, isCollapsed: Boolean) {
        if (isCollapsed) {
            _collapsedPrinters.add(printer)
        } else {
            _collapsedPrinters.remove(printer)
        }
    }

    /**
     * @brief Deletes PrintJob from list.
     *
     * @param job PrintJob to be deleted
     */
    fun deleteJobFromList(job: PrintJob) {
        _printJobs.remove(job)
    }

    /**
     * @brief Deletes printer from list
     *
     * @param printer Printer to be deleted
     */
    fun deletePrinterFromList(printer: Printer) {
        _printers.remove(printer)
    }

    /**
     * @brief Initializes PrintJobsView.
     */
    private fun init() {
        _deleteAnimation = DisplayDeleteAnimation()
        _runnable = AddViewRunnable()
    }

    /**
     * @brief Creates a thread for grouping print jobs and creating views for each group
     */
    private fun groupPrintJobs() {
        if (_thread != null && _thread!!.isAlive) {
            _thread!!.interrupt()
            _thread!!.setIsRunning(false)
        }
        _thread = ViewCreationThread()
        _thread!!.start()
        // RM#942 thread join need to prevent race condition which causes print job groups to be doubled
        try {
            _thread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }// initially assign to 1st column

    /**
     * @brief Gets the column with the least height.
     *
     * @return Index of the smallest column
     */
    private val smallestColumn: Int
        get() {
            // initially assign to 1st column
            var smallestColumn = 0
            var tempHeight = _columnsHeight[smallestColumn]
            for (i in 1 until _columnsHeight.size) {
                if (_columnsHeight[i] < tempHeight) {
                    tempHeight = _columnsHeight[i]
                    smallestColumn = i
                }
            }
            return smallestColumn
        }

    /**
     * @brief Creates PrintJobsGroupView using printer and print jobs.
     *
     * @param jobsList List of the PrintJobs as the items of the print job group
     * @param printer Printer object as the header of the print job group
     */
    private fun createPrintJobsView(
        jobsList: List<PrintJob>,
        printer: Printer
    ): PrintJobsGroupView {
        val pjView = PrintJobsGroupView(context)
        pjView.setData(jobsList, printer, _groupListener, this@PrintJobsView)
        restoreUIstate(pjView, printer)
        return pjView
    }

    /**
     * @brief Restores the UI state of a PrintJobsGroupView.
     *
     * @param pj PrintJobsGroupView to be restored
     * @param printer Printer to be restored
     */
    private fun restoreUIstate(pj: PrintJobsGroupView, printer: Printer) {
        val isCollapsed = _collapsedPrinters.contains(printer)
        val isDeleteShown = _printJobToDelete != null && _printJobs.contains(_printJobToDelete)
        if (isDeleteShown) {
            val v = pj.findViewWithTag<View>(_printJobToDelete)
            if (v != null) {
                beginDelete(pj, v, false)
                pj.restoreState(isCollapsed, _printerToDelete, _printJobToDelete)
            } else {
                pj.restoreState(isCollapsed, _printerToDelete, null)
            }
        } else {
            pj.restoreState(isCollapsed, _printerToDelete, null)
        }
    }

    /**
     * @brief Adds PrintJobsGroupView in columns.
     *
     * @param pjView PrintJobsGroupView to be added
     */
    private fun placeInColumns(pjView: PrintJobsGroupView?) {
        val padding = resources.getDimensionPixelSize(R.dimen.printjob_padding_side)
        val groupParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        if (resources.getBoolean(R.bool.is_tablet)) {
            groupParams.topMargin = resources.getDimensionPixelSize(R.dimen.printjob_margin_top)
            pjView!!.setPadding(padding, 0, padding, 0)
        }
        pjView!!.orientation = VERTICAL
        val col = smallestColumn
        // RM#942 safety check when accessing columns to prevent crash of screen or app
        // also check if total print job groups already equal to number of printers to prevent doubling of jobs
        if (_columns.size > col && totalPrintJobGroups < _printers.size) {
            _columns[col]!!.addView(pjView, groupParams)
            _columnsHeight[col] += pjView.groupHeight + groupParams.topMargin // update column height
        }
    }

    /**
     * @brief Get total number of print job groups
     *
     * @return total number of print job groups already assigned to columns
     */
    private val totalPrintJobGroups: Int
        get() {
            var total = 0
            for (i in _columns.indices) {
                total += _columns[i]!!.childCount
            }
            return total
        }

    /**
     * @brief Adds PrintJobsGroupView in columns.
     */
    private fun addViewsToColumns() {
        for (i in _printGroupViews.indices) {
            placeInColumns(_printGroupViews[i])
        }
        _groupViewCtr = _printers.size - 1
        post(_runnable)
    }

    /**
     * @brief Re-layouts PrintJobsGroupViews in columns.
     */
    private fun relayoutColumns() {
        if (checkIfNeedsRelayout()) {
            for (i in _columns.indices) {
                _columns[i]!!.removeAllViews()
                _columnsHeight[i] = 0 // clear columns height
            }
            _groupViewCtr = 0
            addViewsToColumns()
        }
    }

    /**
     * @brief Checks if there is a need to re-layout after deletion of a PrintJobsGroupView.
     */
    private fun checkIfNeedsRelayout(): Boolean {
        var isColumnCleared = false
        var isLeftCleared = false
        val childNumExceeds: Boolean
        var childrenNum = 0
        for (i in _columns.indices) {
            isColumnCleared = isColumnCleared or (_columns[i]!!.height == 0)
            childrenNum += _columns[i]!!.childCount
        }
        childNumExceeds = childrenNum >= _columns.size
        if (!childNumExceeds) {
            for (i in 0 until childrenNum) {
                isLeftCleared = isLeftCleared or (_columns[i]!!.height == 0)
            }
        }
        return isColumnCleared && (childNumExceeds || isLeftCleared)
    }

    /**
     * @brief Creates column layouts as containers of PrintJobsGroupView.
     *
     * @param width Size of width available for Print Jobs View
     */
    private fun createColumns(width: Int) {
        var colNum = 1
        var columnWidth = context.resources.getDimensionPixelSize(R.dimen.printers_view_width)
        if (resources.getBoolean(R.bool.is_tablet)) {
            // if tablet get number of columns based on whole width
            colNum = (width / columnWidth).coerceAtLeast(MIN_COLUMNS)
            if (colNum == MIN_COLUMNS) {
                // adjust column width depending on whole width
                columnWidth = (width / MIN_COLUMNS).coerceAtMost(columnWidth)
            }
        }
        _columnsHeight = IntArray(colNum)
        for (i in 0 until colNum) {
            val columnParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            _columns.add(LinearLayout(context))
            _columns[i]!!.orientation = VERTICAL

            // if tablet set column width
            if (colNum > 1) {
                columnParams.width = columnWidth
            }
            addView(_columns[i], columnParams)
        }
    }

    /**
     * @brief Checks for a valid swipe.
     *
     * @param ev MotionEvent with action = ACTION_MOVE
     *
     * @retval true if valid swipe
     * @retval false if not a valid swipe
     */
    private fun checkSwipe(ev: MotionEvent): Boolean {
        // if swipe to right end delete mode
        if (ev.rawX - _downPoint!!.x > SWIPE_THRESHOLD) {
            endDelete(true)
            return false
        }
        val coords = IntArray(2)
        val dragged = _downPoint!!.x - ev.rawX > SWIPE_THRESHOLD
        var contains1: Boolean
        var contains2: Boolean
        // check self, if valid swipe don't redisplay nor remove delete button
        if (isDeleteMode) {
            _deleteView!!.getLocationOnScreen(coords)
            val rect = Rect(
                coords[0],
                coords[1],
                coords[0] + _deleteView!!.width,
                coords[1] + _deleteView!!.height
            )
            contains1 = rect.contains(_downPoint!!.x, _downPoint!!.y)
            contains2 = rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
            return contains1 && contains2 && dragged
        }
        for (i in _columns.indices) {
            val column = _columns[i]
            if (column != null) {
                column.getLocationOnScreen(coords)
                val rect =
                    Rect(coords[0], coords[1], coords[0] + column.width, coords[1] + column.height)
                contains1 = rect.contains(_downPoint!!.x, _downPoint!!.y)
                contains2 = rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
                if (contains1 && contains2 && dragged) {
                    for (j in 0 until column.childCount) {
                        val view = (column.getChildAt(j) as PrintJobsGroupView).getJobViewSwiped(
                            _downPoint!!, ev
                        )
                        if (view != null) {
                            beginDelete(column.getChildAt(j) as PrintJobsGroupView, view, true)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    /**
     * @brief Process motion events to detect swipe.
     *
     * @param ev MotionEvent
     *
     * @retval true if valid swipe
     * @retval false if not a valid swipe
     */
    private fun processSwipe(ev: MotionEvent): Boolean {
        var ret = false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> _downPoint = Point(
                ev.rawX.toInt(), ev.rawY.toInt()
            )
            MotionEvent.ACTION_MOVE -> ret = checkSwipe(ev)
        }
        return ret
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (_initialFlag) {
            val screenSize = getScreenDimensions(context as Activity)
            createColumns(screenSize!!.x)
            _initialFlag = false
        }
        //        if (mGroupViewCtr < mPrintGroupViews.size()) {
//            addViewsToColumns();
//        } else 
        if (_columns.size > 1 && _groupViewCtr > _printers.size - 1) {
            // if multiple columns and after deletion of a PrintJobsGroupView
            relayoutColumns()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val coords = IntArray(2)
        if (isDeleteMode) {
            if (_deleteView != null) {
                val deleteButton = _deleteView!!.findViewById<View>(R.id.printJobDeleteBtn)
                if (deleteButton != null) {
                    deleteButton.getLocationOnScreen(coords)
                    val rect = Rect(
                        coords[0],
                        coords[1],
                        coords[0] + deleteButton.width,
                        coords[1] + deleteButton.height
                    )
                    // intercept if touched item is not the delete button
                    if (rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                            _downPoint = Point(
                                ev.rawX.toInt(), ev.rawY.toInt()
                            )
                        }
                        return super.onInterceptTouchEvent(ev)
                    }
                }
                // intercept and clear delete button if ACTION_DOWN on different item
                if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                    _deleteView!!.getLocationOnScreen(coords)
                    val rect = Rect(
                        coords[0],
                        coords[1],
                        coords[0] + _deleteView!!.width,
                        coords[1] + _deleteView!!.height
                    )
                    if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        endDelete(true)
                        return true
                    }
                }
            }
            // check if swipe on same item
            val swipe = processSwipe(ev)
            if (swipe) {
                return true
            }
            if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
                endDelete(true)
                return true
            }
        } else {
            val swipe = processSwipe(ev)
            if (swipe) {
                return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return super.onTouchEvent(ev)
    }

    // ================================================================================
    // INTERFACE - PrintJobsLayoutListener
    // ================================================================================
    override fun deletePrintJobsGroup(printJobsGroupView: PrintJobsGroupView?) {
        _printGroupViews.remove(printJobsGroupView)
    }

    override fun animateGroups(
        printJobsGroupView: PrintJobsGroupView?,
        totalHeight: Int,
        durationMultiplier: Float,
        down: Boolean
    ) {
        var idx = _printGroupViews.indexOf(printJobsGroupView)
        var column = 0
        for (i in _columns.indices) {
            for (j in 0 until _columns[i]!!.childCount) {
                if (_columns[i]!!.getChildAt(j) == printJobsGroupView) {
                    idx = j // get index position in column of collapsed/expanded group
                    column = i // get column of collapsed/expanded group
                    break
                }
            }
        }
        var animation: TranslateAnimation
        for (i in idx + 1 until _columns[column]!!.childCount) {
            if (down) {
                animation = TranslateAnimation(0F, 0F, (-totalHeight).toFloat(), 0F)
                animation.startOffset = (resources.getDimensionPixelSize(R.dimen.printjob_row_height) * durationMultiplier).toLong()
            } else {
                animation = TranslateAnimation(0F, 0F, 0F, (-totalHeight).toFloat())
            }
            animation.duration = printJobsGroupView!!.getAnimationDuration(totalHeight).toLong()
            _columns[column]!!.getChildAt(i).clearAnimation()
            _columns[column]!!.getChildAt(i).startAnimation(animation)
        }
    }

    override fun onDeleteJob() {
        endDelete(false)
    }

    override fun showDeleteButton(pj: PrintJobsGroupView?, view: View?) {
        beginDelete(pj, view, true)
    }

    override fun hideDeleteButton() {
        endDelete(true)
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class AddViewRunnable
     *
     * @brief Requests layout after adding views during onLayout
     *
     * Based on: http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
     */
    private inner class AddViewRunnable : Runnable {
        override fun run() {
            requestLayout()
        }
    }

    /**
     * @interface PrintJobsViewListener
     *
     * @brief Interface for PrintJobsGroupView events
     */
    interface PrintJobsViewListener {
        /**
         * @brief Called after loading initial views
         */
        fun onLoadFinished()
    }

    /**
     * @class ViewCreationThread
     *
     * @brief Background Thread for creating PrintJobsGroupViews
     */
    private inner class ViewCreationThread : Thread() {
        @Volatile
        private var _isRunning = true
        @Synchronized
        fun setIsRunning(b: Boolean) {
            _isRunning = b
        }

        override fun run() {
            var jobCtr = 0
            var start = 0
            for (j in _printers.indices) {
                if (!_isRunning) {
                    return
                }
                val printer = _printers[j]
                val pid = printer.id
                // get printer's jobs list with printerid==pid
                // printJobs is ordered according to prn_id in query
                for (i in start until _printJobs.size) {
                    val id = _printJobs[i].printerId
                    jobCtr = i
                    if (id == pid) {
                        // if current printer id is different from printer id of next print job in the list
                        if (i == _printJobs.size - 1 || pid != _printJobs[i + 1].printerId) {
                            break
                        }
                    }
                }
                val pjView = createPrintJobsView(_printJobs.subList(start, jobCtr + 1), printer)
                start = jobCtr + 1
                if (!_isRunning) {
                    return
                }
                _printGroupViews.add(pjView)
                _groupViewCtr = j
                post {
                    placeInColumns(pjView)
                    if (_groupViewCtr == _printers.size - 1) {
                        if (_listenerRef != null && _listenerRef!!.get() != null) {
                            _listenerRef!!.get()!!.onLoadFinished()
                        }
                    }
                }
            }
        }

        init {
            priority = MIN_PRIORITY
        }
    }

    companion object {
        private const val SWIPE_THRESHOLD = 50
        private const val MIN_COLUMNS = 2
    }
}