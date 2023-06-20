/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintersScreenTabletView.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.FragmentActivity
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.fragment.PrintSettingsFragment
import jp.co.riso.smartdeviceapp.view.fragment.PrintersFragment
import jp.co.riso.smartprint.R
import java.lang.ref.WeakReference

/**
 * @class PrintersScreenTabletView
 *
 * @brief View for Printers Screen of tablet.
 */
class PrintersScreenTabletView : ViewGroup, View.OnClickListener, Handler.Callback,
    OnItemSelectedListener {
    private var _printerManager: PrinterManager? = null
    private var _selectedPrinter: Printer? = null
    private var _printerList: ArrayList<Printer?>? = null
    private var _deleteViewHolder: ViewHolder? = null
    private var _defaultViewHolder: ViewHolder? = null
    private var _handler: Handler? = null
    private var _deleteItem = -1
    private var _width = 0
    private var _height = 0

    /**
     * @brief Get the index of the selected printer having an opened Default Print Settings.
     *
     * @return Index of the selected default print settings
     * @retval EMPTY_ID No selected printers having an opened Default Print Settings
     */
    var defaultSettingSelected = PrinterManager.EMPTY_ID
        private set
    private var _callbackRef: WeakReference<PrintersViewCallback?>? = null
    private var _pauseableHandler: PauseableHandler? = null

    /**
     * @brief Constructor. <br></br>
     *
     * Instantiate Printers Screen tablet view
     *
     * @param context Application context
     */
    constructor(context: Context) : super(context) {
        init()
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
        init()
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
        init()
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
        val numberOfColumn = (screenWidth / childWidth).coerceAtLeast(MIN_COLUMN)
        val numberOfRow =
            ((childCount + numberOfColumn - 1) / numberOfColumn).coerceAtLeast(MIN_ROW)
        if (numberOfColumn == MIN_COLUMN) {
            if (childWidth * MIN_COLUMN > screenWidth) {
                childWidth = screenWidth / numberOfColumn
            }
        }
        val newRowMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
        _width = childWidth
        _height = childHeight
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
        val screenSize = getScreenDimensions(context as Activity?)
        val numberOfColumn = (screenSize!!.x / _width).coerceAtLeast(MIN_COLUMN)
        val margin = (screenSize.x - _width * numberOfColumn) / 2
        var i = 0
        var y = 0
        while (i < childCount) {
            for (x in 0 until numberOfColumn) {
                val child = getChildAt(i)

                // RM#914 getChildCount() sometimes returns incorrect value (duplicates printer count)
                // add checking based on actual printer list size
                if (child == null || i >= _printerList!!.size) {
                    return
                }
                val lps = child.layoutParams as MarginLayoutParams?
                val fLeft = left + _width * x + lps!!.leftMargin
                val fRight = left + _width * (x + 1) - lps.rightMargin
                val fTop = top + _height * y + lps.topMargin
                val fBot = top + _height * (y + 1) - lps.bottomMargin
                child.layout(fLeft + margin, fTop, fRight + margin, fBot)
                i++
            }
            y++
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val coords = IntArray(2)
        if (_selectedPrinter != null) {
            _selectedPrinter = null
        }
        if (_deleteViewHolder != null) {
            if (_deleteViewHolder!!.deleteButton != null) {
                _deleteViewHolder!!.deleteButton!!.getLocationOnScreen(coords)
                val rect = Rect(
                    coords[0],
                    coords[1],
                    coords[0] + _deleteViewHolder!!.deleteButton!!.width,
                    coords[1]
                            + _deleteViewHolder!!.deleteButton!!.height
                )
                if (rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    return super.onInterceptTouchEvent(ev)
                }
            }
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                setPrinterView(_deleteViewHolder!!)
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
        val newMessage = Message.obtain(_handler, MSG_ADD_PRINTER)
        newMessage.obj = printer
        if (isOnline) {
            newMessage.arg1 = 1
        } else {
            newMessage.arg1 = 0
        }
        _handler!!.sendMessage(newMessage)
    }

    /**
     * @brief Restore the Printers Screen previous state.
     *
     * @param printer Printer object
     * @param deleteItem Delete item index
     * @param settingItem Default Print Setting item
     */
    fun restoreState(printer: ArrayList<Printer?>?, deleteItem: Int, settingItem: Int) {
        _printerList = printer
        _deleteItem = deleteItem
        defaultSettingSelected = settingItem
        for (i in printer!!.indices) {
            addToTabletPrinterScreen(printer[i], false)
        }
        val newMessage = Message.obtain(_handler, MSG_SET_UPDATE_VIEWS)
        _handler!!.sendMessage(newMessage)
    }

    /**
     * @brief Get the delete view index.
     *
     * @return Delete view index
     */
    val deleteItemPosition: Int
        get() {
            if (_deleteViewHolder != null) {
                _deleteItem = indexOfChild(_deleteViewHolder!!.onlineIndicator!!.tag as View)
            }
            return _deleteItem
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
            for (index in _printerList!!.indices) {
                if (_printerList!![index]!!.id == printerId) {
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
        _callbackRef = WeakReference(callback)
    }

    /**
     * @brief This function is called when deletion of the printer view is confirmed.
     *
     * @param relayout Re-layout the entire view
     */
    fun confirmDeletePrinterView(relayout: Boolean) {
        if (_deleteViewHolder == null) {
            return
        }
        removeView(_deleteViewHolder!!.onlineIndicator!!.tag as View?)
        _deleteViewHolder = null
        _deleteItem = PrinterManager.EMPTY_ID
        if (relayout) {
            removeAllViews()
            restoreState(_printerList, PrinterManager.EMPTY_ID, PrinterManager.EMPTY_ID)
        }
    }

    /**
     * @brief This function is called when deletion of the printer view is confirmed.
     */
    fun resetDeletePrinterView() {
        if (_deleteViewHolder != null) {
            _deleteViewHolder = null
            _deleteItem = PrinterManager.EMPTY_ID
        }
    }

    /**
     * @brief Set the pauseable handler object.
     *
     * @param handler Pauseable handler
     */
    fun setPauseableHandler(handler: PauseableHandler?) {
        _pauseableHandler = handler
    }
    // ================================================================================
    // Private methods
    // ================================================================================
    /**
     * @brief Initialize PrinterScreenTabletView.
     */
    private fun init() {
        _printerManager = getInstance(SmartDeviceApp.appContext!!)
        _handler = Handler(Looper.myLooper()!!, this)
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
        val printerItem = viewHolder.deleteButton!!.parent as PrintersContainerView?
        if (printerItem!!.default) {
            printerItem.default = false
            viewHolder.defaultPrinter!!.setSelection(1, true)
            viewHolder.defaultPrinterAdapter!!.isNoDisabled = false
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
        if (_defaultViewHolder != null) {
            setPrinterViewToNormal(_defaultViewHolder)
        }
        val printerItem = viewHolder.printerName!!.parent as PrintersContainerView?
        resetDeletePrinterView()
        if (printerItem!!.default) {
            return
        }
        if (_defaultViewHolder != null) {
            setPrinterViewToNormal(_defaultViewHolder)
            _defaultViewHolder = null
        }
        printerItem.default = true
        viewHolder.defaultPrinter!!.setSelection(0, true)
        viewHolder.defaultPrinterAdapter!!.isNoDisabled = true
        _defaultViewHolder = viewHolder
    }

    /**
     * @brief Reset view holder.
     *
     * @param viewHolder View holder to reset
     */
    private fun setPrinterView(viewHolder: ViewHolder) {
        val printer = viewHolder.ipAddress!!.tag as Printer?
        if (_printerManager!!.defaultPrinter == printer!!.id) {
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
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val pView = inflater!!.inflate(R.layout.printers_container_item, this, false)
        // AppUtils.changeChildrenFont((ViewGroup) pView, SmartDeviceApp.getAppFont());
        var printerName = printer.name
        if (printerName == null || printerName.isEmpty()) {
            printerName = context.resources.getString(R.string.ids_lbl_no_name)
        }
        addView(pView)
        val viewHolder = ViewHolder()
        viewHolder.printerName = pView.findViewById(R.id.txt_printerName)
        viewHolder.deleteButton = pView.findViewById(R.id.btn_delete)
        viewHolder.onlineIndicator = pView.findViewById(R.id.img_onOff)
        viewHolder.ipAddress = pView.findViewById(R.id.infoIpAddress)
        viewHolder.macAddress = pView.findViewById(R.id.infoMacAddress)
        viewHolder.printSettings = pView.findViewById(R.id.default_print_settings)
        viewHolder.port = pView.findViewById(R.id.input_port)
        viewHolder.defaultPrinter = pView.findViewById(R.id.default_printer_spinner)
        viewHolder.defaultPrinterAdapter =
            DefaultPrinterArrayAdapter(context, R.layout.printerinfo_port_item)
        viewHolder.defaultPrinterAdapter!!.add(context.getString(R.string.ids_lbl_yes))
        viewHolder.defaultPrinterAdapter!!.add(context.getString(R.string.ids_lbl_no))
        viewHolder.defaultPrinterAdapter!!.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        viewHolder.defaultPrinter!!.adapter = viewHolder.defaultPrinterAdapter
        if (_printerManager!!.defaultPrinter == printer.id) viewHolder.defaultPrinter!!.setSelection(
            0,
            true
        ) //yes
        else viewHolder.defaultPrinter!!.setSelection(1, true) //no
        val portAdapter = ArrayAdapter<String>(context, R.layout.printerinfo_port_item)
        // Assumption is that LPR is always available
        portAdapter.add(context.getString(R.string.ids_lbl_port_lpr))
        if (printer.config!!.isRawAvailable) {
            portAdapter.add(context.getString(R.string.ids_lbl_port_raw))
            portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        } else {
            viewHolder.port?.visibility = GONE
            // Port setting is always displayed as LPR
            pView.findViewById<View>(R.id.defaultPort).visibility = VISIBLE
        }
        viewHolder.port!!.adapter = portAdapter
        viewHolder.port!!.setSelection(printer.portSetting!!.ordinal)
        viewHolder.printerName!!.text = printerName
        viewHolder.ipAddress!!.text = printer.ipAddress
        if (printer.macAddress == null || printer.macAddress == "") {
            viewHolder.macAddress!!.text = "-"
        } else {
            viewHolder.macAddress!!.text = printer.macAddress
        }
        viewHolder.deleteButton!!.setOnClickListener(this)
        viewHolder.printSettings!!.setOnClickListener(this)
        viewHolder.printSettings!!.findViewById<View>(R.id.print_settings).isClickable =
            false
        viewHolder.port!!.onItemSelectedListener = this
        viewHolder.defaultPrinter!!.onItemSelectedListener = this
        pView.tag = viewHolder
        viewHolder.printerName!!.tag = viewHolder
        viewHolder.deleteButton!!.tag = viewHolder
        viewHolder.ipAddress!!.tag = printer
        viewHolder.macAddress!!.tag = printer
        viewHolder.printSettings!!.tag = printer
        viewHolder.printSettings!!.setTag(ID_TAG_DEFAULTSETTINGS, viewHolder)
        viewHolder.onlineIndicator!!.tag = pView
        viewHolder.port!!.tag = printer
        viewHolder.defaultPrinter!!.tag = viewHolder
        if (isOnline) {
            viewHolder.onlineIndicator!!.setImageResource(R.drawable.img_btn_printer_status_online)
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
            _deleteViewHolder = v.tag as ViewHolder?
            if (_callbackRef != null && _callbackRef!!.get() != null) {
                printer = (_deleteViewHolder!!.ipAddress!!.tag as Printer?)!!
                _callbackRef!!.get()!!.onPrinterDeleteClicked(printer)
            }
        } else if (id == R.id.default_print_settings) {
            _selectedPrinter = v.tag as Printer?
            if (context != null && context is MainActivity) {
                val activity = context as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    // Always make new
                    var fragment: PrintSettingsFragment? = null
                    if (fragment == null) {
                        fragment = PrintSettingsFragment()
                        fragment.setPrinterId(_selectedPrinter!!.id)
                        // use new print settings retrieved from the database
                        fragment.setPrintSettings(
                            PrintSettings(
                                _selectedPrinter!!.id,
                                _selectedPrinter!!.printerType!!
                            )
                        )
                        fragment.setTargetFragmentPrinters()

                        if (_pauseableHandler != null) {
                            val msg = Message.obtain(
                                _pauseableHandler,
                                PrintersFragment.MSG_PRINTSETTINGS_BUTTON
                            )
                            msg.obj = fragment
                            msg.arg1 = _selectedPrinter!!.id
                            _pauseableHandler!!.sendMessage(msg)
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
                if (_deleteItem != -1) {
                    val view = getChildAt(_deleteItem)
                    if (view != null) {
                        _deleteViewHolder =
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
                if (!_printerManager!!.isExists(msg.obj as Printer?) || childCount != _printerManager!!.printerCount) {
                    addToTabletPrinterScreen(msg.obj as Printer?, msg.arg1 > 0)
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
            val printer = parentView.tag as Printer?
            var port = PortSetting.LPR
            when (position) {
                1 -> port = PortSetting.RAW
                else -> {}
            }
            printer!!.portSetting = port
            _printerManager!!.updatePortSettings(printer.id, port)
        } else if (parentId == R.id.default_printer_spinner) {
            when (position) {
                0 -> {
                    val viewHolder = parentView.tag as ViewHolder?
                    val printer = viewHolder!!.ipAddress!!.tag as Printer?
                    if (_printerManager!!.defaultPrinter == printer!!.id) {
                        return
                    }
                    if (_printerManager!!.setDefaultPrinter(printer)) {
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
    inner class ViewHolder {
        internal var onlineIndicator: ImageView? = null
        internal var printerName: TextView? = null
        internal var deleteButton: Button? = null
        internal var ipAddress: TextView? = null
        internal var macAddress: TextView? = null
        internal var port: Spinner? = null
        internal var defaultPrinter: Spinner? = null
        internal var defaultPrinterAdapter: DefaultPrinterArrayAdapter? = null
        internal var printSettings: LinearLayout? = null
    }

    companion object {
        private const val MSG_ADD_PRINTER = 0x01
        private const val MSG_SET_UPDATE_VIEWS = 0x02
        private const val MIN_COLUMN = 2
        private const val MIN_ROW = 1
        private const val ID_TAG_DEFAULTSETTINGS = 0x11000001
    }
}