/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.printer

//import jp.co.riso.smartdeviceapp.common.SNMPManager.setCallback
//import jp.co.riso.smartdeviceapp.common.SNMPManager.initializeSNMPManager
//import jp.co.riso.smartdeviceapp.common.SNMPManager.deviceDiscovery
//import jp.co.riso.smartdeviceapp.common.SNMPManager.manualDiscovery
//import jp.co.riso.smartdeviceapp.common.SNMPManager.cancel
//import jp.co.riso.smartdeviceapp.common.SNMPManager.finalizeSNMPManager
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.common.SNMPManager.SNMPManagerCallback
import jp.co.riso.smartdeviceapp.common.SNMPManager
import kotlin.jvm.Synchronized
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting
import android.content.ContentValues
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.common.BaseTask
import android.content.Context
import android.database.Cursor
import android.view.View
import android.widget.ImageView
import androidx.preference.PreferenceManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartprint.R
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.util.*

/**
 * @class PrinterManager
 *
 * @brief Manager responsible for Printer management
 */
class PrinterManager(context: Context?, databaseManager: DatabaseManager?) : SNMPManagerCallback {
    private val mPrinterList: MutableList<Printer?>?

    /**
     * @brief Checks if there is an ongoing printer search.
     *
     * @retval true Printer search is ongoing
     * @retval false No ongoing Printer search
     */
    var isSearching = false
        private set

    /**
     * @brief Checks if the printer search was cancelled.
     *
     * @retval true Printer search is cancelled
     * @retval false Printer search is not cancelled
     */
    var isCancelled = false
        private set
    private val mSNMPManager: SNMPManager
    private var mPrinterSearchCallback: WeakReference<PrinterSearchCallback?>? = null
    private var mPrintersCallback: WeakReference<PrintersCallback?>? = null
    private var mUpdateStatusCallback: WeakReference<UpdateStatusCallback?>? = null
    private var mUpdateStatusTimer: Timer? = null
    private var mDefaultPrintId = EMPTY_ID
    private val mDatabaseManager: DatabaseManager

    /**
     * @brief PrinterManager Constructor.
     *
     * @param context Application context
     */
    private constructor(context: Context) : this(context, DatabaseManager(context)) {}
    // ================================================================================
    // Public Methods
    // ================================================================================
    /**
     * @brief Save Printer to the Database.
     *
     * @param printer Printer object containing the Printer Information such as Printer capabilities
     * @param isOnline Printer online status
     *
     * @retval true Save to Database is successful
     * @retval false Save to Database has failed
     */
    @Synchronized
    fun savePrinterToDB(printer: Printer?, isOnline: Boolean): Boolean {
        if (printer == null || isExists(printer)) {
            return false
        }
        if (!savePrinterInfo(printer)) {
            return false
        }
        if (!setPrinterId(printer)) {
            return false
        }
        if (defaultPrinter == EMPTY_ID) {
            setDefaultPrinter(printer)
        }
        if (mPrintersCallback != null && mPrintersCallback!!.get() != null) {
            mPrintersCallback!!.get()!!.onAddedNewPrinter(printer, isOnline)
        }
        mPrinterList!!.add(printer)
        return true
    }

