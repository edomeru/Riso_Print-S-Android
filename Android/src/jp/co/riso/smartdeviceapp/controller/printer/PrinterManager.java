/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.printer;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.riso.android.util.NetUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.common.SNMPManager;
import jp.co.riso.smartdeviceapp.common.SNMPManager.SNMPManagerCallback;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting;
import jp.co.riso.smartprint.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

public class PrinterManager implements SNMPManagerCallback {
    public static final int EMPTY_ID = -1;
    private static PrinterManager sSharedMngr = null;
    private List<Printer> mPrinterList = null;
    private boolean mIsSearching = false;
    private boolean mIsCancelled = false;
    private SNMPManager mSNMPManager = null;
    private WeakReference<PrinterSearchCallback> mPrinterSearchCallback = null;
    private WeakReference<PrintersCallback> mPrintersCallback = null;
    private WeakReference<UpdateStatusCallback> mUpdateStatusCallback = null;
    private Timer mUpdateStatusTimer = null;
    private int mDefaultPrintId = EMPTY_ID;
    private DatabaseManager mDatabaseManager = null;

    /**
     * @brief PrinterManager Constructor.
     * 
     * @param context Application context
     */
    private PrinterManager(Context context) {
        this(context, new DatabaseManager(context));
    }
    
    /**
     * @brief PrinterManager Constructor.
     * 
     * @param context Application context
     * @param databaseManager DatabaseManager instance
     */
    protected PrinterManager(Context context, DatabaseManager databaseManager) {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager(context);
        }
        mDatabaseManager = databaseManager;
        mPrinterList = new ArrayList<Printer>();
        mSNMPManager = new SNMPManager();
        mSNMPManager.setCallback(this);
    }
    
    /**
     * @brief Get the PrinterManager instance.
     * 
     * @param context Application context
     * 
     * @return Shared PrinterManager instance
     */
    public static PrinterManager getInstance(Context context) {
        if (sSharedMngr == null) {
            sSharedMngr = new PrinterManager(context);
        }
        return sSharedMngr;
    }
    
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
    public boolean savePrinterToDB(Printer printer, boolean isOnline) {
        if (printer == null || isExists(printer)) {
            return false;
        }
        
        if (!savePrinterInfo(printer)) {
            return false;
        }
        
        if (!setPrinterId(printer)) {
            return false;
        }
        
        if (getDefaultPrinter() == EMPTY_ID) {
            setDefaultPrinter(printer);
        }
        
        if (mPrintersCallback != null && mPrintersCallback.get() != null) {
            mPrintersCallback.get().onAddedNewPrinter(printer, isOnline);
        }
        
        mPrinterList.add(printer);
        
        return true;
    }
    
    /**
     * @brief Check Printer Existence. <br>
     * 
     * Determines if the Printer exists in the Saved Printer List
     * 
     * @param printer Printer object that must contain the IP address and Printer Name
     * 
     * @retval true Printer exists in the Saved Printers List
     * @retval false Printer does not exists in the Saved Printers List
     */
    public boolean isExists(Printer printer) {
        if (printer == null) {
            return false;
        }
        
        if (mPrinterList != null) {
            for (int i = 0; i < mPrinterList.size(); i++) {
                Printer printerItem = mPrinterList.get(i);
                if (printerItem.getIpAddress().equals(printer.getIpAddress())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @brief Check Printer Existence. <br>
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
    public boolean isExists(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        
        if (mPrinterList != null) {
            for (int i = 0; i < mPrinterList.size(); i++) {
                Printer printerItem = mPrinterList.get(i);
                if (printerItem.getIpAddress().equals(ipAddress)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @brief Check Printer Existence. <br>
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
    public boolean isExists(int printerId) {
        if (printerId == EMPTY_ID) {
            return false;
        }
        
        if (mPrinterList != null) {
            for (int i = 0; i < mPrinterList.size(); i++) {
                Printer printerItem = mPrinterList.get(i);
                if (printerItem.getId() == printerId) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @brief Retrieves the Printer objects from the Database.
     * 
     * @return List of Saved Printer objects
     */
    public List<Printer> getSavedPrintersList() {
        if (mPrinterList.size() != 0) {
            return mPrinterList;
        }
        
        Cursor cursor = mDatabaseManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        
        mPrinterList.clear();
        if (cursor == null) {            
            return mPrinterList;
        }
        if (cursor.getCount() < 1) {
            mDatabaseManager.close();
            cursor.close();
            return mPrinterList;
        }
        if (cursor.moveToFirst()) {
            do {
                Printer printer = new Printer(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_NAME),
                        DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_IP));
                printer.setId(DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_ID));
                try {
                    printer.setPortSetting(PortSetting.values()[DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PORT)]);
                } catch (IndexOutOfBoundsException e) {
                    printer.setPortSetting(PortSetting.LPR);
                }
                boolean lprAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_LPR);
                boolean rawAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_RAW);
                boolean bookletFinishingAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_BOOKLET_FINISHING);
                boolean staplerAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_STAPLER);
                boolean punch3Available = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PUNCH3);
                boolean punch4Available = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PUNCH4);
                boolean trayFaceDownAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYFACEDOWN);
                boolean trayTopAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYTOP);
                boolean trayStackAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYSTACK);
                
                printer.getConfig().setLprAvailable(lprAvailable);
                printer.getConfig().setRawAvailable(rawAvailable);
                printer.getConfig().setBookletFinishingAvailable(bookletFinishingAvailable);
                printer.getConfig().setStaplerAvailable(staplerAvailable);
                printer.getConfig().setPunch3Available(punch3Available);
                printer.getConfig().setPunch4Available(punch4Available);
                printer.getConfig().setTrayFaceDownAvailable(trayFaceDownAvailable);
                printer.getConfig().setTrayTopAvailable(trayTopAvailable);
                printer.getConfig().setTrayStackAvailable(trayStackAvailable);
                mPrinterList.add(printer);
            } while (cursor.moveToNext());
            
        }
        mDatabaseManager.close();
        cursor.close();
        
        return mPrinterList;
    }
    
    /**
     * @brief Sets the Default Printer by clearing and inserting an entry in DefaultPrinter table.
     * 
     * @param printer The Printer object selected
     * 
     * @retval true Save to Database is Successful
     * @retval false Save to Database is Failed
     */
    public boolean setDefaultPrinter(Printer printer) {
        
        if (printer == null) {
            return false;
        }
        clearDefaultPrinter();
        
        ContentValues newDefaultPrinter = new ContentValues();
        
        newDefaultPrinter.put(KeyConstants.KEY_SQL_PRINTER_ID, printer.getId());
        
        if (!mDatabaseManager.insert(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, newDefaultPrinter)) {
            mDatabaseManager.close();
            return false;
        }
        
        mDefaultPrintId = printer.getId();
        mDatabaseManager.close();
        return true;
    }
    
    /**
     * @brief Clears the Default Printer table in the database.
     */
    public void clearDefaultPrinter() {
        mDatabaseManager.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null);
        mDefaultPrintId = EMPTY_ID;
        mDatabaseManager.close();
    }
    
    /**
     * @brief Removes the Printer from the database.
     * 
     * @param printer The Printer object selected for deletion
     * 
     * @retval true Save to Database is successful
     * @retval false Save to Database has failed
     */
    public boolean removePrinter(Printer printer) {
        boolean ret = false;
        if (printer == null) {
            return false;
        }
        
        for (int i = 0; i < mPrinterList.size(); i++) {
            Printer printerItem = mPrinterList.get(i);
            if (printerItem.getId() == printer.getId()) {
                printer = printerItem;
                break;
            }
        }
        
        if (!isExists(printer)) {
            return false;
        }
        
        ret = mDatabaseManager.delete(KeyConstants.KEY_SQL_PRINTER_TABLE, KeyConstants.KEY_SQL_PRINTER_ID + "=?", String.valueOf(printer.getId()));
        
        if (ret) {
            mPrinterList.remove(printer);
            // Set default printer to invalid
            if (printer.getId() == mDefaultPrintId) {
                if (mPrinterList.size() != 0) {
                    Printer newDefaultPrinter = mPrinterList.get(0);
                    setDefaultPrinter(newDefaultPrinter);
                } else {
                    mDefaultPrintId = EMPTY_ID;
                }
            }
        }
        mDatabaseManager.close();
        return ret;
    }

    /**
     * @brief Obtains the printer ID of the default printer.
     * 
     * @retval mDefaultPrintId default Printer ID
     * @retval EMPTY_ID No default printer
     */
    public int getDefaultPrinter() {
        if (mDefaultPrintId != EMPTY_ID) {
            return mDefaultPrintId;
        }
                
        Cursor cursor = mDatabaseManager.query(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID, null, null, null, null);
        
        if (cursor == null) {
            return EMPTY_ID;
        }
        if (cursor.getCount() != 1) {
            mDatabaseManager.close();
            cursor.close();
            return EMPTY_ID;
        }
        
        if (cursor.moveToFirst()) {
            mDefaultPrintId = DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_ID);
        }
        
        cursor.close();
        mDatabaseManager.close();
        return mDefaultPrintId;
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
    public boolean updatePortSettings(int printerId, PortSetting portSettings) {
        if (portSettings == null) {
            return false;
        }
        boolean ret = false;
        ContentValues cv = new ContentValues();
        cv.put(KeyConstants.KEY_SQL_PRINTER_PORT, portSettings.ordinal());
        ret = mDatabaseManager.update(KeyConstants.KEY_SQL_PRINTER_TABLE, cv, KeyConstants.KEY_SQL_PRINTER_ID + "=?", String.valueOf(printerId));
        mDatabaseManager.close();
        return ret;
    }
    
    /**
     * @brief Search for the Printer Devices using Device Discovery/Auto Search. <br>
     * 
     * The search can be cancelled by calling cancelPrinterSearch()
     */
    public void startPrinterSearch() {
        mIsSearching = true;
        mIsCancelled = false;
        mSNMPManager.initializeSNMPManager();
        mSNMPManager.deviceDiscovery();
    }
    
    /**
     * @brief Search for the Printer Device using Manual Search. <br>
     * 
     * The search can be cancelled by calling cancelPrinterSearch()
     *  
     * @param ipAddress The IP Address of the Printer
     */
    public void searchPrinter(String ipAddress) {
        
        if (ipAddress == null) {
            return;
        }
        
        mIsSearching = true;
        mIsCancelled = false;
        mSNMPManager.initializeSNMPManager();
        mSNMPManager.manualDiscovery(ipAddress);
    }
    
    /**
     * @brief Cancel Printer Search. <br>
     *
     * Stops Device Discovery or Manual Search operation.
     */
    public void cancelPrinterSearch() {
        mIsSearching = false;
        mIsCancelled = true;
        new Timer().schedule(new TimerTask() {
            
            @Override
            public void run() {
                mSNMPManager.cancel();
            }
        }, 0);
    }
    
    /**
     * @brief Checks if there is an ongoing printer search.
     * 
     * @retval true Printer search is ongoing
     * @retval false No ongoing Printer search
     */
    public boolean isSearching() {
        return mIsSearching;
    }
    
    /**
     * @brief Checks if the printer search was cancelled.
     * 
     * @retval true Printer search is cancelled
     * @retval false Printer search is not cancelled
     */
    public boolean isCancelled() {
        return mIsCancelled;
    }
    
    /**
     * @brief Updates the Online Status of the specified printer view.
     * 
     * @param ipAddress IP Address of the printer
     * @param view View of the online indicator
     */
    public void updateOnlineStatus(String ipAddress, View view) {
        if(ipAddress == null) {
            return;
        }
        new UpdateOnlineStatusTask(view, ipAddress).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    /**
     * @brief Set Printer Search Screen Callback. <br>
     *
     * Set the Callback for Adding Printer to the Searched Printer List during Device Discovery/Manual search.
     */
    public void setPrinterSearchCallback(PrinterSearchCallback printerSearchCallback) {
        mPrinterSearchCallback = new WeakReference<PrinterSearchCallback>(printerSearchCallback);
    }
    
    /**
     * @brief Set Printer Screen Callback. <br>
     *
     * Sets the Saved Printer List/View Callback for adding printers.
     */
    public void setPrintersCallback(PrintersCallback printersCallback) {
        mPrintersCallback = new WeakReference<PrintersCallback>(printersCallback);
    }
    
    /**
     * @brief Set Update Status Callback. <br>
     *
     * Set the Callback for Updating Printer Status. Changes status from online to off-line or vice-versa.
     */
    public void setUpdateStatusCallback(UpdateStatusCallback updateStatusCallback) {
        mUpdateStatusCallback = new WeakReference<UpdateStatusCallback>(updateStatusCallback);
    }
    
    /**
     * @brief Get Printer Count.
     * 
     * @return Printer count
     */
    public int getPrinterCount() {
        return mPrinterList.size();
    }
    
    /**
     * @brief Check the Device status. <br>
     *
     * Checks the Device if it is online. This function should not be called from the main thread.
     * 
     * @retval true Device is online
     * @retval false Device is off-line
     */
    public boolean isOnline(String ipAddress) {
        InetAddress inetIpAddress = null;
        try {
            if (ipAddress == null) {
                return false;
            }
            if (NetUtils.isIPv6Address(ipAddress)) {
                return NetUtils.connectToIpv6Address(ipAddress, inetIpAddress);
            } else {
                inetIpAddress = InetAddress.getByName(ipAddress);
            }
            return inetIpAddress.isReachable(AppConstants.CONST_TIMEOUT_PING);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * @brief Create Update Status Thread. <br>
     *
     * Creates the thread that updates the online/off-line status.
     * The thread must be terminated by calling cancelUpdateStatusThread()
     * when the status view is no longer visible.
     */
    public void createUpdateStatusThread() {
        if (mUpdateStatusTimer != null) {
            return;
        }
        mUpdateStatusTimer = new Timer();
        mUpdateStatusTimer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                if (mUpdateStatusCallback != null && mUpdateStatusCallback.get() != null) {
                    mUpdateStatusCallback.get().updateOnlineStatus();
                }
            }
        }, 0, AppConstants.CONST_UPDATE_INTERVAL);
    }
    
    /**
     * @brief Cancel Update Status Thread. <br>
     *
     * Stops the thread that updates the online/off-line status.
     */
    public void cancelUpdateStatusThread() {
        if (mUpdateStatusTimer == null) {
            return;
        }
        mUpdateStatusTimer.cancel();
        mUpdateStatusTimer = null;
    }
    
    /**
     * @brief Setup printer configuration. <br>
     *
     * Saves the printer configuration/capabilities
     * 
     * @param printer Printer object
     * @param capabilities Printer capabilities
     */
    protected static void setupPrinterConfig(Printer printer, boolean[] capabilities) {
        if (printer == null || capabilities == null) {
            return;
        }
        
        for (int i = 0; i < capabilities.length; i++) {
            switch (i) {
                case SNMPManager.SNMP_CAPABILITY_BOOKLET_FINISHING:
                    printer.getConfig().setBookletFinishingAvailable(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_STAPLER:
                    printer.getConfig().setStaplerAvailable(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_FINISH_2_3:
                    printer.getConfig().setPunch3Available(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_FINISH_2_4:
                    printer.getConfig().setPunch4Available(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_TRAY_FACE_DOWN:
                    printer.getConfig().setTrayFaceDownAvailable(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_TRAY_TOP:
                    printer.getConfig().setTrayTopAvailable(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_TRAY_STACK:
                    printer.getConfig().setTrayStackAvailable(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_LPR:
                    printer.getConfig().setLprAvailable(capabilities[i]);
                    break;
                case SNMPManager.SNMP_CAPABILITY_RAW:
                    printer.getConfig().setRawAvailable(capabilities[i]);
                    break;
            }
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * @brief Save Printer Information. <br>
     *
     * Saves the printer information to the database
     * 
     * @param printer Printer object
     * 
     * @retval true Successful
     * @retval false Failed
     */
    private boolean savePrinterInfo(Printer printer) {
        // Create Content
        ContentValues newPrinter = new ContentValues();
        
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_IP, printer.getIpAddress());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_NAME, printer.getName());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PORT, printer.getPortSetting().ordinal());
        
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_LPR, (printer.getConfig().isLprAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_RAW, (printer.getConfig().isRawAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_BOOKLET_FINISHING, (printer.getConfig().isBookletFinishingAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_STAPLER, (printer.getConfig().isStaplerAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PUNCH3, (printer.getConfig().isPunch3Available() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PUNCH4, (printer.getConfig().isPunch4Available() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_TRAYFACEDOWN, (printer.getConfig().isTrayFaceDownAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_TRAYTOP, (printer.getConfig().isTrayTopAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_TRAYSTACK, (printer.getConfig().isTrayStackAvailable() ? 1 : 0));

        if (!mDatabaseManager.insert(KeyConstants.KEY_SQL_PRINTER_TABLE, null, newPrinter)) {
            mDatabaseManager.close();
            return false;
        }
        mDatabaseManager.close();
        return true;
    }
    
    /**
     * @brief Set the Printer ID of the Printer object.
     * 
     * @param printer Printer object
     * 
     * @retval true Successful
     * @retval false Failed
     */
    private boolean setPrinterId(Printer printer) {
        Cursor cursor = mDatabaseManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_NAME + "=? and "
                + KeyConstants.KEY_SQL_PRINTER_IP + "=?", new String[] { printer.getName(), printer.getIpAddress() }, null, null, null);
        boolean ret = false;
               
        ret = getIdFromCursor(cursor, printer);
        if (ret) {
            mDatabaseManager.close();
        }
        return ret;
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
    protected boolean getIdFromCursor(Cursor cursor, Printer printer) {
        if (cursor != null && cursor.moveToFirst() && printer != null) {
            printer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID)));
            cursor.close();
            return true;
        }
        return false;
    }
    
    // ================================================================================
    // Interface - SNMPManagerCallback
    // ================================================================================
    
    @Override
    public void onEndDiscovery(SNMPManager manager, int result) {
        if (manager == null) {
            return;
        }
        mIsSearching = false;
        manager.finalizeSNMPManager();
        
        if (mPrinterSearchCallback != null && mPrinterSearchCallback.get() != null) {
            mPrinterSearchCallback.get().onSearchEnd();
        }
    }
    
    @Override
    public void onFoundDevice(SNMPManager manager, String ipAddress, String name, boolean[] capabilities) {
        if (manager == null || ipAddress == null || name == null || capabilities == null) {
            return;
        }
        Printer printer = new Printer(name, ipAddress);
        PrinterManager.setupPrinterConfig(printer, capabilities);
        
        if (isSearching()) {
            if (mPrinterSearchCallback != null && mPrinterSearchCallback.get() != null) {
                mPrinterSearchCallback.get().onPrinterAdd(printer);
            }
        }
    }
    
    // ================================================================================
    // Interface - PrinterSearchCallback
    // ================================================================================
    
    /**
     * @brief Printers Search Screen Interface. <br>
     *
     * Interface for Printers Search Screen. Used for updating view in the Printers Search Screen.
     */
    public interface PrinterSearchCallback {
        /**
         * @brief On Printer Add callback. <br>
         *
         * Callback called when a printer is to be added as a result of Printers Search (Manual/Auto)
         * 
         * @param printer Printer object
         */
        public void onPrinterAdd(Printer printer);
        
        /**
         * @brief On Search End callback. <br>
         *
         * Callback called at the end of Printer Search
         */
        public void onSearchEnd();
    }
    
    // ================================================================================
    // Interface - PrintersCallback
    // ================================================================================
    
    /**
     * @brief Printers Screen Interface. <br>
     *
     * Interface for Printers Screen. Used for updating view in the Printers Screen.
     */
    public interface PrintersCallback {
        /**
         * @brief Adds printer to the Printers Screen
         * 
         * @param printer Printer object
         * @param isOnline Printer online status            
         */
        public void onAddedNewPrinter(Printer printer, boolean isOnline);
    }
    
    // ================================================================================
    // Interface - UpdateStatusCallback
    // ================================================================================
    
    /**
     * @brief Update Online Status Interface. <br>
     *
     * Interface for updating the online status.
     */
    public interface UpdateStatusCallback {
        /**
         * @brief Callback to update the online status
         */
        public void updateOnlineStatus();
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @brief Update Online Status Task. <br>
     *
     * AsyncTask that updates changes the online status image.
     */
    class UpdateOnlineStatusTask extends AsyncTask<Object, View, Boolean> {
        private WeakReference<View> mViewRef = null;
        private String mIpAddress = null;
        
        /**
         * @brief Instantiate UpdateOnlineStatusTask.
         */
        public UpdateOnlineStatusTask(View view, String ipAddress) {
            mViewRef = new WeakReference<View>(view);
            mIpAddress = ipAddress;
        }
        
        @Override
        protected Boolean doInBackground(Object... arg) {
            if (mIpAddress.isEmpty()) {
                return false;
            }
            return isOnline(mIpAddress);
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            
            if (mViewRef != null && mViewRef.get() != null) {
                ImageView view = (ImageView) mViewRef.get();
                if (result) {
                    view.setImageResource(R.drawable.img_btn_printer_status_online);
                } else {
                    view.setImageResource(R.drawable.img_btn_printer_status_offline);
                }
            }
        }
        
    }
}