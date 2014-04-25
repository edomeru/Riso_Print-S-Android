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

        private string _databasePath;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static DatabaseController() { }

        private DatabaseController() { }

        public static DatabaseController Instance
        {
            get { return _instance; }
        }

        public async Task Initialize()
        {
            await CreateDatabase();

            // TODO: Remove after testing (or create an initial data manager)
            await InsertSampleData();
        }

        private async Task CreateDatabase()
        {
#if true // CREATE_TABLES_USING_SCRIPT
            #region Create Tables Using Script File

            await ExecuteScript(FILE_PATH_DATABASE_SCRIPT);

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
        }

        /// <summary>
        /// Executes all commands in a script file
        /// </summary>
        /// <param name="filePath">file path of the script; assumed to be a resource file</param>
        /// <returns>task</returns>
        private async Task ExecuteScript(string filePath)
        {
            try
            {
                _databasePath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
                using (var db = new SQLite.SQLiteConnection(_databasePath))
                {
                    // Read script from Dummy Resources and create tables
                    StorageFile file = await StorageFileUtility.GetFileFromAppResource(filePath);
                    string script = await FileIO.ReadTextAsync(file);

                    // Loop each commands
                    string[] lines = script.Split(new char[] { ';' },
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
                                db.Execute(sqlStatement);
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
            catch
            {
                // Error handling
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

        private void InsertPrinter(Printer printer)
        {
            using (var db = new SQLite.SQLiteConnection(_databasePath))
            {
                // Create the tables if they don't exist
                db.Insert(printer);
                db.Commit();

                db.Dispose();
                db.Close();
            }


        }

        public async Task<List<Printer>> GetPrinters()
        {
            var printerList = new List<Printer>();
            try
            {
                var db = new SQLite.SQLiteAsyncConnection(_databasePath);

                printerList = await (db.Table<Printer>().ToListAsync());
            }
            catch
            {
            }

            return printerList;
        }

        public async Task<int> SetDefaultPrinter(int printerId)
        {
            var db = new SQLite.SQLiteAsyncConnection(_databasePath);
            try
            {
                if (printerId < 0)
                {
                    //delete value in table
                }
                else
                {
                    var existingDefault = await (db.Table<DefaultPrinter>().FirstOrDefaultAsync());

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

                        int success = await db.InsertAsync(dp);
                    }
                }
            }
            catch
            {
                return 0;
            }
            return 1;
        }

        public async Task<int> DeletePrinterFromDB(int printerId)
        {
            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                //delete in printer table
                var printer = await db.Table<Printer>().Where(
                    p => p.Id == printerId).FirstAsync();

                //check if default printer
                var defaultPrinter = await db.Table<DefaultPrinter>().FirstAsync();

                if (printer.Id == defaultPrinter.PrinterId)
                {
                    //update default printer in DB
                    await db.DeleteAsync(defaultPrinter);
                }

                //delete in Printer table

                await db.DeleteAsync(printer);
            }
            catch
            {
                return 1;
            }

            return 0;
        }

        /// <summary>
        /// Retrieves the default printer
        /// </summary>
        /// <returns>task; DefaultPrinter object if found, null otherwise</returns>
        public async Task<DefaultPrinter> GetDefaultPrinter()
        {
            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                int defaultPrinterCount = await db.Table<DefaultPrinter>().CountAsync();
                if (defaultPrinterCount > 0)
                {
                    return await (db.Table<DefaultPrinter>().FirstOrDefaultAsync());
                }
            }
            catch
            {
                // Error handling here
            }
            return null;
        }

        /// <summary>
        /// Retrives the Printer from the database
        /// </summary>
        /// <param name="id">printer ID</param>
        /// <returns>task; Printer object if found, null otherwise</returns>
        public async Task<Printer> GetPrinter(int id)
        {
            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                return await db.GetAsync<Printer>(id);
            }
            catch
            {
                // Error handling here
            }
            return null;
        }

        /// <summary>
        /// Retrieves the printer name (or IP address when printer name is empty)
        /// </summary>
        /// <param name="id">printer ID</param>
        /// <returns>task; printer name if found, empty string otherwise</returns>
        public async Task<string> GetPrinterName(int id)
        {
            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                Printer printer = await db.GetAsync<Printer>(id);
                if (printer != null)
                {
                    if (string.IsNullOrEmpty(printer.Name.Trim()))
                    {
                        return printer.IpAddress;
                    }
                    return printer.Name;
                }
            }
            catch
            {
                // Error handling here
            }
            return string.Empty;
        }

        #endregion Printer Table Operations

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

            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                return await db.GetAsync<PrintSettings>(id);
            }
            catch
            {
                // Error handling here
            }
            return null;
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

            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                printJobsList = await db.Table<PrintJob>().ToListAsync();
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

            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                return await db.InsertAsync(printJob);
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

            var db = new SQLite.SQLiteAsyncConnection(_databasePath);

            try
            {
                return await db.DeleteAsync(printJob);
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
                await ExecuteScript("Resources/Dummy/SampleData.sql");
            }
        }

        #endregion Dummy - Initial Data

    }
}
