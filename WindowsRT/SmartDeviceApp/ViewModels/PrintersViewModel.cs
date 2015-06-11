using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections.ObjectModel;
using System.Windows.Input;
using System.ComponentModel;
using System.Diagnostics;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Xaml.Controls;
using Windows.Foundation;
using GalaSoft.MvvmLight.Messaging;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Views;
using SmartDeviceApp.Converters;


namespace SmartDeviceApp.ViewModels
{
    public class PrintersViewModel : ViewModelBase, INotifyPropertyChanged
    {
        //contains data that binds with PrintersPage
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;
        
        private PrintersRightPaneMode _rightPaneMode;


        private ICommand _deletePrinter;
        private ICommand _openDefaultPrinterSettings;
        private string _printerToBeDeleted;

        private ObservableCollection<Printer> _printerList;
        private bool _isPrinterListEmpty;

        private PrintersGestureController _gestureController;

        /**
         * 
         * Delegates for controllers
         * 
         * */

        /// <summary>
        /// Transition to Printers Screen event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.OnNavigateToEventHandler OnNavigateToEventHandler;

        /// <summary>
        /// Transition from Printers Screen event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.OnNavigateFromEventHandler OnNavigateFromEventHandler;

        /// <summary>
        /// Printer status polling event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.PollingHandler PollingHandler;

        /// <summary>
        /// Delete printer event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.DeletePrinterHandler DeletePrinterHandler;

        /// <summary>
        /// Open Default Print Settings Screen event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.OpenDefaultPrintSettingsHandler OpenDefaultPrintSettingsHandler;

        #region Properties
        /// <summary>
        /// Navigation handler.
        /// </summary>
        public void OnNavigatedTo()
        {
            if (OnNavigateToEventHandler != null)
            {
                OnNavigateToEventHandler();
            }
        }

        /// <summary>
        /// Navigation handler.
        /// </summary>
        public void OnNavigatedFrom()
        {
            if (OnNavigateFromEventHandler != null)
            {
                OnNavigateFromEventHandler();
            }
        }

        /// <summary>
        /// Contains the list of saved printers.
        /// </summary>
        public ObservableCollection<Printer> PrinterList
        {
            get{ return this._printerList; }
            set
            {
                _printerList = value;
                OnPropertyChanged("PrinterList");
                
            }
        }

        /// <summary>
        /// Flag for checking if the list of printers is empty
        /// </summary>
        public bool IsPrinterListEmpty
        {
            get { return _isPrinterListEmpty; }
            set
            {
                if (_isPrinterListEmpty != value)
                {
                    _isPrinterListEmpty = value;
                    OnPropertyChanged("IsPrinterListEmpty");
                }
            }
        }

        /// <summary>
        /// Gesture controller for Printers screen.
        /// </summary>
        public PrintersGestureController GestureController
        {
            get { return _gestureController; }
            set { _gestureController = value; }
        }

        /// <summary>
        /// Command to be executed when default print settings button is tapped.
        /// </summary>
        public ICommand OpenDefaultPrinterSettings
        {
            get
            {
                if (_openDefaultPrinterSettings == null)
                {
                    _openDefaultPrinterSettings = new RelayCommand<Printer>(
                        (printer) => OpenDefaultPrinterSettingsExecute(printer),
                        (printer) => true
                        );
                }
                return _openDefaultPrinterSettings;
            }
        }

        /// <summary>
        /// Command that will be executed when a printer will be deleted.
        /// </summary>
        public ICommand DeletePrinter
        {
            get
            {
                if (_deletePrinter == null)
                {
                    _deletePrinter = new RelayCommand<string>(
                        (ip) => DeletePrinterExecute(ip),
                        (ip) => true
                    );
                }
                return _deletePrinter;
            }
        }

        /// <summary>
        /// Holds the mode of the right pane: Add Printer/Search Printer/Default Print Settings mode.
        /// </summary>
        public PrintersRightPaneMode RightPaneMode
        {
            get { return _rightPaneMode; }
            set
            {
                if (_rightPaneMode != value)
                {
                    _rightPaneMode = value;
                    OnPropertyChanged("RightPaneMode");

                }
            }
        }

