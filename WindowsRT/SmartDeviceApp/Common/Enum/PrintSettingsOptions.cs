//
//  PrintSettingsOptions.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

namespace SmartDeviceApp.Common.Enum
{
    /*
     * Items in this class must match the options in Assets/printsettings.xml
     */
    /// <summary>
    /// Enumeration of options for Color setting
    /// </summary>
    public enum ColorMode
    {
        Auto,
        FullColor,
        Mono,
        DualColor
    }

    /// <summary>
    /// Enumeration of options for Orientation setting
    /// </summary>
    public enum Orientation
    {
        Portrait,
        Landscape
    }

    /// <summary>
    /// Enumeration of options for Duplex setting
    /// </summary>
    public enum Duplex
    {
        Off,
        LongEdge,
        ShortEdge
    }

    /// <summary>
    /// Enumeration of options for Paper Size setting
    /// </summary>
    public enum PaperSize
    {
        A3,
        A3W,
        A4,
        A5,
        A6,
        B4,
        B5,
        B6,
        Foolscap,
        Tabloid,
        Legal,
        Letter,
        Statement,
        Legal13,
        EightK,
        SixteenK
    }

    /// <summary>
    /// Enumeration of options for Paper Type setting
    /// </summary>
    public enum PaperType
    {
        Any,
        Plain,
        IJPaper,
        MattCoated,
        HighQuality,
        CardIJ,
        LWPaper,
        RoughPaper
    }

    /// <summary>
    /// Enumeration of options for Input Tray setting
    /// </summary>
    public enum InputTray
    {
        Auto,
        Standard,
        Tray1,
        Tray2,
        Tray3
    }

    /// <summary>
    /// Enumeration of options for Imposition setting
    /// </summary>
    public enum Imposition
    {
        Off,
        TwoUp,
        FourUp
    }

    /// <summary>
    /// Enumeration of options for Imposition Order setting
    /// </summary>
    public enum ImpositionOrder
    {
        TwoUpLeftToRight,
        TwoUpRightToLeft,
        FourUpUpperLeftToRight,
        FourUpUpperRightToLeft,
        FourUpUpperLeftToBottom,
        FourUpUpperRightToBottom
    }

    /// <summary>
    /// Enumeration of options for Sort setting
    /// </summary>
    public enum Sort
    {
        PerPage,
        PerCopy
    }

    /// <summary>
    /// Enumeration of options for Booklet Finishing setting
    /// </summary>
    public enum BookletFinishing
    {
        Off,
        PaperFolding,
        FoldAndStaple
    }

    /// <summary>
    /// Enumeration of options for Booklet Layout setting
    /// </summary>
    public enum BookletLayout
    {
        Forward,
        Reverse
    }

    /// <summary>
    /// Enumeration of options for Finishing Side setting
    /// </summary>
    public enum FinishingSide
    {
        Left,
        Top,
        Right
    }

    /// <summary>
    /// Enumeration of options for Staple setting
    /// </summary>
    public enum Staple
    {
        Off,
        OneUpperLeft,
        OneUpperRight,
        One,
        Two
    }

    /// <summary>
    /// Enumeration of options for Punch setting
    /// </summary>
    public enum Punch
    {
        Off,
        TwoHoles,
        ThreeHoles,
        FourHoles
    }

    /// <summary>
    /// Enumeration of options for Output Tray setting
    /// </summary>
    public enum OutputTray
    {
        Auto,
        FaceDown,
        Top,
        Stacking
    }

}
