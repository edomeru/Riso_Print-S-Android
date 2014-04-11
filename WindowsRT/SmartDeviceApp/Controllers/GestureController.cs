using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;

namespace SmartDeviceApp.Controllers
{
    public class GestureController
    {
        private const int SWIPE_THRESHOLD = 500;

        private GestureRecognizer _gestureRecognizer;
        private UIElement _control;
        private UIElement _controlReference;
        private TransformGroup _cumulativeTransform;
        private MatrixTransform _previousTransform;
        private CompositeTransform _deltaTransform;
        private TransformGroup _tempCumulativeTransform;
        private MatrixTransform _tempPreviousTransform;
        private CompositeTransform _tempDeltaTransform;

        private SwipeRightDelegate _swipeRightHandler;
        private SwipeLeftDelegate _swipeLeftHandler;

        private Size _targetSize;
        private Size _controlSize;
        private Point _center;
        private double _originalScale;
        private bool _isScaled;
        private Point _startPoint;

        private bool _isEnabled;

        private bool _isTranslateXEnabled;
        private bool _isTranslateYEnabled;

        public GestureController(UIElement control, UIElement controlReference, Size targetSize,
            double originalScale, SwipeRightDelegate swipeRightHandler, SwipeLeftDelegate swipeLeftHandler)
        {
            _control = control;
            _controlReference = controlReference;
            _targetSize = targetSize;
            _originalScale = originalScale;
            _swipeRightHandler = swipeRightHandler;
            _swipeLeftHandler = swipeLeftHandler;
            Initialize();

            ((ScrollViewer)_controlReference).SizeChanged += ControlReferenceSizeChanged;
        }
        
        public delegate void SwipeRightDelegate();
        public delegate void SwipeLeftDelegate();

        private void Initialize()
        {
            _gestureRecognizer = new GestureRecognizer();
            _gestureRecognizer.GestureSettings =
                GestureSettings.Tap |
                GestureSettings.Hold | //hold must be set in order to recognize the press & hold gesture
                GestureSettings.RightTap |
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

        private void ControlReferenceSizeChanged(Object sender, SizeChangedEventArgs e)
        {
            _controlSize = new Size(((ScrollViewer)_controlReference).ActualWidth, ((ScrollViewer)_controlReference).ActualHeight);
            _center = new Point(_controlSize.Width / 2, _controlSize.Height / 2);
            Normalize();
        }

        public void ResetTransforms() // TODO: Set to private after debug!!
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

            Normalize();
            // Reset scale
            _isScaled = false;
        }

        void OnPointerPressed(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
            _gestureRecognizer.ProcessDownEvent(args.GetCurrentPoint(_controlReference));
            _control.CapturePointer(args.Pointer);
            args.Handled = true;
        }
        void OnPointerReleased(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs args)
        {
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
            _gestureRecognizer.ProcessMoveEvents(args.GetIntermediatePoints(_control));
            args.Handled = true;
        }

        void OnTapped(object sender, TappedEventArgs e)
        {
        }

        void OnRightTapped(object sender, RightTappedEventArgs e)
        {
        }

        private void OnManipulationStarted(object sender, ManipulationStartedEventArgs e)
        {
            _startPoint = e.Position;
        }

        private void OnManipulationUpdated(object sender, ManipulationUpdatedEventArgs e)
        {
            var isSwipe = DetectSwipe(e);
            if (isSwipe) return;
            var isScale = DetectScale(e);
            DetectTranslate(e, isScale);
        }

        private bool DetectSwipe(ManipulationUpdatedEventArgs e)
        {
            var isSwipe = false;
            if (!_isScaled && _gestureRecognizer.IsInertial)
            {
                // Swipe right;
                Point currentPosition = e.Position;
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
            return isSwipe;
        }

        private bool DetectScale(ManipulationUpdatedEventArgs e)
        {
            var isScale = false; // Currently scaling
            if (e.Delta.Scale == 1)
            {
                return isScale;
            }
            
            _tempPreviousTransform.Matrix = _tempCumulativeTransform.Value;

            // Get scale center
            Point center = new Point(e.Position.X, e.Position.Y);
            _tempDeltaTransform.CenterX = center.X;
            _tempDeltaTransform.CenterY = center.Y;

            // Apply scaling on temp transforms first
            _tempDeltaTransform.ScaleX = _tempDeltaTransform.ScaleY = e.Delta.Scale;
            
            // Check if scale is valid, do not scale less than original size
            if (_tempCumulativeTransform.Value.M11 > 1)
            {
                // Check if scaling, current scale is changed
                if (Math.Abs(_cumulativeTransform.Value.M11 - _tempCumulativeTransform.Value.M11) > 0)
                {
                    _previousTransform.Matrix = _cumulativeTransform.Value;
                    _deltaTransform.CenterX = center.X;
                    _deltaTransform.CenterY = center.Y;
                    _deltaTransform.ScaleX = _deltaTransform.ScaleY = e.Delta.Scale;
                    _deltaTransform.TranslateX = _deltaTransform.TranslateY = 0;

                    _isScaled = true; // not original size
                    isScale = true;
                    _isTranslateXEnabled = true;
                    _isTranslateYEnabled = true;
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
            }
            return isTranslate;
        }

        private void OnManipulationCompleted(object sender, ManipulationCompletedEventArgs e)
        {
            Normalize();
        }
    }
}
