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

        /// <summary>
        /// Print setting value changed delegate
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="value">value</param>
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
        private Printer _printer;
        private PrintSettings _printSettings;
        private PrintSettingList _printSettingList;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static PrintSettingsController() { }

        private PrintSettingsController()
        {
            _printer = null;
            _printSettings = null;
            _printSettingList = null;

            _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;
            _printSettingValueChangedEventHandler = new PrintSettingValueChangedEventHandler(PrintSettingValueChanged);

            // Set PrinterList in PrintSettingsViewModel only once (this will be the original list)
            new ViewModelLocator().PrintSettingsViewModel.PrinterList = PrinterController.Instance.PrinterList;
        }

        /// <summary>
        /// PrintSettingsController singleton instance
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
            PrintSettings currPrintSettings = null;// new PrintSettings();

            if (string.IsNullOrEmpty(screenName) || printer == null)
            {
                return;
            }

            _printSettingsViewModel.PrinterId = printer.Id;
            _printSettingsViewModel.PrinterIpAddress = printer.IpAddress;

            int printSettingId = -1;
            if (printer.PrintSettingId != null)
            {
                printSettingId = printer.PrintSettingId.Value;
            }
            currPrintSettings = await GetPrintSettings(printSettingId);

#if !PRINTSETTING_ORIENTATION
            // Special handling: Set Orientation value based on first page of PDF
            if (screenName.Equals(ScreenMode.PrintPreview.ToString()))
            {
                if (DocumentController.Instance.IsPdfPortrait)
                {
                    currPrintSettings.Orientation = (int)Orientation.Portrait;
                }
                else
                {
                    currPrintSettings.Orientation = (int)Orientation.Landscape;
                }
            }
