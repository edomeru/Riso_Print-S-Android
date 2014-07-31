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
using Windows.UI.Xaml;
using SmartDeviceApp.Converters;

namespace SmartDeviceApp.ViewModels
{
    public class PrintSettingOptionsViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _selectPrintSettingOption;
        private ICommand _backToPrintSettings;
        private PrintSetting _printSetting;

        private double _height;

        /// <summary>
        /// PrintSettingOptionsViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public PrintSettingOptionsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetPrinSettingsPane(viewOrientation));
        }

        /// <summary>
        /// PrintSetting property.
        /// Denotes the currently active print setting
        /// </summary>
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

        /// <summary>
        /// Command for selected print setting option
        /// </summary>
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

        /// <summary>
        /// Select print setting option handler
        /// </summary>
        /// <param name="index">print setting index/identifier</param>
        private void SelectPrintSettingOptionExecute(int index)
        {
            PrintSetting.Value = index;
        }

        /// <summary>
        /// Navigate back to Print Settings Screen handler
        /// </summary>
        private void BackToPrintSettingsExecute()
        {
            new ViewModelLocator().PrintSettingsPaneViewModel.PrintSettingsPaneMode = PrintSettingsPaneMode.PrintSettings;
            PrintSetting = null; // Reset PrintSetting on back so that bindings will refresh on re-open
        }

        /// <summary>
        /// Holds the value for the height of the PrintSettings pane.
        /// </summary>
        public double Height
        {
            get { return this._height; }
            set
            {
                _height = value;
                RaisePropertyChanged("Height");

            }
        }


        private void ResetPrinSettingsPane(ViewOrientation viewOrientation)
        {
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            Height = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null)) - titleHeight;
        }
    }

}
