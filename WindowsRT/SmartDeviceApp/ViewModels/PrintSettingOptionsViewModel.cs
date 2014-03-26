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

namespace SmartDeviceApp.ViewModels
{
    public class PrintSettingOptionsViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _backToPrintSettings;
        private PrintSetting _printSetting;
        private PrintSettingOption _selectedPrintSettingOption;

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
                    RaisePropertyChanged("PrintSetting");
                }
            }
        }

        public PrintSettingOption SelectedPrintSettingOption
        {
            get { return _selectedPrintSettingOption; }
            set
            {
                if (_selectedPrintSettingOption != value)
                {
                    _selectedPrintSettingOption = value;
                    RaisePropertyChanged("SelectedPrintSettingOption");
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

        private void BackToPrintSettingsExecute()
        {
            Messenger.Default.Send<RightPaneMode>(RightPaneMode.PrintSettingsVisible);  
        }
    }

}
