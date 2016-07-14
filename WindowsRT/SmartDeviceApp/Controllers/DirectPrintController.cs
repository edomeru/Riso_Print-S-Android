//
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
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;
using System;
using System.Text;
using Windows.Foundation;
using Windows.Storage;
using Windows.ApplicationModel;

namespace SmartDeviceApp.Controllers
{
    public class DirectPrintController
    {
        /// <summary>
        /// Print job progress delegate
        /// </summary>
        /// <param name="progress"></param>
        public delegate void UpdatePrintJobProgress(float progress);
        public event UpdatePrintJobProgress UpdatePrintJobProgressEventHandler;

        /// <summary>
        /// Print job result delegate
        /// </summary>
        /// <param name="name"></param>
        /// <param name="date"></param>
        /// <param name="result"></param>
        public delegate void SetPrintJobResult(string name, DateTime date, int result);
        public event SetPrintJobResult SetPrintJobResultEventHandler;

        private DirectPrint.DirectPrint _directPrint;
        private DirectPrint.directprint_job _printJob;
        private DateTime _startTime;

        private const string FORMAT_PRINT_SETTING_KVO = "{0}={1}\n";

        /// <summary>
        /// DirectPrintController class controller
        /// </summary>
        /// <param name="name">print job name</param>
        /// <param name="file">PDF file</param>
        /// <param name="ipAddress">IP address</param>
        /// <param name="printerName">printer name</param>
        /// <param name="printSettings">print settings</param>
        /// <param name="progressEvent">progress event callback</param>
        /// <param name="resultEvent">print job event callback</param>
        public DirectPrintController(string name, StorageFile file, string ipAddress, 
            string printerName, PrintSettings printSettings, UpdatePrintJobProgress progressEvent,
            SetPrintJobResult resultEvent)
        {
            UpdatePrintJobProgressEventHandler = progressEvent;
            SetPrintJobResultEventHandler = resultEvent;
            
            _printJob = new DirectPrint.directprint_job();

            _printJob.app_name = Package.Current.DisplayName;
            _printJob.app_version = string.Format("{0}.{1}.{2}.{3}",
                    Package.Current.Id.Version.Major,
                    Package.Current.Id.Version.Minor,
                    Package.Current.Id.Version.Build,
                    Package.Current.Id.Version.Revision);
            _printJob.job_name = name;
            _printJob.series_type = PrinterModelUtility.GetSeriesTypeFromPrinterName(printerName);
            //_printJob.filename = name; // TODO: (confirm) To be deleted
            _printJob.file = file;
            _printJob.print_settings = CreateStringFromPrintSettings(printSettings, printerName);
            _printJob.ip_address = ipAddress;
            _printJob.callback = new DirectPrint.directprint_callback(ReceiveResult);
            _printJob.progress_callback = new DirectPrint.progress_callback(UpdateProgress);
            _printJob.progress = 0;
            _printJob.cancel_print = 0;
            _printJob.username = CreateLoginStringFromPrintSettings(printSettings);
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
            if (_directPrint != null)
            {
                _directPrint.cancelPrint();
            }
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
        /// <param name="printerName">printer name</param>
        /// <returns>converted print settings string</returns>
        private string CreateStringFromPrintSettings(PrintSettings printSettings, String printerName)
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
                if (PrinterModelUtility.isISSeries(printerName))
                {
                    //special handling for IS, switch value
                    int value = printSettings.Sort - 1;
                    builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                             PrintSettingConstant.NAME_VALUE_SORT,
                             Math.Abs(value)));
                }
                else
                {
                    builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                                 PrintSettingConstant.NAME_VALUE_SORT,
                                                 printSettings.Sort));
                }
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
                //special handling for IS, adjust value if index is for 4 holes since 3 and 4 holes have same PJL for IS
                int value = printSettings.Punch;
                if (PrinterModelUtility.isISSeries(printerName) && value > (int)Punch.ThreeHoles)
                {
                    value--;
                }
               builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                       PrintSettingConstant.NAME_VALUE_PUNCH,
                                       value));
            }

            // Output Tray
            if (printSettings.OutputTray >= 0)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY,
                                             printSettings.OutputTray));
            }

            //login id
            builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_LOGIN_ID,
                                             SettingController.Instance.CardId));

            //Secure Print
            builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO, 
                                             PrintSettingConstant.NAME_VALUE_SECURE_PRINT,
                                             printSettings.EnabledSecurePrint ? 1 : 0));

            //Authentication
            if (printSettings.EnabledSecurePrint)
            {
                builder.Append(string.Format(FORMAT_PRINT_SETTING_KVO,
                                             PrintSettingConstant.NAME_VALUE_PIN_CODE,
                                             printSettings.PinCode));
            }

            return builder.ToString();
        }


        /// <summary>
        /// Extracts the Login ID from print settings for use as the LPR owner
        /// </summary>
        /// <param name="printSettings">print settings</param>
        /// <returns>converted login ID</returns>
        private string CreateLoginStringFromPrintSettings(PrintSettings printSettings)
        {
            if (SettingController.Instance.CardId != null)
                return SettingController.Instance.CardId;
            else
                return "";
        }
    }
}
