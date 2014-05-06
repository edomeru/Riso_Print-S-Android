//
//  PrintSettingsViewModel.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/20.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading.Tasks;
using System.Xml.Serialization;
using System.Xml.Linq;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.ApplicationModel;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Xaml;
using System.Collections.ObjectModel;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using Windows.UI.Xaml.Data;
using System.ComponentModel;

namespace SmartDeviceApp.ViewModels
{
    public class PrintSettingsViewModel : ViewModelBase
    {
        public event SmartDeviceApp.Controllers.PrintPreviewController.PrintEventHandler ExecutePrintEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private string _printerName;
        private string _printerIpAddress;
        private ICommand _printCommand;
        private ICommand _listPrintersCommand;

        private PrintSettingList _printSettingsList;
        private ICommand _selectPrintSetting;
        private PrintSetting _selectedPrintSetting;

        private string _authenticationPinCode;

        public PrintSettingsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
        }

        public int PrinterId { get; set; }

        public string PrinterName
        {
            get { return _printerName; }
            set
            {
                if (_printerName != value)
                {
                    _printerName = value;
                    RaisePropertyChanged("PrinterName");
                }
            }
        }

        public string PrinterIpAddress
        {
            get { return _printerIpAddress; }
            set
            {
                if (_printerIpAddress != value)
                {
                    _printerIpAddress = value;
                    RaisePropertyChanged("PrinterIpAddress");
                }
            }
        }

        public ICommand PrintCommand
        {
            get
            {
                if (_printCommand == null)
                {
                    _printCommand = new RelayCommand(
                        () => PrintExecute(),
                        () => true
                    );
                }
                return _printCommand;
            }
        }

        public ICommand ListPrintersCommand
        {
            get
            {
                if (_listPrintersCommand == null)
                {
                    _listPrintersCommand = new RelayCommand(
                        () => ListPrintersCommandExecute(),
                        () => true
                    );
                }
                return _listPrintersCommand;
            }
        }

        public PrintSettingList PrintSettingsList
        {
            get { return _printSettingsList; }
            set
            {
                if (_printSettingsList != value)
                {
                    _printSettingsList = value;
                    RaisePropertyChanged("PrintSettingsList");
                }
            }
        }

        public PrintSetting SelectedPrintSetting
        {
            get { return _selectedPrintSetting; }
            set
            {
                if (_selectedPrintSetting != value)
                {
                    _selectedPrintSetting = value;
                    RaisePropertyChanged("SelectedPrintSetting");
                }
            }
        }

        public ICommand SelectPrintSetting
        {
            get
            {
                if (_selectPrintSetting == null)
                {
                    _selectPrintSetting = new RelayCommand<PrintSetting>(
                        (printSetting) => SelectPrintSettingExecute(printSetting),
                        (printSetting) => true
                    );
                }
                return _selectPrintSetting;
            }
        }

        public string AuthenticationLoginPinCode
        {
            get { return _authenticationPinCode; }
            set
            {
                if (_authenticationPinCode != value)
                {
                    _authenticationPinCode = value;
                    RaisePropertyChanged("AuthenticationLoginPinCode");
                }
            }
        }

        private void PrintExecute()
        {
            if (ExecutePrintEventHandler != null)
            {
                ExecutePrintEventHandler();
            }
        }

        private void ListPrintersCommandExecute()
        {
            new ViewModelLocator().SelectPrinterViewModel.SelectedPrinterId = PrinterId;
            new ViewModelLocator().PrintSettingsPaneViewModel.PrintSettingsPaneMode = PrintSettingsPaneMode.SelectPrinter;
        }

        private void SelectPrintSettingExecute(PrintSetting printSetting)
        {
            switch (printSetting.Type)
            {
                case PrintSettingType.boolean:

                    break;
                case PrintSettingType.numeric:
                    break;

                case PrintSettingType.list:
                    new ViewModelLocator().PrintSettingOptionsViewModel.PrintSetting = printSetting;
                    new ViewModelLocator().PrintSettingsPaneViewModel.PrintSettingsPaneMode = PrintSettingsPaneMode.PrintSettingOptions;
                    SelectedPrintSetting = printSetting;
                    break;
                case PrintSettingType.unknown:
                    break;
            }
        }
    }
}