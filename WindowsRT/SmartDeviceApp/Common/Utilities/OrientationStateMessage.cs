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
        /// <summary>
        /// Gets or sets the page orientation
        /// </summary>
        public PageOrientations Orientation
        {
            get;
            private set;
        }

        /// <summary>
        /// Constructor for the Orientation State Message. 
        /// </summary>
        /// <param name="orientation">Sets the orientation with this parameter</param>
        public OrientationStateMessage(PageOrientations orientation)
        {
            Orientation = orientation;
        }
    }
}