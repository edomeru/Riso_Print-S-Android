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
    }

    public class PrintSetting : ObservableObject
    {
        private object _value;
        private PrintSettingOption _selectedOption;

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
        public bool IsEnabled { get; set; }
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
        /// Print setting ID, used by PrintSetting table as primary key
        /// </summary>
        [SQLite.Column("pst_id"), SQLite.PrimaryKey]
        public int Id { get; set; }

        /// <summary>
        /// Printer ID, used by PrintSetting table as primary key and is indexed
        /// </summary>
        [SQLite.Column("prn_id"), SQLite.Indexed(Name = "PrintSetting_FKIndex1")]
        public int PrinterId { get; set; }

        /// <summary>
        /// Color mode setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_color_mode")]
        public int ColorMode { get; set; }

        /// <summary>
        /// Orientation setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_orientation")]
        public int Orientation { get; set; }

        /// <summary>
        /// Copies setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_copies")]
        public int Copies { get; set; }

        /// <summary>
        /// Duplex setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_duplex")]
        public int Duplex { get; set; }

        /// <summary>
        /// Paper size setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_paper_size")]
        public int PaperSize { get; set; }

        /// <summary>
        /// Scale to fit setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_scale_to_fit")]
        public bool ScaleToFit { get; set; }

        /// <summary>
        /// Paper type setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_paper_type")]
        public int PaperType { get; set; }

        /// <summary>
        /// Input tray setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_input_tray")]
        public int InputTray { get; set; }

        /// <summary>
        /// Imposition setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_imposition")]
        public int Imposition { get; set; }

        /// <summary>
        /// Imposition order setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_imposition_order")]
        public int ImpositionOrder { get; set; }


        /// <summary>
        /// Color mode setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_sort")]
        public int Sort { get; set; }

        /// <summary>
        /// Booklet setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_booklet")]
        public bool Booklet { get; set; }

        /// <summary>
        /// Booklet finishing setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_booklet_finishing")]
        public int BookletFinishing { get; set; }

        /// <summary>
        /// Booklet layout setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_booklet_layout")]
        public int BookletLayout { get; set; }

        /// <summary>
        /// Finishing side setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_finishing_side")]
        public int FinishingSide { get; set; }

        /// <summary>
        /// Staple setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_staple")]
        public int Staple { get; set; }

        /// <summary>
        /// Punch setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_punch")]
        public int Punch { get; set; }

        /// <summary>
        /// Output tray setting, used by PrintSetting table
        /// </summary>
        [SQLite.Column("pst_output_tray")]
        public int OutputTray { get; set; }

        #endregion Properties

        /// <summary>
        /// PrintSettings default class constructor
        /// </summary>
        public PrintSettings()
        {
            PrinterId = -1;
            Id = -1;
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
        /// <summary>
        /// PrintSettings class constructor
        /// </summary>
        /// <param name="printerId">printer ID</param>
        /// <param name="id">print setting ID</param>
        /// <param name="colorMode">color mode</param>
        /// <param name="orientation">orientation</param>
        /// <param name="copies">copies</param>
        /// <param name="duplex">duplex</param>
        /// <param name="paperSize">paper size</param>
        /// <param name="scaleToFit">scale to fit</param>
        /// <param name="paperType">paper type</param>
        /// <param name="inputTray">input tray</param>
        /// <param name="imposition">imposition</param>
        /// <param name="impositionOrder">imposition order</param>
        /// <param name="sort">sort</param>
        /// <param name="booklet">booklet</param>
        /// <param name="bookletFinishing">bookletFinishing</param>
        /// <param name="bookletLayout">booklet layout</param>
        /// <param name="finishingSide">finishing size</param>
        /// <param name="staple">staple</param>
        /// <param name="punch">punch</param>
        /// <param name="outputTray">output tray</param>
        public PrintSettings(int printerId, int id, int colorMode, int orientation, int copies,
            int duplex, int paperSize, bool scaleToFit, int paperType, int inputTray,
            int imposition, int impositionOrder, int sort, bool booklet, int bookletFinishing,
            int bookletLayout, int finishingSide, int staple, int punch, int outputTray)
        {
            PrinterId = printerId;
            Id = id;
            ColorMode = colorMode;
            Orientation = orientation;
            Copies = copies;
            Duplex = duplex;
            PaperSize = paperSize;
            ScaleToFit = scaleToFit;
            PaperType = paperType;
            InputTray = inputTray;
            Imposition = imposition;
            ImpositionOrder = impositionOrder;
            Sort = sort;
            Booklet = booklet;
            BookletFinishing = bookletFinishing;
            BookletLayout = bookletLayout;
            FinishingSide = finishingSide;
            Staple = staple;
            Punch = punch;
            OutputTray = outputTray;
        }

    }
}
