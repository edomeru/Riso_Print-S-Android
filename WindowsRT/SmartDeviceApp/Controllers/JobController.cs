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
        private List<PrintJob> _printJobList;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static JobController() { }

        private JobController()
        {
            _printJobList = new List<PrintJob>();
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

            _jobsViewModel.RemoveJobEventHandler += _removeJobEventHandler;
            _jobsViewModel.RemoveGroupedJobsEventHandler += _removeGroupedJobsEventHandler;
        }

        /// <summary>
        /// Clean-up
        /// </summary>
        public void Cleanup()
        {
            if (_printJobList != null)
            {
                _printJobList.Clear();
            }
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
        /// Converts cache to print job list
        /// </summary>
        /// <returns>task; converted list</returns>
        private async Task<PrintJobList> ConvertToPrintJobList()
        {
            PrintJobList list = new PrintJobList();

            if (_printJobList != null)
            {
                var tempList = _printJobList.OrderBy(pj => pj.PrinterId)
                                        .ThenBy(pj => pj.Date)
                                        .GroupBy(pj => pj.PrinterId).ToList();
                foreach (var group in tempList)
                {
                    // Get printer name of the first element
                    string printerName = await DatabaseController.Instance.GetPrinterName(
                        group.First().PrinterId);

                    PrintJobGroup printJobGroup = new PrintJobGroup(printerName.Trim(),
                        group.ToList());
                    list.Add(printJobGroup);
                }
            }

            return list;
        }

        /// <summary>
        /// Retrieves all print jobs in the database
        /// </summary>
        /// <returns>task</returns>
        private async Task FetchJobs()
        {
            _printJobList = await DatabaseController.Instance.GetPrintJobs();
            _jobsViewModel.PrintJobsList = await ConvertToPrintJobList();
        }

        /// <summary>
        /// Adds a print job
        /// </summary>
        /// <param name="printJob">item</param>
        public async void SavePrintJob(PrintJob printJob)
        {
            if (printJob != null)
            {
                int added = await DatabaseController.Instance.InsertPrintJob(printJob);
                if (added == 0)
                {
                    // TODO: Notify error?
                    return;
                }

                _printJobList.Add(printJob);
                _jobsViewModel.PrintJobsList = await ConvertToPrintJobList();
            }
        }

        /// <summary>
        /// Deletes a print job
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
                    return;
                }

                _jobsViewModel.PrintJobsList = await ConvertToPrintJobList();
            }
        }

        /// <summary>
        /// Deletes a set of print jobs based on printer group
        /// </summary>
        /// <param name="printerId">printer ID</param>
        public async void RemoveGroupedJobs(int printerId)
        {
            // Cache items to be deleted
            List<PrintJob> printJobs = _printJobList.Where(pj => pj.PrinterId == printerId).ToList();

            int deleted = 0;
            foreach(PrintJob printJob in printJobs)
            {
                deleted += await DeletePrintJob(printJob);
            }

            if (deleted != printJobs.Count)
            {
                // TODO: Notify view model to display error message
                return;
            }

            _jobsViewModel.PrintJobsList = await ConvertToPrintJobList();
        }

        /// <summary>
        /// Deletes print job from the database and list
        /// </summary>
        /// <param name="printJob">item</param>
        /// <returns>task; number of deleted items</returns>
        private async Task<int> DeletePrintJob(PrintJob printJob)
        {
            // Remove from database
            int deleted = await DatabaseController.Instance.DeletePrintJob(printJob);

            // Remove from list
            _printJobList.Remove(printJob);

            return deleted;
        }

    }
}
