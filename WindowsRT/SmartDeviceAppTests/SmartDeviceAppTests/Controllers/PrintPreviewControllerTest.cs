using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System.Threading.Tasks;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Common.Utilities;
using Windows.Storage;
using SmartDeviceAppTests.Common.Utilities;
using SQLite;
using System.IO;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Constants;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class PrintPreviewControllerTest
    {

        private const string TESTDATA_PDF_REGULAR = "TestData/PDF/RZ1070.pdf";
        private const string TESTDATA_PDF_ENCRYPTED = "TestData/PDF/128AES_Acrobat.pdf";
        private const string TESTDATA_PDF_RANDOM = "TestData/SqlScript/create_table_printsetting.sql";
        private const string TESTDATA_PDF_NOT_EXISTS = "TestData/PDF/123sampletest.pdf";

        // From PrintPreviewController.cs
        private const string PREFIX_PREVIEW_PAGE_IMAGE = "previewpage";

        // From DatabaseController.cs
        private const string FILE_NAME_DATABASE = "SmartDeviceAppDB.db";
        private const string KEY_ISSAMPLEDATAALREADYLOADED = "IsSampleDataAlreadyLoaded";

        private HomeViewModel _homeViewModel;
        private StorageFolder _tempFolder;
        private SQLiteAsyncConnection _dbConnection;

        // Cover Unit Tests using dotCover does not call TestInitialize
        //[TestInitialize]
        //public async Task Initialize()
        private async Task Initialize()
        {
            _homeViewModel = new ViewModelLocator().HomeViewModel;
            _tempFolder = ApplicationData.Current.TemporaryFolder;

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
            _homeViewModel = null;
            _tempFolder = null;

            //DatabaseController.Instance.Cleanup(); // This should be ideal however since events (target methods) are not awaitable, we cannot reset connections
        }

        [UI.UITestMethod]
        public void Test_Initialize_Unsupported()
        {
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_ENCRYPTED);
            //await DocumentController.Instance.Load(file);

            //await PrintPreviewController.Instance.Initialize();
            //Assert.IsFalse(_homeViewModel.IsProgressRingActive);

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_Initialize_Error()
        {
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_RANDOM);
            //await DocumentController.Instance.Load(file);

            //await PrintPreviewController.Instance.Initialize();
            //Assert.IsFalse(_homeViewModel.IsProgressRingActive);

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_Initialize_NotStarted()
        {
            //await DocumentController.Instance.Load(null);

            //await PrintPreviewController.Instance.Initialize();
            //Assert.IsFalse(_homeViewModel.IsProgressRingActive);

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_Initialize_Successful()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);

            //await PrintPreviewController.Instance.Initialize();
            //Assert.IsTrue(_homeViewModel.IsProgressRingActive);

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [TestMethod]
        public void Test_Cleanup()
        {
            //await Initialize(); // Workaround for Cover Unit Tests using dotCover

            //await PrintPreviewController.Instance.Cleanup();
            //Assert.IsFalse(await UnitTestUtility.CheckIfFileExists(PREFIX_PREVIEW_PAGE_IMAGE, _tempFolder));

            //Cleanup(); // Workaround for Cover Unit Tests using dotCover

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_RegisterPrintSettingValueChange_ResetTrue()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //Printer printer = await _dbConnection.GetAsync<Printer>(2);
            //PrintPreviewController.Instance.PrinterDeleted(printer);

            //PrintPreviewController.Instance.RegisterPrintSettingValueChange();
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [TestMethod]
        public void Test_RegisterPrintSettingValueChange_ResetFalse()
        {
            PrintPreviewController.Instance.RegisterPrintSettingValueChange();
            // Note: no public property or return value to assert
        }

        [UI.UITestMethod]
        public void Test_UnregisterPrintSettingValueChange()
        {
            PrintPreviewController.Instance.UnregisterPrintSettingValueChange();
            // Note: no public property or return value to assert
        }

        [UI.UITestMethod]
        public void Test_PrinterDeleted_SelectedPrinter()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //Printer printer = await _dbConnection.GetAsync<Printer>(2);

            //PrintPreviewController.Instance.PrinterDeleted(printer);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [TestMethod]
        public void Test_PrinterDeleted_NotSelectedPrinter()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //Printer printer = await _dbConnection.GetAsync<Printer>(3);

            //PrintPreviewController.Instance.PrinterDeleted(printer);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_SelectedPrinterChanged_NoDefaultPrinter()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //await _dbConnection.DeleteAsync(await _dbConnection.GetAsync<DefaultPrinter>(2)); // Delete default printer
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();

            //PrintPreviewController.Instance.SelectedPrinterChanged(-1);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }


        [UI.UITestMethod]
        public void Test_SelectedPrinterChanged_InvalidPrinter()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();

            //PrintPreviewController.Instance.SelectedPrinterChanged(-1);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_SelectedPrinterChanged_ValidPrinter()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();

            //PrintPreviewController.Instance.SelectedPrinterChanged(3);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [TestMethod]
        public void Test_UpdatePreview_Null()
        {
            PrintPreviewController.Instance.UpdatePreview(null);
            // Note: no public property or return value to assert
        }

        [UI.UITestMethod]
        public void Test_UpdatePreview_ColorMode()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //PrintSetting printSetting = new PrintSetting()
            //{
            //    Name = PrintSettingConstant.NAME_VALUE_COLOR_MODE,
            //    Type = PrintSettingType.numeric,
            //    Value = 2,
            //    Default = 1
            //};
            //PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);

            //PrintPreviewController.Instance.UpdatePreview(printSetting);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePreview_Duplex()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //PrintSetting printSetting = new PrintSetting()
            //{
            //    Name = PrintSettingConstant.NAME_VALUE_DUPLEX,
            //    Type = PrintSettingType.numeric,
            //    Value = 1,
            //    Default = 0
            //};
            //PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);

            //PrintPreviewController.Instance.UpdatePreview(printSetting);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePreview_Imposition()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //PrintSetting printSetting = new PrintSetting()
            //{
            //    Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
            //    Type = PrintSettingType.numeric,
            //    Value = 1,
            //    Default = 0
            //};
            //PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 1);

            //PrintPreviewController.Instance.UpdatePreview(printSetting);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePreview_BookletLayout()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_booklet.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //PrintSetting printSetting = new PrintSetting()
            //{
            //    Name = PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT,
            //    Type = PrintSettingType.numeric,
            //    Value = 2,
            //    Default = 0
            //};
            //PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, 2);

            //PrintPreviewController.Instance.UpdatePreview(printSetting);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePreview_Booklet()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //PrintSetting printSetting = new PrintSetting()
            //{
            //    Name = PrintSettingConstant.NAME_VALUE_BOOKLET,
            //    Type = PrintSettingType.numeric,
            //    Value = true,
            //    Default = false
            //};
            //PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);

            //PrintPreviewController.Instance.UpdatePreview(printSetting);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePreview_Booklet_ChangePageCount()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();
            //PrintPreviewController.Instance.GoToPage((int)DocumentController.Instance.PageCount - 1);
            //PrintSetting printSetting = new PrintSetting()
            //{
            //    Name = PrintSettingConstant.NAME_VALUE_BOOKLET,
            //    Type = PrintSettingType.numeric,
            //    Value = true,
            //    Default = false
            //};
            //PrintSettingsController.Instance.PrintSettingValueChanged(printSetting, true);

            //PrintPreviewController.Instance.UpdatePreview(printSetting);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_GoToPage_BookletOff()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(file);
            //await PrintPreviewController.Instance.Initialize();

            //PrintPreviewController.Instance.GoToPage(3);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_GoToPage_BookletOn()
        {
            //await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printsettings_booklet.sql", _dbConnection);
            //var task = StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            //await DocumentController.Instance.Load(task.Result);
            //await PrintPreviewController.Instance.Initialize();

            //PrintPreviewController.Instance.GoToPage(3);
            // Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_Print()
        {
            //PrintPreviewController.Instance.Print();
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_GetPrinterStatus_True()
        {
            //PrintPreviewController.Instance.GetPrinterStatus(null, true);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_GetPrinterStatus_False()
        {
            //PrintPreviewController.Instance.GetPrinterStatus(null, false);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_CancelPrint()
        {
            //PrintPreviewController.Instance.CancelPrint();
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePrintJobProgress()
        {
            //PrintPreviewController.Instance.UpdatePrintJobProgress(0);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePrintJobResult_Error()
        {
            //PrintPreviewController.Instance.UpdatePrintJobResult("error.pdf", DateTime.Now,
            //    (int)PrintJobResult.Error);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_UpdatePrintJobResult_Successful()
        {
            //PrintPreviewController.Instance.UpdatePrintJobResult("ok.pdf", DateTime.Now,
            //    (int)PrintJobResult.Success);
            //// Note: no public property or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

    }
}
