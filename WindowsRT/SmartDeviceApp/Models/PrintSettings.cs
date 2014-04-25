﻿//
//  PrintSettings.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/20.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Serialization;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Models
{
    public class PrintSettingOption
    {
        public string Text { get; set; }
        public int Index { get; set; }
        public bool IsEnabled { get; set; }

        public override bool Equals(System.Object obj)
        {
            if (obj == null)
            {
                return false;
            }

            PrintSettingOption otherOption = obj as PrintSettingOption;
            if ((System.Object)otherOption == null)
            {
                return false;
            }
            return Text == otherOption.Text;
        }

        public bool Equals(PrintSettingOption otherOption)
        {
            if ((object)otherOption == null)
            {
                return false;
            }
            return (Text == otherOption.Text);
        }

        public override int GetHashCode()
        {
            return Text.GetHashCode();
        }
    }

    public class PrintSetting : ObservableObject
    {
        private object _value;
        private PrintSettingOption _selectedOption;
        private bool _isEnabled;
        private bool _isValueDisplayed;
        public string Name { get; set; }
        public string Text { get; set; }
        public string Icon { get; set; }
        public PrintSettingType Type { get; set; }
        public object Value
        {
            get { return _value; }
            set
            {
                if (_value != value)
                {
                    _value = value;
                    RaisePropertyChanged("Value");
                    RaisePropertyChanged("SelectedOption");
                }
            }
        }

        // Should be used only if PrintSettingType = list
        public PrintSettingOption SelectedOption
        {
            get 
            {
                if ((int)Value < Options.Count) _selectedOption = Options[(int)Value];
                else _selectedOption = null;
                return _selectedOption;
            }
            set
            {
                if (_selectedOption != value)
                {
                    _selectedOption = value;
                    RaisePropertyChanged("SelectedOption");
                }
            }
        }
        public object Default { get; set; }
        public List<PrintSettingOption> Options { get; set; }
        public bool IsEnabled
        {
            get { return _isEnabled; }
            set
            {
                if (_isEnabled != value)
                {
                    _isEnabled = value;
                    RaisePropertyChanged("IsEnabled");
                }
            }
        }
        public bool IsValueDisplayed
        {
            get { return _isEnabled; }
            set
            {
                if (_isEnabled != value)
                {
                    _isEnabled = value;
                    RaisePropertyChanged("IsValueDisplayed");
                }
            }
        }

        public override bool Equals(System.Object obj)
        {
            if (obj == null)
            {
                return false;
            }

            PrintSetting otherPrintSetting = obj as PrintSetting;
            if ((System.Object)otherPrintSetting == null)
            {
                return false;
            }
            return Text == otherPrintSetting.Text;
        }

        public bool Equals(PrintSetting otherPrintSetting)
        {
            if ((object)otherPrintSetting == null)
            {
                return false;
            }
            return (Text == otherPrintSetting.Text);
        }

        public override int GetHashCode()
        {
            return Text.GetHashCode();
        }
    }

    public class PrintSettingGroup
    {
        public string Name { get; set; }
        public string Text { get; set; }
        public List<PrintSetting> PrintSettings { get; set; }
    }

    public class PrintSettingList : ObservableCollection<PrintSettingGroup>
    {
        public List<PrintSettingGroup> Groups { get; set; }
    }

    [SQLite.Table("PrintSetting")]
    public class PrintSettings
    {

        #region Properties

        /// <summary>
        /// Print Settings ID, used by PrintSetting table as primary key
        /// </summary>
        [SQLite.Column("pst_id"), SQLite.NotNull, SQLite.PrimaryKey, SQLite.AutoIncrement]
        public int Id { get; set; }

        /// <summary>
        /// Printer ID, used by PrintSetting table
        /// </summary>
        [SQLite.Column("prn_id"), SQLite.NotNull]
        public int PrinterId { get; set; }

        /// <summary>
        /// Color mode setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_color_mode"), SQLite.NotNull]
        public int ColorMode { get; set; }

        /// <summary>
        /// Orientation setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_orientation"), SQLite.NotNull]
        public int Orientation { get; set; }

        /// <summary>
        /// Copies setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_copies"), SQLite.NotNull]
        public int Copies { get; set; }

        /// <summary>
        /// Duplex setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_duplex"), SQLite.NotNull]
        public int Duplex { get; set; }

        /// <summary>
        /// Paper size setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_paper_size"), SQLite.NotNull]
        public int PaperSize { get; set; }

        /// <summary>
        /// Scale to fit setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_scale_to_fit"), SQLite.NotNull]
        public bool ScaleToFit { get; set; }

        /// <summary>
        /// Paper type setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_paper_type"), SQLite.NotNull]
        public int PaperType { get; set; }

        /// <summary>
        /// Input tray setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_input_tray"), SQLite.NotNull]
        public int InputTray { get; set; }

        /// <summary>
        /// Imposition setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_imposition"), SQLite.NotNull]
        public int Imposition { get; set; }

        /// <summary>
        /// Imposition order setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_imposition_order"), SQLite.NotNull]
        public int ImpositionOrder { get; set; }


        /// <summary>
        /// Color mode setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_sort"), SQLite.NotNull]
        public int Sort { get; set; }

        /// <summary>
        /// Booklet setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_booklet"), SQLite.NotNull]
        public bool Booklet { get; set; }

        /// <summary>
        /// Booklet finishing setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_booklet_finishing"), SQLite.NotNull]
        public int BookletFinishing { get; set; }

        /// <summary>
        /// Booklet layout setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_booklet_layout"), SQLite.NotNull]
        public int BookletLayout { get; set; }

        /// <summary>
        /// Finishing side setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_finishing_side"), SQLite.NotNull]
        public int FinishingSide { get; set; }

        /// <summary>
        /// Staple setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_staple"), SQLite.NotNull]
        public int Staple { get; set; }

        /// <summary>
        /// Punch setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_punch"), SQLite.NotNull]
        public int Punch { get; set; }

        /// <summary>
        /// Output tray setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_output_tray"), SQLite.NotNull]
        public int OutputTray { get; set; }

        #endregion Properties

        /// <summary>
        /// PrintSettings default class constructor
        /// </summary>
        public PrintSettings()
        {
            Id = -1;
            PrinterId = -1;
            ColorMode = -1;
            Orientation = -1;
            Copies = -1;
            Duplex = -1;
            PaperSize = -1;
            ScaleToFit = false;
            PaperType = -1;
            InputTray = -1;
            Imposition = -1;
            ImpositionOrder = -1;
            Sort = -1;
            Booklet = false;
            BookletFinishing = -1;
            BookletLayout = -1;
            FinishingSide = -1;
            Staple = -1;
            Punch = -1;
            OutputTray = -1;
        }

    }
}
