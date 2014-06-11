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
using SmartDeviceApp.Converters;

namespace SmartDeviceApp.ViewModels
{
    public class PrintPreviewViewModel : ViewModelBase
    {
        private const bool IS_SINGLE_PAGE = true;
        private const double SIDE_PANE_RATIO = 2;
        private const double PREVIEW_VIEW_RATIO = 5;

        public event SmartDeviceApp.Controllers.PrintPreviewController.GoToPageEventHandler GoToPageEventHandler;
        public event SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateToEventHandler OnNavigateToEventHandler;
        public event SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateFromEventHandler OnNavigateFromEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private Grid _pageAreaGrid;
        private UIElement _controlReference;
        private double _pageAreaGridMaxHeight;
        private double _pageAreaGridMaxWidth;
        private bool _isPageAreaGridLoaded;
        public PreviewGestureController _gestureController; // TODO: Set to private after removing easter egg!!
        private bool _isPageNumberSliderEnabled;
        private ICommand _goToPreviousPage;
        private ICommand _goToNextPage;
        private ICommand _pageNumberSliderValueChange;
        private ICommand _pageNumberSliderPointerCaptureLost;
        private uint _pageTotal;
        private uint _currentPageIndex;
        private uint _pageIndex;
        private PageNumberInfo _pageNumber;
        private bool _isReverseSwipe;
        private bool _isReverseSwipePrevious;
        private bool _isHorizontalSwipeEnabled;
        private double _scalingFactor = 1;

        private string _documentTitleText;
        private WriteableBitmap _rightPageImage;
        private WriteableBitmap _leftPageImage;
        private Size _rightPageActualSize;
        private Size _leftPageActualSize;
        private PageViewMode _pageViewMode;
        private PageViewMode _previousPageViewMode;

        private ViewControlViewModel _viewControlViewModel;

        public PrintPreviewViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _pageIndex = 0;
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;

            _rightPageImage = new WriteableBitmap(1, 1);
            _leftPageImage = new WriteableBitmap(1, 1);

