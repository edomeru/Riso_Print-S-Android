/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersScreenTabletView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
//import jp.co.riso.smartdeviceapp.view.printers.PrintersContainerView.default
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.defaultPrinter
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.isExists
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.printerCount
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.updatePortSettings
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.setDefaultPrinter
import android.widget.AdapterView.OnItemSelectedListener
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.smartprint.R
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.widget.*
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.fragment.PrintSettingsFragment
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.dialog.DialogUtils
import androidx.fragment.app.FragmentActivity
import jp.co.riso.smartdeviceapp.model.Printer
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * @class PrintersScreenTabletView
 *
 * @brief View for Printers Screen of tablet.
 */
class PrintersScreenTabletView : ViewGroup, View.OnClickListener, Handler.Callback,
    OnItemSelectedListener {
    private var mPrinterManager: PrinterManager? = null
    private var mSelectedPrinter: Printer? = null
    private var mPrinterList: ArrayList<Printer?>? = null
    private var mDeleteViewHolder: ViewHolder? = null
    private var mDefaultViewHolder: ViewHolder? = null
    private var mHandler: Handler? = null
    private var mDeleteItem = -1
    private var mWidth = 0
    private var mHeight = 0

    /**
     * @brief Get the index of the selected printer having an opened Default Print Settings.
     *
     * @return Index of the selected default print settings
     * @retval EMPTY_ID No selected printers having an opened Default Print Settings
     */
    var defaultSettingSelected = PrinterManager.EMPTY_ID
        private set
    private var mCallbackRef: WeakReference<PrintersViewCallback?>? = null
    private var mPauseableHandler: PauseableHandler? = null

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate Printers Screen tablet view
     *
     * @param context Application context
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate Printers Screen tablet view
     *
     * @param context Application context
     * @param attrs Layout attributes
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate Printers Screen tablet view
     *
     * @param context Application context
     * @param attrs Layout attributes
     * @param defStyle Layout styles
     */
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    override fun generateLayoutParams(layoutParams: LayoutParams): LayoutParams {
        return MarginLayoutParams(layoutParams)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val screenWidth = MeasureSpec.getSize(widthMeasureSpec)
        val childCount = childCount
        var childWidth = context.resources.getDimensionPixelSize(R.dimen.printers_view_width)
        val childHeight = context.resources.getDimensionPixelSize(R.dimen.printers_view_height)
        val numberOfColumn = Math.max(screenWidth / childWidth, MIN_COLUMN)
        val numberOfRow = Math.max((childCount + numberOfColumn - 1) / numberOfColumn, MIN_ROW)
        if (numberOfColumn == MIN_COLUMN) {
            if (childWidth * MIN_COLUMN > screenWidth) {
                childWidth = screenWidth / numberOfColumn
            }
        }
        val newRowMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
        mWidth = childWidth
        mHeight = childHeight
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                // Measure the child.
                measureChildWithMargins(child, newRowMeasureSpec, 0, newHeightMeasureSpec, 0)
            }
        }
        setMeasuredDimension(screenWidth, childHeight * numberOfRow)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val childCount = childCount
        val screenSize = getScreenDimensions(context as Activity)
        val numberOfColumn = Math.max(screenSize!!.x / mWidth, MIN_COLUMN)
        val margin = (screenSize.x - mWidth * numberOfColumn) / 2
        var i = 0
        var y = 0
        while (i < childCount) {
            for (x in 0 until numberOfColumn) {
                val child = getChildAt(i)

                // RM#914 getChildCount() sometimes returns incorrect value (duplicates printer count)
                // add checking based on actual printer list size
                if (child == null || i >= mPrinterList!!.size) {
                    return
                }
                val lps = child.layoutParams as MarginLayoutParams
                val fLeft = left + mWidth * x + lps.leftMargin
                val fRight = left + mWidth * (x + 1) - lps.rightMargin
                val fTop = top + mHeight * y + lps.topMargin
                val fBot = top + mHeight * (y + 1) - lps.bottomMargin
                child.layout(fLeft + margin, fTop, fRight + margin, fBot)
                i++
            }
            y++
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val coords = IntArray(2)
        if (mSelectedPrinter != null) {
            mSelectedPrinter = null
        }
        if (mDeleteViewHolder != null) {
            if (mDeleteViewHolder!!.mDeleteButton != null) {
                mDeleteViewHolder!!.mDeleteButton!!.getLocationOnScreen(coords)
                val rect = Rect(
                    coords[0],
                    coords[1],
                    coords[0] + mDeleteViewHolder!!.mDeleteButton!!.width,
                    coords[1]
                            + mDeleteViewHolder!!.mDeleteButton!!.height
                )
                if (rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    return super.onInterceptTouchEvent(ev)
                }
            }
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                setPrinterView(mDeleteViewHolder!!)
            }
            return true
        }
        return false
    }
    // ================================================================================
    // Public Methods
    // ================================================================================
    /**
     * @brief Add printer to the Printers Screen.
     *
     * @param printer Printer object
     * @param isOnline Printer online status
     */
    fun onAddedNewPrinter(printer: Printer?, isOnline: Boolean) {
        val newMessage = Message.obtain(mHandler, MSG_ADD_PRINTER)
        newMessage.obj = printer
        if (isOnline) {
            newMessage.arg1 = 1
        } else {
            newMessage.arg1 = 0
        }
        mHandler!!.sendMessage(newMessage)
    }

    /**
     * @brief Restore the Printers Screen previous state.
     *
     * @param printer Printer object
     * @param deleteItem Delete item index
     * @param settingItem Default Print Setting item
     */
    fun restoreState(printer: ArrayList<Printer?>?, deleteItem: Int, settingItem: Int) {
        mPrinterList = printer
        mDeleteItem = deleteItem
        defaultSettingSelected = settingItem
        for (i in printer!!.indices) {
            addToTabletPrinterScreen(printer[i], false)
        }
        val newMessage = Message.obtain(mHandler, MSG_SET_UPDATE_VIEWS)
        mHandler!!.sendMessage(newMessage)
    }

    /**
     * @brief Get the delete view index.
     *
     * @return Delete view index
     */
    val deleteItemPosition: Int
        get() {
            if (mDeleteViewHolder != null) {
                mDeleteItem = indexOfChild(mDeleteViewHolder!!.mOnlineIndicator!!.tag as View)
            }
            return mDeleteItem
        }

    /**
     * @brief Sets the selected state of a Printer. <br></br>
     *
     * This selected state is set when Default Print Settings is pressed.
     *
     * @param printerId Printer ID of the selected printer
     * @param state Selected state of the Printer
     */
    fun setDefaultSettingSelected(printerId: Int, state: Boolean) {
        if (printerId != PrinterManager.EMPTY_ID) {
            for (index in mPrinterList!!.indices) {
                if (mPrinterList!![index]?.id == printerId) {
                    defaultSettingSelected = index
                }
            }
        }
        if (defaultSettingSelected != PrinterManager.EMPTY_ID) {
            getChildAt(defaultSettingSelected).findViewById<View>(R.id.default_print_settings).isSelected =
                state
        }
        if (!state) {
            defaultSettingSelected = PrinterManager.EMPTY_ID
        }
    }

    /**
     * @brief Sets the PrintersViewCallback function.
     *
     * @param callback Callback function
     */
    fun setPrintersViewCallback(callback: PrintersViewCallback?) {
        mCallbackRef = WeakReference(callback)
    }

    /**
     * @brief This function is called when deletion of the printer view is confirmed.
     *
     * @param relayout Re-layout the entire view
     */
    fun confirmDeletePrinterView(relayout: Boolean) {
        if (mDeleteViewHolder == null) {
            return
        }
        removeView(mDeleteViewHolder!!.mOnlineIndicator!!.tag as View)
        mDeleteViewHolder = null
        mDeleteItem = PrinterManager.EMPTY_ID
        if (relayout) {
            removeAllViews()
            restoreState(mPrinterList, PrinterManager.EMPTY_ID, PrinterManager.EMPTY_ID)
        }
    }

    /**
     * @brief This function is called when deletion of the printer view is confirmed.
     */
    fun resetDeletePrinterView() {
        if (mDeleteViewHolder != null) {
            mDeleteViewHolder = null
            mDeleteItem = PrinterManager.EMPTY_ID
        }
    }

    /**
     * @brief Set the pauseable handler object.
     *
     * @param handler Pauseable handler
     */
    fun setPauseableHandler(handler: PauseableHandler?) {
        mPauseableHandler = handler
    }
    // ================================================================================
    // Private methods
    // ================================================================================
    /**
     * @brief Initialize PrinterScreenTabletView.
     *
     * @param context Application context
     */
    private fun init(context: Context) {
        mPrinterManager = getInstance(SmartDeviceApp.getAppContext())
        mHandler = Handler(Looper.myLooper()!!, this)
    }

    /**
     * @brief Set view holder to normal.
     *
     * @param viewHolder View holder to set as normal.
     */
    private fun setPrinterViewToNormal(viewHolder: ViewHolder?) {
        if (viewHolder == null) {
            return
        }
        val printerItem = viewHolder.mDeleteButton!!.parent as PrintersContainerView
        if (printerItem.default) {
            printerItem.default = false
            viewHolder.mDefaultPrinter!!.setSelection(1, true)
            viewHolder.mDefaultPrinterAdapter!!.isNoDisabled = false
        }
        resetDeletePrinterView()
    }

    /**
     * @brief Set view holder to default.
     *
     * @param viewHolder View holder to set as default
     */
    private fun setPrinterViewToDefault(viewHolder: ViewHolder?) {
        if (viewHolder == null) {
            return
        }
        if (mDefaultViewHolder != null) {
            setPrinterViewToNormal(mDefaultViewHolder)
        }
        val printerItem = viewHolder.mPrinterName!!.parent as PrintersContainerView
        resetDeletePrinterView()
        if (printerItem.default) {
            return
        }
        if (mDefaultViewHolder != null) {
            setPrinterViewToNormal(mDefaultViewHolder)
            mDefaultViewHolder = null
        }
        printerItem.default = true
        viewHolder.mDefaultPrinter!!.setSelection(0, true)
        viewHolder.mDefaultPrinterAdapter!!.isNoDisabled = true
        mDefaultViewHolder = viewHolder
    }

    /**
     * @brief Reset view holder.
     *
     * @param viewHolder View holder to reset
     */
    private fun setPrinterView(viewHolder: ViewHolder) {
        val printer = viewHolder.mIpAddress!!.tag as Printer
        if (mPrinterManager!!.defaultPrinter == printer.id) {
            setPrinterViewToDefault(viewHolder)
        } else {
            setPrinterViewToNormal(viewHolder)
        }
    }

    /**
     * @brief Adds printer object to the Printers Screen.
     *
     * @param printer Printer object
     * @param isOnline Printer online status
     */
    private fun addToTabletPrinterScreen(printer: Printer?, isOnline: Boolean) {
        if (printer == null) {
            return
        }
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val pView = inflater.inflate(R.layout.printers_container_item, this, false)
        // AppUtils.changeChildrenFont((ViewGroup) pView, SmartDeviceApp.getAppFont());
        var printerName = printer.name
        if (printerName == null || printerName.isEmpty()) {
            printerName = context.resources.getString(R.string.ids_lbl_no_name)
        }
        addView(pView)
        var viewHolder = ViewHolder()
        viewHolder.mPrinterName = pView.findViewById(R.id.txt_printerName)
        viewHolder.mDeleteButton = pView.findViewById(R.id.btn_delete)
        viewHolder.mOnlineIndicator = pView.findViewById(R.id.img_onOff)
        viewHolder.mIpAddress = pView.findViewById(R.id.inputIpAddress)
        viewHolder.mPrintSettings = pView.findViewById(R.id.default_print_settings)
        viewHolder.mPort = pView.findViewById(R.id.input_port)
        viewHolder.mDefaultPrinter = pView.findViewById(R.id.default_printer_spinner)
        viewHolder.mDefaultPrinterAdapter =
            DefaultPrinterArrayAdapter(context, R.layout.printerinfo_port_item)
        viewHolder.mDefaultPrinterAdapter!!.add(context.getString(R.string.ids_lbl_yes))
        viewHolder.mDefaultPrinterAdapter!!.add(context.getString(R.string.ids_lbl_no))
        viewHolder.mDefaultPrinterAdapter!!.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        viewHolder.mDefaultPrinter!!.adapter = viewHolder.mDefaultPrinterAdapter
        if (mPrinterManager!!.defaultPrinter == printer.id) viewHolder.mDefaultPrinter!!.setSelection(
            0,
            true
        ) //yes
        else viewHolder.mDefaultPrinter!!.setSelection(1, true) //no
        val portAdapter = ArrayAdapter<String>(context, R.layout.printerinfo_port_item)
        // Assumption is that LPR is always available
        portAdapter.add(context.getString(R.string.ids_lbl_port_lpr))
        if (printer.config!!.isRawAvailable) {
            portAdapter.add(context.getString(R.string.ids_lbl_port_raw))
            portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        } else {
            viewHolder.mPort?.visibility = GONE
            // Port setting is always displayed as LPR
            pView.findViewById<View>(R.id.defaultPort).visibility = VISIBLE
        }
        viewHolder.mPort?.adapter = portAdapter
        viewHolder.mPort?.setSelection(printer.portSetting!!.ordinal)
        viewHolder.mPrinterName?.text = printerName
        viewHolder.mIpAddress?.text = printer.ipAddress
        viewHolder.mDeleteButton?.setOnClickListener(this)
        viewHolder.mPrintSettings?.setOnClickListener(this)
        viewHolder.mPrintSettings?.findViewById<View>(R.id.print_settings)?.isClickable =
            false
        viewHolder.mPort?.onItemSelectedListener = this
        viewHolder.mDefaultPrinter?.onItemSelectedListener = this
        pView.tag = viewHolder
        viewHolder.mPrinterName?.tag = viewHolder
        viewHolder.mDeleteButton?.tag = viewHolder
        viewHolder.mIpAddress?.tag = printer
        viewHolder.mPrintSettings?.tag = printer
        viewHolder.mPrintSettings?.setTag(ID_TAG_DEFAULTSETTINGS, viewHolder)
        viewHolder.mOnlineIndicator?.tag = pView
        viewHolder.mPort?.tag = printer
        viewHolder.mDefaultPrinter?.tag = viewHolder
        if (isOnline) {
            viewHolder.mOnlineIndicator?.setImageResource(R.drawable.img_btn_printer_status_online)
        }
        setPrinterView(viewHolder)
    }

    // ================================================================================
    // INTERFACE - onClick
    // ================================================================================
    override fun onClick(v: View) {
        val printer: Printer
        val id = v.id
        if (id == R.id.btn_delete) {
            mDeleteViewHolder = v.tag as ViewHolder
            if (mCallbackRef != null && mCallbackRef!!.get() != null) {
                printer = mDeleteViewHolder!!.mIpAddress!!.tag as Printer
                mCallbackRef!!.get()!!.onPrinterDeleteClicked(printer)
            }
        } else if (id == R.id.default_print_settings) {
            mSelectedPrinter = v.tag as Printer
            if (context != null && context is MainActivity) {
                val activity = context as MainActivity
                if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                    // Always make new
                    var fragment: PrintSettingsFragment? = null
                    if (fragment == null) {
                        fragment = PrintSettingsFragment()
                        fragment.setPrinterId(mSelectedPrinter!!.id)
                        // use new print settings retrieved from the database
                        fragment.setPrintSettings(
                            PrintSettings(
                                mSelectedPrinter!!.id,
                                mSelectedPrinter!!.printerType
                            )
                        )
                        if (mPauseableHandler != null) {
                            val msg = Message.obtain(
                                mPauseableHandler,
                                PrintersFragment.MSG_PRINTSETTINGS_BUTTON
                            )
                            msg.obj = fragment
                            msg.arg1 = mSelectedPrinter!!.id
                            mPauseableHandler!!.sendMessage(msg)
                        }
                    }
                } else {
                    activity.closeDrawers()
                }
            }
        }
    }

    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_SET_UPDATE_VIEWS -> {
                if (mDeleteItem != -1) {
                    val view = getChildAt(mDeleteItem)
                    if (view != null) {
                        mDeleteViewHolder =
                            view.findViewById<View>(R.id.txt_printerName).tag as ViewHolder
                    }
                }
                if (defaultSettingSelected != PrinterManager.EMPTY_ID) {
                    setDefaultSettingSelected(PrinterManager.EMPTY_ID, true)
                }
                return true
            }
            MSG_ADD_PRINTER -> {
                // BUG#10003: Check if printer to add already exists OR is added already on printers list view to avoid adding multiple views for the same printer
                if (!mPrinterManager!!.isExists(msg.obj as Printer) || childCount != mPrinterManager!!.printerCount) {
                    addToTabletPrinterScreen(msg.obj as Printer, msg.arg1 > 0)
                }
                return true
            }
        }
        return false
    }

    // ================================================================================
    // INTERFACE - onItemSelected
    // ================================================================================
    override fun onItemSelected(
        parentView: AdapterView<*>,
        selectedItemView: View,
        position: Int,
        id: Long
    ) {
        val parentId = parentView.id
        if (parentId == R.id.input_port) {
            val printer = parentView.tag as Printer
            var port = PortSetting.LPR
            when (position) {
                1 -> port = PortSetting.RAW
                else -> {}
            }
            printer.portSetting = port
            mPrinterManager!!.updatePortSettings(printer.id, port)
        } else if (parentId == R.id.default_printer_spinner) {
            when (position) {
                0 -> {
                    val viewHolder = parentView.tag as ViewHolder
                    val printer = viewHolder.mIpAddress!!.tag as Printer
                    if (mPrinterManager!!.defaultPrinter == printer.id) {
                        return
                    }
                    if (mPrinterManager!!.setDefaultPrinter(printer)) {
                        setPrinterViewToDefault(viewHolder)
                    } else {
                        val info = InfoDialogFragment.newInstance(
                            context.getString(R.string.ids_lbl_printers),
                            context.getString(R.string.ids_err_msg_db_failure),
                            context.getString(R.string.ids_lbl_ok)
                        )
                        DialogUtils.displayDialog(
                            context as FragmentActivity,
                            PrintersFragment.KEY_PRINTER_ERR_DIALOG,
                            info
                        )
                    }
                }
                else -> {}
            }
        }
    }

    override fun onNothingSelected(parentView: AdapterView<*>?) {
        // Do nothing
    }
    // ================================================================================
    // INTERFACE - PrintersViewCallback
    // ================================================================================
    /**
     * @interface PrintersViewCallback
     *
     * @brief Printers Screen Interface.
     */
    interface PrintersViewCallback {
        /**
         * @brief Dialog which is displayed to confirm printer delete.
         *
         * @param printer Printer to be deleted
         */
        fun onPrinterDeleteClicked(printer: Printer?)
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class ViewHolder
     *
     * @brief Printers Screen view holder for tablet.
     */
    class ViewHolder {
        var mOnlineIndicator: ImageView? = null
        var mPrinterName: TextView? = null
        var mDeleteButton: Button? = null
        var mIpAddress: TextView? = null
        var mPort: Spinner? = null
        var mDefaultPrinter: Spinner? = null
        var mDefaultPrinterAdapter: DefaultPrinterArrayAdapter? = null
        var mPrintSettings: LinearLayout? = null
    }

    companion object {
        private const val MSG_ADD_PRINTER = 0x01
        private const val MSG_SET_UPDATE_VIEWS = 0x02
        private const val MIN_COLUMN = 2
        private const val MIN_ROW = 1
        private const val ID_TAG_DEFAULTSETTINGS = 0x11000001
    }
}