/*
 * Copyright (c) 2024 RISO, Inc. All rights reserved.
 *
 * ContentPrintFileAdapter.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.contentprint

import android.content.Context
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartprint.R


class ContentPrintFileAdapter(
    context: Context?,
    private val _layoutId: Int,
    values: List<ContentPrintFile>?
) : ArrayAdapter<ContentPrintFile>(context!!, _layoutId, values!!),
    View.OnClickListener {
    private var _adapterInterface: ContentPrintFileAdapterInterface? = null
    private var _lastClickTime: Long = 0

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val contentPrintFile = getItem(position)
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            convertView = inflater!!.inflate(_layoutId, parent, false)
            viewHolder = ViewHolder()
        } else {
            viewHolder = (convertView.tag as ViewHolder?)!!
        }
        initializeView(viewHolder, convertView, contentPrintFile, position)
        return convertView!!
    }

    /**
     * @brief Initialize the view of the printer.
     *
     * @param viewHolder View Holder of the printer object
     * @param convertView Row view
     * @param contentPrintFile ContentPrintFile object
     * @param position Position or index of the printer object
     */
    fun initializeView(
        viewHolder: ViewHolder?,
        convertView: View?,
        contentPrintFile: ContentPrintFile?,
        position: Int
    ) {
        if (viewHolder == null || convertView == null || contentPrintFile == null) {
            return
        }

        viewHolder.contentPrintFilename = convertView.findViewById(R.id.filenameText)
        viewHolder.thumbnailImage = convertView.findViewById(R.id.thumbnailImage)
        viewHolder.newFileText = convertView.findViewById(R.id.newFile)
        viewHolder.position = position

        viewHolder.contentPrintFilename?.text = contentPrintFile.filename
        if (contentPrintFile.thumbnailImagePath != null) {
            val bitmap = BitmapFactory.decodeFile(contentPrintFile.thumbnailImagePath)
            viewHolder.thumbnailImage?.setImageBitmap(bitmap)
        }

        if (contentPrintFile.isRecentlyUploaded == true){
            viewHolder.newFileText?.visibility = View.INVISIBLE
        }

        val separator: View = convertView.findViewById(R.id.contentprint_separator)
        if (position == count - 1) {
            separator.visibility = View.GONE
        } else {
            separator.visibility = View.VISIBLE
        }

        convertView.setOnClickListener(this)
        convertView.tag = viewHolder
    }

    /**
     * @brief Set Content Print Screen Adapter Interface.
     *
     * @param adapterInterface Printer search adapter interface
     */
    fun setAdapterInterface(adapterInterface: ContentPrintFileAdapterInterface?) {
        _adapterInterface = adapterInterface
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
        internal var thumbnailImage: ImageView? = null
        internal var contentPrintFilename: TextView? = null
        internal var newFileText: TextView? = null
        internal var position: Int = -1
    }

    // ================================================================================
    // Interface
    // ================================================================================
    /**
     * @interface ContentPrintFileAdapterInterface
     *
     * @brief Content Print Screen interface.
     */
    interface ContentPrintFileAdapterInterface {
        /**
         * @brief On download a content print file callback. <br></br>
         *
         * Callback called to download a content print file in the Content Print Screen.
         *
         * @param contentPrintFile The selected content print file
         *
         * @retval 0 Success
         * @retval -1 Error
         */
        fun onFileSelect(contentPrintFile: ContentPrintFile?): Int
    }

    // ================================================================================
    // Interface View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        if (v.id == R.id.content_print_row &&
            SystemClock.elapsedRealtime() - _lastClickTime > AppConstants.DOUBLE_TAP_TIME_ELAPSED) {
            // prevent double tap
            _lastClickTime = SystemClock.elapsedRealtime()

            val viewHolder = v.tag as ViewHolder?
            val contentPrintFile = getItem(viewHolder!!.position)

            if (_adapterInterface!!.onFileSelect(contentPrintFile) != -1) {
                v.isActivated = true
               // ContentPrintManager.newUploadedFiles.remove(contentPrintFile?.filename)
                //Save viewd file into the local DB
                contentPrintFile?.filename?.let {
                    ContentPrintManager.saveViewedFile(context,contentPrintFile.fileId.toString(),
                        it
                    )
//                    if (saved) {
//                        // Successfully saved in the database
//                        Log.d("SaveFile", "File $it saved in the database")
//                        // Handle success
//                    } else {
//                        // Failed to save in the database
//                        Log.e("SaveFile", "Failed to save file $it in the database")
//                        // Handle failure
//                    }

                }

                val dbHelper = DatabaseManager(context)
//                val columnTypes = dbHelper.getColumnTypes("ContentPrint")
//                if (columnTypes != null) {
//                    for ((columnName, columnType) in columnTypes) {
//                        Log.e("Check column", "Column: $columnName ColumnType $columnType")
//                    }
//                }

                // log all viewed files

//                    val viewedFiles = ContentPrintManager.getAllViewedFiles()
//                    viewedFiles?.forEach { file ->
//                        Log.e("ContentPrintFileAdapter", "FileId: ${file.fileId}, FileName: ${file.fileName}")
//                    }


            }
        }
    }
}