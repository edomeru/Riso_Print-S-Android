//
//  DialogService.cs
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
using System.Threading.Tasks;
using Windows.ApplicationModel.Resources;
using Windows.UI.Popups;

namespace SmartDeviceApp.Common.Utilities
{
    public class DialogService : IDialogService
    {
        private static DialogService _instance;
        private static ResourceLoader _resourceLoader;

        public static DialogService Instance
        {
            get 
            {
                if (_instance == null)
                {
                    _instance = new DialogService();
                    _resourceLoader = new ResourceLoader();
                }
                return _instance;
            }
        }

        public async Task ShowError(string message, string title, string buttonText, Action afterHideCallback)
        {
            message = _resourceLoader.GetString(message);
            title = _resourceLoader.GetString(title);
            buttonText = _resourceLoader.GetString(buttonText);

            var dialog = new MessageDialog(message, title ?? string.Empty);

            dialog.Commands.Add(
                new UICommand(
                    buttonText,
                    c =>
                    {
                        if (afterHideCallback != null)
                        {
                            afterHideCallback();
                        }
                    }));

            dialog.CancelCommandIndex = 0;
            await dialog.ShowAsync();
        }

        public async Task ShowError(Exception error, string title, string buttonText, Action afterHideCallback)
        {
            var message = _resourceLoader.GetString(error.Message);
            title = _resourceLoader.GetString(title);
            buttonText = _resourceLoader.GetString(buttonText);

            await ShowError(message, title ?? string.Empty, buttonText, afterHideCallback);
        }

        public async Task ShowMessage(string message, string title)
        {
            message = _resourceLoader.GetString(message);
            title = _resourceLoader.GetString(title);

            var dialog = new MessageDialog(message, title ?? string.Empty);
            await dialog.ShowAsync();
        }

        public async Task ShowMessage(string message, string title, string buttonText, Action afterHideCallback)
        {
            message = _resourceLoader.GetString(message);
            title = _resourceLoader.GetString(title);
            buttonText = _resourceLoader.GetString(buttonText);

            var dialog = new MessageDialog(message, title ?? string.Empty);
            dialog.Commands.Add(
                new UICommand(
                    buttonText,
                    c =>
                    {
                        if (afterHideCallback != null)
                        {
                            afterHideCallback();
                        }
                    }));
            dialog.CancelCommandIndex = 0;
            await dialog.ShowAsync();
        }

        public async Task ShowMessage(
            string message,
            string title,
            string buttonConfirmText,
            string buttonCancelText,
            Action<bool> afterHideCallback)
        {
            message = _resourceLoader.GetString(message);
            title = _resourceLoader.GetString(title);
            buttonConfirmText = _resourceLoader.GetString(buttonConfirmText);
            buttonCancelText = _resourceLoader.GetString(buttonCancelText);

            var dialog = new MessageDialog(message, title ?? string.Empty);
            dialog.Commands.Add(new UICommand(buttonConfirmText, c => afterHideCallback(true)));
            dialog.Commands.Add(new UICommand(buttonCancelText, c => afterHideCallback(false)));
            dialog.CancelCommandIndex = 1;
            await dialog.ShowAsync();
        }

        public async Task ShowMessageBox(string message, string title)
        {
            message = _resourceLoader.GetString(message);
            title = _resourceLoader.GetString(title);

            var dialog = new MessageDialog(message, title ?? string.Empty);
            await dialog.ShowAsync();
        }

        public async Task ShowCustomMessageBox(string message, string title,  string buttonText, Action afterHideCallback)
        {
            var dialog = new MessageDialog(message, title ?? string.Empty);
            dialog.Commands.Add(
                new UICommand(
                    buttonText,
                    c =>
                    {
                        if (afterHideCallback != null)
                        {
                            afterHideCallback();
                        }
                    }));
            dialog.CancelCommandIndex = 0;
            await dialog.ShowAsync();
        }
    }
}