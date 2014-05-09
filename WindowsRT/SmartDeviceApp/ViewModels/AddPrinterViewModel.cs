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
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.ViewModels
{
    public class AddPrinterViewModel : ViewModelBase, INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private string _ipAddress;
        private string _username;
        private string _password;

        private ICommand _addPrinter;

        private bool _isProgressRingVisible;
        private bool _isButtonVisible;

        private ImageSource _buttonImage;

        public event SmartDeviceApp.Controllers.PrinterController.AddPrinterHandler AddPrinterHandler;

        private ObservableCollection<PrinterSearchItem> _printerSearchList;

        private string ADD_IMAGE = "ms-appx:///Resources/Images/img_btn_add_printer_normal.scale-100.png";
        private ViewControlViewModel _viewControlViewModel;
        public AddPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;

            IpAddress = "";
            //Username = "";
            //Password = "";
            //ButtonImage = new ImageSource();
            IsProgressRingVisible = false;
            IsButtonVisible = true;
            Messenger.Default.Register<VisibleRightPane>(this, (viewMode) => SetViewMode(viewMode));
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

        public string Username
        {
            get { return _username; }
            set
            {
                this._username = value;
                OnPropertyChanged("Username");
            }
        }

        public string Password
        {
            get { return _password; }
            set
            {
                this._password = value;
                OnPropertyChanged("Password");
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

        private void AddPrinterExecute()
        {
            System.Diagnostics.Debug.WriteLine(IpAddress);

            PrinterSearchList.Clear();

            //check if has data
            if (IpAddress.Equals("") )
            {
                //error please input data
                //display error message TODO
                return;
            }

            //add to printer controller

            IsButtonVisible = false;
            IsProgressRingVisible = true;
            if (AddPrinterHandler(IpAddress) == false)
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

        public ImageSource ButtonImage
        {
            get { return _buttonImage; }
            set
            {
                this._buttonImage = value;
                OnPropertyChanged("ButtonImage");
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
                DialogService.Instance.ShowError("IDS_LBL_ADD_SUCCESSFUL", "IDS_LBL_ADD_PRINTER", "IDS_LBL_OK", null);

                return;
            }
            else
            {
                if (NetworkController.IsConnectedToNetwork)
                {
                    content = loader.GetString("IDS_ERR_MSG_WARNING_CANNOT_FIND_PRINTER") + "\n" + IpAddress + " " +
                        loader.GetString("IDS_LBL_ADD_SUCCESSFUL");
                }
                else
                {
                    
                    content = loader.GetString("IDS_ERR_MSG_NETWORK_ERROR");
                }
            }
            caption = loader.GetString("IDS_LBL_ADD_PRINTER");
            buttonText = loader.GetString("IDS_LBL_OK");
            //clear data
            IpAddress = "";
            Username = "";
            Password = "";

            setVisibilities();
            DisplayMessage(caption, content, buttonText);
        }

        public void setVisibilities()
        {
            IsButtonVisible = true;
            IsProgressRingVisible = false;
        }

        public void DisplayMessage(string caption, string content, string buttonText)
        {
            DialogService.Instance.ShowCustomMessageBox(content, caption, buttonText, null);
        }

        private void SetViewMode(VisibleRightPane viewMode)
        {
            if (_viewControlViewModel.ScreenMode == ScreenMode.Printers)
            {
                if (viewMode == VisibleRightPane.Pane2)
                {
                    //clear data
                    IpAddress = "";
                    Username = "";
                    Password = "";
                }
            }

        }
    }
}
