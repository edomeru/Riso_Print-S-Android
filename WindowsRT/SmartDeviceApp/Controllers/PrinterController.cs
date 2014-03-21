using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartDeviceApp.Models;
using Windows.System.Threading;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using Windows.Networking;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Controllers
{
    public class PrinterController
    {
        ThreadPoolTimer periodicTimer;

        private ObservableCollection<Printer> _printerList = new ObservableCollection<Printer>();
        private ObservableCollection<Printer> _printerListTemp = new ObservableCollection<Printer>();
        private ObservableCollection<PrinterSearchItem> _printerSearchList = new ObservableCollection<PrinterSearchItem>();

        private bool waitingForPrinterStatus;

        SNMPController snmpController = new SNMPController();

        public ObservableCollection<Printer> PrinterList
        {
            get{ return this._printerList; }
            set
            {
                _printerList = value;

                PropertyChanged(this, new PropertyChangedEventArgs("_isOnline"));
            }
        }

        public ObservableCollection<PrinterSearchItem> PrinterSearchList
        {
            get{ return this._printerSearchList;}
            set
            {
                _printerSearchList = value;

                PropertyChanged(this, new PropertyChangedEventArgs("PrinterSearchList"));
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        public PrinterController()
        {
        }

        public async void populatePrintersScreen()
        {
            //get printers from db
            int indexOfDefaultPrinter = 0;
            var printerListFromDB = await App.db.getPrinters();
            var defaultPrinter = await App.db.getDefaultPrinter();

            foreach (var printerFromDB in printerListFromDB)
            {

                Printer printer = new Printer();
                printer.Id = printerFromDB.prn_id;
                printer.Ip_address = printerFromDB.prn_ip_address;
                printer.Name = printerFromDB.prn_name;
                printer.Enabled_LPR = printerFromDB.prn_enabled_lpr;
                printer.Enabled_Raw = printerFromDB.prn_enabled_raw;
                printer.Enabled_Pagination = printerFromDB.prn_enabled_pagination;
                printer.Enabled_BooketBinding = printerFromDB.prn_enabled_booklet_binding;
                printer.Enabled_Duplex = printerFromDB.prn_enabled_duplex;
                printer.Enabled_Staple = printerFromDB.prn_enabled_staple;
                printer.Enabled_Bind = printerFromDB.prn_enabled_bind;


                //check
                //if default printer
                
                if (defaultPrinter.prn_id == printer.Id)
                {
                    printer.isDefaultPrinter = true;
                    indexOfDefaultPrinter = _printerList.Count();
                }
                else
                {
                    printer.isDefaultPrinter = false;
                }
                _printerList.Add(printer);
            }

            if (_printerList.Count > 0)
            {
                //sort printerlist
                sortPrinterList(indexOfDefaultPrinter);
                _printerListTemp = _printerList;
                //start polling TODO
                //call snmp
                updateStatus();
                startPolling();
            }
        }

        public async void updateDefaultPrinter(int index)
        {
            int printerId = _printerList.ElementAt(index).Id;

            int result = await App.db.setDefaultPrinter(printerId);

            if (result != 0)
            {
                //error
            }

            //update list
            sortPrinterList(index);
        }

        private void sortPrinterList(int index)
        {
            Printer defaultPrinter = _printerList.ElementAt(index);

            _printerList.RemoveAt(index);
            _printerList.Insert(0, defaultPrinter);
        }

        public async void deletePrinterFromList(int index)
        {
            int printerId = _printerList.ElementAt(index).Id;

            int result = await App.db.deletePrinterFromDB(printerId);

            if (result != 0)
            {
                //error
            }

            _printerList.RemoveAt(index);
        }


        public void startPolling()
        {
            int period = 5000;
            var timerHandler = new TimerElapsedHandler(getStatus);

            periodicTimer = ThreadPoolTimer.CreatePeriodicTimer(timerHandler, TimeSpan.FromMilliseconds(period));
        }

        public void endPolling()
        {
            if (periodicTimer != null)
            {
                periodicTimer.Cancel();
            }
        }

        private void updateStatus()
        {
            foreach (Printer printer in _printerListTemp)
            {
                //request for eachs printer's printer status
                System.Diagnostics.Debug.WriteLine(printer.Ip_address);
                snmpController.printerControllerGetStatusCallback = new Action<string, bool>(handlePrinterStatus);
                waitingForPrinterStatus = true;
                snmpController.getStatus(printer.Ip_address);

            }
        }

        private async void handlePrinterStatus(string ip, bool isOnline)
        {

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                //find the printer
                Printer printer = _printerList.First(x => x.Ip_address == ip);
                System.Diagnostics.Debug.WriteLine(printer.Ip_address);
                int index = _printerList.IndexOf(printer);

                //update status
                printer.isOnline = isOnline;

                System.Diagnostics.Debug.WriteLine(index);
                System.Diagnostics.Debug.WriteLine(isOnline);
            });
        }


        public void getStatus(ThreadPoolTimer timer)
        {
            //call SNMP Controller get status here
            updateStatus();
            
        }

        public void updateOnlineStatus()
        {
            foreach (Printer printer in _printerList)
            {
                printer.isOnline = !printer.isOnline;
            }
        }



        /**
         * 
         * Add Printer Functions
         * 
         * */

        public bool addPrinter(string ip)
        {
            //check if valid ip address
            if (!isValidIpAddress(ip))
            {
                //display error theat ip is invalid
                return false;
            }

            //check if already in _printerList
            Printer printer = null;
            try { 
            printer = _printerList.First(x => x.Ip_address == ip);
                }
            catch {
                if (printer != null)
                {
                    //cannot add, printer already in list
                    return false;
                }

                //check if online
                //get MIB
                snmpController.printerControllerAddPrinterCallback = handleAddPrinterStatus;
                snmpController.getCapability(ip);

                
            }
            if (printer != null)
            {
                //cannot add, printer already in list
                return false;
            }

            

            return true;

        }

        public bool isValidIpAddress(string ip)
        {
            //  Split string by ".", check that array length is 3
            char chrFullStop = '.';
            string[] arrOctets = ip.Split(chrFullStop);
            if (arrOctets.Length != 4)
            {
                return false;
            }
            //  Check each substring checking that the int value is less than 255 and that is char[] length is !> 2
            Int16 MAXVALUE = 255;
            Int32 temp; // Parse returns Int32
            foreach (String strOctet in arrOctets)
            {
                if (strOctet.Length > 3)
                {
                    return false;
                }

                temp = int.Parse(strOctet);
                if (temp > MAXVALUE)
                {
                    return false;
                }
            }

            return true;
        }

        public async void handleAddPrinterStatus(string ip, string name, bool isOnline, List<string> capabilities)
        {
            if (isOnline)
            {
                //add to _printerList
                DatabaseController.Printer printerDB = new DatabaseController.Printer()
                {
                    prn_ip_address = ip,
                    //Temp data
                    prn_name = name,
                    prn_port_setting  = 1,
                    prn_enabled_lpr  = true,
                    prn_enabled_raw  = true, 
                    prn_enabled_pagination  = true,
                    prn_enabled_duplex  = true, 
                    prn_enabled_booklet_binding  = true,
                    prn_enabled_staple = true,
                    prn_enabled_bind = true 
                };
                //insert to database
                int id = await App.db.insertPrinter(printerDB);
                if (id < 0)
                {
                    return;
                }

                //add to printerList
                Printer printer = new Printer()
               {
                   Id = id,
                   Ip_address = printerDB.prn_ip_address,
                   Name = printerDB.prn_name,
                   Enabled_LPR = printerDB.prn_enabled_lpr,
                   Enabled_Raw = printerDB.prn_enabled_raw,
                   Enabled_Pagination = printerDB.prn_enabled_pagination,
                   Enabled_BooketBinding = printerDB.prn_enabled_booklet_binding,
                   Enabled_Duplex = printerDB.prn_enabled_duplex,
                   Enabled_Staple = printerDB.prn_enabled_staple,
                   Enabled_Bind = printerDB.prn_enabled_bind,
                   isDefaultPrinter = false,
                    isOnline = true
               };
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {

                _printerList.Add(printer);

                _printerListTemp = _printerList;
            });
                

            }
            else
            {
                //display error cannot add printer
            }
        }


        /**
         * Scan Functions
         * 
         * */
        public void scanPrinters()
        {
            _printerSearchList.Clear();
            snmpController.printerControllerDiscoverCallback = new Action<PrinterSearchItem>(handleDeviceDiscovered);
            snmpController.startDiscover();
        }

        private async void handleDeviceDiscovered(PrinterSearchItem printer)
        {
            Printer printerInList = null;
            
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    _printerSearchList.Add(printer);
                    try
                    {
                        printerInList = _printerList.First(x => x.Ip_address == printer.Ip_address);
                    }
                    catch
                    {
                        //not in the list
                        printer.IsInPrinterList = false;

                    }
                    finally
                    {
                        if (printerInList != null) { 
                            printer.IsInPrinterList = true;
                        }
                    }
                });

        }
        


        /**
         * Delete functions
         * 
         * */

        public async Task<bool> deletePrinter(int index)
        {
            Printer printer = _printerList.ElementAt(index);

            int result = await App.db.deletePrinterFromDB(printer.Id);

            if (result > 0)
            {
                return false;
            }


            _printerList.RemoveAt(index);

            _printerListTemp = _printerList;

            return true;
        }


    }
}
