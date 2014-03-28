﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.ViewModels
{
    public class PrintSettingOptionsViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _selectPrintSettingOption;
        private ICommand _backToPrintSettings;
        private PrintSetting _printSetting;
        private int _selectedIndex;

        public PrintSettingOptionsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Messenger.Default.Register<PrintSetting>(this, (printSetting) => OnSelectPrintSetting(printSetting));
        }

        public PrintSetting PrintSetting
        {
            get { return _printSetting; }
            set
            {
                if (_printSetting != value)
                {
                    _printSetting = value;
                    SelectedIndex = (int)_printSetting.Value;
                    RaisePropertyChanged("PrintSetting");
                }
            }
        }

        public ICommand SelectPrintSettingOption
        {
            get
            {
                if (_selectPrintSettingOption == null)
                {
                    _selectPrintSettingOption = new RelayCommand<PrintSettingOption>(
                        (option) => SelectPrintSettingOptionExecute(option),
                        (option) => true
                    );
                }
                return _selectPrintSettingOption;
            }
        }

        public int SelectedIndex
        {
            get { return _selectedIndex; }
            set
            {
                if (_selectedIndex != value)
                {
                    _selectedIndex = value;
                    RaisePropertyChanged("SelectedIndex");
                }
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

        private void OnSelectPrintSetting(PrintSetting printSetting)
        {
            PrintSetting = printSetting;
        }

        private void SelectPrintSettingOptionExecute(PrintSettingOption option)
        {
            SelectedIndex = option.Index;

            // TODO: Send selected item to PrintPreviewController
            //PrintPreviewController.Instance.UpdatePrintSetting(PrintSetting,
            //    SelectedPrintSettingOption);
        }

        private void BackToPrintSettingsExecute()
        {
            Messenger.Default.Send<RightPaneMode>(RightPaneMode.PrintSettingsVisible);  
        }
    }

}
