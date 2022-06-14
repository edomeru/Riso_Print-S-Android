/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * DefaultPrinterArrayAdapter.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printers

import android.widget.ArrayAdapter
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import jp.co.riso.smartprint.R

/**
 * @class DefaultPrinterArrayAdapter
 *
 * @brief Array Adapter for spinner for selecting a printer as default.
 */
class DefaultPrinterArrayAdapter(context: Context?, resource: Int) : ArrayAdapter<String?>(
    context!!, resource
) {
    private val _activity: Activity? = context as Activity?

    @JvmField
    var isNoDisabled: Boolean = false
    override fun areAllItemsEnabled(): Boolean {
        return !isNoDisabled
    }

    override fun isEnabled(position: Int): Boolean {
        return position != 1 || !isNoDisabled
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        if (isNoDisabled && position == 1) //No
        {
            view.setBackgroundColor(ContextCompat.getColor(_activity!!, R.color.theme_light_3))
            view.setTextColor(ContextCompat.getColor(_activity, R.color.theme_light_4))
        } else {
            view.setBackgroundResource(R.drawable.selector_printerinfo_port)
            view.setTextColor(
                ContextCompat.getColorStateList(
                    _activity!!,
                    R.color.selector_printers_text
                )
            )
        }
        return view
    }

}