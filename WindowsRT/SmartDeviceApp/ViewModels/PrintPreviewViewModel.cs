//
//  PrintPreviewViewModel.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.Foundation;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
// using SmartDeviceApp.DummyControllers;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.ViewModels
{
    public class PrintPreviewViewModel : ViewModelBase
    {
        private const bool IS_SINGLE_PAGE = true;
        private const double SIDE_PANE_RATIO = 2;
        private const double PREVIEW_VIEW_RATIO = 5;

        public event SmartDeviceApp.Controllers.PrintPreviewController.GoToPageEventHandler GoToPageEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private Grid _pageAreaGrid;
        public GestureController _gestureController; // TODO: Set to private after removing easter egg!!
        private bool _isPageNumberSliderEnabled;
        private ICommand _goToPage;
        private ICommand _goToPreviousPage;
        private ICommand _goToNextPage;
        private ICommand _pageNumberSliderValueChange;
        private uint _pageTotal;
        private uint _currentPageIndex;
        private uint _rightPageIndex;
        private uint _leftPageIndex;
        private PageNumberInfo _pageNumber;

        private string _documentTitleText;
        private BitmapImage _rightPageImage;
        private BitmapImage _leftPageImage;
        private Size _rightPageActualSize;
        private Size _leftPageActualSize;
        private PageViewMode _pageViewMode;

        private ICommand _toggleMainMenuPane;
        private ICommand _showPreviewViewFullScreen;
        private ICommand _togglePrintSettingsPane;

        private AppViewModel _appViewModel;
        private AppViewMode _appViewMode;

        public PrintPreviewViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _rightPageIndex = 0;
            _appViewModel = new ViewModelLocator().AppViewModel;
            SetAppViewMode(_appViewModel.AppViewMode);
        }


        public void SetPageAreaGrid(Grid pageAreaGrid)
        {
            _pageAreaGrid = pageAreaGrid;
        }

        public void InitializeGestures()
        {
            if (_pageAreaGrid != null && _gestureController == null)
            {
                // Save page height to be used in resizing page images
                var scalingFactor = _pageAreaGrid.ActualHeight / RightPageActualSize.Height;

                var pageAreaScrollViewer = (UIElement)_pageAreaGrid.Parent;
                _gestureController = new GestureController(_pageAreaGrid, pageAreaScrollViewer,
                    RightPageActualSize, scalingFactor,
                    new GestureController.SwipeRightDelegate(SwipeRight),
                    new GestureController.SwipeLeftDelegate(SwipeLeft));

                // TODO: Two-page view handling
            }
        }

        private void SwipeRight()
        {
            GoToPreviousPage.Execute(null);
        }

        private void SwipeLeft()
        {
            GoToNextPage.Execute(null);
        }

        #region PANE VISIBILITY

        public AppViewMode AppViewMode
        {
            get { return _appViewMode; }
            set
            {
                if (_appViewMode != value)
                {
                    _appViewMode = value;
                    RaisePropertyChanged("AppViewMode");
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

        public ICommand TogglePrintSettingsPane
        {
            get
            {
                if (_togglePrintSettingsPane == null)
                {
                    _togglePrintSettingsPane = new RelayCommand(
                        () => TogglePrintSettingsPaneExecute(),
                        () => true
                    );
                }
                return _togglePrintSettingsPane;
            }
        }

        private void ToggleMainMenuPaneExecute()
        {
            switch (_appViewModel.AppViewMode)
            {
                case AppViewMode.MainMenuPaneVisible:
                {
                    SetAppViewMode(AppViewMode.PrintPreviewPageFullScreen);
                    break;
                }

                case AppViewMode.PrintPreviewPageFullScreen:
                {
                    SetAppViewMode(AppViewMode.MainMenuPaneVisible);
                    break;
                }

                case AppViewMode.RightPaneVisible_ResizedView:
                {
                    SetAppViewMode(AppViewMode.PrintPreviewPageFullScreen);
                    SetAppViewMode(AppViewMode.MainMenuPaneVisible);
                    break;
                }
            }
        }

        private void TogglePrintSettingsPaneExecute()
        {
            switch (_appViewModel.AppViewMode)
            {
                case AppViewMode.MainMenuPaneVisible:
                    {
                        SetAppViewMode(AppViewMode.PrintPreviewPageFullScreen);
                        SetAppViewMode(AppViewMode.RightPaneVisible);
                        break;
                    }

                case AppViewMode.PrintPreviewPageFullScreen:
                    {
                        SetAppViewMode(AppViewMode.RightPaneVisible_ResizedView);
                        _appViewModel.RightPaneMode = RightPaneMode.PrintSettings;
                        break;
                    }

                case AppViewMode.RightPaneVisible_ResizedView:
                    {
                        SetAppViewMode(AppViewMode.PrintPreviewPageFullScreen);
                        break;
                    }
            }
        }

        private void SetAppViewMode(AppViewMode appViewMode)
        {
            AppViewMode = appViewMode;
            _appViewModel.AppViewMode = appViewMode;
            switch (appViewMode)
            {
                case AppViewMode.MainMenuPaneVisible:
                {
                    DisablePreviewGestures();
                    break;
                }

                case AppViewMode.PrintPreviewPageFullScreen:
                {
                    EnablePreviewGestures();
                    break;
                }

                case AppViewMode.RightPaneVisible:
                {
                    EnablePreviewGestures();
                    break;
                }
            }
        }

        private void EnablePreviewGestures()
        {
            if (_gestureController != null)
            {
                _gestureController.EnableGestures();
            }
            IsPageNumberSliderEnabled = true;
        }

        private void DisablePreviewGestures()
        {
            if (_gestureController != null) _gestureController.DisableGestures();
            IsPageNumberSliderEnabled = false;
        }

        #endregion

        #region PAGE DISPLAY

        public string DocumentTitleText
        {
            get { return _documentTitleText; }
            set
            {
                if (_documentTitleText != value)
                {
                    _documentTitleText = value;
                    RaisePropertyChanged("DocumentTitleText");
                }
            }
        }

        public BitmapImage RightPageImage
        {
            get { return _rightPageImage; }
            set
            {
                if (_rightPageImage != value)
                {
                    _rightPageImage = value;
                    RaisePropertyChanged("RightPageImage");
                }
            }
        }

        public BitmapImage LeftPageImage
        {
            get { return _leftPageImage; }
            set
            {
                if (_leftPageImage != value)
                {
                    _leftPageImage = value;
                    RaisePropertyChanged("LeftPageImage");
                }
            }
        }

        public Size RightPageActualSize
        {
            get { return _rightPageActualSize; }
            set { _rightPageActualSize = value; }
        }

        public Size LeftPageActualSize
        {
            get { return _leftPageActualSize; }
            set { _leftPageActualSize = value; }
        }
        
        public PageViewMode PageViewMode
        {
            get { return _pageViewMode; }
            set
            {
                if (_pageViewMode != value)
                {
                    _pageViewMode = value;
                    RaisePropertyChanged("PageViewMode");
                }
            }
        }

        #endregion

        #region SINGLE-PAGE NAVIGATION

        public ICommand GoToPreviousPage
        {
            get
            {
                if (_goToPreviousPage == null)
                {
                    _goToPreviousPage = new RelayCommand(
                        () => GoToPreviousPageExecute(),
                        () => CanGoToPreviousPage()
                    );
                }
                return _goToPreviousPage;
            }
        }

        public ICommand GoToNextPage
        {
            get
            {
                if (_goToNextPage == null)
                {
                    _goToNextPage = new RelayCommand(
                        () => GoToNextPageExecute(),
                        () => CanGoToNextPage()
                    );
                }
                return _goToNextPage;
            }
        }

        public PageNumberInfo PageNumber
        {
            get { return _pageNumber; }
            set
            {
                if (_pageNumber != value)
                {
                    _pageNumber = value;
                    RaisePropertyChanged("PageNumber");
                }
            }
        }
        
        public void SetInitialPageIndex(uint index)
        {
            _rightPageIndex = index;
            SetPageIndexes();
        }

        // TODO: Two-page view
        public void GoToPage(uint index)
        {
            //DummyProvider.Instance.LoadPageImage(index);
            if (GoToPageEventHandler != null)
            {
                GoToPageEventHandler((int)index);
            }
            _rightPageIndex = index;
            SetPageIndexes();
        }

        
        // TODO: Two-page view
        private void GoToPreviousPageExecute()
        {
            // DummyProvider.Instance.LoadPageImage(--_rightPageIndex);
            --_rightPageIndex; // Page image will be requested on PageSliderValueChangeExecute
            SetPageIndexes();
        }

        private bool CanGoToPreviousPage()
        {
            // Check if at least second page
            if (_rightPageIndex > 0) return true;
            return false;
        }

        // TODO: Two-page view
        private void GoToNextPageExecute()
        {
            // DummyProvider.Instance.LoadPageImage(++_rightPageIndex);
            ++_rightPageIndex; // Page image will be requested on PageSliderValueChangeExecute
            SetPageIndexes();
        }

        private bool CanGoToNextPage()
        {
            // Check if at least second to the last page
            if (_rightPageIndex < _pageTotal - 1) return true;
            return false;
        }
        
        private void SetPageIndexes()
        {
            if (_pageViewMode == PageViewMode.SinglePageView)
            {
                PageNumber = new PageNumberInfo(0, _rightPageIndex, _pageTotal, _pageViewMode);
                CurrentPageIndex = _rightPageIndex;
            }
            else if (_pageViewMode == PageViewMode.TwoPageView)
            {
                PageNumber = new PageNumberInfo(_leftPageIndex, _rightPageIndex, _pageTotal, _pageViewMode);
                if (_rightPageIndex == 0) // If first page
                {
                    CurrentPageIndex = _rightPageIndex;
                }
                else
                {
                    CurrentPageIndex = _leftPageIndex;
                }
            }
        }

        #endregion

        #region MULTI-PAGE NAVIGATION

        public ICommand PageNumberSliderValueChange
        {
            get
            {
                if (_pageNumberSliderValueChange == null)
                {
                    _pageNumberSliderValueChange = new RelayCommand(
                        () => PageNumberSliderValueChangeExecute(),
                        () => true
                    );
                }
                return _pageNumberSliderValueChange;
            }
        }

        public uint PageTotal
        {
            get { return _pageTotal; }
            set
            {
                if (_pageTotal != value)
                {
                    _pageTotal = value;
                    RaisePropertyChanged("PageTotal");
                }
            }
        }

        // Different handling depending on view mode
        public uint CurrentPageIndex
        {
            get 
            {
                return _currentPageIndex;
            }
            set
            {
                if (_currentPageIndex != value)
                {
                    _currentPageIndex = value;
                    RaisePropertyChanged("CurrentPageIndex");
                }
            }
        }

        public bool IsPageNumberSliderEnabled
        {
            get { return _isPageNumberSliderEnabled; }
            set
            {
                if (_isPageNumberSliderEnabled != value)
                {
                    _isPageNumberSliderEnabled = value;
                    RaisePropertyChanged("IsPageNumberSliderEnabled");
                }
            }
        }

        private void PageNumberSliderValueChangeExecute()
        {
            // TODO: Consider handling the event only when drag is released 
            var newValue = CurrentPageIndex; // verify 0-based
            GoToPage(newValue);
        }

        #endregion

    }
}