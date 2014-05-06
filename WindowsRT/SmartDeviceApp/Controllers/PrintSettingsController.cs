//
//  PrintSettingsController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/26.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.ApplicationModel;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System.Collections.Generic;

namespace SmartDeviceApp.Controllers
{
    public sealed class PrintSettingsController
    {
        static readonly PrintSettingsController _instance = new PrintSettingsController();

        SmartDeviceApp.Controllers.PrintPreviewController.UpdatePreviewEventHandler UpdatePreviewEventHandler;

        // Print setting value changed
        public delegate void PrintSettingValueChangedEventHandler(PrintSetting printSetting,
            object value);
        private PrintSettingValueChangedEventHandler _printSettingValueChangedEventHandler;

        // Constants
        private const string FILE_PATH_ASSET_PRINT_SETTINGS_XML = "Assets/printsettings.xml";

        private PrintSettingsViewModel _printSettingsViewModel;
        private string _activeScreen;

        // Maps (Managed values)
        private Dictionary<string, Printer> _printerMap;
        private Dictionary<string, PrintSettings> _printSettingsMap;
        private Dictionary<string, PrintSettingList> _printSettingListMap;
        private Dictionary<string, bool> _enableAutosaveMap;
        private Dictionary<string, int> _pagesPerSheetMap;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static PrintSettingsController() { }

        private PrintSettingsController()
        {
            _printerMap = new Dictionary<string, Printer>();
            _printSettingsMap = new Dictionary<string, PrintSettings>();
            _printSettingListMap = new Dictionary<string, PrintSettingList>();
            _enableAutosaveMap = new Dictionary<string, bool>();
            _pagesPerSheetMap = new Dictionary<string, int>();

            _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;
            _printSettingValueChangedEventHandler = new PrintSettingValueChangedEventHandler(PrintSettingValueChanged);
        }

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static PrintSettingsController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Initialize data.
        /// Retrives the current print settings for the printer and applies filter
        /// based on printer capabilities and print settings constraints.
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        /// <param name="printer">target printer</param>
        /// <param name="enableAutosave">true to enable save to database, false otherwise</param>
        /// <returns>task; print settings</returns>
        public async Task<PrintSettings> Initialize(string screenName, Printer printer, bool enableAutosave)
        {
            PrintSettings currPrintSettings = new PrintSettings();

            if (string.IsNullOrEmpty(screenName) || printer == null)
            {
                return currPrintSettings;
            }

            RegisterPrintSettingValueChanged(screenName);

            if (!_printerMap.ContainsKey(screenName))
            {
                _printerMap.Add(screenName, printer);
            }
            else
            {
                _printerMap[screenName] = printer;
            }

            if (!_enableAutosaveMap.ContainsKey(screenName))
            {
                _enableAutosaveMap.Add(screenName, enableAutosave);
            }
            else
            {
                _enableAutosaveMap[screenName] = enableAutosave;
            }

            if (printer != null)
            {
                currPrintSettings = await GetPrintSettings(printer.PrintSettingId);
            }

            if (!_printSettingsMap.ContainsKey(screenName))
            {
                _printSettingsMap.Add(screenName, currPrintSettings);
            }
            else
            {
                _printSettingsMap[screenName] = currPrintSettings;
            }

            LoadPrintSettingsOptions();
            FilterPrintSettingsUsingCapabilities();
            MergePrintSettings();
            ApplyPrintSettingConstraints();

            if (!_printSettingListMap.ContainsKey(screenName))
            {
                _printSettingListMap.Add(screenName, _printSettingsViewModel.PrintSettingsList);
            }
            else
            {
                _printSettingListMap[screenName] = _printSettingsViewModel.PrintSettingsList;
            }

            _printSettingsMap.TryGetValue(screenName, out currPrintSettings);
            return currPrintSettings;
        }

        /// <summary>
        /// Uninitialize data
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        public void Uninitialize(string screenName)
        {
            if (!string.IsNullOrEmpty(screenName))
            {
                UnregisterPrintSettingValueChanged(screenName);

                if (_printerMap.ContainsKey(screenName))
                {
                    _printerMap.Remove(screenName);
                }

                if (_enableAutosaveMap.ContainsKey(screenName))
                {
                    _enableAutosaveMap.Remove(screenName);
                }

                if (_printSettingsMap.ContainsKey(screenName))
                {
                    _printSettingsMap.Remove(screenName);
                }
            }
        }

        #region Database Operations

        /// <summary>
        /// Retrives print settings from database.
        /// If not found, default print settings are provided.
        /// </summary>
        /// <param name="id">print setting ID</param>
        /// <returns>task; print settings</returns>
        private async Task<PrintSettings> GetPrintSettings(int id)
        {
            PrintSettings currPrintSettings = await DatabaseController.Instance.GetPrintSettings(id);
            if (currPrintSettings == null)
            {
                currPrintSettings = DefaultsUtility.GetDefaultPrintSettings(
                                                        _printSettingsViewModel.PrintSettingsList);
            }

            return currPrintSettings;
        }

