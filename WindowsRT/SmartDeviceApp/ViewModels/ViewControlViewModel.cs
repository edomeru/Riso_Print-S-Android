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
using Windows.UI.Core;

namespace SmartDeviceApp.ViewModels
{
    public class ViewControlViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _toggleMainMenuPane;
        private ICommand _togglePane1;
        private ICommand _togglePane2;
        private ICommand _togglePane3;
        private ViewMode _viewMode;
        private ViewOrientation _viewOrientation;
        
        private ScreenMode _screenMode;        
        private bool _isPane1Visible = false;
        private bool _isPane2Visible = false;
        private bool _isPane3Visible = false;
        private bool _tapHandled = false;
        private Windows.Foundation.Rect _screenBound;
        /// <summary>
        /// ViewControlViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public ViewControlViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            ViewMode = ViewMode.FullScreen;
            ScreenMode = ScreenMode.Home;
            _screenBound = Windows.Foundation.Rect.Empty;
            InitializeMainMenu();
            
        }

        private void RecomputeBounds()
        {
          
            RaisePropertyChanged("ViewMode");
            Messenger.Default.Send<ViewMode>(_viewMode); // Broadcast to all viewmodels that need to be updated
            RaisePropertyChanged("ViewOrientation");
            Messenger.Default.Send<ViewOrientation>(_viewOrientation); // Broadcast to all viewmodels that need to be updated
              
        }

        public Windows.Foundation.Rect ScreenBound
        {
            get
            {
                if (_screenBound.IsEmpty)
                {
                    _screenBound = Window.Current.Bounds;
                }
                return _screenBound;
            }
            set
            {
                if (_screenBound.Width != value.Width || _screenBound.Height != value.Height)
                {
                    _screenBound.Width = value.Width;
                    _screenBound.Height = value.Height;
                    RecomputeBounds();
                }
            }
        }
        /// <summary>
        /// Gets/sets the current view mode
        /// </summary>
        public ViewMode ViewMode
        {
            get { return _viewMode; }
            set
            {
                if (_viewMode != value)
                {
                    _viewMode = value;
                    RecomputeBounds();
                }
            }
        }

        /// <summary>
        /// Gets/sets the current view orientation
        /// </summary>
        public ViewOrientation ViewOrientation
        {
            get { return _viewOrientation; }
            set
            {
                if (_viewOrientation != value)
                {
                    _viewOrientation = value;
                    RecomputeBounds();
                }
            }
        }

        /// <summary>
        /// Gets/sets the current screen mode
        /// </summary>
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

        /// <summary>
        /// License agreement status
        /// </summary>
        public bool IsLicenseAgreed
        {
            get;
            set;
        }

        /// <summary>
        /// Command for toggle main menu pane
        /// </summary>
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

        /// <summary>
        /// Command for toggle first right pane
        /// </summary>
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

        /// <summary>
        /// Command for toggle second right pane
        /// </summary>
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

        /// <summary>
        /// Command for toggle third right pane
        /// </summary>
        public ICommand TogglePane3
        {
            get
            {
                if (_togglePane3 == null)
                {
                    _togglePane3 = new RelayCommand(
                        () => TogglePane3Execute(),
                        () => true
                    );
                }
                return _togglePane3;
            }
        }

        /// <summary>
        /// Gets/sets the visibility state of the first right pane.
        /// True when visible, false otherwise.
        /// </summary>
        public bool IsPane1Visible
        {
            get { return _isPane1Visible; }
            set { _isPane1Visible = value; }
        }

        /// <summary>
        /// Gets/sets the visibility state of the second right pane.
        /// True when visible, false otherwise.
        /// </summary>
        public bool IsPane2Visible
        {
            get { return _isPane2Visible; }
            set { _isPane2Visible = value; }
        }

        /// <summary>
        /// Gets/sets the visibility state of the third right pane.
        /// True when visible, false otherwise.
        /// </summary>
        public bool IsPane3Visible
        {
            get { return _isPane3Visible; }
            set { _isPane3Visible = value; }
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
                        else if (_isPane3Visible)
                        {
                            ViewMode = ViewMode.FullScreen;
                            _isPane3Visible = false;
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
                        else if (_isPane3Visible)
                        {
                            ViewMode = ViewMode.FullScreen;
                            _isPane3Visible = false;
                        }
                        // else, do nothing; keep right pane visible
                        break;
                    }
            }
        }

        private void TogglePane3Execute()
        {
            switch (ViewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        // NOTE: Technically, this is not possible
                        // Close main menu pane first then open right pane
                        ViewMode = ViewMode.FullScreen;
                        if (ScreenMode == ScreenMode.PrintPreview)
                        {
                            ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                        }
                        else
                        {
                            ViewMode = ViewMode.RightPaneVisible;
                        }
                        _isPane3Visible = true;
                        Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane3);
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        if (ScreenMode == ScreenMode.PrintPreview)
                        {
                            ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
                        }
                        else
                        {
                            ViewMode = ViewMode.RightPaneVisible;
                        }
                        _isPane3Visible = true;
                        Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane3);
                        break;
                    }

                case ViewMode.RightPaneVisible:
                case ViewMode.RightPaneVisible_ResizedWidth:
                    {
                        if (_isPane3Visible)
                        {
                            ViewMode = ViewMode.FullScreen;
                            _isPane3Visible = false;
                        }
                        else if (_isPane2Visible)
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

        /// <summary>
        /// Gets/sets the flag when tap gesture is already handled.
        /// True when event is handled, false otherwise.
        /// </summary>
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
        private ICommand _goToLegalPage;
        private ICommand _goToLicensePage;

        private MainMenuItemList _mainMenuItems;

        /// <summary>
        /// Gets/sets the flag when go to home command will be executed.
        /// True when allowed to execute, false otherwise.
        /// </summary>
        public bool EnabledGoToHomeExecute { get; set; } // Enables the GoToHomePage command

        /// <summary>
        /// Gets/sets the main menu items
        /// </summary>
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

        /// <summary>
        /// Command to navigate to Home Screen or Print Preview Screen
        /// </summary>
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

        /// <summary>
        /// Command to navigate to Printers Screen
        /// </summary>
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

        /// <summary>
        /// Command to navigate to Print Job History Screen
        /// </summary>
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

        /// <summary>
        /// Command to navigate to Settings Screen
        /// </summary>
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

        /// <summary>
        /// Command to navigate to Legal Screen
        /// </summary>
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

        /// <summary>
        /// Command to navigate to License Screen
        /// </summary>
        public ICommand GoToLicensePage
        {
            get
            {
                if (_goToLicensePage == null)
                {
                    _goToLicensePage = new RelayCommand(
                        () => GoToLicensePageExecute(),
                        () => true 
                    );
                }
                return _goToLicensePage;
            }
        }

        private void InitializeMainMenu()
        {
            MainMenuItems = new MainMenuItemList();
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_HOME", GoToHomePage, true));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_PRINTERS", GoToPrintersPage, false));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_PRINT_JOB_HISTORY", GoToJobsPage, false));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_SETTINGS", GoToSettingsPage, false));
            MainMenuItems.Add(new MainMenuItem("IDS_LBL_LEGAL", GoToLegalPage, false));
        }

        private bool CanGoToHomePage()
        {
            if (!IsLicenseAgreed) return false;
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

        private void GoToLegalPageExecute()
        {
            _navigationService.Navigate(typeof(LegalPage));
            ScreenMode = ScreenMode.Legal;
            ViewMode = ViewMode.FullScreen;
            MainMenuItems[4].IsChecked = true;
        }

        private void GoToLicensePageExecute()
        {
            _navigationService.Navigate(typeof(LicensePage));
            ScreenMode = ScreenMode.License;
            ViewMode = ViewMode.FullScreen;
        }

        #endregion
    }
}
