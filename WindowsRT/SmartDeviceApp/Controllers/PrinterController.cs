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
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Constants;

namespace SmartDeviceApp.Controllers
{
    public class PrinterController : INotifyPropertyChanged
    {
        static readonly PrinterController _instance = new PrinterController();
        private PrintSettingsController _printSettingsController;

        public delegate void PollingHandler(bool willPoll);
        private PollingHandler _pollingHandler;

        public delegate bool AddPrinterHandler(string ip);
        private AddPrinterHandler _addPrinterHandler;

        public delegate void SearchPrinterHandler();
        private SearchPrinterHandler _searchPrinterHandler;

        public delegate Task<bool> AddPrinterFromSearchHandler(string ip);
        private AddPrinterFromSearchHandler _addPrinterFromSearchHandler;

        public delegate void SearchPrinterTimeoutHandler(string ipAddress);
        private SearchPrinterTimeoutHandler _searchPrinterTimeoutHandler;

        public delegate Task<bool> DeletePrinterHandler(string ipAddress);
        private DeletePrinterHandler _deletePrinterHandler;

        public delegate void OpenDefaultPrintSettingsHandler(Printer printer);
        private OpenDefaultPrintSettingsHandler _openDefaultPrintSettingsHandler;

        public delegate void OnNavigateToEventHandler();
        private OnNavigateToEventHandler _onNavigateToEventHandler;

        public delegate void OnNavigateFromEventHandler();
        private OnNavigateFromEventHandler _onNavigateFromEventHandler;

        public delegate void DeletePrinterItemsHandler(Printer printer);
        public event DeletePrinterItemsHandler DeletePrinterItemsEventHandler;

        ThreadPoolTimer periodicTimer;

        private ObservableCollection<Printer> _printerList = new ObservableCollection<Printer>();
        private ObservableCollection<Printer> _printerListTemp = new ObservableCollection<Printer>();
        private ObservableCollection<PrinterSearchItem> _printerSearchList = new ObservableCollection<PrinterSearchItem>();

        private PrintersViewModel _printersViewModel;
        private SearchPrinterViewModel _searchPrinterViewModel;
        private AddPrinterViewModel _addPrinterViewModel;
        private PrintSettingsViewModel _printSettingsViewModel;
        private string _screenName;

        private bool isPolling = false;

        //SNMPController snmpController = new SNMPController();


        static PrinterController() { }

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static PrinterController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Initializes add, search, and delete handlers for view models
        /// </summary>
        public async Task Initialize()
        {
            _printersViewModel = new ViewModelLocator().PrintersViewModel;
            _searchPrinterViewModel = new ViewModelLocator().SearchPrinterViewModel;
            _addPrinterViewModel = new ViewModelLocator().AddPrinterViewModel;
            _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;

            _screenName = SmartDeviceApp.Common.Enum.ScreenMode.Printers.ToString();

            _pollingHandler = new PollingHandler(setPolling);
            _addPrinterHandler = new AddPrinterHandler(addPrinter);
            _searchPrinterHandler = new SearchPrinterHandler(searchPrinters);
            _addPrinterFromSearchHandler = new AddPrinterFromSearchHandler(addPrinterFromSearch);
            _deletePrinterHandler = new DeletePrinterHandler(deletePrinter);
            _searchPrinterTimeoutHandler = new SearchPrinterTimeoutHandler(handleSearchTimeout);
            _openDefaultPrintSettingsHandler = new OpenDefaultPrintSettingsHandler(handleOpenDefaultPrintSettings);

            _onNavigateToEventHandler = new OnNavigateToEventHandler(RegisterPrintSettingValueChange);
            _onNavigateFromEventHandler = new OnNavigateFromEventHandler(UnregisterPrintSettingValueChange);

            _printersViewModel.DeletePrinterHandler += _deletePrinterHandler;
            populatePrintersScreen();
            _printersViewModel.PrinterList = PrinterList;
            _printersViewModel.OpenDefaultPrintSettingsHandler += _openDefaultPrintSettingsHandler;
            _printersViewModel.OnNavigateFromEventHandler += _onNavigateFromEventHandler;
            _printersViewModel.OnNavigateToEventHandler += _onNavigateToEventHandler;
            _printersViewModel.PollingHandler += _pollingHandler;

            _searchPrinterViewModel.AddPrinterFromSearchHandler += _addPrinterFromSearchHandler;
            _searchPrinterViewModel.SearchPrinterHandler += _searchPrinterHandler;
            _searchPrinterViewModel.PrinterSearchList = PrinterSearchList;

            _addPrinterViewModel.AddPrinterHandler += _addPrinterHandler;
            _addPrinterViewModel.PrinterSearchList = PrinterSearchList;
            SNMPController.Instance.Initialize();

        }

        private async void handleOpenDefaultPrintSettings(Printer printer)
        {
            //get new print settings
            PrintSettingsController.Instance.Uninitialize(_screenName);
            _printSettingsViewModel.PrinterName = printer.Name;
            _printSettingsViewModel.PrinterId = printer.Id;
            _printSettingsViewModel.PrinterIpAddress = printer.IpAddress;
            await PrintSettingsController.Instance.Initialize(_screenName, printer);
        }

