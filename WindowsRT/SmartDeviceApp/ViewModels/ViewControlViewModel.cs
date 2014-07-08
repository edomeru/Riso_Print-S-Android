﻿using System;
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
        private ViewOrientation _viewOrientation;
        private ScreenMode _screenMode;        
        private bool _isPane1Visible = false;
        private bool _isPane2Visible = false;
        private bool _tapHandled = false;

        public ViewControlViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            ViewMode = ViewMode.FullScreen;
            ScreenMode = ScreenMode.Home;
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

        public ViewOrientation ViewOrientation
        {
            get { return _viewOrientation; }
            set
            {
                if (_viewOrientation != value)
                {
                    _viewOrientation = value;
                    RaisePropertyChanged("ViewOrientation");
                    Messenger.Default.Send<ViewOrientation>(_viewOrientation); // Broadcast to all viewmodels that need to be updated
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
                    Messenger.Default.Send<ScreenMode>(_screenMode); // Broadcast to all viewmodels that need to be updated
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
            var printSettingsPaneViewModel = new ViewModelLocator().PrintSettingsPaneViewModel;

            switch (ViewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        // NOTE: Technically, this is not possible
                        // Close main menu pane first then open right pane
                        ViewMode = ViewMode.FullScreen;
                        if (ScreenMode == ScreenMode.PrintPreview)
                        {
                            if (printSettingsPaneViewModel.IsEnabled)
                            {
                                ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                            }
                            else
                            {
                                DialogService.Instance.ShowError("IDS_ERR_MSG_NO_SELECTED_PRINTER", "IDS_LBL_PRINT_SETTINGS", "IDS_LBL_OK", null);
                                break;
                            }
                        }
                        else ViewMode = ViewMode.RightPaneVisible;
                        _isPane1Visible = true;
                        Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane1);
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        if (ScreenMode == ScreenMode.PrintPreview)
                        {
                            if (printSettingsPaneViewModel.IsEnabled)
                            {
                                ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                            }
                            else
                            {
                                DialogService.Instance.ShowError("IDS_ERR_MSG_NO_SELECTED_PRINTER", "IDS_LBL_PRINT_SETTINGS", "IDS_LBL_OK", null);
                                break;
                            }
                        }
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

        public bool TapHandled
        {
            get { return _tapHandled; }
            set { _tapHandled = value; }
        }

        #region MAIN MENU

        private ICommand _goToHomePage;
        private ICommand _goToPrintersPage;
        private ICommand _goToJobsPage;
        private ICommand _goToSettingsPage;
        private ICommand _goToHelpPage;
        private ICommand _goToLegalPage;

        private MainMenuItemList _mainMenuItems;

        public bool EnabledGoToHomeExecute { get; set; } // Enables the GoToHomePage command

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
                        () => CanGoToHomePage()
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
                        () => CanGoToPrintersPage()
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
                        () => CanGoToJobsPage()
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
                        () => CanGoToSettingsPage()
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
                        () => CanGoToHelpPage()
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
                        () => CanGoToLegalPage()
                    );
                }
                return _goToLegalPage;
            }
        }

        private void InitializeMainMenu()
        {
            MainMenuItems = new MainMenuItemList();
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_HOME", GoToHomePage, true));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_PRINTERS", GoToPrintersPage, false));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_PRINT_JOB_HISTORY", GoToJobsPage, false));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_SETTINGS", GoToSettingsPage, false));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_HELP", GoToHelpPage, false));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_LEGAL", GoToLegalPage, false));
        }

        private bool CanGoToHomePage()
        {
            if (!EnabledGoToHomeExecute &&
                (ScreenMode == ScreenMode.Home || ScreenMode == ScreenMode.PrintPreview))
            {
                return false;
            }
            return true;
        }
        
        private bool CanGoToPrintersPage()
        {
            if (ScreenMode == ScreenMode.Printers)
            {
                return false;
            }
            return true;
        }

        private bool CanGoToJobsPage()
        {
            if (ScreenMode == ScreenMode.Jobs)
            {
                return false;
            }
            return true;
        }

        private bool CanGoToSettingsPage()
        {
            if (ScreenMode == ScreenMode.Settings)
            {
                return false;
            }
            return true;
        }

        private bool CanGoToHelpPage()
        {
            if (ScreenMode == ScreenMode.Help)
            {
                return false;
            }
            return true;
        }

        private bool CanGoToLegalPage()
        {
            if (ScreenMode == ScreenMode.Legal)
            {
                return false;
            }
            return true;
        }

        private void GoToHomePageExecute()
        {
            var previousScreenMode = ScreenMode;
            if (DocumentController.Instance.Result == LoadDocumentResult.Successful)
            {
                if (previousScreenMode != ScreenMode.PrintPreview)
                {
                    _navigationService.Navigate(typeof(PrintPreviewPage));
                    ScreenMode = ScreenMode.PrintPreview;
                    ViewMode = ViewMode.FullScreen;
                }
                MainMenuItems[0].IsChecked = false; // Need to reset to cancel toggled state
                MainMenuItems[0].IsChecked = true;
                return;
            }
            if (previousScreenMode != ScreenMode.Home)
            {
                _navigationService.Navigate(typeof(HomePage));
                ScreenMode = ScreenMode.Home;
                ViewMode = ViewMode.FullScreen;
            }
            MainMenuItems[0].IsChecked = false; // Need to reset to cancel toggled state
            MainMenuItems[0].IsChecked = true;
        }

        private void GoToPrintersPageExecute()
        {
            _navigationService.Navigate(typeof(PrintersPage));
            ScreenMode = ScreenMode.Printers;
            ViewMode = ViewMode.FullScreen;
            MainMenuItems[1].IsChecked = true;
        }

        private void GoToJobsPageExecute()
        {
            _navigationService.Navigate(typeof(JobsPage));
            ScreenMode = ScreenMode.Jobs;
            ViewMode = ViewMode.FullScreen;
            MainMenuItems[2].IsChecked = true;
        }

        private void GoToSettingsPageExecute()
        {
            _navigationService.Navigate(typeof(SettingsPage));
            ScreenMode = ScreenMode.Settings;
            ViewMode = ViewMode.FullScreen;
            MainMenuItems[3].IsChecked = true;
        }

        private void GoToHelpPageExecute()
        {
            _navigationService.Navigate(typeof(HelpPage));
            ScreenMode = ScreenMode.Help;
            ViewMode = ViewMode.FullScreen;
            MainMenuItems[4].IsChecked = true;
        }

        private void GoToLegalPageExecute()
        {
            _navigationService.Navigate(typeof(LegalPage));
            ScreenMode = ScreenMode.Legal;
            ViewMode = ViewMode.FullScreen;
            MainMenuItems[5].IsChecked = true;
        }

        #endregion
    }
}
