//
//  DefaultsUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Models;
using System.Collections.Generic;
using System.Linq;

namespace SmartDeviceApp.Common.Utilities
{
    public static class DefaultsUtility
    {

        #region Print Settings

        /// <summary>
        /// Gets the default print settings from list.
        /// </summary>
        /// <param name="printSettingList">print settings list</param>
        /// <returns>PagePrintSetting object</returns>
        public static PrintSettings GetDefaultPrintSettings(PrintSettingList printSettingList)
        {
            PrintSettings defaultPrintSettings = new PrintSettings();

            if (printSettingList != null)
            {
                List<PrintSetting> tempList =
                    printSettingList.SelectMany(group => group.PrintSettings)
                                    .Where(ps => true).ToList<PrintSetting>();

                foreach(PrintSetting printSetting in tempList)
                {
                    object defaultValue = printSetting.Default;
                    
                    switch (printSetting.Name)
                    {
                        case PrintSettingConstant.NAME_VALUE_COLOR_MODE:
                            defaultPrintSettings.ColorMode = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_ORIENTATION:
                            defaultPrintSettings.Orientation = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_COPIES:
                            defaultPrintSettings.Copies = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_DUPLEX:
                            defaultPrintSettings.Duplex = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_PAPER_SIZE:
                            defaultPrintSettings.PaperSize = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT:
                            defaultPrintSettings.ScaleToFit = (bool)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_PAPER_TYPE:
                            defaultPrintSettings.PaperType = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_INPUT_TRAY:
                            defaultPrintSettings.InputTray = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_IMPOSITION:
                            defaultPrintSettings.Imposition = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER:
                            defaultPrintSettings.ImpositionOrder = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_SORT:
                            defaultPrintSettings.Sort = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_BOOKLET:
                            defaultPrintSettings.Booklet = (bool)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING:
                            defaultPrintSettings.BookletFinishing = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT:
                            defaultPrintSettings.BookletLayout = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_FINISHING_SIDE:
                            defaultPrintSettings.FinishingSide = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_STAPLE:
                            defaultPrintSettings.Staple = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_PUNCH:
                            defaultPrintSettings.Punch = (int)defaultValue;
                            break;
                        case PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY:
                            defaultPrintSettings.OutputTray = (int)defaultValue;
                            break;
                    }
                }

            }

            return defaultPrintSettings;
        }

        #endregion Print Settings

    }
}