        /// <summary>
        /// Deletes print settings
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        /// <returns>task</returns>
        public async Task RemovePrintSettings(string screenName)
        {
            PrintSettings currPrintSettings = new PrintSettings();
            if (_printSettingsMap.TryGetValue(screenName, out currPrintSettings))
            {
                int deleted = await DatabaseController.Instance.DeletePrintSettings(currPrintSettings);
                if (deleted == 0)
                {
                    // TODO: Display error message
                }

                // TODO: Set this to null?
                // _currPrintSettings = null;
            }
        }

        #endregion Database Operations

        #region Print Settings Events

        /// <summary>
        /// Register for print setting updates
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        public void RegisterPrintSettingValueChanged(String screenName)
        {
            _activeScreen = screenName;
            PrintSettingList printSettingList = null;
            _printSettingListMap.TryGetValue(screenName, out printSettingList);
            _printSettingsViewModel.PrintSettingsList = printSettingList;
            PrintSettingUtility.PrintSettingValueChangedEventHandler += _printSettingValueChangedEventHandler;
        }

        /// <summary>
        /// Unregister for print setting updates
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        public void UnregisterPrintSettingValueChanged(String screenName)
        {
            _activeScreen = null;
            PrintSettingUtility.PrintSettingValueChangedEventHandler -= _printSettingValueChangedEventHandler;
        }

        #endregion Print Settings Events

        #region Print Preview Events

        /// <summary>
        /// Register for print settings changes to update preview
        /// </summary>
        /// <param name="handler"></param>
        public void RegisterUpdatePreviewEventHandler(
            SmartDeviceApp.Controllers.PrintPreviewController.UpdatePreviewEventHandler handler)
        {
            UpdatePreviewEventHandler += handler;
        }

        /// <summary>
        /// Unregister for print settings changes
        /// </summary>
        /// <param name="handler"></param>
        public void UnregisterUpdatePreviewEventHandler(
            SmartDeviceApp.Controllers.PrintPreviewController.UpdatePreviewEventHandler handler)
        {
            UpdatePreviewEventHandler -= handler;
        }

        #endregion Print Preview Events

        #region PrintSettingList Operations

        /// <summary>
        /// Loads the initial print settings file
        /// </summary>
        private void LoadPrintSettingsOptions()
        {
            PrintSettingToValueConverter valueConverter = new PrintSettingToValueConverter();

            string xmlPath = Path.Combine(Package.Current.InstalledLocation.Path,
                FILE_PATH_ASSET_PRINT_SETTINGS_XML);
            XDocument data = XDocument.Load(xmlPath);

            var printSettingsData = from groupData in data.Descendants(PrintSettingConstant.KEY_GROUP)
                                    select new PrintSettingGroup
                                    {
                                        Name = (string)groupData.Attribute(PrintSettingConstant.KEY_NAME),
                                        Text = (string)groupData.Attribute(PrintSettingConstant.KEY_TEXT),
                                        PrintSettings = (
                                            from settingData in groupData.Elements(PrintSettingConstant.KEY_SETTING)
                                            select new PrintSetting
                                            {
                                                Name = (string)settingData.Attribute(PrintSettingConstant.KEY_NAME),
                                                Text = (string)settingData.Attribute(PrintSettingConstant.KEY_TEXT),
                                                Icon = (string)settingData.Attribute(PrintSettingConstant.KEY_ICON),
                                                Type = (PrintSettingType)Enum.Parse(typeof(PrintSettingType),
                                                    (string)settingData.Attribute(PrintSettingConstant.KEY_TYPE)),
                                                Value = valueConverter.Convert(
                                                    (string)settingData.Attribute(PrintSettingConstant.KEY_DEFAULT),
                                                    null, (string)settingData.Attribute(PrintSettingConstant.KEY_NAME),
                                                    null),
                                                Default = valueConverter.Convert(
                                                    (string)settingData.Attribute(PrintSettingConstant.KEY_DEFAULT),
                                                    null, (string)settingData.Attribute(PrintSettingConstant.KEY_NAME),
                                                    null),
                                                Options = (
                                                    from optionData in settingData.Elements(PrintSettingConstant.KEY_OPTION)
                                                    select new PrintSettingOption
                                                    {
                                                        Text = (string)optionData.Value,
                                                        Index = optionData.ElementsBeforeSelf().Count(),
                                                        IsEnabled = true // To be updated later upon apply constraints
                                                    }).ToList<PrintSettingOption>(),
                                                IsEnabled = true,        // To be updated later upon apply constraints
                                                IsValueDisplayed = true  // To be updated later upon apply constraints
                                            }).ToList<PrintSetting>()
                                    };

            // Construct the PrintSettingList
            _printSettingsViewModel.PrintSettingsList = new PrintSettingList();
            var tempList = printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
            foreach (PrintSettingGroup group in tempList)
            {
                _printSettingsViewModel.PrintSettingsList.Add(group);
            }
        }

        /// <summary>
        /// Removes print setting and options depending on printer capabilities
        /// </summary>
        private void FilterPrintSettingsUsingCapabilities()
        {
            Printer printer = null;
            _printerMap.TryGetValue(_activeScreen, out printer);
            
            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);