        public void RegisterPrintSettingValueChange()
        {
            PrintSettingsController.Instance.RegisterPrintSettingValueChanged(_screenName);
        }

        public void UnregisterPrintSettingValueChange()
        {
            PrintSettingsController.Instance.UnregisterPrintSettingValueChanged(_screenName);
            PrintSettingsController.Instance.Uninitialize(_screenName);
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
            //get printers from db
            int indexOfDefaultPrinter = 0;
            var printerListFromDB = await DatabaseController.Instance.GetPrinters();
            var defaultPrinter = await DatabaseController.Instance.GetDefaultPrinter();

            foreach (var printerFromDB in printerListFromDB)
            {
                //check
                //if default printer
                if (defaultPrinter == null)
                {
                    printerFromDB.IsDefault = false;
                }
                else
                {

                    if (defaultPrinter.PrinterId == printerFromDB.Id)
                    {
                        printerFromDB.IsDefault = true;
                        indexOfDefaultPrinter = _printerList.Count();
                    }
                    else
                    {
                        printerFromDB.IsDefault = false;
                    }
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
                //sortPrinterList(indexOfDefaultPrinter);
                _printerListTemp = _printerList;
            }
        }

        private void sortPrinterList(int index)
        {
            Printer defaultPrinter = _printerList.ElementAt(index);

            _printerList.RemoveAt(index);
            _printerList.Insert(0, defaultPrinter);
        }

        public void setPolling(bool willPoll)
        {
            if(willPoll)
            {
                startPolling();
            }
            else
            {
                endPolling();
            }
        }

        private void startPolling()
        {
            isPolling = true;
            int period = 5000;
            var timerHandler = new TimerElapsedHandler(getStatus);

            

            periodicTimer = ThreadPoolTimer.CreatePeriodicTimer(timerHandler, TimeSpan.FromMilliseconds(period));
        }

        private void endPolling()
        {
            isPolling = false;
            if (periodicTimer != null)
            {
                periodicTimer.Cancel();
            }
        }


        public void getStatus(ThreadPoolTimer timer)
        {
            //call SNMP Controller get status here
            updateStatus();

        }

        private async Task updateStatus()
        {
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
                while (i < _printerListTemp.Count && isPolling);
        }

        private async void handlePrinterStatus(string ip, bool isOnline)
        {

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                //find the printer TODO: error handling here when the printer is deleted while handling status
                try{
                    if (isPolling)
                    { 
                        Printer printer = _printerList.First(x => x.IpAddress == ip);
                        System.Diagnostics.Debug.WriteLine(printer.IpAddress);
                        int index = _printerList.IndexOf(printer);

                        //update status
                        printer.IsOnline = isOnline;

                        System.Diagnostics.Debug.WriteLine(index);
                        System.Diagnostics.Debug.WriteLine(isOnline);
                    }
                }
                catch(Exception e)
                {
                    
                }
            });
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
                DialogService.Instance.ShowError("IDS_ERR_MSG_INVALID_IP_ADDRESS", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                return false;
            }

            //check if _printerList is already full
            if (_printerList.Count() >= 10)//TODO: Change to CONSTANTS
            {
                DialogService.Instance.ShowError("IDS_ERR_MSG_MAX_PRINTER_COUNT", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
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
                    DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    return false;
                }

                //check if online
                //get MIB
                SNMPController.Instance.printerControllerAddTimeout = new Action<string, string, List<string>>(handleAddTimeout);
                SNMPController.Instance.printerControllerAddPrinterCallback = handleAddPrinterStatus;
                SNMPController.Instance.getDevice(ip);

                
            }

            if (printer != null)
            {
                //cannot add, printer already in list
                DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
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

        public async void handleAddPrinterStatus(string ip, string name, bool isOnline, List<string> capabilitesList)
        {

            
            
                //add to printerList
                Printer printer = new Printer() { IpAddress = ip, Name = name };
                
                //get capabilities
                if (capabilitesList.Count > 0)
                {
                    printer.EnabledBooklet = (capabilitesList.ElementAt(0) == "true")? true : false;
                    printer.EnabledStapler = (capabilitesList.ElementAt(1) == "true")? true : false;
                    printer.EnabledPunchFour = (capabilitesList.ElementAt(2) == "true")? true : false;
                    printer.EnabledTrayFacedown = (capabilitesList.ElementAt(4) == "true")? true : false;
                    printer.EnabledTrayAutostack = (capabilitesList.ElementAt(5) == "true")? true : false;
                    printer.EnabledTrayTop = (capabilitesList.ElementAt(6) == "true")? true : false;
                    printer.EnabledTrayStack = (capabilitesList.ElementAt(7) == "true") ? true : false;
                }
                else
                {
                    printer.EnabledBooklet = true;
                    printer.EnabledStapler = true;
                    printer.EnabledPunchFour = true;
                    printer.EnabledTrayFacedown = true;
                    printer.EnabledTrayAutostack = true;
                    printer.EnabledTrayTop = true;
                    printer.EnabledTrayStack = true;
                }

                try
                {
                    printer.PrintSettingId = await PrintSettingsController.Instance.CreatePrintSettings(printer); // TODO: (Verify) Create print settings here or update printer after
                    int i = await DatabaseController.Instance.InsertPrinter(printer);
                    if (i == 0)
                    {
                        await DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    }
                    printer.IsOnline = true;
                }
                catch (Exception e)
                {
                    var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                    DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    return;
                }
                
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {

                printer.PropertyChanged += handlePropertyChanged;
                _printerList.Add(printer);


                _printerListTemp = _printerList;
                _addPrinterViewModel.handleAddIsSuccessful(true);

                //if added from printer search
                if (PrinterSearchList.Count > 0) { 
                    PrinterSearchItem searchItem = PrinterSearchList.First(x => x.Ip_address == ip);
                    if (searchItem == null)
                    {
                        //error in adding;
                        var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                        await DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    }
                    else
                    {
                        searchItem.IsInPrinterList = true;
                    }
                }
                
            });
        }

