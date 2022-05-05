/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobsGroupView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.jobs

//import jp.co.riso.smartdeviceapp.model.Printer.id
import jp.co.riso.android.util.AppUtils.getScreenDimensions
//import jp.co.riso.smartdeviceapp.model.Printer.name
//import jp.co.riso.smartdeviceapp.model.Printer.ipAddress
import android.view.View.OnTouchListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsLayoutListener
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager
import jp.co.riso.smartprint.R
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.dialog.DialogUtils
import androidx.fragment.app.FragmentActivity
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView
import android.view.MotionEvent
import jp.co.riso.android.util.AppUtils
import android.app.Activity
import android.os.Looper
import android.view.LayoutInflater
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import android.view.animation.TranslateAnimation
import android.view.animation.Animation.AnimationListener
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.*
import jp.co.riso.smartdeviceapp.model.PrintJob
import jp.co.riso.smartdeviceapp.model.Printer
import java.util.*

/**
 * @class PrintJobsGroupView
 *
 * @brief Custom view for a print jobs group (jobs under the same printer).
 */
class PrintJobsGroupView : LinearLayout, View.OnClickListener, OnTouchListener, Handler.Callback {
    private var mPrintGroupView: View? = null
    private var mPrintJobs: MutableList<PrintJob>? = null
    private var mPrinter: Printer? = null
    private var mViewToDelete: View? = null
    private var mGroupListener: PrintJobsGroupListener? = null
    private var mLayoutListener: PrintJobsLayoutListener? = null
    private var mPrintJobGroupLayout: RelativeLayout? = null
    private var mIsCollapsed = false
    private var mTitle: String? = null
    private var mErrorMessage: String? = null
    private var mOkText: String? = null
    private var mHandler: Handler? = null
    private var mJobsLayout: LinearLayout? = null
    private var mRowHeight = 0
    private var mSeparatorHeight = 0

    /**
     * @brief Default Constructor
     *
     * @param context Activity context
     */
    constructor(context: Context?) : super(context) {
        init()
    }

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
     * @brief Sets the data for the PrintJobsGroupView and creates view.
     *
     * @param printJobs List of print jobs
     * @param printer Printer object of the print jobs
     * @param groupListener Print job group listener
     * @param layoutListener Layout listener
     */
    fun setData(
        printJobs: List<PrintJob>?,
        printer: Printer?,
        groupListener: PrintJobsGroupListener?,
        layoutListener: PrintJobsLayoutListener?
    ) {
        mPrintJobs = ArrayList(printJobs as MutableList)
        mPrinter = printer
        mGroupListener = groupListener
        mLayoutListener = layoutListener
        createView()
    }

    /**
     * @brief Retrieves the expanded height of the PrintJobsGroupView
     *
     * @return Expanded height of PrintJobsGroupView
     */
    val groupHeight: Int
        get() = (mJobsLayout!!.childCount + 1) * mRowHeight + (mJobsLayout!!.childCount - 1) * mSeparatorHeight

    /**
     * @brief Restores the UI state of the jobs group
     *
     * @param isCollapsed Collapsed state
     * @param printerToDelete Printer of the print jobs group to be deleted
     * @param jobToDelete Print job to be deleted
     */
    fun restoreState(isCollapsed: Boolean, printerToDelete: Printer?, jobToDelete: PrintJob?) {
        val isDeleteAllClicked = printerToDelete != null && printerToDelete == mPrinter
        val isDeleteShown = jobToDelete != null && mPrintJobs!!.contains(jobToDelete)
        if (isCollapsed) {
            animateCollapse(false)
            mIsCollapsed = true
        }
        if (isDeleteAllClicked) {
            mGroupListener!!.setPrinterToDelete(this, mPrinter)
            mPrintGroupView!!.findViewWithTag<View>(printerToDelete).isSelected = true
        }
        if (isDeleteShown) {
            mGroupListener!!.setDeletePrintJob(this, jobToDelete)
        }
    }