            if (printer == null || printSettings == null)
            {
                return;
            }

            if (!printer.EnabledBooklet)
            {
                printSettings.Booklet = false;

                PrintSetting bookletPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET);
                if (bookletPrintSetting != null)
                {
                    RemovePrintSetting(bookletPrintSetting);
                }
                PrintSetting bookletLayoutPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
                if (bookletLayoutPrintSetting != null)
                {
                    RemovePrintSetting(bookletLayoutPrintSetting);
                }
                PrintSetting bookletFinishPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
                if (bookletFinishPrintSetting != null)
                {
                    RemovePrintSetting(bookletFinishPrintSetting);
                }
            }
            if (!printer.EnabledStapler)
            {
                PrintSetting staplePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
                if (staplePrintSetting != null)
                {
                    RemovePrintSetting(staplePrintSetting);
                    printSettings.Staple = (int)Staple.Off;
                }
                PrintSetting bookletFinishPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
                if (bookletFinishPrintSetting != null)
                {
                    RemovePrintSettingOption(bookletFinishPrintSetting, (int)BookletFinishing.FoldAndStaple);
                }

            }
            if (!printer.EnabledPunchFour) // Meaning punch3
            {
                // TODO: Verify assumption
                // Assumption that punch4 is the default in print settings xml
                PrintSetting punchPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
                if (punchPrintSetting != null)
                {
                    PrintSettingOption punchFourPrintSettingOption =
                        GetPrintSettingOption(punchPrintSetting, (int)Punch.FourHoles);
                    if (punchFourPrintSettingOption != null)
                    {
                        punchFourPrintSettingOption.Text = "ids_lbl_punch_3holes";
                    }
                }
            }
            if (!printer.EnabledTrayFacedown)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                }
            }
            if (!printer.EnabledTrayAutostack)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Auto);
                }
            }
            if (!printer.EnabledTrayTop)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Top);
                }
            }
            if (!printer.EnabledTrayStack)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Stacking);
                }
            }
            // TODO: Need to create a generic function to remove print settings (type list) with empty options
            if (!(printer.EnabledTrayFacedown || printer.EnabledTrayAutostack ||
                  printer.EnabledTrayTop || printer.EnabledTrayStack))
            {
                // Remove output tray print setting since all options are off
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSetting(outputTrayPrintSetting);
                }
            }

            _printSettingsMap[_activeScreen] = printSettings;
        }

        /// <summary>
        /// Queries the print settings list based on name.
        /// </summary>
        /// <param name="name">print setting name</param>
        /// <returns>PrintSetting if found, else null</returns>
        private PrintSetting GetPrintSetting(string name)
        {
            var query = _printSettingsViewModel.PrintSettingsList
                .SelectMany(printSettingGroup => printSettingGroup.PrintSettings)
                .Where(ps => ps.Name == name);
            return query.FirstOrDefault();
        }

        /// <summary>
        /// Removes a print setting from the group
        /// </summary>
        /// <param name="printSetting">print setting</param>
        private void RemovePrintSetting(PrintSetting printSetting)
        {
            PrintSettingGroup printSettingGroup = GetPrintSettingGroup(printSetting);
            if (printSettingGroup != null)
            {
                printSettingGroup.PrintSettings.Remove(printSetting);
            }
        }

        /// <summary>
        /// Queries the print setting group based on print setting
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <returns>PrintSettingGroup if founf, else null</returns>
        private PrintSettingGroup GetPrintSettingGroup(PrintSetting printSetting)
        {
            return _printSettingsViewModel.PrintSettingsList
                .FirstOrDefault(group => group.PrintSettings.Contains(printSetting));
        }

        /// <summary>
        /// Removes a print setting option
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="index">option index</param>
        private void RemovePrintSettingOption(PrintSetting printSetting, int index)
        {
            PrintSettingOption printSettingOption = printSetting.Options
                .FirstOrDefault(setting => setting.Index == index);
            if (printSettingOption != null)
            {
                printSetting.Options.Remove(printSettingOption);
            }
        }

        /// <summary>
        /// Merges print settings cache (PrintSettings) to print settings list (PrintSettingList)
        /// to reflect actual values from database
        /// </summary>
        private void MergePrintSettings()
        {
            PrintSettings printSettings;
            if (_printSettingsMap.TryGetValue(_activeScreen, out printSettings))
            {
                foreach (var group in _printSettingsViewModel.PrintSettingsList)
                {
                    foreach (var printSetting in group.PrintSettings)
                    {
                        switch (printSetting.Name)
                        {
                            case PrintSettingConstant.NAME_VALUE_COLOR_MODE:
                                printSetting.Value = printSettings.ColorMode;
                                break;
                            case PrintSettingConstant.NAME_VALUE_ORIENTATION:
                                printSetting.Value = printSettings.Orientation;
                                break;
                            case PrintSettingConstant.NAME_VALUE_COPIES:
                                printSetting.Value = printSettings.Copies;
                                break;
                            case PrintSettingConstant.NAME_VALUE_DUPLEX:
                                printSetting.Value = printSettings.Duplex;
                                break;
                            case PrintSettingConstant.NAME_VALUE_PAPER_SIZE:
                                printSetting.Value = printSettings.PaperSize;
                                break;
                            case PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT:
                                printSetting.Value = printSettings.ScaleToFit;
                                break;
                            case PrintSettingConstant.NAME_VALUE_PAPER_TYPE:
                                printSetting.Value = printSettings.PaperType;
                                break;
                            case PrintSettingConstant.NAME_VALUE_INPUT_TRAY:
                                printSetting.Value = printSettings.InputTray;
                                break;
                            case PrintSettingConstant.NAME_VALUE_IMPOSITION:
                                printSetting.Value = printSettings.Imposition;
                                break;
                            case PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER:
                                printSetting.Value = printSettings.ImpositionOrder;
                                break;
                            case PrintSettingConstant.NAME_VALUE_SORT:
                                printSetting.Value = printSettings.Sort;
                                break;
                            case PrintSettingConstant.NAME_VALUE_BOOKLET:
                                printSetting.Value = printSettings.Booklet;
                                break;
                            case PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING:
                                printSetting.Value = printSettings.BookletFinishing;
                                break;
                            case PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT:
                                printSetting.Value = printSettings.BookletLayout;
                                break;
                            case PrintSettingConstant.NAME_VALUE_FINISHING_SIDE:
                                printSetting.Value = printSettings.FinishingSide;
                                break;
                            case PrintSettingConstant.NAME_VALUE_STAPLE:
                                printSetting.Value = printSettings.Staple;
                                break;
                            case PrintSettingConstant.NAME_VALUE_PUNCH:
                                printSetting.Value = printSettings.Punch;
                                break;
                            case PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY:
                                printSetting.Value = printSettings.OutputTray;
                                break;
                            default:
                                // Do nothing
                                break;
                        } // switch-case
                    } // foreach printSetting
                } // foreach group
            } // if statement
        }

        /// <summary>
        /// Updates constraints (value/enable state) on print settings list (PrintSettingList)
        /// and cache (PrintSettings)
        /// </summary>
        private void ApplyPrintSettingConstraints()
        {
            PrintSettings printSettings;
            if (_printSettingsMap.TryGetValue(_activeScreen, out printSettings))
            {
                UpdateConstraintsBasedOnBooklet(printSettings.Booklet);
                UpdateConstraintsBasedOnImposition(printSettings.Imposition);
                UpdateConstraintsBasedOnFinishingSide(printSettings.FinishingSide);
                UpdateConstraintsBasedOnStaple(printSettings.Staple);
                UpdateConstraintsBasedOnPunch(printSettings.Punch);
            }
        }

        /// <summary>
        /// Updates print settings dependent on imposition constraints
        /// </summary>
        /// <param name="value">new imposition value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnImposition(int value)
        {
            bool isUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isUpdated;
            }

            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);
            /* TODO: Delete commented out line if Booklet related settings
             * does not necessarily required to be updated
             * since in order to update Imposition, Booklet must be Off.
            PrintSetting bookletPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET);
            PrintSetting bookletFinishPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
            PrintSetting bookletLayoutPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
             */

            if (value == (int)Imposition.Off)
            {
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    impositionOrderPrintSetting.IsValueDisplayed = false;
                    impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;

                    isUpdated = true;
                }
            }
            else if (value == (int)Imposition.TwoUp)
            {
                if (impositionOrderPrintSetting != null)
                {
                    PrintSettingOption twoUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpLeftToRight);
                    if (twoUpLR != null)
                    {
                        twoUpLR.IsEnabled = true;
                    }
                    PrintSettingOption twoUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpRightToLeft);
                    if (twoUpRL != null)
                    {
                        twoUpRL.IsEnabled = true;
                    }
                    PrintSettingOption fourUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToRight);
                    if (fourUpLR != null)
                    {
                        fourUpLR.IsEnabled = false;
                    }
                    PrintSettingOption fourUpLB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToBottom);
                    if (fourUpLB != null)
                    {
                        fourUpLB.IsEnabled = false;
                    }
                    PrintSettingOption fourUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToLeft);
                    if (fourUpRL != null)
                    {
                        fourUpRL.IsEnabled = false;
                    }
                    PrintSettingOption fourUpRB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToBottom);
                    if (fourUpRB != null)
                    {
                        fourUpRB.IsEnabled = false;
                    }
                    impositionOrderPrintSetting.IsEnabled = true;
                    impositionOrderPrintSetting.IsValueDisplayed = true;

                    //impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperLeftToRight ||
                        (int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperLeftToBottom)
                    {
                        impositionOrderPrintSetting.Value = (int)ImpositionOrder.TwoUpLeftToRight;
                    }
                    else if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperRightToLeft ||
                        (int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperRightToBottom)
                    {
                        impositionOrderPrintSetting.Value = (int)ImpositionOrder.TwoUpRightToLeft;
                    }
                    printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Value;

                    isUpdated = true;
                }
                /* TODO: Delete commented out lines if Booklet related settings
                 * does not necessarily required to be updated
                 * since in order to update Imposition, Booklet must be Off.
                if (bookletPrintSetting != null)
                {
                    bookletPrintSetting.Value = false;
                    PrintSettings.Booklet = false;

                    isUpdated = true;
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    bookletFinishPrintSetting.IsValueDisplayed = false;
                    bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                    PrintSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    bookletLayoutPrintSetting.IsValueDisplayed = false;
                    bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                    PrintSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;

                    isUpdated = true;
                }
                 */
            }
            else if (value == (int)Imposition.FourUp)
            {
                if (impositionOrderPrintSetting != null)
                {
                    PrintSettingOption twoUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpLeftToRight);
                    if (twoUpLR != null)
                    {
                        twoUpLR.IsEnabled = false;
                    }
                    PrintSettingOption twoUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpRightToLeft);
                    if (twoUpRL != null)
                    {
                        twoUpRL.IsEnabled = false;
                    }
                    PrintSettingOption fourUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToRight);
                    if (fourUpLR != null)
                    {
                        fourUpLR.IsEnabled = true;
                    }
                    PrintSettingOption fourUpLB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToBottom);
                    if (fourUpLB != null)
                    {
                        fourUpLB.IsEnabled = true;
                    }
                    PrintSettingOption fourUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToLeft);
                    if (fourUpRL != null)
                    {
                        fourUpRL.IsEnabled = true;
                    }
                    PrintSettingOption fourUpRB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToBottom);
                    if (fourUpRB != null)
                    {
                        fourUpRB.IsEnabled = true;
                    }
                    impositionOrderPrintSetting.IsEnabled = true;
                    impositionOrderPrintSetting.IsValueDisplayed = true;

                    //impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.TwoUpLeftToRight)
                    {
                        impositionOrderPrintSetting.Value = (int)ImpositionOrder.FourUpUpperLeftToRight;
                    }
                    else if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.TwoUpRightToLeft)
                    {
                        impositionOrderPrintSetting.Value = (int)ImpositionOrder.FourUpUpperRightToLeft;
                    }
                    printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Value;

                    isUpdated = true;
                }
                /* TODO: Delete commented out lines if Booklet related settings
                 * does not necessarily required to be updated
                 * since in order to update Imposition, Booklet must be Off.
                if (bookletPrintSetting != null)
                {
                    bookletPrintSetting.Value = false;
                    PrintSettings.Booklet = false;

                    isUpdated = true;
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    bookletFinishPrintSetting.IsValueDisplayed = false;
                    bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                    PrintSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    bookletLayoutPrintSetting.IsValueDisplayed = false;
                    bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                    PrintSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;

                    isUpdated = true;
                }
                 */
            }

            UpdatePagesPerSheet(value);

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on booklet constraints
        /// </summary>
        /// <param name="value">new booklet value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnBooklet(bool value)
        {
            bool isUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isUpdated;
            }

            PrintSetting duplexPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_DUPLEX);
            PrintSetting impositionPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION);
            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);
            PrintSetting bookletFinishPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
            PrintSetting bookletLayoutPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
            PrintSetting finishingSidePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE);
            PrintSetting staplePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
            PrintSetting punchPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            if (value)
            {
                if (duplexPrintSetting != null)
                {
                    duplexPrintSetting.IsEnabled = false;
                    duplexPrintSetting.Value = Duplex.ShortEdge;
                    printSettings.Duplex = (int)Duplex.ShortEdge;

                    isUpdated = true;
                }
                if (impositionPrintSetting != null)
                {
                    impositionPrintSetting.IsEnabled = false;
                    impositionPrintSetting.IsValueDisplayed = false;
                    impositionPrintSetting.Value = Imposition.Off;
                    printSettings.Imposition = (int)Imposition.Off;

                    isUpdated = true;
                }
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    impositionOrderPrintSetting.IsValueDisplayed = false;
                    impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;

                    isUpdated = true;
                }
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = false;
                    finishingSidePrintSetting.IsValueDisplayed = false;
                    finishingSidePrintSetting.Value = (int)FinishingSide.Left;
                    printSettings.FinishingSide = (int)FinishingSide.Left;
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = false;
                    staplePrintSetting.IsValueDisplayed = false;
                    staplePrintSetting.Value = (int)Staple.Off;
                    printSettings.Staple = (int)Staple.Off;

                    isUpdated = true;
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = false;
                    punchPrintSetting.IsValueDisplayed = false;
                    punchPrintSetting.Value = (int)Punch.Off;
                    printSettings.Punch = (int)Punch.Off;

                    isUpdated = true;
                }
                /* Constraints for Output Tray
                if (outputTrayPrintSetting != null)
                {
                    outputTrayPrintSetting.IsEnabled = false;
                    outputTrayPrintSetting.IsValueDisplayed = false;
                    outputTrayPrintSetting.Value = (int)OutputTray.Auto;
                    PrintSettings.OutputTray = (int)OutputTray.Auto;

                    isUpdated = true;
                }
                 */
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = true;
                    bookletFinishPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = true;
                    bookletLayoutPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
            }
            else
            {
                if (duplexPrintSetting != null)
                {
                    duplexPrintSetting.IsEnabled = true;
                    duplexPrintSetting.Value = (int)Duplex.Off; // TODO: Verify if needs to set to Off since this is not indicated in specifications
                    printSettings.Duplex = (int)Duplex.Off;

                    isUpdated = true;
                }
                if (impositionPrintSetting != null)
                {
                    impositionPrintSetting.IsEnabled = true;
                    impositionPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (impositionOrderPrintSetting != null)
                {
                    // impositionOrderPrintSetting.IsEnabled and impositionOrderPrintSetting.IsValueDisplayed
                    // are not set to true since Imposition is Off

                    isUpdated = true;
                }
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = true;
                    finishingSidePrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = true;
                    staplePrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = true;
                    punchPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (outputTrayPrintSetting != null)
                {
                    outputTrayPrintSetting.IsEnabled = true;
                    outputTrayPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    bookletFinishPrintSetting.IsValueDisplayed = false;
                    bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                    printSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    bookletLayoutPrintSetting.IsValueDisplayed = false;
                    bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                    printSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;

                    isUpdated = true;
                }
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on finishing side constraints
        /// </summary>
        /// <param name="value">new finishing side value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnFinishingSide(int value)
        {
            bool isUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isUpdated;
            }

            PrintSetting staplePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);

            if (staplePrintSetting == null)
            {
                return isUpdated;
            }

            int currFinishingSide = printSettings.FinishingSide;
            int currStaple = printSettings.Staple;

            if (value == (int)FinishingSide.Left || value == (int)FinishingSide.Right)
            {
                if (currStaple == (int)Staple.OneUpperLeft || currStaple == (int)Staple.OneUpperLeft)
                {
                    staplePrintSetting.Value = (int)Staple.One;
                    printSettings.Staple = (int)Staple.One;

                    isUpdated = true;
                }

                PrintSettingOption oneUL = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperLeft);
                if (oneUL != null)
                {
                    oneUL.IsEnabled = false;

                    isUpdated = true;
                }
                PrintSettingOption oneUR = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperRight);
                if (oneUR != null)
                {
                    oneUR.IsEnabled = false;

                    isUpdated = true;
                }
                PrintSettingOption one = GetPrintSettingOption(staplePrintSetting, (int)Staple.One);
                if (one != null)
                {
                    one.IsEnabled = true;

                    isUpdated = true;
                }
            }
            else if (value == (int)FinishingSide.Top)
            {
                // Change selected value
                if (currFinishingSide == (int)FinishingSide.Left && currStaple == (int)Staple.One)
                {
                    staplePrintSetting.Value = (int)Staple.OneUpperLeft;
                    printSettings.Staple = (int)Staple.OneUpperLeft;
                }
                else if (currFinishingSide == (int)FinishingSide.Right && currStaple == (int)Staple.One)
                {
                    staplePrintSetting.Value = (int)Staple.OneUpperRight;
                    printSettings.Staple = (int)Staple.OneUpperRight;
                }

                PrintSettingOption oneUL = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperLeft);
                if (oneUL != null)
                {
                    oneUL.IsEnabled = true;

                    isUpdated = true;
                }
                PrintSettingOption oneUR = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperRight);
                if (oneUR != null)
                {
                    oneUR.IsEnabled = true;

                    isUpdated = true;
                }
                PrintSettingOption one = GetPrintSettingOption(staplePrintSetting, (int)Staple.One);
                if (one != null)
                {
                    one.IsEnabled = false;

                    isUpdated = true;
                }

            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on staple constraints
        /// </summary>
        /// <param name="value">new staple value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnStaple(int value)
        {
            bool isUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isUpdated;
            }

            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            if (outputTrayPrintSetting == null)
            {
                return isUpdated;
            }

            int currOutputTray = printSettings.OutputTray;

            if (value == (int)Staple.OneUpperLeft || value == (int)Staple.OneUpperRight ||
                value == (int)Staple.One || value == (int)Staple.Two)
            {
                /* Constraints for Output Tray
                if (currOutputTray == (int)OutputTray.FaceUp)
                {
                    int newOutputTray = (int)outputTrayPrintSetting.Default;
                    outputTrayPrintSetting.Value = newOutputTray;
                    PrintSettings.OutputTray = newOutputTray;

                    PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                    if (faceUp != null)
                    {
                        faceUp.IsEnabled = false;
                    }

                    isUpdated = true;
                }
                 */
            }
            else if (value == (int)Staple.Off)
            {
                /* Constraints for Output Tray
                PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                if (faceUp != null)
                {
                    faceUp.IsEnabled = true;

                    isUpdated = true;
                }
                 */
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on punch constraints
        /// </summary>
        /// <param name="value">new punch value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnPunch(int value)
        {
            bool isUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isUpdated;
            }

            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            if (outputTrayPrintSetting == null)
            {
                return isUpdated;
            }

            int currOutputTray = printSettings.OutputTray;

            if (value == (int)Punch.TwoHoles || value == (int)Punch.FourHoles)
            {
                if (currOutputTray == (int)OutputTray.FaceDown) // ||
                //currOutputTray == (int)OutputTray.FaceUp)
                {
                    int newOutputTray = (int)outputTrayPrintSetting.Default;
                    outputTrayPrintSetting.Value = newOutputTray;
                    printSettings.OutputTray = newOutputTray;

                    isUpdated = true;
                }

                PrintSettingOption faceDown = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                if (faceDown != null)
                {
                    faceDown.IsEnabled = false;

                    isUpdated = true;
                }
                /* Constraints for Output Tray
                PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                if (faceUp != null)
                {
                    faceUp.IsEnabled = false;

                    isUpdated = true;
                }
                 */

                isUpdated = true;
            }
            else if (value == (int)Punch.Off)
            {
                PrintSettingOption faceDown = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                if (faceDown != null)
                {
                    faceDown.IsEnabled = true;

                    isUpdated = true;
                }
                /* Constraints for Output Tray
                PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                if (faceUp != null)
                {
                    faceUp.IsEnabled = true;

                    isUpdated = true;
                }
                 */

                isUpdated = true;
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isUpdated;
        }

        /// <summary>
        /// Queries the print setting option based on print setting and the option index.
        /// Option index is fixed (should be mapped with Common\Enum\PrintSettingsOptions class)
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="index">option index</param>
        /// <returns>PrintSettingOption if found, else null</returns>
        private PrintSettingOption GetPrintSettingOption(PrintSetting printSetting, int index)
        {
            return printSetting.Options.FirstOrDefault(option => option.Index == index);
        }

        /// <summary>
        /// Receives modified print setting and its new value
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        /// <param name="value">updated value</param>
        public async void PrintSettingValueChanged(PrintSetting printSetting, object value)
        {
            if (printSetting == null || value == null)
            {
                return;
            }

            bool isPreviewAffected = false;

            switch (printSetting.Type)
            {
                case PrintSettingType.boolean:
                    isPreviewAffected = await UpdatePrintSettings(printSetting, (bool)value);
                    break;
                case PrintSettingType.list:
                case PrintSettingType.numeric:
                    isPreviewAffected = await UpdatePrintSettings(printSetting, (int)value);
                    break;
                case PrintSettingType.unknown:
                default:
                    // Do nothing
                    break;
            }

            if (UpdatePreviewEventHandler != null && isPreviewAffected)
            {
                UpdatePreviewEventHandler(printSetting);
            }
        }

        /// <summary>
        /// Updates the print settings list (PrintSettingList) and cache (PrintSettings),
        /// updates value and enabled options based on constraints, and applies
        /// changes to the PreviewPage image.
        /// </summary>
        /// <param name="printSetting">source print setting</param>
        /// <param name="value">updated value</param>
        /// <returns>task; true if print preview needs to be refreshed, false otherwise</returns>
        public async Task<bool> UpdatePrintSettings(PrintSetting printSetting, int value)
        {
            bool isPreviewAffected = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSetting == null)
            {
                return isPreviewAffected;
            }

            string name = printSetting.Name;

            if (name.Equals(PrintSettingConstant.NAME_VALUE_COLOR_MODE))
            {
                int prevColorMode = printSettings.ColorMode;
                if (printSettings.ColorMode != value)
                {
                    printSettings.ColorMode = value;
                    // Matters only if changed to/from Black
                    if (prevColorMode == (int)ColorMode.Mono ||
                        value == (int)ColorMode.Mono)
                    {
                        isPreviewAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_COPIES))
            {
                bool isValid;
                value = CheckIfCopiesValid(value, out isValid);
                if (!isValid)
                {
                    printSetting.Value = value;
                }
                if (printSettings.Copies != value)
                {
                    printSettings.Copies = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_ORIENTATION))
            {
                if (printSettings.Orientation != value)
                {
                    // isConstraintAffected = UpdateConstraintBookletLayoutUsingOrientation(value);
                    printSettings.Orientation = value;
                    isPreviewAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_DUPLEX))
            {
                if (printSettings.Duplex != value)
                {
                    printSettings.Duplex = value;
                    isPreviewAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PAPER_SIZE))
            {
                if (printSettings.PaperSize != value)
                {
                    printSettings.PaperSize = value;
                    isPreviewAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PAPER_TYPE))
            {
                if (printSettings.PaperType != value)
                {
                    printSettings.PaperType = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_INPUT_TRAY))
            {
                if (printSettings.InputTray != value)
                {
                    printSettings.InputTray = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION))
            {
                if (printSettings.Imposition != value)
                {
                    isPreviewAffected = UpdateConstraintsBasedOnImposition(value);
                    printSettings.Imposition = value;
                    isPreviewAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER))
            {
                if (printSettings.ImpositionOrder != value)
                {
                    printSettings.ImpositionOrder = value;
                    if (GetPagesPerSheet(_activeScreen) > 1) // Matters only if pages per sheet is more than 1
                    {
                        isPreviewAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_SORT))
            {
                if (printSettings.Sort != value)
                {
                    printSettings.Sort = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING))
            {
                if (printSettings.BookletFinishing != value)
                {
                    printSettings.BookletFinishing = value;
                    // Matters only when booklet is ON
                    if (printSettings.Booklet == true)
                    {
                        isPreviewAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT))
            {
                if (printSettings.BookletLayout != value)
                {
                    printSettings.BookletLayout = value;
                    // Matters only when booklet is ON
                    if (printSettings.Booklet == true)
                    {
                        isPreviewAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE))
            {
                if (printSettings.FinishingSide != value)
                {
                    isPreviewAffected = UpdateConstraintsBasedOnFinishingSide(value);
                    printSettings.FinishingSide = value;
                    // Matters only when staple or punch is ON
                    if (printSettings.Staple != (int)Staple.Off ||
                        printSettings.Punch != (int)Punch.Off)
                    {
                        isPreviewAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_STAPLE))
            {
                if (printSettings.Staple != value)
                {
                    isPreviewAffected = UpdateConstraintsBasedOnStaple(value);
                    printSettings.Staple = value;
                    isPreviewAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PUNCH))
            {
                if (printSettings.Punch != value)
                {
                    isPreviewAffected = UpdateConstraintsBasedOnPunch(value);
                    printSettings.Punch = value;
                    isPreviewAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY))
            {
                if (printSettings.OutputTray != value)
                {
                    printSettings.OutputTray = value;
                }
            }

            bool enableAutosave = false;
            _enableAutosaveMap.TryGetValue(_activeScreen, out enableAutosave);
            if (enableAutosave)
            {
                await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isPreviewAffected;
        }

        /// <summary>
        /// Updates the print settings list (PrintSettingList) and cache (PrintSettings),
        /// updates value and enabled options based on constraints, and applies
        /// changes to the PreviewPage image.
        /// </summary>
        /// <param name="printSetting">source print setting</param>
        /// <param name="state">updated value</param>
        /// <returns>task; true </returns>
        public async Task<bool> UpdatePrintSettings(PrintSetting printSetting, bool state)
        {
            bool isPreviewAffected = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSetting == null)
            {
                return isPreviewAffected;
            }

            string name = printSetting.Name;

            if (name.Equals(PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT))
            {
                if (printSettings.ScaleToFit != state)
                {
                    printSettings.ScaleToFit = state;
                    isPreviewAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET))
            {
                if (printSettings.Booklet != state)
                {
                    isPreviewAffected = UpdateConstraintsBasedOnBooklet(state);
                    printSettings.Booklet = state;
                    isPreviewAffected = true;
                }
            }

            bool enableAutosave = false;
            _enableAutosaveMap.TryGetValue(_activeScreen, out enableAutosave);
            if (enableAutosave)
            {
                await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isPreviewAffected;
        }

        #endregion PrintSettingList Operations

        #region Get Properties

        /// <summary>
        /// Retrieves the current print settings
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        /// <returns>print settings</returns>
        public PrintSettings GetCurrentPrintSettings(string screenName)
        {
            PrintSettings printSettings = null;
            if (!string.IsNullOrEmpty(screenName) && _printSettingsMap.ContainsKey(screenName))
            {
                printSettings = _printSettingsMap[screenName];
            }

            return printSettings;
        }

        /// <summary>
        /// Retrives the number of pages per sheet
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        /// <returns>number of pages per sheet</returns>
        public int GetPagesPerSheet(string screenName)
        {
            int pagesPerSheet = 1;
            if (!string.IsNullOrEmpty(screenName) && _pagesPerSheetMap.ContainsKey(screenName))
            {
                pagesPerSheet = _pagesPerSheetMap[screenName];
            }
            return pagesPerSheet;
        }

        #endregion Get Properties

        #region Utilities

        /// <summary>
        /// Checks if the copies value is within acceptable range
        /// </summary>
        /// <param name="value">input value</param>
        /// <param name="result">true if input value is accepted, false otherwise</param>
        /// <returns>the copies value if input value is not accepted</returns>
        private int CheckIfCopiesValid(int value, out bool result)
        {
            result = true;

            // Note: Number received from event is 0 for non-numeric input
            if (value < PrintSettingConstant.COPIES_MIN)
            {
                result = false;
                return PrintSettingConstant.COPIES_MIN;
            }
            else if (value > PrintSettingConstant.COPIES_MAX)
            {
                result = false;
                return PrintSettingConstant.COPIES_MAX;
            }

            return value;
        }

        /// <summary>
        /// Computes the number of pages based on imposition
        /// </summary>
        /// <param name="imposition">imposition type</param>
        /// <returns>number of pages per sheet</returns>
        private void UpdatePagesPerSheet(int imposition)
        {
            int pagesPerSheet = 1;
            switch (imposition)
            {
                case (int)Imposition.TwoUp:
                    pagesPerSheet = 2;
                    break;
                case (int)Imposition.FourUp:
                    pagesPerSheet = 4;
                    break;
                case (int)Imposition.Off:
                default:
                    // Do nothing
                    break;
            }

            if (_pagesPerSheetMap.ContainsKey(_activeScreen))
            {
                _pagesPerSheetMap[_activeScreen] = pagesPerSheet;
            }
        }

        #endregion Utilities

    }
}
