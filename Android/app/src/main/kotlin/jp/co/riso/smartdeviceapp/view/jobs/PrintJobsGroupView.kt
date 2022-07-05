/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintJobsGroupView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.jobs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.fragment.app.FragmentActivity
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager
import jp.co.riso.smartdeviceapp.model.PrintJob
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartprint.R
import java.util.*

/**
 * @class PrintJobsGroupView
 *
 * @brief Custom view for a print jobs group (jobs under the same printer).
 */
class PrintJobsGroupView : LinearLayout, View.OnClickListener, OnTouchListener, Handler.Callback {
    private var _printGroupView: View? = null
    private var _printJobs: MutableList<PrintJob>? = null
    private var _printer: Printer? = null
    private var _viewToDelete: View? = null
    private var _groupListener: PrintJobsGroupListener? = null
    private var _layoutListener: PrintJobsLayoutListener? = null
    private var _printJobGroupLayout: RelativeLayout? = null
    private var _isCollapsed = false
    private var _title: String? = null
    private var _errorMessage: String? = null
    private var _okText: String? = null
    private var _handler: Handler? = null
    private var _jobsLayout: LinearLayout? = null
    private var _rowHeight = 0
    private var _separatorHeight = 0

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
        _printJobs = ArrayList(printJobs!!)
        _printer = printer
        _groupListener = groupListener
        _layoutListener = layoutListener
        createView()
    }

    /**
     * @brief Retrieves the expanded height of the PrintJobsGroupView
     *
     * @return Expanded height of PrintJobsGroupView
     */
    val groupHeight: Int
        get() = (_jobsLayout!!.childCount + 1) * _rowHeight + (_jobsLayout!!.childCount - 1) * _separatorHeight

    /**
     * @brief Restores the UI state of the jobs group
     *
     * @param isCollapsed Collapsed state
     * @param printerToDelete Printer of the print jobs group to be deleted
     * @param jobToDelete Print job to be deleted
     */
    fun restoreState(isCollapsed: Boolean, printerToDelete: Printer?, jobToDelete: PrintJob?) {
        val isDeleteAllClicked = printerToDelete != null && printerToDelete == _printer
        val isDeleteShown = jobToDelete != null && _printJobs!!.contains(jobToDelete)
        if (isCollapsed) {
            animateCollapse(false)
            _isCollapsed = true
        }
        if (isDeleteAllClicked) {
            _groupListener!!.setPrinterToDelete(this, _printer)
            _printGroupView!!.findViewWithTag<View>(printerToDelete).isSelected = true
        }
        if (isDeleteShown) {
            _groupListener!!.setDeletePrintJob(this, jobToDelete)
        }
    }

    /**
     * @brief Deletes Job Group in the database and view
     */
    fun onDeleteJobGroup() {
        val pm = PrintJobManager.getInstance(context)
        val isSuccess = pm!!.deleteWithPrinterId(_printer!!.id)
        if (isSuccess) {
            animateDeleteGroup()
        } else {
            _printJobGroupLayout!!.findViewById<View>(R.id.printJobGroupDelete).isSelected =
                false
            // show dialog
            val errordialog = InfoDialogFragment.newInstance(_title, _errorMessage, _okText)
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
            _groupListener!!.deleteJobFromList(job)
            animateDeleteJob(_jobsLayout!!.findViewWithTag(job))
        } else {
            // show dialog
            val errordialog = InfoDialogFragment.newInstance(_title, _errorMessage, _okText)
            DialogUtils.displayDialog(context as FragmentActivity, TAG, errordialog)
        }
        // clears delete state
        _layoutListener!!.onDeleteJob()
    }

    /**
     * @brief Cancels delete Job Group
     */
    fun onCancelDeleteGroup() {
        _printJobGroupLayout!!.findViewById<View>(R.id.printJobGroupDelete).isSelected = false
    }

    /**
     * @brief Sets delete button state
     *
     * @param v Print Job view to be deleted
     */
    fun setDeleteButton(v: View?) {
        _viewToDelete = v
        _viewToDelete!!.isSelected = true
        // RM#1104 print job button should not be selected - it must only be selected when clicked
        _viewToDelete!!.findViewById<View>(R.id.printJobDeleteBtn).isSelected = false
        _groupListener!!.setDeletePrintJob(this, _viewToDelete!!.tag as PrintJob?)
    }

    /**
     * @brief Clears delete button state
     */
    fun clearDeleteButton() {
        if (_viewToDelete != null) {
            _viewToDelete!!.isSelected = false
            _viewToDelete = null
            _groupListener!!.setDeletePrintJob(null, null)
        }
    }

    /**
     * @brief After a print job is deleted, move focus to next print job if present,
     * if it is the last print job, move focus to the previous print job
     */
    fun focusNextPrintJob() {
        if (_viewToDelete != null) {
            var nextFocus = _viewToDelete!!.focusSearch(FOCUS_DOWN)
            // focus can move beyond the print job group so check if a child of the group
            if (nextFocus == null || nextFocus.parent !== _jobsLayout) {
                nextFocus = _viewToDelete!!.focusSearch(FOCUS_UP)
            }
            nextFocus.requestFocus()
        }
    }

    /**
     * @brief After delete is cancelled, return focus to the print job
     */
    fun returnFocusToPrintJob() {
        if (_viewToDelete != null) {
            _viewToDelete!!.requestFocus()
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
        if (_isCollapsed) {
            return null
        }
        val coords = IntArray(2)
        for (i in 0 until _jobsLayout!!.childCount) {
            val view = _jobsLayout!!.getChildAt(i)
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
        val newHeight = originalHeight.coerceAtMost(getScreenDimensions(context as Activity?)!!.y)
        return (newHeight * DURATION_MULTIPLIER).toInt()
    }

    /**
     * @brief Initializes PrintJobsGroupView
     */
    private fun init() {
        if (!isInEditMode) {
            _title = resources.getString(R.string.ids_info_msg_delete_jobs_title)
            _okText = resources.getString(R.string.ids_lbl_ok)
            _errorMessage = resources.getString(R.string.ids_err_msg_db_failure)
            orientation = VERTICAL
            _rowHeight = resources.getDimensionPixelSize(R.dimen.printjob_row_height)
            _separatorHeight = resources.getDimensionPixelSize(R.dimen.separator_size)
            (context as Activity?)!!.runOnUiThread {
                _handler = Handler(Looper.myLooper()!!, this@PrintJobsGroupView)
            }
        }
    }

    /**
     * @brief Creates the view for print jobs group.
     */
    private fun createView() {
        // create header
        if (_printJobs!!.isNotEmpty()) {
            createHeader()
        }

        // add print jobs
        for (i in _printJobs!!.indices) {
            createItem(i)
        }
        _printJobs!!.clear()
    }

    /**
     * @brief Creates view for the header.
     */
    private fun createHeader() {
        val factory = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        _printGroupView = factory!!.inflate(R.layout.printjobs_group, this, true)
        _printJobGroupLayout = _printGroupView!!.findViewById(R.id.printJobsGroupLayout)
        val printJobGroupText = _printGroupView!!.findViewById<TextView>(R.id.printJobGroupText)
        val printJobGroupSubText = _printGroupView!!.findViewById<TextView>(R.id.printJobGroupSubText)
        val printJobGroupDelete = _printGroupView!!.findViewById<Button>(R.id.printJobGroupDelete)
        var printerName = _printer!!.name
        if (printerName == null || printerName.isEmpty()) {
            printerName = context.resources.getString(R.string.ids_lbl_no_name)
        }
        printJobGroupText.text = printerName
        printJobGroupSubText.text = _printer!!.ipAddress
        printJobGroupDelete.tag = _printer
        _printJobGroupLayout!!.setOnClickListener(this)
        printJobGroupDelete.setOnClickListener(this)

        // AppUtils.changeChildrenFont(mPrintJobGroupLayout, SmartDeviceApp.getAppFont());
        _jobsLayout = LinearLayout(context)
        _jobsLayout!!.orientation = VERTICAL
        addView(_jobsLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        if (resources.getBoolean(R.bool.is_tablet)) {
            _printGroupView!!.findViewById<View>(R.id.printJobDeleteSeparator).visibility =
                INVISIBLE
        }
    }

    /**
     * @brief Creates view for an item in the jobs list.
     *
     * @param index Index of the print job item in the list to be created
     */
    private fun createItem(index: Int) {
        val factory = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val tempView = factory!!.inflate(R.layout.printjobs_item, this, false)
        val printJobName = tempView.findViewById<TextView>(R.id.printJobName)
        val printJobError = tempView.findViewById<ImageView>(R.id.printJobError)
        val printJobSuccess = tempView.findViewById<ImageView>(R.id.printJobSuccess)
        val printJobDeleteBtn = tempView.findViewById<Button>(R.id.printJobDeleteBtn)
        val printJobDate = tempView.findViewById<TextView>(R.id.printJobDate)
        val pj = _printJobs!![index]
        tempView.tag = pj
        tempView.setOnTouchListener(this)
        printJobName.text = pj.name
        printJobDate.text = formatDate(pj.date)
        printJobDeleteBtn.setOnClickListener(this)
        if (pj.result == JobResult.ERROR) {
            printJobError.visibility = VISIBLE
            printJobSuccess.visibility = GONE
        }
        tempView.setOnClickListener(this)
        _jobsLayout!!.addView(tempView)

        // AppUtils.changeChildrenFont((ViewGroup) tempView, SmartDeviceApp.getAppFont());
        if (index == _printJobs!!.size - 1) {
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
    private fun formatDate(date: Date?): String {
        val dateStr = DateFormat.getDateFormat(context).format(date!!)
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
        _isCollapsed = if (_isCollapsed) {
            animateExpand(true)
            false
        } else {
            animateCollapse(true)
            true
        }
        _groupListener!!.setCollapsed(_printer, _isCollapsed)
    }

    /**
     * @brief Animates expand of a print job group.
     *
     * @param animate true if expand with animation
     */
    private fun animateExpand(animate: Boolean) {
        _jobsLayout!!.visibility = VISIBLE
        val totalHeight =
            _jobsLayout!!.childCount * _rowHeight + (_jobsLayout!!.childCount - 1) * _separatorHeight
        if (animate) {
            for (i in 0 until _jobsLayout!!.childCount) {
                val child = _jobsLayout!!.getChildAt(i)
                val animation = TranslateAnimation(0F, 0F, (-totalHeight).toFloat(), 0F)
                animation.duration = getAnimationDuration(totalHeight).toLong()
                if (i == _jobsLayout!!.childCount - 1) {
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            val newMessage = Message.obtain(_handler, MSG_EXPAND)
                            _handler!!.sendMessage(newMessage)
                        }
                    })
                }
                child.clearAnimation()
                child.startAnimation(animation)
            }
            _layoutListener!!.animateGroups(this, totalHeight, DURATION_MULTIPLIER, true)
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
                _jobsLayout!!.childCount * _rowHeight + (_jobsLayout!!.childCount - 1) * _separatorHeight
            for (i in 0 until _jobsLayout!!.childCount) {
                val child = _jobsLayout!!.getChildAt(i)
                val animation = TranslateAnimation(0F, 0F, 0F, (-totalHeight).toFloat())
                animation.duration = getAnimationDuration(totalHeight).toLong()
                animation.fillAfter = true
                if (i == _jobsLayout!!.childCount - 1) {
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            val newMessage = Message.obtain(_handler, MSG_COLLAPSE)
                            _handler!!.sendMessage(newMessage)
                        }
                    })
                }
                child.clearAnimation()
                child.startAnimation(animation)
            }
            _layoutListener!!.animateGroups(
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
        val totalHeight = _rowHeight + _separatorHeight
        if (_jobsLayout!!.childCount == 1) {
            animateDeleteGroup()
        } else {
            val jobToDelete = _jobsLayout!!.indexOfChild(v) //mPrintJobViews.indexOf(v);
            val deleteAnim = ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f)
            deleteAnim.duration = (totalHeight * DURATION_MULTIPLIER).toLong()
            if (jobToDelete == _jobsLayout!!.childCount - 1) {
                deleteAnim.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        val newMessage = Message.obtain(_handler, MSG_DELETEJOB)
                        newMessage.arg1 = jobToDelete
                        _handler!!.sendMessage(newMessage)
                    }
                })
            }
            v.clearAnimation()
            v.startAnimation(deleteAnim)
            for (i in jobToDelete + 1 until _jobsLayout!!.childCount) {
                val child = _jobsLayout!!.getChildAt(i)
                val animation = TranslateAnimation(0F, 0F, 0F, (-totalHeight).toFloat())
                animation.duration = (totalHeight * DURATION_MULTIPLIER).toLong()
                if (i == _jobsLayout!!.childCount - 1) {
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            val newMessage = Message.obtain(_handler, MSG_DELETEJOB)
                            newMessage.arg1 = jobToDelete
                            _handler!!.sendMessage(newMessage)
                        }
                    })
                }
                child.clearAnimation()
                child.startAnimation(animation)
            }
            _layoutListener!!.animateGroups(
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
        val totalHeight: Int = if (!_isCollapsed) {
            groupHeight
        } else if (resources.getBoolean(R.bool.is_tablet)) {
            _rowHeight
        } else {
            _rowHeight + _separatorHeight
        }
        val animation = ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f)
        animation.duration = getAnimationDuration(totalHeight).toLong()
        animation.fillAfter = true
        animation.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                val newMessage = Message.obtain(_handler, MSG_DELETEGROUP)
                _handler!!.sendMessage(newMessage)
            }
        })
        _printGroupView!!.clearAnimation()
        _printGroupView!!.startAnimation(animation)
        _layoutListener!!.animateGroups(
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
        _printJobGroupLayout!!.isSelected = false
        if (!resources.getBoolean(R.bool.is_tablet)) {
            findViewById<View>(R.id.printJobGroupSeparator).visibility =
                GONE
        }
        _printJobGroupLayout!!.isClickable = true
    }

    /**
     * @brief Collapses view group.
     */
    private fun collapseGroupView() {
        _jobsLayout!!.visibility = GONE
        _printJobGroupLayout!!.isSelected = true
        _printJobGroupLayout!!.findViewById<View>(R.id.printJobGroupDelete).isSelected =
            false
        if (!resources.getBoolean(R.bool.is_tablet)) {
            findViewById<View>(R.id.printJobGroupSeparator).visibility = VISIBLE
        }
        _printJobGroupLayout!!.isClickable = true
    }

    /**
     * @brief Displays delete print jobs dialog when the delete all button is clicked.
     *
     * @param v Delete view clicked
     */
    private fun deleteJobGroup(v: View) {
        _groupListener!!.setPrinterToDelete(this, _printer)
        if (_groupListener!!.showDeleteDialog()) {
            v.findViewById<View>(R.id.printJobGroupDelete).isSelected = true
        }
    }

    /**
     * @brief Deletes the print job group view.
     */
    private fun deletePrintJobGroupView() {
        for (i in 0 until _jobsLayout!!.childCount) {
            _groupListener!!.deleteJobFromList(_jobsLayout!!.getChildAt(i).tag as PrintJob?)
        }
        _groupListener!!.deletePrinterFromList(_printer)
        _layoutListener!!.deletePrintJobsGroup(this)
        (_printGroupView!!.parent as LinearLayout?)!!.removeView(_printGroupView)
    }

    /**
     * @brief Deletes a print job row layout.
     *
     * @param i Index of Print Job View to be deleted
     */
    private fun deletePrintJobView(i: Int) {
        _jobsLayout!!.removeViewAt(i)
        if (_jobsLayout!!.childCount == 0) {
            deletePrintJobGroupView()
        } else if (i == _jobsLayout!!.childCount) {
            // after deletion remove separator in last row
            val lastRow = i - 1
            _jobsLayout!!.getChildAt(lastRow)
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
            if (_layoutListener!!.isDeleteMode) {
                _layoutListener!!.hideDeleteButton()
            } else {
                deleteJobGroup(v)
            }
        } else if (id == R.id.printJobDeleteBtn) {
            // RM#1104 set button to selected to change the button background color when clicked
            v.isSelected = true
            _groupListener!!.showDeleteDialog()
        } else if (id == R.id.printJobsGroupLayout) {
            // if delete button is visible, hide that instead of handling the click
            if (_layoutListener!!.isDeleteMode) {
                _layoutListener!!.hideDeleteButton()
            } else {
                toggleGroupView(v)
            }
        } else if (id == R.id.printJobRow) {
            if (_layoutListener!!.isDeleteMode) {
                _layoutListener!!.hideDeleteButton()
            } else {
                _layoutListener!!.showDeleteButton(this, v)
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