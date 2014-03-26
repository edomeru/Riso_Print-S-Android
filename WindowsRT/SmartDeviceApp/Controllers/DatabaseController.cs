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

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SQLite;
using SmartDeviceApp.Models;
using Windows.Storage;

namespace SmartDeviceApp.Controllers
{
    public class DatabaseController
    {
        static readonly DatabaseController _instance = new DatabaseController();

        private const string FILE_NAME_DATABASE = "SmartDeviceAppDB.db";
        private const string FILE_PATH_DATABASE_SCRIPT = "Assets/SmartDeviceAppDB.sql";

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static DatabaseController() { }

        private DatabaseController() { }

        public static DatabaseController Instance
        {
            get { return _instance; }
        }

        // public async Task Initialize() // Used for create database from script
        public void Initialize()
        {
            // await CreateDatabase();  // Used for create database from script
            CreateDatabase();
        }

        // private async Task CreateDatabase() // Used for create database from script
        private void CreateDatabase()
        {
            try
            {
                var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path,
                    FILE_NAME_DATABASE);
                using (var db = new SQLite.SQLiteConnection(dbpath))
                {
                    #region Create Tables Using Script File
                    /*
                    // Read script from Assets and create tables
                    string dbScriptPath = Path.Combine(Package.Current.InstalledLocation.Path,
                        FILE_PATH_DATABASE_SCRIPT);
                    StorageFile file =await
                        StorageFile.GetFileFromApplicationUriAsync(new Uri(dbScriptPath));
                    string script = await FileIO.ReadTextAsync(file);
                        
                    // Loop each commands
                    string[] commands = script.Split(new char[]{';'},
                        StringSplitOptions.RemoveEmptyEntries);
                    foreach(string command in commands)
                    {
                        try
                        {
                            // Since each parameter in the script is in each line,
                            // convert them into a single line statement
                            db.Execute(command.Replace("\r\n", " ").Trim());
                        }
                        catch (SQLiteException)
                        {
                            // Table already exists
                            // Coninue execution just to create other empty tables
                        }
                    }
                    */
                    #endregion Create Create Tables Using Script File

                    /*
                    // DeleteAll is for testing purposes only PrintersModule
                    db.DeleteAll<Printer>();
                    db.Commit();

                    db.DeleteAll<DefaultPrinter>();
                    db.Commit();
                    */

                    #region Create Tables Using Model Classes

                    // Create the tables if they don't exist

                    //Printer table
                    db.CreateTable<Printer>();
                    db.Commit();

                    //DefaultPrinter Table
                    db.CreateTable<DefaultPrinter>();
                    db.Commit();

                    //PrintSettings Table
                    db.CreateTable<PagePrintSetting>();
                    db.Commit();

                    //PrintJob Table
                    db.CreateTable<PrintJob>();
                    db.Commit();

                    db.Dispose();
                    db.Close();

                    #endregion Create Tables Using Model Classes

                    // insertPrinters(); //for testing PrintersModule
                }
            }
            catch
            {
                // Error in creating tables
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
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            using (var db = new SQLite.SQLiteConnection(dbpath))
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
                var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);

                var db = new SQLite.SQLiteAsyncConnection(dbpath);

                printerList = await (db.Table<Printer>().ToListAsync());
            }
            catch
            {
            }

            return printerList;
        }

        public async Task<int> SetDefaultPrinter(int printerId)
        {
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            var db = new SQLite.SQLiteAsyncConnection(dbpath);
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
                        existingDefault.PrinterId = printerId;
                    }
                    else
                    {
                        // no default printer, insert new
                        DefaultPrinter dp = new DefaultPrinter();
                        dp.PrinterId = printerId;

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
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            var db = new SQLite.SQLiteAsyncConnection(dbpath);

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

        public async Task<Printer> GetDefaultPrinter()
        {
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            var db = new SQLite.SQLiteAsyncConnection(dbpath);
            DefaultPrinter defaultPrinter = new DefaultPrinter();
            Printer printer = new Printer();
            try
            {
                int defaultPrinterCount = await db.Table<DefaultPrinter>().CountAsync();
                if (defaultPrinterCount > 0)
                {
                    defaultPrinter = await (db.Table<DefaultPrinter>().FirstAsync());
                    printer = await (db.GetAsync<Printer>(defaultPrinter.PrinterId));
                    printer.IsDefault = true;
                }
            }
            catch
            {
                // Error handling here
            }
            return printer;
        }

        #endregion Printer Table Operations

        #region PrintSetting Table Operations

        public async Task<PagePrintSetting> GetPrintSetting(int printerId)
        {
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            var db = new SQLite.SQLiteAsyncConnection(dbpath);
            PagePrintSetting printSetting = null;

            try
            {
                printSetting = await db.GetAsync<PagePrintSetting>(printerId);
            }
            catch
            {
                // Error handling here
            }
            return printSetting;
        }

        #endregion PrintSetting Table Operations

    }
}