#endif // !PRINTSETTING_ORIENTATION

            if (screenName.Equals(ScreenMode.PrintPreview.ToString()))
            {
                _prevPinCode = null;
            }

            // Fixed latent bug: printer details is not set before updating the UI
            _printer = printer;
            
            RegisterPrintSettingValueChanged(screenName);

            if (_printSettings != currPrintSettings)
            {
                _printSettings = currPrintSettings;
            }


            LoadPrintSettingsOptions();

            FilterPrintSettingsUsingCapabilities();
            FilterPrintSettingsUsingModel(PrinterModelUtility.GetSeriesTypeFromPrinterName(_printer.Name));
            MergePrintSettings();
            ApplyPrintSettingConstraints();

            _printSettingList = _printSettingsViewModel.PrintSettingsList;
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

                /*
                //should not unload, these should be reused 
                _printer = null;
                _printSettings = null;
                */
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
                // Ignore error
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
        /// <returns>task; print setting ID when successful, -1 otherwise</returns>
        public async Task<int> CreatePrintSettings(Printer printer)
        {
            PrintSettings printSettings = new PrintSettings();

            if (printer != null && printer.Id > -1)
            {
                printSettings.PrinterId = printer.Id;
                bool result = await DatabaseController.Instance.InsertPrintSettings(printSettings);
                // TODO: Check DB error with Add Printer
                //if (!result)
                //{
                //    return -1;
                //}
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
                if (_printer != null)
                {
                    _printSettingsViewModel.PrinterId = _printer.Id;
                    _printSettingsViewModel.PrinterIpAddress = _printer.IpAddress;
                }

                // Refresh Print Settings
                if (_printSettingList != null)
                {
                    if (_printSettingsViewModel.PrintSettingsList != _printSettingList)
                    {
                        _printSettingsViewModel.PrintSettingsList = _printSettingList;
                    }
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

        private static PrintSettingList fullSettings = retrievePrintSettings();
        private PrintSettingList printSettings = null;
        private static List<PrintSettingGroup> authGroup = null;

        /// <summary>
        /// Loads the initial print settings file
        /// </summary>
        private void LoadPrintSettingsOptions()
        {
            // Parse Print Settings XML once
            if (printSettings == null)
            {
                printSettings = retrievePrintSettings();
            }
            _printSettingsViewModel.PrintSettingsList = printSettings;

            // Remove Authentication group for Default Print Settings screen
            if (!_activeScreen.Equals(ScreenMode.PrintPreview.ToString()))
            {
                foreach (PrintSettingGroup g in authGroup)
                {
                    _printSettingsViewModel.PrintSettingsList.Remove(g);
                }
            }            


#if !PRINTSETTING_ORIENTATION
            // Remove Orientation in list
            PrintSetting orientationPrintSetting = GetPrintSetting(PrintSettingConstant.NAME_VALUE_ORIENTATION);
            if (orientationPrintSetting != null)
            {
                RemovePrintSetting(orientationPrintSetting);
            }
#endif // !PRINTSETTING_ORIENTATION

            System.GC.Collect();
        }

        private static PrintSettingList retrievePrintSettings()
        {
            PrintSettingList printSettings = new PrintSettingList();
            List<PrintSettingGroup> mainGroup = null;

            mainGroup = ParseXmlFile(FILE_PATH_ASSET_PRINT_SETTINGS_XML);
            authGroup = ParseXmlFile(FILE_PATH_ASSET_PRINT_SETTINGS_AUTH_XML);

            foreach (PrintSettingGroup g in mainGroup)
            {
                printSettings.Add(g);
            }
            foreach (PrintSettingGroup g in authGroup)
            {
                printSettings.Add(g);
            }

            return printSettings;
        }

        /// <summary>
        /// Reads an XML file and creates PrintSettingGroup
        /// </summary>
        /// <param name="path">XML file relative path from install location</param>
        /// <returns>list of print setting groups</returns>
        private static List<PrintSettingGroup> ParseXmlFile(string path)
        {
            Dictionary<string, List<PrintSettingGroup>> printSettings = new Dictionary<string, List<PrintSettingGroup>>();
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

            PrintSettingGroup group = new PrintSettingGroup();
            PrintSetting fakeSetting = new PrintSetting();
            fakeSetting.Text = "blah";
            group.Name = "Fake Group";
            group.PrintSettings = new List<PrintSetting>();
            group.PrintSettings.Add(fakeSetting);

            //List<PrintSettingGroup> groupList = new List<PrintSettingGroup>();
            //groupList.Add(group);
            //return groupList;
            //printSettings.Add((string)type.Attribute(PrintSettingConstant.KEY_ID), printSettingsData.ToList<PrintSettingGroup>());
            //}
            //List<PrintSettingGroup> groupList = printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
            return printSettingsData.ToList<PrintSettingGroup>();
            //return printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
        }

        /// <summary>
        /// Removes or adds print setting and options depending on printer model
        /// </summary>
        private void FilterPrintSettingsUsingModel(int printerType)
        {
            if (printerType != (int)DirectPrint.SeriesType.FW)
            {
                // Remove Dual Color
                PrintSetting colorModePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_COLOR_MODE);
                if (colorModePrintSetting != null)
                {
                    RemovePrintSettingOption(colorModePrintSetting, (int)ColorMode.DualColor);
                }

                // Remove Rough Paper
                PrintSetting roughPaperPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PAPER_TYPE);
                if (roughPaperPrintSetting != null)
                {
                    RemovePrintSettingOption(roughPaperPrintSetting, (int)PaperType.RoughPaper);
                }

                // Add Tray 3
                PrintSetting inputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_INPUT_TRAY);
                if (inputTrayPrintSetting != null)
                {
                    AddPrintSettingOption(inputTrayPrintSetting, PrintSettingConstant.NAME_VALUE_INPUT_TRAY, (int)InputTray.Tray3);
                }

                if (printerType == (int)DirectPrint.SeriesType.IS)
                {
                    // Remove Legal13, 8K, and 16K 
                    PrintSetting paperSizePrintSetting =
                        GetPrintSetting(PrintSettingConstant.NAME_VALUE_PAPER_SIZE);
                    if (paperSizePrintSetting != null)
                    {
                        RemovePrintSettingOption(paperSizePrintSetting, (int)PaperSize.Legal13);
                        RemovePrintSettingOption(paperSizePrintSetting, (int)PaperSize.EightK);
                        RemovePrintSettingOption(paperSizePrintSetting, (int)PaperSize.SixteenK);
                    }
                }
                else
                {
                    // Add Legal13, 8K, and 16K 
                    PrintSetting paperSizePrintSetting =
                        GetPrintSetting(PrintSettingConstant.NAME_VALUE_PAPER_SIZE);
                    if (paperSizePrintSetting != null)
                    {
                        AddPrintSettingOption(paperSizePrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                            (int)PaperSize.Legal13);
                        AddPrintSettingOption(paperSizePrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                            (int)PaperSize.EightK);
                        AddPrintSettingOption(paperSizePrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                            (int)PaperSize.SixteenK);
                    }
                }
            }
            else
            {
                // Remove Tray 3
                PrintSetting inputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_INPUT_TRAY);
                if (inputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(inputTrayPrintSetting, (int)InputTray.Tray3);
                }

                // Add Dual Color
                PrintSetting colorModePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_COLOR_MODE);
                if (colorModePrintSetting != null)
                {
                    AddPrintSettingOption(colorModePrintSetting, PrintSettingConstant.NAME_VALUE_COLOR_MODE,
                        (int)ColorMode.DualColor);
                }

                // Add Rough Paper
                PrintSetting roughPaperPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PAPER_TYPE);
                if (roughPaperPrintSetting != null)
                {
                    AddPrintSettingOption(roughPaperPrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_TYPE,
                        (int)PaperType.RoughPaper);
                }

                // Add Legal13, 8K, and 16K 
                PrintSetting paperSizePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PAPER_SIZE);
                if (paperSizePrintSetting != null)
                {
                    AddPrintSettingOption(paperSizePrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                            (int)PaperSize.Legal13);
                    AddPrintSettingOption(paperSizePrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                        (int)PaperSize.EightK);
                    AddPrintSettingOption(paperSizePrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                        (int)PaperSize.SixteenK);
                }
            }
        }

        /// <summary>
        /// Removes print setting and options depending on printer capabilities
        /// </summary>
        private void FilterPrintSettingsUsingCapabilities()
        {
            Printer printer = null;
            printer = _printer;
            //_printerMap.TryGetValue(_activeScreen, out printer);
            
            PrintSettings printSettings = null;
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);

            if (printer == null || printSettings == null)
            {
                return;
            }

            // prn_enabled_paper_lw
            PrintSetting paperTypePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PAPER_TYPE);
            if (paperTypePrintSetting != null)
            {
                if (printer.EnabledPaperLW)
                {
                    AddPrintSettingOption(paperTypePrintSetting, PrintSettingConstant.NAME_VALUE_PAPER_TYPE, (int)PaperType.LWPaper);
                }
                else
                {
                    RemovePrintSettingOption(paperTypePrintSetting, (int)PaperType.LWPaper);
                }
                
            }

            // prn_enabled_feed_tray1
            PrintSetting inputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_INPUT_TRAY);
            if (paperTypePrintSetting != null)
            {
                if (printer.EnabledFeedTrayOne)
                {
                    AddPrintSettingOption(inputTrayPrintSetting, PrintSettingConstant.NAME_VALUE_INPUT_TRAY, (int)InputTray.Tray1);
                }
                else
                {
                    RemovePrintSettingOption(inputTrayPrintSetting, (int)InputTray.Tray1);
                }

                if (printer.EnabledFeedTrayTwo)
                {
                    AddPrintSettingOption(inputTrayPrintSetting, PrintSettingConstant.NAME_VALUE_INPUT_TRAY, (int)InputTray.Tray2);
                }
                else
                {
                    RemovePrintSettingOption(inputTrayPrintSetting, (int)InputTray.Tray2);
                }

                if (printer.EnabledFeedTrayThree)
                {
                    AddPrintSettingOption(inputTrayPrintSetting, PrintSettingConstant.NAME_VALUE_INPUT_TRAY, (int)InputTray.Tray3);
                }
                else
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
            else
            {
                AddPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
            }

            // prn_enabled_stapler
            PrintSetting staplePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
            if (staplePrintSetting != null)
            {
                if (!printer.EnabledStapler)
                {
                    RemovePrintSetting(staplePrintSetting);

                    PrintSetting bookletFinishPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
                    if (bookletFinishPrintSetting != null)
                    {
                        RemovePrintSettingOption(bookletFinishPrintSetting, (int)BookletFinishing.FoldAndStaple);
                    }
                }
                else
                {
                    AddPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
                }
            }

            if (!printer.EnabledPunchThree && !printer.EnabledPunchFour) // No punch
            {
                PrintSetting punchPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
                if (punchPrintSetting != null)
                {
                    RemovePrintSetting(punchPrintSetting);
                }
            }
            else
            {
                PrintSetting punchPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
                if (punchPrintSetting != null)
                {
                    // prn_enabled_punch3
                    if (printer.EnabledPunchThree)
                    {
                        AddPrintSettingOption(punchPrintSetting, PrintSettingConstant.NAME_VALUE_PUNCH, (int)Punch.ThreeHoles);
                    }
                    else
                    {
                        RemovePrintSettingOption(punchPrintSetting, (int)Punch.ThreeHoles);
                    }

                    // prn_enabled_punch4
                    if (printer.EnabledPunchFour)
                    {
                        AddPrintSettingOption(punchPrintSetting, PrintSettingConstant.NAME_VALUE_PUNCH, (int)Punch.FourHoles);
                    }
                    else
                    {
                        RemovePrintSettingOption(punchPrintSetting, (int)Punch.FourHoles);
                    }
                }
            }

            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
            if (outputTrayPrintSetting != null)
            {
                // prn_enabled_tray_facedown
                if (printer.EnabledTrayFacedown)
                {
                     AddPrintSettingOption(outputTrayPrintSetting, PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY, (int)OutputTray.FaceDown);
                }
                else
                {
                     RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                }

                // prn_enabled_tray_top
                if (printer.EnabledTrayTop)
                {
                    AddPrintSettingOption(outputTrayPrintSetting, PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY, (int)OutputTray.Top);
                }
                else
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Top);
                }
                // prn_enabled_tray_stack
                if (printer.EnabledTrayStack)
                {
                    AddPrintSettingOption(outputTrayPrintSetting, PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY, (int)OutputTray.Stacking);
                }
                else
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Stacking);
                }
            }

            _printSettings = printSettings;
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
        /// Adds a print setting from the group
        /// </summary>
        /// <param name="printSetting">print setting</param>
        private void AddPrintSetting(string name)
        {
            var query = fullSettings
                .SelectMany(printSettingGroup => printSettingGroup.PrintSettings)
                .Where(ps => ps.Name == name);
            PrintSetting setting = query.FirstOrDefault();
            PrintSettingGroup settingGroup = fullSettings
                .FirstOrDefault(group => group.PrintSettings.Contains(setting));

            var query2 = _printSettingsViewModel.PrintSettingsList
                .FirstOrDefault(group2 => group2.Name.Equals(settingGroup.Name));
            if (query2 != null)
            {
                query2.PrintSettings.Add(setting);
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
        /// Add a print setting option
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="index">option index</param>
        private void AddPrintSettingOption(PrintSetting printSetting, string name, int index)
        {
            PrintSettingOption printSettingOption = printSetting.Options
                .FirstOrDefault(setting => setting.Index == index);
            if (printSettingOption == null)
            {
                var query = fullSettings
                    .SelectMany(printSettingGroup => printSettingGroup.PrintSettings)
                    .Where(ps => ps.Name == name);

                PrintSetting printSettingFull = query.FirstOrDefault();

                PrintSettingOption printSettingOptionFromFull = printSettingFull.Options
                    .FirstOrDefault(setting => setting.Index == index);

                printSettingOption = new PrintSettingOption();
                printSettingOption.Text = printSettingOptionFromFull.Text;
                printSettingOption.Index = index;
                printSettingOption.IsEnabled = true;
                printSetting.Options.Add(printSettingOption);
            }
        }

        /// <summary>
        /// Merges print settings cache (PrintSettings) to print settings list (PrintSettingList)
        /// to reflect actual values from database
        /// </summary>
        private void MergePrintSettings()
        {
            PrintSettings printSettings = _printSettings;
            if (printSettings != null)
            //if (_printSettingsMap.TryGetValue(_activeScreen, out printSettings))
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
        /// This function is called on first display of Print Settings pane
        /// </summary>
        private void ApplyPrintSettingConstraints()
        {
            PrintSettings printSettings;
            printSettings = _printSettings;
            if (printSettings != null)
            //if (_printSettingsMap.TryGetValue(_activeScreen, out printSettings))
            {
                // Note: The following order must be checked when constraints specifications is updated
                UpdateConstraintsBasedOnBooklet(printSettings.Booklet, false);
                UpdateConstraintsBasedOnOrientation(printSettings.Orientation, false);
                UpdateConstraintsBasedOnImposition(printSettings.Imposition, false);
                UpdateConstraintsBasedOnFinishingSide(printSettings.FinishingSide, false);
                UpdateConstraintsBasedOnPunch(printSettings.Punch, false);
                UpdateConstraintsBasedOnBookletFinishing(printSettings.BookletFinishing, false);
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
            printSettings = _printSettings;
            //if (_printSettingsMap.TryGetValue(_activeScreen, out printSettings))
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

            _printSettings = printSettings;
            //_printSettingsMap[_activeScreen] = printSettings;

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
            printSettings = _printSettings;
            //if (_printSettingsMap.TryGetValue(_activeScreen, out printSettings))
            if (printSettings == null)
            {
                return isValueUpdated;
            }

            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);
            PrintSetting bookletPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET);

            bool currBooklet = printSettings.Booklet;

            if (value == (int)Imposition.Off)
            {
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    if (currBooklet)
                    {
                        impositionOrderPrintSetting.IsValueDisplayed = true;
                    }
                    else
                    {
                        impositionOrderPrintSetting.IsValueDisplayed = false;
                    }
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
                if (bookletPrintSetting != null)
                {
                    if (updateValues)
                    {
                        bool newBookletValue = false;
                        bookletPrintSetting.Value = newBookletValue;
                        printSettings.Booklet = newBookletValue;
                    }
                }
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
                if (bookletPrintSetting != null)
                {
                    if (updateValues)
                    {
                        bool newBookletValue = false;
                        //UpdateConstraintsBasedOnBooklet(newBookletValue, true);
                        bookletPrintSetting.Value = newBookletValue;
                        printSettings.Booklet = newBookletValue;
                    }
                }
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

            _printSettings = printSettings;
            //_printSettingsMap[_activeScreen] = printSettings;

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
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings))
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
            int currImposition = printSettings.Imposition;

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
                    if (updateValues)
                    {
                        int newImpositonValue = (int)Imposition.Off;
                        impositionPrintSetting.Value = newImpositonValue;
                        printSettings.Imposition = newImpositonValue;
                    }
                }
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    impositionOrderPrintSetting.IsValueDisplayed = true;
                    if (updateValues)
                    {
                        impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                        printSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;
                    }
                }
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = false;
                    if (updateValues)
                    {
                        finishingSidePrintSetting.Value = (int)FinishingSide.Left;
                        printSettings.FinishingSide = (int)FinishingSide.Left;
                    }
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = false;
                    if (updateValues)
                    {
                        staplePrintSetting.Value = (int)Staple.Off;
                        printSettings.Staple = (int)Staple.Off;
                    }
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = false;
                    if (updateValues)
                    {
                        punchPrintSetting.Value = (int)Punch.Off;
                        printSettings.Punch = (int)Punch.Off;
                    }
                }
                
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = true;
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
                if (impositionOrderPrintSetting != null)
                {
                    if (currImposition == (int)Imposition.Off)
                    {
                        impositionOrderPrintSetting.IsValueDisplayed = false;
                    }
                    else
                    {
                        impositionOrderPrintSetting.IsValueDisplayed = true;
                    }
                }
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = true;
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = true;
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = true;
                }
                
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    if (updateValues)
                    {
                        bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                        printSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;
                    }
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    if (updateValues)
                    {
                        bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                        printSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;
                    }
                }
                isValueUpdated = true;
            }

            _printSettings = printSettings;
            //_printSettingsMap[_activeScreen] = printSettings;

            return isValueUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on booklet finishing constraints
        /// </summary>
        /// <param name="value">new booklet finishing value</param>
        /// <param name="updateValues">true when dependent values should be updated, false otherwise</param>
        /// <returns>true when dependent values are updated, false otherwise</returns>
        private bool UpdateConstraintsBasedOnBookletFinishing(int value, bool updateValues)
        {
            bool isValueUpdated = false;

            PrintSettings printSettings = null;
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isValueUpdated;
            }

            PrintSetting outputTrayPrintSettings =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            if (value != (int)BookletFinishing.Off)
            {
                if (outputTrayPrintSettings != null)
                {
                    if (updateValues)
                    {
                        outputTrayPrintSettings.Value = (int)OutputTray.Auto;
                        printSettings.OutputTray = (int)OutputTray.Auto;
                    }

                    PrintSettingOption outputTrayFaceDown = GetPrintSettingOption(outputTrayPrintSettings, (int)OutputTray.FaceDown);
                    if (outputTrayFaceDown != null)
                    {
                        outputTrayFaceDown.IsEnabled = false;
                    }

                    PrintSettingOption outputTrayTop = GetPrintSettingOption(outputTrayPrintSettings, (int)OutputTray.Top);
                    if (outputTrayTop != null)
                    {
                        outputTrayTop.IsEnabled = false;
                    }

                    PrintSettingOption outputTrayStacking = GetPrintSettingOption(outputTrayPrintSettings, (int)OutputTray.Stacking);
                    if (outputTrayStacking != null)
                    {
                        outputTrayStacking.IsEnabled = false;
                    }
                }

                isValueUpdated = true;
            }
            else
            {
                if (outputTrayPrintSettings != null)
                {
                    PrintSettingOption outputTrayFaceDown = GetPrintSettingOption(outputTrayPrintSettings, (int)OutputTray.FaceDown);
                    if (outputTrayFaceDown != null)
                    {
                        outputTrayFaceDown.IsEnabled = true;
                    }

                    PrintSettingOption outputTrayTop = GetPrintSettingOption(outputTrayPrintSettings, (int)OutputTray.Top);
                    if (outputTrayTop != null)
                    {
                        outputTrayTop.IsEnabled = true;
                    }

                    PrintSettingOption outputTrayStacking = GetPrintSettingOption(outputTrayPrintSettings, (int)OutputTray.Stacking);
                    if (outputTrayStacking != null)
                    {
                        outputTrayStacking.IsEnabled = true;
                    }
                }

                isValueUpdated = true;
            }



            return isValueUpdated;
        }


        /// <summary>
        /// Updates print settings dependent on finishing side constraints
        /// </summary>
        /// <param name="value">new finishing side value</param>
        /// <returns>true when dependent values are updated, false otherwise</returns>
        private bool UpdateConstraintsBasedOnFinishingSide(int value, bool updateValues)
        {
            bool isValueUpdated = false;

            PrintSettings printSettings = null;
            printSettings = _printSettings;            
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);
            if (printSettings == null)
            {
                return isValueUpdated;
            }

            PrintSetting staplePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
            PrintSetting punchPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);

            int currOrientation = printSettings.Orientation;
            int currFinishingSide = printSettings.FinishingSide;
            int currStaple = printSettings.Staple;
            int currPunch = printSettings.Punch;

            if (value == (int)FinishingSide.Left || value == (int)FinishingSide.Right)
            {
                //if (currOrientation == (int)Orientation.Landscape && currPunch == (int)Punch.FourHoles)
                //{
                //    if (punchPrintSetting != null)
                //    {
                //        int newPunch = (int)punchPrintSetting.Default;
                //        if (updateValues)
                //        {
                //            punchPrintSetting.Value = newPunch;
                //            printSettings.Punch = newPunch;
                //        }
                //    }
                //}

                if (staplePrintSetting != null)
                {
                    if (updateValues && (currStaple == (int)Staple.OneUpperLeft || currStaple == (int)Staple.OneUpperRight))
                    {
                        staplePrintSetting.Value = (int)Staple.One;
                        printSettings.Staple = (int)Staple.One;
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

                isValueUpdated = true;
            }
            else if (value == (int)FinishingSide.Top)
            {
                //if (currOrientation == (int)Orientation.Portrait && currPunch == (int)Punch.FourHoles)
                //{
                //    if (punchPrintSetting != null)
                //    {
                //        int newPunch = (int)punchPrintSetting.Default;
                //        if (updateValues)
                //        {
                //            punchPrintSetting.Value = newPunch;
                //            printSettings.Punch = newPunch;
                //        }
                //    }
                //}

                if (staplePrintSetting != null)
                {
                    if (updateValues)
                    {
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

                isValueUpdated = true;
            }

            _printSettings = printSettings;
            //_printSettingsMap[_activeScreen] = printSettings;

            return isValueUpdated;
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
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);
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
                //if (currOrientation == (int)Orientation.Portrait &&
                //currFinishingSide == (int)FinishingSide.Top)
                //{
                //    if (finishingSidePrintSetting != null)
                //    {
                //        int newFinishingSide = (int)finishingSidePrintSetting.Default; // Left
                //        if (updateValues)
                //        {
                //            finishingSidePrintSetting.Value = newFinishingSide;
                //            printSettings.FinishingSide = newFinishingSide;
                //        }
                //    }
                //}
                //else if (currOrientation == (int)Orientation.Landscape &&
                //    currFinishingSide != (int)FinishingSide.Top)
                //{
                //    if (finishingSidePrintSetting != null)
                //    {
                //        int newFinishingSide = (int)FinishingSide.Top;
                //        if (updateValues)
                //        {
                //            finishingSidePrintSetting.Value = newFinishingSide;
                //            printSettings.FinishingSide = newFinishingSide;
                //        }
                //    }
                //}

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

            _printSettings = printSettings;
            //_printSettingsMap[_activeScreen] = printSettings;

            return isValueUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on secure print constraints
        /// </summary>
        /// <param name="value">new scure print value</param>
        private void UpdateConstraintsBasedOnSecurePrint(bool value)
        {
            PrintSettings printSettings = null;
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);
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
                    _prevPinCode = (string.IsNullOrEmpty(printSettings.PinCode) ? string.Empty : printSettings.PinCode);
                    printSettings.PinCode = null;
                    pinCodePrintSetting.Value = string.Empty;
                }
            }

            _printSettings = printSettings;
            //_printSettingsMap[_activeScreen] = printSettings;
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
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);
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
#if PREVIEW_STAPLE
                    isPreviewAffected = (printSettings.Booklet == true) &&
                        ((printSettings.BookletFinishing == (int)BookletFinishing.FoldAndStaple && value != (int)BookletFinishing.FoldAndStaple) ||
                        (printSettings.BookletFinishing != (int)BookletFinishing.FoldAndStaple && value == (int)BookletFinishing.FoldAndStaple));
