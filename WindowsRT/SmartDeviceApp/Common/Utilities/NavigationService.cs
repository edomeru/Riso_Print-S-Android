//
//  NavigationService.cs
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
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.Common.Utilities
{
    public class NavigationService : INavigationService
    {
        /// <summary>
        /// Determines if a page can navigate back.
        /// </summary>
        public virtual bool CanGoBack
        {
            get
            {
                var frame = ((Frame)Window.Current.Content);
                return frame.CanGoBack;
            }
        }

        /// <summary>
        /// Determines the current page type
        /// </summary>
        public Type CurrentPageType
        {
            get
            {
                var frame = ((Frame)Window.Current.Content);
                return frame.CurrentSourcePageType;
            }
        }

        /// <summary>
        /// Navigates the page back.
        /// </summary>
        public virtual void GoBack()
        {
            var frame = ((Frame)Window.Current.Content);

            if (frame.CanGoBack)
            {
                frame.GoBack();
            }
        }

        /// <summary>
        /// Navigates the page forward
        /// </summary>
        public virtual void GoForward()
        {
            var frame = ((Frame)Window.Current.Content);

            if (frame.CanGoForward)
            {
                frame.GoForward();
            }
        }

        /// <summary>
        /// Navigates the page to Home page
        /// </summary>
        public virtual void GoHome()
        {
            var frame = ((Frame)Window.Current.Content);

            while (frame.CanGoBack)
            {
                frame.GoBack();
            }
        }

        /// <summary>
        /// Navigates the page to a certain page
        /// </summary>
        /// <param name="sourcePageType">type of source page</param>
        public virtual void Navigate(Type sourcePageType)
        {
            ((Frame)Window.Current.Content).Navigate(sourcePageType);
        }

        /// <summary>
        /// Navigates the page to a certain page
        /// </summary>
        /// <param name="sourcePageType">type of source page</param>
        /// <param name="parameter">parameter to be interpreted by the target of the navigation</param>
        public virtual void Navigate(Type sourcePageType, object parameter)
        {
            ((Frame)Window.Current.Content).Navigate(sourcePageType, parameter);
        }
    }
}