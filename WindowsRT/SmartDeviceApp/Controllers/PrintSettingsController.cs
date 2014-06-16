﻿//
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

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.ApplicationModel;

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
        private const string FILE_PATH_ASSET_PRINT_SETTINGS_AUTH_XML = "Assets/printsettings_authentication.xml";

        private PrintSettingsViewModel _printSettingsViewModel;
        private string _activeScreen;
        private string _prevPinCode;

        // Maps (Managed values)
        private Dictionary<string, Printer> _printerMap;
        private Dictionary<string, PrintSettings> _printSettingsMap;
        private Dictionary<string, PrintSettingList> _printSettingListMap;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static PrintSettingsController() { }

        private PrintSettingsController()
        {
            _printerMap = new Dictionary<string, Printer>();
            _printSettingsMap = new Dictionary<string, PrintSettings>();
            _printSettingListMap = new Dictionary<string, PrintSettingList>();

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
        /// <returns>task</returns>
        public async Task Initialize(string screenName, Printer printer)
        {
            PrintSettings currPrintSettings = new PrintSettings();

            if (string.IsNullOrEmpty(screenName) || printer == null)
            {
                return;
            }

            _printSettingsViewModel.PrinterName = printer.Name;
            _printSettingsViewModel.PrinterId = printer.Id;
            _printSettingsViewModel.PrinterIpAddress = printer.IpAddress;

            int printSettingId = -1;
            if (printer.PrintSettingId != null)
            {
                printSettingId = printer.PrintSettingId.Value;
            }
            currPrintSettings = await GetPrintSettings(printSettingId);

            if (screenName.Equals(ScreenMode.PrintPreview.ToString()))
            {
                _prevPinCode = null;
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
            PrintSettings currPrintSettings = null;
            if (id > -1)
            {
                currPrintSettings = await DatabaseController.Instance.GetPrintSettings(id);
            }
            if (currPrintSettings == null)
            {
                currPrintSettings = DefaultsUtility.GetDefaultPrintSettings(
                                                        _printSettingsViewModel.PrintSettingsList);
            }

            return currPrintSettings;
        }

        /// <summary>
        /// Create default print settings for the printer.
        /// Requires that the printer is valid.
        /// </summary>
        /// <param name="printer">printer</param>
        /// <returns>task; print setting ID</returns>
        public async Task<int> CreatePrintSettings(Printer printer)
        {
            PrintSettings printSettings = new PrintSettings();

            if (printer != null && printer.Id > -1)
            {
                printSettings.PrinterId = printer.Id;
                int added = await DatabaseController.Instance.InsertPrintSettings(printSettings);
                if (added == 0)
                {
                    // TODO: Display error?
                    return -1;
                }
            }

            return printSettings.Id;
        }

        #endregion Database Operations

        #region Print Settings Events

        /// <summary>
        /// Register for print setting updates
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        public void RegisterPrintSettingValueChanged(String screenName)
        {
            if (!string.IsNullOrEmpty(screenName))
            {
                _activeScreen = screenName;

                // Show/hide other controls
                _printSettingsViewModel.IsPrintPreview = screenName.Equals(ScreenMode.PrintPreview.ToString());

                // Refresh Printer
                Printer printer = null;
                if (_printerMap.TryGetValue(screenName, out printer))
                {
                    _printSettingsViewModel.PrinterName = printer.Name;
                    _printSettingsViewModel.PrinterId = printer.Id;
                    _printSettingsViewModel.PrinterIpAddress = printer.IpAddress;
                }

                // Refresh Print Settings
                PrintSettingList printSettingList = null;
                if (_printSettingListMap.TryGetValue(screenName, out printSettingList))
                {
                    _printSettingsViewModel.PrintSettingsList = printSettingList;
                }

                PrintSettingUtility.PrintSettingValueChangedEventHandler += _printSettingValueChangedEventHandler;
            }
        }

        /// <summary>
        /// Unregister for print setting updates
        /// </summary>
        /// <param name="screenName">name of active screen</param>
        public void UnregisterPrintSettingValueChanged(String screenName)
        {
            if (string.Equals(_activeScreen, screenName))
            {
                PrintSettingUtility.PrintSettingValueChangedEventHandler -= _printSettingValueChangedEventHandler;
                _activeScreen = null;
            }
        }

        #endregion Print Settings Events

        #region Print Preview Events

        /// <summary>
        /// Register for print settings changes to update preview
        /// </summary>
        /// <param name="handler">update preview event handler</param>
        public void RegisterUpdatePreviewEventHandler(
            SmartDeviceApp.Controllers.PrintPreviewController.UpdatePreviewEventHandler handler)
        {
            UpdatePreviewEventHandler += handler;
        }

        /// <summary>
        /// Unregister for print settings changes
        /// </summary>
        /// <param name="handler">update preview event handler</param>
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
            // Construct the PrintSettingList
            _printSettingsViewModel.PrintSettingsList = new PrintSettingList();
            var tempList = ParseXmlFile(FILE_PATH_ASSET_PRINT_SETTINGS_XML);

            // Append Authentication group for Print Preview screen
            if (_activeScreen.Equals(ScreenMode.PrintPreview.ToString()))
            {
                tempList.AddRange(ParseXmlFile(FILE_PATH_ASSET_PRINT_SETTINGS_AUTH_XML).AsEnumerable());
            }
            foreach (PrintSettingGroup group in tempList)
            {
                _printSettingsViewModel.PrintSettingsList.Add(group);
            }
        }

        /// <summary>
        /// Reads an XML file and creates PrintSettingGroup
        /// </summary>
        /// <param name="path">XML file relative path from install location</param>
        /// <returns>list of print setting groups</returns>
        private List<PrintSettingGroup> ParseXmlFile(string path)
        {
            PrintSettingToValueConverter valueConverter = new PrintSettingToValueConverter();
            string xmlPath = Path.Combine(Package.Current.InstalledLocation.Path, path);
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

            return printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
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

            // prn_enabled_paper_lw
            if (!printer.EnabledPaperLW)
            {
                PrintSetting paperTypePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PAPER_TYPE);
                if (paperTypePrintSetting != null)
                {
                    RemovePrintSettingOption(paperTypePrintSetting, (int)PaperType.LWPaper);
                }
            }

            // prn_enabled_feed_tray1
            if (!printer.EnabledFeedTrayOne)
            {
                PrintSetting inputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_INPUT_TRAY);
                if (inputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(inputTrayPrintSetting, (int)InputTray.Tray1);
                }
            }

            // prn_enabled_feed_tray2
            if (!printer.EnabledFeedTrayTwo)
            {
                PrintSetting inputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_INPUT_TRAY);
                if (inputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(inputTrayPrintSetting, (int)InputTray.Tray2);
                }
            }

            // prn_enabled_feed_tray3
            if (!printer.EnabledFeedTrayThree)
            {
                PrintSetting inputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_INPUT_TRAY);
                if (inputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(inputTrayPrintSetting, (int)InputTray.Tray3);
                }
            }

            // prn_enabled_booklet_finishing
            if (!printer.EnabledBookletFinishing)
            {
                PrintSetting bookletFinishPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
                if (bookletFinishPrintSetting != null)
                {
                    RemovePrintSetting(bookletFinishPrintSetting);
                }
            }

            // prn_enabled_stapler
            if (!printer.EnabledStapler)
            {
                PrintSetting staplePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
                if (staplePrintSetting != null)
                {
                    RemovePrintSetting(staplePrintSetting);
                }
                PrintSetting bookletFinishPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
                if (bookletFinishPrintSetting != null)
                {
                    RemovePrintSettingOption(bookletFinishPrintSetting, (int)BookletFinishing.FoldAndStaple);
                }
            }

            // prn_enabled_punch3 and prn_enabled_punch4
            if (printer.EnabledPunchThree && !printer.EnabledPunchFour) // Meaning punch3
            {
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
            else if (!printer.EnabledPunchThree && !printer.EnabledPunchFour) // No punch
            {
                PrintSetting punchPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
                if (punchPrintSetting != null)
                {
                    RemovePrintSetting(punchPrintSetting);
                }
            }

            // prn_enabled_tray_facedown
            if (!printer.EnabledTrayFacedown)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                }
            }

            // prn_enabled_tray_top
            if (!printer.EnabledTrayTop)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Top);
                }
            }

            // prn_enabled_tray_stack
            if (!printer.EnabledTrayStack)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Stacking);
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
                            case PrintSettingConstant.NAME_VALUE_SECURE_PRINT:
                                printSetting.Value = printSettings.EnabledSecurePrint;
                                break;
                            case PrintSettingConstant.NAME_VALUE_PIN_CODE:
                                printSetting.Value = printSettings.PinCode;
                                break;
                            default:
                                // Do nothing
                                break;
                        } // Statement: switch-case
                    } // Statement: foreach printSetting
                } // Statement: foreach group
            } // Statement: if printSettingsMap
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
                UpdateConstraintsBasedOnBooklet(printSettings.Booklet, false);
                UpdateConstraintsBasedOnOrientation(printSettings.Orientation, false);
                UpdateConstraintsBasedOnImposition(printSettings.Imposition, false);
                UpdateConstraintsBasedOnFinishingSide(printSettings.FinishingSide, false);
                UpdateConstraintsBasedOnPunch(printSettings.Punch, false);
                UpdateConstraintsBasedOnSecurePrint(printSettings.EnabledSecurePrint);
            }
        }

        /// <summary>
        /// Updates print settings dependent on orientation constraints
        /// </summary>
        /// <param name="value">new imposition value</param>
        /// <returns>true when dependent values are updated, false otherwise</returns>
        private bool UpdateConstraintsBasedOnOrientation(int value, bool updateValues)
        {
            bool isValueUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isValueUpdated;
            }

            PrintSetting bookletLayoutPrintSetting =
                 GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
            PrintSetting punchPrintSetting =
                 GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);

            bool currBooklet = printSettings.Booklet;
            int currBookletLayout = printSettings.BookletLayout;
            int currFinishingSide = printSettings.FinishingSide;
            int currPunch = printSettings.Punch;

            if (value == (int)Orientation.Landscape)
            {
                if (currFinishingSide != (int)FinishingSide.Top &&
                    currPunch == (int)Punch.FourHoles)
                {
                    if (punchPrintSetting != null)
                    {
                        int newPunch = (int)punchPrintSetting.Default;
                        if (updateValues)
                        {
                            punchPrintSetting.Value = newPunch;
                            printSettings.Punch = newPunch;
                        }
                    }
                }
                isValueUpdated = true;
            }
            else if (value == (int)Orientation.Portrait)
            {
                if (currFinishingSide == (int)FinishingSide.Top &&
                    currPunch == (int)Punch.FourHoles)
                {
                    if (punchPrintSetting != null)
                    {
                        int newPunch = (int)punchPrintSetting.Default;
                        if (updateValues)
                        {
                            punchPrintSetting.Value = newPunch;
                            printSettings.Punch = newPunch;
                        }
                    }
                }
                isValueUpdated = true;
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isValueUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on imposition constraints
        /// </summary>
        /// <param name="value">new imposition value</param>
        /// <returns>true when dependent values are updated, false otherwise</returns>
        private bool UpdateConstraintsBasedOnImposition(int value, bool updateValues)
        {
            bool isValueUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isValueUpdated;
            }

            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);

            if (value == (int)Imposition.Off)
            {
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    impositionOrderPrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                        printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;
                    }
                }
                isValueUpdated = true;
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

                    if (updateValues)
                    {
                        if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperLeftToRight ||
                            (int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperLeftToBottom)
                        {
                            impositionOrderPrintSetting.Value = (int)ImpositionOrder.TwoUpLeftToRight;
                            printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Value;
                        }
                        else if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperRightToLeft ||
                            (int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.FourUpUpperRightToBottom)
                        {
                            impositionOrderPrintSetting.Value = (int)ImpositionOrder.TwoUpRightToLeft;
                            printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Value;
                        }
                    }
                }
                isValueUpdated = true;
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

                    if (updateValues)
                    {
                        if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.TwoUpLeftToRight)
                        {
                            impositionOrderPrintSetting.Value = (int)ImpositionOrder.FourUpUpperLeftToRight;
                            printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Value;
                        }
                        else if ((int)impositionOrderPrintSetting.Value == (int)ImpositionOrder.TwoUpRightToLeft)
                        {
                            impositionOrderPrintSetting.Value = (int)ImpositionOrder.FourUpUpperRightToLeft;
                            printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Value;
                        }
                    }
                }
                isValueUpdated = true;
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isValueUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on booklet constraints
        /// </summary>
        /// <param name="value">new booklet value</param>
        /// <param name="updateValues">true when dependent values should be updated, false otherwise</param>
        /// <returns>true when dependent values are updated, false otherwise</returns>
        private bool UpdateConstraintsBasedOnBooklet(bool value, bool updateValues)
        {
            bool isValueUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isValueUpdated;
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

            int currOrientation = printSettings.Orientation;

            if (value)
            {
                if (duplexPrintSetting != null)
                {
                    duplexPrintSetting.IsEnabled = false;
                    if (updateValues)
                    {
                        duplexPrintSetting.Value = Duplex.ShortEdge;
                        printSettings.Duplex = (int)Duplex.ShortEdge;
                    }
                }
                if (impositionPrintSetting != null)
                {
                    impositionPrintSetting.IsEnabled = false;
                    impositionPrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        impositionPrintSetting.Value = Imposition.Off;
                        printSettings.Imposition = (int)Imposition.Off;
                    }
                }
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    impositionOrderPrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                        printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;
                    }
                }
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = false;
                    finishingSidePrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        finishingSidePrintSetting.Value = (int)FinishingSide.Left;
                        printSettings.FinishingSide = (int)FinishingSide.Left;
                    }
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = false;
                    staplePrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        staplePrintSetting.Value = (int)Staple.Off;
                        printSettings.Staple = (int)Staple.Off;
                    }
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = false;
                    punchPrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        punchPrintSetting.Value = (int)Punch.Off;
                        printSettings.Punch = (int)Punch.Off;
                    }
                }
                if (outputTrayPrintSetting != null)
                {
                    PrintSettingOption faceDownTray =
                        GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                    if (faceDownTray != null)
                    {
                        faceDownTray.IsEnabled = false;
                    }
                    PrintSettingOption stackingTray =
                        GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Stacking);
                    if (stackingTray != null)
                    {
                        stackingTray.IsEnabled = false;
                    }
                    PrintSettingOption topTray =
                        GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Top);
                    if (topTray != null)
                    {
                        topTray.IsEnabled = false;
                    }
                    if (updateValues)
                    {
                        outputTrayPrintSetting.Value = (int)OutputTray.Auto;
                        printSettings.OutputTray = (int)OutputTray.Auto;
                    }
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = true;
                    bookletFinishPrintSetting.IsValueDisplayed = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = true;
                    bookletLayoutPrintSetting.IsValueDisplayed = true;
                }
                isValueUpdated = true;
            }
            else
            {
                if (duplexPrintSetting != null)
                {
                    duplexPrintSetting.IsEnabled = true;
                    if (updateValues)
                    {
                        duplexPrintSetting.Value = (int)Duplex.Off;
                        printSettings.Duplex = (int)Duplex.Off;
                    }
                }
                if (impositionPrintSetting != null)
                {
                    impositionPrintSetting.IsEnabled = true;
                    impositionPrintSetting.IsValueDisplayed = true;
                }
                // impositionOrderPrintSetting.IsEnabled and impositionOrderPrintSetting.IsValueDisplayed
                // are not set to true since Imposition is Off
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = true;
                    finishingSidePrintSetting.IsValueDisplayed = true;
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = true;
                    staplePrintSetting.IsValueDisplayed = true;
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = true;
                    punchPrintSetting.IsValueDisplayed = true;
                }
                if (outputTrayPrintSetting != null)
                {
                    PrintSettingOption faceDownTray =
                        GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                    if (faceDownTray != null)
                    {
                        faceDownTray.IsEnabled = true;
                    }
                    PrintSettingOption stackingTray =
                        GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Stacking);
                    if (stackingTray != null)
                    {
                        stackingTray.IsEnabled = true;
                    }
                    PrintSettingOption topTray =
                        GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Top);
                    if (topTray != null)
                    {
                        topTray.IsEnabled = true;
                    }
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    bookletFinishPrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                        printSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;
                    }
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    bookletLayoutPrintSetting.IsValueDisplayed = false;
                    if (updateValues)
                    {
                        bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                        printSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;
                    }
                }
                isValueUpdated = true;
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isValueUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on finishing side constraints
        /// </summary>
        /// <param name="value">new finishing side value</param>
        /// <returns>true when dependent values are updated, false otherwise</returns>
        private bool UpdateConstraintsBasedOnFinishingSide(int value, bool updateValues)
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
            PrintSetting punchPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);

            int currOrientation = printSettings.Orientation;
            int currFinishingSide = printSettings.FinishingSide;
            int currStaple = printSettings.Staple;
            int currPunch = printSettings.Punch;

            if (currStaple == (int)Staple.Two)
            {
                isUpdated = true;
            }

            if (value == (int)FinishingSide.Left || value == (int)FinishingSide.Right)
            {
                if (currStaple == (int)Staple.Two ||
                    (value == (int)FinishingSide.Left && currFinishingSide == (int)FinishingSide.Right && currStaple == (int)Staple.One) ||
                    (value == (int)FinishingSide.Left && currFinishingSide == (int)FinishingSide.Top && currStaple == (int)Staple.OneUpperRight) ||
                    (value == (int)FinishingSide.Right && currFinishingSide == (int)FinishingSide.Left && currStaple == (int)Staple.One) ||
                    (value == (int)FinishingSide.Right && currFinishingSide == (int)FinishingSide.Top && currStaple == (int)Staple.OneUpperLeft))
                {
                    isUpdated = true;
                }

                if (currOrientation == (int)Orientation.Landscape && currPunch == (int)Punch.FourHoles)
                {
                    if (punchPrintSetting != null)
                    {
                        int newPunch = (int)punchPrintSetting.Default;
                        if (updateValues)
                        {
                            punchPrintSetting.Value = newPunch;
                            printSettings.Punch = newPunch;
                            isUpdated = true;
                        }
                    }
                }

                if (staplePrintSetting != null)
                {
                    if (updateValues && (currStaple == (int)Staple.OneUpperLeft || currStaple == (int)Staple.OneUpperRight))
                    {
                        staplePrintSetting.Value = (int)Staple.One;
                        printSettings.Staple = (int)Staple.One;
                        isUpdated = true;
                    }

                    PrintSettingOption oneUL = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperLeft);
                    if (oneUL != null)
                    {
                        oneUL.IsEnabled = false;
                    }
                    PrintSettingOption oneUR = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperRight);
                    if (oneUR != null)
                    {
                        oneUR.IsEnabled = false;
                    }
                    PrintSettingOption one = GetPrintSettingOption(staplePrintSetting, (int)Staple.One);
                    if (one != null)
                    {
                        one.IsEnabled = true;
                    }
                }
            }
            else if (value == (int)FinishingSide.Top)
            {
                if (currOrientation == (int)Orientation.Portrait && currPunch == (int)Punch.FourHoles)
                {
                    if (punchPrintSetting != null)
                    {
                        int newPunch = (int)punchPrintSetting.Default;
                        if (updateValues)
                        {
                            punchPrintSetting.Value = newPunch;
                            printSettings.Punch = newPunch;
                            isUpdated = true;
                        }
                    }
                }

                if (staplePrintSetting != null)
                {
                    if (updateValues)
                    {
                        if (currFinishingSide == (int)FinishingSide.Left && currStaple == (int)Staple.One)
                        {
                            staplePrintSetting.Value = (int)Staple.OneUpperLeft;
                            printSettings.Staple = (int)Staple.OneUpperLeft;
                            isUpdated = true;
                        }
                        else if (currFinishingSide == (int)FinishingSide.Right && currStaple == (int)Staple.One)
                        {
                            staplePrintSetting.Value = (int)Staple.OneUpperRight;
                            printSettings.Staple = (int)Staple.OneUpperRight;
                            isUpdated = true;
                        }
                    }

                    PrintSettingOption oneUL = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperLeft);
                    if (oneUL != null)
                    {
                        oneUL.IsEnabled = true;
                    }
                    PrintSettingOption oneUR = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperRight);
                    if (oneUR != null)
                    {
                        oneUR.IsEnabled = true;
                    }
                    PrintSettingOption one = GetPrintSettingOption(staplePrintSetting, (int)Staple.One);
                    if (one != null)
                    {
                        one.IsEnabled = false;
                    }
                }
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on punch constraints
        /// </summary>
        /// <param name="value">new punch value</param>
        /// <returns>true when dependent values are updated, false otherwise</returns>
        private bool UpdateConstraintsBasedOnPunch(int value, bool updateValues)
        {
            bool isValueUpdated = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isValueUpdated;
            }

            PrintSetting finishingSidePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE);
            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            int currOrientation = printSettings.Orientation;
            int currFinishingSide = printSettings.FinishingSide;
            int currOutputTray = printSettings.OutputTray;

            if (value == (int)Punch.FourHoles)
            {
                if (currOrientation == (int)Orientation.Portrait &&
                currFinishingSide == (int)FinishingSide.Top)
                {
                    if (finishingSidePrintSetting != null)
                    {
                        int newFinishingSide = (int)finishingSidePrintSetting.Default; // Left
                        if (updateValues)
                        {
                            finishingSidePrintSetting.Value = newFinishingSide;
                            printSettings.FinishingSide = newFinishingSide;
                        }
                    }
                }
                else if (currOrientation == (int)Orientation.Landscape &&
                    currFinishingSide != (int)FinishingSide.Top)
                {
                    if (finishingSidePrintSetting != null)
                    {
                        int newFinishingSide = (int)FinishingSide.Top;
                        if (updateValues)
                        {
                            finishingSidePrintSetting.Value = newFinishingSide;
                            printSettings.FinishingSide = newFinishingSide;
                        }
                    }
                }

                if (currOutputTray == (int)OutputTray.FaceDown)
                {
                    if (outputTrayPrintSetting != null)
                    {
                        int newOutputTray = (int)outputTrayPrintSetting.Default;
                        if (updateValues)
                        {
                            outputTrayPrintSetting.Value = newOutputTray;
                            printSettings.OutputTray = newOutputTray;
                        }
                    }
                }

                PrintSettingOption faceDown = GetPrintSettingOption(outputTrayPrintSetting,
                    (int)OutputTray.FaceDown);
                if (faceDown != null)
                {
                    faceDown.IsEnabled = false;
                }
                isValueUpdated = true;
            }
            else if (value == (int)Punch.TwoHoles)
            {
                if (currOutputTray == (int)OutputTray.FaceDown)
                {
                    if (outputTrayPrintSetting != null)
                    {
                        int newOutputTray = (int)outputTrayPrintSetting.Default;
                        if (updateValues)
                        {
                            outputTrayPrintSetting.Value = newOutputTray;
                            printSettings.OutputTray = newOutputTray;
                        }
                    }
                }

                PrintSettingOption faceDown = GetPrintSettingOption(outputTrayPrintSetting,
                    (int)OutputTray.FaceDown);
                if (faceDown != null)
                {
                    faceDown.IsEnabled = false;
                }
                isValueUpdated = true;
            }
            else if (value == (int)Punch.Off)
            {
                PrintSettingOption faceDown = GetPrintSettingOption(outputTrayPrintSetting,
                    (int)OutputTray.FaceDown);
                if (faceDown != null)
                {
                    faceDown.IsEnabled = true;
                }
                isValueUpdated = true;
            }

            _printSettingsMap[_activeScreen] = printSettings;

            return isValueUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on secure print constraints
        /// </summary>
        /// <param name="value">new scure print value</param>
        private void UpdateConstraintsBasedOnSecurePrint(bool value)
        {
            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return;
            }

            PrintSetting pinCodePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_PIN_CODE);

            if (value)
            {
                if (pinCodePrintSetting != null)
                {
                    pinCodePrintSetting.IsEnabled = true;
                    printSettings.PinCode = _prevPinCode;
                    pinCodePrintSetting.Value = _prevPinCode;
                }
            }
            else
            {
                if (pinCodePrintSetting != null)
                {
                    pinCodePrintSetting.IsEnabled = false;
                    _prevPinCode = printSettings.PinCode;
                    printSettings.PinCode = null;
                    pinCodePrintSetting.Value = string.Empty;
                }
            }

            _printSettingsMap[_activeScreen] = printSettings;
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
            if (printSetting == null || value == null || string.IsNullOrEmpty(_activeScreen))
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
                case PrintSettingType.password:
                    UpdatePrintSettings(printSetting, value.ToString());
                    break;
                case PrintSettingType.unknown:
                default:
                    // Do nothing
                    break;
            }

            if (UpdatePreviewEventHandler != null && isPreviewAffected &&
                _activeScreen.Equals(ScreenMode.PrintPreview.ToString()))
            {
                UpdatePreviewEventHandler(printSetting);
            }
        }

        /// <summary>
        /// Updates the print settings list (PrintSettingList) and cache (PrintSettings),
        /// updates value and enabled options based on constraints.
        /// </summary>
        /// <param name="printSetting">source print setting</param>
        /// <param name="value">updated value</param>
        /// <returns>task; true if print preview needs to be refreshed, false otherwise</returns>
        private async Task<bool> UpdatePrintSettings(PrintSetting printSetting, int value)
        {
            bool isPreviewAffected = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isPreviewAffected;
            }

            string name = printSetting.Name;

            if (name.Equals(PrintSettingConstant.NAME_VALUE_COLOR_MODE))
            {
                int prevColorMode = printSettings.ColorMode;
                if (printSettings.ColorMode != value)
                {
                    isPreviewAffected = ((prevColorMode == (int)ColorMode.Mono) ||
                                         (value == (int)ColorMode.Mono));
                    printSettings.ColorMode = value;
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
                    isPreviewAffected = UpdateConstraintsBasedOnOrientation(value, true);
                    printSettings.Orientation = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_DUPLEX))
            {
                if (printSettings.Duplex != value)
                {
                    isPreviewAffected = true;
                    printSettings.Duplex = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PAPER_SIZE))
            {
                if (printSettings.PaperSize != value)
                {
                    isPreviewAffected = true;
                    printSettings.PaperSize = value;
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
                    isPreviewAffected = UpdateConstraintsBasedOnImposition(value, true);
                    printSettings.Imposition = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER))
            {
                if (printSettings.ImpositionOrder != value)
                {
                    isPreviewAffected = (printSettings.Imposition != (int)Imposition.Off);
                    printSettings.ImpositionOrder = value;
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
                    isPreviewAffected = (printSettings.Booklet == true) &&
                        ((printSettings.BookletFinishing == (int)BookletFinishing.FoldAndStaple && value != (int)BookletFinishing.FoldAndStaple) ||
                        (printSettings.BookletFinishing != (int)BookletFinishing.FoldAndStaple && value == (int)BookletFinishing.FoldAndStaple));
                    printSettings.BookletFinishing = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT))
            {
                if (printSettings.BookletLayout != value)
                {
                    isPreviewAffected = (printSettings.Booklet == true);
                    printSettings.BookletLayout = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE))
            {
                if (printSettings.FinishingSide != value)
                {
                    // No need to update preview for finishing side (since punch/staple are not updated also)
                    //isPreviewAffected = UpdateConstraintsBasedOnFinishingSide(value, true);
                    UpdateConstraintsBasedOnFinishingSide(value, true);
                    printSettings.FinishingSide = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_STAPLE))
            {
                if (printSettings.Staple != value)
                {
                    // No need to update preview for staple
                    //isPreviewAffected = (value == (int)Staple.Off) ||
                    //    (value == (int)Staple.OneUpperLeft && printSettings.Staple != (int)Staple.One) ||
                    //    (value == (int)Staple.OneUpperRight && printSettings.Staple != (int)Staple.One) ||
                    //    (value == (int)Staple.One && (printSettings.Staple != (int)Staple.OneUpperLeft && printSettings.Staple != (int)Staple.OneUpperRight)) ||
                    //    (value == (int)Staple.Two);
                    printSettings.Staple = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PUNCH))
            {
                if (printSettings.Punch != value)
                {
                    // No need to update preview for punch
                    //isPreviewAffected = UpdateConstraintsBasedOnPunch(value, true);
                    UpdateConstraintsBasedOnPunch(value, true);
                    printSettings.Punch = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY))
            {
                if (printSettings.OutputTray != value)
                {
                    printSettings.OutputTray = value;
                }
            }

            if (!_printSettingsViewModel.IsPrintPreview)
            {
                await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            }

            if (!string.IsNullOrEmpty(_activeScreen) && _printSettingsMap.ContainsKey(_activeScreen))
            {
                _printSettingsMap[_activeScreen] = printSettings;
            }

            return isPreviewAffected;
        }

        /// <summary>
        /// Updates the print settings list (PrintSettingList) and cache (PrintSettings),
        /// updates value and enabled options based on constraints, and applies
        /// changes to the preview page image.
        /// </summary>
        /// <param name="printSetting">source print setting</param>
        /// <param name="state">updated value</param>
        /// <returns>task; true if print preview needs to be refreshed, false otherwise</returns>
        private async Task<bool> UpdatePrintSettings(PrintSetting printSetting, bool state)
        {
            bool isPreviewAffected = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isPreviewAffected;
            }

            string name = printSetting.Name;

            if (name.Equals(PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT))
            {
                if (printSettings.ScaleToFit != state)
                {
                    isPreviewAffected = true;
                    printSettings.ScaleToFit = state;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET))
            {
                if (printSettings.Booklet != state)
                {
                    isPreviewAffected = UpdateConstraintsBasedOnBooklet(state, true);
                    printSettings.Booklet = state;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_SECURE_PRINT))
            {
                if (printSettings.EnabledSecurePrint != state)
                {
                    UpdateConstraintsBasedOnSecurePrint(state);
                    printSettings.EnabledSecurePrint = state;
                }
            }

            if (!_printSettingsViewModel.IsPrintPreview)
            {
                await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            }

            if (!string.IsNullOrEmpty(_activeScreen) && _printSettingsMap.ContainsKey(_activeScreen))
            {
                _printSettingsMap[_activeScreen] = printSettings;
            }

            return isPreviewAffected;
        }

        /// <summary>
        /// Updates the print settings list (PrintSettingList) and cache (PrintSettings),
        /// updates value and enabled options based on constraints.
        /// </summary>
        /// <param name="printSetting">source print setting</param>
        /// <param name="value">updated value</param>
        private void UpdatePrintSettings(PrintSetting printSetting, string value)
        {
            //bool isPreviewAffected = false;

            PrintSettings printSettings = null;
            _printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                //return isPreviewAffected;
                return;
            }

            string name = printSetting.Name;

            if (name.Equals(PrintSettingConstant.NAME_VALUE_PIN_CODE))
            {
                printSettings.PinCode = (string)value;
            }

            // Note: Enable when needed
            //if (!_printSettingsViewModel.IsPrintPreview)
            //{
            //    await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            //}

            if (!string.IsNullOrEmpty(_activeScreen) && _printSettingsMap.ContainsKey(_activeScreen))
            {
                _printSettingsMap[_activeScreen] = printSettings;
            }

            //return isPreviewAffected;
        }

        #endregion PrintSettingList Operations

        #region Get PrintSettings Properties

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

        #endregion Get PrintSettings Properties

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

        #endregion Utilities

    }
}
