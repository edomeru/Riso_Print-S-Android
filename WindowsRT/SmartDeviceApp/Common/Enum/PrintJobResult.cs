//
//  PrintJobResult.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/28.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

namespace SmartDeviceApp.Common.Enum
{
    // Note: Should be mapped with DirectPrint class
    /// <summary>
    /// Enumeration of results when printing a job
    /// </summary>
    public enum PrintJobResult
    {
        Success,
        Error,
        Cancelled,
        NoNetwork
    }
}
