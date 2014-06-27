using System;
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

        public PrintSettingOptionsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
        }

        public PrintSetting PrintSetting
        {
            get { return _printSetting; }
            set
            {
                if (_printSetting != value)
                {
                    _printSetting = value;
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
                    _selectPrintSettingOption = new RelayCommand<int>(
                        (selectedIndex) => SelectPrintSettingOptionExecute(selectedIndex),
                        (selectedIndex) => true
                    );
                }
                return _selectPrintSettingOption;
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

        private void SelectPrintSettingOptionExecute(int index)
        {
            PrintSetting.Value = index;
        }

        private void BackToPrintSettingsExecute()
        {
            new ViewModelLocator().PrintSettingsPaneViewModel.PrintSettingsPaneMode = PrintSettingsPaneMode.PrintSettings;
            PrintSetting = null; // Reset PrintSetting on back so that bindings will refresh on re-open
        }
    }

}
