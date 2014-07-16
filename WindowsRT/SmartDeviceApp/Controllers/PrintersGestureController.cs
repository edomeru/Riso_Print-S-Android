using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.Controllers
{
    public class PrintersGestureController
    {
        private const string PRINTER_NAME = "printerName";
        private const string IP_ADDRESS = "ipAddress";
        private const string PORT = "port";
        private const string DEFAULT_SWITCH = "defaultPrinterSwitch";
        private const string SETTINGS = "settings";

        private GestureRecognizer _gestureRecognizer;
        private UIElement _control;
        private UIElement _targetControl;
        private UIElement _controlReference;
        private Point _controlPosition;

        private bool _isDeleteJobButtonVisible;
        //private JobListItemControl _lastJobListItem;

        private Point _startPoint;
        private bool _isEnabled;

        /// <summary>
        /// PrintersGestureController class constructor
        /// </summary>
        public PrintersGestureController()
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
                GestureSettings.ManipulationMultipleFingerPanning | //reduces zoom jitter when panning with multiple fingers
                GestureSettings.ManipulationScaleInertia;
        }

        /// <summary>
        /// UI control
        /// </summary>
        public UIElement Control
        {
            set
            {
                _control = value;
                Initialize();
            }
        }

        /// <summary>
        /// Target UI control
        /// </summary>
        public UIElement TargetControl
        {
            get { return _targetControl; }
            set { _targetControl = value; }
        }

        /// <summary>
        /// UI control reference (scroll view)
        /// </summary>
        public UIElement ControlReference
        {
            set { _controlReference = value; }
        }

        private void Initialize()
        {
            EnableGestures();

            var transform = _control.TransformToVisual(null);
            _controlPosition = transform.TransformPoint(new Point());
        }

        /// <summary>
        /// Enables handling of gestures
        /// </summary>
        public void EnableGestures()
        {
            if (_control == null) return;
            _control.Visibility = Visibility.Collapsed;
            //if (!_isEnabled)
            //{
            //    _control.PointerCanceled += OnPointerCanceled;
            //    _control.PointerPressed += OnPointerPressed;
            //    _control.PointerReleased += OnPointerReleased;
            //    _control.PointerMoved += OnPointerMoved;

            //    // Gesture recognizer outputs
            //    _gestureRecognizer.Tapped += OnTapped;
            //    //_gestureRecognizer.RightTapped += OnRightTapped;
            //    _gestureRecognizer.ManipulationStarted += OnManipulationStarted;
            //    _gestureRecognizer.ManipulationUpdated += OnManipulationUpdated;
            //    _gestureRecognizer.ManipulationCompleted += OnManipulationCompleted;
            //    _isEnabled = true;
            //}
        }

        /// <summary>
        /// Disables handling of gestures
        /// </summary>
        public void DisableGestures()
        {
            if (_control == null) return;
            _control.Visibility = Visibility.Visible;
            //if (_isEnabled)
            //{
            //    _control.PointerCanceled -= OnPointerCanceled;
            //    _control.PointerPressed -= OnPointerPressed;
            //    _control.PointerReleased -= OnPointerReleased;
            //    _control.PointerMoved -= OnPointerMoved;

            //    // Gesture recognizer outputs
            //    _gestureRecognizer.Tapped -= OnTapped;
            //    //_gestureRecognizer.RightTapped -= OnRightTapped;
            //    _gestureRecognizer.ManipulationStarted -= OnManipulationStarted;
            //    _gestureRecognizer.ManipulationUpdated -= OnManipulationUpdated;
            //    _gestureRecognizer.ManipulationCompleted -= OnManipulationCompleted;
            //    _isEnabled = false;
            //}
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
            //var point = TransformPointToGlobalCoordinates(e.Position, false);
            //var elements = VisualTreeHelper.FindElementsInHostCoordinates(point, _targetControl);
            //bool isDelete = false;
            //Button deleteButton;
            //ToggleButton jobListHeader = null;
            //foreach (UIElement element in elements)
            //{
            //    var elementName = ((FrameworkElement)element).Name;
            //    if (elementName == JOB_LIST_HEADER_DELETE_CONTROL_NAME)
            //    {
            //        deleteButton = (Button)element;
            //        isDelete = true;
            //    }
            //    else if (elementName == JOB_LIST_HEADER_CONTROL_NAME)
            //    {
            //        jobListHeader = (ToggleButton)element;
            //        break;
            //    }
            //}
           
        }

        //void OnRightTapped(object sender, RightTappedEventArgs e)
        //{
        //}

        private void OnManipulationStarted(object sender, ManipulationStartedEventArgs e)
        {
            _startPoint = e.Position;
            //Debug.WriteLine("OnManipulationStarted");
        }

        private void OnManipulationUpdated(object sender, ManipulationUpdatedEventArgs e)
        {
            var isVerticalSwipe = DetectVerticalSwipe(e.Delta.Translation);
            if (isVerticalSwipe)
            {
                return;
            }
        }

     

        private bool DetectVerticalSwipe(Point delta)
        {
            var isTranslate = false;
            if (Math.Abs(delta.Y) > 0)
            {
                isTranslate = true;
                var scrollViewer = (ScrollViewer)_controlReference;
                scrollViewer.ChangeView(null, scrollViewer.VerticalOffset - delta.Y, null);
                
            }
            return isTranslate;
        }

        private void OnManipulationCompleted(object sender, ManipulationCompletedEventArgs e)
        {
            //Debug.WriteLine("OnManipulationCompleted");
        }
    }
}
