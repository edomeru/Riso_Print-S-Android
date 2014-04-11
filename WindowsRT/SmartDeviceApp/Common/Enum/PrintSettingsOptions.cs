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
        A3,
        A3W,
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
        TwoUpLeftToRight,
        TwoUpRightToLeft,
        FourUpUpperLeftToRight,
        FourUpUpperRightToLeft,
        FourUpUpperLeftToBottom,
        FourUpUpperRightToBottom
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
        One,
        Two
    }

    public enum Punch
    {
        Off,
        TwoHoles,
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

}
