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
using SmartDeviceApp.Controls;
using Windows.Graphics.Display;

namespace SmartDeviceApp.ViewModels
{
    public class PrintPreviewViewModel : ViewModelBase
    {
        private const bool IS_SINGLE_PAGE = true;
        private const double SIDE_PANE_RATIO = 2;
        private const double PREVIEW_VIEW_RATIO = 5;

        /// <summary>
        /// Go to page event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrintPreviewController.GoToPageEventHandler GoToPageEventHandler;

        /// <summary>
        /// Turn page (for swipe direction) event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrintPreviewController.TurnPageEventHandler TurnPageEventHandler;

        /// <summary>
        /// Transition to Print Preview Screen event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateToEventHandler OnNavigateToEventHandler;

        // Transition from Print Preview Screen event handler
        public event SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateFromEventHandler OnNavigateFromEventHandler;

        /// <summary>
        /// PageAreaGrid loaded event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.PrintPreviewController.PageAreaGridLoadedEventHandler PageAreaGridLoadedEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private TwoPageControl _twoPageControl;
        private UIElement _controlReference;
        private double _pageAreaGridMaxHeight;
        private double _pageAreaGridMaxWidth;
        private PreviewGestureController _gestureController;
        private bool _isPageNumberSliderEnabled;
        private ICommand _goToPreviousPage;
        private ICommand _goToNextPage;
        private ICommand _pageNumberSliderValueChange;
        private ICommand _pageNumberSliderPointerCaptureLost;
        private uint _pageTotal;
        private uint _pagesPerSheet;
        private uint _currentPageIndex;
        private uint _pageIndex;
        private PageNumberInfo _pageNumber;
        private bool _isReverseSwipe;
        private bool _isReverseSwipePrevious;
        private bool _isHorizontalSwipeEnabled;
        private bool _isHorizontalSwipeEnabledPrevious;
        private double _scalingFactor = 1;

        private string _trimmedTitleText;
        private double _mainMenuButtonWidth;
        private double _printSettingsButtonWidth;

        private WriteableBitmap _rightPageImage;
        private WriteableBitmap _leftPageImage;
        private WriteableBitmap _rightBackPageImage;
        private WriteableBitmap _leftBackPageImage;
        private WriteableBitmap _rightNextPageImage;
        private WriteableBitmap _leftNextPageImage;
        private Size _rightPageActualSize;
        private Size _leftPageActualSize;
        private PageViewMode _pageViewMode;
        private PageViewMode _previousPageViewMode;

        private ViewControlViewModel _viewControlViewModel;

        /// <summary>
        /// PrintPreviewViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public PrintPreviewViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _pageIndex = 0;
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;

            _rightPageImage = new WriteableBitmap(1, 1);
            _leftPageImage = new WriteableBitmap(1, 1);
            _rightBackPageImage = new WriteableBitmap(1, 1);
            _leftBackPageImage = new WriteableBitmap(1, 1);
            _rightNextPageImage = new WriteableBitmap(1, 1);
            _leftNextPageImage = new WriteableBitmap(1, 1);

