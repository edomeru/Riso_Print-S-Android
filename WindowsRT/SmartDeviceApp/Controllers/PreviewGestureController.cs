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
        private const int SWIPE_THRESHOLD = 100;
        private const int MAX_ZOOM_LEVEL_FACTOR = 4;

        private int SOME_W = 80000;
        private int SOME_H = 80000;

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
        private bool _isHorizontalSwipeEnabled;

        private Size _scaledSize;
        private Size _targetSize;
        private Size _controlSize;
        private Point _center;
        private double _originalScale;
        private bool _isScaled;
        private Point _startPoint;
        private double _currentZoomLength; // based on width
        private double _maxZoomLength; // based on width * max zoom level factor

        private bool _isEnabled;
        private bool _isDisposed;

        private bool _isTranslateXEnabled;
        private bool _isTranslateYEnabled;

        private TwoPageControl _twoPageControl;

        public PreviewGestureController(TwoPageControl twoPageControl, UIElement controlReference, Size targetSize,
            double originalScale, SwipeRightDelegate swipeRightHandler, SwipeLeftDelegate swipeLeftHandler)
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

            Initialize();
            _currentZoomLength = _targetSize.Width;
            _maxZoomLength = _targetSize.Width * MAX_ZOOM_LEVEL_FACTOR;

            ((ScrollViewer)_controlReference).SizeChanged += ControlReferenceSizeChanged;
        }

        public void InitializeSwipe(bool isHorizontalSwipeEnabled,
            SwipeLeftDelegate swipeLeftHandler,
            SwipeRightDelegate swipeRightHandler,
            SwipeTopDelegate swipeTopHandler,
            SwipeBottomDelegate swipeBottomHandler)
        {
            _isHorizontalSwipeEnabled = isHorizontalSwipeEnabled;
            _swipeLeftHandler = swipeLeftHandler;
            _swipeRightHandler = swipeRightHandler;
            _swipeTopHandler = swipeTopHandler;
            _swipeBottomHandler = swipeBottomHandler;
        }
        
        public delegate void SwipeRightDelegate();
        public delegate void SwipeLeftDelegate();
        public delegate void SwipeTopDelegate();
        public delegate void SwipeBottomDelegate();

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
            _controlSize = new Size(((ScrollViewer)_controlReference).ActualWidth, ((ScrollViewer)_controlReference).ActualHeight);
            _center = new Point(_controlSize.Width / 2, _controlSize.Height / 2);
            ResetTransforms();
        }

        public void EnableGestures()
        {
            if (!_isEnabled)
            {
                _control.PointerCanceled += OnPointerCanceled;
                _control.PointerPressed += OnPointerPressed;
                _control.PointerReleased += OnPointerReleased;
                _control.PointerMoved += OnPointerMoved;

                // Gesture recognizer outputs
                _gestureRecognizer.Tapped += OnTapped;
                _gestureRecognizer.RightTapped += OnRightTapped;
                _gestureRecognizer.ManipulationStarted += OnManipulationStarted;
                _gestureRecognizer.ManipulationUpdated += OnManipulationUpdated;
                _gestureRecognizer.ManipulationCompleted += OnManipulationCompleted;
                _isEnabled = true;
            }
        }

        public void DisableGestures()
        {
            if (_isEnabled)
            {
                _control.PointerCanceled -= OnPointerCanceled;
                _control.PointerPressed -= OnPointerPressed;
                _control.PointerReleased -= OnPointerReleased;
                _control.PointerMoved -= OnPointerMoved;

                // Gesture recognizer outputs
                _gestureRecognizer.Tapped -= OnTapped;
                _gestureRecognizer.RightTapped -= OnRightTapped;
                _gestureRecognizer.ManipulationStarted -= OnManipulationStarted;
                _gestureRecognizer.ManipulationUpdated -= OnManipulationUpdated;
                _gestureRecognizer.ManipulationCompleted -= OnManipulationCompleted;
                _isEnabled = false;
            }
        }

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

            _pageAreaGrid.RenderTransform = _cumulativeTransform;
            _transitionGrid.RenderTransform = _cumulativeTransform;
            _displayAreaGrid.RenderTransform = _cumulativeTransform;

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

            _scaledSize = new Size(_targetSize.Width * _deltaTransform.ScaleX, _targetSize.Height * _deltaTransform.ScaleY);
        }
        private uint pointerCount = 0;
        private bool _multipleFingersDetected = false;
        void OnPointerPressed(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
            _gestureRecognizer.ProcessDownEvent(args.GetCurrentPoint(_controlReference));
            pointerCount++;

            if (pointerCount > 1)
                _multipleFingersDetected = true;
            else
                _multipleFingersDetected = false;
            _control.CapturePointer(args.Pointer);
            args.Handled = true;
            
        }
        void OnPointerReleased(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
            pointerCount--;

            _gestureRecognizer.ProcessUpEvent(args.GetCurrentPoint(_controlReference));
            args.Handled = true;
        }
        void OnPointerCanceled(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
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
            //_twoPageControl.PageAreaGrid.Opacity = 1;
            _pageAreaGrid.Opacity = 1;
            _startPoint = e.Position;
            if (!_multipleFingersDetected)
                ManipulationGrid_ManipulationStarted(e);
        }

        private void OnManipulationUpdated(object sender, ManipulationUpdatedEventArgs e)
        {
            //var isSwipe = DetectSwipe(e);
            //if (isSwipe) return;
            //if (e.)
            var isScale = DetectScale(e);

            if (DetectTranslate(e, isScale)) return;

            if (!_multipleFingersDetected)
                ManipulationGrid_ManipulationDelta(e);
        }

        private bool DetectSwipe(ManipulationUpdatedEventArgs e)
        {
            var isSwipe = false;
            if (!_isScaled && _gestureRecognizer.IsInertial)
            {                
                Point currentPosition = e.Position;
                // Horizontal Swipe
                if (_isHorizontalSwipeEnabled) 
                {
                    // Swipe right;
                    if (currentPosition.X - _startPoint.X >= SWIPE_THRESHOLD)
                    {
                        _gestureRecognizer.CompleteGesture();
                        _swipeRightHandler();
                        isSwipe = true;
                    }
                    // Swipe left
                    else if (_startPoint.X - currentPosition.X >= SWIPE_THRESHOLD)
                    {
                        _gestureRecognizer.CompleteGesture();
                        _swipeLeftHandler();
                        isSwipe = true;
                    }
                }
                // Vertical swipe
                else 
                {
                    // Swipe bottom
                    if (currentPosition.Y - _startPoint.Y >= SWIPE_THRESHOLD)
                    {
                        _gestureRecognizer.CompleteGesture();
                        _swipeBottomHandler();
                        isSwipe = true;
                    }
                    // Swipe top
                    else if (_startPoint.Y - currentPosition.Y >= SWIPE_THRESHOLD)
                    {
                        _gestureRecognizer.CompleteGesture();
                        _swipeTopHandler();
                        isSwipe = true;
                    }
                }
            }
            return isSwipe;
        }

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


        private Point _manipulationStartPosition;
        private double _rotationCenterX;
        private double _rotationCenterY;
        private bool _backCurl;
        private bool _willContinue;

        private void ManipulationGrid_ManipulationStarted(ManipulationStartedEventArgs e)
        {

            //if (_twoPageControl.Image.Source == null)
            //{
            //    //CancelManipulation(e);
            //    return;
            //}
            
            //
            
            //getScreenShot();
            _manipulationStartPosition = e.Position;
            
            if (_isHorizontalSwipeEnabled)
            {
                _backCurl = false;
                //var startOfBackCurlPosition = 0.0;
                //var startOfBackCurlPosition = _scaledSize.Width * 0.5;
                //if (_twoPageControl.PageAreaGrid.ActualWidth > _rightPageWidth)
                //{
                //    IsDuplex = true;
                //    startOfBackCurlPosition = _manipulationGrid.ActualWidth * 0.5;
                //}
                //else
                //{
                //    IsDuplex = false;
                //    startOfBackCurlPosition = _manipulationGrid.ActualWidth * 0.25;
                //}

                //if (manipulationStartPosition.X < startOfBackCurlPosition)
                //{
                //    //use page1 clip transition
                //    _backCurl = true;

                //}

                //flipDirection = FlipDirections.Left;
                //_twoPageControl.Page2TranslateTransform.X = _manipulationGrid.ActualWidth; //get Page2ClipTranslateTransform and put in XAML.
                _twoPageControl.Page2TranslateTransform.X = _targetSize.Width;
                //Page2.Opacity = 1;
                //_twoPageControl.PageAreaGrid.Opacity = 1;
                _twoPageControl.TransitionTranslateTransform.X = -SOME_W;
                //_twoPageControl.TransitionContainerTransform.TranslateX = _manipulationGrid.ActualWidth;
                _twoPageControl.TransitionContainerTransform.TranslateX = _targetSize.Width;
                _transitionGrid.Opacity = 1;
            }
            else
            {
                _backCurl = false;
                //var startOfBackCurlPosition = 0.0;
                var startOfBackCurlPosition = _scaledSize.Height * 0.5;
                //if (_twoPageControl.PageAreaGrid.ActualHeight > _targetSize.Height)
                //{
                //    IsDuplex = true;
                //    startOfBackCurlPosition = _manipulationGrid.ActualHeight * 0.5;
                //}
                //else
                //{
                //    IsDuplex = false;
                //    startOfBackCurlPosition = _manipulationGrid.ActualHeight * 0.25;
                //}

                //if (_manipulationStartPosition.Y < startOfBackCurlPosition)
                //{
                //    //use page1 clip transition
                //    _backCurl = true;

                //}

                _twoPageControl.Page2TranslateTransform.Y = _scaledSize.Height;
                _twoPageControl.TransitionTranslateTransform.Y = -SOME_H;
                _twoPageControl.TransitionContainerTransform.TranslateY = _scaledSize.Height;
                _transitionGrid.Opacity = .975;
            }
            
        }

        private void ManipulationGrid_ManipulationDelta(ManipulationUpdatedEventArgs e)
        {
            var w = _targetSize.Width;
            var h = _targetSize.Height;

            if (_isHorizontalSwipeEnabled)
            {
                //var tempW = 0.0;
                //var tempW = -_scaledSize.Width * 2; // TODO: Add logic for two page view here

                //if (_twoPageControl.PageAreaGrid.ActualWidth > _rightPageWidth)
                //{
                //    tempW = -w;
                //}
                //else
                //{
                //    tempW = -w * 2;
                //}

                var cx = Math.Min(0, Math.Max(e.Position.X - w, -w));
                var cy = e.Cumulative.Translation.Y;
                var angle = (Math.Atan2(cx + _manipulationStartPosition.Y - w, -cy) * 180 / Math.PI + 90) % 360;

                _rotationCenterX = w + cx / 2;

                if (cy < 0)
                {
                    _rotationCenterY = h;
                }
                else
                {
                    _rotationCenterY = 0;
                }

                _twoPageControl.Page2TranslateTransform.X = w + cx / 2;
                _twoPageControl.Page2TranslateTransform.Y = -(SOME_H / 2) + h / 2;
                _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                _twoPageControl.Page2RotateTransform.Angle = angle;

                _twoPageControl.TransitionTranslateTransform.X = -SOME_W - (cx / 2);
                _twoPageControl.TransitionTranslateTransform.Y = -(SOME_H / 2) + h / 2;
                _twoPageControl.TransitionRotateTransform.CenterX = -cx / 2;
                _twoPageControl.TransitionRotateTransform.CenterY = _rotationCenterY;
                _twoPageControl.TransitionRotateTransform.Angle = angle;

                _twoPageControl.TransitionContainerTransform.TranslateX = w + cx;
                _twoPageControl.TransitionContainerTransform.CenterX = -cx / 2;
                _twoPageControl.TransitionContainerTransform.CenterY = _rotationCenterY;
                _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
            }
            else
            {
                //var tempH = 0.0;
                //var tempH = -_targetSize.Height; // TODO: Add logic for two page view here
                //if (_twoPageControl.PageAreaGrid.ActualHeight > _targetSize.Height)
                //{
                //    tempH = -h;
                //}
                //else
                //{
                //    tempH = -h * 2;
                //}

                var cy = Math.Min(0, Math.Max(e.Position.Y - h, -h));
                var cx = e.Cumulative.Translation.X;
                var angle = (Math.Atan2(cx - _manipulationStartPosition.X - h, cx) * 180 / Math.PI + 90) % 360;

                _rotationCenterY = h + cy / 2;

                if (cx < 0)
                {
                    _rotationCenterX = w;
                }
                else
                {
                    _rotationCenterX = 0;
                }

                _twoPageControl.Page2TranslateTransform.Y = h + cy / 2;
                _twoPageControl.Page2TranslateTransform.X = -(SOME_W / 2) + w / 2;
                _twoPageControl.Page2RotateTransform.CenterX = _rotationCenterX;
                _twoPageControl.Page2RotateTransform.CenterY = _rotationCenterY;
                _twoPageControl.Page2RotateTransform.Angle = angle;

                _twoPageControl.TransitionTranslateTransform.Y = -SOME_H - (cy / 2);
                _twoPageControl.TransitionTranslateTransform.X = -(SOME_W / 2) + w / 2;
                _twoPageControl.TransitionRotateTransform.CenterY = -cy / 2;
                _twoPageControl.TransitionRotateTransform.CenterX = _rotationCenterX;
                _twoPageControl.TransitionRotateTransform.Angle = -angle;

                _twoPageControl.TransitionContainerTransform.TranslateY = h + cy;
                _twoPageControl.TransitionContainerTransform.CenterY = -cy / 2;
                _twoPageControl.TransitionContainerTransform.CenterX = _rotationCenterX;
                _twoPageControl.TransitionContainerTransform.Rotation = 2 * angle;
            }
        }

        private void ManipulationGrid_ManipulationCompleted(ManipulationCompletedEventArgs e)
        {
            var w = _targetSize.Width;
            var h = _targetSize.Height;

            _willContinue = false;
            if (_backCurl)
            {
                if (e.Position.X > w * 0.25)
                {
                    _willContinue = true;
                }
            }
            else
            {
                if (e.Position.X < w * 0.75)
                {
                    _willContinue = true;
                }
            }

            if (_isHorizontalSwipeEnabled)
            {
                var sb = new Storyboard();
                sb.Completed += StoryBoardAnimationCompleted;
                if (!_backCurl)
                {
                    var to = 0;
                    if (_willContinue)
                    {
                        //if (IsDuplex)
                        //    to = (int)(0);
                        //else
                            to = (int)-w;
                        //System.Diagnostics.Debug.WriteLine("Will continue");
                    }
                    else
                    {
                        to = (int)w;
                    }
                    AddAnimation(sb, _twoPageControl.Page2TranslateTransform, "X", to);
                    AddAnimation(sb, _twoPageControl.Page2RotateTransform, "CenterX", 0);
                    AddAnimation(sb, _twoPageControl.Page2RotateTransform, "Angle", 0);

                    if (_willContinue)
                    {
                        //if (IsDuplex)
                        //    to = (int)(-SOME_VALUE + (w));
                        //else
                            to = (int)(-SOME_W + (w));
                    }
                    else
                    {
                        to = (int)-SOME_W;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, "X", to);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "CenterX", 0);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "Angle", 0);
                    if (_willContinue)
                    {
                        //if (IsDuplex)
                        //    to = 0;
                        //else
                            to = (int)-w;
                    }
                    else
                    {
                        to = (int)w;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "TranslateX", to);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "CenterX", 0);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "Rotation", 0);
                    sb.Begin();
                    //if (!IsDuplex)
                    _transitionGrid.Opacity = 0;

                    if (_willContinue)
                    {
                        _swipeLeftHandler();
                    }
                }
                else
                {
                    AddAnimation(sb, _twoPageControl.Page1TranslateTransform, "X", 0);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, "CenterX", w / 2);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, "Angle", 0);
                    //Page1.Opacity = 1;
                    //Page2.Opacity = 0;
                    var to = 0;
                    if (_willContinue)
                    {
                        to = (int)(-SOME_W);
                    }
                    else
                    {
                        to = (int)0;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, "X", to);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "CenterX", w / 2);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "Angle", 0);

                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "TranslateX", to);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "CenterX", w / 2);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "Rotation", 0);
                    sb.Begin();
                }
            }
            else
            {
                var sb = new Storyboard();
                sb.Completed += StoryBoardAnimationCompleted;
                if (!_backCurl)
                {
                    var to = 0;
                    if (_willContinue)
                    {
                        //if (IsDuplex)
                        //    to = (int)(0);
                        //else
                            to = (int)-h;
                        //System.Diagnostics.Debug.WriteLine("Will continue");
                    }
                    else
                    {
                        to = (int)h;
                    }
                    AddAnimation(sb, _twoPageControl.Page2TranslateTransform, "Y", to);
                    AddAnimation(sb, _twoPageControl.Page2RotateTransform, "CenterY", 0);
                    AddAnimation(sb, _twoPageControl.Page2RotateTransform, "Angle", 0);

                    if (_willContinue)
                    {
                        //if (IsDuplex)
                        //    to = (int)(-SOME_VALUE + (h));
                        //else
                            to = (int)(-SOME_H + (h));
                    }
                    else
                    {
                        to = (int)-SOME_H;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, "Y", to);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "CenterY", 0);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "Angle", 0);
                    if (_willContinue)
                    {
                        //if (IsDuplex)
                        //    to = 0;
                        //else
                            to = (int)-h;
                    }
                    else
                    {
                        to = (int)h;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "TranslateY", to);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "CenterY", 0);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "Rotation", 0);
                    sb.Begin();
                    //if (!IsDuplex)
                    _transitionGrid.Opacity = 0;

                    if (_willContinue)
                    {
                        _swipeTopHandler();
                    }

                }
                else
                {
                    AddAnimation(sb, _twoPageControl.Page1TranslateTransform, "Y", 0);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, "CenterY", h / 2);
                    AddAnimation(sb, _twoPageControl.Page1RotateTransform, "Angle", 0);
                    //Page1.Opacity = 1;
                    //Page2.Opacity = 0;
                    var to = 0;
                    if (_willContinue)
                    {
                        to = (int)(-SOME_H);
                    }
                    else
                    {
                        to = (int)0;
                    }
                    AddAnimation(sb, _twoPageControl.TransitionTranslateTransform, "Y", to);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "CenterY", h / 2);
                    AddAnimation(sb, _twoPageControl.TransitionRotateTransform, "Angle", 0);

                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "TranslateY", to);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "CenterY", h / 2);
                    AddAnimation(sb, _twoPageControl.TransitionContainerTransform, "Rotation", 0);
                    sb.Begin();
                }
            }
            
        }

        private void StoryBoardAnimationCompleted(object sender, object e)
        {
            // Reset
            _twoPageControl.Page2TranslateTransform.X = 0;
            _twoPageControl.PageAreaGrid.Opacity = 0;
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

    }
}