    /**
     * @brief Check Printer Existence. <br></br>
     *
     * Determines if the Printer exists in the Saved Printer List
     *
     * @param printer Printer object that must contain the IP address and Printer Name
     *
     * @retval true Printer exists in the Saved Printers List
     * @retval false Printer does not exists in the Saved Printers List
     */
    fun isExists(printer: Printer?): Boolean {
        if (printer == null) {
            return false
        }
        if (mPrinterList != null) {
            for (i in mPrinterList.indices) {
                val printerItem = mPrinterList[i]
                if (printerItem!!.ipAddress == printer.ipAddress) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * @brief Check Printer Existence. <br></br>
     *
     * Determines if the Printer exists in the Saved Printer List by
     * matching the IP Address to the Printers in the Saved Printers
     * List
     *
     * @param ipAddress IP address of the Printer
     *
     * @retval true Printer exists in the Saved Printers List
     * @retval false Printer does not exists in the Saved Printers List
     */
    fun isExists(ipAddress: String?): Boolean {
        if (ipAddress == null) {
            return false
        }
        if (mPrinterList != null) {
            for (i in mPrinterList.indices) {
                val printerItem = mPrinterList[i]
                if (printerItem!!.ipAddress == ipAddress) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * @brief Check Printer Existence. <br></br>
     *
     * Determines if the Printer exists in the Saved Printer List by
     * matching the printer ID to the Printers in the Saved Printers
     * List
     *
     * @param printerId Printer ID.
     *
     * @retval true Printer exists in the Saved Printers List
     * @retval false Printer does not exists in the Saved Printers List
     */
    fun isExists(printerId: Int): Boolean {
        if (printerId == EMPTY_ID) {
            return false
        }
        if (mPrinterList != null) {
            for (i in mPrinterList.indices) {
                val printerItem = mPrinterList[i]
                if (printerItem!!.id == printerId) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * @brief Retrieves the Printer objects from the Database.
     *
     * @return List of Saved Printer objects
     */
    @get:Synchronized
    val savedPrintersList: List<Printer?>?
        get() {
            if (mPrinterList!!.size != 0) {
                return mPrinterList
            }
            val cursor = mDatabaseManager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
            )
            mPrinterList.clear()
            if (cursor == null) {
                return mPrinterList
            }
            if (cursor.count < 1) {
                mDatabaseManager.close()
                cursor.close()
                return mPrinterList
            }
            if (cursor.moveToFirst()) {
                do {
                    val printer = Printer(
                        DatabaseManager.getStringFromCursor(
                            cursor,
                            KeyConstants.KEY_SQL_PRINTER_NAME
                        ),
                        DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_IP)
                    )
                    printer.id =
                        DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_ID)
                    try {
                        printer.portSetting = PortSetting.values()[DatabaseManager.getIntFromCursor(
                            cursor,
                            KeyConstants.KEY_SQL_PRINTER_PORT
                        )]
                    } catch (e: IndexOutOfBoundsException) {
                        printer.portSetting = PortSetting.LPR
                    }
                    val lprAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_LPR
                    )
                    val rawAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_RAW
                    )
                    val bookletFinishingAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_BOOKLET_FINISHING
                    )
                    val staplerAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_STAPLER
                    )
                    val punch3Available = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_PUNCH3
                    )
                    val punch4Available = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_PUNCH4
                    )
                    val trayFaceDownAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_TRAYFACEDOWN
                    )
                    val trayTopAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_TRAYTOP
                    )
                    val trayStackAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_TRAYSTACK
                    )
                    val externalFeederAvailable = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_EXTERNALFEEDER
                    )
                    val punch0Available = DatabaseManager.getBooleanFromCursor(
                        cursor,
                        KeyConstants.KEY_SQL_PRINTER_PUNCH0
                    )
                    printer.config!!.isLprAvailable = lprAvailable
                    printer.config!!.isRawAvailable = rawAvailable
                    printer.config!!.isBookletFinishingAvailable = bookletFinishingAvailable
                    printer.config!!.isStaplerAvailable = staplerAvailable
                    printer.config!!.isPunch3Available = punch3Available
                    printer.config!!.isPunch4Available = punch4Available
                    printer.config!!.isTrayFaceDownAvailable = trayFaceDownAvailable
                    printer.config!!.isTrayTopAvailable = trayTopAvailable
                    printer.config!!.isTrayStackAvailable = trayStackAvailable
                    printer.config!!.isExternalFeederAvailable = externalFeederAvailable
                    printer.config!!.isPunch0Available = punch0Available
                    mPrinterList.add(printer)
                } while (cursor.moveToNext())
            }
            mDatabaseManager.close()
            cursor.close()
            return mPrinterList
        }

    /**
     * @brief Sets the Default Printer by clearing and inserting an entry in DefaultPrinter table.
     *
     * @param printer The Printer object selected
     *
     * @retval true Save to Database is Successful
     * @retval false Save to Database is Failed
     */
    fun setDefaultPrinter(printer: Printer?): Boolean {
        if (printer == null) {
            return false
        }
        clearDefaultPrinter()
        val newDefaultPrinter = ContentValues()
        newDefaultPrinter.put(KeyConstants.KEY_SQL_PRINTER_ID, printer.id)
        if (!mDatabaseManager.insert(
                KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE,
                null,
                newDefaultPrinter
            )
        ) {
            mDatabaseManager.close()
            return false
        }
        mDefaultPrintId = printer.id
        mDatabaseManager.close()
        return true
    }

    /**
     * @brief Clears the Default Printer table in the database.
     */
    fun clearDefaultPrinter() {
        mDatabaseManager.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null)
        mDefaultPrintId = EMPTY_ID
        mDatabaseManager.close()
    }

    /**
     * @brief Removes the Printer from the database.
     *
     * @param printer The Printer object selected for deletion
     *
     * @retval true Save to Database is successful
     * @retval false Save to Database has failed
     */
    fun removePrinter(printer: Printer?): Boolean {
        var printer = printer
        val ret: Boolean
        if (printer == null) {
            return false
        }
        for (i in mPrinterList!!.indices) {
            val printerItem = mPrinterList[i]
            if (printerItem!!.id == printer!!.id) {
                printer = printerItem
                break
            }
        }
        if (!isExists(printer)) {
            return false
        }
        ret = mDatabaseManager.delete(
            KeyConstants.KEY_SQL_PRINTER_TABLE,
            KeyConstants.KEY_SQL_PRINTER_ID + "=?",
            printer!!.id.toString()
        )
        if (ret) {
            mPrinterList.remove(printer)
            // Set default printer to invalid
            if (printer.id == mDefaultPrintId) {
                if (mPrinterList.size != 0) {
                    val newDefaultPrinter = mPrinterList[0]
                    setDefaultPrinter(newDefaultPrinter)
                } else {
                    mDefaultPrintId = EMPTY_ID
                }
            }
        }
        mDatabaseManager.close()
        return ret
    }

    /**
     * @brief Obtains the printer ID of the default printer.
     *
     * @return Default Printer ID
     * @retval EMPTY_ID No default printer
     */
    val defaultPrinter: Int
        get() {
            if (mDefaultPrintId != EMPTY_ID) {
                return mDefaultPrintId
            }
            val cursor = mDatabaseManager.query(
                KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE,
                null,
                KeyConstants.KEY_SQL_PRINTER_ID,
                null,
                null,
                null,
                null
            )
                ?: return EMPTY_ID
            if (cursor.count != 1) {
                mDatabaseManager.close()
                cursor.close()
                return EMPTY_ID
            }
            if (cursor.moveToFirst()) {
                mDefaultPrintId =
                    DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_ID)
            }
            cursor.close()
            mDatabaseManager.close()
            return mDefaultPrintId
        }

    /**
     * @brief Obtains the information about the printer type of a printer with corresponding ID
     *
     * @param printerId The id of the printer that must be determined for printer type
     * @return String representing printer type
     */
    fun getPrinterType(printerId: Int): String? {
        val printerList = savedPrintersList
        for (printer in printerList!!) {
            if (printer!!.id == printerId) {
                return printer.printerType
            }
        }
        return null
    }

    /**
     * @brief Update the value of port settings.
     *
     * @param printerId Printer ID
     * @param portSettings Port Settings
     *
     * @retval true Save to Database is Successful
     * @retval false Save to Database is Failed
     */
    fun updatePortSettings(printerId: Int, portSettings: PortSetting?): Boolean {
        if (portSettings == null) {
            return false
        }
        val ret: Boolean
        val cv = ContentValues()
        cv.put(KeyConstants.KEY_SQL_PRINTER_PORT, portSettings.ordinal)
        ret = mDatabaseManager.update(
            KeyConstants.KEY_SQL_PRINTER_TABLE,
            cv,
            KeyConstants.KEY_SQL_PRINTER_ID + "=?",
            printerId.toString()
        )
        mDatabaseManager.close()
        return ret
    }

    /**
     * @brief Search for the Printer Devices using Device Discovery/Auto Search. <br></br>
     *
     * The search can be cancelled by calling cancelPrinterSearch()
     */
    fun startPrinterSearch() {
        isSearching = true
        isCancelled = false
        mSNMPManager.initializeSNMPManager(snmpCommunityNameFromSharedPrefs)
        mSNMPManager.deviceDiscovery()
    }

    /**
     * @brief Search for the Printer Device using Manual Search. <br></br>
     *
     * The search can be cancelled by calling cancelPrinterSearch()
     *
     * @param ipAddress The IP Address of the Printer
     */
    fun searchPrinter(ipAddress: String?) {
        if (ipAddress == null) {
            return
        }
        isSearching = true
        isCancelled = false
        mSNMPManager.initializeSNMPManager(snmpCommunityNameFromSharedPrefs)
        mSNMPManager.manualDiscovery(ipAddress)
    }

    /**
     * @brief Cancel Printer Search. <br></br>
     *
     * Stops Device Discovery or Manual Search operation.
     */
    fun cancelPrinterSearch() {
        isSearching = false
        isCancelled = true
        Timer().schedule(object : TimerTask() {
            override fun run() {
                mSNMPManager.cancel()
            }
        }, 0)
    }

    /**
     * @brief Updates the Online Status of the specified printer view.
     *
     * @param ipAddress IP Address of the printer
     * @param view View of the online indicator
     */
    fun updateOnlineStatus(ipAddress: String?, view: View?) {
        if (ipAddress == null) {
            return
        }
        UpdateOnlineStatusTask(view, ipAddress).execute()
    }

    /**
     * @brief Set Printer Search Screen Callback. <br></br>
     *
     * Set the Callback for Adding Printer to the Searched Printer List during Device Discovery/Manual search.
     *
     * @param printerSearchCallback Printer search callback function
     */
    fun setPrinterSearchCallback(printerSearchCallback: PrinterSearchCallback?) {
        mPrinterSearchCallback = WeakReference(printerSearchCallback)
    }

    /**
     * @brief Set Printer Screen Callback. <br></br>
     *
     * Sets the Saved Printer List/View Callback for adding printers.
     *
     * @param printersCallback Printers screen callback function
     */
    fun setPrintersCallback(printersCallback: PrintersCallback?) {
        mPrintersCallback = WeakReference(printersCallback)
    }

    /**
     * @brief Set Update Status Callback. <br></br>
     *
     * Set the Callback for Updating Printer Status. Changes status from online to off-line or vice-versa.
     *
     * @param updateStatusCallback Update online status callback function
     */
    fun setUpdateStatusCallback(updateStatusCallback: UpdateStatusCallback?) {
        mUpdateStatusCallback = WeakReference(updateStatusCallback)
    }

    /**
     * @brief Get Printer Count.
     *
     * @return Printer count
     */
    val printerCount: Int
        get() = mPrinterList!!.size

    /**
     * @brief Check the Device status. <br></br>
     *
     * Checks the Device if it is online. This function should not be called from the main thread.
     *
     * @param ipAddress IP Address
     *
     * @retval true Device is online
     * @retval false Device is off-line
     */
    fun isOnline(ipAddress: String?): Boolean {
        var inetIpAddress: InetAddress
        return try {
            if (ipAddress == null) {
                return false
            }
            if (NetUtils.isIPv6Address(ipAddress)) {
                NetUtils.connectToIpv6Address(ipAddress, null)
            } else {
                // RM#901 use socket connect to check printer online status
                NetUtils.connectToIpv4Address(ipAddress)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * @brief Create Update Status Thread. <br></br>
     *
     * Creates the thread that updates the online/off-line status.
     * The thread must be terminated by calling cancelUpdateStatusThread()
     * when the status view is no longer visible.
     */
    fun createUpdateStatusThread() {
        if (mUpdateStatusTimer != null) {
            return
        }
        mUpdateStatusTimer = Timer()
        mUpdateStatusTimer!!.schedule(object : TimerTask() {
            override fun run() {
                if (mUpdateStatusCallback != null && mUpdateStatusCallback!!.get() != null) {
                    mUpdateStatusCallback!!.get()!!.updateOnlineStatus()
                }
            }
        }, 0, AppConstants.CONST_UPDATE_INTERVAL.toLong())
    }

    /**
     * @brief Cancel Update Status Thread. <br></br>
     *
     * Stops the thread that updates the online/off-line status.
     */
    fun cancelUpdateStatusThread() {
        if (mUpdateStatusTimer == null) {
            return
        }
        mUpdateStatusTimer!!.cancel()
        mUpdateStatusTimer = null
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Save Printer Information. <br></br>
     *
     * Saves the printer information to the database
     *
     * @param printer Printer object
     *
     * @retval true Successful
     * @retval false Failed
     */
    private fun savePrinterInfo(printer: Printer): Boolean {
        // Create Content
        val newPrinter = ContentValues()
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_IP, printer.ipAddress)
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_NAME, printer.name)
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PORT, printer.portSetting!!.ordinal)
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_LPR,
            if (printer.config!!.isLprAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_RAW,
            if (printer.config!!.isRawAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_BOOKLET_FINISHING,
            if (printer.config!!.isBookletFinishingAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_STAPLER,
            if (printer.config!!.isStaplerAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_PUNCH3,
            if (printer.config!!.isPunch3Available) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_PUNCH4,
            if (printer.config!!.isPunch4Available) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_TRAYFACEDOWN,
            if (printer.config!!.isTrayFaceDownAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_TRAYTOP,
            if (printer.config!!.isTrayTopAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_TRAYSTACK,
            if (printer.config!!.isTrayStackAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_EXTERNALFEEDER,
            if (printer.config!!.isExternalFeederAvailable) 1 else 0
        )
        newPrinter.put(
            KeyConstants.KEY_SQL_PRINTER_PUNCH0,
            if (printer.config!!.isPunch0Available) 1 else 0
        )
        if (!mDatabaseManager.insert(KeyConstants.KEY_SQL_PRINTER_TABLE, null, newPrinter)) {
            mDatabaseManager.close()
            return false
        }
        mDatabaseManager.close()
        return true
    }

    /**
     * @brief Set the Printer ID of the Printer object.
     *
     * @param printer Printer object
     *
     * @retval true Successful
     * @retval false Failed
     */
    private fun setPrinterId(printer: Printer): Boolean {
        val cursor = mDatabaseManager.query(
            KeyConstants.KEY_SQL_PRINTER_TABLE,
            null,
            KeyConstants.KEY_SQL_PRINTER_NAME + "=? and "
                    + KeyConstants.KEY_SQL_PRINTER_IP + "=?",
            arrayOf(printer.name, printer.ipAddress),
            null,
            null,
            null
        )
        val ret: Boolean
        ret = getIdFromCursor(cursor, printer)
        if (ret) {
            mDatabaseManager.close()
        }
        return ret
    }

    val snmpCommunityNameFromSharedPrefs: String?
        get() {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
            return sharedPreferences.getString(
                AppConstants.PREF_KEY_SNMP_COMMUNITY_NAME,
                AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME
            )
        }

    /**
     * @brief Get Printer ID from Cursor object.
     *
     * @param cursor Cursor object
     * @param printer Printer object
     *
     * @retval true Successful
     * @retval false Failed
     */
    fun getIdFromCursor(cursor: Cursor?, printer: Printer?): Boolean {
        if (cursor != null && cursor.moveToFirst() && printer != null) {
            printer.id =
                cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID))
            cursor.close()
            return true
        }
        return false
    }

    // ================================================================================
    // Interface - SNMPManagerCallback
    // ================================================================================
    override fun onEndDiscovery(manager: SNMPManager?, result: Int) {
        if (manager == null) {
            return
        }
        isSearching = false
        manager.finalizeSNMPManager()
        if (mPrinterSearchCallback != null && mPrinterSearchCallback!!.get() != null) {
            mPrinterSearchCallback!!.get()!!.onSearchEnd()
        }
    }

    override fun onFoundDevice(
        manager: SNMPManager?,
        ipAddress: String?,
        name: String?,
        capabilities: BooleanArray?
    ) {
        if (manager == null || ipAddress == null || name == null || capabilities == null) {
            return
        }
        val printer = Printer(name, ipAddress)
        if (printer.isActualPrinterTypeInvalid) {
            return
        }
        setupPrinterConfig(printer, capabilities)
        if (isSearching) {
            if (mPrinterSearchCallback != null && mPrinterSearchCallback!!.get() != null) {
                mPrinterSearchCallback!!.get()!!.onPrinterAdd(printer)
            }
        }
    }
    // ================================================================================
    // Interface - PrinterSearchCallback
    // ================================================================================
    /**
     * @interface PrinterSearchCallback
     *
     * @brief Printers Search Screen Interface. <br></br>
     *
     * Interface for Printers Search Screen. Used for updating view in the Printers Search Screen.
     */
    interface PrinterSearchCallback {
        /**
         * @brief On Printer Add callback. <br></br>
         *
         * Callback called when a printer is to be added as a result of Printers Search (Manual/Auto)
         *
         * @param printer Printer object
         */
        fun onPrinterAdd(printer: Printer?)

        /**
         * @brief On Search End callback. <br></br>
         *
         * Callback called at the end of Printer Search
         */
        fun onSearchEnd()
    }
    // ================================================================================
    // Interface - PrintersCallback
    // ================================================================================
    /**
     * @interface PrintersCallback
     *
     * @brief Printers Screen Interface. <br></br>
     *
     * Interface for Printers Screen. Used for updating view in the Printers Screen.
     */
    interface PrintersCallback {
        /**
         * @brief Adds printer to the Printers Screen
         *
         * @param printer Printer object
         * @param isOnline Printer online status
         */
        fun onAddedNewPrinter(printer: Printer?, isOnline: Boolean)
    }
    // ================================================================================
    // Interface - UpdateStatusCallback
    // ================================================================================
    /**
     * @interface UpdateOnlineStatusTask
     *
     * @brief Update Online Status Interface. <br></br>
     *
     * Interface for updating the online status.
     */
    interface UpdateStatusCallback {
        /**
         * @brief Callback to update the online status
         */
        fun updateOnlineStatus()
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class UpdateOnlineStatusTask
     *
     * @brief Update Online Status Task. <br></br>
     *
     * AsyncTask that updates changes the online status image.
     */
    internal inner class UpdateOnlineStatusTask(view: View?, ipAddress: String) :
        BaseTask<Any?, Boolean?>() {
        private val mViewRef: WeakReference<View?>?
        private val mIpAddress: String
        protected override fun doInBackground(vararg arg: Any?): Boolean? {
            return if (mIpAddress.isEmpty()) {
                false
            } else isOnline(mIpAddress)
        }

        protected override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            val activity = SmartDeviceApp.getActivity()
            activity.runOnUiThread {
                if (mViewRef != null && mViewRef.get() != null) {
                    val view = mViewRef.get() as ImageView?
                    if (result == true) {
                        view!!.setImageResource(R.drawable.img_btn_printer_status_online)
                    } else {
                        view!!.setImageResource(R.drawable.img_btn_printer_status_offline)
                    }
                }
            }
        }

        /**
         * @brief Instantiate UpdateOnlineStatusTask.
         */
        init {
            mViewRef = WeakReference(view)
            mIpAddress = ipAddress
        }
    }

    companion object {
        /// Printer ID for invalid printer
        const val EMPTY_ID = -1
        private var sSharedMngr: PrinterManager? = null

        /**
         * @brief Get the PrinterManager instance.
         *
         * @param context Application context
         *
         * @return Shared PrinterManager instance
         */
        @JvmStatic
        fun getInstance(context: Context): PrinterManager? {
            if (sSharedMngr == null) {
                sSharedMngr = PrinterManager(context)
            }
            return sSharedMngr
        }

        /**
         * @brief Setup printer configuration. <br></br>
         *
         * Saves the printer configuration/capabilities
         *
         * @param printer Printer object
         * @param capabilities Printer capabilities
         */
        @JvmStatic
        fun setupPrinterConfig(printer: Printer?, capabilities: BooleanArray?) {
            if (printer == null || capabilities == null) {
                return
            }
            for (i in capabilities.indices) {
                when (i) {
                    SNMPManager.SNMP_CAPABILITY_BOOKLET_FINISHING -> printer.config!!.isBookletFinishingAvailable =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_STAPLER -> printer.config!!.isStaplerAvailable =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_FINISH_2_3 -> printer.config!!.isPunch3Available =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_FINISH_2_4 -> printer.config!!.isPunch4Available =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_TRAY_FACE_DOWN -> printer.config!!.isTrayFaceDownAvailable =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_TRAY_TOP -> printer.config!!.isTrayTopAvailable =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_TRAY_STACK -> printer.config!!.isTrayStackAvailable =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_LPR -> printer.config!!.isLprAvailable =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_RAW -> printer.config!!.isRawAvailable =
                        capabilities[i]
                    SNMPManager.SNMP_CAPABILITY_EXTERNAL_FEEDER -> if (printer.isPrinterFTorCEREZONA_S || printer.isPrinterGL) {
                        printer.config!!.isExternalFeederAvailable = capabilities[i]
                    } else {
                        printer.config!!.isExternalFeederAvailable = false
                    }
                    SNMPManager.SNMP_CAPABILITY_FINISH_0 -> if (printer.isPrinterFTorCEREZONA_S || printer.isPrinterGL) {
                        printer.config!!.isPunch0Available = capabilities[i]
                    } else {
                        printer.config!!.isPunch0Available = false
                    }
                }
            }
        }
    }

    /**
     * @brief PrinterManager Constructor.
     *
     * @param context Application context
     * @param databaseManager DatabaseManager instance
     */
    init {
        var databaseManager = databaseManager
        if (databaseManager == null) {
            databaseManager = DatabaseManager(context)
        }
        mDatabaseManager = databaseManager
        mPrinterList = ArrayList()
        mSNMPManager = SNMPManager()
        mSNMPManager.setCallback(this)
    }
}