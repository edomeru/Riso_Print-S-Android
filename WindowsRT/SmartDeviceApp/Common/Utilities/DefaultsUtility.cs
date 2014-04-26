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
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Windows.Storage;

namespace SmartDeviceApp.Common.Utilities
{
    public static class DefaultsUtility
    {

        public const string KEY_COLUMN_NAME_PRN_PORT_SETTING = "prn_port_setting";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_LPR = "prn_enabled_lpr";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_RAW = "prn_enabled_raw";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_BOOKLET = "prn_enabled_booklet";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_STAPLER = "prn_enabled_stapler";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_PUNCH4 = "prn_enabled_punch4";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_TRAY_FACEDOWN = "prn_enabled_tray_facedown";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_TRAY_AUTOSTACK = "prn_enabled_tray_autostack";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_TRAY_TOP = "prn_enabled_tray_top";
        public const string KEY_COLUMN_NAME_PRN_ENABLED_TRAY_STACK = "prn_enabled_tray_stack";
        public const string KEY_COLUMN_NAME_PST_COLOR_MODE = "pst_color_mode";
        public const string KEY_COLUMN_NAME_PST_ORIENTATION = "pst_orientation";
        public const string KEY_COLUMN_NAME_PST_COPIES = "pst_copies";
        public const string KEY_COLUMN_NAME_PST_DUPLEX = "pst_duplex";
        public const string KEY_COLUMN_NAME_PST_PAPER_SIZE = "pst_paper_size";
        public const string KEY_COLUMN_NAME_PST_SCALE_TO_FIT = "pst_scale_to_fit";
        public const string KEY_COLUMN_NAME_PST_PAPER_TYPE = "pst_paper_type";
        public const string KEY_COLUMN_NAME_PST_INPUT_TRAY = "pst_input_tray";
        public const string KEY_COLUMN_NAME_PST_IMPOSITION = "pst_imposition";
        public const string KEY_COLUMN_NAME_PST_IMPOSITION_ORDER = "pst_imposition_order";
        public const string KEY_COLUMN_NAME_PST_SORT = "pst_sort";
        public const string KEY_COLUMN_NAME_PST_BOOKLET = "pst_booklet";
        public const string KEY_COLUMN_NAME_PST_BOOKLET_FINISH = "pst_booklet_finish";
        public const string KEY_COLUMN_NAME_PST_BOOKLET_LAYOUT = "pst_booklet_layout";
        public const string KEY_COLUMN_NAME_PST_FINISHING_SIDE = "pst_finishing_side";
        public const string KEY_COLUMN_NAME_PST_STAPLE = "pst_staple";
        public const string KEY_COLUMN_NAME_PST_PUNCH = "pst_punch";
        public const string KEY_COLUMN_NAME_PST_OUTPUT_TRAY = "pst_output_tray";

        private const string KEY_DATABASE_DEFAULT = "DEFAULT";

        private static Dictionary<string, string> _sqlScriptDefaults = new Dictionary<string, string>();

        #region Database / SQL Script

        /// <summary>
        /// Parses the SQL script and saves the default value as a key-value pair where column name
        /// is the key.
        /// </summary>
        /// <param name="filePath">source file path</param>
        /// <returns>task</returns>
        public async static Task LoadDefaultsFromSqlScript(string filePath)
        {
            if (!string.IsNullOrEmpty(filePath))
            {
                StorageFile file = await StorageFileUtility.GetFileFromAppResource(filePath);

                var lines = await FileIO.ReadLinesAsync(file);
                foreach(string line in lines)
                {
                    if (line.Contains(KEY_DATABASE_DEFAULT))
                    {
                        string[] tokens = line.Split(new char[] { ' ', ',' },
                            StringSplitOptions.RemoveEmptyEntries);
                        // First token as key, last token as value
                        _sqlScriptDefaults.Add(tokens[0], tokens[tokens.Length - 1]);
                    }
                }
            }
        }

        /// <summary>
        /// Retrieves the default value. Value must come from parsed SQL script.
        /// </summary>
        /// <param name="key"></param>
        /// <returns>default value if found from dictionary</returns>
        public static object GetDefaultValueFromSqlScript(string key, ListValueType type)
        {
            object value = null;
            string strValue = null;
            int intValue = -1;

            if (!string.IsNullOrEmpty(key))
            {
                _sqlScriptDefaults.TryGetValue(key, out strValue);
            }

            switch (type)
            {
                case ListValueType.Boolean:
                    bool boolValue = false;
                    if (int.TryParse(strValue, out intValue))
                    {
                        boolValue = (intValue == 1) ? true : false;
                    }
                    value = boolValue;
                    break;
                case ListValueType.Int:
                    int.TryParse(strValue, out intValue);
                    value = intValue;
                    break;
                case ListValueType.String:
                default:
                    // Do nothing
                    break;
            }

            return value;
        }

        #endregion Database / SQL Script

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