        #endregion Properties

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
        /// Constructor for PrintersViewModel. Registers Messenger event handlers.
        /// </summary>
        /// <param name="dataService"></param>
        /// <param name="navigationService"></param>
        public PrintersViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            Messenger.Default.Register<VisibleRightPane>(this, (visibleRightPane) => SetRightPaneMode(visibleRightPane));
            Messenger.Default.Register<ViewMode>(this, (viewMode) => EnableMode(viewMode));
            Messenger.Default.Register<ScreenMode>(this, (screenMode) => ScreenModeChanged(screenMode));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetPrinterInfoGrid(viewOrientation));            
        }

        #region Private Methods

        private void ResetPrinterInfoGrid(ViewOrientation viewOrientation)
        {
            if (GestureController != null && GestureController.TargetControl != null)
            {
                var columns = (viewOrientation == Common.Enum.ViewOrientation.Landscape) ? 3 : 2;
                var _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
                var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];

                var targetControl = (AdaptableGridView)GestureController.TargetControl;
                var converter = new PrintersListWidthConverter();
                var viewMode = _viewControlViewModel.ViewMode;
                var param = new ViewItemParameters() { columns = columns, viewOrientation = viewOrientation };
                targetControl.ItemWidth = (double) converter.Convert(viewMode, null,param , null);
            }
        }

        private void ScreenModeChanged(Common.Enum.ScreenMode screenMode)
        {
            if(screenMode != Common.Enum.ScreenMode.Printers)
            {
                if (PollingHandler != null)
                    PollingHandler(false);
            }
        }

        private void EnableMode(Common.Enum.ViewMode viewMode)
        {
            
            if (_gestureController != null)
            {
                var viewControlVM = new ViewModelLocator().ViewControlViewModel;
                if (viewControlVM.ScreenMode == Common.Enum.ScreenMode.Printers)
                { 
                    if (viewMode == Common.Enum.ViewMode.FullScreen)
                    {
                        _gestureController.EnableGestures();
                        foreach(Printer p in PrinterList)
                        {
                            p.VisualState = "Normal";
                        }
                        

                        //start polling
                        if (PollingHandler != null)
                            PollingHandler(true);
                    }
                    else
                    {
                        //end polling
                        if (PollingHandler != null)
                            PollingHandler(false);
                        _gestureController.DisableGestures();
                    }
                }
            }
            
        }
        

        private void SetRightPaneMode(VisibleRightPane visibleRightPane)
        {
            var viewControlVM = new ViewModelLocator().ViewControlViewModel;
            if (viewControlVM.ScreenMode == Common.Enum.ScreenMode.Printers)
            {
                switch (visibleRightPane)
                {
                    case VisibleRightPane.Pane1:
                        RightPaneMode = PrintersRightPaneMode.SearchPrinter;
                        break;
                    case VisibleRightPane.Pane2:
                        RightPaneMode = PrintersRightPaneMode.AddPrinter;
                        break;
                }
            }
        }

        private async Task DeletePrinterExecute(string ipAddress)
        {
            _printerToBeDeleted = ipAddress;
            await DialogService.Instance.ShowMessage("IDS_INFO_MSG_DELETE_JOBS", "IDS_LBL_PRINTERS", "IDS_LBL_OK", "IDS_LBL_CANCEL", new Action<bool>(DeletePrinterFromDB));
        }

        private void DeletePrinterFromDB(bool isOk)
        {

            if (isOk)
            {
                if (_printerToBeDeleted != "")
                {
                    DeletePrinterHandler(_printerToBeDeleted);
                    _printerToBeDeleted = "";
                }
            }
            else
            {
                Printer printer = PrinterList.FirstOrDefault(x => x.IpAddress == _printerToBeDeleted);
                if (printer != null)
                    printer.WillBeDeleted = false;
            }   
        }

        private void OpenDefaultPrinterSettingsExecute(Printer printer)
        {
            //use visual state.
            //get default printer settings using ip
            System.Diagnostics.Debug.WriteLine("OpenDefaultPrinterSettingsExecute");
            printer.VisualState = "Pressed";
            var _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            
            _viewControlViewModel.ViewMode = Common.Enum.ViewMode.RightPaneVisible;
            
            _viewControlViewModel.TapHandled = true;
            RightPaneMode = Common.Enum.PrintersRightPaneMode.PrintSettings;
            _viewControlViewModel.IsPane1Visible = true; // Note: Need to set this so that pane will be closed when pane buttons are toggled
            _gestureController.DisableGestures();
            
            OpenDefaultPrintSettingsHandler(printer);

        }

        #endregion Private Methods

    }

}
