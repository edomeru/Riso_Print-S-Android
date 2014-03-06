//
//  OrientationStateMessage.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

namespace SmartDeviceApp.Common.Utilities
{
    public class OrientationStateMessage
    {
        public PageOrientations Orientation
        {
            get;
            private set;
        }

        public OrientationStateMessage(PageOrientations orientation)
        {
            Orientation = orientation;
        }
    }
}