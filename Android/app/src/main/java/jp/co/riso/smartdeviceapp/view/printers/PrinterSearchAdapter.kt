/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterSearchAdapter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartprint.R

/**
 * @class PrinterSearchAdapter
 *
 * @brief Array Adapter used for Printers Search Screen
 */
class PrinterSearchAdapter(context: Context?, private val _layoutId: Int, values: List<Printer?>?) :
    ArrayAdapter<Printer?>(
        context!!, _layoutId, values!!
    ), View.OnClickListener {
    private var _searchAdapterInterface: PrinterSearchAdapterInterface? = null
    private val _printerManager: PrinterManager? = getInstance(SmartDeviceApp.appContext!!)
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val printer = getItem(position)
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(_layoutId, parent, false)
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
        val printerName = printer.name
        viewHolder.printerName = convertView.findViewById(R.id.printerText)
        viewHolder.ipAddress = convertView.findViewById(R.id.ipAddressText)
        viewHolder.addedIndicator = convertView.findViewById(R.id.addPrinterButton)
        viewHolder.printerName?.text = printer.name
        viewHolder.ipAddress?.text = printer.ipAddress
        viewHolder.addedIndicator?.setBackgroundResource(R.drawable.selector_printersearch_addprinter)
        viewHolder.addedIndicator?.tag = position
        viewHolder.addedIndicator?.isClickable = false
        val separator: View = convertView.findViewById(R.id.printers_separator)
        if (position == count - 1) {
            separator.visibility = View.GONE
        } else {
            separator.visibility = View.VISIBLE
        }
        if (_printerManager!!.isExists(printer)) {
            convertView.isClickable = false
            viewHolder.addedIndicator?.isActivated = true
        } else {
            viewHolder.addedIndicator?.isActivated = false
        }
        if (printerName!!.isEmpty()) {
            viewHolder.printerName?.text = context.resources.getString(R.string.ids_lbl_no_name)
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
        _searchAdapterInterface = searchAdapterInterface
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class ViewHolder
     *
     * @brief Printer Search Screen view holder.
     */
    inner class ViewHolder {
        internal var addedIndicator: ImageView? = null
        internal var printerName: TextView? = null
        internal var ipAddress: TextView? = null
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
            val printer = getItem((viewHolder.addedIndicator!!.tag as Int))
            if (viewHolder.addedIndicator!!.isActivated) {
                return
            }
            if (_searchAdapterInterface!!.onAddPrinter(printer) != -1) {
                v.isActivated = true
            }
        }
    }

}