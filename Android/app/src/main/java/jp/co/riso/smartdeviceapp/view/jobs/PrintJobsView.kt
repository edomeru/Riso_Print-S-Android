/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobsView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.jobs

//import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation.beginDeleteModeOnView
//import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.setDeleteButton
//import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation.endDeleteMode
//import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.clearDeleteButton
//import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.setData
//import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.restoreState
//import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.groupHeight
//import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.getJobViewSwiped
import jp.co.riso.android.util.AppUtils.getScreenDimensions
//import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.getAnimationDuration
//import jp.co.riso.smartdeviceapp.model.Printer.id
import android.widget.LinearLayout
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsLayoutListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView.PrintJobsViewListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView.ViewCreationThread
import jp.co.riso.smartprint.R
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView.AddViewRunnable
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView
import android.view.MotionEvent
import jp.co.riso.android.util.AppUtils
import android.app.Activity
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.TranslateAnimation
import jp.co.riso.smartdeviceapp.model.PrintJob
import jp.co.riso.smartdeviceapp.model.Printer
import java.lang.ref.WeakReference
import java.util.ArrayList
import kotlin.jvm.Volatile
import kotlin.jvm.Synchronized

/**
 * @class PrintJobsView
 *
 * @brief Custom view for the Print Job History view
 */
