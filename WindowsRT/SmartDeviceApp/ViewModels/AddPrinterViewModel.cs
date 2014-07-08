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
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.ViewModels
{
    public class AddPrinterViewModel : ViewModelBase, INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private string _ipAddress;

        private ICommand _addPrinter;

        private bool _isProgressRingVisible;
        private bool _isButtonVisible;

        private double _height;

        public event SmartDeviceApp.Controllers.PrinterController.AddPrinterHandler AddPrinterHandler;
        public event SmartDeviceApp.Controllers.PrinterController.ClearIpAddressToAddHandler ClearIpAddressToAddHandler;

        private ObservableCollection<PrinterSearchItem> _printerSearchList;
        private ObservableCollection<Printer> _printerList;

        private ViewControlViewModel _viewControlViewModel;
        public AddPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;

            IpAddress = "";
            IsProgressRingVisible = false;
            IsButtonVisible = true;
            Messenger.Default.Register<VisibleRightPane>(this, (rightPaneMode) => SetRightPaneMode(rightPaneMode));
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<MessageType>(this, (strMsg) => HandleStringMessage(strMsg));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetAddPane(viewOrientation));
            
        }

        private void ResetAddPane(ViewOrientation viewOrientation)
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

        private async Task HandleStringMessage(MessageType strMsg)
        {
            if (strMsg == MessageType.AddPrinter)
            {
                await AddPrinterExecute();
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }

        public string IpAddress
        {
            get { return _ipAddress; }
            set
            {
                this._ipAddress = value;
                OnPropertyChanged("IpAddress");
            }
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

        public ICommand AddPrinter
        {
            get
            {
                if (_addPrinter == null)
                {
                    _addPrinter = new RelayCommand(
                        () => AddPrinterExecute(),
                        () => true
                    );
                }
                return _addPrinter;
            }
        }

        private async Task AddPrinterExecute()
        {
            //Messenger.Default.Send<string>("HideKeyboard");

            System.Diagnostics.Debug.WriteLine(IpAddress);

            PrinterSearchList.Clear();

            //check if has data
            if (IpAddress.Equals(""))
            {
                //display error message
                await DialogService.Instance.ShowError("IDS_ERR_MSG_INVALID_IP_ADDRESS", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);
                return;
            }

            //add to printer controller

            IsButtonVisible = false;
            IsProgressRingVisible = true;
            bool result = await AddPrinterHandler(IpAddress);
            if (result == false)
            {
                setVisibilities();
            }
            
        }

        public bool IsProgressRingVisible
        {
            get { return _isProgressRingVisible; }
            set
            {
                this._isProgressRingVisible = value;
                OnPropertyChanged("IsProgressRingVisible");
            }
        }

        public bool IsButtonVisible
        {
            get { return _isButtonVisible; }
            set
            {
                this._isButtonVisible = value;
                OnPropertyChanged("IsButtonVisible");
            }
        }

        public void handleAddIsSuccessful(bool isSuccessful)
        {
            string caption = "";
            string content = "";
            string buttonText = "";
            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();

            if (isSuccessful)
            {
                content = loader.GetString("IDS_INFO_MSG_PRINTER_ADD_SUCCESSFUL");
            }
            else
            {
                if (NetworkController.IsConnectedToNetwork)
                {
                    content = loader.GetString("IDS_INFO_MSG_WARNING_CANNOT_FIND_PRINTER");
                }
                else
                {
                    
                    content = loader.GetString("IDS_ERR_MSG_NETWORK_ERROR");
                }
            }
            caption = loader.GetString("IDS_LBL_ADD_PRINTER");
            buttonText = loader.GetString("IDS_LBL_OK");
            

            setVisibilities();
            DisplayMessage(caption, content, buttonText);
        }

        public void setVisibilities()
        {
            IsButtonVisible = true;
            IsProgressRingVisible = false;
        }

        public void handleAddError()
        {
            setVisibilities();
        }

        public void DisplayMessage(string caption, string content, string buttonText)
        {
            DialogService.Instance.ShowCustomMessageBox(content, caption, buttonText, new Action(ClosePane));
        }

        private void ClosePane()
        {
            _viewControlViewModel.ViewMode = ViewMode.FullScreen;
        }

        private async void SetRightPaneMode(VisibleRightPane rightPaneMode)
        {
            if (_viewControlViewModel.ScreenMode == ScreenMode.Printers)
            {
                if (rightPaneMode == VisibleRightPane.Pane2)
                {
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
                }
                
            }
        }

        private void SetViewMode(ViewMode viewMode)
        {
            if (viewMode == ViewMode.FullScreen)
            {
                IpAddress = "";
                setVisibilities();
                ClearIpAddressToAddHandler();
            }
        }
    }
}
