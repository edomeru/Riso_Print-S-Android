//
//  PageBase.cs
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
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Navigation;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Common.Base
{
    public abstract class PageBase : Page
    {
        private const string StateKey = "State";

        private readonly NavigationHelper _navigationHelper;

        /// <summary>
        /// Aids in navigation between pages.
        /// </summary>
        public NavigationHelper NavigationHelper
        {
            get
            {
                return _navigationHelper;
            }
        }

        protected PageBase()
        {
            _navigationHelper = new NavigationHelper(this);
            _navigationHelper.LoadState += NavigationHelperLoadState;
            _navigationHelper.SaveState += NavigationHelperSaveState;
        }

        protected virtual void LoadState(object state)
        {
        }

        protected void NavigationHelperLoadState(object sender, LoadStateEventArgs e)
        {
            if (e.PageState != null
                && e.PageState.ContainsKey(StateKey))
            {
                LoadState(e.PageState[StateKey]);
            }
        }

        protected void NavigationHelperSaveState(object sender, SaveStateEventArgs e)
        {
            if (e.PageState == null)
            {
                throw new InvalidOperationException("PageState is null");
            }

            if (e.PageState.ContainsKey(StateKey))
            {
                e.PageState.Remove(StateKey);
            }

            var state = SaveState();

            if (state != null)
            {
                e.PageState.Add(StateKey, state);
            }
        }

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            _navigationHelper.OnNavigatedFrom(e);
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            _navigationHelper.OnNavigatedTo(e);
        }

        protected virtual object SaveState()
        {
            return null;
        }
    }
}