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
        private double _pageAreaGridOriginalHeight;
        private bool _isPageAreaGridLoaded;
        public PreviewGestureController _gestureController; // TODO: Set to private after removing easter egg!!
        private bool _isPageNumberSliderEnabled;
        private ICommand _goToPage;
        private ICommand _goToPreviousPage;
        private ICommand _goToNextPage;
        private ICommand _pageNumberSliderValueChange;
        private uint _pageTotal;
        private uint _currentPageIndex;
        private uint _pageIndex;
        private PageNumberInfo _pageNumber;

        private string _documentTitleText;
        private BitmapImage _rightPageImage;
        private BitmapImage _leftPageImage;
        private Size _rightPageActualSize;
        private Size _leftPageActualSize;
        private PageViewMode _pageViewMode;

        private ViewControlViewModel _viewControlViewModel;

        public PrintPreviewViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _pageIndex = 0;
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;

            SetViewMode(_viewControlViewModel.ViewMode); 
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
        }

        public void SetPageAreaGrid(Grid pageAreaGrid)
        {
            _pageAreaGrid = pageAreaGrid;
            _pageAreaGridOriginalHeight = _pageAreaGrid.ActualHeight;
            _isPageAreaGridLoaded = true;
        }

        public void InitializeGestures()
        {
            if (_isPageAreaGridLoaded)
            {
                // Save page height to be used in resizing page images                
                var scalingFactor = _pageAreaGridOriginalHeight / RightPageActualSize.Height;
                var pageAreaScrollViewer = (UIElement)_pageAreaGrid.Parent;
                if (_gestureController != null) _gestureController.Dispose();
                Size targetSize;
                if (PageViewMode == PageViewMode.SinglePageView)
                {
                    targetSize = RightPageActualSize;
                }
                else if (PageViewMode == PageViewMode.TwoPageView)
                {
                    var defaultMargin = (int)((double)Application.Current.Resources["MARGIN_Default"]);
                    targetSize = new Size(LeftPageActualSize.Width + RightPageActualSize.Width + defaultMargin, RightPageActualSize.Height);                   
                }
                _gestureController = new PreviewGestureController(_pageAreaGrid, pageAreaScrollViewer,
                       targetSize, scalingFactor,
                       new PreviewGestureController.SwipeRightDelegate(SwipeRight),
                       new PreviewGestureController.SwipeLeftDelegate(SwipeLeft));
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

        private bool _isLoadPageActive;

        public bool IsLoadPageActive
        {
            get { return _isLoadPageActive; }
            set
            {
                //if (_isLoadPageActive != value)
                {
                    _isLoadPageActive = value;
                    RaisePropertyChanged("IsLoadPageActive");
                }
            }
        }
                
        private void SetViewMode(ViewMode viewMode)
        {
            if (_viewControlViewModel.ScreenMode != ScreenMode.PrintPreview) return;
            switch (viewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        DisablePreviewGestures();
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        EnablePreviewGestures();
                        break;
                    }
                case ViewMode.RightPaneVisible: // NOTE: Technically not possible
                case ViewMode.RightPaneVisible_ResizedWidth:
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
            set
            {
                if (_rightPageActualSize != value)
                {
                    _rightPageActualSize = value;
                    RaisePropertyChanged("RightPageActualSize");
                }
            }
        }

        public Size LeftPageActualSize
        {
            get { return _leftPageActualSize; }
            set
            {
                if (_leftPageActualSize != value)
                {
                    _leftPageActualSize = value;
                    RaisePropertyChanged("LeftPageActualSize");
                }
            }
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
            _pageIndex = index;
            SetPageIndexes();
        }

        // TODO: Two-page view
        public void GoToPage(uint index)
        {
            if (GoToPageEventHandler != null)
            {
                GoToPageEventHandler((int)index);
            }
            UpdatePageIndexes(index);
        }

        public void UpdatePageIndexes(uint index)
        {
            _pageIndex = index;
            SetPageIndexes();
        }

        // TODO: Two-page view
        private void GoToPreviousPageExecute()
        {
            if (_pageViewMode == PageViewMode.SinglePageView)
            {
                --_pageIndex; // Page image will be requested on PageSliderValueChangeExecute
            }
            else if (_pageViewMode == PageViewMode.TwoPageView)
            {
                _pageIndex -= 2;
            }
            SetPageIndexes();
        }

        private bool CanGoToPreviousPage()
        {
            if (_pageViewMode == PageViewMode.SinglePageView)
            {
                // Check if at least second page
                if (_pageIndex > 0) return true;
            }
            else if (_pageViewMode == PageViewMode.TwoPageView)
            {
                if (_pageIndex > 1) return true;
            }
            return false;
        }

        // TODO: Two-page view
        private void GoToNextPageExecute()
        {
            if (_pageViewMode == PageViewMode.SinglePageView)
            {
                ++_pageIndex; // Page image will be requested on PageSliderValueChangeExecute
            }
            else if (_pageViewMode == PageViewMode.TwoPageView)
            {
                _pageIndex += 2;
            }
            SetPageIndexes();
        }

        private bool CanGoToNextPage()
        {
            if (_pageViewMode == PageViewMode.SinglePageView)
            {
                // Check if at least second to the last page
                if (_pageIndex < _pageTotal - 1) return true;
            }
            else if (_pageViewMode == PageViewMode.TwoPageView)
            {
                // Check if at least second to the last page
                if (_pageIndex < _pageTotal - 2) return true;
            }
            return false;
        }
        
        private void SetPageIndexes()
        {
            PageNumber = new PageNumberInfo(_pageIndex, _pageTotal, _pageViewMode);
            CurrentPageIndex = _pageIndex;
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