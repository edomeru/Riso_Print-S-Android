//
//  DatabaseController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/17.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using SQLite;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Windows.Storage;

namespace SmartDeviceApp.Controllers
{
    public class DatabaseController
    {
        static readonly DatabaseController _instance = new DatabaseController();

        private const string FILE_NAME_DATABASE = "SmartDeviceAppDB.db";
        private const string FILE_PATH_DATABASE_SCRIPT = "Assets/SmartDeviceAppDB.sql";
        //private const string FORMAT_PRAGMA_FOREIGN_KEYS = "PRAGMA foreign_keys = {0}";
        //private const string ON = "ON";
        //private const string OFF = "OFF";

        private string _databasePath;
        private SQLiteAsyncConnection _dbConnection;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static DatabaseController() { }

        private DatabaseController()
        {
            _databasePath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            _dbConnection = new SQLite.SQLiteAsyncConnection(_databasePath);
        }

        public static DatabaseController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Initialize database
        /// </summary>
        /// <returns></returns>
        public async Task Initialize()
        {
            await CreateDatabase();

            // TODO: Remove after testing (or create an initial data manager)
            await InsertSampleData();
        }

        /// <summary>
        /// Clean up
        /// </summary>
        public void Cleanup()
        {
            SQLiteConnectionPool.Shared.Reset(); // Close connections
        }

        /// <summary>
        /// Create database and tables
        /// </summary>
        /// <returns>task</returns>
        private async Task CreateDatabase()
        {
#if true // CREATE_TABLES_USING_SCRIPT
            #region Create Tables Using Script File

            await ExecuteScript(FILE_PATH_DATABASE_SCRIPT);
            await DefaultsUtility.LoadDefaultsFromSqlScript(FILE_PATH_DATABASE_SCRIPT);

            #endregion Create Create Tables Using Script File
#else // CREATE_TABLES_USING_SCRIPT
            #region Create Tables Using Model Classes

            try
            {
                // TODO: Handling when database file does not exist

                _databasePath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
                using (var db = new SQLite.SQLiteConnection(_databasePath))
                {
                    /*
                    // DeleteAll is for testing purposes only PrintersModule
                    db.DeleteAll<Printer>();
                    db.Commit();

                    db.DeleteAll<DefaultPrinter>();
                    db.Commit();
                     * */

                    // Create the tables if they don't exist

                    // Printer table
                    db.CreateTable<Printer>();
                    db.Commit();

                    // DefaultPrinter Table
                    db.CreateTable<DefaultPrinter>();
                    db.Commit();

                    // PrintSettings Table
                    db.CreateTable<PrintSettings>();
                    db.Commit();

                    // PrintJob Table
                    db.CreateTable<PrintJob>();
                    db.Commit();

                    db.Dispose();
                    db.Close();

                    // insertPrinters(); //for testing PrintersModule
                }
            }
            catch
            {
                // Error in creating tables
            }

            #endregion Create Tables Using Model Classes
#endif // CREATE_TABLES_USING_SCRIPT

            // TODO: Enable this pragma then debug
            // Note: When pragma foreign_keys is enabled, SQLiteException ("Constraints")
            // is encountered when deleting a printer even if the referenced ids are valid
            //await _dbConnection.ExecuteAsync(string.Format(FORMAT_PRAGMA_FOREIGN_KEYS, ON)); // Enable foreign keys
        }

