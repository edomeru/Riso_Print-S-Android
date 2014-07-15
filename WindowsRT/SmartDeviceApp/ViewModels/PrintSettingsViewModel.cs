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
        /// <summary>
        /// Print button event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrintPreviewController.PrintEventHandler ExecutePrintEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private int _printerId;
        private string _printerName;
        private string _printerIpAddress;
        private bool _isPrintPreview;
        private ICommand _printCommand;
        private ICommand _listPrintersCommand;

        private PrintSettingList _printSettingsList;
        private ICommand _selectPrintSetting;
        private PrintSetting _selectedPrintSetting;

        /// <summary>
        /// PrintSettingsViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public PrintSettingsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
        }

        /// <summary>
        /// Gets/sets the printer ID of the currently selected printer
        /// </summary>
        public int PrinterId
        {
            get { return _printerId; }
            set
            {
                if (_printerId != value)
                {
                    _printerId = value;
                    RaisePropertyChanged("PrinterId");
                }
            }
        }

        /// <summary>
        /// Gets/sets the text label based on the selected printer
        /// </summary>
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

        /// <summary>
        /// Gets/sets the sub-text label based on the IP address of the selected printer
        /// </summary>
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

        /// <summary>
        /// Gets/sets if the currently displayed view is associated with Print Preview Screen.
        /// True when Print Preview Screen is active, false otherwise (should be Default Print Settings)
        /// </summary>
        public bool IsPrintPreview
        {
            get { return _isPrintPreview; }
            set
            {
                if (_isPrintPreview != value)
                {
                    _isPrintPreview = value;
                    RaisePropertyChanged("IsPrintPreview");
                }
            }
        }

        /// <summary>
        /// Command for Print button
        /// </summary>
        public ICommand PrintCommand
        {
            get
            {
                if (_printCommand == null)
                {
                    _printCommand = new RelayCommand(
                        () => PrintExecute(),
                        () => _isPrintPreview
                    );
                }
                return _printCommand;
            }
        }

        /// <summary>
        /// Command for printers list
        /// </summary>
        public ICommand ListPrintersCommand
        {
            get
            {
                if (_listPrintersCommand == null)
                {
                    _listPrintersCommand = new RelayCommand(
                        () => ListPrintersCommandExecute(),
                        () => _isPrintPreview
                    );
                }
                return _listPrintersCommand;
            }
        }

        /// <summary>
        /// Gets/sets the printer list
        /// </summary>
        public ObservableCollection<Printer> PrinterList { get; set; }

        /// <summary>
        /// Gets/sets the print settings list
        /// </summary>
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

        /// <summary>
        /// Gets/sets the selected print setting
        /// </summary>
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

        /// <summary>
        /// Command for the selected print setting
        /// </summary>
        public ICommand SelectPrintSetting
        {
            get
            {
                if (_selectPrintSetting == null)
                {
                    _selectPrintSetting = new RelayCommand<PrintSetting>(
                        (printSetting) => SelectPrintSettingExecute(printSetting),
                        (printSetting) => printSetting != null
                    );
                }
                return _selectPrintSetting;
            }
        }

        /// <summary>
        /// Enables the PrintSettingsPane
        /// </summary>
        public void SetPrintSettingsPaneEnable()
        {
            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
            var printSettingsPaneViewModel = new ViewModelLocator().PrintSettingsPaneViewModel;
            if (PrinterList == null || PrinterList.Count == 0)
            {
                printSettingsPaneViewModel.IsEnabled = false;
            }
            else printSettingsPaneViewModel.IsEnabled = true;
        }

        private void PrintExecute()
        {
            if (_printerId == -1)
            {
                DialogService.Instance.ShowError("IDS_ERR_MSG_NO_SELECTED_PRINTER", "IDS_APP_NAME", "IDS_LBL_OK", null);
                return;
            }
            if (ExecutePrintEventHandler != null)
            {
                ExecutePrintEventHandler();
            }
        }

        private void ListPrintersCommandExecute()
        {
            new ViewModelLocator().SelectPrinterViewModel.PrinterList = PrinterList; // Set printer list
            new ViewModelLocator().PrintSettingsPaneViewModel.PrintSettingsPaneMode = PrintSettingsPaneMode.SelectPrinter;

            Messenger.Default.Send<PrintSettingsPaneMode>(PrintSettingsPaneMode.SelectPrinter);
        }

        private void SelectPrintSettingExecute(PrintSetting printSetting)
        {
            switch (printSetting.Type)
            {
                case PrintSettingType.boolean:
                    break;
                case PrintSettingType.numeric:
                    break;
                case PrintSettingType.password:
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