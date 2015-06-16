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
        private const string JOB_LIST_ITEM_DELETE_CONTROL_NAME = "deleteButton";
        private const string JOBS_GRID_CONTROL_NAME = "jobsGrid";

        private GestureRecognizer _gestureRecognizer;
        private UIElement _control;
        private UIElement _targetControl;
        private UIElement _controlReference;
        private Point _controlPosition;

        private bool _isDeleteJobButtonVisible;
        private JobListItemControl _lastJobListItem;
        private PrintJob _lastPrintJob;
        private Button _lastDeleteAllButton;
        private PrintJobGroup _lastPrintJobGroup;

        private Point _startPoint;
        private bool _isEnabled;

        /// <summary>
        /// JobGestureController class constructor
        /// </summary>
        public JobGestureController()
        {
            _gestureRecognizer = new GestureRecognizer();

        }

        /// <summary>
        /// UI control where gestures are handled
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
        /// Enable handling of gestures
        /// </summary>
        public void EnableGestures()
        {
            if (_control == null) return;
            if (!_isEnabled)
            {
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

        /// <summary>
        /// Disable handling of gestures
        /// </summary>
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
            bool isDeleteJob;
            var jobListItem = GetJobListItemControlForDelete(e.Position, false);
            if (jobListItem != null)
            {
                PrintJob printJob = (PrintJob)jobListItem.DataContext;
                (new ViewModelLocator().JobsViewModel).DeleteJobCommand.Execute(printJob);
                return;
            }
            if (_isDeleteJobButtonVisible)
            {
                HideDeleteJobButton();
                return;
            }

            var point = TransformPointToGlobalCoordinates(e.Position, false);
            var elements = VisualTreeHelper.FindElementsInHostCoordinates(point, _targetControl);
            bool isDeleteAllJobs = false;
            Button deleteButton = null;
            ToggleButton jobListHeader = null;
            foreach (UIElement element in elements)
            {
                var elementName = ((FrameworkElement)element).Name;
                if (elementName == JOB_LIST_HEADER_DELETE_CONTROL_NAME)
                {
                    deleteButton = (Button)element;
                    isDeleteAllJobs = true;
                }
                else if (elementName == JOB_LIST_HEADER_CONTROL_NAME)
                {
                    jobListHeader = (ToggleButton)element;
                    break;
                }
            }
            // Manually execute delete command
            if (isDeleteAllJobs && jobListHeader != null)
            {
                PrintJobGroup printJobGroup = ((PrintJobGroup)jobListHeader.DataContext);
                var printerId = printJobGroup.Jobs[0].PrinterId;
                _lastDeleteAllButton = deleteButton;
                _lastPrintJobGroup = printJobGroup;
                printJobGroup.DeleteButtonVisualState = "DeletePressed";
                (new ViewModelLocator().JobsViewModel).DeleteAllJobsCommand.Execute(printerId);
            }
            else if (!isDeleteAllJobs && jobListHeader != null)
            {
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
            // If delete job button is visible, ignore gesture
            if (_isDeleteJobButtonVisible && _lastJobListItem != null)
            {
                HideDeleteJobButton();
                _gestureRecognizer.CompleteGesture();
                return;
            }
            var isVerticalSwipe = DetectVerticalSwipe(e.Position, e.Delta.Translation);
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
            }
            // Swipe left, show delete button
            else if (_startPoint.X - currentPosition.X >= SWIPE_THRESHOLD)
            {
                isSwipe = true;
                _gestureRecognizer.CompleteGesture();
                var jobListItem = GetJobListItemControl(currentPosition, isScrolled);
                if (jobListItem == null) return isSwipe;
                if (jobListItem.DeleteButtonVisibility != Visibility.Visible)
                {
                    PrintJob printJob = (PrintJob)jobListItem.DataContext;
                    printJob.DeleteButtonVisibility = Visibility.Visible;
                    jobListItem.VisualState = "Pressed";
                    _lastJobListItem = jobListItem;
                    _lastPrintJob = printJob;
                    _isDeleteJobButtonVisible = true;
                }
            }
            return isSwipe;
        }

        /// <summary>
        /// Hides the delete button.
        /// </summary>
        public void HideDeleteJobButton()
        {
            if (_lastJobListItem == null || !_isDeleteJobButtonVisible) return;
            //PrintJob printJob = (PrintJob)_lastJobListItem.DataContext;
            _lastPrintJob.DeleteButtonVisibility = Visibility.Collapsed;
            _lastJobListItem.VisualState = "Normal";
            _isDeleteJobButtonVisible = false;
        }

        /// <summary>
        /// Hides the Delete All button.
        /// </summary>
        public void HideDeleteAllJobsButton()
        {
            if (_lastDeleteAllButton == null || _lastPrintJobGroup == null) return;
            _lastPrintJobGroup.DeleteButtonVisualState = "DeleteNormal";
            _lastDeleteAllButton = null;
            _lastPrintJobGroup = null;
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

        /// Get tapped JobListItemControl for delete operation
        /// <param name="isScrolled">True if from ManipulationUpdatedEventArgs
        /// Job list item control, null if not tapped at delete button area
        /// False if from TappedEventArgs</param>        
        private JobListItemControl GetJobListItemControlForDelete(Point position, bool isScrolled)
        {
            bool isDeleteJob = false;
            if (!_isDeleteJobButtonVisible) return null;
            var point = TransformPointToGlobalCoordinates(position, isScrolled);
            var elements = VisualTreeHelper.FindElementsInHostCoordinates(point, _targetControl);
            JobListItemControl jobListItemControl = null;
            Button deleteButton = null;
            bool isDeleteButtonHit = false;
            foreach (UIElement element in elements)
            {
                if (jobListItemControl != null && deleteButton != null)
                {
                    break;
                }
                var elementName = ((FrameworkElement)element).Name;
                if (elementName == JOB_LIST_ITEM_CONTROL_NAME)
                {
                    jobListItemControl = (JobListItemControl)element;
                }
                else if (elementName == JOB_LIST_ITEM_DELETE_CONTROL_NAME) // Delete button is hit
                {
                    deleteButton = (Button)element;
                    isDeleteButtonHit = true;
                }
            }
            // Delete job only when the delete button of the current job list item is hit
            isDeleteJob = isDeleteButtonHit && jobListItemControl == _lastJobListItem;
            if (!isDeleteJob) jobListItemControl = null;
            return jobListItemControl;
        }

        private bool DetectVerticalSwipe(Point currentPosition, Point delta)
        {
            var isTranslate = false;
            if (Math.Abs(delta.Y) > 0)
            {
                isTranslate = true;
                var scrollViewer = (ScrollViewer)_controlReference;
                scrollViewer.ChangeView(null, scrollViewer.VerticalOffset - delta.Y, null);

                // Hide delete button only on outside of containing row
                var jobListItem = GetJobListItemControl(currentPosition, isTranslate);
                if (_isDeleteJobButtonVisible &&
                    _lastJobListItem != null && _lastJobListItem != jobListItem)
                {
                    HideDeleteJobButton();
                }
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
