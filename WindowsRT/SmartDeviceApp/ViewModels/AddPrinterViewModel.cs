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

        /// <summary>
        /// Add printer event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.AddPrinterHandler AddPrinterHandler;

        /// <summary>
        /// Clear IP address text field event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.ClearIpAddressToAddHandler ClearIpAddressToAddHandler;

        private ObservableCollection<PrinterSearchItem> _printerSearchList;
        private ObservableCollection<Printer> _printerList;

        private ViewControlViewModel _viewControlViewModel;

        private ViewOrientation _viewOrientation;

        /// <summary>
        /// Constructor for AddPrinterViewModel.
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
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


        /// <summary>
        /// Event handler for property change.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        #region Properties

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

        /// <summary>
        /// Holds the value of the ip address of the printer to be added
        /// </summary>
        public string IpAddress
        {
            get { return _ipAddress; }
            set
            {
                this._ipAddress = value;
                OnPropertyChanged("IpAddress");
            }
        }

        /// <summary>
        /// Contains the printers searched. This is used to clear the search list if a search is conducted before manual addition.
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
        /// Contains the printers already added.
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
        /// Command to be executed when Add button is tapped
        /// </summary>
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

        public bool IsProgressRingVisible
        {
            get { return _isProgressRingVisible; }
            set
            {
                this._isProgressRingVisible = value;
                OnPropertyChanged("IsProgressRingVisible");
            }
        }

        /// <summary>
        /// Flag to check if add button is visible or not. Add button will be replaced by a progress ring while the printer is being added.
        /// </summary>
        public bool IsButtonVisible
        {
            get { return _isButtonVisible; }
            set
            {
                this._isButtonVisible = value;
                OnPropertyChanged("IsButtonVisible");
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

        #endregion Properties

        #region Public Methods
        /// <summary>
        /// Handles the result of the addition of printer. Displays the necessary result.
        /// </summary>
        /// <param name="isSuccessful">Flag to check if addition of printer is successful or not.</param>
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

        /// <summary>
        /// Sets the visibilities of the Add button and the Progress ring
        /// </summary>
        public void setVisibilities()
        {
            IsButtonVisible = true;
            IsProgressRingVisible = false;
        }

        /// <summary>
        /// Handles addition errors and sets the visibilities of the button and progress ring.
        /// </summary>
        public void handleAddError()
        {
            setVisibilities();
        }

        /// <summary>
        /// Displays the message result.
        /// </summary>
        /// <param name="caption">Title of the dialog</param>
        /// <param name="content">Message result</param>
        /// <param name="buttonText">Text for the button</param>
        public void DisplayMessage(string caption, string content, string buttonText)
        {
            DialogService.Instance.ShowCustomMessageBox(content, caption, buttonText, new Action(ClosePane));
        }

        /// <summary>
        /// Notifies classes that a property has been changed.
        /// </summary>
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }
        #endregion Public Methods

        #region Private Methods

        private void ResetAddPane(ViewOrientation viewOrientation)
        {
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            Height = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null)) - titleHeight;

            ViewOrientation = viewOrientation;
        }

        private async Task HandleStringMessage(MessageType strMsg)
        {
            if (strMsg == MessageType.AddPrinter)
            {
                await AddPrinterExecute();
            }
        }

        private async Task AddPrinterExecute()
        {
            //Messenger.Default.Send<string>("HideKeyboard");

            System.Diagnostics.Debug.WriteLine(IpAddress);

            IpAddress = System.Text.RegularExpressions.Regex.Replace(IpAddress, "0*([0-9]+)", "${1}");

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

        #endregion Private Methods

    }
}
