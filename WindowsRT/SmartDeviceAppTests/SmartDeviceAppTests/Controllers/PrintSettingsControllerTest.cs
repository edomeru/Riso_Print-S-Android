using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using System.Threading.Tasks;
using SQLite;
using SmartDeviceApp.ViewModels;
using Windows.Storage;
using System.IO;
using SmartDeviceAppTests.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Constants;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class PrintSettingsControllerTest
    {

        private const string FILE_NAME_DATABASE = "SmartDeviceAppDB.db";
        private const string KEY_ISSAMPLEDATAALREADYLOADED = "IsSampleDataAlreadyLoaded";

        private SQLiteAsyncConnection _dbConnection;
        private PrintSettingsViewModel _jobsViewModel;
        private string _screenName;

        // Cover Unit Tests using dotCover does not call TestInitialize
        //[TestInitialize]
        //public async Task Initialize()
        private async Task Initialize()
        {
            _jobsViewModel = new ViewModelLocator().PrintSettingsViewModel;

            string databasePath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            _dbConnection = new SQLite.SQLiteAsyncConnection(databasePath);

            await UnitTestUtility.DropAllTables(_dbConnection);

            // Initilize database
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values[KEY_ISSAMPLEDATAALREADYLOADED] = true; // avoid loading of sample data
            await DatabaseController.Instance.Initialize();
        }

        // Cover Unit Tests using dotCover does not call TestCleanup
        //[TestCleanup]
        //public void Cleanup()
        private void Cleanup()
        {
            //PrintSettingsController.Instance.Uninitialize(_screenName);
            _screenName = null;
            //DatabaseController.Instance.Cleanup(); // This should be ideal however since events (target methods) are not awaitable, we cannot reset connections
        }

        [TestMethod]
        public async Task Test_Initialize_NullScreenName()
        {
            _screenName = null;
            await PrintSettingsController.Instance.Initialize(_screenName, new Printer());
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNull(printSettings);
        }

        [TestMethod]
        public async Task Test_Initialize_EmptyScreenName()
        {
            _screenName = string.Empty;
            await PrintSettingsController.Instance.Initialize(_screenName, new Printer());
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNull(printSettings);
        }

        [TestMethod]
        public async Task Test_Initialize_NullPrinter()
        {
            _screenName = "screen";
            await PrintSettingsController.Instance.Initialize(_screenName, null);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNull(printSettings);
        }

        [TestMethod]
        public async Task Test_Initialize_MapNewEntry_FullPrinterCapabilities()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(2, printSettings.Id);
            Assert.AreEqual(2, printSettings.PrinterId);
            Assert.AreEqual(1, printSettings.ColorMode);
            Assert.AreEqual(0, printSettings.Orientation);
            Assert.AreEqual(1, printSettings.Copies);
            Assert.AreEqual(0, printSettings.Duplex);
            Assert.AreEqual(2, printSettings.PaperSize);
            Assert.IsTrue(printSettings.ScaleToFit);
            Assert.AreEqual(0, printSettings.PaperType);
            Assert.AreEqual(0, printSettings.InputTray);
            Assert.AreEqual(0, printSettings.Imposition);
            Assert.AreEqual(0, printSettings.ImpositionOrder);
            Assert.AreEqual(0, printSettings.Sort);
            Assert.IsFalse(printSettings.Booklet);
            Assert.AreEqual(0, printSettings.BookletFinishing);
            Assert.AreEqual(0, printSettings.BookletLayout);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual(0, printSettings.Staple);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreEqual(0, printSettings.OutputTray);
            Assert.IsNull(printSettings.LoginId);
            Assert.IsNull(printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Initialize_MapExistingEntry()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer prevPrinter = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, prevPrinter);
            Printer newPrinter = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, newPrinter);
            PrintSettings newPrintSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNotNull(newPrintSettings);
            Assert.AreEqual(1, newPrintSettings.Id);
            Assert.AreEqual(1, newPrintSettings.PrinterId);
            Assert.AreEqual(0, newPrintSettings.ColorMode);
            Assert.AreEqual(1, newPrintSettings.Orientation);
            Assert.AreEqual(2, newPrintSettings.Copies);
            Assert.AreEqual(0, newPrintSettings.Duplex);
            Assert.AreEqual(1, newPrintSettings.PaperSize);
            Assert.IsFalse(newPrintSettings.ScaleToFit);
            Assert.AreEqual(1, newPrintSettings.PaperType);
            Assert.AreEqual(1, newPrintSettings.InputTray);
            Assert.AreEqual(0, newPrintSettings.Imposition);
            Assert.AreEqual(0, newPrintSettings.ImpositionOrder);
            Assert.AreEqual(1, newPrintSettings.Sort);
            Assert.IsFalse(newPrintSettings.Booklet);
            Assert.AreEqual(0, newPrintSettings.BookletFinishing);
            Assert.AreEqual(0, newPrintSettings.BookletLayout);
            Assert.AreEqual(0, newPrintSettings.FinishingSide);
            Assert.AreEqual(0, newPrintSettings.Staple);
            Assert.AreEqual(0, newPrintSettings.Punch);
            Assert.AreEqual(0, newPrintSettings.OutputTray);
            Assert.IsNull(newPrintSettings.LoginId);
            Assert.IsNull(newPrintSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Initialize_NoPrinterCapabilities()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(5);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(5, printSettings.Id);
            Assert.AreEqual(5, printSettings.PrinterId);
            Assert.AreEqual(1, printSettings.ColorMode);
            Assert.AreEqual(1, printSettings.Orientation);
            Assert.AreEqual(1, printSettings.Copies);
            Assert.AreEqual(0, printSettings.Duplex);
            Assert.AreEqual(2, printSettings.PaperSize);
            Assert.IsTrue(printSettings.ScaleToFit);
            Assert.AreEqual(0, printSettings.PaperType);
            Assert.AreEqual(0, printSettings.InputTray);
            Assert.AreEqual(1, printSettings.Imposition);
            Assert.AreEqual(1, printSettings.ImpositionOrder);
            Assert.AreEqual(0, printSettings.Sort);
            Assert.IsFalse(printSettings.Booklet);
            Assert.AreEqual(0, printSettings.BookletFinishing);
            Assert.AreEqual(0, printSettings.BookletLayout);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual(2, printSettings.Staple);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreEqual(1, printSettings.OutputTray);
            Assert.IsNull(printSettings.LoginId);
            Assert.IsNull(printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Initialize_NoPrintSettings()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(10);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(-1, printSettings.Id);
            Assert.AreEqual(-1, printSettings.PrinterId);
            Assert.AreEqual(0, printSettings.ColorMode);
            Assert.AreEqual(0, printSettings.Orientation);
            Assert.AreEqual(1, printSettings.Copies);
            Assert.AreEqual(0, printSettings.Duplex);
            Assert.AreEqual(2, printSettings.PaperSize);
            Assert.IsFalse(printSettings.ScaleToFit);
            Assert.AreEqual(0, printSettings.PaperType);
            Assert.AreEqual(0, printSettings.InputTray);
            Assert.AreEqual(0, printSettings.Imposition);
            Assert.AreEqual(0, printSettings.ImpositionOrder);
            Assert.AreEqual(0, printSettings.Sort);
            Assert.IsFalse(printSettings.Booklet);
            Assert.AreEqual(0, printSettings.BookletFinishing);
            Assert.AreEqual(0, printSettings.BookletLayout);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual(0, printSettings.Staple);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreEqual(0, printSettings.OutputTray);
            Assert.IsNull(printSettings.LoginId);
            Assert.IsNull(printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_Uninitialize_Null()
        {
            PrintSettingsController.Instance.Uninitialize(null);
            // Note: no public properties or retrun value to assert
        }

        [TestMethod]
        public void Test_Uninitialize_Empty()
        {
            PrintSettingsController.Instance.Uninitialize(string.Empty);
            // Note: no public properties or retrun value to assert
        }

        [TestMethod]
        public async Task Test_Uninitialize_Exists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSettingsController.Instance.Uninitialize(_screenName);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Uninitialize_NotExists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSettingsController.Instance.Uninitialize("random");

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemovePrintSettings_Null()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            PrintSettingsController.Instance.RemovePrintSettings(null);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemovePrintSettings_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            PrintSettingsController.Instance.RemovePrintSettings(printer);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemovePrintSettings_Invalid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            Printer printer = await _dbConnection.GetAsync<Printer>(10);
            PrintSettingsController.Instance.RemovePrintSettings(printer);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_CreatePrintSettings_Null()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            int result = await PrintSettingsController.Instance.CreatePrintSettings(null);
            Assert.AreEqual(-1, result);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_CreatePrintSettings_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            Printer printer = new Printer() { Id = 2, IpAddress = "172.0.0.1", Name = "new printer" };
            printer.PrintSettingId = await PrintSettingsController.Instance.CreatePrintSettings(printer);
            Assert.AreEqual(2, printer.PrintSettingId);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_CreatePrintSettings_Invalid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            Printer printer = new Printer() { IpAddress = "172.0.0.1", Name = "new printer" };
            printer.PrintSettingId = await PrintSettingsController.Instance.CreatePrintSettings(printer);
            Assert.AreEqual(-1, printer.PrintSettingId);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_RegisterPrintSettingValueChanged_Null()
        {
            PrintSettingsController.Instance.RegisterPrintSettingValueChanged(null);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_RegisterPrintSettingValueChanged_Empty()
        {
            PrintSettingsController.Instance.RegisterPrintSettingValueChanged(string.Empty);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_RegisterPrintSettingValueChanged_NotExists()
        {
            PrintSettingsController.Instance.RegisterPrintSettingValueChanged("screen");
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public async Task Test_RegisterPrintSettingValueChanged_Exists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSettingsController.Instance.RegisterPrintSettingValueChanged(_screenName);
            // Note: no public properties or return value to assert

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_UnregisterPrintSettingValueChanged_Null()
        {
            PrintSettingsController.Instance.UnregisterPrintSettingValueChanged(null);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_UnregisterPrintSettingValueChanged_Empty()
        {
            PrintSettingsController.Instance.UnregisterPrintSettingValueChanged(string.Empty);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_UnregisterPrintSettingValueChanged_NotExists()
        {
            PrintSettingsController.Instance.UnregisterPrintSettingValueChanged("screen");
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public async Task Test_UnregisterPrintSettingValueChanged_Exists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSettingsController.Instance.UnregisterPrintSettingValueChanged(_screenName);
            // Note: no public properties or return value to assert

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_RegisterUpdatePreviewEventHandler()
        {
            PrintSettingsController.Instance.RegisterUpdatePreviewEventHandler(MockUpdatePreviewEventHandler);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_UnregisterUpdatePreviewEventHandler()
        {
            PrintSettingsController.Instance.UnregisterUpdatePreviewEventHandler(MockUpdatePreviewEventHandler);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_PrintSettingValueChanged_NullPrintSetting()
        {
            PrintSettingsController.Instance.PrintSettingValueChanged(null, 1);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_PrintSettingValueChanged_NullValue()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_COLOR_MODE,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 1
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, null);
            // Note: no public properties or return value to assert
        }

        //[TestMethod]
        //public void Test_PrintSettingValueChanged_NotNull()
        //{
        //    PrintSetting printSetting = new PrintSetting()
        //    {
        //        Name = PrintSettingConstant.NAME_VALUE_COLOR_MODE,
        //        Type = PrintSettingType.numeric,
        //        Value = 2,
        //        Default = 1
        //    };

        //    PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
        //    // Note: no public properties or return value to assert
        //}

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_UpdatePreview()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(6);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);
            PrintSettingsController.Instance.RegisterUpdatePreviewEventHandler(MockUpdatePreviewEventHandler);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Imposition);

           Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }


        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_ColorMode()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_COLOR_MODE,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 1
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.ColorMode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromPortraitPunchFour()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Orientation);
            Assert.AreEqual((int)Punch.Off, printSettings.Punch);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromLandscapePunchFour()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Orientation);
            Assert.AreEqual((int)Punch.FourHoles, printSettings.Punch);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromPortraitPunchOther()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(3);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Orientation);
            Assert.AreEqual((int)Punch.TwoHoles, printSettings.Punch);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromLandscapePunchOther()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(4);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Orientation);
            Assert.AreEqual((int)Punch.TwoHoles, printSettings.Punch);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromPortraitBookletLayoutLR()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(5);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Orientation);
            Assert.AreEqual((int)BookletLayout.TopToBottom, printSettings.BookletLayout);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromLandscapeBookletLayoutLR()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(6);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Orientation);
            Assert.AreEqual((int)BookletLayout.LeftToRight, printSettings.BookletLayout);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }
        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromPortraitBookletLayoutTB()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(7);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Orientation);
            Assert.AreEqual((int)BookletLayout.TopToBottom, printSettings.BookletLayout);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Orientation_FromLandscapeBookletLayoutTB()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_orientation.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(8);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Orientation);
            Assert.AreEqual((int)BookletLayout.TopToBottom, printSettings.BookletLayout);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Copies()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_COPIES,
                Type = PrintSettingType.numeric,
                Value = 100,
                Default = 1
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 100);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(100, printSettings.Copies);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Duplex()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_DUPLEX,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Duplex);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_PaperSize()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                Type = PrintSettingType.numeric,
                Value = 5,
                Default = 2
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 5);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(5, printSettings.PaperSize);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Bool_ScaleToFit_FromTrue()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT,
                Type = PrintSettingType.boolean,
                Value = false,
                Default = true
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, false);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsFalse(printSettings.ScaleToFit);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_PaperType()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PAPER_TYPE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.PaperType);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_InputTray()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_INPUT_TRAY,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.InputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromOff_ToTwoUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpLeftToRight, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromOff_ToFourUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.FourUpUpperLeftToRight, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromTwoUpLeft_ToFourUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.FourUpUpperLeftToRight, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromTwoUpRight_ToFourUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(3);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.FourUpUpperRightToLeft, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }


        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromTwoUpRight_ToOff()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(3);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpLeftToRight, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromFourUpLeftToRight_ToTwoUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(4);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpLeftToRight, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromFourUpRightToLeft_ToTwoUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(5);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpRightToLeft, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromFourUpLeftToBottom_ToTwoUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(6);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpLeftToRight, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromFourUpRightToBottom_ToTwoUp()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(7);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpRightToLeft, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Imposition_FromFourUpRightToBottom_ToOff()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(7);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpLeftToRight, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_ImpositionOrder()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.ImpositionOrder);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Sort()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SORT,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Sort);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Bool_Booklet_FromFalse()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_booklet.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_BOOKLET,
                Type = PrintSettingType.boolean,
                Value = true,
                Default = false
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsTrue(printSettings.Booklet);
            Assert.AreEqual((int)Duplex.ShortEdge, printSettings.Duplex);
            Assert.AreEqual((int)Imposition.Off, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpLeftToRight, printSettings.ImpositionOrder);
            Assert.AreEqual((int)FinishingSide.Left, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.Off, printSettings.Staple);
            Assert.AreEqual((int)Punch.Off, printSettings.Punch);
            Assert.AreEqual((int)OutputTray.Auto, printSettings.OutputTray);
            Assert.AreEqual((int)BookletLayout.LeftToRight, printSettings.BookletLayout);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Bool_Booklet_FromTrue()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_booklet.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_BOOKLET,
                Type = PrintSettingType.boolean,
                Value = false,
                Default = false
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, false);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsFalse(printSettings.Booklet);
            Assert.AreEqual((int)Duplex.Off, printSettings.Duplex);
            Assert.AreEqual((int)Imposition.Off, printSettings.Imposition);
            Assert.AreEqual((int)ImpositionOrder.TwoUpLeftToRight, printSettings.ImpositionOrder);
            Assert.AreEqual((int)FinishingSide.Left, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.Off, printSettings.Staple);
            Assert.AreEqual((int)Punch.Off, printSettings.Punch);
            Assert.AreEqual((int)OutputTray.Auto, printSettings.OutputTray);
            Assert.AreEqual((int)BookletLayout.LeftToRight, printSettings.BookletLayout);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_BookletFinishing()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_booklet.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.BookletFinishing);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_BookletLayout()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_booklet.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.BookletLayout);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromLeftStapleOff()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.OneUpperLeft, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromLeftStapleOn()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.OneUpperLeft, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromTopStapleOff()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(3);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.Off, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromTopStapleOnLeft_ToLeft()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(4);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.One, printSettings.Staple);

           Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromTopStapleOnLeft_ToRight()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(4);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.One, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromTopStapleOnRight_ToLeft()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(5);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.One, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromTopStapleOnRight_ToRight()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(5);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.One, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromTopStapleTwo_ToLeft()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(6);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.Two, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromTopStapleTwo_ToRight()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(6);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.Two, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromRightStapleOff()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(7);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.Off, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromRightStapleOn_ToLeft()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(8);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.One, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_FinishingSide_FromRightStapleOn_ToRight()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_finishingside.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(8);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(2, printSettings.FinishingSide);
            Assert.AreEqual((int)Staple.One, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Staple()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_STAPLE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Staple);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Punch_FromOffOutputTrayAuto()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_punch.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PUNCH,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Punch);
            Assert.AreEqual((int)OutputTray.Auto, printSettings.OutputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Punch_FromOffOutputTrayOthers()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_punch.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PUNCH,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.Punch);
            Assert.AreNotEqual((int)OutputTray.Auto, printSettings.OutputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Punch_FromTwoHolesOutputTrayAuto()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_punch.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(3);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PUNCH,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreEqual((int)OutputTray.Auto, printSettings.OutputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Punch_FromTwoHolesOutputTrayOthers()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_punch.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(4);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PUNCH,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreNotEqual((int)OutputTray.Auto, printSettings.OutputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Punch_FromFourHolesOutputTrayAuto()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_punch.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(5);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PUNCH,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreEqual((int)OutputTray.Auto, printSettings.OutputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_Punch_FromFourHolesOutputTrayOthers()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_punch.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(6);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PUNCH,
                Type = PrintSettingType.numeric,
                Value = 0,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 0);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreNotEqual((int)OutputTray.Auto, printSettings.OutputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Int_OutputTray()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.Printers.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual(1, printSettings.OutputTray);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Bool_SecurePrint_FromFalse_WithoutPinCode()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SECURE_PRINT,
                Type = PrintSettingType.boolean,
                Value = true,
                Default = false
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsTrue(printSettings.EnabledSecurePrint);
            Assert.IsNull(printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Bool_SecurePrint_FromTrue_WithoutPinCode()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SECURE_PRINT,
                Type = PrintSettingType.boolean,
                Value = true,
                Default = false
            };
            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);
            printSetting.Value = false;

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, false);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsFalse(printSettings.EnabledSecurePrint);
            Assert.IsNull(printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Bool_SecurePrint_FromFalse_WithPinCode()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SECURE_PRINT,
                Type = PrintSettingType.boolean,
                Value = true,
                Default = false
            };
            PrintSetting pinCodePrintSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PIN_CODE,
                Type = PrintSettingType.password,
                Value = "",
                Default = ""
            };
            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);
            PrintSettingsController.Instance.PrintSettingValueChanged(pinCodePrintSetting, "1234");
            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, false);

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsTrue(printSettings.EnabledSecurePrint);
            Assert.AreEqual("1234", printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_Bool_SecurePrint_FromTrue_WithPinCode()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SECURE_PRINT,
                Type = PrintSettingType.boolean,
                Value = true,
                Default = false
            };
            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);
            PrintSetting pinCodePrintSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PIN_CODE,
                Type = PrintSettingType.password,
                Value = "",
                Default = ""
            };
            PrintSettingsController.Instance.PrintSettingValueChanged(pinCodePrintSetting, "1234");
            printSetting.Value = false;

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, false);
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsFalse(printSettings.EnabledSecurePrint);
            Assert.IsNull(printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_PrintSettingValueChanged_String_PinCode()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PIN_CODE,
                Type = PrintSettingType.password,
                Value = "",
                Default = ""
            };

            PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, "1234");
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.AreEqual("1234", printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_GetCurrentPrintSettings_Null()
        {
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(null);
            Assert.IsNull(printSettings);
        }

        [TestMethod]
        public void Test_GetCurrentPrintSettings_Empty()
        {
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(string.Empty);
            Assert.IsNull(printSettings);
        }

        [TestMethod]
        public void Test_GetCurrentPrintSettings_NotExists()
        {
            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings("sample");
            Assert.IsNull(printSettings);
        }

        [TestMethod]
        public async Task Test_GetCurrentPrintSettings_Exists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            PrintSettings printSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(2, printSettings.Id);
            Assert.AreEqual(2, printSettings.PrinterId);
            Assert.AreEqual(1, printSettings.ColorMode);
            Assert.AreEqual(0, printSettings.Orientation);
            Assert.AreEqual(1, printSettings.Copies);
            Assert.AreEqual(0, printSettings.Duplex);
            Assert.AreEqual(2, printSettings.PaperSize);
            Assert.IsTrue(printSettings.ScaleToFit);
            Assert.AreEqual(0, printSettings.PaperType);
            Assert.AreEqual(0, printSettings.InputTray);
            Assert.AreEqual(0, printSettings.Imposition);
            Assert.AreEqual(0, printSettings.ImpositionOrder);
            Assert.AreEqual(0, printSettings.Sort);
            Assert.IsFalse(printSettings.Booklet);
            Assert.AreEqual(0, printSettings.BookletFinishing);
            Assert.AreEqual(0, printSettings.BookletLayout);
            Assert.AreEqual(0, printSettings.FinishingSide);
            Assert.AreEqual(0, printSettings.Staple);
            Assert.AreEqual(0, printSettings.Punch);
            Assert.AreEqual(0, printSettings.OutputTray);
            Assert.IsNull(printSettings.LoginId);
            Assert.IsNull(printSettings.PinCode);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_GetPagesPerSheet_Null()
        {
            int pagesPerSheet = PrintSettingsController.Instance.GetPagesPerSheet(null);
            Assert.AreEqual(1, pagesPerSheet);
        }

        [TestMethod]
        public void Test_GetPagesPerSheet_Empty()
        {
            int pagesPerSheet = PrintSettingsController.Instance.GetPagesPerSheet(string.Empty);
            Assert.AreEqual(1, pagesPerSheet);
        }

        [TestMethod]
        public void Test_GetPagesPerSheet_NotExists()
        {
            int pagesPerSheet = PrintSettingsController.Instance.GetPagesPerSheet("sample");
            Assert.AreEqual(1, pagesPerSheet);
        }

        [TestMethod]
        public async Task Test_GetPagesPerSheet_Exists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_imposition.sql", _dbConnection);

            _screenName = ScreenMode.PrintPreview.ToString();
            Printer printer = await _dbConnection.GetAsync<Printer>(2);
            await PrintSettingsController.Instance.Initialize(_screenName, printer);

            int pagesPerSheet = PrintSettingsController.Instance.GetPagesPerSheet(_screenName);
            Assert.AreEqual(2, pagesPerSheet);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        #region Mock Functions

        private void MockUpdatePreviewEventHandler(PrintSetting printSetting)
        {
            // Do nothing
        }

        #endregion Mock Functions

    }
}
