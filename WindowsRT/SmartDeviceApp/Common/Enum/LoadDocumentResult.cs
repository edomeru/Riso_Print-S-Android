//
//  LoadDocumentResult.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/08.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

namespace SmartDeviceApp.Common.Enum
{
    /// <summary>
    /// Enumeration for the result of loading a document
    /// </summary>
    public enum LoadDocumentResult
    {
        NotStarted,
        Successful,
        UnsupportedPdf,
        ErrorReadPdf
    }
}
