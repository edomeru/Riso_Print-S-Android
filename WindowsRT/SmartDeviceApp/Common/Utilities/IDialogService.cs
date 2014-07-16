//
//  IDialogService.cs 
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

namespace SmartDeviceApp.Common.Utilities
{
    /// <summary>
    /// Interface for the DialogService class
    /// </summary>
    public interface IDialogService
    {
        Task ShowError(
            string message,
            string title,
            string buttonText,
            Action afterHideCallback);

        Task ShowError(
            Exception error,
            string title,
            string buttonText,
            Action afterHideCallback);

        Task ShowMessage(
            string message,
            string title);

        Task ShowMessage(
            string message,
            string title,
            string buttonText,
            Action afterHideCallback);

        Task ShowMessage(
            string message,
            string title,
            string buttonConfirmText,
            string buttonCancelText,
            Action<bool> afterHideCallback);

        Task ShowMessageBox(
            string message,
            string title);
    }
}