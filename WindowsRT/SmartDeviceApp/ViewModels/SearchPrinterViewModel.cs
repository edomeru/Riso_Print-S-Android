using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Windows.UI.Xaml;

namespace SmartDeviceApp.ViewModels
{
    public class SearchPrinterViewModel : ViewModelBase, INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ObservableCollection<PrinterSearchItem> _printerSearchList;
        private ObservableCollection<Printer> _printerList;

        private bool _willRefresh;

        /// <summary>
        /// Add printer from automatic printer search event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.AddPrinterFromSearchHandler AddPrinterFromSearchHandler;

        /// <summary>
        /// Automatic printer search event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.SearchPrinterHandler SearchPrinterHandler;

        private ICommand _printerSearchItemSelected;

        private ICommand _printerSearchRefreshed;

        private bool _noPrintersFound;

        private double _height;

        private ViewControlViewModel _viewControlViewModel;

        private ViewOrientation _viewOrientation;

        private bool _isAdding;

        /// <summary>
        /// Constructor for SearchPrinterViewModel.
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public SearchPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            WillRefresh = false;
            NoPrintersFound = false;
            _isAdding = false;
            //Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<VisibleRightPane>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetSearchPane(viewOrientation));
            
            
        }

        private void ResetSearchPane(ViewOrientation viewOrientation)
        {
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            Height = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null)) - titleHeight;

            ViewOrientation = viewOrientation;
        }

        /// <summary>
        /// Holds the value for the height of the Add Printer pane.
        /// </summary>
        public double Height
        {
            get { return this._height; }
            set
            {
                _height = value;
                OnPropertyChanged("Height");

            }
        }

        private async void SetViewMode(VisibleRightPane viewMode)
        {
            if (_viewControlViewModel.ScreenMode == ScreenMode.Printers)
            {
                if (viewMode == VisibleRightPane.Pane1)
                {
                    WillRefresh = false;
                    if (PrinterList.Count >= 10)
                    {
                        ClosePane();
                        await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                        Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                        {
                            await DialogService.Instance.ShowError("IDS_ERR_MSG_MAX_PRINTER_COUNT", "IDS_LBL_PRINTERS", "IDS_LBL_OK", null);
                        });
                        return;
                    }

                    PrinterSearchList.Clear();
                    if (NetworkController.IsConnectedToNetwork)
                    {
                        SetStateRefreshState();
                    }
                    else
                    {
                        System.Diagnostics.Debug.WriteLine("No network");
                        Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.NotRefreshingState);
                        System.Diagnostics.Debug.WriteLine("Notrefreshing state");
                        await DialogService.Instance.ShowError("IDS_ERR_MSG_NETWORK_ERROR", "IDS_LBL_SEARCH_PRINTERS", "IDS_LBL_OK", null);
                    }
                }
            }
            
        }

        /// <summary>
        /// Event handler for property change.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Notifies classes that a property has been changed.
        /// </summary>
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }

        /// <summary>
        /// Contains the printers searched.
        /// </summary>
        public ObservableCollection<PrinterSearchItem> PrinterSearchList
        {
            get { return this._printerSearchList; }
            set
            {
                _printerSearchList = value;
                OnPropertyChanged("PrinterSearchList");
            }
        }

        /// <summary>
        /// contains the printers added
        /// </summary>
        public ObservableCollection<Printer> PrinterList
        {
            get { return this._printerList; }
            set
            {
                _printerList = value;
                OnPropertyChanged("PrinterList");

            }
        }

        /// <summary>
        /// Flag for refreshing printer search
        /// </summary>
        public bool WillRefresh
        {
            get { return this._willRefresh; }
            set
            {
                if (_willRefresh != value)
                {
                    _willRefresh = value;
                    if (_willRefresh) PrinterSearchRefresh();
                    OnPropertyChanged("WillRefresh");                    
                }
            }
        }

        /// <summary>
        /// Flag whether there are printers found.
        /// </summary>
        public bool NoPrintersFound
        {
            get { return this._noPrintersFound; }
            set
            {
                _noPrintersFound = value;
                OnPropertyChanged("NoPrintersFound");
            }
        }

        /// <summary>
        /// Gets/sets the current view orientation
        /// </summary>
        public ViewOrientation ViewOrientation
        {
            get { return _viewOrientation; }
            set
            {
                if (_viewOrientation != value)
                {
                    _viewOrientation = value;
                    OnPropertyChanged("ViewOrientation");
                }
            }
        }

        /// <summary>
        /// Command executed when a printer in the search list is saved to the printer list.
        /// </summary>
        public ICommand PrinterSearchItemSelected
        {
            get
            {
                if (_printerSearchItemSelected == null)
                {
                    _printerSearchItemSelected = new RelayCommand<PrinterSearchItem>(
                        (item) => PrinterSearchItemSelectedExecute(item),
                        (item) => true
                    );
                }
                return _printerSearchItemSelected;
            }
        }


        private async Task PrinterSearchItemSelectedExecute(PrinterSearchItem item)
        {
            //Check if already added
            if (!item.IsInPrinterList && !_isAdding)
            {
                _isAdding = true;
                //add to printer
                bool isSuccessful = await AddPrinterFromSearchHandler(item.Ip_address);
                if (!isSuccessful)
                {
                    //display error message TODO
                    _isAdding = false;
                    return;
                }

                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    await DialogService.Instance.ShowMessage("IDS_INFO_MSG_PRINTER_ADD_SUCCESSFUL", "IDS_LBL_SEARCH_PRINTERS", "IDS_LBL_OK", ClosePane);
                    _isAdding = false;
                });
            }
        }

        private void PrinterSearchRefresh()
        {
            PrinterSearchList.Clear();
            NoPrintersFound = false;
            if (NetworkController.IsConnectedToNetwork)
            {
                SearchPrinterHandler();
            }
            else
            {
                Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.NotRefreshingState);
                DialogService.Instance.ShowError("IDS_ERR_MSG_NETWORK_ERROR", "IDS_LBL_SEARCH_PRINTERS", "IDS_LBL_OK", null);
            }
        }

        /// <summary>
        /// Handles search time out.
        /// </summary>
        public async void SearchTimeout()
        {
            if (WillRefresh) // BTS#14537 - Handle timeout only on refreshed state
            {
                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
                {
                    if (PrinterSearchList.Count > 0)
                        NoPrintersFound = false;
                    else
                        NoPrintersFound = true;

                    WillRefresh = false;

                    Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.NotRefreshingState);
                });
            }
        }

        /// <summary>
        /// Sets the visual state of Search Printer Pane.
        /// </summary>
        public void SetStateRefreshState()
        {
            NoPrintersFound = false;
            WillRefresh = true;
            Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.RefreshingState);
        }

        private void ClosePane()
        {
            _viewControlViewModel.ViewMode = ViewMode.FullScreen;
        }
    }


}
