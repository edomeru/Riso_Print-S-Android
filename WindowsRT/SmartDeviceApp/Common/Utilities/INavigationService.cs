//
//  INavigationService.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;

namespace SmartDeviceApp.Common.Utilities
{
    /// <summary>
    /// Interface for the Navigation Service class
    /// </summary>
    public interface INavigationService
    {
        bool CanGoBack
        {
            get;
        }

        Type CurrentPageType
        {
            get;
        }

        void GoBack();
        void GoForward();
        void GoHome();
        void Navigate(Type sourcePageType);
        void Navigate(Type sourcePageType, object parameter);
    }
}