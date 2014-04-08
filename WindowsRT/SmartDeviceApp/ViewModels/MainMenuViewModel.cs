using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Collections.ObjectModel;
using Windows.UI.Xaml;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.ViewModels
{
    public class MainMenuViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private AppViewModel _appViewModel;
        private ICommand _goToHomePage;
        private ICommand _goToPreviewPage;
        private ICommand _goToPrintersPage;
        private ICommand _goToJobsPage;
        private ICommand _goToSettingsPage;
        private ICommand _goToHelpPage;
        private ICommand _goToLegalPage;

        private MainMenuItemList _mainMenuItems;

        private const string TITLE = "IDS_APP_NAME";

        public MainMenuViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            _appViewModel = new ViewModelLocator().AppViewModel;

            // Instantiate menu items:
            MainMenuItems = new MainMenuItemList();
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_HOME", GoToHomePage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_PRINTERS", GoToPrintersPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_JOBS", GoToJobsPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_SETTINGS", GoToSettingsPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_HELP", GoToHelpPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_LEGAL", GoToLegalPage, Visibility.Collapsed));
        }

        public string Title
        {
            get { return TITLE; }
        }

        public MainMenuItemList MainMenuItems
        {
            get { return _mainMenuItems; }
            set
            {
                if (_mainMenuItems != value)
                {
                    _mainMenuItems = value;
                    RaisePropertyChanged("MainMenuItems");
                }
            }
        }

        public ICommand GoToHomePage
        {
            get
            {
                if (_goToHomePage == null)
                {
                    _goToHomePage = new RelayCommand(
                        () => { _appViewModel.AppViewMode = AppViewMode.HomePageFullScreen; },
                        () => true
                    );
                }
                return _goToHomePage;
            }
        }

        public ICommand GoToPreviewPage
        {
            get
            {
                if (_goToPreviewPage == null)
                {
                    _goToPreviewPage = new RelayCommand(
                        () => { _appViewModel.AppViewMode = AppViewMode.PrintPreviewPageFullScreen; },
                        () => true
                    );
                }
                return _goToPreviewPage;
            }
        }

        public ICommand GoToPrintersPage
        {
            get
            {
                if (_goToPrintersPage == null)
                {
                    _goToPrintersPage = new RelayCommand(
                        () => { _appViewModel.AppViewMode = AppViewMode.PrintersPageFullScreen; },
                        () => true
                    );
                }
                return _goToPrintersPage;
            }
        }

        public ICommand GoToJobsPage
        {
            get
            {
                if (_goToJobsPage == null)
                {
                    _goToJobsPage = new RelayCommand(
                        () => { _appViewModel.AppViewMode = AppViewMode.JobsPageFullScreen; },
                        () => true
                    );
                }
                return _goToJobsPage;
            }
        }

        public ICommand GoToSettingsPage
        {
            get
            {
                if (_goToSettingsPage == null)
                {
                    _goToSettingsPage = new RelayCommand(
                        () => { _appViewModel.AppViewMode = AppViewMode.SettingsPageFullScreen; },
                        () => true
                    );
                }
                return _goToSettingsPage;
            }
        }

        public ICommand GoToHelpPage
        {
            get
            {
                if (_goToHelpPage == null)
                {
                    _goToHelpPage = new RelayCommand(
                        () => { _appViewModel.AppViewMode = AppViewMode.HelpPageFullScreen; },
                        () => true
                    );
                }
                return _goToHelpPage;
            }
        }

        public ICommand GoToLegalPage
        {
            get
            {
                if (_goToLegalPage == null)
                {
                    _goToLegalPage = new RelayCommand(
                        () => { _appViewModel.AppViewMode = AppViewMode.LegalPageFullScreen; },
                        () => true
                    );
                }
                return _goToLegalPage;
            }
        }
    }

    
}
