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
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Controllers
{
    public class PrinterController : INotifyPropertyChanged
    {

        static readonly PrinterController _instance = new PrinterController();


        public delegate bool AddPrinterHandler(string ip);
        private AddPrinterHandler _addPrinterHandler;

        public delegate void SearchPrinterHandler();
        private SearchPrinterHandler _searchPrinterHandler;

        public delegate void SearchPrinterTimeoutHandler(string ipAddress);
        private SearchPrinterTimeoutHandler _searchPrinterTimeoutHandler;

        public delegate Task<bool> DeletePrinterHandler(string ipAddress);
        private DeletePrinterHandler _deletePrinterHandler;

        ThreadPoolTimer periodicTimer;

        private ObservableCollection<Printer> _printerList = new ObservableCollection<Printer>();
        private ObservableCollection<Printer> _printerListTemp = new ObservableCollection<Printer>();
        private ObservableCollection<PrinterSearchItem> _printerSearchList = new ObservableCollection<PrinterSearchItem>();

        private bool waitingForPrinterStatus;

        private PrintersViewModel _printersViewModel;
        private SearchPrinterViewModel _searchPrinterViewModel;
        private AddPrinterViewModel _addPrinterViewModel;

        SNMPController snmpController = new SNMPController();


        static PrinterController() { }

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static PrinterController Instance
        {
            get { return _instance; }
        }


        
        public async Task Initialize()
        {
            _printersViewModel = new ViewModelLocator().PrintersViewModel;
            _searchPrinterViewModel = new ViewModelLocator().SearchPrinterViewModel;
            _addPrinterViewModel = new ViewModelLocator().AddPrinterViewModel;

            _addPrinterHandler = new AddPrinterHandler(addPrinter);
            _searchPrinterHandler = new SearchPrinterHandler(searchPrinters);
            _deletePrinterHandler = new DeletePrinterHandler(deletePrinter);
            _searchPrinterTimeoutHandler = new SearchPrinterTimeoutHandler(handleSearchTimeout);

            _printersViewModel.DeletePrinterHandler += _deletePrinterHandler;
            

            populatePrintersScreen();
            _printersViewModel.PrinterList = PrinterList;
            

            _searchPrinterViewModel.AddPrinterHandler += _addPrinterHandler;
            _searchPrinterViewModel.SearchPrinterHandler += _searchPrinterHandler;
            _searchPrinterViewModel.PrinterSearchList = PrinterSearchList;
            

            _addPrinterViewModel.AddPrinterHandler += _addPrinterHandler;

            
        }


        public ObservableCollection<Printer> PrinterList
        {
            get { return this._printerList; }
            set
            {
                _printerList = value;
                PropertyChanged(this, new PropertyChangedEventArgs("PrinterList"));
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
            System.Diagnostics.Debug.WriteLine("PASOK");
            //get printers from db
            int indexOfDefaultPrinter = 0;
            var printerListFromDB = await DatabaseController.Instance.GetPrinters();
            var defaultPrinter = await DatabaseController.Instance.GetDefaultPrinter();

            foreach (var printerFromDB in printerListFromDB)
            {
                //check
                //if default printer

                if (defaultPrinter.PrinterId == printerFromDB.Id)
                {
                    printerFromDB.IsDefault = true;
                    indexOfDefaultPrinter = _printerList.Count();
                    //printerFromDB.PropertyChanged += handlePropertyChanged;
                }
                else
                {
                    printerFromDB.IsDefault = false;
                }
                printerFromDB.IsOnline = false;
                
                _printerList.Add(printerFromDB);
                
            }

            if (_printerList.Count > 0)
            {

                foreach (Printer p in PrinterList)
                {
                    p.PropertyChanged += new PropertyChangedEventHandler(handlePropertyChanged);
                }
                //sort printerlist
                sortPrinterList(indexOfDefaultPrinter);
                _printerListTemp = _printerList;
               
                await updateStatus();
            }
        }

        private void sortPrinterList(int index)
        {
            Printer defaultPrinter = _printerList.ElementAt(index);

            _printerList.RemoveAt(index);
            _printerList.Insert(0, defaultPrinter);
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

        private async Task updateStatus()
        {
            //foreach (Printer printer in _printerListTemp)
            //{

                int i = 0;
                do
                {
                    Printer printer = _printerListTemp.ElementAt(i);
                    //request for eachs printer's printer status
                    System.Diagnostics.Debug.WriteLine(printer.IpAddress);

                    NetworkController.Instance.networkControllerPingStatusCallback = new Action<string, bool>(handlePrinterStatus);
                    await NetworkController.Instance.pingDevice(printer.IpAddress);

                    i++;
                }
                while (i < _printerListTemp.Count);

            //}
        }

        private async void handlePrinterStatus(string ip, bool isOnline)
        {

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                //find the printer TODO: error handling here when the printer is deleted while handling status
                try{
                    Printer printer = _printerList.First(x => x.IpAddress == ip);
                    System.Diagnostics.Debug.WriteLine(printer.IpAddress);
                    int index = _printerList.IndexOf(printer);

                    //update status
                    printer.IsOnline = isOnline;

                    System.Diagnostics.Debug.WriteLine(index);
                    System.Diagnostics.Debug.WriteLine(isOnline);
                }
                catch(Exception e)
                {
                    
                }
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
                printer.IsOnline = !printer.IsOnline;
            }
        }



        /**
         * 
         * Add Printer Functions
         * 
         * */

        public bool addPrinter(string ip)
        {
            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
            //check if valid ip address
            if (!isValidIpAddress(ip))
            {
                //display error theat ip is invalid
                _addPrinterViewModel.DisplayMessage(loader.GetString("IDS_LBL_ADD_PRINTER"), loader.GetString("IDS_ERR_MSG_INVALID_IP_ADDRESS"));
                
                return false;
            }

            //check if _printerList is already full
            if (_printerList.Count() >= 10)//TODO: Change to CONSTANTS
            {
                _addPrinterViewModel.DisplayMessage(loader.GetString("IDS_LBL_ADD_PRINTER"), loader.GetString("IDS_ERR_MSG_MAX_PRINTER_COUNT "));
                return false;
            }


            //check if already in _printerList
            Printer printer = null;
            try {
                printer = _printerList.First(x => x.IpAddress == ip);
                }
            catch {
                if (printer != null)
                {
                    _addPrinterViewModel.DisplayMessage(loader.GetString("IDS_LBL_ADD_PRINTER"), loader.GetString("IDS_ERR_MSG_CANNOT_ADD_PRINTER"));
                    return false;
                }

                //check if online
                //get MIB
                snmpController.printerControllerTimeout = new Action<string>(handleAddTimeout);
                snmpController.printerControllerAddPrinterCallback = handleAddPrinterStatus;
                snmpController.getDevice(ip);

                
            }
            if (printer != null)
            {
                //cannot add, printer already in list
                _addPrinterViewModel.DisplayMessage(loader.GetString("IDS_LBL_ADD_PRINTER"), loader.GetString("IDS_ERR_MSG_CANNOT_ADD_PRINTER"));
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

        public async void handleAddPrinterStatus(string ip, string name, bool isOnline)
        {
            
                //add to printerList
            //    Printer printer = new Printer(-1, -1, ip, name, -1,
            //false, false, false, false,
            //false, false, false);

                Printer printer = new Printer() { IpAddress = ip, Name = name};
                //printer.IpAddress = ip;
                //printer.Name = name;

                try
                {
                    //insert to database
                    int id = await DatabaseController.Instance.InsertPrinter(printer);
                    if (id < 0)
                    {
                        return;
                    }
                

                printer.Id = id;
                printer.IsOnline = true;
                }
                catch (Exception e)
                {
                    //add error message TODO
                    return;
                }
                //printer.IsDefault = true; //for testing
                
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {

                printer.PropertyChanged += handlePropertyChanged;
                _printerList.Add(printer);


                _printerListTemp = _printerList;
                _addPrinterViewModel.handleAddIsSuccessful(true);
                
            });
        }

        private async void handlePropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            Printer printer = (Printer) sender;
            if (e.PropertyName == "IsDefault")
            {
                //change default printer in list and in db
                if (printer.IsDefault == true)
                {
                    await DatabaseController.Instance.SetDefaultPrinter(printer.Id);

                    foreach (var printerInList in PrinterList)
                    {
                        
                        if (printerInList.IpAddress == printer.IpAddress)
                        {
                            if (printerInList.IsDefault != true)
                                printerInList.IsDefault = true;
                        }
                        else
                        {
                            printerInList.IsDefault = false;
                        }       
                    }

                    //sortPrinterList(index); TBA
                
                //System.Diagnostics.Debug.WriteLine(e.PropertyName);

                //System.Diagnostics.Debug.WriteLine(printer.IpAddress);
                //System.Diagnostics.Debug.WriteLine(printer.Name);
                //System.Diagnostics.Debug.WriteLine(printer.IsDefault);
                }

            }
            if (e.PropertyName == "WillPerformDelete")
            {
                if (printer.WillPerformDelete == true)
                {
                    await deletePrinter(printer.IpAddress);
                }
            }

            if (e.PropertyName == "WillBeDeleted" && printer.WillBeDeleted == true)
            {
                foreach(var printerInList in PrinterList)
                {
                    if (printer.IpAddress == printerInList.IpAddress)
                    {
                        if (printerInList.WillBeDeleted != true)
                        {
                            printerInList.WillBeDeleted = true;
                        }
                    }
                    else
                    {
                        printerInList.WillBeDeleted = false;
                    }
                 }
                
            }
            //throw new NotImplementedException();
        }

        private async void handleAddTimeout(string ip)
        {


            //add to printerList

            Printer printer = new Printer() { IpAddress = ip, Name = "No name"};
                

                //insert to database
                int id = await DatabaseController.Instance.InsertPrinter(printer);
                if (id < 0)
                {
                    return;
                }

                printer.Id = id;
                printer.IsOnline = false;
                //printer.IsDefault = true; //for testing
                
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                printer.PropertyChanged += handlePropertyChanged;
                _printerList.Add(printer);

                _printerListTemp = _printerList;


                _addPrinterViewModel.handleAddIsSuccessful(false);
                });
        }


        /**
         * Scan Functions
         * 
         * */
        public void searchPrinters()
        {
            _searchPrinterViewModel.SetStateRefreshState();
            _printerSearchList.Clear();
            snmpController.printerControllerTimeout = new Action<string>(handleSearchTimeout);
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
                        printerInList = _printerList.First(x => x.IpAddress == printer.Ip_address);
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

        public async Task<bool> deletePrinter(string ipAddress)
        {
            Printer printer =  _printerList.First(x => x.IpAddress == ipAddress);

            int result = await DatabaseController.Instance.DeletePrinterFromDB(printer.Id);

            if (result > 0)
            {
                return false;
            }
            int index = _printerList.IndexOf(printer);

            _printerList.RemoveAt(index);

            _printerListTemp = _printerList;

            return true;
        }

        private void handleSearchTimeout(string ip)
        {
            _searchPrinterViewModel.SearchTimeout();
            
        }


    }
}