        private async void handleAddTimeout(string ip, string name, List<string> capabilities)
        {
            //add to printerList

            Printer printer = new Printer() { IpAddress = ip, Name = name};

            printer.EnabledBooklet = true;
            printer.EnabledStapler = true;
            printer.EnabledPunchFour = true;
            printer.EnabledTrayFacedown = true;
            printer.EnabledTrayAutostack = true;
            printer.EnabledTrayTop = true;
            printer.EnabledTrayStack = true;

            //insert to database
            printer.PrintSettingId = await PrintSettingsController.Instance.CreatePrintSettings(printer); // TODO: (Verify) Create print settings here or update printer after
            int i = await DatabaseController.Instance.InsertPrinter(printer);
            if (i == 0)
            {
                await DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
            }

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

            //if added from printer search
            if (PrinterSearchList.Count > 0)
            {
                PrinterSearchItem searchItem = PrinterSearchList.First(x => x.Ip_address == ip);
                if (searchItem == null)
                {
                    //error in adding;
                    var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                    await DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                }
                else
                {
                    searchItem.IsInPrinterList = true;
                }
            }
        }


        private async void handlePropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            Printer printer = (Printer) sender;
            if (e.PropertyName == "IsDefault")
            {
                //change default printer in list and in db
                if (printer.IsDefault == true)
                {
                    DatabaseController.Instance.SetDefaultPrinter(printer.Id);

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

            if (e.PropertyName == "PortSetting")
            {
                await DatabaseController.Instance.UpdatePortNumber(printer);
            }
        }

        


        /**
         * Scan Functions
         * 
         * */
        public void searchPrinters()
        {
            _searchPrinterViewModel.SetStateRefreshState();
            _printerSearchList.Clear();
            SNMPController.Instance.printerControllerTimeout = new Action<string>(handleSearchTimeout);
            SNMPController.Instance.printerControllerDiscoverCallback = new Action<PrinterSearchItem>(handleDeviceDiscovered);
            SNMPController.Instance.startDiscover();
            
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

        private void handleSearchTimeout(string ip)
        {
            _searchPrinterViewModel.SearchTimeout();

        }

        public async Task<bool> addPrinterFromSearch(string ip)
        {

            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();

            //check if _printerList is already full
            if (_printerList.Count() >= 10)//TODO: Change to CONSTANTS
            {
                await DialogService.Instance.ShowError("IDS_ERR_MSG_MAX_PRINTER_COUNT", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                return false;
            }

            Printer printer = SNMPController.Instance.getPrinterFromSNMPDevice(ip);
                try
                {
                    printer.PrintSettingId = await PrintSettingsController.Instance.CreatePrintSettings(printer); // TODO: (Verify) Create print settings here or update printer after
                    int i = await DatabaseController.Instance.InsertPrinter(printer);
                    if (i == 0)
                        return false;

                    printer.IsOnline = true;
                }
                catch (Exception e)
                {
                    DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    return false;
                }
                

                printer.PropertyChanged += handlePropertyChanged;
                _printerList.Add(printer);


                _printerListTemp = _printerList;
                _addPrinterViewModel.handleAddIsSuccessful(true);

                if (PrinterSearchList.Count > 0) { 
                    PrinterSearchItem searchItem = PrinterSearchList.First(x => x.Ip_address == ip);
                    if (searchItem == null)
                    {
                        //error in adding;
                        await DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    }
                    else
                    {
                        searchItem.IsInPrinterList = true;
                    }
                }
                return true;
        }
        


        /**
         * Delete functions
         * 
         * */

        public async Task<bool> deletePrinter(string ipAddress)
        {
            Printer printer =  _printerList.First(x => x.IpAddress == ipAddress);

            int result = await DatabaseController.Instance.DeletePrinter(printer);

            if (result == 0)
            {
                return false;
            }
            int index = _printerList.IndexOf(printer);

            _printerList.RemoveAt(index);

            _printerListTemp = _printerList;

            if (DeletePrinterItemsEventHandler != null)
            {
                DeletePrinterItemsEventHandler(printer);
            }

            return true;
        }


    }
}