    /**
     * @brief Deletes Job Group in the database and view
     */
    fun onDeleteJobGroup() {
        val pm = PrintJobManager.getInstance(context)
        val isSuccess = pm!!.deleteWithPrinterId(mPrinter!!.id)
        if (isSuccess) {
            animateDeleteGroup()
        } else {
            mPrintJobGroupLayout!!.findViewById<View>(R.id.printJobGroupDelete).isSelected =
                false
            // show dialog
            val errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mOkText)
            DialogUtils.displayDialog(context as FragmentActivity, TAG, errordialog)
        }
    }

    /**
     * @brief Deletes a print job in the database and view
     *
     * @param job Print Job to be deleted
     */
    fun onDeletePrintJob(job: PrintJob) {
        val pm = PrintJobManager.getInstance(context)
        val isSuccess = pm!!.deleteWithJobId(job.id)
        if (isSuccess) {
            mGroupListener!!.deleteJobFromList(job)
            animateDeleteJob(mJobsLayout!!.findViewWithTag(job))
        } else {
            // show dialog
            val errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mOkText)
            DialogUtils.displayDialog(context as FragmentActivity, TAG, errordialog)
        }
        // clears delete state
        mLayoutListener!!.onDeleteJob()
    }

    /**
     * @brief Cancels delete Job Group
     */
    fun onCancelDeleteGroup() {
        mPrintJobGroupLayout!!.findViewById<View>(R.id.printJobGroupDelete).isSelected = false
    }

    /**
     * @brief Sets delete button state
     *
     * @param v Print Job view to be deleted
     */
    fun setDeleteButton(v: View?) {
        mViewToDelete = v
        mViewToDelete!!.isSelected = true
        // RM#1104 print job button should not be selected - it must only be selected when clicked
        mViewToDelete!!.findViewById<View>(R.id.printJobDeleteBtn).isSelected = false
        mGroupListener!!.setDeletePrintJob(this, mViewToDelete!!.tag as PrintJob)
    }

    /**
     * @brief Clears delete button state
     */
    fun clearDeleteButton() {
        if (mViewToDelete != null) {
            mViewToDelete!!.isSelected = false
            mViewToDelete = null
            mGroupListener!!.setDeletePrintJob(null, null)
        }
    }

    /**
     * @brief After a print job is deleted, move focus to next print job if present,
     * if it is the last print job, move focus to the previous print job
     */
    fun focusNextPrintJob() {
        if (mViewToDelete != null) {
            var nextFocus = mViewToDelete!!.focusSearch(FOCUS_DOWN)
            // focus can move beyond the print job group so check if a child of the group
            if (nextFocus == null || nextFocus.parent !== mJobsLayout) {
                nextFocus = mViewToDelete!!.focusSearch(FOCUS_UP)
            }
            nextFocus?.requestFocus()
        }
    }

    /**
     * @brief After delete is cancelled, return focus to the print job
     */
    fun returnFocusToPrintJob() {
        if (mViewToDelete != null) {
            mViewToDelete!!.requestFocus()
        }
    }

    /**
     * @brief Retrieves a print job row layout if a swipe is valid within the row.
     *
     * @param downPoint Point containing the coordinates of the ACTION_DOWN event
     * @param ev MotionEvent of the ACTION_MOVE containing the coordinates
     *
     * @return Swiped job view
     * @retval null Invalid swipe
     */
    fun getJobViewSwiped(downPoint: Point, ev: MotionEvent): View? {
        if (mIsCollapsed) {
            return null
        }
        val coords = IntArray(2)
        for (i in 0 until mJobsLayout!!.childCount) {
            val view = mJobsLayout!!.getChildAt(i)
            if (view != null) {
                view.getLocationOnScreen(coords)
                val rect =
                    Rect(coords[0], coords[1], coords[0] + view.width, coords[1] + view.height)
                val contains1 = rect.contains(downPoint.x, downPoint.y)
                val contains2 = rect.contains(
                    ev.rawX.toInt(), ev.rawY.toInt()
                )
                if (contains1 && contains2) {
                    return view
                }
            }
        }
        return null
    }

    /**
     * @brief Gets animation duration based on the height of the view animated or screen height whichever is smaller
     *
     * @param originalHeight Height of the view to be animated
     *
     * @return Computed animation duration
     */
    fun getAnimationDuration(originalHeight: Int): Int {
        val newHeight = Math.min(originalHeight, getScreenDimensions(context as Activity)!!.y)
        return (newHeight * DURATION_MULTIPLIER).toInt()
    }

    /**
     * @brief Initializes PrintJobsGroupView
     */
    private fun init() {
        if (!isInEditMode) {
            mTitle = resources.getString(R.string.ids_info_msg_delete_jobs_title)
            mOkText = resources.getString(R.string.ids_lbl_ok)
            mErrorMessage = resources.getString(R.string.ids_err_msg_db_failure)
            orientation = VERTICAL
            mRowHeight = resources.getDimensionPixelSize(R.dimen.printjob_row_height)
            mSeparatorHeight = resources.getDimensionPixelSize(R.dimen.separator_size)
            (context as Activity).runOnUiThread {
                mHandler = Handler(Looper.myLooper()!!, this@PrintJobsGroupView)
            }
        }
    }

    /**
     * @brief Creates the view for print jobs group.
     */
    private fun createView() {
        // create header
        if (!mPrintJobs!!.isEmpty()) {
            createHeader()
        }

        // add print jobs
        for (i in mPrintJobs!!.indices) {
            createItem(i)
        }
        mPrintJobs!!.clear()
    }

    /**
     * @brief Creates view for the header.
     */
    private fun createHeader() {
        val factory = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mPrintGroupView = factory.inflate(R.layout.printjobs_group, this, true)
        mPrintJobGroupLayout = mPrintGroupView!!.findViewById(R.id.printJobsGroupLayout)
        val printJobGroupText = mPrintGroupView!!.findViewById<TextView>(R.id.printJobGroupText)
        val printJobGroupSubText = mPrintGroupView!!.findViewById<TextView>(R.id.printJobGroupSubText)
        val printJobGroupDelete = mPrintGroupView!!.findViewById<Button>(R.id.printJobGroupDelete)
        var printerName = mPrinter!!.name
        if (printerName == null || printerName.isEmpty()) {
            printerName = context.resources.getString(R.string.ids_lbl_no_name)
        }
        printJobGroupText.text = printerName
        printJobGroupSubText.text = mPrinter!!.ipAddress
        printJobGroupDelete.tag = mPrinter
        mPrintJobGroupLayout!!.setOnClickListener(this)
        printJobGroupDelete.setOnClickListener(this)

        // AppUtils.changeChildrenFont(mPrintJobGroupLayout, SmartDeviceApp.getAppFont());
        mJobsLayout = LinearLayout(context)
        mJobsLayout!!.orientation = VERTICAL
        addView(mJobsLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        if (resources.getBoolean(R.bool.is_tablet)) {
            mPrintGroupView!!.findViewById<View>(R.id.printJobDeleteSeparator).visibility =
                INVISIBLE
        }
    }

    /**
     * @brief Creates view for an item in the jobs list.
     *
     * @param index Index of the print job item in the list to be created
     */
    private fun createItem(index: Int) {
        val factory = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val tempView = factory.inflate(R.layout.printjobs_item, this, false)
        val printJobName = tempView.findViewById<TextView>(R.id.printJobName)
        val printJobError = tempView.findViewById<ImageView>(R.id.printJobError)
        val printJobSuccess = tempView.findViewById<ImageView>(R.id.printJobSuccess)
        val printJobDeleteBtn = tempView.findViewById<Button>(R.id.printJobDeleteBtn)
        val printJobDate = tempView.findViewById<TextView>(R.id.printJobDate)
        val pj = mPrintJobs!![index]
        tempView.tag = pj
        tempView.setOnTouchListener(this)
        printJobName.text = pj.name
        printJobDate.text = formatDate(pj.date!!)
        printJobDeleteBtn.setOnClickListener(this)
        if (pj.result == JobResult.ERROR) {
            printJobError.visibility = VISIBLE
            printJobSuccess.visibility = GONE
        }
        tempView.setOnClickListener(this)
        mJobsLayout!!.addView(tempView)

        // AppUtils.changeChildrenFont((ViewGroup) tempView, SmartDeviceApp.getAppFont());
        if (index == mPrintJobs!!.size - 1) {
            tempView.findViewById<View>(R.id.printJobSeparator).visibility =
                GONE
        }
    }

    /**
     * @brief Format date into string using locale format (date and time)
     *
     * @param date Date to be formatted
     *
     * @return Converted string format
     */
    private fun formatDate(date: Date): String {
        val dateStr = DateFormat.getDateFormat(context).format(date)
        val timeStr = DateFormat.getTimeFormat(context).format(date)
        return dateStr + C_SPACE + timeStr
    }

    /**
     * @brief Toggles collapse/expand of a group view when clicked.
     *
     * @param v Header view of the group view to be collapse/expand
     */
    private fun toggleGroupView(v: View) {
        v.isClickable = false
        mIsCollapsed = if (mIsCollapsed) {
            animateExpand(true)
            false
        } else {
            animateCollapse(true)
            true
        }
        mGroupListener!!.setCollapsed(mPrinter, mIsCollapsed)
    }

    /**
     * @brief Animates expand of a print job group.
     *
     * @param animate true if expand with animation
     */
    private fun animateExpand(animate: Boolean) {
        mJobsLayout!!.visibility = VISIBLE
        val totalHeight =
            mJobsLayout!!.childCount * mRowHeight + (mJobsLayout!!.childCount - 1) * mSeparatorHeight
        if (animate) {
            for (i in 0 until mJobsLayout!!.childCount) {
                val child = mJobsLayout!!.getChildAt(i)
                val animation = TranslateAnimation(0F, 0F, (-totalHeight).toFloat(), 0F)
                animation.duration = getAnimationDuration(totalHeight).toLong()
                if (i == mJobsLayout!!.childCount - 1) {
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            val newMessage = Message.obtain(mHandler, MSG_EXPAND)
                            mHandler!!.sendMessage(newMessage)
                        }
                    })
                }
                child.clearAnimation()
                child.startAnimation(animation)
            }
            mLayoutListener!!.animateGroups(this, totalHeight, DURATION_MULTIPLIER, true)
        } else {
            expandGroupView()
        }
    }

    /**
     * @brief Animates collapse of a print job group.
     *
     * @param animate true if collapse with animation
     */
    private fun animateCollapse(animate: Boolean) {
        if (animate) {
            val totalHeight =
                mJobsLayout!!.childCount * mRowHeight + (mJobsLayout!!.childCount - 1) * mSeparatorHeight
            for (i in 0 until mJobsLayout!!.childCount) {
                val child = mJobsLayout!!.getChildAt(i)
                val animation = TranslateAnimation(0F, 0F, 0F, (-totalHeight).toFloat())
                animation.duration = getAnimationDuration(totalHeight).toLong()
                animation.fillAfter = true
                if (i == mJobsLayout!!.childCount - 1) {
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            val newMessage = Message.obtain(mHandler, MSG_COLLAPSE)
                            mHandler!!.sendMessage(newMessage)
                        }
                    })
                }
                child.clearAnimation()
                child.startAnimation(animation)
            }
            mLayoutListener!!.animateGroups(
                this@PrintJobsGroupView,
                totalHeight,
                DURATION_MULTIPLIER,
                false
            )
        } else {
            collapseGroupView()
        }
    }

    /**
     * @brief Animates deletion of a Print Job view.
     *
     * @param v View of the print job row to be deleted
     */
    private fun animateDeleteJob(v: View) {
        val totalHeight = mRowHeight + mSeparatorHeight
        if (mJobsLayout!!.childCount == 1) {
            animateDeleteGroup()
        } else {
            val jobToDelete = mJobsLayout!!.indexOfChild(v) //mPrintJobViews.indexOf(v);
            val deleteAnim = ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f)
            deleteAnim.setDuration((totalHeight * DURATION_MULTIPLIER).toLong())
            if (jobToDelete == mJobsLayout!!.childCount - 1) {
                deleteAnim.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        val newMessage = Message.obtain(mHandler, MSG_DELETEJOB)
                        newMessage.arg1 = jobToDelete
                        mHandler!!.sendMessage(newMessage)
                    }
                })
            }
            v.clearAnimation()
            v.startAnimation(deleteAnim)
            for (i in jobToDelete + 1 until mJobsLayout!!.childCount) {
                val child = mJobsLayout!!.getChildAt(i)
                val animation = TranslateAnimation(0F, 0F, 0F, (-totalHeight).toFloat())
                animation.setDuration((totalHeight * DURATION_MULTIPLIER).toLong())
                if (i == mJobsLayout!!.childCount - 1) {
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            val newMessage = Message.obtain(mHandler, MSG_DELETEJOB)
                            newMessage.arg1 = jobToDelete
                            mHandler!!.sendMessage(newMessage)
                        }
                    })
                }
                child.clearAnimation()
                child.startAnimation(animation)
            }
            mLayoutListener!!.animateGroups(
                this@PrintJobsGroupView,
                totalHeight,
                DURATION_MULTIPLIER,
                false
            )
        }
    }

    /**
     * @brief Animates deletion of Print Jobs Group.
     */
    private fun animateDeleteGroup() {
        val totalHeight: Int
        totalHeight = if (!mIsCollapsed) {
            groupHeight
        } else if (resources.getBoolean(R.bool.is_tablet)) {
            mRowHeight
        } else {
            mRowHeight + mSeparatorHeight
        }
        val animation = ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f)
        animation.duration = getAnimationDuration(totalHeight).toLong()
        animation.fillAfter = true
        animation.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                val newMessage = Message.obtain(mHandler, MSG_DELETEGROUP)
                mHandler!!.sendMessage(newMessage)
            }
        })
        mPrintGroupView!!.clearAnimation()
        mPrintGroupView!!.startAnimation(animation)
        mLayoutListener!!.animateGroups(
            this@PrintJobsGroupView,
            totalHeight,
            DURATION_MULTIPLIER,
            false
        )
    }

    /**
     * @brief Expands view group.
     */
    private fun expandGroupView() {
        mPrintJobGroupLayout!!.isSelected = false
        if (!resources.getBoolean(R.bool.is_tablet)) {
            findViewById<View>(R.id.printJobGroupSeparator).visibility =
                GONE
        }
        mPrintJobGroupLayout!!.isClickable = true
    }

    /**
     * @brief Collapses view group.
     */
    private fun collapseGroupView() {
        mJobsLayout!!.visibility = GONE
        mPrintJobGroupLayout!!.isSelected = true
        mPrintJobGroupLayout!!.findViewById<View>(R.id.printJobGroupDelete).isSelected =
            false
        if (!resources.getBoolean(R.bool.is_tablet)) {
            findViewById<View>(R.id.printJobGroupSeparator).visibility = VISIBLE
        }
        mPrintJobGroupLayout!!.isClickable = true
    }

    /**
     * @brief Displays delete print jobs dialog when the delete all button is clicked.
     *
     * @param v Delete view clicked
     */
    private fun deleteJobGroup(v: View) {
        mGroupListener!!.setPrinterToDelete(this, mPrinter)
        if (mGroupListener!!.showDeleteDialog()) {
            v.findViewById<View>(R.id.printJobGroupDelete).isSelected = true
        }
    }

    /**
     * @brief Deletes the print job group view.
     */
    private fun deletePrintJobGroupView() {
        for (i in 0 until mJobsLayout!!.childCount) {
            mGroupListener!!.deleteJobFromList(mJobsLayout!!.getChildAt(i).tag as PrintJob)
        }
        mGroupListener!!.deletePrinterFromList(mPrinter)
        mLayoutListener!!.deletePrintJobsGroup(this)
        (mPrintGroupView!!.parent as LinearLayout).removeView(mPrintGroupView)
    }

    /**
     * @brief Deletes a print job row layout.
     *
     * @param i Index of Print Job View to be deleted
     */
    private fun deletePrintJobView(i: Int) {
        mJobsLayout!!.removeViewAt(i)
        if (mJobsLayout!!.childCount == 0) {
            deletePrintJobGroupView()
        } else if (i == mJobsLayout!!.childCount) {
            // after deletion remove separator in last row
            val lastRow = i - 1
            mJobsLayout!!.getChildAt(lastRow)
                .findViewById<View>(R.id.printJobSeparator).visibility = GONE
        }
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.printJobGroupDelete) {
            // if delete button is visible, hide that instead of handling the click
            if (mLayoutListener!!.isDeleteMode) {
                mLayoutListener!!.hideDeleteButton()
            } else {
                deleteJobGroup(v)
            }
        } else if (id == R.id.printJobDeleteBtn) {
            // RM#1104 set button to selected to change the button background color when clicked
            v.isSelected = true
            mGroupListener!!.showDeleteDialog()
        } else if (id == R.id.printJobsGroupLayout) {
            // if delete button is visible, hide that instead of handling the click
            if (mLayoutListener!!.isDeleteMode) {
                mLayoutListener!!.hideDeleteButton()
            } else {
                toggleGroupView(v)
            }
        } else if (id == R.id.printJobRow) {
            if (mLayoutListener!!.isDeleteMode) {
                mLayoutListener!!.hideDeleteButton()
            } else {
                mLayoutListener!!.showDeleteButton(this, v)
            }
        }
    }

    // ================================================================================
    // INTERFACE - View.onTouchListener
    // ================================================================================
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                v.isPressed = true
                v.findViewById<View>(R.id.printJobDeleteBtn).isPressed = false
                return true
            }
            MotionEvent.ACTION_MOVE -> return true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.isPressed = false
                return true
            }
        }
        return false
    }

    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_COLLAPSE -> {
                collapseGroupView()
                return true
            }
            MSG_EXPAND -> {
                expandGroupView()
                return true
            }
            MSG_DELETEJOB -> {
                deletePrintJobView(msg.arg1)
                return true
            }
            MSG_DELETEGROUP -> {
                deletePrintJobGroupView()
                return true
            }
        }
        return false
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @interface PrintJobsGroupListener
     *
     * @brief Interface for PrintJobsGroupView events such as collapse,expand and delete.
     */
    interface PrintJobsGroupListener {
        /**
         * @brief Called when a print jobs group is deleted
         *
         * @param printer Printer object of the print job group to be removed
         */
        fun deletePrinterFromList(printer: Printer?)

        /**
         * @brief Called when a print job is deleted
         *
         * @param printJob PrintJob object to be removed
         */
        fun deleteJobFromList(printJob: PrintJob?)

        /**
         * @brief Called when a print job group is expanded/collapsed
         *
         * @param printer Printer object of the print job group
         * @param isCollapsed Collapse state
         */
        fun setCollapsed(printer: Printer?, isCollapsed: Boolean)

        /**
         * @brief Callback for setting print job to be deleted
         *
         * @param printJobsGroupView PrintJobsGroupView containing the PrintJob to be deleted
         * @param job PrintJob to be deleted
         */
        fun setDeletePrintJob(printJobsGroupView: PrintJobsGroupView?, job: PrintJob?)

        /**
         * @brief Callback for setting print job group to be deleted
         *
         * @param printJobsGroupView PrintJobsGroupView of the job group to be deleted
         * @param printer Printer of the print job group to be deleted
         */
        fun setPrinterToDelete(printJobsGroupView: PrintJobsGroupView?, printer: Printer?)

        /**
         * @brief Called when a delete button is clicked
         */
        fun showDeleteDialog(): Boolean
    }

    /**
     * @interface PrintJobsLayoutListener
     *
     * @brief Interface for PrintJobsGroupView events such as animate and delete.
     */
    interface PrintJobsLayoutListener {
        /**
         * @brief Called when a print job group is deleted
         *
         * @param printJobsGroupView Print job group to be removed
         */
        fun deletePrintJobsGroup(printJobsGroupView: PrintJobsGroupView?)

        /**
         * @brief Called when animating a print job group
         *
         * @param printJobsGroupView Print job group view to animate
         * @param totalHeight Total height of the view to animate
         * @param durationMultiplier Duration multiplier for the animation
         * @param down Direction of animation; if true views translates downwards, else upwards
         */
        fun animateGroups(
            printJobsGroupView: PrintJobsGroupView?,
            totalHeight: Int,
            durationMultiplier: Float,
            down: Boolean
        )

        /**
         * @brief Called when a print job is deleted
         */
        fun onDeleteJob()

        /**
         * @brief Called to show the print job delete button
         *
         * @param pj Print job group view which contains the item to animate
         * @param view Print job item to show the delete button for
         */
        fun showDeleteButton(pj: PrintJobsGroupView?, view: View?)

        /**
         * @brief Called to hide the print job delete button
         */
        fun hideDeleteButton()

        /**
         * @brief Check if delete mode is ongoing i.e. a delete button is shown
         *
         * @return True if delete mode is ongoing
         */
        val isDeleteMode: Boolean
    }

    companion object {
        private val TAG = PrintJobsGroupView::class.java.name
        private const val C_SPACE = " "
        private const val MSG_COLLAPSE = 0
        private const val MSG_EXPAND = 1
        private const val MSG_DELETEJOB = 2
        private const val MSG_DELETEGROUP = 3
        private const val DURATION_MULTIPLIER = 0.2f
    }
}