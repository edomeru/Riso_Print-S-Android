using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using System.Collections.ObjectModel;

namespace SmartDeviceApp.ViewModels
{
    public class JobsViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        public JobsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            //Initialize();
            //SortPrintJobsListToColumns();
        }

        private PrintJobList _printJobsList;
        private PrintJobList _printJobsColumn1;
        private PrintJobList _printJobsColumn2;
        private PrintJobList _printJobsColumn3;

        public PrintJobList PrintJobsList
        {
            get { return _printJobsList; }
            set { _printJobsList = value; }
        }

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

        // TODO: Change back to private
        public void SortPrintJobsListToColumns()
        {
            var column1 = new PrintJobList();
            var column2 = new PrintJobList();
            var column3 = new PrintJobList();
            var column1Count = 0;
            var column2Count = 0;
            var column3Count = 0;

            // Place each print job group in column with least items
            // Add 1 count for every group for group header control
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

            PrintJobsColumn1 = column1;
            PrintJobsColumn2 = column2;
            PrintJobsColumn3 = column3;
        }

        // TODO: REMOVE AFTER TESTING
        private void Initialize()
        {   
            var jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0101, 1, "Job1", DateTime.Now, 0));            
            PrintJobsList = new PrintJobList();
            PrintJobsList.Add(new PrintJobGroup("Printer1", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0201, 2, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0102, 2, "Job2", DateTime.Now, 0));
            PrintJobsList.Add(new PrintJobGroup("Printer2", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0301, 3, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0302, 3, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(0303, 3, "Job3", DateTime.Now, 0));
            PrintJobsList.Add(new PrintJobGroup("Printer3", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0401, 4, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0402, 4, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(0403, 4, "Job3", DateTime.Now, 0));
            jobs.Add(new PrintJob(0404, 4, "Job4", DateTime.Now, 1));
            PrintJobsList.Add(new PrintJobGroup("Printer4", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0501, 5, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0502, 5, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(0503, 5, "Job3", DateTime.Now, 0));
            jobs.Add(new PrintJob(0504, 5, "Job4", DateTime.Now, 1));
            jobs.Add(new PrintJob(0505, 5, "Job5", DateTime.Now, 1));
            PrintJobsList.Add(new PrintJobGroup("Printer5", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0601, 6, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0602, 6, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(0603, 6, "Job3", DateTime.Now, 0));
            jobs.Add(new PrintJob(0604, 6, "Job4", DateTime.Now, 1));
            jobs.Add(new PrintJob(0605, 6, "Job5", DateTime.Now, 1));
            jobs.Add(new PrintJob(0606, 6, "Job6", DateTime.Now, 1));
            PrintJobsList.Add(new PrintJobGroup("Printer6", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0701, 7, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0702, 7, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(0703, 7, "Job3", DateTime.Now, 0));
            jobs.Add(new PrintJob(0704, 7, "Job4", DateTime.Now, 1));
            jobs.Add(new PrintJob(0705, 7, "Job5", DateTime.Now, 1));
            jobs.Add(new PrintJob(0706, 7, "Job6", DateTime.Now, 1));
            jobs.Add(new PrintJob(0707, 7, "Job7", DateTime.Now, 1));
            PrintJobsList.Add(new PrintJobGroup("Printer7", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0801, 8, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0802, 8, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(0803, 8, "Job3", DateTime.Now, 0));
            jobs.Add(new PrintJob(0804, 8, "Job4", DateTime.Now, 1));
            jobs.Add(new PrintJob(0805, 8, "Job5", DateTime.Now, 1));
            jobs.Add(new PrintJob(0806, 8, "Job6", DateTime.Now, 1));
            jobs.Add(new PrintJob(0807, 8, "Job7", DateTime.Now, 1));
            jobs.Add(new PrintJob(0808, 8, "Job8", DateTime.Now, 1));
            PrintJobsList.Add(new PrintJobGroup("Printer8", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(0901, 9, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(0902, 9, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(0903, 9, "Job3", DateTime.Now, 0));
            jobs.Add(new PrintJob(0904, 9, "Job4", DateTime.Now, 1));
            jobs.Add(new PrintJob(0905, 9, "Job5", DateTime.Now, 1));
            jobs.Add(new PrintJob(0906, 9, "Job6", DateTime.Now, 1));
            jobs.Add(new PrintJob(0907, 9, "Job7", DateTime.Now, 1));
            jobs.Add(new PrintJob(0908, 9, "Job8", DateTime.Now, 1));
            jobs.Add(new PrintJob(0909, 9, "Job9", DateTime.Now, 1));
            PrintJobsList.Add(new PrintJobGroup("Printer9", jobs));

            jobs = new List<PrintJob>();
            jobs.Add(new PrintJob(1001, 10, "Job1", DateTime.Now, 0));
            jobs.Add(new PrintJob(1002, 10, "Job2", DateTime.Now, 0));
            jobs.Add(new PrintJob(1003, 10, "Job3", DateTime.Now, 0));
            jobs.Add(new PrintJob(1004, 10, "Job4", DateTime.Now, 1));
            jobs.Add(new PrintJob(1005, 10, "Job5", DateTime.Now, 1));
            jobs.Add(new PrintJob(1006, 10, "Job6", DateTime.Now, 1));
            jobs.Add(new PrintJob(1007, 10, "Job7", DateTime.Now, 1));
            jobs.Add(new PrintJob(1008, 10, "Job8", DateTime.Now, 1));
            jobs.Add(new PrintJob(1009, 10, "Job9", DateTime.Now, 1));
            jobs.Add(new PrintJob(1010, 10, "Job10", DateTime.Now, 1));
            PrintJobsList.Add(new PrintJobGroup("Printer10", jobs));
        }
    }
}