class PrintJobsView : LinearLayout, PrintJobsLayoutListener {
    private var mListenerRef: WeakReference<PrintJobsViewListener?>? = null
    private var mPrintJobs: MutableList<PrintJob> = ArrayList()
    private var mPrinters: MutableList<Printer> = ArrayList()
    private val mCollapsedPrinters: MutableList<Printer> = ArrayList()
    private val mColumns: MutableList<LinearLayout> = ArrayList()
    private val mPrintGroupViews: MutableList<PrintJobsGroupView?> = ArrayList()
    private var mPrintGroupWithDelete: PrintJobsGroupView? = null
    private var mDeleteAnimation: DisplayDeleteAnimation? = null
    private var mGroupListener: PrintJobsGroupListener? = null
    private var mPrintJobToDelete: PrintJob? = null
    private var mPrinterToDelete: Printer? = null
    private var mRunnable: Runnable? = null
    private var mGroupViewCtr = 0
    private var mInitialFlag = false
    override var isDeleteMode = false
        private set
    private var mDeleteView: View? = null
    private var mDownPoint: Point? = null
    private lateinit var mColumnsHeight: IntArray
    private var mThread: ViewCreationThread? = null

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
        printJobs: List<PrintJob?>?,
        printers: List<Printer?>?,
        groupListener: PrintJobsGroupListener?,
        viewListener: PrintJobsViewListener?
    ) {
        mPrintJobs = ArrayList(printJobs as MutableList)
        mPrinters = ArrayList(printers as MutableList)
        mGroupListener = groupListener
        mListenerRef = WeakReference(viewListener)
        reset()
    }

    /**
     * @brief Begins delete mode, i.e. displays the delete button.
     *
     * @param pj Print jobs group view containing the delete button to be displayed
     * @param view Job row layout view containing the delete button to be displayed
     * @param animate If true, delete button is displayed with animation
     */
    fun beginDelete(pj: PrintJobsGroupView?, view: View?, animate: Boolean) {
        if (!isDeleteMode) {
            isDeleteMode = true
            mDeleteAnimation!!.beginDeleteModeOnView(
                view!!,
                animate,
                R.id.printJobDeleteBtn,
                R.id.printJobDate
            )
            mDeleteView = view
            pj!!.setDeleteButton(view)
            mPrintGroupWithDelete = pj
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
            mDeleteAnimation!!.endDeleteMode(
                mDeleteView!!,
                animate,
                R.id.printJobDeleteBtn,
                R.id.printJobDate
            )
            mDeleteView = null
            mPrintGroupWithDelete!!.clearDeleteButton()
        }
    }

    /**
     * @brief Resets the PrintJobsView.
     */
    fun reset() {
        isDeleteMode = false
        mGroupViewCtr = 0
        mPrintGroupViews.clear()
        mInitialFlag = true
        removeAllViews()
        mColumns.clear()
        groupPrintJobs()
    }

    /**
     * @brief Sets print job to be deleted.
     *
     * @param job PrintJob to be deleted
     */
    fun setJobToDelete(job: PrintJob?) {
        mPrintJobToDelete = job
    }

    /**
     * @brief Set print job group to be deleted.
     *
     * @param printer Printer of the job group to be deleted
     */
    fun setPrinterToDelete(printer: Printer?) {
        mPrinterToDelete = printer
    }

    /**
     * @brief Set collapse state of the print job group.
     *
     * @param printer Printer object of the print job group to be collapsed/expanded
     * @param isCollapsed Collapse state
     */
    fun setCollapsedPrinters(printer: Printer, isCollapsed: Boolean) {
        if (isCollapsed) {
            mCollapsedPrinters.add(printer)
        } else {
            mCollapsedPrinters.remove(printer)
        }
    }

    /**
     * @brief Deletes PrintJob from list.
     *
     * @param job PrintJob to be deleted
     */
    fun deleteJobFromList(job: PrintJob) {
        mPrintJobs.remove(job)
    }

    /**
     * @brief Deletes printer from list
     *
     * @param printer Printer to be deleted
     */
    fun deletePrinterFromList(printer: Printer) {
        mPrinters.remove(printer)
    }

    /**
     * @brief Initializes PrintJobsView.
     */
    private fun init() {
        mDeleteAnimation = DisplayDeleteAnimation()
        mRunnable = AddViewRunnable()
    }

    /**
     * @brief Creates a thread for grouping print jobs and creating views for each group
     */
    private fun groupPrintJobs() {
        if (mThread != null && mThread!!.isAlive) {
            mThread!!.interrupt()
            mThread!!.setIsRunning(false)
        }
        mThread = ViewCreationThread()
        mThread!!.start()
        // RM#942 thread join need to prevent race condition which causes print job groups to be doubled
        try {
            mThread!!.join()
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
            var tempHeight = mColumnsHeight[smallestColumn]
            for (i in 1 until mColumnsHeight.size) {
                if (mColumnsHeight[i] < tempHeight) {
                    tempHeight = mColumnsHeight[i]
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
        pjView.setData(jobsList, printer, mGroupListener, this@PrintJobsView)
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
        val isCollapsed = mCollapsedPrinters.contains(printer)
        val isDeleteShown = mPrintJobToDelete != null && mPrintJobs.contains(mPrintJobToDelete)
        if (isDeleteShown) {
            val v = pj.findViewWithTag<View>(mPrintJobToDelete)
            if (v != null) {
                beginDelete(pj, v, false)
                pj.restoreState(isCollapsed, mPrinterToDelete, mPrintJobToDelete)
            } else {
                pj.restoreState(isCollapsed, mPrinterToDelete, null)
            }
        } else {
            pj.restoreState(isCollapsed, mPrinterToDelete, null)
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
        if (mColumns.size > col && totalPrintJobGroups < mPrinters.size) {
            mColumns[col].addView(pjView, groupParams)
            mColumnsHeight[col] += pjView.groupHeight + groupParams.topMargin // update column height
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
            for (i in mColumns.indices) {
                total += mColumns[i].childCount
            }
            return total
        }

    /**
     * @brief Adds PrintJobsGroupView in columns.
     */
    private fun addViewsToColumns() {
        for (i in mPrintGroupViews.indices) {
            placeInColumns(mPrintGroupViews[i])
        }
        mGroupViewCtr = mPrinters.size - 1
        post(mRunnable)
    }

    /**
     * @brief Re-layouts PrintJobsGroupViews in columns.
     */
    private fun relayoutColumns() {
        if (checkIfNeedsRelayout()) {
            for (i in mColumns.indices) {
                mColumns[i].removeAllViews()
                mColumnsHeight[i] = 0 // clear columns height
            }
            mGroupViewCtr = 0
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
        for (i in mColumns.indices) {
            isColumnCleared = isColumnCleared or (mColumns[i].height == 0)
            childrenNum += mColumns[i].childCount
        }
        childNumExceeds = childrenNum >= mColumns.size
        if (!childNumExceeds) {
            for (i in 0 until childrenNum) {
                isLeftCleared = isLeftCleared or (mColumns[i].height == 0)
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
            colNum = Math.max(width / columnWidth, MIN_COLUMNS)
            if (colNum == MIN_COLUMNS) {
                // adjust column width depending on whole width
                columnWidth = Math.min(width / MIN_COLUMNS, columnWidth)
            }
        }
        mColumnsHeight = IntArray(colNum)
        for (i in 0 until colNum) {
            val columnParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            mColumns.add(LinearLayout(context))
            mColumns[i].orientation = VERTICAL

            // if tablet set column width
            if (colNum > 1) {
                columnParams.width = columnWidth
            }
            addView(mColumns[i], columnParams)
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
        if (ev.rawX - mDownPoint!!.x > SWIPE_THRESHOLD) {
            endDelete(true)
            return false
        }
        val coords = IntArray(2)
        val dragged = mDownPoint!!.x - ev.rawX > SWIPE_THRESHOLD
        var contains1: Boolean
        var contains2: Boolean
        // check self, if valid swipe don't redisplay nor remove delete button
        if (isDeleteMode) {
            mDeleteView!!.getLocationOnScreen(coords)
            val rect = Rect(
                coords[0],
                coords[1],
                coords[0] + mDeleteView!!.width,
                coords[1] + mDeleteView!!.height
            )
            contains1 = rect.contains(mDownPoint!!.x, mDownPoint!!.y)
            contains2 = rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
            return contains1 && contains2 && dragged
        }
        for (i in mColumns.indices) {
            val column = mColumns[i]
            column.getLocationOnScreen(coords)
            val rect =
                Rect(coords[0], coords[1], coords[0] + column.width, coords[1] + column.height)
            contains1 = rect.contains(mDownPoint!!.x, mDownPoint!!.y)
            contains2 = rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
            if (contains1 && contains2 && dragged) {
                for (j in 0 until column.childCount) {
                    val view = (column.getChildAt(j) as PrintJobsGroupView).getJobViewSwiped(
                        mDownPoint!!, ev
                    )
                    if (view != null) {
                        beginDelete(column.getChildAt(j) as PrintJobsGroupView, view, true)
                        return true
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
        val action = ev.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> mDownPoint = Point(
                ev.rawX.toInt(), ev.rawY.toInt()
            )
            MotionEvent.ACTION_MOVE -> ret = checkSwipe(ev)
        }
        return ret
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mInitialFlag) {
            val screenSize = getScreenDimensions(context as Activity)
            createColumns(screenSize!!.x)
            mInitialFlag = false
        }
        //        if (mGroupViewCtr < mPrintGroupViews.size()) {
//            addViewsToColumns();
//        } else 
        if (mColumns.size > 1 && mGroupViewCtr > mPrinters.size - 1) {
            // if multiple columns and after deletion of a PrintJobsGroupView
            relayoutColumns()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val coords = IntArray(2)
        if (isDeleteMode) {
            if (mDeleteView != null) {
                val deleteButton = mDeleteView!!.findViewById<View>(R.id.printJobDeleteBtn)
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
                            mDownPoint = Point(
                                ev.rawX.toInt(), ev.rawY.toInt()
                            )
                        }
                        return super.onInterceptTouchEvent(ev)
                    }
                }
                // intercept and clear delete button if ACTION_DOWN on different item
                if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                    mDeleteView!!.getLocationOnScreen(coords)
                    val rect = Rect(
                        coords[0],
                        coords[1],
                        coords[0] + mDeleteView!!.width,
                        coords[1] + mDeleteView!!.height
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
        mPrintGroupViews.remove(printJobsGroupView)
    }

    override fun animateGroups(
        printJobsGroupView: PrintJobsGroupView?,
        totalHeight: Int,
        durationMultiplier: Float,
        down: Boolean
    ) {
        var idx = mPrintGroupViews.indexOf(printJobsGroupView)
        var column = 0
        for (i in mColumns.indices) {
            for (j in 0 until mColumns[i].childCount) {
                if (mColumns[i].getChildAt(j) == printJobsGroupView) {
                    idx = j // get index position in column of collapsed/expanded group
                    column = i // get column of collapsed/expanded group
                    break
                }
            }
        }
        var animation: TranslateAnimation
        for (i in idx + 1 until mColumns[column].childCount) {
            if (down) {
                animation = TranslateAnimation(0F, 0F, (-totalHeight).toFloat(), 0F)
                animation.setStartOffset(
                    (resources.getDimensionPixelSize(R.dimen.printjob_row_height) * durationMultiplier).toLong()

                )
            } else {
                animation = TranslateAnimation(0F, 0F, 0F, (-totalHeight).toFloat())
            }
            animation.duration = printJobsGroupView!!.getAnimationDuration(totalHeight).toLong()
            mColumns[column].getChildAt(i).clearAnimation()
            mColumns[column].getChildAt(i).startAnimation(animation)
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
        private var mIsRunning = true
        @Synchronized
        fun setIsRunning(b: Boolean) {
            mIsRunning = b
        }

        override fun run() {
            var jobCtr = 0
            var start = 0
            for (j in mPrinters.indices) {
                if (!mIsRunning) {
                    return
                }
                val printer = mPrinters[j]
                val pid = printer.id
                // get printer's jobs list with printerid==pid
                // printJobs is ordered according to prn_id in query
                for (i in start until mPrintJobs.size) {
                    val id = mPrintJobs[i].printerId
                    jobCtr = i
                    if (id == pid) {
                        // if current printer id is different from printer id of next print job in the list
                        if (i == mPrintJobs.size - 1 || pid != mPrintJobs[i + 1].printerId) {
                            break
                        }
                    }
                }
                val pjView = createPrintJobsView(mPrintJobs.subList(start, jobCtr + 1), printer)
                start = jobCtr + 1
                if (!mIsRunning) {
                    return
                }
                mPrintGroupViews.add(pjView)
                mGroupViewCtr = j
                post {
                    placeInColumns(pjView)
                    if (mGroupViewCtr == mPrinters.size - 1) {
                        if (mListenerRef != null && mListenerRef!!.get() != null) {
                            mListenerRef!!.get()!!.onLoadFinished()
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