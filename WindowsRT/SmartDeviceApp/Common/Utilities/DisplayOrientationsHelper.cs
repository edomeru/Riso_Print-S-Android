//
//  DisplayOrientationHelper.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using Windows.Graphics.Display;

namespace SmartDeviceApp.Common.Utilities
{
    public static class DisplayOrientationsHelper
    {
        public static PageOrientations GetPageOrientation(this DisplayOrientations orientation)
        {
            switch (orientation)
            {
                case DisplayOrientations.LandscapeFlipped:
                    return PageOrientations.LandscapeFlipped;

                case DisplayOrientations.Portrait:
                    return PageOrientations.Portrait;

                case DisplayOrientations.PortraitFlipped:
                    return PageOrientations.PortraitFlipped;

                default:
                    return PageOrientations.Landscape;
            }
        }
    }
}