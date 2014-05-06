using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace SmartDeviceApp.ViewModels
{
    public class SearchPrinterViewModel : ViewModelBase, INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ObservableCollection<PrinterSearchItem> _printerSearchList;

        private bool _willRefresh;

        public event SmartDeviceApp.Controllers.PrinterController.AddPrinterHandler AddPrinterHandler;
        public event SmartDeviceApp.Controllers.PrinterController.SearchPrinterHandler SearchPrinterHandler;

        private ICommand _printerSearchItemSelected;

        private ICommand _printerSearchRefreshed;

        private ViewControlViewModel _viewControlViewModel;
        public SearchPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;

            //Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<VisibleRightPane>(this, (viewMode) => SetViewMode(viewMode));
            
            
        }

        private void SetViewMode(VisibleRightPane viewMode)
        {
            if (_viewControlViewModel.ScreenMode == ScreenMode.Printers)
            {
                if (viewMode == VisibleRightPane.Pane1)
                {
                    if (NetworkController.IsConnectedToNetwork)
                    {
                        SearchPrinterHandler();
                    }
                    else
                    {
                        var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                        DisplayMessage(loader.GetString("IDS_LBL_SEARCH_PRINTERS"), loader.GetString("IDS_ERR_MSG_NETWORK_ERROR"));
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

        public bool WillRefresh
        {
            get { return this._willRefresh; }
            set
            {
                _willRefresh = value;
                OnPropertyChanged("WillRefresh");
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


        private void PrinterSearchItemSelectedExecute(PrinterSearchItem item)
        {
            //Check if already added
            if (!item.IsInPrinterList)
            {
                //add to printer
                bool isSuccessful = AddPrinterHandler(item.Ip_address);
                if (!isSuccessful)
                {
                    //display error message TODO

                    return;
                }

                //item.IsInPrinterList = true;

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
            if (NetworkController.IsConnectedToNetwork)
            {
                SearchPrinterHandler();
            }
            else
            {
                var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                DisplayMessage(loader.GetString("IDS_LBL_SEARCH_PRINTERS"), loader.GetString("IDS_ERR_MSG_NETWORK_ERROR"));
            }
        }

        public void SearchTimeout()
        {
            Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.NotRefreshingState);
        }

        public void SetStateRefreshState()
        {
            WillRefresh = true;
            Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.RefreshingState);
        }

        public void DisplayMessage(string caption, string content)
        {
            MessageAlert ma = new MessageAlert();
            ma.Caption = caption;
            ma.Content = content;
            Messenger.Default.Send<MessageAlert>(ma);
        }
    }


}
