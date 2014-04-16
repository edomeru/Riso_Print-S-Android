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
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections.ObjectModel;
using GalaSoft.MvvmLight;

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
        /// Printer ID, used by PrintJob table and is indexed
        /// </summary>
        [SQLite.Column("prn_id"), SQLite.NotNull, SQLite.Indexed(Name = "PrintJob_FKIndex1")]
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
            Id = id;
            PrinterId = printerId;
            Name = name;
            Date = date;
            Result = result;
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
        private List<PrintJob> _jobs;
        public int PrinterId { get; set; }
        public string PrinterName { get; set; }
        public List<PrintJob> Jobs
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

        public PrintJobGroup(string printerName, List<PrintJob> jobs)
        {
            PrinterName = printerName;
            Jobs = jobs;
        }
    }

    public class PrintJobList : ObservableCollection<PrintJobGroup>
    {
        public List<PrintJobGroup> Groups { get; set; }
    }
}
