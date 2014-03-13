using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartDeviceApp.Models;
using Windows.System.Threading;
using System.Collections.ObjectModel;

namespace SmartDeviceApp.Controllers
{
    public class PrinterController
    {
        ThreadPoolTimer periodicTimer;
        private ObservableCollection<Printer> _printerList = new ObservableCollection<Printer>();
        private ObservableCollection<Printer> _printerListTemp = new ObservableCollection<Printer>();
        private bool waitingForPrinterStatus;
        public ObservableCollection<Printer> PrinterList
        {
            get 
            {
                return this._printerList;
            }
        }

        SNMPController snmpController = new SNMPController();


        public PrinterController()
        {
        }

        //request saved printers
        public async void getPrintersFromDB()
        {
            //fetch list from DB using await
            //printerList = await App.db.getPrinters();

        }

        public async Task<ObservableCollection<Printer>> populatePrintersScreen()
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
                //populate printers screen TODO

                _printerListTemp = _printerList;
                //start polling TODO
                //call snmp
                //create callback when snmp gets the results
                updateStatus();
                updateStatus2();
                //startPolling();
            }

            

            return _printerList;

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

            //update Printer Screen TODO
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

            //update Printer Screen TODO
        }


        public void startPolling()
        {
            int period = 500;
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
            foreach (Printer printer in _printerList)
            {
                //request for eachs printer's printer status
                System.Diagnostics.Debug.WriteLine(printer.Ip_address);
                snmpController.printerControllerGetStatusCallback = new Action<string, bool>(handlePrinterStatus);
                waitingForPrinterStatus = true;
                snmpController.getStatus(printer.Ip_address);

            }
        }

        private void updateStatus2()
        {
            //_printerList = _printerListTemp;
            
        }

        private async void handlePrinterStatus(string ip, bool isOnline)
        {

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                //find the printer
                Printer printer = _printerListTemp.Where(x => x.Ip_address == ip).First();
                System.Diagnostics.Debug.WriteLine(printer.Ip_address);
                int index = _printerListTemp.IndexOf(printer);

                //update status
                printer.isOnline = isOnline;

                //update list
                _printerListTemp.Insert(index, printer);
                waitingForPrinterStatus = false;
                System.Diagnostics.Debug.WriteLine(index);
                System.Diagnostics.Debug.WriteLine(isOnline);
            });
        }


        public void getStatus(ThreadPoolTimer timer)
        {
            //call SNMP Controller get status here
            updateStatus();
            
        }




        

    }
}
