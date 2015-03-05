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

        /// <summary>
        /// Singleton instance
        /// </summary>
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

        /// <summary>
        /// Displays the error message
        /// </summary>
        /// <param name="message">Resource id of the message to be extracted from the resources.</param>
        /// <param name="title">Resource id of the title of the dialog</param>
        /// <param name="buttonText">Resource id of the text of the button</param>
        /// <param name="afterHideCallback">Function call back after dialog is dismissed</param>
        /// <returns>Task</returns>
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
            try
            {
                await dialog.ShowAsync();
            }
            catch (Exception)
            {
                // Do not show message dialog if error occurs
                // E.g. UnauthorizedAccessException: Another dialog is currently shown
                // May occur if sender control is tapped repeatedly
            }
        }

        /// <summary>
        /// Displays the error message
        /// </summary>
        /// <param name="error">Resource id of the message to be extracted from the resources.</param>
        /// <param name="title">Resource id of the title of the dialog</param>
        /// <param name="buttonText">Resource id of the text of the button</param>
        /// <param name="afterHideCallback">Function call back after dialog is dismissed</param>
        /// <returns>Task</returns>
        public async Task ShowError(Exception error, string title, string buttonText, Action afterHideCallback)
        {
            var message = _resourceLoader.GetString(error.Message);
            title = _resourceLoader.GetString(title);
            buttonText = _resourceLoader.GetString(buttonText);

            await ShowError(message, title ?? string.Empty, buttonText, afterHideCallback);
        }

        /// <summary>
        /// Displays a dialog with the message.
        /// </summary>
        /// <param name="message">Resource id of the message to be extracted from the resources.</param>
        /// <param name="title">Resource id of the title of the dialog</param>
        /// <returns>Task</returns>
        public async Task ShowMessage(string message, string title)
        {
            message = _resourceLoader.GetString(message);
            title = _resourceLoader.GetString(title);

            var dialog = new MessageDialog(message, title ?? string.Empty);
            await dialog.ShowAsync();
        }

        /// <summary>
        /// Displays a dialog with the message
        /// </summary>
        /// <param name="message">Resource id of the message to be extracted from the resources.</param>
        /// <param name="title">Resource id of the title of the dialog</param>
        /// <param name="buttonText">Resource id of the text of the button</param>
        /// <param name="afterHideCallback">Function call after dialog is dismissed</param>
        /// <returns>Task</returns>
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

        /// <summary>
        /// Displays a dialog with Confirm and Cancel button
        /// </summary>
        /// <param name="message">Resource id of the message to be extracted from the resources.</param>
        /// <param name="title">Resource id of the title of the dialog</param>
        /// <param name="buttonConfirmText">Resource id of the text of the confirm button</param>
        /// <param name="buttonCancelText">Resource id of the text of the cancel button</param>
        /// <param name="afterHideCallback">Function called after dialog is dismissed</param>
        /// <returns>Task</returns>
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

        /// <summary>
        /// Displays a dialog.
        /// </summary>
        /// <param name="message">Resource id of the message to be extracted from the resources.</param>
        /// <param name="title">Resource id of the title of the dialog</param>
        /// <returns>Task</returns>
        public async Task ShowMessageBox(string message, string title)
        {
            message = _resourceLoader.GetString(message);
            title = _resourceLoader.GetString(title);

            var dialog = new MessageDialog(message, title ?? string.Empty);
            await dialog.ShowAsync();
        }

        /// <summary>
        /// Displays a dialog with custom message.
        /// </summary>
        /// <param name="message">Message to be displayed.</param>
        /// <param name="title">Title of the dialog</param>
        /// <param name="buttonText">Resource id of the text of the button</param>
        /// <param name="afterHideCallback">Function called after dialog is dismissed</param>
        /// <returns></returns>
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