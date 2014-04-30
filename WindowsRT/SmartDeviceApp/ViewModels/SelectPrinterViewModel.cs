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
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace SmartDeviceApp.ViewModels
{
    public class SelectPrinterViewModel : ViewModelBase
    {
        public event SmartDeviceApp.Controllers.PrintPreviewController.SelectedPrinterChangedEventHandler SelectPrinterEvent;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _selectPrinter;
        private ICommand _backToPrintSettings;

        private List<Printer> _printers;
        private int _selectedPrinterId;

        public SelectPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
        }

        public List<Printer> Printers
        {
            get { return _printers; }
            set
            {
                if (_printers != value)
                {
                    _printers = value;
                    RaisePropertyChanged("Printers");
                }
            }
        }

        public int SelectedPrinterId
        {
            get { return _selectedPrinterId; }
            set
            {
                if (_selectedPrinterId != value)
                {
                    _selectedPrinterId = value;
                    RaisePropertyChanged("SelectedPrinterId");
                }
            }
        }

        public ICommand SelectPrinter
        {
            get
            {
                if (_selectPrinter == null)
                {
                    _selectPrinter = new RelayCommand<Printer>(
                        (prn) => SelectPrinterExecute(prn.Id),
                        (prn) => true
                    );
                }
                return _selectPrinter;
            }
        }

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

        private void SelectPrinterExecute(int id)
        {
            if (SelectPrinterEvent != null)
            {
                SelectPrinterEvent(id);
            }
        }

        private void BackToPrintSettingsExecute()
        {
            new ViewModelLocator().PrintSettingsPaneViewModel.PrintSettingsPaneMode = PrintSettingsPaneMode.PrintSettings;
        }

    }
}
