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
using SmartDeviceApp.Models;
using System.Text;
using Windows.Storage;

namespace SmartDeviceApp.Controllers
{
    public class DirectPrintController
    {

        private const string FORMAT_PRINT_SETTING_KVO = "{0}={1}\n";

        /// <summary>
        /// Prepare print job for printing
        /// </summary>
        /// <param name="name">job name</param>
        /// <param name="file">actual PDF file</param>
        /// <param name="printer">printer</param>
        /// <param name="printSettings">print settings</param>
        public void SendPrintJob(string name, StorageFile file, Printer printer,
            PrintSettings printSettings)
        {
            DirectPrint.directprint_job job = new DirectPrint.directprint_job();
            job.job_name = name;
            job.file = file;
            job.print_settings = CreateStringFromPrintSettings(printSettings);
            job.ip_address = printer.IpAddress;
            job.progress = 0;
            job.cancel_print = 0;

            job.callback = null;// TODO: add callback***


            // Process print job
            DirectPrint.DirectPrint directPrint = new DirectPrint.DirectPrint();
            directPrint.startLPRPrint(job);
            // TODO: Error handling (result of directPrint)
            
        }

        /// <summary>
        /// Converts print settings into a string
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
