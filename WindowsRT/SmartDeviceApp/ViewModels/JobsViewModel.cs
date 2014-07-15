using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Collections.ObjectModel;
using System.Diagnostics;
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
using SmartDeviceApp.Common.Constants;

namespace SmartDeviceApp.ViewModels
{
    public class JobsViewModel : ViewModelBase
    {
        /// <summary>
        /// Remove job event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.JobController.RemoveJobEventHandler RemoveJobEventHandler;

        /// <summary>
        /// Remove grouped jobs event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.JobController.RemoveGroupedJobsEventHandler RemoveGroupedJobsEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _deleteAllJobsCommand;
        private ICommand _deleteJobCommand;

        private PrintJobList _printJobsList;
        private PrintJobList _printJobsColumn1;
        private PrintJobList _printJobsColumn2;
        private PrintJobList _printJobsColumn3;
        private int _maxColumns;
        
        private const int MAX_COLUMNS_LANDSCAPE = 3;
        private const int MAX_COLUMNS_PORTRAIT = 2;

        private JobGestureController _gestureController;
        private ViewControlViewModel _viewControlViewModel;

        private bool _isPrintJobsListEmpty;
        private bool _isProgressRingActive;

        private double _columnWidth;
        private double _keyTextWidth;

        /// <summary>
        /// JobsViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public JobsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => SetViewOrientation(viewOrientation));
            SetViewOrientation(_viewControlViewModel.ViewOrientation); // Initialize MaxColumns
        }

        /// <summary>
        /// Command for Delete All button
        /// </summary>
        public ICommand DeleteAllJobsCommand
        {
            get
            {
                if (_deleteAllJobsCommand == null)
                {
                    _deleteAllJobsCommand = new RelayCommand<int>(
                        (printerId) => DeleteAllJobsExecute(printerId),
                        (printerId) => true
                    );
                }
                return _deleteAllJobsCommand;
            }
        }

        /// <summary>
        /// Command for Delete button (print job item)
        /// </summary>
        public ICommand DeleteJobCommand
        {
            get
            {
                if (_deleteJobCommand == null)
                {
                    _deleteJobCommand = new RelayCommand<PrintJob>(
                        (printJob) => DeleteJobExecute(printJob),
                        (printJob) => true
                    );
                }
                return _deleteJobCommand;
            }
        }

        /// <summary>
        /// All print jobs list
        /// </summary>
        public PrintJobList PrintJobsList
        {
            get { return _printJobsList; }
            set
            {
                if (_printJobsList != value)
                {
                    _printJobsList = value;
                    RaisePropertyChanged("PrintJobsList");
                }
            }
        }

        /// <summary>
        /// True when print job list is empty, false otherwise
        /// </summary>
        public bool IsPrintJobsListEmpty
        {
            get { return _isPrintJobsListEmpty; }
            set
            {
                if (_isPrintJobsListEmpty != value)
                {
                    _isPrintJobsListEmpty = value;
                    RaisePropertyChanged("IsPrintJobsListEmpty");
                    if (_isPrintJobsListEmpty) IsProgressRingActive = false;
                    else IsProgressRingActive = true;
                }
            }
        }

        /// <summary>
        /// Sorted print job list for the first column
        /// </summary>
        public PrintJobList PrintJobsColumn1
        {
            get { return _printJobsColumn1; }
            set
            {
                if (_printJobsColumn1 != value)
                {
                    _printJobsColumn1 = value;
                    RaisePropertyChanged("PrintJobsColumn1");
                }
            }
        }

        /// <summary>
        /// Sorted print job list for the second column
        /// </summary>
        public PrintJobList PrintJobsColumn2
        {
            get { return _printJobsColumn2; }
            set
            {
                if (_printJobsColumn2 != value)
                {
                    _printJobsColumn2 = value;
                    RaisePropertyChanged("PrintJobsColumn2");
                }
            }
        }

        /// <summary>
        /// Sorted print job list for the third column
        /// </summary>
        public PrintJobList PrintJobsColumn3
        {
            get { return _printJobsColumn3; }
            set
            {
                if (_printJobsColumn3 != value)
                {
                    _printJobsColumn3 = value;
                    RaisePropertyChanged("PrintJobsColumn3");
                }
            }
        }

        /// <summary>
        /// Gets/sets the maximum number of columns in Print Job History Screen
        /// </summary>
        public int MaxColumns
        {
            get { return _maxColumns; }
            set
            {
                if (_maxColumns != value)
                {
                    _maxColumns = value;
                    RaisePropertyChanged("MaxColumns");
                    SortPrintJobsListToColumns();
                }
            }
        }

        /// <summary>
        /// Gets/sets the width of the columns
        /// </summary>
        public double ColumnWidth
        {
            get { return _columnWidth; }
            set
            {
                if (_columnWidth != value)
                {
                    _columnWidth = value;
                    SetMaxTextWidth();
                }
            }
        }

