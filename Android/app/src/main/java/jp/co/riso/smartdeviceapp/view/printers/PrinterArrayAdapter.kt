/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterArrayAdapter.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.content.Context
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.defaultPrinter
import android.widget.ArrayAdapter
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import jp.co.riso.smartprint.R
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.model.Printer
import java.lang.ref.WeakReference

/**
 * @class PrinterArrayAdapter
 *
 * @brief Array Adapter used for Printers Screen of a phone
 */
class PrinterArrayAdapter(context: Context?, private val mLayoutId: Int, values: List<Printer?>?) :
    ArrayAdapter<Printer?>(
        context!!, mLayoutId, values!!
    ), View.OnClickListener {

    private var mCallbackRef: WeakReference<PrinterArrayAdapterInterface?>? = null
    private val mPrinterManager: PrinterManager?
    private var mDeleteViewHolder: ViewHolder?

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var viewHolder: ViewHolder
        val printer = getItem(position)
        val separator: View
        val printerName = printer!!.name
        if (convertView == null) {
            convertView = inflater.inflate(mLayoutId, parent, false)
            viewHolder = ViewHolder()
            viewHolder.mPrinterName = convertView.findViewById(R.id.txt_printerName)
            viewHolder.mIpAddress = convertView.findViewById(R.id.txt_ipAddress)
            viewHolder.mPrinterName?.text = printer.name
            viewHolder.mIpAddress?.text = printer.ipAddress
            viewHolder.mDiscloseImage = convertView.findViewById(R.id.img_disclosure)
            viewHolder.mDeleteButton = convertView.findViewById(R.id.btn_delete)
            viewHolder.mDiscloseImage?.tag = printer
            viewHolder.mPrinterName?.tag = printer
            viewHolder.mDeleteButton?.tag = convertView
            convertView.setOnClickListener(this)
            viewHolder.mDeleteButton?.setOnClickListener(this)
            convertView.tag = viewHolder

            // AppUtils.changeChildrenFont((ViewGroup) convertView, SmartDeviceApp.getAppFont());
        } else {
            viewHolder = convertView.tag as ViewHolder
            viewHolder.mPrinterName!!.text = printer.name
            viewHolder.mIpAddress!!.text = printer.ipAddress
            viewHolder.mDiscloseImage!!.tag = printer
            viewHolder.mPrinterName!!.tag = printer
            viewHolder.mDeleteButton!!.tag = convertView
        }
        if (mPrinterManager!!.defaultPrinter == printer.id) {
            setPrinterRowToDefault(viewHolder)
        } else if (!(convertView as PrintersContainerView?)!!.delete) {
            setPrinterRowToNormal(viewHolder)
        }
        if (printerName == null || printerName.isEmpty()) {
            viewHolder.mPrinterName!!.text = context.resources.getString(R.string.ids_lbl_no_name)
        }
        separator = convertView!!.findViewById(R.id.printers_separator)
        if (position == count - 1) {
            separator.visibility = View.GONE
        } else {
            separator.visibility = View.VISIBLE
        }
        return convertView
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    /**
     * @brief Sets the PrinterArrayAdapterInterface function.
     *
     * @param callback Callback function
     */
    fun setPrintersArrayAdapterInterface(callback: PrinterArrayAdapterInterface?) {
        mCallbackRef = WeakReference(callback)
    }

    /**
     * @brief This function is called to reset the delete view.
     */
    fun resetDeletePrinterView() {
        if (mDeleteViewHolder != null) {
            val printerItem = mDeleteViewHolder!!.mDeleteButton!!.tag as PrintersContainerView
            printerItem.delete = false
            mDeleteViewHolder = null
        }
    }
    // ================================================================================
    // Public Methods
    // ================================================================================
    /**
     * @brief Set the view to delete.
     *
     * @param convertView View to set as delete view
     */
    fun setPrinterRowToDelete(convertView: View?) {
        if (convertView == null) {
            return
        }
        val printerItem = convertView as PrintersContainerView
        val viewHolder = convertView.getTag() as ViewHolder
        printerItem.delete = true
        mDeleteViewHolder = viewHolder
    }

    /**
     * @brief Reset the view.
     *
     * @param convertView View to reset
     */
    fun setPrinterRow(convertView: View) {
        val viewHolder = convertView.tag as ViewHolder
        val printer = viewHolder.mPrinterName!!.tag as Printer
        if (printer.id == mPrinterManager!!.defaultPrinter) {
            setPrinterRowToDefault(viewHolder)
        } else {
            setPrinterRowToNormal(viewHolder)
        }
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Set the view holder to normal.
     *
     * @param viewHolder View holder to set as normal view
     */
    private fun setPrinterRowToNormal(viewHolder: ViewHolder?) {
        if (viewHolder == null) {
            return
        }
        val printerItem = viewHolder.mDeleteButton!!.tag as PrintersContainerView
        if (printerItem.default) {
            printerItem.default = false
        }
        if (printerItem.delete) {
            printerItem.delete = false
            mDeleteViewHolder = null
        }
    }

    /**
     * @brief Set the view holder to default.
     *
     * @param viewHolder View holder to set as default view
     */
    private fun setPrinterRowToDefault(viewHolder: ViewHolder?) {
        if (viewHolder == null) {
            return
        }
        val printerItem = viewHolder.mDeleteButton!!.tag as PrintersContainerView
        printerItem.default = true
    }

    // ================================================================================
    // INTERFACE - View.OnClick
    // ================================================================================
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.printerListRow) {
            val printer = v.findViewById<View>(R.id.img_disclosure).tag as Printer
            if (mCallbackRef != null && mCallbackRef!!.get() != null) {
                mCallbackRef!!.get()!!.onPrinterListClicked(printer)
            }
        } else if (id == R.id.btn_delete) {
            if (mCallbackRef != null && mCallbackRef!!.get() != null) {
                val printerContainer = v.tag as PrintersContainerView
                mDeleteViewHolder = printerContainer.tag as ViewHolder
                mCallbackRef!!.get()!!
                    .onPrinterDeleteClicked(mDeleteViewHolder!!.mDiscloseImage!!.tag as Printer)
            }
        }
    }
    // ================================================================================
    // INTERFACE - PrinterArrayAdapterInterface
    // ================================================================================
    /**
     * @brief PrinterArrayAdapter Interface.
     */
    interface PrinterArrayAdapterInterface {
        /**
         * @brief Dialog which is displayed to confirm printer delete.
         *
         * @param printer Printer to be deleted
         */
        fun onPrinterDeleteClicked(printer: Printer?)

        /**
         * @brief Display the PrinterInfoFragment of the corresponding printer item clicked.
         *
         * @param printer PrinterInfoFragment to be displayed
         */
        fun onPrinterListClicked(printer: Printer?)
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class ViewHolder
     *
     * @brief Printers Screen view holder for phone.
     */
    class ViewHolder {
        var mPrinterName: TextView? = null
        var mIpAddress: TextView? = null
        var mDeleteButton: Button? = null
        var mDiscloseImage: ImageView? = null
    }

    /**
     * @brief Constructor.
     *
     * @param context Application context
     * @param resource Resource ID to be used as a printer row
     * @param values Printers list
     */
    init {
        mPrinterManager = getInstance(SmartDeviceApp.appContext!!)
        mDeleteViewHolder = null
    }
}