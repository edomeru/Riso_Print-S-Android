﻿using System;
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

        /// <summary>
        /// Printer status polling delegate
        /// </summary>
        /// <param name="willPoll"></param>
        public delegate void PollingHandler(bool willPoll);
        private PollingHandler _pollingHandler;

        /// <summary>
        /// Add printer delegate
        /// </summary>
        /// <param name="ip">IP address</param>
        /// <param name="communityName">The SNMP Community Name</param>
        /// <returns>true when printer is added, false otherwise</returns>
        public delegate Task<bool> AddPrinterHandler(string ip, string communityName);
        private AddPrinterHandler _addPrinterHandler;

        /// <summary>
        /// Automatic printer search delegate
        /// <param name="communityName">The SNMP Community Name</param>
        /// </summary>
        public delegate void SearchPrinterHandler(string communityName);
        private SearchPrinterHandler _searchPrinterHandler;

        /// <summary>
        /// Add printer from automatic printer search delegate
        /// </summary>
        /// <param name="ip">IP address</param>
        /// <returns>true when printer is added, false otherwise</returns>
        public delegate Task<bool> AddPrinterFromSearchHandler(string ip);
        private AddPrinterFromSearchHandler _addPrinterFromSearchHandler;

        /// <summary>
        /// Event when the first printer is added
        /// </summary>
        public event PrintPreviewController.AddFirstPrinterEventHandler AddFirstPrinterEvent;

        /// <summary>
        /// Automatic printer search timeout delegate
        /// </summary>
        /// <param name="ipAddress">IP address</param>
        public delegate void SearchPrinterTimeoutHandler(string ipAddress);
        private SearchPrinterTimeoutHandler _searchPrinterTimeoutHandler;

        /// <summary>
        /// Delete printer delegate
        /// </summary>
        /// <param name="ipAddress">IP address</param>
        /// <returns>true when printer is deleted, false otherwise</returns>
        public delegate Task<bool> DeletePrinterHandler(string ipAddress);
        private DeletePrinterHandler _deletePrinterHandler;

        /// <summary>
        /// Open default print settings delegate
        /// </summary>
        /// <param name="printer">printer</param>
        public delegate void OpenDefaultPrintSettingsHandler(Printer printer);
        private OpenDefaultPrintSettingsHandler _openDefaultPrintSettingsHandler;

        /// <summary>
        /// Navigate to Printer screen delegate
        /// </summary>
        public delegate void OnNavigateToEventHandler();
        private OnNavigateToEventHandler _onNavigateToEventHandler;

        /// <summary>
        /// Navigate from Printer screen delegate
        /// </summary>
        public delegate void OnNavigateFromEventHandler();
        private OnNavigateFromEventHandler _onNavigateFromEventHandler;

        /// <summary>
        /// Delete Printer-related items delegate
        /// </summary>
        /// <param name="printer"></param>
        public delegate void DeletePrinterItemsHandler(Printer printer);
        public event DeletePrinterItemsHandler DeletePrinterItemsEventHandler;

        /// <summary>
        /// Clear IP address text field delegate
        /// </summary>
        public delegate void ClearIpAddressToAddHandler();
        private ClearIpAddressToAddHandler _clearIpAddressToAddHandler;
        
        ThreadPoolTimer periodicTimer;

        private ObservableCollection<Printer> _printerList = new ObservableCollection<Printer>();
        private ObservableCollection<Printer> _printerListTemp = new ObservableCollection<Printer>();
        private ObservableCollection<PrinterSearchItem> _printerSearchList = new ObservableCollection<PrinterSearchItem>();

        private PrintersViewModel _printersViewModel;
        private SearchPrinterViewModel _searchPrinterViewModel;
        private SearchSettingsViewModel _searchSettingsViewModel;
        private AddPrinterViewModel _addPrinterViewModel;
        private PrintSettingsViewModel _printSettingsViewModel;
        private SelectPrinterViewModel _selectPrinterViewModel;
        private string _screenName;

        private bool isPolling = false;

        private string _manualAddIP = "";

        /// <summary>
        /// PropertyChanged event
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

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

            _printerList = new ObservableCollection<Printer>();
            _printerListTemp = new ObservableCollection<Printer>();
            _printerSearchList = new ObservableCollection<PrinterSearchItem>();
            _printersViewModel = new ViewModelLocator().PrintersViewModel;
            _searchPrinterViewModel = new ViewModelLocator().SearchPrinterViewModel;
            _searchSettingsViewModel = new ViewModelLocator().SearchSettingsViewModel;
            _addPrinterViewModel = new ViewModelLocator().AddPrinterViewModel;
            _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;
            _selectPrinterViewModel = new ViewModelLocator().SelectPrinterViewModel;

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

            _clearIpAddressToAddHandler = new ClearIpAddressToAddHandler(clearIpAddressToAdd);

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
            _searchPrinterViewModel.PrinterList = PrinterList;

            _addPrinterViewModel.AddPrinterHandler += _addPrinterHandler;
            _addPrinterViewModel.PrinterSearchList = PrinterSearchList;
            _addPrinterViewModel.PrinterList = PrinterList;
            _addPrinterViewModel.ClearIpAddressToAddHandler += _clearIpAddressToAddHandler;

            _selectPrinterViewModel.PollingHandler += _pollingHandler;
            SNMPController.Instance.Initialize();

        }

        #region Properties

        /// <summary>
        /// Holds the printers saved.
        /// </summary>
        public ObservableCollection<Printer> PrinterList
        {
            get { return this._printerList; }
        }

        /// <summary>
        /// Holds the searched printers.
        /// </summary>
        public ObservableCollection<PrinterSearchItem> PrinterSearchList
        {
            get { return this._printerSearchList; }
        }
        #endregion Properties


        #region Public Methods

        /// <summary>
        /// Registers print settings value change event.
        /// </summary>
        public void RegisterPrintSettingValueChange()
        {
          //  PrintSettingsController.Instance.RegisterPrintSettingValueChanged(_screenName); 
            PrintSettingsController.Instance.RegisterScreenModeChange(_screenName);
        }

        /// <summary>
        /// Unregisters print settings value change event.
        /// </summary>
        public void UnregisterPrintSettingValueChange()
        {
            PrintSettingsController.Instance.UnregisterPrintSettingValueChanged(_screenName);
            PrintSettingsController.Instance.Uninitialize(_screenName);
            PrintPreviewController.Instance.ReinitializeSettings();
        }

        /// <summary>
        /// Populates the printer list of printers saved in the database.
        /// </summary>
        public async void populatePrintersScreen()
        {
            //get printers from db
            int indexOfDefaultPrinter = 0;
            var printerListFromDB = await DatabaseController.Instance.GetPrinters();
            var defaultPrinter = await DatabaseController.Instance.GetDefaultPrinter();

            foreach (var printerFromDB in printerListFromDB)
            {
                //check if default printer
                if (defaultPrinter.PrinterId == printerFromDB.Id)
                {
                    printerFromDB.IsDefault = true;
                    indexOfDefaultPrinter = _printerList.Count();
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
                //sortPrinterList(indexOfDefaultPrinter);
                _printerListTemp = _printerList;
            }

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
            Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                CheckPrinterListEmpty();
            });
        }

        /// <summary>
        /// Gets the default printer from the printer list.
        /// </summary>
        /// <returns>The default printer.</returns>
        public Printer GetDefaultPrinter()
        {
            return PrinterList.FirstOrDefault(prn => prn.IsDefault);
        }

        /// <summary>
        /// Gets the printer from the printer list with the same id as the parameter.
        /// </summary>
        /// <param name="printerId">Printer ID used to search the list.</param>
        /// <returns>printer with the same ID as the parameter</returns>
        public Printer GetPrinter(int printerId)
        {
            return PrinterList.FirstOrDefault(prn => prn.Id == printerId);
        }

        /// <summary>
        /// Starts or ends the polling for the printers.
        /// </summary>
        /// <param name="willPoll">Flag to check whether to start or end the polling</param>
        public void setPolling(bool willPoll)
        {
            if (willPoll)
            {
                if (PrinterList.Count > 0)
                    startPolling();
            }
            else
            {
                endPolling();
            }
        }

        /// <summary>
        /// Saves the new printer to the printer list.
        /// </summary>
        /// <param name="ip">IP Address of the new printer</param>
        /// <param name="snmpCommunityName">SNMP Community Name used to check the printer capabilities</param>
        /// <returns>true if successfully added, else false</returns>
        public async Task<bool> addPrinter(string ip, string snmpCommunityName)
        {

            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();

            // No checking for valid IP format here. This is assumed to be checked from ViewModel before using this method.

            //check if _printerList is already full
            if (_printerList.Count() >= 10)//TODO: Change to CONSTANTS
            {
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    await DialogService.Instance.ShowError("IDS_ERR_MSG_MAX_PRINTER_COUNT", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                });
                return false;
            }

            _manualAddIP = ip;
            //check if already in _printerList
            Printer printer = _printerList.FirstOrDefault(x => x.IpAddress == ip);
            if (printer == null)
            {
                //check if online
                //get MIB
                if (NetworkController.IsConnectedToNetwork)
                {
                    //check invalid ip
                    if (ip.Equals("255.255.255.255"))
                    {
                        handleAddError();
                    }
                    else
                    {
                        SNMPController.Instance.printerControllerAddTimeout = new Action<string, string, List<string>>(handleAddTimeout);
                        SNMPController.Instance.printerControllerAddPrinterCallback = new Action<string, string, bool, bool, List<string>>(handleAddPrinterStatus);
                        SNMPController.Instance.printerControllerErrorCallBack = new Action(handleAddError);
                        SNMPController.Instance.getDevice(ip, snmpCommunityName);
                    }
                }
                else
                {
                    _addPrinterViewModel.setVisibilities();
                    _addPrinterViewModel.handleAddIsSuccessful(false);
                }
            }
            else
            {
                //cannot add, printer already in list
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    await Task.Delay(350);
                    DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                });
                return false;
            }

            return true;

        }

        private async void handleAddError()
        {
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    DialogService.Instance.ShowError("IDS_ERR_MSG_INVALID_IP_ADDRESS", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    _addPrinterViewModel.setVisibilities();
                });
            return;
        }

        /// <summary>
        /// Starts the search of printers in the network.
        /// <param name="snmpCommunityName">The SNMP Community Name used for printer search</param>
        /// </summary>
        public void searchPrinters(string snmpCommunityName)
        {
            //_searchPrinterViewModel.SetStateRefreshState();
            _printerSearchList.Clear();

            //for testing
            //PrinterSearchItem p1 = new PrinterSearchItem() {Name = "Test1", Ip_address = "192.12.12.12", IsInPrinterList = true };
            //PrinterSearchItem p2 = new PrinterSearchItem() { Name = "Test2", Ip_address = "192.12.12.11", IsInPrinterList = true };

            //PrinterSearchList.Add(p1);
            //PrinterSearchList.Add(p2);

            SNMPController.Instance.printerControllerTimeout = new Action<string>(handleSearchTimeout);
            SNMPController.Instance.printerControllerDiscoverCallback = new Action<PrinterSearchItem>(handleDeviceDiscovered);
            SNMPController.Instance.Discovery.SnmpCommunityName = snmpCommunityName;
            SNMPController.Instance.startDiscover();
        }

        /// <summary>
        /// Adds the selected printer from the printer search list to the saved printers.
        /// </summary>
        /// <param name="ip">IP Address of the new printer to be added</param>
        /// <returns>true if successful, else false</returns>
        public async Task<bool> addPrinterFromSearch(string ip)
        {

            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();

            //check if _printerList is already full
            if (_printerList.Count() >= 10)//TODO: Change to CONSTANTS
            {
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    await DialogService.Instance.ShowError("IDS_ERR_MSG_MAX_PRINTER_COUNT", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);

                });
                return false;
            }

            Printer printer = SNMPController.Instance.getPrinterFromSNMPDevice(ip);
            if (printer != null)
            {
                try
                {
                    bool result = await DatabaseController.Instance.InsertPrinter(printer);
                    if (!result)
                    {
                        await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                        Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                        {
                            await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE",
                                "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                        });
                        return false;
                    }

                    printer.PrintSettingId = await PrintSettingsController.Instance.CreatePrintSettings(printer);
                    // TODO: Check DB error, consider "Cannot add printer" message and rollback add printer
                    //if (printer.PrintSettingId == -1)
                    //{
                    //    await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE",
                    //        "IDS_LBL_PRINTERS", "IDS_LBL_OK", null);
                    //    return false;
                    //}
                    await DatabaseController.Instance.UpdatePrinter(printer);
                    // TODO: Check DB error, consider "Cannot add printer" message and rollback add printer

                    printer.IsOnline = true;
                }
                catch (Exception e)
                {
                    DialogService.Instance.ShowError("IDS_ERR_MSG_CANNOT_ADD_PRINTER", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                    return false;
                }

                if (PrinterList.Count == 0)
                {
                    printer.IsDefault = true;
                    bool result = await DatabaseController.Instance.SetDefaultPrinter(printer.Id);
                    // Check DB error
                    if (!result)
                    {
                        await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE",
                            "IDS_LBL_PRINTERS", "IDS_LBL_OK", null);
                        printer.IsDefault = false;
                    }
                }
                printer.PropertyChanged += handlePropertyChanged;
                _printerList.Add(printer);

                // If only printer in list
                if (PrinterList.Count == 1 && AddFirstPrinterEvent != null)
                {
                    await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                    Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                    {
                        AddFirstPrinterEvent();
                    });
                }

                _printerListTemp = _printerList;


                if (PrinterSearchList.Count > 0)
                {

                    PrinterSearchItem searchItem = PrinterSearchList.FirstOrDefault(x => x.Ip_address == ip);

                    if (searchItem != null)
                    {
                        await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                        Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                        {
                            searchItem.IsInPrinterList = true;
                        });
                    }
                }


                CheckPrinterListEmpty();
                return true;
            }
            else
            {
                CheckPrinterListEmpty();
                return false;
            }
        }

        /// <summary>
        /// Deletes a printer from the printer list.
        /// </summary>
        /// <param name="ipAddress">IP address of the printer to be deleted.</param>
        /// <returns>true if successful, else false</returns>
        public async Task<bool> deletePrinter(string ipAddress)
        {
            Printer printer = _printerList.FirstOrDefault(x => x.IpAddress == ipAddress);
            bool result = await DatabaseController.Instance.DeletePrinter(printer);
            if (!result)
            {
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE", "IDS_LBL_PRINTERS",
                        "IDS_LBL_OK", null);
                });
                return false;
            }
            int index = _printerList.IndexOf(printer);

            if (printer.IsDefault)
            {
                if (PrinterList.Count > 1)
                {
                    int indexOfNextDefault = index + 1;
                    Printer nextDefault = null;
                    if (index > 0)
                    {

                        nextDefault = _printerList.ElementAt(0);
                        nextDefault.IsDefault = true;
                    }
                    else
                    {
                        nextDefault = _printerList.ElementAt(indexOfNextDefault);
                        nextDefault.IsDefault = true;
                    }
                }
            }

            _printerList.RemoveAt(index);

            if (PrinterList.Count == 1 && AddFirstPrinterEvent != null)
            {
                AddFirstPrinterEvent();
            }

            _printerListTemp = _printerList;

            if (DeletePrinterItemsEventHandler != null)
            {
                DeletePrinterItemsEventHandler(printer);
            }

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
            Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                CheckPrinterListEmpty();
            });

            if (PrinterList.Count == 0 && isPolling)
                endPolling();

            return true;
        }

        /// <summary>
        /// Check if the printer list is empty
        /// </summary>
        public void CheckPrinterListEmpty()
        {
            if (PrinterList.Count == 0)
            {
                _printersViewModel.IsPrinterListEmpty = true;
            }
            else
            {
                _printersViewModel.IsPrinterListEmpty = false;
            }
        }

        #endregion Public Methods


        #region Private Methods

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


        private void getStatus(ThreadPoolTimer timer)
        {
            //call SNMP Controller get status here
            updateStatus();

        }

        private async Task updateStatus()
        {
            int i = 0;
            if (PrinterList.Count > 0)
            {
                do
                {
                    Printer printer = _printerListTemp.ElementAt(i);
                    //request for eachs printer's printer status
                    if (printer != null)
                    {
                        NetworkController.Instance.networkControllerPingStatusCallback = new Action<string, bool>(handlePrinterStatus);
                        await NetworkController.Instance.pingDevice(printer.IpAddress);
                    }
                    i++;

                }
                while (i < _printerListTemp.Count && isPolling);
            }
        }

        private async void handlePrinterStatus(string ip, bool isOnline)
        {
            try
            {
                if (isPolling)
                {
                    Printer printer = _printerList.FirstOrDefault(x => x.IpAddress == ip);

                    if (printer == null)
                    {
                        //printer might have been deleted already, so ignore the null printer
                        return;
                    }

                    await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                        Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                        {
                            //update status
                            printer.IsOnline = isOnline;
                        });
                }
            }
            catch (Exception e)
            {

            }
        }

        private async void handleOpenDefaultPrintSettings(Printer printer)
        {
            //get new print settings
            PrintSettingsController.Instance.Uninitialize(_screenName);
            await PrintSettingsController.Instance.Initialize(_screenName, printer, null);
        }

        private async void handleAddPrinterStatus(string ip, string name, bool isOnline, bool isSupported, List<string> capabilitesList)
        {
            //check if viewmode is rightpanevisible
            var viewControl = new ViewModelLocator().ViewControlViewModel;

            if (viewControl.ScreenMode == Common.Enum.ScreenMode.Printers)
            {
                if (viewControl.ViewMode == Common.Enum.ViewMode.RightPaneVisible)
                {
                    if (!isSupported)
                    {
                        await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                        Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                        {
                            _addPrinterViewModel.handleAddIsSuccessful(isSupported);
                        });
                    }
                    else if (!string.IsNullOrEmpty(_manualAddIP))
                    {
                        clearIpAddressToAdd();


                        if (PrinterList.Count > 0)
                        {

                            Printer inList = PrinterList.FirstOrDefault(x => x.IpAddress == ip);
                            if (inList != null)
                            {
                                return;
                            }

                        }

                        //add to printerList
                        Printer printer = new Printer() { IpAddress = ip, Name = name };

                        //get capabilities
                        if (capabilitesList.Count > 0)
                        {
                            try
                            {
                                printer.EnabledBookletFinishing = (capabilitesList.ElementAt(0) == "true");
                                // multifunction finisher 2/3 and 2/4 also has staple, so enable stapler when the multifunction finisher is available
                                printer.EnabledStapler = (capabilitesList.ElementAt(1) == "true") || (capabilitesList.ElementAt(2) == "true") || (capabilitesList.ElementAt(3) == "true");
                                printer.EnabledPunchFour = (capabilitesList.ElementAt(2) == "true");
                                printer.EnabledPunchThree = (capabilitesList.ElementAt(3) == "true");
                                printer.EnabledTrayFacedown = true;
                                //printer.EnabledTrayAutostack = (capabilitesList.ElementAt(5) == "true")? true : false;
                                printer.EnabledTrayTop = (capabilitesList.ElementAt(6) == "true");
                                printer.EnabledTrayStack = (capabilitesList.ElementAt(7) == "true");
                                printer.EnabledPaperLW = (capabilitesList.ElementAt(8) == "true");
                                printer.EnabledFeedTrayOne = (capabilitesList.ElementAt(9) == "true");
                                printer.EnabledFeedTrayTwo = (capabilitesList.ElementAt(10) == "true");
                                printer.EnabledFeedTrayThree = (capabilitesList.ElementAt(11) == "true");
                            }
                            catch (Exception e)
                            {
                                LogUtility.LogError(e);
                            }
                        }

                        bool result = await DatabaseController.Instance.InsertPrinter(printer);
                        if (!result)
                        {
                            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                            Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                            {
                                await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE",
                                    "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                                _addPrinterViewModel.handleAddError();
                            });
                            return;
                        }
                        printer.PrintSettingId = await PrintSettingsController.Instance.CreatePrintSettings(printer);
                        // TODO: Check DB error, consider "Cannot add printer" message and rollback add printer
                        //if (printer.PrintSettingId == -1)
                        //{
                        //    await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE",
                        //        "IDS_LBL_PRINTERS", "IDS_LBL_OK", null);
                        //    return;
                        //}
                        await DatabaseController.Instance.UpdatePrinter(printer);
                        // TODO: Check DB error, consider "Cannot add printer" message and rollback add printer

                        printer.IsOnline = true;

                        if (PrinterList.Count == 0)
                        {
                            printer.IsDefault = true;
                            result = await DatabaseController.Instance.SetDefaultPrinter(printer.Id);
                            // Check DB error
                            if (!result)
                            {
                                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                                {
                                    await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE",
                                        "IDS_LBL_PRINTERS", "IDS_LBL_OK", null);
                                    printer.IsDefault = false;
                                    _addPrinterViewModel.handleAddError();
                                });
                            }
                        }
                        await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                        Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                        {
                            printer.PropertyChanged += handlePropertyChanged;
                            _printerList.Add(printer);

                            // If only printer in list
                            if (PrinterList.Count == 1 && AddFirstPrinterEvent != null)
                            {
                                AddFirstPrinterEvent();
                            }

                            _printerListTemp = _printerList;
                            _addPrinterViewModel.handleAddIsSuccessful(isSupported);

                            //if added from printer search
                            if (PrinterSearchList.Count > 0)
                            {
                                PrinterSearchItem searchItem = PrinterSearchList.FirstOrDefault(x => x.Ip_address == ip);
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
                }
            }
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
            Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                CheckPrinterListEmpty();
            });
        }

        private async void handleAddTimeout(string ip, string name, List<string> capabilities)
        {
            //add to printerList
            //check if viewmode is rightpanevisible
            var viewControl = new ViewModelLocator().ViewControlViewModel;

            if (viewControl.ScreenMode == Common.Enum.ScreenMode.Printers)
            {
                if (viewControl.ViewMode == Common.Enum.ViewMode.RightPaneVisible)
                {
                    await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                    Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                    {
                        _addPrinterViewModel.handleAddIsSuccessful(false);
                    });
                }
            }
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
            Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                CheckPrinterListEmpty();
            });
        }


        private async void handlePropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            Printer printer = (Printer)sender;
            if (e.PropertyName == "IsDefault")
            {
                //change default printer in list and in db
                if (printer.IsDefault == true)
                {
                    bool result = await DatabaseController.Instance.SetDefaultPrinter(printer.Id);
                    // Check DB error
                    if (!result)
                    {
                        await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                        Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                        {
                            await DialogService.Instance.ShowError("IDS_ERR_MSG_DB_FAILURE",
                                "IDS_LBL_PRINTERS", "IDS_LBL_OK", null);
                            printer.IsDefault = false;
                        });
                        return;
                    }

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
                foreach (var printerInList in PrinterList)
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
            //if (e.PropertyName == "VisualState")
            //{
            //    if (printer.VisualState == "Pressed")
            //    {
            //        handleOpenDefaultPrintSettings(printer);
            //    }
            //}
            //For future use
            //if (e.PropertyName == "PortSetting")
            //{
            //    await DatabaseController.Instance.UpdatePrinter(printer);
            //}
        }

        private async void handleDeviceDiscovered(PrinterSearchItem printer)
        {
            Printer printerInList = null;

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    _printerSearchList.Add(printer);

                    printerInList = _printerList.FirstOrDefault(x => x.IpAddress == printer.Ip_address);
                    if (printerInList == null)
                    {
                        printer.IsInPrinterList = false;
                    }
                    else
                    {
                        printer.IsInPrinterList = true;
                    }
                });

        }

        private void handleSearchTimeout(string ip)
        {
            _searchPrinterViewModel.SearchTimeout();

        }

        private void clearIpAddressToAdd()
        {
            _manualAddIP = "";
        }

        #endregion Private Methods

        public PrinterController()
        {
        }
    }
}