        /// <summary>
        /// Gets/sets the width of the key text
        /// </summary>
        public double KeyTextWidth
        {
            get { return _keyTextWidth; }
            set
            {
                if (_keyTextWidth != value)
                {
                    _keyTextWidth = value;
                    RaisePropertyChanged("KeyTextWidth");
                }
            }
        }

        /// <summary>
        /// True when loading indicator is active, false otherwise
        /// </summary>
        public bool IsProgressRingActive
        {
            get { return _isProgressRingActive; }
            set
            {
                if (_isProgressRingActive != value)
                {
                    _isProgressRingActive = value;
                    RaisePropertyChanged("IsProgressRingActive");
                }
            }
        }

        /// <summary>
        /// Gets/sets the GestureController object
        /// </summary>
        public JobGestureController GestureController
        {
            get { return _gestureController; }
            set { _gestureController = value; }
        }

        private void SetViewMode(ViewMode viewMode)
        {
            if (_viewControlViewModel.ScreenMode != ScreenMode.Jobs) return;
            switch (viewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        if (_gestureController != null)
                        {
                            _gestureController.DisableGestures();
                        }
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        if (_gestureController != null)
                        {
                            _gestureController.EnableGestures();
                        }
                        break;
                    }
                case ViewMode.RightPaneVisible:
                case ViewMode.RightPaneVisible_ResizedWidth: // NOTE: Technically not possible
                    {
                        if (_gestureController != null)
                        {
                            _gestureController.EnableGestures();
                        }
                        break;
                    }
            }
        }

        private void SetViewOrientation(ViewOrientation viewOrientation)
        {
            if (!IsPrintJobsListEmpty) IsProgressRingActive = true;
            if (viewOrientation == ViewOrientation.Landscape)
            {
                MaxColumns = MAX_COLUMNS_LANDSCAPE;
            }
            else if (viewOrientation == ViewOrientation.Portrait)
            {
                MaxColumns = MAX_COLUMNS_PORTRAIT;
            }            
        }

        private async void DeleteAllJobsExecute(int printerId)
        {
            await DialogService.Instance.ShowMessage(
                "IDS_INFO_MSG_DELETE_JOBS", 
                "IDS_INFO_MSG_DELETE_JOBS_TITLE", 
                "IDS_LBL_OK", "IDS_LBL_CANCEL",
                new Action<bool>(isDelete =>
                    {
                        if (isDelete && RemoveGroupedJobsEventHandler != null)
                        {
                            RemoveGroupedJobsEventHandler(printerId);
                        }
                    }));
        }

        private async void DeleteJobExecute(PrintJob printJob)
        {
            await DialogService.Instance.ShowMessage(
                "IDS_INFO_MSG_DELETE_JOBS",
                "IDS_INFO_MSG_DELETE_JOBS_TITLE",
                "IDS_LBL_OK", "IDS_LBL_CANCEL",
                new Action<bool>(isDelete =>
                {
                    if (isDelete && RemoveJobEventHandler != null)
                    {
                        RemoveJobEventHandler(printJob);
                    }
                }));
        }

        /// <summary>
        /// Sorts the print job list into columns (based on size)
        /// </summary>
        public void SortPrintJobsListToColumns()
        {
            if (PrintJobsList == null || PrintJobsList.Count == 0)
            {
                IsPrintJobsListEmpty = true;
                return;
            }
            else 
            {
                IsPrintJobsListEmpty = false;
            }

            // Place each print job group in column with least items
            // Add 1 count for every group for group header control
            var column1 = new PrintJobList();
            var column2 = new PrintJobList();            
            var column1Count = 0;
            var column2Count = 0;           

            if (MaxColumns == MAX_COLUMNS_LANDSCAPE)
            {
                var column3 = new PrintJobList();
                var column3Count = 0;
                foreach (PrintJobGroup group in PrintJobsList)
                {
                    if (column1Count <= column2Count && column1Count <= column3Count)
                    {
                        column1.Add(group);
                        column1Count += 1 + group.Jobs.Count;
                    }
                    else if (column2Count <= column1Count && column2Count <= column3Count)
                    {
                        column2.Add(group);
                        column2Count += 1 + group.Jobs.Count;
                    }
                    else if (column3Count <= column1Count && column3Count <= column2Count)
                    {
                        column3.Add(group);
                        column3Count += 1 + group.Jobs.Count;
                    }
                }
                PrintJobsColumn3 = column3;
            }
            else if (MaxColumns == MAX_COLUMNS_PORTRAIT)                
            {
                foreach (PrintJobGroup group in PrintJobsList)
                {
                    if (column1Count <= column2Count)
                    {
                        column1.Add(group);
                        column1Count += 1 + group.Jobs.Count;
                    }
                    else if (column2Count <= column1Count)
                    {
                        column2.Add(group);
                        column2Count += 1 + group.Jobs.Count;
                    }
                }
            }

            PrintJobsColumn1 = column1;
            PrintJobsColumn2 = column2;
        }

        #region For testing
        //// TODO: REMOVE AFTER TESTING
        //private void Initialize()
        //{
        //    var printJobsList = new PrintJobList();

        //    var jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0101, 1, "Job1", DateTime.Now, 0));
        //    printJobsList = new PrintJobList();
        //    printJobsList.Add(new PrintJobGroup("Printer1", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0201, 2, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0102, 2, "Job2", DateTime.Now, 0));
        //    printJobsList.Add(new PrintJobGroup("Printer2", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0301, 3, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0302, 3, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0303, 3, "Job3", DateTime.Now, 0));
        //    printJobsList.Add(new PrintJobGroup("Printer3", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0401, 4, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0402, 4, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0403, 4, "Job3", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0404, 4, "Job4", DateTime.Now, 1));
        //    printJobsList.Add(new PrintJobGroup("Printer4", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0501, 5, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0502, 5, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0503, 5, "Job3", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0504, 5, "Job4", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0505, 5, "Job5", DateTime.Now, 1));
        //    printJobsList.Add(new PrintJobGroup("Printer5", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0601, 6, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0602, 6, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0603, 6, "Job3", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0604, 6, "Job4", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0605, 6, "Job5", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0606, 6, "Job6", DateTime.Now, 1));
        //    printJobsList.Add(new PrintJobGroup("Printer6", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0701, 7, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0702, 7, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0703, 7, "Job3", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0704, 7, "Job4", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0705, 7, "Job5", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0706, 7, "Job6", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0707, 7, "Job7", DateTime.Now, 1));
        //    printJobsList.Add(new PrintJobGroup("Printer7", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0801, 8, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0802, 8, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0803, 8, "Job3", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0804, 8, "Job4", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0805, 8, "Job5", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0806, 8, "Job6", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0807, 8, "Job7", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0808, 8, "Job8", DateTime.Now, 1));
        //    printJobsList.Add(new PrintJobGroup("Printer8", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(0901, 9, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0902, 9, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0903, 9, "Job3", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(0904, 9, "Job4", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0905, 9, "Job5", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0906, 9, "Job6", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0907, 9, "Job7", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0908, 9, "Job8", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(0909, 9, "Job9", DateTime.Now, 1));
        //    printJobsList.Add(new PrintJobGroup("Printer9", jobs));

        //    jobs = new ObservableCollection<PrintJob>();
        //    jobs.Add(new PrintJob(1001, 10, "Job1", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(1002, 10, "Job2", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(1003, 10, "Job3", DateTime.Now, 0));
        //    jobs.Add(new PrintJob(1004, 10, "Job4", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(1005, 10, "Job5", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(1006, 10, "Job6", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(1007, 10, "Job7", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(1008, 10, "Job8", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(1009, 10, "Job9", DateTime.Now, 1));
        //    jobs.Add(new PrintJob(1010, 10, "Job10", DateTime.Now, 1));
        //    printJobsList.Add(new PrintJobGroup("Printer10", jobs));

        //    PrintJobsList = printJobsList;
        //    SortPrintJobsListToColumns();
        //}
        #endregion

        /// <summary>
        /// Removes a print job group and sorts the columns if needed
        /// </summary>
        /// <param name="deletedGroup">print job group to be deleted</param>
        public void RemovePrintJobGroup(PrintJobGroup deletedGroup)
        {
            if (PrintJobsColumn1.Contains(deletedGroup))
            {
                PrintJobsColumn1.Remove(deletedGroup);
                if (PrintJobsColumn1.Count == 0)
                {
                    SortPrintJobsListToColumns();
                }
                return;
            }
            if (PrintJobsColumn2.Contains(deletedGroup))
            {
                PrintJobsColumn2.Remove(deletedGroup);
                if (PrintJobsColumn2.Count == 0)
                {
                    SortPrintJobsListToColumns();
                }
                return;
            }
            if (MaxColumns == MAX_COLUMNS_LANDSCAPE)
            {
                if (PrintJobsColumn3.Contains(deletedGroup))
                {
                    PrintJobsColumn3.Remove(deletedGroup);
                    if (PrintJobsColumn3.Count == 0)
                    {
                        SortPrintJobsListToColumns();
                    }
                    return;
                }
            }            
        }

        /// <summary>
        /// Sets the maximum width of the text view
        /// </summary>
        public void SetMaxTextWidth()
        {
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var smallMargin = (double)Application.Current.Resources["MARGIN_Small"];

            double maxTextWidth = ColumnWidth;

            // Left and right margins
            maxTextWidth -= (defaultMargin * 2);

            // Status icon width
            var tempControl = new JobListItemControl();
            var imageWidth = ImageConstant.GetIconImageWidth(tempControl);
            maxTextWidth -= imageWidth;
            maxTextWidth -= defaultMargin;
            tempControl = null;

            // Value text width
            maxTextWidth -= (double)Application.Current.Resources["SIZE_JobListValueTextWidth"];
            maxTextWidth -= smallMargin; // Space between key and value texts
            KeyTextWidth = maxTextWidth;
        }
    }
}