            SetViewMode(_viewControlViewModel.ViewMode); 
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => SetViewOrientation(viewOrientation));
        }

        /// <summary>
        /// Transition to Print Preview Screen event handler
        /// </summary>
        public void OnNavigatedTo()
        {
            if (OnNavigateToEventHandler != null)
            {
                OnNavigateToEventHandler();
            }
            new ViewModelLocator().PrintSettingsViewModel.SetPrintSettingsPaneEnable();
        }

        /// <summary>
        /// Transition from Print Preview Screen event handler
        /// </summary>
        public void OnNavigatedFrom()
        {
            if (OnNavigateFromEventHandler != null)
            {
                OnNavigateFromEventHandler();
            }

            // Execute select printer box reset tasks
            new ViewModelLocator().SelectPrinterViewModel.BackToPrintSettings.Execute(null);
        }

        /// <summary>
        /// Sets the page area gird
        /// </summary>
        /// <param name="twoPageControl">two-page control</param>
        public void SetPageAreaGrid(SmartDeviceApp.Controls.TwoPageControl twoPageControl)
        {
            if (!IsPageAreaGridLoaded)
            {
                _twoPageControl = twoPageControl;
                _controlReference = (UIElement)((Grid)twoPageControl.DisplayAreaGrid.Parent).Parent;
                ResetPageAreaGrid(_viewControlViewModel.ViewMode, _viewControlViewModel.ViewOrientation);

                IsPageAreaGridLoaded = true;

                if (PageAreaGridLoadedEventHandler != null)
                {
                    PageAreaGridLoadedEventHandler();
                }
            }
        }

        /// <summary>
        /// Resets the page area grid dimensions.
        /// Should be reset everytime the view mode or view orientation is changed.
        /// </summary>
        /// <param name="viewMode">view mode</param>
        /// <param name="viewOrientation">view orientation</param>
        private void ResetPageAreaGrid(ViewMode viewMode, ViewOrientation viewOrientation)
        {
            if (_controlReference != null)
            {
                var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];

                // Update desired controlReference's width
                _pageAreaGridMaxWidth = (double)((new ResizedViewWidthConverter()).Convert(viewMode, null, viewOrientation, null)) - defaultMargin * 2;
                ((ScrollViewer)_controlReference).Width = _pageAreaGridMaxWidth;

                // Update desired controlReference's height
                var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
                var sliderHeight = ((GridLength)Application.Current.Resources["SIZE_PageNumberSliderHeight"]).Value;
                var sliderTextHeight = ((GridLength)Application.Current.Resources["SIZE_PageNumberTextHeight"]).Value;
                _pageAreaGridMaxHeight = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null))
                    - defaultMargin * 2 - titleHeight - sliderHeight - sliderTextHeight;
                ((ScrollViewer)_controlReference).Height = _pageAreaGridMaxHeight;
            }
        }

        /// <summary>
        /// Initializes the gestures (event handlers and GestureController)
        /// </summary>
        public void InitializeGestures()
        {
            if (IsPageAreaGridLoaded)
            {
                // Save page height to be used in resizing page images
                double scalingFactor = 1;
                bool isDuplex = false;
                Size targetSize;
                Size pageAreaGridSize = new Size(LeftPageActualSize.Width + RightPageActualSize.Width, RightPageActualSize.Height);
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
                        isDuplex = true;
                        break;

                    case PageViewMode.TwoPageViewVertical:
                        scalingFactor = Math.Min(_pageAreaGridMaxHeight / (RightPageActualSize.Height + LeftPageActualSize.Height),
                            _pageAreaGridMaxWidth / RightPageActualSize.Width);
                        targetSize = new Size(RightPageActualSize.Width, RightPageActualSize.Height + LeftPageActualSize.Height);
                        isDuplex = true;
                        break;
                }

                // Resize grids
                _twoPageControl.PageAreaGrid.MaxWidth = targetSize.Width;
                _twoPageControl.PageAreaGrid.MaxHeight = targetSize.Height;
                _twoPageControl.TransitionGrid.MaxWidth = targetSize.Width;
                _twoPageControl.TransitionGrid.MaxHeight = targetSize.Height;
                _twoPageControl.DisplayAreaGrid.MaxWidth = targetSize.Width;
                _twoPageControl.DisplayAreaGrid.MaxHeight = targetSize.Height;

                PreviewGestureController.SwipeRightDelegate swipeRight = null;
                PreviewGestureController.SwipeLeftDelegate swipeLeft = null;
                PreviewGestureController.SwipeTopDelegate swipeTop = null;
                PreviewGestureController.SwipeBottomDelegate swipeBottom = null;
                PreviewGestureController.SwipeDirectionDelegate swipeDirection =
                    new PreviewGestureController.SwipeDirectionDelegate(SwipeDirection);

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
                    if (!IsReverseSwipe)
                    {
                        swipeTop = new PreviewGestureController.SwipeTopDelegate(SwipeTop);
                        swipeBottom = new PreviewGestureController.SwipeBottomDelegate(SwipeBottom);
                    }
                    else
                    {
                        swipeTop = new PreviewGestureController.SwipeTopDelegate(SwipeTopReverse);
                        swipeBottom = new PreviewGestureController.SwipeBottomDelegate(SwipeBottomReverse);
                    }
                }

                // Note: If view and page areas are not resized or PageViewMode is not changed, 
                // no need to reset gestureController

                if (scalingFactor != _scalingFactor || PageViewMode != _previousPageViewMode)
                {
                    _scalingFactor = scalingFactor;
                    if (_gestureController != null)
                    {
                        _gestureController.Dispose();
                        _gestureController = null;
                    }
                    _gestureController = new PreviewGestureController(_twoPageControl, _controlReference,
                           targetSize, scalingFactor, swipeRight, swipeLeft, isDuplex, _currentPageIndex,_pageTotal);
                    _gestureController.InitializeSwipe(IsHorizontalSwipeEnabled, IsReverseSwipe, swipeLeft, swipeRight,
                        swipeTop, swipeBottom, swipeDirection);
                }
                else
                {
                    if (IsReverseSwipe != _isReverseSwipePrevious ||
                        IsHorizontalSwipeEnabled != _isHorizontalSwipeEnabledPrevious)
                    {
                        _gestureController.InitializeSwipe(IsHorizontalSwipeEnabled, IsReverseSwipe, swipeLeft, swipeRight,
                            swipeTop, swipeBottom, swipeDirection);
                    }
                }
                _previousPageViewMode = PageViewMode;
                _isReverseSwipePrevious = IsReverseSwipe;
                _isHorizontalSwipeEnabledPrevious = IsHorizontalSwipeEnabled;
            }
        }

        /// <summary>
        /// Clean-up
        /// </summary>
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
            if (_leftBackPageImage != null)
            {
                _leftBackPageImage.Clear();
                _leftBackPageImage.Invalidate();
            }
            if (_rightBackPageImage != null)
            {
                _rightBackPageImage.Clear();
                _rightBackPageImage.Invalidate();
            }
            _rightPageActualSize = new Size();
            _leftPageActualSize = new Size();
            IsPageAreaGridLoaded = false;
            _scalingFactor = 1;
        }

        /// <summary>
        /// Gets/sets the reverse state of swipe direction.
        /// True when reverse swipe is enabled, false otherwise.
        /// </summary>
        public bool IsReverseSwipe
        {
            get { return _isReverseSwipe; }
            set { _isReverseSwipe = value; }
        }

        /// <summary>
        /// Gets/sets the swipe direction state.
        /// True when horizontal swipe is enabled, false otherwise (vertical swipe is enabled)
        /// </summary>
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

        private void SwipeTopReverse()
        {
            GoToPreviousPage.Execute(null);
        }

        private void SwipeBottom()
        {
            GoToPreviousPage.Execute(null);
        }

        private void SwipeBottomReverse()
        {
            GoToNextPage.Execute(null);
        }

        private void SwipeDirection(bool isSwipeLeft)
        {
            if (TurnPageEventHandler != null)
            {
                TurnPageEventHandler(isSwipeLeft);
            }
        }

        //public ContentControl DrawingSurface
        //{
        //    get;
        //    set;
        //}

        #region PANE VISIBILITY

        private bool _isLoadLeftPageActive;
        private bool _isLoadRightPageActive;
        private bool _isLoadLeftBackPageActive;
        private bool _isLoadRightBackPageActive;
        private bool _isLoadLeftNextPageActive;
        private bool _isLoadRightNextPageActive;

        /// <summary>
        /// Gets/sets the loading indicator active state for the left back page.
        /// True when loading indicator for the left back page is active, false otherwise.
        /// </summary>
        public bool IsLoadLeftBackPageActive
        {
            get { return _isLoadLeftBackPageActive; }
            set
            {
                if (_isLoadLeftBackPageActive != value)
                {
                    _isLoadLeftBackPageActive = value;
                    RaisePropertyChanged("IsLoadLeftBackPageActive");
                }
            }
        }

        /// <summary>
        /// Gets/sets the loading indicator active state for the right back page.
        /// True when loading indicator for the right back page is active, false otherwise
        /// </summary>
        public bool IsLoadRightBackPageActive
        {
            get { return _isLoadRightBackPageActive; }
            set
            {
                if (_isLoadRightBackPageActive != value)
                {
                    _isLoadRightBackPageActive = value;
                    RaisePropertyChanged("IsLoadRightBackPageActive");
                }
            }
        }

        /// <summary>
        /// Gets/sets the loading indicator active state for the left next page.
        /// True when loading indicator for the left next page is active, false otherwise
        /// </summary>
        public bool IsLoadLeftNextPageActive
        {
            get { return _isLoadLeftNextPageActive; }
            set
            {
                if (_isLoadLeftNextPageActive != value)
                {
                    _isLoadLeftNextPageActive = value;
                    RaisePropertyChanged("IsLoadLeftNextPageActive");
                }
            }
        }

        /// <summary>
        /// Gets/sets the loading indicator active state for the right next page.
        /// True when loading indicator for the right next page is active, false otherwise
        /// </summary>
        public bool IsLoadRightNextPageActive
        {
            get { return _isLoadRightNextPageActive; }
            set
            {
                if (_isLoadRightNextPageActive != value)
                {
                    _isLoadRightNextPageActive = value;
                    RaisePropertyChanged("IsLoadRightNextPageActive");
                }
            }
        }

        /// <summary>
        /// Gets/sets the loading indicator active state for the left page.
        /// True when loading indicator for the left page is active, false otherwise
        /// </summary>
        public bool IsLoadLeftPageActive
        {
            get { return _isLoadLeftPageActive; }
            set
            {
                if (_isLoadLeftPageActive != value)
                {
                    _isLoadLeftPageActive = value;
                    RaisePropertyChanged("IsLoadLeftPageActive");
                }
            }
        }

        /// <summary>
        /// Gets/sets the loading indicator active state for the left page.
        /// True when loading indicator for right page is active, false otherwise
        /// </summary>
        public bool IsLoadRightPageActive
        {
            get { return _isLoadRightPageActive; }
            set
            {
                if (_isLoadRightPageActive != value)
                {
                    _isLoadRightPageActive = value;
                    RaisePropertyChanged("IsLoadRightPageActive");
                }
            }
        }

        private void SetViewMode(ViewMode viewMode)
        {
            if (_viewControlViewModel.ScreenMode != ScreenMode.PrintPreview &&
                _viewControlViewModel.ScreenMode != ScreenMode.Home)
            {
                return;
            }


            if (viewMode != ViewMode.Unknown)
            {
                // BTS#14894 - Determine controlReference size only when view mode is valid
                ResetPageAreaGrid(viewMode, _viewControlViewModel.ViewOrientation);
            }

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
                        TrimTitleText(viewMode); // Update title text on resize
                        break;
                    }
                case ViewMode.RightPaneVisible: // NOTE: Technically not possible
                case ViewMode.RightPaneVisible_ResizedWidth:
                    {
                        DisablePreviewGestures();
                        TrimTitleText(viewMode); // Update title text on resize
                        break;
                    }
            }
        }

        private void SetViewOrientation(ViewOrientation viewOrientation)
        {
            ResetPageAreaGrid(_viewControlViewModel.ViewMode, viewOrientation);
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

        /// <summary>
        /// Document name
        /// </summary>
        public string DocumentTitleText { get; set; }

        /// <summary>
        /// Text label for the Print Preview Screen based on the document name
        /// </summary>
        public string TrimmedTitleText
        {
            get { return _trimmedTitleText; }
            set
            {
                if (_trimmedTitleText != value)
                {
                    _trimmedTitleText = value;
                    RaisePropertyChanged("TrimmedTitleText");
                }
            }
        }

        /// <summary>
        /// Handler when view control is loaded.
        /// </summary>
        /// <param name="viewControl">view control</param>
        public void SetViewControl(ViewControl viewControl)
        {
            // Update title text
            _mainMenuButtonWidth = viewControl.MainMenuButtonWidth;
            _printSettingsButtonWidth = viewControl.Button1Width;
            TrimTitleText(new ViewModelLocator().ViewControlViewModel.ViewMode);
        }

        /// <summary>
        /// Updates the title text of the screen. Performs middle trim if needed.
        /// </summary>
        /// <param name="viewMode">current view mode</param>
        private void TrimTitleText(ViewMode viewMode)
        {
            // Adjust widths
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var rightPaneWidth = (double)Application.Current.Resources["SIZE_SidePaneWidthWithBorder"];

            var maxTextWidth = (int)Window.Current.Bounds.Width;

            // Left and right margins
            maxTextWidth -= ((int)defaultMargin * 2);

            // Main menu button is always visible
            maxTextWidth -= (int)_mainMenuButtonWidth;
            maxTextWidth -= (int)defaultMargin;

            // Print Settings button is always visible
            maxTextWidth -= (int)_printSettingsButtonWidth;
            maxTextWidth -= (int)defaultMargin;

            // RightPaneVisible_ResizeWidth view mode
            if (viewMode == ViewMode.RightPaneVisible_ResizedWidth)
            {
                maxTextWidth -= (int)rightPaneWidth;
            }

            TrimmedTitleText = (string)new TitleToMiddleTrimmedTextConverter().Convert(
                DocumentTitleText, null, (double)maxTextWidth, null);
        }

        /// <summary>
        /// Right back page image
        /// </summary>
        public WriteableBitmap RightBackPageImage
        {
            get { return _rightBackPageImage; }
            set
            {
                if (_rightBackPageImage != value)
                {
                    _rightBackPageImage = value;
                    RaisePropertyChanged("RightBackPageImage");
                }
            }
        }

        /// <summary>
        /// Left back page image
        /// </summary>
        public WriteableBitmap LeftBackPageImage
        {
            get { return _leftBackPageImage; }
            set
            {
                if (_leftBackPageImage != value)
                {
                    _leftBackPageImage = value;
                    RaisePropertyChanged("LeftBackPageImage");
                }
            }
        }

        /// <summary>
        /// Right page image
        /// </summary>
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

        /// <summary>
        /// Left page image
        /// </summary>
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

        /// <summary>
        /// Right next page image
        /// </summary>
        public WriteableBitmap RightNextPageImage
        {
            get { return _rightNextPageImage; }
            set
            {
                if (_rightNextPageImage != value)
                {
                    _rightNextPageImage = value;
                    RaisePropertyChanged("RightNextPageImage");
                }
            }
        }

        /// <summary>
        /// Left next page image
        /// </summary>
        public WriteableBitmap LeftNextPageImage
        {
            get { return _leftNextPageImage; }
            set
            {
                if (_leftNextPageImage != value)
                {
                    _leftNextPageImage = value;
                    RaisePropertyChanged("LeftNextPageImage");
                }
            }
        }

        /// <summary>
        /// Right page image actual size
        /// </summary>
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

        /// <summary>
        /// Left page image actual size
        /// </summary>
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

        /// <summary>
        /// Gets/sets the current page view mode
        /// </summary>
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

        /// <summary>
        /// True when page area grid is already loaded, false otherwise
        /// </summary>
        public bool IsPageAreaGridLoaded { get; private set; }

        #endregion

        #region SINGLE-PAGE NAVIGATION

        /// <summary>
        /// Command for turn to previous page
        /// </summary>
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

        /// <summary>
        /// Command for turn to next page
        /// </summary>
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

        /// <summary>
        /// Page number information
        /// </summary>
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

        private bool _isBooklet;

        /// <summary>
        /// Sets the initial page index
        /// </summary>
        /// <param name="index">page index</param>
        /// <param name="isBooklet">true when booklet is enabled, false otherwise</param>
        /// <param name="pagesPerSheet">Pages printed per sheet</param>
        public void SetInitialPageIndex(uint index, bool isBooklet, uint pagesPerSheet)
        {
            _pageIndex = index;
            _isBooklet = isBooklet;
            _pagesPerSheet = pagesPerSheet;
            SetPageIndexes();

            IsPageNumberSliderEnabled = true;
        }

        /// <summary>
        /// Go to page event handler
        /// </summary>
        /// <param name="index"></param>
        public void GoToPage(uint index)
        {
            if (GoToPageEventHandler != null)
            {
                GoToPageEventHandler((int)index);
            }
        }

        /// <summary>
        /// Updates the page index
        /// </summary>
        /// <param name="index">page index</param>
        /// <param name="isBooklet">true when booklet is enabled, false otherwise</param>
        /// <param name="pagesPerSheet">Pages printed per sheet</param>
        public void UpdatePageIndexes(uint index, bool isBooklet, uint pagesPerSheet)
        {
            _pageIndex = index;
            _isBooklet = isBooklet;
            _pagesPerSheet = pagesPerSheet;
            SetPageIndexes();
        }

        /// <summary>
        /// Turn to previous page handler
        /// </summary>
        private void GoToPreviousPageExecute()
        {
            --_pageIndex;
            SetPageIndexes();
            GoToPage(_pageIndex);
        }

        /// <summary>
        /// Checks if can turn to previous page
        /// </summary>
        /// <returns>true if allowed, false otherwise</returns>
        private bool CanGoToPreviousPage()
        {
            // Check if at least second page
            if (_pageIndex > 0) return true;
            return false;
        }

        /// <summary>
        /// Turn to next page handler
        /// </summary>
        private void GoToNextPageExecute()
        {
            ++_pageIndex;
            SetPageIndexes();
            GoToPage(_pageIndex);
        }

        /// <summary>
        /// Checks if can turn to next page
        /// </summary>
        /// <returns>true when allowed, false otherwise</returns>
        private bool CanGoToNextPage()
        {
            // Check if at least second to the last page
            if (_pageIndex < _pageTotal - 1) return true;
            return false;
        }
        
        private void SetPageIndexes()
        {
            PageNumber = new PageNumberInfo(_pageIndex, DocumentController.Instance.PageCount, _pageViewMode, _isBooklet, _pagesPerSheet);
            CurrentPageIndex = _pageIndex;
        }

        #endregion

        #region MULTI-PAGE NAVIGATION

        /// <summary>
        /// Command for page slider value changed
        /// </summary>
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

        /// <summary>
        /// Command for pointer capture lost of page slider
        /// </summary>
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

        /// <summary>
        /// Page total
        /// </summary>
        public uint PageTotal
        {
            get { return _pageTotal; }
            set
            {
                if (_pageTotal != value)
                {
                    _pageTotal = value;
                    RaisePropertyChanged("PageTotal");
                    if (_gestureController != null)
                    {
                        _gestureController.SetPageTotal(_pageTotal);
                    }
                }
            }
        }

        /// <summary>
        /// Current page index
        /// </summary>
        public uint CurrentPageIndex
        {
            get 
            {
                return _currentPageIndex;
            }
            set
            {
                if (_gestureController != null)
                {
                    _gestureController.SetPageIndex(_currentPageIndex);
                }
                if (_currentPageIndex != value)
                {
                    _currentPageIndex = value;
                    RaisePropertyChanged("CurrentPageIndex");
                }
            }
        }

        /// <summary>
        /// Gets/sets the enabled state of page slider.
        /// True when enabled, false otherwise.
        /// </summary>
        public bool IsPageNumberSliderEnabled
        {
            get { return _isPageNumberSliderEnabled; }
            set
            {
                // If document has only one page, always disable
                if (DocumentController.Instance.PageCount == 1)
                {
                    _isPageNumberSliderEnabled = false;
                    RaisePropertyChanged("IsPageNumberSliderEnabled");
                }
                else if (_isPageNumberSliderEnabled != value)
                {
                    _isPageNumberSliderEnabled = value;
                    RaisePropertyChanged("IsPageNumberSliderEnabled");
                }
            }
        }

        private void PageNumberSliderValueChangeExecute()
        {
            var newValue = CurrentPageIndex;
            UpdatePageIndexes(newValue, _isBooklet, _pagesPerSheet);
        }

        private void PageNumberSliderPointerCaptureLostExecute()
        {
            GoToPage(_pageIndex);
        }

        #endregion

    }
}