#endif // PREVIEW_STAPLE
                    printSettings.BookletFinishing = value;
                    UpdateConstraintsBasedOnBookletFinishing(value, true);
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
                    isPreviewAffected = UpdateConstraintsBasedOnFinishingSide(value, true);
                    printSettings.FinishingSide = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_STAPLE))
            {
                if (printSettings.Staple != value)
                {
#if PREVIEW_STAPLE
                    isPreviewAffected = (value == (int)Staple.Off) ||
                        (value == (int)Staple.OneUpperLeft && printSettings.Staple != (int)Staple.One) ||
                        (value == (int)Staple.OneUpperRight && printSettings.Staple != (int)Staple.One) ||
                        (value == (int)Staple.One && (printSettings.Staple != (int)Staple.OneUpperLeft && printSettings.Staple != (int)Staple.OneUpperRight)) ||
                        (value == (int)Staple.Two);
#endif // PREVIEW_STAPLE
                    printSettings.Staple = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PUNCH))
            {
                if (printSettings.Punch != value)
                {
#if PREVIEW_PUNCH
                    isPreviewAffected =
#endif // PREVIEW_PUNCH
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
                // Ignore error
            }

            //if (!string.IsNullOrEmpty(_activeScreen) && _printSettingsMap.ContainsKey(_activeScreen))
            {
                _printSettings = printSettings;
                //_printSettingsMap[_activeScreen] = printSettings;
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
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);
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
                // Ignore error
            }

            //if (!string.IsNullOrEmpty(_activeScreen) && _printSettingsMap.ContainsKey(_activeScreen))
            {
                _printSettings = printSettings;
                //_printSettingsMap[_activeScreen] = printSettings;
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
            printSettings = _printSettings;
            //_printSettingsMap.TryGetValue(_activeScreen, out printSettings);
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

            //if (!string.IsNullOrEmpty(_activeScreen) && _printSettingsMap.ContainsKey(_activeScreen))
            {
                _printSettings = printSettings;
                //_printSettingsMap[_activeScreen] = printSettings;
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
            //if (!string.IsNullOrEmpty(screenName) && _printSettingsMap.ContainsKey(screenName))
            {
                printSettings = _printSettings;
                //printSettings = _printSettingsMap[screenName];
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
