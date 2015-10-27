//
//  SelectPrinterViewModel.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/29.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
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
    public class SelectPrinterViewModel : ViewModelBase
    {
        /// <summary>
        /// Select printer event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrintPreviewController.SelectedPrinterChangedEventHandler SelectPrinterEvent;

        /// <summary>
        /// Printer status polling event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrinterController.PollingHandler PollingHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _selectPrinter;
        private ICommand _backToPrintSettings;

        private ObservableCollection<Printer> _printerList;
        private bool _isPrinterListEmpty;

        /// <summary>
        /// SelectPrinterViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public SelectPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            Messenger.Default.Register<PrintSettingsPaneMode>(this, (printSettingsPaneMode) => StartPolling(printSettingsPaneMode));
        }

        /// <summary>
        /// Gets/sets the printer list
        /// </summary>
        public ObservableCollection<Printer> PrinterList
        {
            get { return _printerList; }
            set
            {
                if (_printerList != value)
                {
                    _printerList = value;
                    RaisePropertyChanged("PrinterList");
                    CheckPrinterListEmpty();
                }
            }
        }

        /// <summary>
        /// Gets/sets the flag when the printer list is empty.
        /// True when printer list is empty, false otherwise.
        /// </summary>
        public bool IsPrinterListEmpty
        {
            get { return _isPrinterListEmpty; }
            set
            {
                if (_isPrinterListEmpty != value)
                {
                    _isPrinterListEmpty = value;
                    RaisePropertyChanged("IsPrinterListEmpty");
                }
            }
        }

        /// <summary>
        /// Command for select printer
        /// </summary>
        public ICommand SelectPrinter
        {
            get
            {
                if (_selectPrinter == null)
                {
                    _selectPrinter = new RelayCommand<int>(
                        (prnId) => SelectPrinterExecute(prnId),
                        (prnId) => true
                    );
                }
                return _selectPrinter;
            }
        }

        /// <summary>
        /// Command to navigate back to Print Settings Screen
        /// </summary>
        public ICommand BackToPrintSettings
        {
            get
            {
                if (_backToPrintSettings == null)
                {
                    _backToPrintSettings = new RelayCommand(
                        () => BackToPrintSettingsExecute(),
                        () => true
                    );
                }
                return _backToPrintSettings;
            }
        }

        private void CheckPrinterListEmpty()
        {
            if (_printerList != null && _printerList.Count == 0)
            {
                IsPrinterListEmpty = true;
            }
            else
            {
                IsPrinterListEmpty = false;
            }
        }

        private void SelectPrinterExecute(int id)
        {
            if (SelectPrinterEvent != null)
            {
                SelectPrinterEvent(id);
            }
        }

        private void StartPolling(PrintSettingsPaneMode printSettingsPaneMode)
        {
            if (printSettingsPaneMode == PrintSettingsPaneMode.SelectPrinter
                && PollingHandler != null)
            {
                PollingHandler(true);
            }
        }

        private void BackToPrintSettingsExecute()
        {
            if (PollingHandler != null)
            {
                PollingHandler(false);
            }
            new ViewModelLocator().PrintSettingsPaneViewModel.PrintSettingsPaneMode = PrintSettingsPaneMode.PrintSettings;
            PrinterList = null; // Reset PrinterList on back so that bindings will refresh on re-open
        }

    }
}
