//
//  JobController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/11.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;

namespace SmartDeviceApp.Controllers
{
    public sealed class JobController
    {
        static readonly JobController _instance = new JobController();

        public delegate void RemoveJobEventHandler(PrintJob printJob);
        public delegate void RemoveGroupedJobsEventHandler(int printJobId);
        private RemoveJobEventHandler _removeJobEventHandler;
        private RemoveGroupedJobsEventHandler _removeGroupedJobsEventHandler;

        private JobsViewModel _jobsViewModel;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static JobController() { }

        private JobController()
        {
            _removeJobEventHandler = new RemoveJobEventHandler(RemoveJob);
            _removeGroupedJobsEventHandler = new RemoveGroupedJobsEventHandler(RemoveGroupedJobs);
        }

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static JobController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Initialize
        /// </summary>
        /// <returns></returns>
        public async Task Initialize()
        {
            _jobsViewModel = new ViewModelLocator().JobsViewModel;
            await RefreshPrintJobsList();
            _jobsViewModel.SortPrintJobsListToColumns();

            _jobsViewModel.RemoveJobEventHandler += _removeJobEventHandler;
            _jobsViewModel.RemoveGroupedJobsEventHandler += _removeGroupedJobsEventHandler;
        }

        /// <summary>
        /// Clean-up
        /// </summary>
        public void Cleanup()
        {
            _jobsViewModel.RemoveJobEventHandler -= _removeJobEventHandler;
            _jobsViewModel.RemoveGroupedJobsEventHandler -= _removeGroupedJobsEventHandler;
        }

        /// <summary>
        /// Refreshes the print jobs list
        /// </summary>
        /// <returns>task</returns>
        private async Task RefreshPrintJobsList()
        {
            Cleanup();
            await FetchJobs();
        }

        /// <summary>
        /// Retrieves all print jobs in the database
        /// </summary>
        /// <returns>task</returns>
        private async Task FetchJobs()
        {
            List<PrintJob> printJobList = await DatabaseController.Instance.GetPrintJobs();
            PrintJobList tempList = new PrintJobList();
            var orderedList = printJobList.OrderBy(pj => pj.PrinterId)
                                          .ThenBy(pj => pj.Date)
                                          .GroupBy(pj => pj.PrinterId).ToList();
            foreach (var group in orderedList)
            {
                // Get printer name of the first element
                string printerName = string.Empty;
                PrintJob firstSample = group.FirstOrDefault();
                if (firstSample != null)
                {
                    printerName = await DatabaseController.Instance
                        .GetPrinterName(firstSample.PrinterId);
                }

                PrintJobGroup printJobGroup = new PrintJobGroup(printerName.Trim(),
                    new ObservableCollection<PrintJob>(group));
                tempList.Add(printJobGroup);
            }

            _jobsViewModel.PrintJobsList = tempList;
        }

        /// <summary>
        /// Adds a print job
        /// </summary>
        /// <param name="printJob">item</param>
        public async void SavePrintJob(PrintJob printJob)
        {
            if (printJob != null && printJob.PrinterId > -1)
            {
                int added = await DatabaseController.Instance.InsertPrintJob(printJob);
                if (added == 0)
                {
                    // TODO: Notify error?
                    return;
                }

                // TODO: Verify bindings
                PrintJobGroup printJobGroup = _jobsViewModel.PrintJobsList
                    .FirstOrDefault(group => group.Jobs[0].PrinterId == printJob.PrinterId);
                if (printJobGroup != null)
                {
                    printJobGroup.Jobs.Add(printJob);
                }
            }
        }

        /// <summary>
        /// Event handler for deleting a print job
        /// </summary>
        /// <param name="printJob">item</param>
        public async void RemoveJob(PrintJob printJob)
        {
            if (printJob != null)
            {
                int deleted = await DeletePrintJob(printJob);
                if (deleted == 0)
                {
                    // TODO: Notify view model to display error message
                }
            }
        }

        /// <summary>
        /// Event handler for deleting a grouped print job
        /// </summary>
        /// <param name="printerId">printer ID</param>
        public async void RemoveGroupedJobs(int printerId)
        {
            PrintJobGroup printJobGroup = _jobsViewModel.PrintJobsList
                .FirstOrDefault(group => group.Jobs[0].PrinterId == printerId);

            int deleted = 0;

            if (printJobGroup != null)
            {
                foreach (PrintJob printJob in printJobGroup.Jobs)
                {
                    deleted += await DeletePrintJob(printJob);
                }
            }
            
            if (deleted != printJobGroup.Jobs.Count)
            {
                // TODO: Notify view model to display error message
            }
        }

        /// <summary>
        /// Deletes a print job from the database and updates the list.
        /// Also, removes the group if the list is empty.
        /// </summary>
        /// <param name="printJob">print job to be deleted</param>
        /// <returns>task; number of deleted print jobs</returns>
        private async Task<int> DeletePrintJob(PrintJob printJob)
        {
            int deleted = await DatabaseController.Instance.DeletePrintJob(printJob);
            if (deleted == 0)
            {
                return deleted;
            }

            PrintJobGroup printJobGroup = _jobsViewModel.PrintJobsList
                .FirstOrDefault(group => group.Jobs[0].PrinterId == printJob.PrinterId);
            if (printJobGroup != null)
            {
                printJobGroup.Jobs.Remove(printJob);
            }
            if (printJobGroup.Jobs.Count == 0)
            {
                _jobsViewModel.PrintJobsList.Remove(printJobGroup);
            }

            return deleted;
        }

    }
}
