//
//  PreviewGestureController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Controls;
using SmartDeviceApp.ViewModels;
using System;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Animation;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Controllers
{
    public class PreviewGestureController : IDisposable
    {
        private const int SWIPE_THRESHOLD = 10;
        private const int MAX_ZOOM_LEVEL_FACTOR = 4;
        private const int RECT_BOUND = 80000; // This is defined in TwoPageControl.xaml (see RectangleGeometry)

        private const string TRANSFORMPROP_X = "X";
        private const string TRANSFORMPROP_Y = "Y";
        private const string TRANSFORMPROP_CENTER_X = "CenterX";
        private const string TRANSFORMPROP_CENTER_Y = "CenterY";
        private const string TRANSFORMPROP_ANGLE = "Angle";
        private const string TRANSFORMPROP_TRANSLATE_X = "TranslateX";
        private const string TRANSFORMPROP_TRANSLATE_Y = "TranslateY";
        private const string TRANSFORMPROP_ROTATION = "Rotation";

        private GestureRecognizer _gestureRecognizer;
        private UIElement _control;
        private UIElement _controlReference;
        private UIElement _pageAreaGrid;
        private UIElement _transitionGrid;
        private UIElement _displayAreaGrid;
        private TransformGroup _cumulativeTransform;
        private MatrixTransform _previousTransform;
        private CompositeTransform _deltaTransform;
        private TransformGroup _tempCumulativeTransform;
        private MatrixTransform _tempPreviousTransform;
        private CompositeTransform _tempDeltaTransform;

        private SwipeRightDelegate _swipeRightHandler;
        private SwipeLeftDelegate _swipeLeftHandler;
        private SwipeTopDelegate _swipeTopHandler;
        private SwipeBottomDelegate _swipeBottomHandler;
        private SwipeDirectionDelegate _swipeDirectionHandler;
        private bool _isHorizontalSwipeEnabled;
        private bool _isReverse;

        private Size _targetSize;
        private Size _controlSize;
        private Point _center;
        private double _originalScale;
        private bool _isScaled;
        private Point _startPoint;
        private double _currentZoomLength; // based on width
        private double _maxZoomLength; // based on width * max zoom level factor
        //private bool _isDirectionSet;
        private bool _isDuplex;

        private bool _isEnabled;
        private bool _isDisposed;

        private bool _isTranslateXEnabled;
        private bool _isTranslateYEnabled;

        private TwoPageControl _twoPageControl;

        private uint pointerCount = 0;
        private bool _multipleFingersDetected = false;

        private double _rotationCenterX;
        private double _rotationCenterY;
        private bool _isOnGridFirstHalf; // Flag for back curl, if going to previous page
        //private bool _isPageTurnNext;
        //private bool _willContinue;

        private uint _currPageIndex;
        private uint _totalPages;
        private bool _manipulationCancel;

        /// <summary>
        /// PreviewGestureController class constructor
        /// </summary>
        /// <param name="twoPageControl">two-page control</param>
        /// <param name="controlReference">control reference</param>
        /// <param name="targetSize">target size</param>
        /// <param name="originalScale">original scale</param>
        /// <param name="swipeRightHandler">swipe right handler</param>
        /// <param name="swipeLeftHandler">swipe left handler</param>
        /// <param name="isDuplex">true when duplex is enabled, false otherwise</param>
        /// <param name="currPageIndex">current page index</param>
        /// <param name="totalPages">total page count</param>
        public PreviewGestureController(TwoPageControl twoPageControl, UIElement controlReference,
            Size targetSize, double originalScale, SwipeRightDelegate swipeRightHandler,
            SwipeLeftDelegate swipeLeftHandler, bool isDuplex, uint currPageIndex, uint totalPages)
        {
            _twoPageControl = twoPageControl;
            _control = twoPageControl.ManipulationGrid;
            _controlReference = controlReference;
            _targetSize = targetSize;
            _originalScale = originalScale;
            _swipeRightHandler = swipeRightHandler;
            _swipeLeftHandler = swipeLeftHandler;
            _displayAreaGrid = twoPageControl.DisplayAreaGrid;
            _pageAreaGrid = twoPageControl.PageAreaGrid;
            _transitionGrid = twoPageControl.TransitionGrid;
            _isDuplex = isDuplex;
            _currPageIndex = currPageIndex;
            _totalPages = totalPages;

            Initialize();
            _currentZoomLength = _targetSize.Width;
            _maxZoomLength = _targetSize.Width * MAX_ZOOM_LEVEL_FACTOR;

            ((ScrollViewer)_controlReference).SizeChanged += ControlReferenceSizeChanged;
        }

        /// <summary>
        /// Initializes swipe event handlers
        /// </summary>
        /// <param name="isHorizontalSwipeEnabled">true when horizontal swipe is enabled, false otherwise</param>
        /// <param name="isReverse">true when reverse (for handling of swipe) is enabled, false otherwise</param>
        /// <param name="swipeLeftHandler">swipe left handler</param>
        /// <param name="swipeRightHandler">swipe right handler</param>
        /// <param name="swipeTopHandler">swipe top handler</param>
        /// <param name="swipeBottomHandler">swipe bottom handler</param>
        /// <param name="swipeDirectionHandler">swipe direction handler</param>
        public void InitializeSwipe(bool isHorizontalSwipeEnabled, bool isReverse,
            SwipeLeftDelegate swipeLeftHandler,
            SwipeRightDelegate swipeRightHandler,
            SwipeTopDelegate swipeTopHandler,
            SwipeBottomDelegate swipeBottomHandler,
            SwipeDirectionDelegate swipeDirectionHandler)
        {
            _isHorizontalSwipeEnabled = isHorizontalSwipeEnabled;
            _isReverse = isReverse;
            _swipeLeftHandler = swipeLeftHandler;
            _swipeRightHandler = swipeRightHandler;
            _swipeTopHandler = swipeTopHandler;
            _swipeBottomHandler = swipeBottomHandler;
            _swipeDirectionHandler = swipeDirectionHandler;

            ResetClipTransforms();
        }

        /// <summary>
        /// Swipe right delegate
        /// </summary>
        public delegate void SwipeRightDelegate();

        /// <summary>
        /// Swipe left delegate
        /// </summary>
        public delegate void SwipeLeftDelegate();

        /// <summary>
        /// Swipe top delegate
        /// </summary>
        public delegate void SwipeTopDelegate();

        /// <summary>
        /// Swipe bottom delegate
        /// </summary>
        public delegate void SwipeBottomDelegate();

        /// <summary>
        /// Swipe direction delegate
        /// </summary>
        /// <param name="isSwipeLeft"></param>
        public delegate void SwipeDirectionDelegate(bool isSwipeLeft);

        private void Initialize()
        {
            _gestureRecognizer = new GestureRecognizer();
            _gestureRecognizer.GestureSettings =
                GestureSettings.Tap |
                GestureSettings.Hold | //hold must be set in order to recognize the press & hold gesture
                GestureSettings.RightTap |
                GestureSettings.DoubleTap |
                GestureSettings.ManipulationTranslateX |
                GestureSettings.ManipulationTranslateY |
                GestureSettings.ManipulationScale |
                GestureSettings.ManipulationTranslateInertia |
                GestureSettings.ManipulationMultipleFingerPanning  | //reduces zoom jitter when panning with multiple fingers
                GestureSettings.ManipulationScaleInertia;

            EnableGestures();
            
            // InitializeTransforms
            _controlSize = new Size(((ScrollViewer)_controlReference).Width, ((ScrollViewer)_controlReference).Height);  // BTS#14894 - Use size set programmatically from caller class. Actual size will be retrieved on SizeChanged event.
            _center = new Point(_controlSize.Width / 2, _controlSize.Height / 2);
            ResetTransforms();
        }

        /// <summary>
        /// Enables handling of gestures
        /// </summary>
        public void EnableGestures()
        {
            if (!_isEnabled)
            {
                _control.PointerCanceled += OnPointerCanceled;
                _control.PointerPressed += OnPointerPressed;
                _control.PointerReleased += OnPointerReleased;
                _control.PointerMoved += OnPointerMoved;
                _control.PointerCaptureLost += OnPointerCaptureLost;

                // Gesture recognizer outputs
                _gestureRecognizer.Tapped += OnTapped;
                _gestureRecognizer.RightTapped += OnRightTapped;
                _gestureRecognizer.ManipulationStarted += OnManipulationStarted;
                _gestureRecognizer.ManipulationUpdated += OnManipulationUpdated;
                _gestureRecognizer.ManipulationCompleted += OnManipulationCompleted;
                _isEnabled = true;
            }
        }

        private void OnPointerCaptureLost(object sender, PointerRoutedEventArgs e)
        {
            if (pointerCount > 0)
                pointerCount--;
        }

        /// <summary>
        /// Disables handling of gestures
        /// </summary>
        public void DisableGestures()
        {
            if (_isEnabled)
            {
                _control.PointerCanceled -= OnPointerCanceled;
                _control.PointerPressed -= OnPointerPressed;
                _control.PointerReleased -= OnPointerReleased;
                _control.PointerMoved -= OnPointerMoved;
                _control.PointerCaptureLost -= OnPointerCaptureLost;

                // Gesture recognizer outputs
                _gestureRecognizer.Tapped -= OnTapped;
                _gestureRecognizer.RightTapped -= OnRightTapped;
                _gestureRecognizer.ManipulationStarted -= OnManipulationStarted;
                _gestureRecognizer.ManipulationUpdated -= OnManipulationUpdated;
                _gestureRecognizer.ManipulationCompleted -= OnManipulationCompleted;
                _isEnabled = false;
            }
        }

        /// <summary>
        /// Disposes this object
        /// </summary>
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (_isDisposed) return;

            if (disposing)
            {
                DisableGestures();

                // Unregister SizeChanged event since gesture controller will be disposed.
                // This is to avoid multiple (unnecessary) calls to SizeChanged event.
                ((ScrollViewer)_controlReference).SizeChanged -= ControlReferenceSizeChanged;

                _gestureRecognizer = null; 
            }
            _isDisposed = true;
        }

        private void ControlReferenceSizeChanged(Object sender, SizeChangedEventArgs e)
        {
            _controlSize = new Size(((ScrollViewer)_controlReference).ActualWidth, ((ScrollViewer)_controlReference).ActualHeight);
            _center = new Point(_controlSize.Width / 2, _controlSize.Height / 2);
            Normalize();
        }

        private void ResetTransforms()
        {
            _cumulativeTransform = new TransformGroup();
            _deltaTransform = new CompositeTransform();
            _previousTransform = new MatrixTransform() { Matrix = Matrix.Identity };
            _cumulativeTransform.Children.Add(_previousTransform);
            _cumulativeTransform.Children.Add(_deltaTransform);

            _control.RenderTransform = _cumulativeTransform;

            // Temp transforms for checking validity of transforms before applying
            _tempCumulativeTransform = new TransformGroup();
            _tempDeltaTransform = new CompositeTransform();
            _tempPreviousTransform = new MatrixTransform() { Matrix = Matrix.Identity };
            _tempCumulativeTransform.Children.Add(_tempPreviousTransform);
            _tempCumulativeTransform.Children.Add(_tempDeltaTransform);

            // Set original scale
            _deltaTransform.CenterX = _center.X;
            _deltaTransform.CenterY = _center.Y;
            _deltaTransform.ScaleX = _deltaTransform.ScaleY = _originalScale;

            _currentZoomLength = _targetSize.Width;

            Normalize();
            // Reset scale
            _isScaled = false;
        }

        private void ResetClipTransforms()
        {
            var w = _targetSize.Width;
            var h = _targetSize.Height;

            if (_isHorizontalSwipeEnabled)
            {
                _twoPageControl.Page1TranslateTransform.X = 0;
                _twoPageControl.Page1TranslateTransform.Y = 0;


                _twoPageControl.Page2TranslateTransform.X = w;
                _twoPageControl.Page2TranslateTransform.Y = 0;
                _twoPageControl.Page2RotateTransform.CenterX = 0;
                _twoPageControl.Page2RotateTransform.CenterY = 0;
                _twoPageControl.Page2RotateTransform.Angle = 0;

                _twoPageControl.TransitionTranslateTransform.X = -RECT_BOUND;
                _twoPageControl.TransitionTranslateTransform.Y = 0;
                _twoPageControl.TransitionRotateTransform.CenterX = 0;
                _twoPageControl.TransitionRotateTransform.CenterY = 0;
                _twoPageControl.TransitionRotateTransform.Angle = 0;

                _twoPageControl.TransitionContainerTransform.TranslateX = w;
                _twoPageControl.TransitionContainerTransform.TranslateY = 0;
                _twoPageControl.TransitionContainerTransform.CenterX = 0;
                _twoPageControl.TransitionContainerTransform.CenterY = 0;
                _twoPageControl.TransitionContainerTransform.Rotation = 0;
            }
            else
            {
                _twoPageControl.Page1TranslateTransform.X = 0;
                _twoPageControl.Page1TranslateTransform.Y = 0;

                _twoPageControl.Page2TranslateTransform.Y = h;
                _twoPageControl.Page2TranslateTransform.X = 0;
                _twoPageControl.Page2RotateTransform.CenterX = 0;
                _twoPageControl.Page2RotateTransform.CenterY = 0;
                _twoPageControl.Page2RotateTransform.Angle = 0;

                _twoPageControl.TransitionTranslateTransform.Y = -RECT_BOUND;
                _twoPageControl.TransitionTranslateTransform.X = 0;
                _twoPageControl.TransitionRotateTransform.CenterX = 0;
                _twoPageControl.TransitionRotateTransform.CenterY = 0;
                _twoPageControl.TransitionRotateTransform.Angle = 0;

                _twoPageControl.TransitionContainerTransform.TranslateY = h;
                _twoPageControl.TransitionContainerTransform.TranslateX = 0;
                _twoPageControl.TransitionContainerTransform.CenterX = 0;
                _twoPageControl.TransitionContainerTransform.CenterY = 0;
                _twoPageControl.TransitionContainerTransform.Rotation = 0;
            }

        }

        void OnPointerPressed(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
            _gestureRecognizer.ProcessDownEvent(args.GetCurrentPoint(_controlReference));
            pointerCount++;
            _startPoint = args.GetCurrentPoint((Grid)sender).Position;
            _control.CapturePointer(args.Pointer);
            args.Handled = true;
            
        }

        void OnPointerReleased(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
            if(pointerCount > 0)
                pointerCount--;
            _control.ReleasePointerCapture(args.Pointer);
            _gestureRecognizer.ProcessUpEvent(args.GetCurrentPoint(_controlReference));
            args.Handled = true;
        }

        void OnPointerCanceled(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
            pointerCount--;
            _gestureRecognizer.CompleteGesture();
            args.Handled = true;
        }
        void OnPointerMoved(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
            if (pointerCount > 1)
                _multipleFingersDetected = true;
            else
                _multipleFingersDetected = false;

            _gestureRecognizer.ProcessMoveEvents(args.GetIntermediatePoints(_control));
            args.Handled = true;
        }

        void OnTapped(object sender, TappedEventArgs e)
        {
            if (e.TapCount == 2) // Double tap for reset zoom
            {
                ResetTransforms();
            }
        }

        void OnRightTapped(object sender, RightTappedEventArgs e)
        {
        }

        private void OnManipulationStarted(object sender, ManipulationStartedEventArgs e)
        {
            if (!_multipleFingersDetected)
                ManipulationGrid_ManipulationStarted(e);
        }

        private void OnManipulationUpdated(object sender, ManipulationUpdatedEventArgs e)
        {
            //var isSwipe = DetectSwipe(e);
            //if (isSwipe) return;
            if (_multipleFingersDetected || _isScaled)
            {
                var isScale = DetectScale(e);

                DetectTranslate(e, isScale);

                ManipulationGrid_ManipulationCancel();
            }
            else 
            {
                if (!_isScaled)
                {
                    if (e.Delta.Scale != 1.0f)
                    {
                        Normalize();
                    }
                    ManipulationGrid_ManipulationDelta(e);
                }
                else
                {
                    ManipulationGrid_ManipulationCancel();
                }
                
            }

        }

        //private bool DetectSwipe(ManipulationUpdatedEventArgs e)
        //{
        //    var isSwipe = false;
        //    if (!_isScaled && _gestureRecognizer.IsInertial)
        //    {                
        //        Point currentPosition = e.Position;
        //        // Horizontal Swipe
        //        if (_isHorizontalSwipeEnabled) 
        //        {
        //            // Swipe right;
        //            if (currentPosition.X - _startPoint.X >= SWIPE_THRESHOLD)
        //            {
        //                _gestureRecognizer.CompleteGesture();
        //                _swipeRightHandler();
        //                isSwipe = true;
        //            }
        //            // Swipe left
        //            else if (_startPoint.X - currentPosition.X >= SWIPE_THRESHOLD)
        //            {
        //                _gestureRecognizer.CompleteGesture();
        //                _swipeLeftHandler();
        //                isSwipe = true;
        //            }
        //        }
        //        // Vertical swipe
        //        else 
        //        {
        //            // Swipe bottom
        //            if (currentPosition.Y - _startPoint.Y >= SWIPE_THRESHOLD)
        //            {
        //                _gestureRecognizer.CompleteGesture();
        //                _swipeBottomHandler();
        //                isSwipe = true;
        //            }
        //            // Swipe top
        //            else if (_startPoint.Y - currentPosition.Y >= SWIPE_THRESHOLD)
        //            {
        //                _gestureRecognizer.CompleteGesture();
        //                _swipeTopHandler();
        //                isSwipe = true;
        //            }
        //        }
        //    }
        //    return isSwipe;
        //}

        private bool DetectScale(ManipulationUpdatedEventArgs e)
        {
            var isScale = false; // Currently scaling
            float scale = e.Delta.Scale;

            if (scale == 1 || // No scale change
                (_currentZoomLength > _maxZoomLength && scale > 1)) // Prevent scale up on maximum
            {
                return isScale;
            }

            double tempWidth = _currentZoomLength * scale;
            if (tempWidth > _maxZoomLength)
            {
                scale = (float)_maxZoomLength / (float)_currentZoomLength; // Change scale to maximum
                tempWidth = _currentZoomLength * scale;
            }

            _tempPreviousTransform.Matrix = _tempCumulativeTransform.Value;

            // Get scale center
            Point center = new Point(e.Position.X, e.Position.Y);
            _tempDeltaTransform.CenterX = center.X;
            _tempDeltaTransform.CenterY = center.Y;

            // Apply scaling on temp transforms first
            _tempDeltaTransform.ScaleX = _tempDeltaTransform.ScaleY = scale;
            
            // Check if scale is valid, do not scale less than original size
            if (_tempCumulativeTransform.Value.M11 > 1)
            {
                // Check if scaling, current scale is changed
                if (Math.Abs(_cumulativeTransform.Value.M11 - _tempCumulativeTransform.Value.M11) > 0)
                {
                    _previousTransform.Matrix = _cumulativeTransform.Value;
                    _deltaTransform.CenterX = center.X;
                    _deltaTransform.CenterY = center.Y;
                    _deltaTransform.ScaleX = _deltaTransform.ScaleY = scale;
                    _deltaTransform.TranslateX = _deltaTransform.TranslateY = 0;

                    _isScaled = true; // not original size
                    isScale = true;
                    _isTranslateXEnabled = true;
                    _isTranslateYEnabled = true;

                    _currentZoomLength = tempWidth;
                }
            }
            else
            {
                // Invalid scale
                ResetTransforms();
            }
            return isScale;
        }

        private void Normalize()
        {
            FitToEdges();
            FitToCenter();
        }

        private void FitToEdges()
        {
            // t1 = target top-left point, t2 = target bottom-right point
            var t1 = new Point(_cumulativeTransform.Value.OffsetX, _cumulativeTransform.Value.OffsetY);
            var t2 = new Point(_cumulativeTransform.Value.OffsetX + _targetSize.Width * _cumulativeTransform.Value.M11,
                _cumulativeTransform.Value.OffsetY + _targetSize.Height * _cumulativeTransform.Value.M11);
            // c1 = control top-left point, c2 = control bottom-right point
            var c1 = new Point(0, 0);
            var c2 = new Point(_controlSize.Width, _controlSize.Height);

            // Horizontal check
            // Case 1: t1x, t2x inside left/right edges: Do nothing
            // Case 2: both t1x and t2x outside left and right edges: Do nothing
            // Case 3: t1x outside left edge, t2x inside right edge
            if (t1.X < c1.X && t2.X <= c2.X)
            {
                // Adjust t1X to move inside left edge if there is enough space to move inside right edge,
                // Otherwise move only up to right edge
                var offset1 = Math.Abs(t1.X);
                var offset2 = c2.X - t2.X;
                if (offset1 <= offset2) _deltaTransform.TranslateX += offset1;
                else _deltaTransform.TranslateX += offset2;
            }
            // Case 4: t2x outside right edge, t1x inside left edge
            else if (t2.X > c2.X && t1.X >= c1.X)
            {
                // Adjust t2X to move inside right edge if there is enough space to move inside left edge,
                // Otherwise move only up to left edge
                var offset1 = t2.X - c2.X;
                var offset2 = t1.X;
                if (offset1 <= offset2) _deltaTransform.TranslateX -= offset1;
                else _deltaTransform.TranslateX -= offset2;
            }

            // Vertical check
            // Case 1: t1y, t2y inside left/right edges: Do nothing
            // Case 2: both t1y and t2y outside left and right edges: Do nothing
            // Case 3: t1y outside left edge, t2y inside right edge
            if (t1.Y < c1.Y && t2.Y <= c2.Y)
            {
                // Adjust t1Y to move inside left edge if there is enough space to move inside right edge,
                // Otherwise move only up to right edge
                var offset1 = Math.Abs(t1.Y);
                var offset2 = c2.Y - t2.Y;
                if (offset1 <= offset2) _deltaTransform.TranslateY += offset1;
                else _deltaTransform.TranslateY += offset2;
            }
            // Case 4: t2y outside right edge, t1y inside left edge
            else if (t2.Y > c2.Y && t1.Y >= c1.Y)
            {
                // Adjust t2Y to move inside right edge if there is enough space to move inside left edge,
                // Otherwise move only up to left edge
                var offset1 = t2.Y - c2.Y;
                var offset2 = t1.Y;
                if (offset1 <= offset2) _deltaTransform.TranslateY -= offset1;
                else _deltaTransform.TranslateY -= offset2;
            }
        }

        private void FitToCenter()
        {
            //get pageview mode
            var viewModel = new ViewModelLocator().PrintPreviewViewModel;
 
            // t1 = target top-left point, t2 = target bottom-right point
            var t1 = new Point(_cumulativeTransform.Value.OffsetX, _cumulativeTransform.Value.OffsetY);
            var t2 = new Point(_cumulativeTransform.Value.OffsetX + _targetSize.Width * _cumulativeTransform.Value.M11,
                _cumulativeTransform.Value.OffsetY + _targetSize.Height * _cumulativeTransform.Value.M11);
            // c1 = control top-left point, c2 = control bottom-right point
            var c1 = new Point(0, 0);
            var c2 = new Point(_controlSize.Width, _controlSize.Height);
            // p = center point
            var p = new Point(_controlSize.Width / 2 - _targetSize.Width * _cumulativeTransform.Value.M11 / 2, 
                _controlSize.Height / 2 - _targetSize.Height * _cumulativeTransform.Value.M11 / 2);

            if (t1.X >= c1.X && t2.X <= c2.X)
            { 
                _deltaTransform.TranslateX += (p.X - t1.X);
                _isTranslateXEnabled = false; // Disable horizontal panning if not zoomed
            }
            if (t1.Y >= c1.Y && t2.Y <= c2.Y)
            {
                _deltaTransform.TranslateY += (p.Y - t1.Y);
                _isTranslateYEnabled = false; // Disable vertical panning if not zoomed
            }
        }

        private bool DetectTranslate(ManipulationUpdatedEventArgs e, bool isScale)
        {
            var isTranslate = false;
            if (_isScaled && !isScale && (_isTranslateXEnabled || _isTranslateYEnabled))
            {
                _previousTransform.Matrix = _cumulativeTransform.Value;
                _deltaTransform.ScaleX = _deltaTransform.ScaleY = 1;
                if (_isTranslateXEnabled) _deltaTransform.TranslateX = e.Delta.Translation.X;
                if (_isTranslateYEnabled) _deltaTransform.TranslateY = e.Delta.Translation.Y;
                Normalize();
                isTranslate = true;
            }
            return isTranslate;
        }

        private void OnManipulationCompleted(object sender, ManipulationCompletedEventArgs e)
        {
            Normalize();
            if (!_multipleFingersDetected)
                ManipulationGrid_ManipulationCompleted(e);
        }

        private void ManipulationGrid_ManipulationStarted(ManipulationStartedEventArgs e)
        {
            var w = _targetSize.Width;
            var h = _targetSize.Height;

            _manipulationCancel = false;

            if (!DetectPageBounds())
            {
                ManipulationGrid_ManipulationCancel();
                return;
            }

            DetectPageTurnDirection(_startPoint);

            ResetClipTransforms();

            _transitionGrid.Opacity = 1;
            _pageAreaGrid.Opacity = 1;
            
        }

        private void ManipulationGrid_ManipulationDelta(ManipulationUpdatedEventArgs e)
        {
            var w = _targetSize.Width;
            var h = _targetSize.Height;

            var ttv2 = _twoPageControl.DisplayAreaGrid.TransformToVisual(_control);
            var pagePoint = ttv2.TransformPoint(e.Position);

            var currentPosition = pagePoint;

            if (_isHorizontalSwipeEnabled)
            {
                if (!DetectCanTurnPage(currentPosition))
                {
                    ManipulationGrid_ManipulationCancel();
                    return;
                }

                if (currentPosition.X - _startPoint.X == 0)
                {
                    return;
                }


                if (_isOnGridFirstHalf)
                {
                    if (_isDuplex) // Horizontal two-page view and back curl
                    {
                        // Scenarios:
                        // duplex left bind (turn back)
                        // duplex right bind (turn back)
                        // booklet portrait forward (turn back)
                        // booklet portrait reverse (turn back)
                        var tempW = -w * 2;
                        if (_isDuplex)
                        {
                            tempW = -w;
                        }

                        var cx = Math.Min(0, Math.Max(e.Position.X - w, tempW));
                        var cy = e.Cumulative.Translation.Y;
                        var angle = (Math.Atan2(cx - w, -cy) * 180 / Math.PI + 90) % 360;

                        _rotationCenterX = w + cx / 2;

                        if (cy < 0)
                        {
                            _rotationCenterY = h;
                        }
                        else
                        {
                            _rotationCenterY = 0;
                        }
                        
                        _twoPageControl.Page1TranslateTransform.X = w / 2;
                        _twoPageControl.Page1TranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;

                        _twoPageControl.Page2TranslateTransform.X = -RECT_BOUND + Math.Abs(w + cx / 2);
                        _twoPageControl.Page2TranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                        _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                        _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.Page2RotateTransform.Angle = angle;

                        _twoPageControl.TransitionTranslateTransform.X = -RECT_BOUND - (cx / 2);
                        _twoPageControl.TransitionTranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                        _twoPageControl.TransitionRotateTransform.CenterX = -cx / 2;
                        _twoPageControl.TransitionRotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.TransitionRotateTransform.Angle = -angle;

                        _twoPageControl.TransitionContainerTransform.TranslateX = w + cx;
                        _twoPageControl.TransitionContainerTransform.CenterX = -cx / 2;
                        _twoPageControl.TransitionContainerTransform.CenterY = _rotationCenterY;
                        _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;

                    }
                    else // Horizontal single page view and back curl
                    {
                        if (_isReverse) // single view right bind forward curl
                        {
                            w = w * 2;
                            var tempW = -w * 2;

                            var cx = Math.Min(0, Math.Max(e.Position.X - w, tempW));
                            var cy = e.Cumulative.Translation.Y;
                            var angle = -(Math.Atan2(cx + _startPoint.Y - w, -cy) * 180 / Math.PI + 90) % 360;

                            _rotationCenterX = w/2 + cx / 2;

                            if (cy < 0)
                            {
                                _rotationCenterY = h;
                            }
                            else
                            {
                                _rotationCenterY = 0;
                            }

                            _twoPageControl.Page2TranslateTransform.X = -RECT_BOUND + Math.Abs(e.Position.X / 2);
                            _twoPageControl.Page2TranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                            _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                            _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                            _twoPageControl.Page2RotateTransform.Angle = angle;

                            _twoPageControl.TransitionTranslateTransform.X = - (cx / 2);
                            _twoPageControl.TransitionTranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                            _twoPageControl.TransitionRotateTransform.CenterX = -cx / 2;
                            _twoPageControl.TransitionRotateTransform.CenterY = _rotationCenterY;
                            _twoPageControl.TransitionRotateTransform.Angle = -angle;

                            _twoPageControl.TransitionContainerTransform.TranslateX = w/2 + cx;
                            _twoPageControl.TransitionContainerTransform.CenterX = -cx / 2;
                            _twoPageControl.TransitionContainerTransform.CenterY = _rotationCenterY;
                            _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
                        }
                        else
                        {
                            // Scenarios:
                            // single left bind (turn back)
                            var tempW = -w * 2;
                            if (_isDuplex)
                            {
                                tempW = -w;
                            }

                            var cx = Math.Min(0, Math.Max(e.Position.X - w, tempW));
                            var cy = e.Cumulative.Translation.Y;
                            var angle = (Math.Atan2(cx - w, -cy) * 180 / Math.PI + 90) % 360;

                            _rotationCenterX = w + cx / 2;

                            if (cy < 0)
                            {
                                _rotationCenterY = h;
                            }
                            else
                            {
                                _rotationCenterY = 0;
                            }

                            _twoPageControl.Page2TranslateTransform.X = -RECT_BOUND + Math.Abs(w + cx / 2);
                            _twoPageControl.Page2TranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                            _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                            _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                            _twoPageControl.Page2RotateTransform.Angle = angle;

                            _twoPageControl.TransitionTranslateTransform.X = -RECT_BOUND - (cx / 2);
                            _twoPageControl.TransitionTranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                            _twoPageControl.TransitionRotateTransform.CenterX = -cx / 2;
                            _twoPageControl.TransitionRotateTransform.CenterY = _rotationCenterY;
                            _twoPageControl.TransitionRotateTransform.Angle = -angle;

                            _twoPageControl.TransitionContainerTransform.TranslateX = w + cx;
                            _twoPageControl.TransitionContainerTransform.CenterX = -cx / 2;
                            _twoPageControl.TransitionContainerTransform.CenterY = _rotationCenterY;
                            _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
                        }
                    }
                }
                else // Second half
                {
                    if (_isReverse && !_isDuplex) //single view right bind back curl
                    {
                        w = w * 2;
                        var tempW = -w * 2;

                        var cx = Math.Min(0, Math.Max(e.Position.X - w, tempW));
                        var cy = e.Cumulative.Translation.Y;
                        var angle = -(Math.Atan2(cx + _startPoint.Y - w, -cy) * 180 / Math.PI + 90) % 360;

                        _rotationCenterX = w/2 + cx / 2;

                        if (cy < 0)
                        {
                            _rotationCenterY = h;
                        }
                        else
                        {
                            _rotationCenterY = 0;
                        }

                        _twoPageControl.Page2TranslateTransform.X = (w/2) + cx / 2;
                        _twoPageControl.Page2TranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                        _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                        _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.Page2RotateTransform.Angle = angle;

                        _twoPageControl.TransitionTranslateTransform.X = -(cx / 2);
                        _twoPageControl.TransitionTranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                        _twoPageControl.TransitionRotateTransform.CenterX = -cx / 2;
                        _twoPageControl.TransitionRotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.TransitionRotateTransform.Angle = -angle;

                        _twoPageControl.TransitionContainerTransform.TranslateX = w / 2 + cx;
                        _twoPageControl.TransitionContainerTransform.CenterX = -cx / 2;
                        _twoPageControl.TransitionContainerTransform.CenterY = _rotationCenterY;
                        _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
                    }
                    else
                    {
                        var tempW = -w * 2;
                        if (_isDuplex)
                        {
                            tempW = -w;
                        }

                        var cx = Math.Min(0, Math.Max(e.Position.X - w, tempW));
                        var cy = e.Cumulative.Translation.Y;
                        var angle = (Math.Atan2(cx - w, -cy) * 180 / Math.PI + 90) % 360;

                        _rotationCenterX = w + cx / 2;

                        if (cy < 0)
                        {
                            _rotationCenterY = h;
                        }
                        else
                        {
                            _rotationCenterY = 0;
                        }

                        _twoPageControl.Page1TranslateTransform.X = -RECT_BOUND + Math.Abs(w + cx / 2);
                        _twoPageControl.Page1TranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                        _twoPageControl.Page1RotateTransform.CenterX = _rotationCenterX;
                        _twoPageControl.Page1RotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.Page1RotateTransform.Angle = angle;

                        _twoPageControl.Page2TranslateTransform.X = w + cx / 2;
                        _twoPageControl.Page2TranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                        _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                        _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.Page2RotateTransform.Angle = angle;

                        _twoPageControl.TransitionTranslateTransform.X = -RECT_BOUND - (cx / 2);
                        _twoPageControl.TransitionTranslateTransform.Y = -(RECT_BOUND / 2) + h / 2;
                        _twoPageControl.TransitionRotateTransform.CenterX = -cx / 2;
                        _twoPageControl.TransitionRotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.TransitionRotateTransform.Angle = -angle;

                        _twoPageControl.TransitionContainerTransform.TranslateX = w + cx;
                        _twoPageControl.TransitionContainerTransform.CenterX = -cx / 2;
                        _twoPageControl.TransitionContainerTransform.CenterY = _rotationCenterY;
                        _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
                    }
                    }
                //}
            }
            else // Vertical swipe
            {

                if (!DetectCanTurnPage(currentPosition))
                {
                    ManipulationGrid_ManipulationCancel();
                    return;
                }

                if (currentPosition.Y - _startPoint.Y == 0)
                {
                    return;
                }
                
                if (_isOnGridFirstHalf)
                {
                    //if (_isDuplex) // Vertical two-page view and back curl
                    //{
                    //    // Scenarios:
                    //    // duplex top bind (turn back)
                    //    // booklet landscape forward (turn back)
                    //    // booklet landscape reverse (turn back)
                    //}
                    //else // Vertical single page view and back curl =========================================> OK
                    //{
                        // Scenarios:
                        // single top bind (turn back)

                        var tempH = -h * 2;

                        var cy = Math.Min(0, Math.Max(e.Position.Y - h, tempH));
                        var cx = e.Cumulative.Translation.Y;
                        var angle = (Math.Atan2(cy - h, cx) * 180 / Math.PI + 90) % 360;

                        _rotationCenterY = h + cy / 2;

                        if (cx < 0)
                        {
                            _rotationCenterX = w;
                        }
                        else
                        {
                            _rotationCenterX = 0;
                        }

                        _twoPageControl.Page1TranslateTransform.Y = h / 2;
                        _twoPageControl.Page1TranslateTransform.X = -(RECT_BOUND / 2) + w / 2;

                        _twoPageControl.Page2TranslateTransform.Y = -RECT_BOUND + Math.Abs(h + cy / 2);
                        _twoPageControl.Page2TranslateTransform.X = -(RECT_BOUND / 2) + w / 2;
                        _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                        _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                        _twoPageControl.Page2RotateTransform.Angle = angle;

                        _twoPageControl.TransitionTranslateTransform.Y = -RECT_BOUND - (cy / 2);
                        _twoPageControl.TransitionTranslateTransform.X = -(RECT_BOUND / 2) + w / 2;
                        _twoPageControl.TransitionRotateTransform.CenterY = -cy / 2;
                        _twoPageControl.TransitionRotateTransform.CenterX = _rotationCenterX;
                        _twoPageControl.TransitionRotateTransform.Angle = -angle;

                        _twoPageControl.TransitionContainerTransform.TranslateY = h + cy;
                        _twoPageControl.TransitionContainerTransform.CenterY = -cy / 2;
                        _twoPageControl.TransitionContainerTransform.CenterX = _rotationCenterX;
                        _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
                    //}
                }
                else // Vertical forward curl =========================================> OK
                {
                    // Scenarios:
                    // single top bind (turn next)
                    // duplex top bind (turn next)
                    // booklet landscape forward (turn next)
                    // booklet landscape reverse (turn next)

                    var tempH = -h * 2;
                    if (_isDuplex)
                    {
                        tempH = -h;
                    }

                    var cy = Math.Min(0, Math.Max(e.Position.Y - h, tempH));
                    var cx = e.Cumulative.Translation.X;
                    var angle = (Math.Atan2(cy - h, cx) * 180 / Math.PI + 90) % 360;

                    _rotationCenterY = h + cy / 2;

                    if (cx < 0)
                    {
                        _rotationCenterX = w;
                    }
                    else
                    {
                        _rotationCenterX = 0;
                    }

                    _twoPageControl.Page1TranslateTransform.Y = -(RECT_BOUND) + Math.Abs(h + cy / 2);
                    _twoPageControl.Page1TranslateTransform.X = -(RECT_BOUND / 2) + w / 2;
                    _twoPageControl.Page1RotateTransform.CenterX = _rotationCenterX;
                    _twoPageControl.Page1RotateTransform.CenterY = _rotationCenterY;
                    _twoPageControl.Page1RotateTransform.Angle = angle;

                    _twoPageControl.Page2TranslateTransform.Y = h + cy / 2;
                    _twoPageControl.Page2TranslateTransform.X = -(RECT_BOUND / 2) - w / 2;
                    _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                    _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                    _twoPageControl.Page2RotateTransform.Angle = angle;

                    _twoPageControl.TransitionTranslateTransform.Y = -RECT_BOUND - (cy / 2);
                    _twoPageControl.TransitionTranslateTransform.X = -(RECT_BOUND / 2) + w / 2;
                    _twoPageControl.TransitionRotateTransform.CenterY = -cy / 2;
                    _twoPageControl.TransitionRotateTransform.CenterX = _rotationCenterX;
                    _twoPageControl.TransitionRotateTransform.Angle = -angle;

                    _twoPageControl.TransitionContainerTransform.TranslateY = h + cy;
                    _twoPageControl.TransitionContainerTransform.CenterY = -cy / 2;
                    _twoPageControl.TransitionContainerTransform.CenterX = _rotationCenterX;
                    _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
                }
            }
        }

        private void ManipulationGrid_ManipulationCompleted(ManipulationCompletedEventArgs e)
        {
            if (_manipulationCancel)
            {
                return;
            }

            var w = _targetSize.Width;
            var h = _targetSize.Height;

            var _willContinue = false;
            if (_isOnGridFirstHalf)
            {
                if (_isHorizontalSwipeEnabled)
                {
                    if (e.Position.X > w * 0.5)
                    {
                        _willContinue = true;
                    }
                }
                else
                {
                    if (e.Position.Y > h * 0.5)
                    {
                        _willContinue = true;
                    }
                }
            }
            else
            {
                if (_isHorizontalSwipeEnabled)
                {
                    if (e.Position.X < w * 0.5)
                    {
                        _willContinue = true;
                    }
                }
                else
                {
                    if (e.Position.Y < h * 0.5)
                    {
                        _willContinue = true;
                    }
                }
            }

            var sb = new Storyboard();
            if (_isHorizontalSwipeEnabled)
            {
                //var sb = new Storyboard();
                sb.Completed += StoryBoardAnimationCompleted;
                if (!_isOnGridFirstHalf)
                {
                    var to = 0;
                   
                    if (_willContinue)
                    {
                        if (_isDuplex)
                        {
                            to = (int)(0);
                        }
                        else
                        {
                            to = (int)-w;
                            if (_isReverse)
                                to = 0;
                        }
                    }
                    else
                    {
                        to = (int)w;
                    }
                    AddAnimation(sb, _twoPageControl.Page1TranslateTransform, TRANSFORMPROP_X, 0);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_CENTER_X, 0);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_ANGLE, 0);


                    if (_isDuplex)
                    {
                        AddAnimation(sb, _twoPageControl.Page2TranslateTransform, TRANSFORMPROP_X, to);
                        AddAnimation(sb, _twoPageControl.Page2RotateTransform, TRANSFORMPROP_CENTER_X, 0);
                        AddAnimation(sb, _twoPageControl.Page2RotateTransform, TRANSFORMPROP_ANGLE, 0);
                    }
                    else if (!_isReverse)
                    {
                        AddAnimation(sb, _twoPageControl.Page2TranslateTransform, TRANSFORMPROP_X, to);
                        AddAnimation(sb, _twoPageControl.Page2RotateTransform, TRANSFORMPROP_CENTER_X, 0);
                        AddAnimation(sb, _twoPageControl.Page2RotateTransform, TRANSFORMPROP_ANGLE, 0);
                    }
                    
                    if (_willContinue)
                    {
                        to = (int)(-RECT_BOUND + (w));
                        if (_isReverse && !_isDuplex)
                        {
                            to = (int)(-RECT_BOUND + (w * 2));
                        }
                    }
                    else
                    {
                        to = (int)-RECT_BOUND;
                    }
                    if (_isDuplex)
                    {
                        AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, TRANSFORMPROP_X, to);
                        AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_CENTER_X, 0);
                        AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_ANGLE, 0);
                    }
                    else if (!_isReverse)
                    {
                        AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, TRANSFORMPROP_X, to);
                        AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_CENTER_X, 0);
                        AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_ANGLE, 0);
                    }

                    if (_willContinue)
                    {
                        if (_isDuplex)
                        {
                            to = 0;
                        }
                        else
                        {
                            to = (int)-w;
                            if (_isReverse)
                            {
                                to = 0;
                            }        
                        }
                    }
                    else
                    {
                        to = (int)w;
                    }
                    if (_isDuplex)
                    {
                        AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_TRANSLATE_X, to);
                        AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_CENTER_X, 0);
                        AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_ROTATION, 0);    
                    }
                    else if (!_isReverse)
                    {
                        AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_TRANSLATE_X, to);
                        AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_CENTER_X, w/2);
                        AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_ROTATION, 0);
                    }
                    
                    sb.Begin();

                    if (_willContinue)
                    {
                        _swipeLeftHandler();
                    }
                }
                else
                {
                    var to = 0;
                    
                    AddAnimation(sb, _twoPageControl.Page1TranslateTransform, TRANSFORMPROP_X, to);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_CENTER_X, w / 2);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_ANGLE, 0);

                    if (_willContinue)
                    {
                        to = (int)(-RECT_BOUND);
                        if (_isReverse && !_isDuplex)
                            to = 0;
                    }
                    else
                    {
                        to = (int)0;
                        if (_isReverse && !_isDuplex)
                            to = (int)(-RECT_BOUND);
                    }
                    AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, TRANSFORMPROP_X, to);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_CENTER_X, w / 2);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_ANGLE, 0);

                    if (_willContinue)
                    {
                        to = (int)(-RECT_BOUND);
                        if (_isReverse && !_isDuplex)
                            to = (int)w;
                    }
                    else
                    {
                        to = (int)0;
                        if (_isReverse && !_isDuplex)
                            to = (int)(-RECT_BOUND);
                    }

                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_TRANSLATE_X, to);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_CENTER_X, w / 2);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_ROTATION, 0);
                    sb.Begin();

                    if (_willContinue)
                    {
                        _swipeRightHandler();
                    }
                }
            }
            else
            {
                sb.Completed += StoryBoardAnimationCompleted;
                if (!_isOnGridFirstHalf)
                {
                    var to = 0;
                    if (_willContinue)
                    {
                        if (_isDuplex)
                        {
                            to = (int)(0);
                        }
                        else
                        {
                            to = (int)-h;
                        }
                    }
                    else
                    {
                        to = (int)h;
                    }
                    AddAnimation(sb, _twoPageControl.Page1TranslateTransform, TRANSFORMPROP_Y, 0);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_CENTER_Y, 0);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_ANGLE, 0);

                    AddAnimation(sb, _twoPageControl.Page2TranslateTransform, TRANSFORMPROP_Y, to);
                    AddAnimation(sb, _twoPageControl.Page2RotateTransform, TRANSFORMPROP_CENTER_Y, 0);
                    AddAnimation(sb, _twoPageControl.Page2RotateTransform, TRANSFORMPROP_ANGLE, 0);

                    if (_willContinue)
                    {
                        to = (int)(-RECT_BOUND + (h));
                    }
                    else
                    {
                        to = (int)-RECT_BOUND;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, TRANSFORMPROP_Y, to);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_CENTER_Y, 0);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_ANGLE, 0);
                    if (_willContinue)
                    {
                        if (_isDuplex)
                            to = 0;
                        else
                            to = (int)-h;
                    }
                    else
                    {
                        to = (int)h;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_TRANSLATE_Y, to);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_CENTER_Y, 0);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_ROTATION, 0);
                    sb.Begin();

                    if (_willContinue)
                    {
                        _swipeTopHandler();
                    }
                }
                else
                {
                    AddAnimation(sb, _twoPageControl.Page1TranslateTransform, TRANSFORMPROP_Y, 0);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_CENTER_Y, h / 2);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, TRANSFORMPROP_ANGLE, 0);

                    var to = 0;
                    if (_willContinue)
                    {
                        to = (int)(-RECT_BOUND);
                    }
                    else
                    {
                        to = (int)0;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, TRANSFORMPROP_Y, to);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_CENTER_Y, h / 2);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, TRANSFORMPROP_ANGLE, 0);

                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_TRANSLATE_Y, to);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_CENTER_Y, h / 2);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, TRANSFORMPROP_ROTATION, 0);
                    sb.Begin();

                    if (_willContinue)
                    {
                        _swipeBottomHandler();
                    }
                }
            }
        }

        private void StoryBoardAnimationCompleted(object sender, object e)
        {
            // Reset
            _twoPageControl.Page2TranslateTransform.X = 0;
            _twoPageControl.PageAreaGrid.Opacity = 0;
            _twoPageControl.TransitionGrid.Opacity = 0;
        }

        private static void AddAnimation(Storyboard sb, DependencyObject dob, string path, double to)
        {
            var da = new DoubleAnimation();
            Storyboard.SetTarget(da, dob);
            Storyboard.SetTargetProperty(da, path);
            da.To = to;
            da.Duration = TimeSpan.FromSeconds(.2);
            sb.Children.Add(da);
        }

        private void ManipulationGrid_ManipulationCancel()
        {
            _manipulationCancel = true;
        }

        /// <summary>
        /// Updates the current page index
        /// </summary>
        /// <param name="index">page index</param>
        public void SetPageIndex(uint index)
        {
            _currPageIndex = index;
        }

        /// <summary>
        /// Updates the current page total
        /// </summary>
        /// <param name="pageTotal">page total</param>
        public void SetPageTotal(uint pageTotal)
        {
            _totalPages = pageTotal;
        }

        private void DetectPageTurnDirection(Point position)
        {
            bool isPageTurnNext = true;

            if (_isHorizontalSwipeEnabled)
            {
                var midpoint = _targetSize.Width * 0.5;

                if (position.X < midpoint)
                {
                    // Turn page backwards
                    isPageTurnNext = false;
                }
                // else, Turn page forward
            }
            else
            {
                var midpoint = _targetSize.Height * 0.5;

                if (position.Y < midpoint)
                {
                    // Turn page backwards
                    isPageTurnNext = false;
                }
            }

            _isOnGridFirstHalf = !isPageTurnNext;

            if (_isReverse)
            {
                isPageTurnNext = !isPageTurnNext;
            }

            _swipeDirectionHandler(isPageTurnNext);
        }

        private bool DetectCanTurnPage(Point currentPosition)
        {
            bool canTurnPage = true;
            if (_isHorizontalSwipeEnabled)
            {
                //forward
                var midpoint = _targetSize.Width * 0.5;
                if (_startPoint.X > midpoint)
                {
                    if (!_isReverse)
                    {
                        if (_currPageIndex == _totalPages - 1)
                        {
                            canTurnPage = false;
                        }
                    }
                    else
                    {
                        if (_currPageIndex == 0)
                        {
                            canTurnPage = false;
                        }
                    }
                }
                //back
                else
                {
                    if (!_isReverse)
                    {
                        if (_currPageIndex == 0)
                        {
                            canTurnPage = false;
                        }
                    }
                    else
                    {
                        if (_currPageIndex == _totalPages - 1)
                        {
                            canTurnPage = false;
                        }
                    }
                }
            }
            else
            {
                var midpoint = _targetSize.Height * 0.5;
                //upward
                if (_startPoint.Y > midpoint)
                {
                    if (!_isReverse)
                    {
                        if (_currPageIndex == _totalPages - 1)
                        {
                            canTurnPage = false;
                        }
                    }
                    else
                    {
                        if (_currPageIndex == 0)
                        {
                            canTurnPage = false;
                        }
                    }
                }
                //back
                else
                {
                    if (!_isReverse)
                    {
                        if (_currPageIndex == 0)
                        {
                            canTurnPage = false;
                        }
                    }
                    else
                    {
                        if (_currPageIndex == _totalPages - 1)
                        {
                            canTurnPage = false;
                        }
                    }
                }
            }

            return canTurnPage;
        }

        private bool DetectPageBounds()
        {
            // TODO:

            return true;
        }

    }
}
