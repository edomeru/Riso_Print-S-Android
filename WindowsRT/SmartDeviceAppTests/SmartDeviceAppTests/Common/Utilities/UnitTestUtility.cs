//
//  UnitTestUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/05/13.
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
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Storage;

namespace SmartDeviceAppTests.Common.Utilities
{
    public static class UnitTestUtility
    {

        /// <summary>
        /// Executes all commands in a script file
        /// </summary>
        /// <param name="filePath">file path of the script; assumed to be a resource file</param>
        /// <param name="dbConnection">connection</param>
        /// <returns></returns>
        public static async Task ExecuteScript(string filePath, SQLiteAsyncConnection dbConnection)
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
                            await dbConnection.ExecuteAsync(sqlStatement);
                        }
                    }
                    catch (SQLiteException)
                    {
                        // Error handling
                    }
                }
            }
        }

        /// <summary>
        /// Deletes all (known) tables in the database
        /// </summary>
        /// <returns></returns>
        public static async Task DropAllTables(SQLiteAsyncConnection dbConnection)
        {
            try
            {
                await dbConnection.DropTableAsync<Printer>();
                await dbConnection.DropTableAsync<PrintSetting>();
                await dbConnection.DropTableAsync<PrintJob>();
                await dbConnection.DropTableAsync<DefaultPrinter>();
            }
            catch (SQLiteException)
            {
                // Error handling
            }
        }

        /// <summary>
        /// Checks is a file exists
        /// </summary>
        /// <param name="keyword">part of file name</param>
        /// <param name="folderLocation">target location</param>
        /// <returns>task; true if a file is found, false otherwise</returns>
        public static async Task<bool> CheckIfFileExists(string keyword, StorageFolder folderLocation)
        {
            var files = await folderLocation.GetFilesAsync();
            foreach (var file in files)
            {
                if (file.Name.Contains(keyword))
                {
                    return true;
                }
            }
            return false;
        }

    }
}