        /// <summary>
        /// Executes all commands in a script file
        /// </summary>
        /// <param name="filePath">file path of the script; assumed to be a resource file</param>
        /// <returns>task</returns>
        private async Task ExecuteScript(string filePath)
        {
            string scriptText = null;

            try
            {
                // Read script file
                StorageFile file = await StorageFileUtility.GetFileFromAppResource(filePath);
                scriptText = await FileIO.ReadTextAsync(file);
            }
            catch
            {
                // Error handling
            }

            if (!string.IsNullOrEmpty(scriptText))
            {
                // Loop each commands
                string[] lines = scriptText.Split(new char[] { ';' },
                                                  StringSplitOptions.RemoveEmptyEntries);

                foreach (string line in lines)
                {
                    try
                    {
                        // Since each parameter in the script is in each line,
                        // convert them into a single line statement
                        string sqlStatement = line.Replace("\r\n", string.Empty).Trim();
                        if (!string.IsNullOrEmpty(sqlStatement))
                        {
                            await _dbConnection.ExecuteAsync(sqlStatement);
                        }
                    }
                    catch (SQLiteException)
                    {
                        // Error handling
                        // Possible cause:
                        // * table/item already exists
                    }
                }
            }
        }

        #region Printer Table Operations

        /*
        private void insertPrinters()
        {
            Printer printer = new Printer(1, 1, "192.168.0.22", "RISO_Printer1", 1, true, true,
                true, true, true, true, true);
            Printer printer2 = new Printer(2, 2, "192.168.0.2", "RISO_Printer2", 1, true, true,
                true, true, true, true, true);
            Printer printer3 = new Printer(3, 3, "192.168.0.3", "RISO_Printer3", 1, true, true,
                true, true, true, true, true);

            DefaultPrinter dp = new DefaultPrinter(printer.Id);

            var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path,
                DATABASE_FILE_NAME);
            using (var db = new SQLite.SQLiteConnection(dbpath))
            {
                // Create the tables if they don't exist
                db.Insert(printer);
                db.Commit();

                db.Insert(printer2);
                db.Commit();

                db.Insert(printer3);
                db.Commit();

                db.Insert(dp);
                db.Commit();

                db.Dispose();
                db.Close();
            }


        }
        */

