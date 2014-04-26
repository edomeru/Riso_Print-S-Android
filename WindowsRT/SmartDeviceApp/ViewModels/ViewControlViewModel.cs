using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Collections.ObjectModel;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Views;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.ViewModels
{
    public class ViewControlViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _toggleMainMenuPane;
        private ICommand _togglePane1;
        private ICommand _togglePane2;
        private ViewMode _viewMode;
        private ScreenMode _screenMode;
        private bool _isPane1Visible = false;
        private bool _isPane2Visible = false;

        public ViewControlViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            ViewMode = ViewMode.FullScreen;
            ScreenMode = ScreenMode.PrintPreview;
            InitializeMainMenu();
        }

        public ViewMode ViewMode
        {
            get { return _viewMode; }
            set
            {
                if (_viewMode != value)
                {
                    _viewMode = value;
                    RaisePropertyChanged("ViewMode");
                    Messenger.Default.Send<ViewMode>(_viewMode); // Broadcast to all viewmodels that need to be updated
                }
            }
        }

        public ScreenMode ScreenMode
        {
            get { return _screenMode; }
            set
            {
                if (_screenMode != value)
                {
                    _screenMode = value;
                    RaisePropertyChanged("ScreenMode");
                }
            }
        }

        public ICommand ToggleMainMenuPane
        {
            get
            {
                if (_toggleMainMenuPane == null)
                {
                    _toggleMainMenuPane = new RelayCommand(
                        () => ToggleMainMenuPaneExecute(),
                        () => true
                    );
                }
                return _toggleMainMenuPane;
            }
        }

        public ICommand TogglePane1
        {
            get
            {
                if (_togglePane1 == null)
                {
                    _togglePane1 = new RelayCommand(
                        () => TogglePane1Execute(),
                        () => true
                    );
                }
                return _togglePane1;
            }
        }

        public ICommand TogglePane2
        {
            get
            {
                if (_togglePane2 == null)
                {
                    _togglePane2 = new RelayCommand(
                        () => TogglePane2Execute(),
                        () => true
                    );
                }
                return _togglePane2;
            }
        }

        public bool IsPane1Visible
        {
            get { return _isPane1Visible; }
            set { _isPane1Visible = value; }
        }

        public bool IsPane2Visible
        {
            get { return _isPane2Visible; }
            set { _isPane2Visible = value; }
        }

        private void ToggleMainMenuPaneExecute()
        {
            switch (ViewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        ViewMode = ViewMode.FullScreen;
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        ViewMode = ViewMode.MainMenuPaneVisible;
                        break;
                    }

                case ViewMode.RightPaneVisible:
                case ViewMode.RightPaneVisible_ResizedWidth:
                    {
                        // Close right pane first then open main menu pane
                        ViewMode = ViewMode.FullScreen;
                        ViewMode = ViewMode.MainMenuPaneVisible;
                        break;
                    }
            }
        }

        private void TogglePane1Execute()
        {
            switch (ViewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        // NOTE: Technically, this is not possible
                        // Close main menu pane first then open right pane
                        ViewMode = ViewMode.FullScreen;
                        if (ScreenMode == ScreenMode.PrintPreview) ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                        else ViewMode = ViewMode.RightPaneVisible;
                        _isPane1Visible = true;
                        Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane1);
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        if (ScreenMode == ScreenMode.PrintPreview) ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                        else ViewMode = ViewMode.RightPaneVisible;
                        _isPane1Visible = true;
                        Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane1);
                        break;
                    }

                case ViewMode.RightPaneVisible:
                case ViewMode.RightPaneVisible_ResizedWidth:
                    {
                        if (_isPane1Visible)
                        {
                            ViewMode = ViewMode.FullScreen;
                            _isPane1Visible = false;
                        }
                        else if (_isPane2Visible)
                        {
                            ViewMode = ViewMode.FullScreen;
                            _isPane2Visible = false;
                        }
                        // else do nothing; keep right pane visible
                        break;
                    }
            }
        }

        private void TogglePane2Execute()
        {
            switch (ViewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        // NOTE: Technically, this is not possible
                        // Close main menu pane first then open right pane
                        ViewMode = ViewMode.FullScreen;
                        if (ScreenMode == ScreenMode.PrintPreview) ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                        else ViewMode = ViewMode.RightPaneVisible;
                        _isPane2Visible = true;
                        Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane2);
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        if (ScreenMode == ScreenMode.PrintPreview) ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                        else ViewMode = ViewMode.RightPaneVisible;
                        _isPane2Visible = true;
                        Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane2);
                        break;
                    }

                case ViewMode.RightPaneVisible:
                case ViewMode.RightPaneVisible_ResizedWidth:
                    {
                        if (_isPane2Visible)
                        {
                            ViewMode = ViewMode.FullScreen;
                            _isPane2Visible = false;
                        }
                        else if (_isPane1Visible)
                        {
                            ViewMode = ViewMode.FullScreen;
                            _isPane1Visible = false;
                        }
                        // else, do nothing; keep right pane visible
                        break;
                    }
            }
        }

        #region MAIN MENU

        private ICommand _goToHomePage;
        private ICommand _goToPrintersPage;
        private ICommand _goToJobsPage;
        private ICommand _goToSettingsPage;
        private ICommand _goToHelpPage;
        private ICommand _goToLegalPage;

        private MainMenuItemList _mainMenuItems;

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
                        () => GoToHomePageExecute(),
                        () => true
                    );
                }
                return _goToHomePage;
            }
        }

        public ICommand GoToPrintersPage
        {
            get
            {
                if (_goToPrintersPage == null)
                {
                    _goToPrintersPage = new RelayCommand(
                        () => GoToPrintersPageExecute(),
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
                        () => GoToJobsPageExecute(),
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
                        () => GoToSettingsPageExecute(),
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
                        () => GoToHelpPageExecute(),
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
                        () => GoToLegalPageExecute(),
                        () => true
                    );
                }
                return _goToLegalPage;
            }
        }

        private void InitializeMainMenu()
        {
            MainMenuItems = new MainMenuItemList();
            // TODO: Add handling to toggle preview page or home page
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_HOME", GoToHomePage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_PRINTERS", GoToPrintersPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_JOBS", GoToJobsPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_SETTINGS", GoToSettingsPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_HELP", GoToHelpPage, Visibility.Visible));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_LEGAL", GoToLegalPage, Visibility.Collapsed));
        }

        private void GoToHomePageExecute()
        {
            if (DocumentController.Instance.IsFromFilePicker)
            {
                _navigationService.Navigate(typeof(HomePage));
                ScreenMode = ScreenMode.Home;
            }
            else
            {
                _navigationService.Navigate(typeof(PrintPreviewPage));
                ScreenMode = ScreenMode.PrintPreview;
                //new ViewModelLocator().PrintPreviewViewModel.InitializeGestures();
            }
            ViewMode = ViewMode.FullScreen;
        }

        private void GoToPrintersPageExecute()
        {
            _navigationService.Navigate(typeof(PrintersPage));
            ScreenMode = ScreenMode.Printers;
            ViewMode = ViewMode.FullScreen;
        }

        private void GoToJobsPageExecute()
        {
            _navigationService.Navigate(typeof(JobsPage));
            ScreenMode = ScreenMode.Jobs;
            ViewMode = ViewMode.FullScreen;
        }

        private void GoToSettingsPageExecute()
        {
            _navigationService.Navigate(typeof(SettingsPage));
            ScreenMode = ScreenMode.Settings;
            ViewMode = ViewMode.FullScreen;
        }

        private void GoToHelpPageExecute()
        {
            _navigationService.Navigate(typeof(HelpPage));
            ScreenMode = ScreenMode.Help;
            ViewMode = ViewMode.FullScreen;
        }

        private void GoToLegalPageExecute()
        {
            _navigationService.Navigate(typeof(LegalPage));
            ScreenMode = ScreenMode.Legal;
            ViewMode = ViewMode.FullScreen;
        }

        #endregion
    }
}
