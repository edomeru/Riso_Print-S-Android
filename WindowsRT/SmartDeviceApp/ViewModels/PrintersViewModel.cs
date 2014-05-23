﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Controllers;
using System.Collections.ObjectModel;
using System.Windows.Input;
using SmartDeviceApp.Common;
using GalaSoft.MvvmLight.Messaging;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using System.ComponentModel;
using System.Diagnostics;
using GalaSoft.MvvmLight.Command;
using Windows.UI.Xaml;
using System.Windows.Input;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;

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

        private ObservableCollection<Printer> _printerList;

        /**
         * 
         * Delegates for controllers
         * 
         * */
        public event SmartDeviceApp.Controllers.PrinterController.OnNavigateToEventHandler OnNavigateToEventHandler;
        public event SmartDeviceApp.Controllers.PrinterController.OnNavigateFromEventHandler OnNavigateFromEventHandler;
        public event SmartDeviceApp.Controllers.PrinterController.PollingHandler PollingHandler;

        public event SmartDeviceApp.Controllers.PrinterController.DeletePrinterHandler DeletePrinterHandler;

        public void OnNavigatedTo()
        {
            if (OnNavigateToEventHandler != null)
            {
                OnNavigateToEventHandler();
            }
        }

        public void OnNavigatedFrom()
        {
            if (OnNavigateFromEventHandler != null)
            {
                OnNavigateFromEventHandler();
            }
        }

        public ObservableCollection<Printer> PrinterList
        {
            get{ return this._printerList; }
            set
            {
                _printerList = value;
                OnPropertyChanged("PrinterList");
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }


        public PrintersViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            Messenger.Default.Register<VisibleRightPane>(this, (visibleRightPane) => SetRightPaneMode(visibleRightPane));
            Messenger.Default.Register<ViewMode>(this, (viewMode) => EnableMode(viewMode));
            Messenger.Default.Register<ScreenMode>(this, (screenMode) => ScreenModeChanged(screenMode));
            Messenger.Default.Register<string>(this, (tapped) => GridTapped(tapped));
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

        private void GridTapped(string tapped)
        {
            //if (tapped == "ClearDelete")
            //{
            //    int i = 0;
            //    while (i < PrinterList.Count)
            //    {
            //        Printer printer = PrinterList.ElementAt(i);
            //        printer.WillBeDeleted = false;
            //        i++;
            //    } 
            //}
        }


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
        private string _printerToBeDeleted;
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
                Printer printer = PrinterList.First(x => x.IpAddress == _printerToBeDeleted);
                printer.WillBeDeleted = false;
            }
            
        }

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

        public event SmartDeviceApp.Controllers.PrinterController.OpenDefaultPrintSettingsHandler OpenDefaultPrintSettingsHandler;

        private void OpenDefaultPrinterSettingsExecute(Printer printer)
        {
            //use visual state.
            //get default printer settings using ip
            System.Diagnostics.Debug.WriteLine("OpenDefaultPrinterSettingsExecute");

            var _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            
            _viewControlViewModel.ViewMode = Common.Enum.ViewMode.RightPaneVisible;
            
            _viewControlViewModel.TapHandled = true;
            RightPaneMode = Common.Enum.PrintersRightPaneMode.PrintSettings;
            _viewControlViewModel.IsPane1Visible = true; // Note: Need to set this so that pane will be closed when pane buttons are toggled
            _gestureController.DisableGestures();
            
            OpenDefaultPrintSettingsHandler(printer);
            
        }

        private PrintersGestureController _gestureController;
        public PrintersGestureController GestureController
        {
            get { return _gestureController; }
            set { _gestureController = value; }
        }
    }

}
