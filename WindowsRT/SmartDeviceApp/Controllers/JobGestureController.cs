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
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Media;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Controllers
{

    public class JobGestureController
    {
        private const int SWIPE_THRESHOLD = 25;
        private const string JOB_LIST_HEADER_CONTROL_NAME = "header";
        private const string JOB_LIST_HEADER_DELETE_CONTROL_NAME = "deleteButton";
        private const string JOB_LIST_ITEM_CONTROL_NAME = "jobListItemControl";
        private const string JOBS_GRID_CONTROL_NAME = "jobsGrid";

        private GestureRecognizer _gestureRecognizer;
        private UIElement _control;
        private UIElement _targetControl;
        private UIElement _controlReference;
        private Point _controlPosition;

        private bool _isDeleteJobButtonVisible;
        private JobListItemControl _lastJobListItem;

        private TransformGroup _cumulativeTransform;
        private MatrixTransform _previousTransform;
        private CompositeTransform _deltaTransform;
        private TransformGroup _tempCumulativeTransform;
        private MatrixTransform _tempPreviousTransform;
        private CompositeTransform _tempDeltaTransform;

        private Point _startPoint;
        private bool _isEnabled;
        private bool _isTranslateXEnabled;
        private bool _isTranslateYEnabled;

        public UIElement Control
        {
            set 
            {
                _control = value;
                Initialize();
            }
        }

        public UIElement TargetControl
        {
            set { _targetControl = value; }
        }

        public UIElement ControlReference
        {
            set { _controlReference = value; }
        }
        
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
                GestureSettings.ManipulationMultipleFingerPanning | //reduces zoom jitter when panning with multiple fingers
                GestureSettings.ManipulationScaleInertia;

            EnableGestures();

            var transform = _control.TransformToVisual(null);
            _controlPosition = transform.TransformPoint(new Point());
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
                //_gestureRecognizer.RightTapped += OnRightTapped;
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
                //_gestureRecognizer.RightTapped -= OnRightTapped;
                _gestureRecognizer.ManipulationStarted -= OnManipulationStarted;
                _gestureRecognizer.ManipulationUpdated -= OnManipulationUpdated;
                _gestureRecognizer.ManipulationCompleted -= OnManipulationCompleted;
                _isEnabled = false;
            }
        }
        
        //public void ResetTransforms() // TODO: Set to private after debug!!
        //{
        //    _cumulativeTransform = new TransformGroup();
        //    _deltaTransform = new CompositeTransform();
        //    _previousTransform = new MatrixTransform() { Matrix = Matrix.Identity };
        //    _cumulativeTransform.Children.Add(_previousTransform);
        //    _cumulativeTransform.Children.Add(_deltaTransform);
        //    _control.RenderTransform = _cumulativeTransform;

        //    // Temp transforms for checking validity of transforms before applying
        //    _tempCumulativeTransform = new TransformGroup();
        //    _tempDeltaTransform = new CompositeTransform();
        //    _tempPreviousTransform = new MatrixTransform() { Matrix = Matrix.Identity };
        //    _tempCumulativeTransform.Children.Add(_tempPreviousTransform);
        //    _tempCumulativeTransform.Children.Add(_tempDeltaTransform);
        //}

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
            if (_isDeleteJobButtonVisible)
            {
                // Check if tap is for jobListControl which has the visible delete button
                var jobListItem = GetJobListItemControl(e.Position);
                if (jobListItem != null && jobListItem == _lastJobListItem)
                {
                    var printJob = jobListItem.DataContext;
                    (new ViewModelLocator().JobsViewModel).DeleteJobCommand.Execute(printJob);
                    HideDeleteJobButton();
                }
                else
                {
                    HideDeleteJobButton();
                }
                return;
            }

            var point = TransformPointToGlobalCoordinates(e.Position);
            var elements = VisualTreeHelper.FindElementsInHostCoordinates(point, _targetControl);
            bool isDelete = false;
            Button deleteButton;
            ToggleButton jobListHeader = null;
            foreach (UIElement element in elements)
            {
                var elementName = ((FrameworkElement)element).Name;
                if (elementName == JOB_LIST_HEADER_DELETE_CONTROL_NAME)
                {
                    deleteButton = (Button)element;
                    isDelete = true;
                }
                else if (elementName == JOB_LIST_HEADER_CONTROL_NAME)
                {
                    jobListHeader = (ToggleButton)element;
                    break;
                }
            }
            // Manually execute delete command
            if (isDelete && jobListHeader != null)
            {
                var printerId = ((PrintJobGroup)jobListHeader.DataContext).Jobs[0].PrinterId;
                (new ViewModelLocator().JobsViewModel).DeleteAllJobsCommand.Execute(printerId);
            }
            else if (!isDelete && jobListHeader != null)
            {
                jobListHeader.IsChecked = !jobListHeader.IsChecked; // Manually toggle the button
            }
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
            if (_isDeleteJobButtonVisible) 
            {
                HideDeleteJobButton();
                return;
            }

            var isVerticalSwipe = DetectVerticalSwipe(e);
            if (isVerticalSwipe)
            {
                Debug.WriteLine("Vertical swipe detected");
                return;
            }
            var isHorizontalSwipe = DetectHorizontalSwipe(e);
            if (isHorizontalSwipe)
            {
                
            }
        }

        private bool DetectVerticalSwipe(ManipulationUpdatedEventArgs e)
        {
            var isSwipe = false;
            //if (_gestureRecognizer.IsInertial)
            {
                // Swipe right;
                Point currentPosition = e.Position;
                if (currentPosition.Y - _startPoint.Y >= SWIPE_THRESHOLD)
                {
                    _gestureRecognizer.CompleteGesture();
                    // TODO: Handle swipe
                    isSwipe = true;
                }
                // Swipe left
                else if (_startPoint.Y - currentPosition.Y >= SWIPE_THRESHOLD)
                {
                    _gestureRecognizer.CompleteGesture();
                    // TODO: Handle swipe
                    isSwipe = true;
                }
            }
            //Debug.WriteLine("DetectVerticalSwipe = {0}", isSwipe);
            return isSwipe;
        }

        private bool DetectHorizontalSwipe(ManipulationUpdatedEventArgs e)
        {
            var isSwipe = false;
            Point currentPosition = e.Position;
            // Swipe right
            if (currentPosition.X - _startPoint.X >= SWIPE_THRESHOLD)
            {
                _gestureRecognizer.CompleteGesture();
                var jobListItem = GetJobListItemControl(e.Position);
                if (jobListItem == null) return isSwipe;
                jobListItem.DeleteButtonVisibility = Visibility.Collapsed;
                _lastJobListItem = jobListItem;
                isSwipe = true;
            }
            // Swipe left
            else if (_startPoint.X - currentPosition.X >= SWIPE_THRESHOLD)
            {
                _gestureRecognizer.CompleteGesture();
                var jobListItem = GetJobListItemControl(e.Position);
                if (jobListItem == null) return isSwipe;
                jobListItem.DeleteButtonVisibility = Visibility.Visible;
                _lastJobListItem = jobListItem;
                _isDeleteJobButtonVisible = true;
                isSwipe = true;
            }
            //Debug.WriteLine("DetectHorizontalSwipe = {0}", isSwipe);
            return isSwipe;
        }

        private void HideDeleteJobButton()
        {
            _lastJobListItem.DeleteButtonVisibility = Visibility.Collapsed;
            _isDeleteJobButtonVisible = false;
        }

        private JobListItemControl GetJobListItemControl(Point position)
        {
            // Check if sender is jobListItemControl
            // If true, show delete button
            // Otherwise, ignore
            var point = TransformPointToGlobalCoordinates(position);
            var elements = VisualTreeHelper.FindElementsInHostCoordinates(point, _targetControl);                
            foreach (UIElement element in elements)
            {
                var elementName = ((FrameworkElement)element).Name;
                //if (elementName == JOB_LIST_HEADER_CONTROL_NAME || elementName == JOB_LIST_ITEM_CONTROL_NAME || elementName == JOBS_GRID_CONTROL_NAME)
                if (elementName == JOB_LIST_ITEM_CONTROL_NAME)
                {
                    return (JobListItemControl)element;
                }
            }
            return null;
        }

        private bool DetectTranslate(ManipulationUpdatedEventArgs e)
        {
            var isTranslate = false;
            if (_isTranslateXEnabled || _isTranslateYEnabled)
            {
                _previousTransform.Matrix = _cumulativeTransform.Value;
                if (_isTranslateXEnabled) _deltaTransform.TranslateX = e.Delta.Translation.X;
                if (_isTranslateYEnabled) _deltaTransform.TranslateY = e.Delta.Translation.Y;
            }
            Debug.WriteLine("Translate = {0}", isTranslate);
            return isTranslate;
        }

        private void OnManipulationCompleted(object sender, ManipulationCompletedEventArgs e)
        {
            //Debug.WriteLine("OnManipulationCompleted");
        }

        private Point TransformPointToGlobalCoordinates(Point point)
        {
            var transformedPoint = new Point();
            transformedPoint.X = point.X + _controlPosition.X;
            transformedPoint.Y = point.Y + _controlPosition.Y;
            return transformedPoint;
        }
    }
}
