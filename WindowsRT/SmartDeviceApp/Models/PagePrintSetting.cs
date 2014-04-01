//
//  PagePrintSetting.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Models
{

    public enum ColorMode
    {
        Auto,
        FullColor,
        Mono
    }

    public enum Orientation
    {
        Portrait,
        Landscape
    }

    public enum Duplex
    {
        Off,
        LongEdge,
        ShortEdge
    }

    public enum PaperSize
    {
        A3W,
        A3,
        A4,
        A5,
        A6,
        B4,
        B5,
        Foolscap,
        Tabloid,
        Legal,
        Letter,
        Statement
    }

    public enum PaperType
    {
        Any,
        Plain,
        IJPaper,
        MattCoated,
        HighQuality,
        CardIJ,
        LWPaper
    }

    public enum InputTray
    {
        Auto,
        Standard,
        Tray1,
        Tray2,
        Tray3
    }

    public enum Imposition
    {
        Off,
        TwoUp,
        FourUp
    }

    public enum ImpositionOrder
    {
        TwoUpRightToLeft,
        TwoUpLeftToRight,
        FourUpUpperLeftToBottom,
        FourUpUpperLeftToRight,
        FourUpUpperRightToBottom,
        FourUpUpperRightToLeft
    }

    public enum Sort
    {
        PerPage,
        PerCopy
    }

    public enum BookletFinishing
    {
        PaperFolding,
        FoldAndStaple
    }

    public enum BookletLayout
    {
        LeftToRight,
        RightToLeft,
        TopToBottom
    }

    public enum FinishingSide
    {
        Left,
        Top,
        Right
    }

    public enum Staple
    {
        Off,
        OneUpperLeft,
        OneUpperRight,
        Two,
        One
    }

    public enum Punch
    {
        Off,
        TwoHoles,
        ThreeHoles,
        FourHoles
    }

    public enum OutputTray
    {
        Auto,
        FaceDown,
        FaceUp,
        Top,
        Stacking
    }

    [SQLite.Table("PrintSetting")]
    public class PagePrintSetting
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
        /// PrintSetting default class constructor
        /// </summary>
        public PagePrintSetting()
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
        /// PrintSetting class constructor
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
        public PagePrintSetting(int printerId, int id, int colorMode, int orientation, int copies,
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
