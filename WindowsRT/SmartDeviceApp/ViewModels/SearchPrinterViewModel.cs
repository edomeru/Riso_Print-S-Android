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

        public event SmartDeviceApp.Controllers.PrinterController.AddPrinterFromSearchHandler AddPrinterFromSearchHandler;
        public event SmartDeviceApp.Controllers.PrinterController.SearchPrinterHandler SearchPrinterHandler;

        private ICommand _printerSearchItemSelected;

        private ICommand _printerSearchRefreshed;

        private bool _noPrintersFound;

        private double _height;

        private ViewControlViewModel _viewControlViewModel;
        public SearchPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            //NoPrintersFound = true;
            WillRefresh = false;
            NoPrintersFound = false;
            //Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<VisibleRightPane>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetSearchPane(viewOrientation));
            
            
        }

        private void ResetSearchPane(ViewOrientation viewOrientation)
        {
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            Height = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null)) - titleHeight;
        }


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

        public event PropertyChangedEventHandler PropertyChanged;
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }

        public ObservableCollection<PrinterSearchItem> PrinterSearchList
        {
            get { return this._printerSearchList; }
            set
            {
                _printerSearchList = value;
                OnPropertyChanged("PrinterSearchList");
            }
        }

        public ObservableCollection<Printer> PrinterList
        {
            get { return this._printerList; }
            set
            {
                _printerList = value;
                OnPropertyChanged("PrinterList");

            }
        }

        public bool WillRefresh
        {
            get { return this._willRefresh; }
            set
            {
                _willRefresh = value;
                OnPropertyChanged("WillRefresh");
            }
        }

        public bool NoPrintersFound
        {
            get { return this._noPrintersFound; }
            set
            {
                _noPrintersFound = value;
                OnPropertyChanged("NoPrintersFound");
            }
        }

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
            if (!item.IsInPrinterList)
            {
                //add to printer
                bool isSuccessful = await AddPrinterFromSearchHandler(item.Ip_address);
                if (!isSuccessful)
                {
                    //display error message TODO

                    return;
                }

                await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    await DialogService.Instance.ShowMessage("IDS_INFO_MSG_PRINTER_ADD_SUCCESSFUL", "IDS_LBL_SEARCH_PRINTERS", "IDS_LBL_OK", ClosePane);
                });
            }
        }

        public ICommand PrinterSearchRefreshed
        {
            get
            {
                if (_printerSearchRefreshed == null)
                {
                    _printerSearchRefreshed = new RelayCommand(
                        () => PrinterSearchRefreshedExecute(),
                        () => true
                    );
                }
                return _printerSearchRefreshed;
            }
        }

        private void PrinterSearchRefreshedExecute()
        {
            PrinterSearchList.Clear();
            if (NetworkController.IsConnectedToNetwork)
            {
                NoPrintersFound = false;
                SearchPrinterHandler();

            }
            else
            {
                Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.NotRefreshingState);
                DialogService.Instance.ShowError("IDS_ERR_MSG_NETWORK_ERROR", "IDS_LBL_SEARCH_PRINTERS", "IDS_LBL_OK", null);
            }
        }

        public void SearchTimeout()
        {
            WillRefresh = false;
            if (PrinterSearchList.Count > 0)
                NoPrintersFound = false;
            else
                NoPrintersFound = true;

            Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.NotRefreshingState);
        }

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
