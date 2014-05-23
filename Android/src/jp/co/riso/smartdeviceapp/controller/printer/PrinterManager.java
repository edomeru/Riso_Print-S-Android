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
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.common.SNMPManager;
import jp.co.riso.smartdeviceapp.common.SNMPManager.SNMPManagerCallback;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
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
     * Printer Manager Constructor
     * <p>
     * Printer Manager Constructor
     * 
     * @param context
     *            Context of the SmartDeviceApp
     */
    private PrinterManager(Context context) {        
        mDatabaseManager = new DatabaseManager(context);
        mPrinterList = new ArrayList<Printer>();
        mSNMPManager = new SNMPManager();
        mSNMPManager.setCallback(this);
    }
    
    /**
     * Printer Manager Constructor for testing
     * <p>
     * Printer Manager Constructor
     * 
     * @param context
     *            Context of the SmartDeviceApp
     * @param databaseManager
     *            DatabaseManager instance
     */
    protected PrinterManager(Context context, DatabaseManager databaseManager) {
        mDatabaseManager = databaseManager;
        if (mDatabaseManager == null) {
            mDatabaseManager = new DatabaseManager(context);
        }
        mPrinterList = new ArrayList<Printer>();
        mSNMPManager = new SNMPManager();
        mSNMPManager.setCallback(this);
    }
    
    /**
     * Get PrinterManager instance
     * <p>
     * Get the PrinterManager instance
     * 
     * @param context
     *            Context of the SmartDeviceApp
     * @return sSharedMngr
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
     * Save Printer to the Database
     * <p>
     * Saves the Printer to the Database
     * 
     * @param printer
     *            Printer object containing the Printer Information such as Print Settings
     * @param isOnline
     *            Printer online status
     * @return true if successful
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
        
        if (mPrintersCallback != null && mPrintersCallback.get() != null) {
            mPrintersCallback.get().onAddedNewPrinter(printer, isOnline);
        }
        
        mPrinterList.add(printer);
        
        return true;
    }
    
    /**
     * Check Printer Existence
     * <p>
     * Determines if the Printer exists in the Saved Printer List
     * 
     * @param printer
     *            Printer object that must contain the IP address and Printer Name.
     * @return true if exists
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
     * Check Printer Existence
     * <p>
     * Determines if the Printer exists in the Saved Printer List
     * 
     * @param ipAddress
     *            IP address of the Printer.
     * @return true if exists
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
     * Retrieves the Printer objects from the Database
     * 
     * @return list of Printer objects
     */
    public List<Printer> getSavedPrintersList() {
        if (mPrinterList.size() != 0) {
            return mPrinterList;
        }
        
        Cursor cursor = mDatabaseManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        
        mPrinterList.clear();
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
                printer.setPortSetting(DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PORT));
                
                boolean lprAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_LPR);
                boolean rawAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_RAW);
                boolean bookletAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_BOOKLET);
                boolean staplerAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_STAPLER);
                boolean punch3Available = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PUNCH3);
                boolean punch4Available = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PUNCH4);
                boolean trayFaceDownAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYFACEDOWN);
                boolean trayTopAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYTOP);
                boolean trayStackAvailable = DatabaseManager.getBooleanFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYSTACK);
                
                printer.getConfig().setLprAvailable(lprAvailable);
                printer.getConfig().setRawAvailable(rawAvailable);
                printer.getConfig().setBookletAvailable(bookletAvailable);
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
     * Sets the Default Printer by clearing and inserting an entry in DefaultPrinter table
     * 
     * @param printer
     *            The Printer object selected
     */
    public void setDefaultPrinter(Printer printer) {
        
        if (printer == null) {
            return;
        }
        clearDefaultPrinter();
        
        ContentValues newDefaultPrinter = new ContentValues();
        
        newDefaultPrinter.put(KeyConstants.KEY_SQL_PRINTER_ID, printer.getId());
        
        if (!mDatabaseManager.insert(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, newDefaultPrinter)) {
            mDatabaseManager.close();
            return;
        }
        
        mDefaultPrintId = printer.getId();
        mDatabaseManager.close();
    }
    
    /**
     * Clears the Default Printer table in the database.
     * 
     */
    public void clearDefaultPrinter() {
        mDatabaseManager.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null);
        mDefaultPrintId = EMPTY_ID;
        mDatabaseManager.close();
    }
    
    /**
     * Removes the Printer from the database.
     * 
     * @param printer
     *            The Printer object selected for deletion
     * @return true if successful
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
                mDefaultPrintId = EMPTY_ID;
            }
        }
        mDatabaseManager.close();
        return ret;
    }

    /**
     * Get Default Printer
     * <p>
     * Obtains the printer ID of the default printer
     * 
     * @return Printer ID of the default printer
     */
    public int getDefaultPrinter() {
        if (mDefaultPrintId != EMPTY_ID) {
            return mDefaultPrintId;
        }
                
        Cursor cursor = mDatabaseManager.query(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID, null, null, null, null);
        
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
     * Search for Printer Device
     * <p>
     * Search for the Printer Devices using Device Discovery/Auto Search.
     * 
     * @param ipAddress
     *            The IP Address of the Printer
     */
    public void startPrinterSearch() {
        mIsSearching = true;
        mIsCancelled = false;
        mSNMPManager.initializeSNMPManager();
        mSNMPManager.deviceDiscovery();
        Log.d("PrinterManager", "Auto");
    }
    
    /**
     * Search for Printer Device
     * <p>
     * Search for the Printer Device using Manual Search.
     * 
     * @param ipAddress
     *            The IP Address of the Printer
     */
    public void searchPrinter(String ipAddress) {
        
        if (ipAddress == null) {
            return;
        }
        
        mIsSearching = true;
        mIsCancelled = false;
        mSNMPManager.initializeSNMPManager();
        mSNMPManager.manualDiscovery(ipAddress);
        Log.d("PrinterManager", "Manual");
    }
    
    /**
     * Cancel Printer Search
     * <p>
     * Stops Device Discovery/Manual Search.
     */
    public void cancelPrinterSearch() {
        mIsSearching = false;
        mIsCancelled = true;
        new Timer().schedule(new TimerTask() {
            
            @Override
            public void run() {
                mSNMPManager.cancel();
                Log.d("PrinterManager", "Cancel");
            }
        }, 0);
    }
    
    /**
     * <p>
     * Checks if there is an ongoing printer search.
     * 
     * @return mIsSearching
     */
    public boolean isSearching() {
        return mIsSearching;
    }
    
    /**
     * <p>
     * Checks if the printer search was cancelled.
     * 
     * @return mIsCancelled
     */
    public boolean isCancelled() {
        return mIsCancelled;
    }
    
    /**
     * <p>
     * Updates the Online Status of the specified printer view
     * 
     * @param ipAddress
     *            IP Address of the printer
     * @param view
     *            View of the online indicator
     */
    public void updateOnlineStatus(String ipAddress, View view) {
        if(ipAddress == null) {
            return;
        }
        new UpdateOnlineStatusTask(view, ipAddress).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    /**
     * Set Printer Search Screen Callback
     * <p>
     * Set the Callback for Adding Printer to the Searched Printer List during Device Discovery/Manual search.
     */
    public void setPrinterSearchCallback(PrinterSearchCallback printerSearchCallback) {
        mPrinterSearchCallback = new WeakReference<PrinterSearchCallback>(printerSearchCallback);
    }
    
    /**
     * Set Printer Screen Callback
     * <p>
     * Sets the Saved Printer List/View Callback for adding printers.
     */
    public void setPrintersCallback(PrintersCallback printersCallback) {
        mPrintersCallback = new WeakReference<PrintersCallback>(printersCallback);
    }
    
    /**
     * Set Update Status Callback
     * <p>
     * Set the Callback for Updating Printer Status. Changes status from online to off-line or vice-versa.
     */
    public void setUpdateStatusCallback(UpdateStatusCallback updateStatusCallback) {
        mUpdateStatusCallback = new WeakReference<UpdateStatusCallback>(updateStatusCallback);
    }
    
    /**
     * Get Printer Count
     * <p>
     * 
     * @return printer count
     */
    public int getPrinterCount() {
        return mPrinterList.size();
    }
    
    /**
     * Check if the Device is online
     * <p>
     * Checks the Device if it is online. This function should not be called from the main thread.
     * 
     * @return true if online
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
     * Create Update Status Thread
     * <p>
     * Creates a thread that updates the online/off-line status.
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
     * Cancel Update Status Thread
     * <p>
     * Stops the thread that updates the online/off-line status.
     */
    public void cancelUpdateStatusThread() {
        if (mUpdateStatusTimer == null) {
            return;
        }
        mUpdateStatusTimer.cancel();
        mUpdateStatusTimer = null;
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * Save Printer Information
     * <p>
     * Saves the printer information to the database
     * 
     * @param printer
     *            Printer object
     * @return true if successful
     */
    private boolean savePrinterInfo(Printer printer) {
        // Create Content
        ContentValues newPrinter = new ContentValues();
        
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_IP, printer.getIpAddress());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_NAME, printer.getName());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PORT, printer.getPortSetting());
        
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_LPR, (printer.getConfig().isLprAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_RAW, (printer.getConfig().isRawAvailable() ? 1 : 0));
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_BOOKLET, (printer.getConfig().isBookletAvailable() ? 1 : 0));
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
     * Setup printer configuration
     * <p>
     * Saves the printer configuration/capabilities
     * 
     * @param capabilities
     *            Printer capabilities
     */
    private static void setupPrinterConfig(Printer printer, boolean[] capabilities) {
        printer.getConfig().setBookletAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_BOOKLET]);
        printer.getConfig().setStaplerAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_STAPLER]);
        printer.getConfig().setPunch4Available(capabilities[SNMPManager.SNMP_CAPABILITY_FINISH_2_4]);
        printer.getConfig().setTrayFaceDownAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_TRAY_FACE_DOWN]);
        printer.getConfig().setTrayTopAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_TRAY_TOP]);
        printer.getConfig().setTrayStackAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_TRAY_STACK]);
        
    }
    
    /**
     * Set Printer ID
     * <p>
     * Set the Printer ID of the Printer object. 
     * 
     * @param printer
     *            Printer object
     * @return true if successful
     */
    private boolean setPrinterId(Printer printer) {
        Cursor cursor = mDatabaseManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_NAME + "=? and "
                + KeyConstants.KEY_SQL_PRINTER_IP + "=?", new String[] { printer.getName(), printer.getIpAddress() }, null, null, null);
        boolean ret = false;
               
        ret = getIdFromCursor(cursor, printer);

        mDatabaseManager.close();
        return ret;
    }
    
    /**
     * Get Printer ID from Cursor object
     * 
     * @param cursor
     *            Cursor object
     * @param printer
     *            Printer object
     * @return true when successful
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
    
    /** {@inheritDoc} */
    @Override
    public void onEndDiscovery(SNMPManager manager, int result) {
        if (manager == null) {
            return;
        }
        mIsSearching = false;
        manager.finalizeSNMPManager();
        Log.d("PrinterManager", "Finalize");
        if (mPrinterSearchCallback != null && mPrinterSearchCallback.get() != null) {
            mPrinterSearchCallback.get().onSearchEnd();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onFoundDevice(SNMPManager manager, String ipAddress, String name, boolean[] capabilities) {
        if (manager == null || ipAddress == null || name == null || capabilities == null) {
            return;
        }
        Printer printer = new Printer(name, ipAddress);
        PrinterManager.setupPrinterConfig(printer, capabilities);
        Log.d("PrinterManager", "onFoundDevice");
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
     * Printers Search Screen Interface
     * <p>
     * Interface for Printers Search Screen. Used for updating view in the Printers Search Screen.
     */
    public interface PrinterSearchCallback {
        /**
         * On Printer Add callback.
         * <p>
         * Callback called when a printer is to be added as a result of Printers Search (Manual/Auto)
         * 
         * @param printer
         *            Printer object
         */
        public void onPrinterAdd(Printer printer);
        
        /**
         * On Search End callback.
         * <p>
         * Callback called at the end of Printer Search
         */
        public void onSearchEnd();
    }
    
    // ================================================================================
    // Interface - PrintersCallback
    // ================================================================================
    
    /**
     * Printers Screen Interface
     * <p>
     * Interface for Printers Screen. Used for updating view in the Printers Screen.
     */
    public interface PrintersCallback {
        /**
         * Adds printer to the Printers Screen
         * 
         * @param printer
         *            printer object
         * @param isOnline
         *            Printer online status            
         */
        public void onAddedNewPrinter(Printer printer, boolean isOnline);
    }
    
    // ================================================================================
    // Interface - UpdateStatusCallback
    // ================================================================================
    
    /**
     * Update Online Status Interface
     * <p>
     * Interface for updating the online status.
     */
    public interface UpdateStatusCallback {
        /**
         * Callback to update the online status
         */
        public void updateOnlineStatus();
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * Update Online Status Task
     * <p>
     * AsyncTask that updates changes the online status image.
     */
    class UpdateOnlineStatusTask extends AsyncTask<Object, View, Boolean> {
        private WeakReference<View> mViewRef = null;
        private String mIpAddress = null;
        
        /**
         * Instantiate UpdateOnlineStatusTask
         */
        public UpdateOnlineStatusTask(View view, String ipAddress) {
            mViewRef = new WeakReference<View>(view);
            mIpAddress = ipAddress;
        }
        
        /** {@inheritDoc} */
        @Override
        protected Boolean doInBackground(Object... arg) {
            if (mIpAddress.isEmpty()) {
                return false;
            }
            return isOnline(mIpAddress);
        }
        
        /** {@inheritDoc} */
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
