﻿//
//  DirectPrintController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/24.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Models;
using System;
using System.Text;
using Windows.Foundation;
using Windows.Storage;

namespace SmartDeviceApp.Controllers
{
    public class DirectPrintController
    {

        public delegate void UpdatePrintJobProgress(float progress);
        public event UpdatePrintJobProgress UpdatePrintJobProgressEventHandler;

        public delegate void SetPrintJobResult(string name, DateTime date, int result);
        public event SetPrintJobResult SetPrintJobResultEventHandler;

        private DirectPrint.DirectPrint _directPrint;
        private DirectPrint.directprint_job _printJob;
        private DateTime _startTime;

        private const string FORMAT_PRINT_SETTING_KVO = "{0}={1}\n";

        public DirectPrintController(string name, StorageFile file, string ipAddress,
            PrintSettings printSettings, UpdatePrintJobProgress progressEvent,
            SetPrintJobResult resultEvent)
        {
            UpdatePrintJobProgressEventHandler = progressEvent;
            SetPrintJobResultEventHandler = resultEvent;

            _printJob = new DirectPrint.directprint_job();

            _printJob.job_name = name;
            //_printJob.filename = name; // TODO: (confirm) To be deleted
            _printJob.file = file;
            _printJob.print_settings = CreateStringFromPrintSettings(printSettings);
            _printJob.ip_address = ipAddress;
            _printJob.callback = new DirectPrint.directprint_callback(ReceiveResult);
            _printJob.progress_callback = new DirectPrint.progress_callback(UpdateProgress);
            _printJob.progress = 0;
            _printJob.cancel_print = 0;
        }

        /// <summary>
        /// Unregisters events
        /// </summary>
        public void UnsubscribeEvents()
        {
            UpdatePrintJobProgressEventHandler = null;
            SetPrintJobResultEventHandler = null;
        }

        /// <summary>
        /// Send print job for printing
        /// </summary>
        public void SendPrintJob()
        {
            _startTime = DateTime.Now;
            _directPrint = new DirectPrint.DirectPrint();
            _directPrint.startLPRPrint(_printJob);            
        }

        /// <summary>
        /// Cancel print job processing
        /// </summary>
        public void CancelPrintJob()
        {
            _directPrint.cancelPrint();
        }

        /// <summary>
        /// Event handler for progress updates
        /// </summary>
        /// <param name="progress">value</param>
        public void UpdateProgress(float progress)
        {
            System.Diagnostics.Debug.WriteLine("[DirectPrintController] UpdateProgress:" + progress);
            if (UpdatePrintJobProgressEventHandler != null)
            {
                UpdatePrintJobProgressEventHandler(progress);
            }
        }

        /// <summary>
        /// Event handler for print job result
        /// </summary>
        /// <param name="result">result value</param>
        public void ReceiveResult(int result)
        {
            System.Diagnostics.Debug.WriteLine("[DirectPrintController] ReceiveResult:" + result);
            if (SetPrintJobResultEventHandler != null)
            {
                SetPrintJobResultEventHandler(_printJob.job_name, _startTime, result);
            }
        }

        /// <summary>
        /// Converts print settings into a string understood by DirectPrintSettings class
        /// </summary>
        /// <param name="printSettings">print settings</param>
        /// <returns>converted print settings string</returns>
        private string CreateStringFromPrintSettings(PrintSettings printSettings)
        {
            StringBuilder builder = new StringBuilder();

            // Color Mode
            if (printSettings.ColorMode >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_COLOR_MODE,
                                             printSettings.ColorMode));
            }

            // Orientation
            if (printSettings.Orientation >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_ORIENTATION,
                                             printSettings.Orientation));
            }

            // Copies
            if (printSettings.Copies >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_COPIES,
                                             printSettings.Copies));
            }

            // Duplex
            if (printSettings.Duplex >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_DUPLEX,
                                             printSettings.Duplex));
            }

            // Paper Size
            if (printSettings.PaperSize >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                                             printSettings.PaperSize));
            }

            // Scale to Fit
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT,
                                             (printSettings.ScaleToFit ? 1 : 0)));
            }

            // Paper Type
            if (printSettings.PaperType >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_PAPER_TYPE,
                                             printSettings.PaperType));
            }

            // Input Tray
            if (printSettings.InputTray >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_INPUT_TRAY,
                                             printSettings.InputTray));
            }

            // Imposition
            if (printSettings.Imposition >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_IMPOSITION,
                                             printSettings.Imposition));
            }

            // Imposition Order
            if (printSettings.ImpositionOrder >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER,
                                             printSettings.ImpositionOrder));
            }

            // Sort
            if (printSettings.Sort >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_SORT,
                                             printSettings.Sort));
            }

            // Booklet
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_BOOKLET,
                                             (printSettings.Booklet ? 1 : 0)));
            }

            // Booklet Finishing
            if (printSettings.BookletFinishing >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING,
                                             printSettings.BookletFinishing));
            }

            // Booklet Layout
            if (printSettings.BookletLayout >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT,
                                             printSettings.BookletLayout));
            }

            // Finishing Side
            if (printSettings.FinishingSide >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_FINISHING_SIDE ,
                                             printSettings.FinishingSide));
            }

            // Staple
            if (printSettings.Staple >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_STAPLE,
                                             printSettings.Staple));
            }

            // Punch
            if (printSettings.Punch >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_PUNCH,
                                             printSettings.Punch));
            }

            // Output Tray
            if (printSettings.OutputTray >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY,
                                             printSettings.OutputTray));
            }

            return builder.ToString();
        }

    }
}