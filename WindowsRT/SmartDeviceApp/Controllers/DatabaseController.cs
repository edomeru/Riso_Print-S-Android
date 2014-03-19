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

        private const string sdaDatabase = "SmartDeviceAppDB.db";

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static DatabaseController() { }

        private DatabaseController() { }

        public static DatabaseController Instance
        {
            get { return _instance; }
        }

        public void Initialize()
        {
            CreateDatabase();
        }

        private void CreateDatabase()
        {
            try
            {
                var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
                using (var db = new SQLite.SQLiteConnection(dbpath))
                {
                    /*
                    // DeleteAll is for testing purposes only PrintersModule
                    db.DeleteAll<Printer>();
                    db.Commit();

                    db.DeleteAll<DefaultPrinter>();
                    db.Commit();
                    */

                    // Create the tables if they don't exist

                    //Printer table
                    db.CreateTable<Printer>();
                    db.Commit();

                    //DefaultPrinter Table
                    db.CreateTable<DefaultPrinter>();
                    db.Commit();

                    //PrintSettings Table
                    db.CreateTable<PrintSetting>();
                    db.Commit();

                    //PrintJob Table
                    db.CreateTable<PrintJob>();
                    db.Commit();

                    db.Dispose();
                    db.Close();

                    //insertPrinters(); //for testing PrintersModule
                }
            }
            catch
            {

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

            var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
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
         * */

        private void InsertPrinter(Printer printer)
        {
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, sdaDatabase);
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
                var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, sdaDatabase);

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
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, sdaDatabase);
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
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, sdaDatabase);
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
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, sdaDatabase);
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

        public async Task<PrintSetting> GetPrintSetting(int printerId)
        {
            var dbpath = Path.Combine(ApplicationData.Current.LocalFolder.Path, sdaDatabase);
            var db = new SQLite.SQLiteAsyncConnection(dbpath);
            PrintSetting printSetting = null;

            try
            {
                printSetting = await db.GetAsync<PrintSetting>(printerId);
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
