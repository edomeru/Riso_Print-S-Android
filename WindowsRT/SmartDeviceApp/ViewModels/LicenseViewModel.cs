using System;
using System.Windows.Input;
using Windows.UI.Xaml.Controls;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;


namespace SmartDeviceApp.ViewModels
{
    public class LicenseViewModel : ViewModelBase
    {
        public event SettingController.SetLicenseAgreedEventHandler SetLicenseAgreedEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _licenseAgreeCommand;
        private ICommand _licenseDisagreeCommand;

        /// <summary>
        /// LicenseViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService"></param>
        public LicenseViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => EnableMode(viewMode));
        }

        /// <summary>
        /// Command for license agree
        /// </summary>
        public ICommand LicenseAgreeCommand
        {
            get
            {
                if (_licenseAgreeCommand == null)
                {
                    _licenseAgreeCommand = new RelayCommand(
                        () => LicenseAgreeExecute(),
                        () => true
                    );
                }
                return _licenseAgreeCommand;
            }
        }

        /// <summary>
        /// Command for license disagree
        /// </summary>
        public ICommand LicenseDisagreeCommand
        {
            get
            {
                if (_licenseDisagreeCommand == null)
                {
                    _licenseDisagreeCommand = new RelayCommand(
                        () => LicenseDisagreeExecute(),
                        () => true
                    );
                }
                return _licenseDisagreeCommand;
            }
        }

        private void EnableMode(ViewMode viewMode)
        {
            if (viewMode == ViewMode.FullScreen)
            {
                if (LicenseGestureGrid != null)
                {
                    LicenseGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Collapsed;
                }
            }
            else
            {
                if (LicenseGestureGrid != null)
                {
                    LicenseGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Visible;
                }
            }
        }

        /// <summary>
        /// Grid control for enabling/disabling gestures on License Screen
        /// </summary>
        public Grid LicenseGestureGrid
        {
            get;
            set;
        }

        private void LicenseAgreeExecute()
        {
            SetLicenseAgreed();
            new ViewModelLocator().ViewControlViewModel.GoToHomePage.Execute(null);
        }

        private async void LicenseDisagreeExecute()
        {
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    await DialogService.Instance.ShowError("IDS_ERR_MSG_LICENSE_DISAGREE",
                        "IDS_LBL_LICENSE", "IDS_LBL_OK", null);
                });
        }

        /// <summary>
        /// License agreement value changed event handler
        /// </summary>
        private void SetLicenseAgreed()
        {
            if (SetLicenseAgreedEventHandler != null)
            {
                SetLicenseAgreedEventHandler();
            }
        }
    }
}