            SetViewMode(_viewControlViewModel.ViewMode); 
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetPageAreaGrid(viewOrientation));
        }

        public void OnNavigatedTo()
        {
            if (OnNavigateToEventHandler != null)
            {
                OnNavigateToEventHandler();
            }
        }

        public void OnNavigatedFrom()
        {
            if (OnNavigateFromEventHandler != null)
            {
                OnNavigateFromEventHandler();
            }
        }

        public void SetPageAreaGrid(Grid pageAreaGrid)
        {
            if (!_isPageAreaGridLoaded)
            {   
                _pageAreaGrid = pageAreaGrid;
                _controlReference = (UIElement)_pageAreaGrid.Parent;
                ResetPageAreaGrid(_viewControlViewModel.ViewOrientation);
                _isPageAreaGridLoaded = true;
            }
        }

        // Should be reset everytime the view orientation is changed
        private void ResetPageAreaGrid(ViewOrientation viewOrientation)
        {
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            _pageAreaGridMaxWidth = (double)((new ResizedViewWidthConverter()).Convert(_viewControlViewModel.ViewMode, null, viewOrientation, null)) - defaultMargin * 2;
            ((ScrollViewer)_controlReference).Width = _pageAreaGridMaxWidth;
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            var sliderHeight = ((GridLength)Application.Current.Resources["SIZE_PageNumberSliderHeight"]).Value;
            var sliderTextHeight = ((GridLength)Application.Current.Resources["SIZE_PageNumberTextHeight"]).Value;
            _pageAreaGridMaxHeight = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null))
                - defaultMargin * 2 - titleHeight - sliderHeight - sliderTextHeight;
        }

        public void InitializeGestures()
        {
            if (_isPageAreaGridLoaded)
            {
                // Save page height to be used in resizing page images
                double scalingFactor = 1;
                Size targetSize;
                switch (PageViewMode)
                {
                    case PageViewMode.SinglePageView:
                        scalingFactor = Math.Min(_pageAreaGridMaxHeight / RightPageActualSize.Height,
                            _pageAreaGridMaxWidth / RightPageActualSize.Width);
                        targetSize = RightPageActualSize;
                        break;

                    case PageViewMode.TwoPageViewHorizontal:
                        scalingFactor = Math.Min(_pageAreaGridMaxHeight / RightPageActualSize.Height,
                            _pageAreaGridMaxWidth / (LeftPageActualSize.Width + RightPageActualSize.Width));
                        targetSize = new Size(LeftPageActualSize.Width + RightPageActualSize.Width, RightPageActualSize.Height);
                        break;

                    case PageViewMode.TwoPageViewVertical:
                        scalingFactor = Math.Min(_pageAreaGridMaxHeight / (RightPageActualSize.Height + LeftPageActualSize.Height),
                            _pageAreaGridMaxWidth / RightPageActualSize.Width);
                        targetSize = new Size(RightPageActualSize.Width, RightPageActualSize.Height + LeftPageActualSize.Height);
                        break;
                }
                
                PreviewGestureController.SwipeRightDelegate swipeRight = null;
                PreviewGestureController.SwipeLeftDelegate swipeLeft = null;
                PreviewGestureController.SwipeTopDelegate swipeTop = null;
                PreviewGestureController.SwipeBottomDelegate swipeBottom = null;
                if (IsHorizontalSwipeEnabled)
                {                
                    if (!IsReverseSwipe)
                    {
                        swipeRight = new PreviewGestureController.SwipeRightDelegate(SwipeRight);
                        swipeLeft = new PreviewGestureController.SwipeLeftDelegate(SwipeLeft);
                    }
                    else
                    {
                        swipeRight = new PreviewGestureController.SwipeRightDelegate(SwipeRightReverse);
                        swipeLeft = new PreviewGestureController.SwipeLeftDelegate(SwipeLeftReverse);
                    }
                }
                else
                {
                    swipeTop = new PreviewGestureController.SwipeTopDelegate(SwipeTop);
                    swipeBottom = new PreviewGestureController.SwipeBottomDelegate(SwipeBottom);
                }
                // Note: If view and page areas are not resized or PageViewMode is not changed, 
                // no need to reset gestureController
                if (scalingFactor != _scalingFactor || PageViewMode != _previousPageViewMode)
                {
                    _scalingFactor = scalingFactor;
                    if (_gestureController != null) _gestureController.Dispose();
                    _gestureController = new PreviewGestureController(_pageAreaGrid, _controlReference,
                           targetSize, scalingFactor, swipeRight, swipeLeft);
                    _gestureController.InitializeSwipe(IsHorizontalSwipeEnabled, swipeLeft, swipeRight,
                        swipeTop, swipeBottom);
                }
                else
                {
                    if (IsReverseSwipe != _isReverseSwipePrevious)
                    {
                        _gestureController.InitializeSwipe(IsHorizontalSwipeEnabled, swipeLeft, swipeRight,
                            swipeTop, swipeBottom);
                    }
                }
                _previousPageViewMode = PageViewMode;
                _isReverseSwipePrevious = IsReverseSwipe;
            }
        }

        new public void Cleanup()
        {
            if (_gestureController != null)
            {
                _gestureController.Dispose();
                _gestureController = null;
            }
            if (_leftPageImage != null)
            {
                _leftPageImage.Clear();
                _leftPageImage.Invalidate();
            }
            if (_rightPageImage != null)
            {
                _rightPageImage.Clear();
                _rightPageImage.Invalidate();
            }
            _rightPageActualSize = new Size();
            _leftPageActualSize = new Size();
            _isPageAreaGridLoaded = false;
            _scalingFactor = 0;
        }

        public bool IsReverseSwipe
        {
            get { return _isReverseSwipe; }
            set { _isReverseSwipe = value; }
        }

        public bool IsHorizontalSwipeEnabled
        {
            get { return _isHorizontalSwipeEnabled; }
            set { _isHorizontalSwipeEnabled = value; }
        }

        private void SwipeRight()
        {
            GoToPreviousPage.Execute(null);
        }

        private void SwipeRightReverse()
        {
            GoToNextPage.Execute(null);
        }

        private void SwipeLeft()
        {
            GoToNextPage.Execute(null);
        }

        private void SwipeLeftReverse()
        {
            GoToPreviousPage.Execute(null);
        }

        private void SwipeTop()
        {
            GoToNextPage.Execute(null);
        }

        private void SwipeBottom()
        {
            GoToPreviousPage.Execute(null);
        }

        #region PANE VISIBILITY

        private bool _isLoadPageActive;

        public bool IsLoadPageActive
        {
            get { return _isLoadPageActive; }
            set
            {
                if (_isLoadPageActive != value)
                {
                    _isLoadPageActive = value;
                    RaisePropertyChanged("IsLoadPageActive");
                }
            }
        }
                
        private void SetViewMode(ViewMode viewMode)
        {
            if (_viewControlViewModel.ScreenMode != ScreenMode.PrintPreview &&
                _viewControlViewModel.ScreenMode != ScreenMode.Home) return;
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            _pageAreaGridMaxWidth = (double)((new ResizedViewWidthConverter()).Convert(viewMode, null, null, null)) - defaultMargin * 2;
            if (_controlReference != null) ((ScrollViewer)_controlReference).Width = _pageAreaGridMaxWidth;
            InitializeGestures();
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

        public WriteableBitmap RightPageImage
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

        public WriteableBitmap LeftPageImage
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
                    RaisePropertyChanged("PageViewMode");
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

        public void GoToPage(uint index)
        {
            if (GoToPageEventHandler != null)
            {
                GoToPageEventHandler((int)index);
            }
        }

        public void UpdatePageIndexes(uint index)
        {
            _pageIndex = index;
            SetPageIndexes();
        }

        private void GoToPreviousPageExecute()
        {
            --_pageIndex;
            SetPageIndexes();
            GoToPage(_pageIndex);
        }

        private bool CanGoToPreviousPage()
        {
            // Check if at least second page
            if (_pageIndex > 0) return true;
            return false;
        }

        private void GoToNextPageExecute()
        {
            ++_pageIndex;
            SetPageIndexes();
            GoToPage(_pageIndex);
        }

        private bool CanGoToNextPage()
        {
            // Check if at least second to the last page
            if (_pageIndex < _pageTotal - 1) return true;
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

        public ICommand PageNumberSliderPointerCaptureLost
        {
            get
            {
                if (_pageNumberSliderPointerCaptureLost == null)
                {
                    _pageNumberSliderPointerCaptureLost = new RelayCommand(
                        () => PageNumberSliderPointerCaptureLostExecute(),
                        () => true
                    );
                }
                return _pageNumberSliderPointerCaptureLost;
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
            var newValue = CurrentPageIndex;
            UpdatePageIndexes(newValue);
        }

        private void PageNumberSliderPointerCaptureLostExecute()
        {
            GoToPage(_pageIndex);
        }

        #endregion

    }
}