        /// <summary>
        /// Insert an item into Printer table
        /// </summary>
        /// <param name="printer">printer to be added</param>
        /// <returns>task; number for added rows</returns>
        public async Task<int> InsertPrinter(Printer printer)
        {
            if (printer == null)
            {
                return 0;
            }

            try
            {
                int i = await _dbConnection.InsertAsync(printer);
                if (i > 0) //1 object inserted to table
                {
                    //get id of last inserted
                    return printer.Id;
                }
                else
                {
                    return -1;
                }
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        public async Task<List<Printer>> GetPrinters()
        {
            var printerList = new List<Printer>();

            try
            {
                printerList = await (_dbConnection.Table<Printer>().ToListAsync());
            }
            catch
            {
                // Error handling
            }

            return printerList;
        }

        // TODO: Check usage. Use public async Task<int> UpdateDefaultPrinter(int printerId) below instead
        public async Task<int> SetDefaultPrinter(int printerId)
        {
            try
            {
                if (printerId < 0)
                {
                    //delete value in table
                }
                else
                {
                    var existingDefault = await (_dbConnection.Table<DefaultPrinter>().FirstOrDefaultAsync());

                    if (existingDefault != null)
                    {
                        // update default printer id
                        existingDefault.PrinterId = (uint)printerId;
                    }
                    else
                    {
                        // no default printer, insert new
                        DefaultPrinter dp = new DefaultPrinter();
                        dp.PrinterId = (uint)printerId;

                        int success = await _dbConnection.InsertAsync(dp);
                    }
                }
            }
            catch
            {
                return 0;
            }
            return 1;
        }

        /// <summary>
        /// Updates a printer in the database
        /// </summary>
        /// <param name="printer">printer to be updated</param>
        /// <returns>task; number of updated rows</returns>
        public async Task<int> UpdatePrinter(Printer printer)
        {
            if (printer == null)
            {
                return 0;
            }

            try
            {
                return await _dbConnection.UpdateAsync(printer);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        /// <summary>
        /// Deletes a printer from the database
        /// </summary>
        /// <param name="printer">printer to be deleted</param>
        /// <returns>task; number of deleted items</returns>
        public async Task<int> DeletePrinter(Printer printer)
        {
            if (printer == null)
            {
                return 0;
            }

            try
            {
                int count = await _dbConnection.Table<DefaultPrinter>().CountAsync();
                if (count > 0)
                {
                    DefaultPrinter dp = await _dbConnection.Table<DefaultPrinter>().FirstAsync();

                    if (printer.Id == dp.PrinterId)
                    {
                        //delete default.
                        await _dbConnection.DeleteAsync(dp);
                    }
                }


                return await _dbConnection.DeleteAsync(printer);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        /// <summary>
        /// Retrives the Printer from the database
        /// </summary>
        /// <param name="id">printer ID</param>
        /// <returns>task; Printer object if found, null otherwise</returns>
        public async Task<Printer> GetPrinter(int id)
        {
            try
            {
                return await _dbConnection.GetAsync<Printer>(id);
            }
            catch
            {
                // Error handling here
            }
            return null;
        }

        ///// <summary>
        ///// Retrieves the printer name
        ///// </summary>
        ///// <param name="id">printer ID</param>
        ///// <returns>task; printer name if found, empty string otherwise</returns>
        //public async Task<string> GetPrinterName(int id)
        //{
        //    try
        //    {
        //        Printer printer = await _dbConnection.GetAsync<Printer>(id);
        //        if (printer != null)
        //        {
        //            return printer.Name;
        //        }
        //    }
        //    catch
        //    {
        //        // Error handling here
        //    }
        //    return string.Empty;
        //}

        public async Task UpdatePortNumber(Printer printer)
        {
            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                var printerFromDB = await db.GetAsync<Printer>(printer.Id);
                printerFromDB.PortSetting = printer.PortSetting;

                int i = await db.UpdateAsync(printerFromDB);
            }
            catch
            {
                // Error handling here
            }
            return;
        }

        #endregion Printer Table Operations

        #region DefaultPrinter Table Operations

        /// <summary>
        /// Retrieves the default printer
        /// </summary>
        /// <returns>task; DefaultPrinter object if found, null otherwise</returns>
        public async Task<DefaultPrinter> GetDefaultPrinter()
        {
            try
            {
                return await _dbConnection.Table<DefaultPrinter>().FirstOrDefaultAsync();
            }
            catch
            {
                // Error handling here
            }
            return null;
        }

        /// <summary>
        /// Sets the default printer in the database
        /// </summary>
        /// <param name="printerId"></param>
        /// <returns></returns>
        public async Task<int> UpdateDefaultPrinter(int printerId)
        {
            try
            {
                DefaultPrinter existingDefault = await _dbConnection.Table<DefaultPrinter>()
                                                                    .FirstOrDefaultAsync();

                if (existingDefault != null)
                {
                    // TODO: Verify if printer to be set as default printer exists before deletion
                    // Or just assume that calls to this function is always an existing printer ?
                    await _dbConnection.DeleteAsync(existingDefault);
                }

                DefaultPrinter newDefaultPrinter = new DefaultPrinter() { PrinterId = (uint)printerId };
                return await _dbConnection.InsertAsync(newDefaultPrinter);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        #endregion DefaultPrinter Table Operations

        #region PrintSetting Table Operations

        /// <summary>
        /// Retrives print settings
        /// </summary>
        /// <param name="id">print setting ID</param>
        /// <returns>task; print settings if found, null otherwise</returns>
        public async Task<PrintSettings> GetPrintSettings(int id)
        {
            if (id < 0)
            {
                return null;
            }

            try
            {
                return await _dbConnection.GetAsync<PrintSettings>(id);
            }
            catch
            {
                // Error handling here
            }
            return null;
        }

        /// <summary>
        /// Insert an item into PrintSetting table
        /// </summary>
        /// <param name="printSettings">print settings to be added</param>
        /// <returns>task; number for added rows</returns>
        public async Task<int> InsertPrintSettings(PrintSettings printSettings)
        {
            if (printSettings == null)
            {
                return 0;
            }

            try
            {
                return await _dbConnection.InsertAsync(printSettings);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        /// <summary>
        /// Updates a print setting in the database
        /// </summary>
        /// <param name="printSettings">print settings</param>
        /// <returns>task; number of updated print settings</returns>
        public async Task<int> UpdatePrintSettings(PrintSettings printSettings)
        {
            if (printSettings == null)
            {
                return 0;
            }

            try
            {
                return await _dbConnection.UpdateAsync(printSettings);
            }
            catch
            {
                // Error handling here
            }
            return 0;
        }

        /// <summary>
        /// Deletes a print setting in the database
        /// </summary>
        /// <param name="printSettings">print settings</param>
        /// <returns>task; number of deleted rows</returns>
        public async Task<int> DeletePrintSettings(PrintSettings printSettings)
        {
            if (printSettings == null)
            {
                return 0;
            }

            try
            {
                return await _dbConnection.DeleteAsync(printSettings);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        #endregion PrintSetting Table Operations

        #region PrintJob Table Operations

        /// <summary>
        /// Retrieves all print jobs
        /// </summary>
        /// <returns>task; list of all print jobs</returns>
        public async Task<List<PrintJob>> GetPrintJobs()
        {
            var printJobsList = new List<PrintJob>();

            try
            {
                printJobsList = await _dbConnection.Table<PrintJob>().ToListAsync();
            }
            catch
            {
                // Error handling here
            }

            return printJobsList;
        }

        /// <summary>
        /// Insert an item into PrintJob table
        /// </summary>
        /// <param name="printJob">print job to be added</param>
        /// <returns>task; number for added rows</returns>
        public async Task<int> InsertPrintJob(PrintJob printJob)
        {
            if (printJob == null)
            {
                return 0;
            }

            try
            {
                return await _dbConnection.InsertAsync(printJob);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        /// <summary>
        /// Updates a print job in the database
        /// </summary>
        /// <param name="printJob">print job to be updated</param>
        /// <returns>task; number of updated rows</returns>
        public async Task<int> UpdatePrintJob(PrintJob printJob)
        {
            if (printJob == null)
            {
                return 0;
            }

            try
            {
                return await _dbConnection.UpdateAsync(printJob);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        /// <summary>
        /// Deletes a print job from the database
        /// </summary>
        /// <param name="printJob">print job to be deleted</param>
        /// <returns>task; number of deleted items</returns>
        public async Task<int> DeletePrintJob(PrintJob printJob)
        {
            if (printJob == null)
            {
                return 0;
            }

            try
            {
                return await _dbConnection.DeleteAsync(printJob);
            }
            catch
            {
                // Error handling
            }

            return 0;
        }

        #endregion PrintJob Table Operations

        #region Dummy - Initial Data

        /// <summary>
        /// Inserts sample data on first run of app after installation
        /// </summary>
        /// <returns>task</returns>
        private async Task InsertSampleData()
        {
            bool isPreviouslyLoaded = false;
            string key = "IsSampleDataAlreadyLoaded";
            var localSettings = ApplicationData.Current.LocalSettings;
            if (localSettings.Values.ContainsKey(key))
            {
                isPreviouslyLoaded = (bool)localSettings.Values[key];
            }
            else
            {
                localSettings.Values[key] = true;
            }

            if (!isPreviouslyLoaded)
            {
                //await _dbConnection.ExecuteAsync(string.Format(FORMAT_PRAGMA_FOREIGN_KEYS, OFF)); // Disable foreign keys
                await ExecuteScript("Resources/Dummy/SampleData.sql");
                //await _dbConnection.ExecuteAsync(string.Format(FORMAT_PRAGMA_FOREIGN_KEYS, ON)); // Re-enable foreign keys
            }
        }

        #endregion Dummy - Initial Data

    }
}
