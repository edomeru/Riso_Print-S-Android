//
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

using System.Collections.Generic;
using System.Collections.ObjectModel;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Models
{
    public class PrintSettingOption
    {
        public string Text { get; set; }
        public int Index { get; set; } // Note: Index here refers to index from original options (not with respect to some options removed due to constraints)
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
                _selectedOption = Options.Find(option => option.Index == (int)Value);
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
            get { return _isValueDisplayed; }
            set
            {
                if (_isValueDisplayed != value)
                {
                    _isValueDisplayed = value;
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
        [SQLite.Column("pst_booklet_finish"), SQLite.NotNull]
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

        /// <summary>
        /// Log-in ID for Authentication; used only on a single print session
        /// </summary>
        [SQLite.Ignore]
        public string LoginId { get; set; }

        /// <summary>
        /// PIN Code for Authentication; used only on a single print session
        /// </summary>
        [SQLite.Ignore]
        public string PinCode { get; set; }

        #endregion Properties

        /// <summary>
        /// PrintSettings default class constructor
        /// </summary>
        public PrintSettings()
        {
            Id = -1;
            PrinterId = -1;
            ColorMode = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_COLOR_MODE, ListValueType.Int);
            Orientation = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_ORIENTATION, ListValueType.Int);
            Copies = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_COPIES, ListValueType.Int);
            Duplex = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_DUPLEX, ListValueType.Int);
            PaperSize = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_PAPER_SIZE, ListValueType.Int);
            ScaleToFit = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_SCALE_TO_FIT, ListValueType.Boolean);
            PaperType = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_PAPER_TYPE, ListValueType.Int);
            InputTray = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_INPUT_TRAY, ListValueType.Int);
            Imposition = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_IMPOSITION, ListValueType.Int);
            ImpositionOrder = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_IMPOSITION_ORDER, ListValueType.Int);
            Sort = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_SORT, ListValueType.Int);
            Booklet = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_BOOKLET, ListValueType.Boolean);
            BookletFinishing = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_BOOKLET_FINISH, ListValueType.Int);
            BookletLayout = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_BOOKLET_LAYOUT, ListValueType.Int);
            FinishingSide = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_FINISHING_SIDE, ListValueType.Int);
            Staple = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_STAPLE, ListValueType.Int);
            Punch = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_PUNCH, ListValueType.Int);
            OutputTray = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PST_OUTPUT_TRAY, ListValueType.Int);
            LoginId = null;
            PinCode = null;
        }

    }
}
