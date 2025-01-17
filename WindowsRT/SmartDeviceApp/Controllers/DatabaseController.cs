﻿//
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
        private const string FORMAT_PRAGMA_FOREIGN_KEYS = "PRAGMA foreign_keys = {0}";
        private const string ON = "ON";
        private const string OFF = "OFF";

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

        /// <summary>
        /// DatabaseController singleton instance
        /// </summary>
        public static DatabaseController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Initialize database
        /// </summary>
        /// <returns>task</returns>
        public async Task Initialize()
        {
            // Check if database file exists. Otherwise, create database.
            if ((await StorageFileUtility.GetExistingFile(FILE_NAME_DATABASE,
                                                    ApplicationData.Current.LocalFolder)) == null)
            {
                await CreateDatabase();
            }
            await EnablePragmaForeignKeys(true); // Enable foreign keys
            await DefaultsUtility.LoadDefaultsFromSqlScript(FILE_PATH_DATABASE_SCRIPT);
        }

        /// <summary>
        /// Clean up
        /// </summary>
        public void Cleanup()
        {
            SQLiteConnectionPool.Shared.Reset(); // Close connections
        }

        /// <summary>
        /// Enables/disables foreign key constraints
        /// </summary>
        /// <param name="state">true when on, false otherwise</param>
        /// <returns>task</returns>
        public async Task EnablePragmaForeignKeys(bool state)
        {
            await _dbConnection.ExecuteAsync(string.Format(FORMAT_PRAGMA_FOREIGN_KEYS,
                                                           (state) ? ON : OFF));
        }

        /// <summary>
        /// Create database and tables
        /// </summary>
        /// <returns>task</returns>
        private async Task CreateDatabase()
        {
            await ExecuteScript(FILE_PATH_DATABASE_SCRIPT);
        }

        /// <summary>
        /// Executes all commands in a script file
        /// </summary>
        /// <param name="filePath">file path of the script; assumed to be a resource file</param>
        /// <returns>task</returns>
        public async Task ExecuteScript(string filePath)
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
                        // Just ignore the error since table/row already exists
                    }
                }
            }
        }

        #region Printer Table Operations

        /// <summary>
        /// Insert an item into Printer table
        /// </summary>
        /// <param name="printer">printer to be added</param>
        /// <returns>task; true when successful, false otherwise</returns>
        public async Task<bool> InsertPrinter(Printer printer)
        {
            bool result = true;

            if (printer == null)
            {
                return result;
            }

            try
            {
                await _dbConnection.InsertAsync(printer);
            }
            catch
            {
                result = false;
            }

            return result;
        }

        /// <summary>
        /// Retrieves all print jobs
        /// </summary>
        /// <returns>task; list of printers when successful</returns>
        public async Task<List<Printer>> GetPrinters()
        {
            List<Printer> printerList = new List<Printer>();

            try
            {
                printerList = await (_dbConnection.Table<Printer>().ToListAsync());
            }
            catch
            {
                //printerList = null;
                // Ignore error
            }

            return printerList;
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
        /// <returns>task; true when successful, false otherwise</returns>
        public async Task<bool> DeletePrinter(Printer printer)
        {
            bool result = true;

            if (printer == null)
            {
                return result; // No DB error since no item is deleted
            }

            try
            {
                await _dbConnection.DeleteAsync(printer);
            }
            catch
            {
                result = false;
            }

            return result;
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

        #endregion Printer Table Operations

        #region DefaultPrinter Table Operations

        /// <summary>
        /// Retrieves the default printer
        /// </summary>
        /// <returns>task; DefaultPrinter object when found,
        /// DefaultPrinter object with printer ID is 0 when no default printer set</returns>
        public async Task<DefaultPrinter> GetDefaultPrinter()
        {
            DefaultPrinter defaultPrinter = new DefaultPrinter(); // Unset DefaultPrinter

            try
            {
                int count = await _dbConnection.Table<DefaultPrinter>().CountAsync();
                if (count > 0)
                {
                    defaultPrinter = await _dbConnection.Table<DefaultPrinter>().FirstAsync();
                }
            }
            catch
            {
                //defaultPrinter = null;
                // Ignore error
            }

            return defaultPrinter;
        }

        /// <summary>
        /// Sets the default printer in the database
        /// </summary>
        /// <param name="printerId">printer ID</param>
        /// <returns>task; true when successful, false otherwise</returns>
        public async Task<bool> SetDefaultPrinter(int printerId)
        {
            bool result = true;

            try
            {
                DefaultPrinter existingDefault = await _dbConnection.Table<DefaultPrinter>()
                                                                    .FirstOrDefaultAsync();
                if (existingDefault != null)
                {
                    result = await DeleteDefaultPrinter();
                }

                int count = await _dbConnection.Table<DefaultPrinter>().CountAsync();

                if (result)
                {
                    DefaultPrinter newDefaultPrinter = new DefaultPrinter();
                    newDefaultPrinter.PrinterId = (uint)printerId;
                    await _dbConnection.InsertAsync(newDefaultPrinter);
                }
            }
            catch
            {
                result = false;
            }

            return result;
        }

        /// <summary>
        /// Deletes the existing default printer
        /// </summary>
        /// <returns>task; true when successful</returns>
        public async Task<bool> DeleteDefaultPrinter()
        {
            bool result = true;

            try
            {
                DefaultPrinter existingDefault = await _dbConnection.Table<DefaultPrinter>()
                                                                    .FirstOrDefaultAsync();
                if (existingDefault != null)
                {
                    await _dbConnection.DeleteAsync(existingDefault);
                }
            }
            catch
            {
                //result = false;
                // TODO: Handle error
            }

            return result;
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
            try
            {
                return await _dbConnection.GetAsync<PrintSettings>(id);
            }
            catch
            {
                // Do nothing
            }

            return null;
        }

        /// <summary>
        /// Insert an item into PrintSetting table
        /// </summary>
        /// <param name="printSettings">print settings to be added</param>
        /// <returns>task; true when successful, false otherwise</returns>
        public async Task<bool> InsertPrintSettings(PrintSettings printSettings)
        {
            bool result = true;

            if (printSettings == null)
            {
                return result;
            }

            try
            {
                await _dbConnection.InsertAsync(printSettings);
            }
            catch
            {
                result = false;
            }

            return result;
        }

        /// <summary>
        /// Updates a print setting in the database
        /// </summary>
        /// <param name="printSettings">print settings</param>
        /// <returns>task; true when successful, false otherwise</returns>
        public async Task<bool> UpdatePrintSettings(PrintSettings printSettings)
        {
            bool result = true;

            if (printSettings == null)
            {
                return result;
            }

            try
            {
                await _dbConnection.UpdateAsync(printSettings);
            }
            catch
            {
                result = false;
            }

            return result;
        }

        ///// <summary>
        ///// Deletes a print setting in the database
        ///// </summary>
        ///// <param name="printSettings">print settings</param>
        ///// <returns>task; true when successful, false otherwise</returns>
        //public async Task<bool> DeletePrintSettings(PrintSettings printSettings)
        //{
        //    bool result = true;

        //    if (printSettings == null)
        //    {
        //        return result;
        //    }

        //    try
        //    {
        //        await _dbConnection.DeleteAsync(printSettings);
        //    }
        //    catch
        //    {
        //        result = false;
        //    }

        //    return result;
        //}

        #endregion PrintSetting Table Operations

        #region PrintJob Table Operations

        /// <summary>
        /// Retrieves all print jobs
        /// </summary>
        /// <returns>task; list of all print jobs when successful</returns>
        public async Task<List<PrintJob>> GetPrintJobs()
        {
            List<PrintJob> printJobsList = new List<PrintJob>();

            try
            {
                printJobsList = await _dbConnection.Table<PrintJob>().ToListAsync();
            }
            catch
            {
                //printJobsList = null;
                // Ignore error
            }

            return printJobsList;
        }

        /// <summary>
        /// Insert an item into PrintJob table
        /// </summary>
        /// <param name="printJob">print job to be added</param>
        /// <returns>task; true if successful, false otherwise</returns>
        public async Task<bool> InsertPrintJob(PrintJob printJob)
        {
            bool result = true;

            if (printJob == null)
            {
                return result;
            }

            try
            {
                await _dbConnection.InsertAsync(printJob);
            }
            catch
            {
                result = false;
            }

            return result;
        }

        ///// <summary>
        ///// Updates a print job in the database
        ///// </summary>
        ///// <param name="printJob">print job to be updated</param>
        ///// <returns>task; true when successful, false otherwise</returns>
        //public async Task<bool> UpdatePrintJob(PrintJob printJob)
        //{
        //    bool result = true;

        //    if (printJob == null)
        //    {
        //        return result;
        //    }

        //    try
        //    {
        //        await _dbConnection.UpdateAsync(printJob);
        //    }
        //    catch
        //    {
        //        result = false;
        //    }

        //    return result;
        //}

        /// <summary>
        /// Deletes a print job from the database
        /// </summary>
        /// <param name="printJob">print job to be deleted</param>
        /// <returns>task; true when successful, false otherwise</returns>
        public async Task<bool> DeletePrintJob(PrintJob printJob)
        {
            bool result = true;

            if (printJob == null)
            {
                return result; // No DB error since no item is deleted
            }

            try
            {
                await _dbConnection.DeleteAsync(printJob);
            }
            catch
            {
                result = false;
            }

            return result;
        }

        #endregion PrintJob Table Operations

    }
}
