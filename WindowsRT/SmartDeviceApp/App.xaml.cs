﻿//
//  App.xaml.cs
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
using Windows.ApplicationModel;
using Windows.ApplicationModel.Activation;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Navigation;
using GalaSoft.MvvmLight.Threading;
using SmartDeviceApp.Views;
using SmartDeviceApp.Controllers;
using DirectPrint;
using SmartDeviceApp.ViewModels;
using System.Threading.Tasks;

namespace SmartDeviceApp
{
    /// <summary>
    /// Provides application-specific behavior to supplement the default Application class.
    /// </summary>
    sealed partial class App : Application
    {
        /// <summary>
        /// Initializes the singleton application object.  This is the first line of authored code
        /// executed, and as such is the logical equivalent of main() or WinMain().
        /// </summary>
        public App()
        {
            this.UnhandledException += this.Application_UnhandledException;

            InitializeComponent();            
            Suspending += OnSuspending;

            MainController.Initialize();
        }

        /// <summary>
        /// Invoked when the application is launched normally by the end user.  Other entry points
        /// will be used such as when the application is launched to open a specific file.
        /// </summary>
        /// <param name="e">Details about the launch request and process.</param>
        protected override async void OnLaunched(LaunchActivatedEventArgs e)
        {

#if DEBUG
            if (System.Diagnostics.Debugger.IsAttached)
            {
                this.DebugSettings.EnableFrameRateCounter = true;
            }
#endif

            Frame rootFrame = Window.Current.Content as Frame;

            // Do not repeat app initialization when the Window already has content,
            // just ensure that the window is active
            if (rootFrame == null)
            {
                // Create a Frame to act as the navigation context and navigate to the first page
                rootFrame = new Frame();
                // Set the default language
                rootFrame.Language = Windows.Globalization.ApplicationLanguages.Languages[0];

                rootFrame.NavigationFailed += OnNavigationFailed;

                if (e.PreviousExecutionState == ApplicationExecutionState.Terminated)
                {
                    //TODO: Load state from previously suspended application
                }

                // Place the frame in the current Window
                Window.Current.Content = rootFrame;
                DispatcherHelper.Initialize();
            }

            if (rootFrame.Content == null)
            {
                // When the navigation stack isn't restored navigate to the first page,
                // configuring the new page by passing required information as a navigation
                // parameter
                rootFrame.Navigate(typeof(HomePage), e.Arguments);
            }
            // Ensure the current window is active
            Window.Current.Activate();

            SettingController.ShowLicenseAgreement();

            //temp
            //DirectPrint.DirectPrint p = new DirectPrint.DirectPrint();
        }

        private async Task openFile(FileActivatedEventArgs e,bool frameIsNull)
        {
            if (frameIsNull)
            {
                await Task.Delay(500);
            }
            await MainController.FileActivationHandler(e.Files[0] as Windows.Storage.StorageFile);
        }
        /// <summary>
        /// Invoked when the application is launched by the end user thru Open With.
        /// </summary>
        /// <param name="args">Details about the launch request and process.</param>
        protected override async void OnFileActivated(FileActivatedEventArgs e)
        {
            // Disable open document button in Home Screen
            (new ViewModelLocator().HomeViewModel).EnabledOpenDocumentCommand = false;

            Frame rootFrame = Window.Current.Content as Frame;
            var frameIsNull = rootFrame == null;
            // Do not repeat app initialization when the Window already has content,
            // just ensure that the window is active
            if (frameIsNull)
            {
                // Create a Frame to act as the navigation context and navigate to the first page
                rootFrame = new Frame();
                // Set the default language
                rootFrame.Language = Windows.Globalization.ApplicationLanguages.Languages[0];

                rootFrame.NavigationFailed += OnNavigationFailed;

                if (e.PreviousExecutionState == ApplicationExecutionState.Terminated)
                {
                    //TODO: Load state from previously suspended application
                }

                // Place the frame in the current Window
                Window.Current.Content = rootFrame;
                DispatcherHelper.Initialize();
            }
            
            if (rootFrame.Content == null)
            {
                // When the navigation stack isn't restored navigate to the first page,
                // configuring the new page by passing required information as a navigation
                // parameter
                rootFrame.Navigate(typeof(HomePage), e.Files);
            }
            // Ensure the current window is active
            Window.Current.Activate();

            SettingController.ShowLicenseAgreement();

            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
            Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                if (frameIsNull)
                {
                    await Task.Delay(400);
                }
                await MainController.FileActivationHandler(e.Files[0] as Windows.Storage.StorageFile);      
            });
        
             
        }

        /// <summary>
        /// Invoked when Navigation to a certain page fails
        /// </summary>
        /// <param name="sender">The Frame which failed navigation</param>
        /// <param name="e">Details about the navigation failure</param>
        void OnNavigationFailed(object sender, NavigationFailedEventArgs e)
        {
            throw new Exception("Failed to load Page " + e.SourcePageType.FullName);
        }

        /// <summary>
        /// Invoked when application execution is being suspended.  Application state is saved
        /// without knowing whether the application will be terminated or resumed with the contents
        /// of memory still intact.
        /// </summary>
        /// <param name="sender">The source of the suspend request.</param>
        /// <param name="e">Details about the suspend request.</param>
        private void OnSuspending(object sender, SuspendingEventArgs e)
        {
            var deferral = e.SuspendingOperation.GetDeferral();
            //TODO: Save application state and stop any background activity
            deferral.Complete();
            MainController.Cleanup();
        }

        private void Application_UnhandledException(object sender,
            UnhandledExceptionEventArgs e)
        {
            // Handle xaml exceptions manually, do not exit app unexpectedly
            e.Handled = true;
        }

    }
}
