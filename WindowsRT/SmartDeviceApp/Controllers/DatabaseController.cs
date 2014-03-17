using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SQLite;

namespace SmartDeviceApp.Controllers
{
    public class DatabaseController
    {
        static private string sdaDatabase = "SmartDeviceAppDB.db";

        /*
        //DefaultPrinter Table
        public class DefaultPrinter
        {
            [MaxLength(5), PrimaryKey]

            public int prn_id { get; set; }
            
        }

        //Printer Table
        public class Printer
        {
            public int prn_id { get; set; }                //id in database
            [MaxLength(20)]
            public string prn_ip_address { get; set; }     //ip address of the printer
            [MaxLength(255)]
            public string prn_name { get; set; }           //printer name
            public int prn_port_setting { get; set; }
            public bool prn_enabled_lpr { get; set; }
            public bool prn_enabled_raw { get; set; }
            public bool prn_enabled_pagination { get; set; }
            public bool prn_enabled_duplex { get; set; }
            public bool prn_enabled_booklet_binding { get; set; }
            public bool prn_enabled_staple { get; set; }
            public bool prn_enabled_bind { get; set; }
        }

        
       
        //class table for printers
        public DatabaseController()
        {
            createDatabase();
        }


        private void createDatabase(){
            try
            {
                var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
                using (var db = new SQLite.SQLiteConnection(dbpath))
                {
                    // DeleteAll is for testing purposes only PrintersModule
                    db.DeleteAll<Printer>();
                    db.Commit();

                    db.DeleteAll<DefaultPrinter>();
                    db.Commit();
                    // Create the tables if they don't exist
                    //Printer table
                    db.CreateTable<Printer>();
                    db.Commit();

                    //DefaultPrinter Table
                    db.CreateTable<DefaultPrinter>();
                    db.Commit();

                    //PrintSettings Table

                    //PrintJob Table

                    db.Dispose();
                    db.Close();

                    insertPrinters(); //for testing PrintersModule
                }
            }
            catch
            {

            }
        }


        private void insertPrinters()
        {
            Printer printer = new Printer() { prn_id=1, prn_ip_address="192.168.0.22", prn_name="RISO_Printer1", prn_port_setting=1,
                prn_enabled_lpr = true, prn_enabled_raw = true, prn_enabled_pagination = true, prn_enabled_duplex = true,
                                              prn_enabled_booklet_binding = true,
                                              prn_enabled_staple = true,
                                              prn_enabled_bind = true
            };

            Printer printer2 = new Printer()
            {
                prn_id = 2,
                prn_ip_address = "192.168.0.2",
                prn_name = "RISO_Printer2",
                prn_port_setting = 1,
                prn_enabled_lpr = true,
                prn_enabled_raw = true,
                prn_enabled_pagination = true,
                prn_enabled_duplex = true,
                prn_enabled_booklet_binding = true,
                prn_enabled_staple = true,
                prn_enabled_bind = true
            };
            Printer printer3 = new Printer()
            {
                prn_id = 3,
                prn_ip_address = "192.168.0.3",
                prn_name = "RISO_Printer3",
                prn_port_setting = 1,
                prn_enabled_lpr = true,
                prn_enabled_raw = true,
                prn_enabled_pagination = true,
                prn_enabled_duplex = true,
                prn_enabled_booklet_binding = true,
                prn_enabled_staple = true,
                prn_enabled_bind = true
            };

            DefaultPrinter dp = new DefaultPrinter() { prn_id = printer.prn_id };

            var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
            using (var db = new SQLite.SQLiteConnection(dbpath))
            {
                // Create the tables if they don't exist
                db.Insert(printer);
                db.Commit();

                //db.Insert(printer2);
                //db.Commit();

                //db.Insert(printer3);
                //db.Commit();

                db.Insert(dp);
                db.Commit();

                db.Dispose();
                db.Close();
            }
                
         
        }

        public async Task<List<Printer>> getPrinters()
        {
            var printerList = new List<Printer>();
            try
            {
                var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
                
                var db = new SQLite.SQLiteAsyncConnection(dbpath);

                printerList = await (db.Table<Printer>().ToListAsync());

                //var defaultPrinter = await (db.Table<DefaultPrinter>().FirstAsync());
                //foreach (var sd in d)
                //{
                    
                    

                //    printerList.Add(sd);
                //}
            }
            catch
            {
            }

            return printerList;
        }

        public async Task<int> setDefaultPrinter(int printerId)
        {
            //DefaultPrinter existingDefault = new DefaultPrinter();
            
            var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
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
                        existingDefault.prn_id = printerId;

                    }
                    else
                    {
                        // no default printer, insert new
                        DefaultPrinter dp = new DefaultPrinter();
                        dp.prn_id = printerId;

                        int success = await db.InsertAsync(dp);
                    }
                }
                
            }catch
            {
                return 0;
            }
            return 1;
        }

        public async Task<int> deletePrinterFromDB(int printerId)
        {
            var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
            var db = new SQLite.SQLiteAsyncConnection(dbpath);

            try
            {
                //delete in printer table
                var printer = await db.Table<Printer>().Where(
                    p => p.prn_id == printerId).FirstAsync();

                //check if default printer
                var defaultPrinter = await db.Table<DefaultPrinter>().FirstAsync();
            
                if (printer.prn_id == defaultPrinter.prn_id)
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
        

        public async Task<DefaultPrinter> getDefaultPrinter()
        {
            var dbpath = Path.Combine(Windows.Storage.ApplicationData.Current.LocalFolder.Path, sdaDatabase);
            var db = new SQLite.SQLiteAsyncConnection(dbpath);
            DefaultPrinter defaultPrinter = new DefaultPrinter();
            try
            {
                defaultPrinter = await (db.Table<DefaultPrinter>().FirstAsync());
            }
            catch
            {

            }
            return defaultPrinter;
        }
        */

    }
}
