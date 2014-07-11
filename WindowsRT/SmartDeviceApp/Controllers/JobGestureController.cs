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

        private Point _startPoint;
        private bool _isEnabled;

        public JobGestureController()
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
            EnableGestures();

            var transform = _control.TransformToVisual(null);
            _controlPosition = transform.TransformPoint(new Point());
        }

        public void EnableGestures()
        {
            if (_control == null) return;
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
            if (_control == null) return;
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
                HideDeleteJobButton();
                // Check if tap is for jobListControl which has the visible delete button
                var jobListItem = GetJobListItemControl(e.Position, false);
                if (jobListItem != null && jobListItem == _lastJobListItem)
                {
                    var printJob = jobListItem.DataContext;
                    (new ViewModelLocator().JobsViewModel).DeleteJobCommand.Execute(printJob);
                }
                return;
            }

            var point = TransformPointToGlobalCoordinates(e.Position, false);
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
                HideDeleteJobButton();
                var printerId = ((PrintJobGroup)jobListHeader.DataContext).Jobs[0].PrinterId;
                (new ViewModelLocator().JobsViewModel).DeleteAllJobsCommand.Execute(printerId);
            }
            else if (!isDelete && jobListHeader != null)
            {
                HideDeleteJobButton();
                if ((bool)jobListHeader.IsChecked) // Manually set pressed states
                {
                    VisualStateManager.GoToState(jobListHeader, "CheckedPressed", true);
                }
                else
                {
                    VisualStateManager.GoToState(jobListHeader, "Pressed", true);
                }
                jobListHeader.IsChecked = !jobListHeader.IsChecked; // Manually toggle the button
                ((PrintJobGroup)jobListHeader.DataContext).IsCollapsed = jobListHeader.IsChecked.Value;
            }
            else if (jobListHeader != null)
            {
                VisualStateManager.GoToState(jobListHeader, "Normal", true);
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
            var isVerticalSwipe = DetectVerticalSwipe(e.Delta.Translation);
            if (isVerticalSwipe)
            {
                return;
            }
            var isHorizontalSwipe = DetectHorizontalSwipe(e.Position, true);
        }

        private bool DetectHorizontalSwipe(Point currentPosition, bool isScrolled)
        {
            var isSwipe = false;
            // Swipe right, hide delete button
            if (currentPosition.X - _startPoint.X >= SWIPE_THRESHOLD)
            {
                isSwipe = true;
                _gestureRecognizer.CompleteGesture();
                var jobListItem = GetJobListItemControl(currentPosition, isScrolled);
                if (jobListItem == null) return isSwipe;
                if (_isDeleteJobButtonVisible && _lastJobListItem != null)
                {
                    HideDeleteJobButton();
                    return isSwipe;
                }
            }
            // Swipe left, show delete button
            else if (_startPoint.X - currentPosition.X >= SWIPE_THRESHOLD)
            {
                isSwipe = true;
                _gestureRecognizer.CompleteGesture();
                var jobListItem = GetJobListItemControl(currentPosition, isScrolled);
                if (jobListItem == null) return isSwipe;
                if (_isDeleteJobButtonVisible && _lastJobListItem != null && _lastJobListItem != jobListItem)
                {
                    HideDeleteJobButton();
                    return isSwipe;
                }
                if (jobListItem.DeleteButtonVisibility != Visibility.Visible)
                {
                    jobListItem.DeleteButtonVisibility = Visibility.Visible;
                    jobListItem.VisualState = "Pressed";
                    _lastJobListItem = jobListItem;
                    _isDeleteJobButtonVisible = true;
                }
            }
            return isSwipe;
        }

        private void HideDeleteJobButton()
        {
            if (_lastJobListItem == null || !_isDeleteJobButtonVisible) return;
            _lastJobListItem.DeleteButtonVisibility = Visibility.Collapsed;
            _lastJobListItem.VisualState = "Normal";
            _isDeleteJobButtonVisible = false;
        }

        /// <param name="isScrolled">True if from ManipulationUpdatedEventArgs,
        /// False if from TappedEventArgs</param>
        private JobListItemControl GetJobListItemControl(Point position, bool isScrolled)
        {
            // Check if sender is jobListItemControl
            // If true, show delete button
            // Otherwise, ignore
            var point = TransformPointToGlobalCoordinates(position, isScrolled);
            var elements = VisualTreeHelper.FindElementsInHostCoordinates(point, _targetControl);                
            foreach (UIElement element in elements)
            {
                var elementName = ((FrameworkElement)element).Name;
                if (elementName == JOB_LIST_ITEM_CONTROL_NAME)
                {
                    return (JobListItemControl)element;
                }
            }
            return null;
        }

        private bool DetectVerticalSwipe(Point delta)
        {
            var isTranslate = false;
            if (Math.Abs(delta.Y) > 0)
            {
                isTranslate = true;
                var scrollViewer = (ScrollViewer)_controlReference;
                scrollViewer.ChangeView(null, scrollViewer.VerticalOffset - delta.Y, null);                
                HideDeleteJobButton();
            }
            return isTranslate;
        }

        private void OnManipulationCompleted(object sender, ManipulationCompletedEventArgs e)
        {
            //Debug.WriteLine("OnManipulationCompleted");
        }

        private Point TransformPointToGlobalCoordinates(Point point, bool isScrolled)
        {
            var transformedPoint = new Point();
            var scrollViewer = (ScrollViewer)_controlReference;
            transformedPoint.X = point.X + _controlPosition.X;
            transformedPoint.Y = point.Y + _controlPosition.Y;
            if (isScrolled)
            {
                transformedPoint.X -= scrollViewer.HorizontalOffset;
                transformedPoint.Y -= scrollViewer.VerticalOffset;
            }
            return transformedPoint;
        }
    }
}
