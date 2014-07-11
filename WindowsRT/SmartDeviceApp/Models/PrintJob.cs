//
//  PrintJob.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/17.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using GalaSoft.MvvmLight;
using Windows.UI.Xaml;

namespace SmartDeviceApp.Models
{
    public class PrintJob : ObservableObject
    {

        #region Properties

        /// <summary>
        /// Print job ID, used by PrintJob table as primary key
        /// </summary>
        [SQLite.Column("pjb_id"), SQLite.NotNull, SQLite.PrimaryKey, SQLite.AutoIncrement]
        public int Id { get; set; }

        /// <summary>
        /// Printer ID, used by PrintJob table
        /// </summary>
        [SQLite.Column("prn_id"), SQLite.NotNull]
        public int PrinterId { get; set; }

        /// <summary>
        /// Print job name, used by PrintJob table
        /// </summary>
        [SQLite.Column("pjb_name")]
        public string Name { get; set; }

        /// <summary>
        /// Print job date/time, used by PrintJob table
        /// </summary>
        [SQLite.Column("pjb_date")]
        public DateTime Date { get; set; }

        /// <summary>
        /// Print job result, used by PrintJob table
        /// </summary>
        [SQLite.Column("pjb_result")]
        public int Result { get; set; }

        
        private Visibility _deleteButtonVisibility;
        [SQLite.Ignore]
        public Visibility DeleteButtonVisibility
        {
            get { return _deleteButtonVisibility; }
            set
            {
                _deleteButtonVisibility = value;
                RaisePropertyChanged("DeleteButtonVisibility");
            }
        }

        #endregion Properties

        /// <summary>
        /// PrintJob default class constructor
        /// </summary>
        public PrintJob()
        {
            Id = -1;
            PrinterId = -1;
            Name = null;
            Date = DateTime.MinValue;
            Result = -1;
            _deleteButtonVisibility = Visibility.Collapsed;
        }

        /// <summary>
        /// PrintJob class constructor
        /// </summary>
        /// <param name="id">print job ID</param>
        /// <param name="printerId">printer ID</param>
        /// <param name="name">print job name</param>
        /// <param name="date">print job date/time</param>
        /// <param name="result">print job result</param>
        public PrintJob(int id, int printerId, string name, DateTime date, int result)
        {
            // TODO: This constructor should be deleted
            Id = id;
            PrinterId = printerId;
            Name = name;
            Date = date;
            Result = result;
            _deleteButtonVisibility = Visibility.Collapsed;
        }

        public override bool Equals(System.Object obj)
        {
            if (obj == null)
            {
                return false;
            }

            PrintJob otherOption = obj as PrintJob;
            if ((System.Object)otherOption == null)
            {
                return false;
            }
            return Id == otherOption.Id;
        }

        public bool Equals(PrintJob otherOption)
        {
            if ((object)otherOption == null)
            {
                return false;
            }
            return (Id == otherOption.Id);
        }

        public override int GetHashCode()
        {
            return Id.GetHashCode();
        }
    }

    public class PrintJobGroup : ObservableObject
    {
        private ObservableCollection<PrintJob> _jobs;
        public string PrinterName { get; set; }
        public string IpAddress { get; set; }
        public ObservableCollection<PrintJob> Jobs
        {
            get { return _jobs; }
            set
            {
                if (_jobs != value)
                {
                    _jobs = value;
                    RaisePropertyChanged("Jobs");
                }
            }
        }

        public PrintJobGroup(string printerName, string ipAddress, ObservableCollection<PrintJob> jobs)
        {
            PrinterName = printerName;
            IpAddress = ipAddress;
            Jobs = jobs;
        }

        public override bool Equals(System.Object obj)
        {
            if (obj == null)
            {
                return false;
            }

            PrintJobGroup otherOption = obj as PrintJobGroup;
            if ((System.Object)otherOption == null)
            {
                return false;
            }
            return (PrinterName == otherOption.PrinterName && 
                IpAddress == otherOption.IpAddress);
        }

        public bool Equals(PrintJobGroup otherOption)
        {
            if ((object)otherOption == null)
            {
                return false;
            }
            return (PrinterName == otherOption.PrinterName && 
                IpAddress == otherOption.IpAddress);
        }

        public override int GetHashCode()
        {
            return IpAddress.GetHashCode();
        }
    }

    public class PrintJobList : ObservableCollection<PrintJobGroup>
    {
        public List<PrintJobGroup> Groups { get; set; }
    }
}
