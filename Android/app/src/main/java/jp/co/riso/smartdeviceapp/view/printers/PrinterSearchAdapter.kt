/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterSearchAdapter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.content.Context
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.isExists
import android.widget.ArrayAdapter
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import jp.co.riso.smartprint.R
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.model.Printer

/**
 * @class PrinterSearchAdapter
 *
 * @brief Array Adapter used for Printers Search Screen
 */
class PrinterSearchAdapter(context: Context?, private val layoutId: Int, values: List<Printer?>?) :
    ArrayAdapter<Printer?>(
        context!!, layoutId, values!!
    ), View.OnClickListener {

    private var mSearchAdapterInterface: PrinterSearchAdapterInterface? = null
    private val mPrinterManager: PrinterManager?

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val printer = getItem(position)
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(layoutId, parent, false)
            // AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());
            viewHolder = ViewHolder()
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        initializeView(viewHolder, convertView, printer, position)
        return convertView!!
    }

    /**
     * @brief Initialize the view of the printer.
     *
     * @param viewHolder View Holder of the printer object
     * @param convertView Row view
     * @param printer Printer object
     * @param position Position or index of the printer object
     */
    fun initializeView(
        viewHolder: ViewHolder?,
        convertView: View?,
        printer: Printer?,
        position: Int
    ) {
        if (viewHolder == null || convertView == null || printer == null) {
            return
        }
        val separator: View
        val printerName = printer.name
        viewHolder.mPrinterName = convertView.findViewById(R.id.printerText)
        viewHolder.mIpAddress = convertView.findViewById(R.id.ipAddressText)
        viewHolder.mAddedIndicator = convertView.findViewById(R.id.addPrinterButton)
        viewHolder.mPrinterName?.setText(printer.name)
        viewHolder.mIpAddress?.setText(printer.ipAddress)
        viewHolder.mAddedIndicator?.setBackgroundResource(R.drawable.selector_printersearch_addprinter)
        viewHolder.mAddedIndicator?.setTag(position)
        viewHolder.mAddedIndicator?.setClickable(false)
        separator = convertView.findViewById(R.id.printers_separator)
        if (position == count - 1) {
            separator.visibility = View.GONE
        } else {
            separator.visibility = View.VISIBLE
        }
        if (mPrinterManager!!.isExists(printer)) {
            convertView.isClickable = false
            viewHolder.mAddedIndicator?.setActivated(true)
        } else {
            viewHolder.mAddedIndicator?.setActivated(false)
        }
        if (printerName!!.isEmpty()) {
            viewHolder.mPrinterName?.setText(context.resources.getString(R.string.ids_lbl_no_name))
        }
        convertView.setOnClickListener(this)
        convertView.tag = viewHolder
    }

    /**
     * @brief Set Printer Search Screen Adapter Interface.
     *
     * @param searchAdapterInterface Printer search adapter interface
     */
    fun setSearchAdapterInterface(searchAdapterInterface: PrinterSearchAdapterInterface?) {
        mSearchAdapterInterface = searchAdapterInterface
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class ViewHolder
     *
     * @brief Printer Search Screen view holder.
     */
    class ViewHolder {
        var mAddedIndicator: ImageView? = null
        var mPrinterName: TextView? = null
        var mIpAddress: TextView? = null
    }
    // ================================================================================
    // Interface
    // ================================================================================
    /**
     * @interface PrinterSearchAdapterInterface
     *
     * @brief Printer Search Screen interface.
     */
    interface PrinterSearchAdapterInterface {
        /**
         * @brief On add printer callback. <br></br>
         *
         * Callback called to add a searched printer to the Printers Screen.
         *
         * @param printer Searched printer
         *
         * @retval 0 Success
         * @retval -1 Error
         */
        fun onAddPrinter(printer: Printer?): Int
    }

    // ================================================================================
    // Interface View.OnClick
    // ================================================================================
    override fun onClick(v: View) {
        if (v.id == R.id.printer_search_row) {
            val viewHolder = v.tag as ViewHolder
            val printer = getItem((viewHolder.mAddedIndicator!!.tag as Int))
            if (viewHolder.mAddedIndicator!!.isActivated) {
                return
            }
            if (mSearchAdapterInterface!!.onAddPrinter(printer) != -1) {
                v.isActivated = true
            }
        }
    }

    /**
     * @brief Constructor.
     *
     * @param context Application context
     * @param resource Resource ID to be used as Searched printer row
     * @param values Searched printers list
     */
    init {
        mPrinterManager = getInstance(SmartDeviceApp.appContext!!)
    }
}