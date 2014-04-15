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

        private JobsViewModel _jobsViewModel;
        private List<PrintJob> _printJobsList;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static JobController() { }

        private JobController() { }

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static JobController Instance
        {
            get { return _instance; }
        }

        public async Task Initialize()
        {
            _jobsViewModel = new ViewModelLocator().JobsViewModel;
            await RefreshPrintJobsList();
        }

        public void Cleanup()
        {
            if (_printJobsList != null)
            {
                _printJobsList.Clear();
                _printJobsList = null;
            }
        }

        private async Task RefreshPrintJobsList()
        {
            Cleanup();
            await FetchJobs();

            PrintJobList printJobList = new PrintJobList();
            if (_printJobsList != null)
            {
                var list = _printJobsList.OrderBy(pj => pj.PrinterId).ThenBy(pj => pj.Date).GroupBy(pj => pj.PrinterId).ToList();
                foreach (var group in list)
                {
                    // Get printer name of the first element
                    string printerName = await DatabaseController.Instance.GetPrinterName(
                        group.First().PrinterId);

                    PrintJobGroup printJobGroup = new PrintJobGroup(printerName.Trim(), group.ToList());
                    printJobList.Add(printJobGroup);
                }
            }

            _jobsViewModel.PrintJobsList = printJobList;

            // TODO: Temporary only
            _jobsViewModel.SortPrintJobsListToColumns();
        }

        /// <summary>
        /// Retrieves all print jobs in the database
        /// </summary>
        /// <returns>task</returns>
        private async Task FetchJobs()
        {
            _printJobsList = await DatabaseController.Instance.GetPrintJobs();
        }

        public async Task SavePrintJob(PrintJob printJob)
        {
            if (printJob == null || _printJobsList == null) // TODO: Checking of list is empty needed?
            {
                return;
            }

            int added = await DatabaseController.Instance.InsertPrintJob(printJob);
            if (added > 0)
            {
                _printJobsList.Add(printJob);
            }
        }

        public async Task RemoveJob(int printJobId, int printerId)
        {
            if (_printJobsList == null) // TODO: Checking of list is empty needed?
            {
                return;
            }

            PrintJob printJob = _printJobsList.Where(pj => pj.PrinterId == printerId)
                                              .Where(pj => pj.Id == printJobId).First();

            if (printJob != null)
            {
                int deleted = await DeletePrintJob(printJob);
                if (deleted == 0)
                {
                    // TODO: Notify view model to display error message
                    return;
                }

                // TODO: Update bindings
                _jobsViewModel.PrintJobsList.ElementAt(printerId).Jobs.Remove(printJob);
            }
        }

        public async Task RemoveGroupedJobs(int printerId)
        {
            if (_printJobsList == null) // TODO: Checking of list is empty needed?
            {
                return;
            }

            List<PrintJob> printJobs = _printJobsList.Where(pj => pj.PrinterId == printerId).ToList();

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

            // TODO: Update bindings
            PrintJobGroup group = _jobsViewModel.PrintJobsList.First(pjg => pjg.PrinterId == printerId);
            _jobsViewModel.PrintJobsList.Remove(group);
        }

        private async Task<int> DeletePrintJob(PrintJob printJob)
        {
            // Remove from database
            int deleted = await DatabaseController.Instance.DeletePrintJob(printJob);

            // Remove from list
            _printJobsList.Remove(printJob);

            return deleted;
        }

    }
}
