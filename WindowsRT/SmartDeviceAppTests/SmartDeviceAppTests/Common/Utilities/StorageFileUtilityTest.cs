using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.Common.Utilities
{
    [TestClass]
    public class StorageFileUtilityTest
    {

        private const string TESTDATA_SQL_SCRIPT = "TestData/SqlScript/create_table_printer.sql";
        private const string TESTDATA_PDF_REGULAR = "TestData/PDF/RZ1070.pdf";
        private const string FILE_NAME_PDF = "tempCopy.pdf";
        private const string FILE_NAME_SQL = "tempCopy.sql";

        private StorageFolder _tempFolder;

        // Cover Unit Tests using dotCover does not call TestInitialize
        //[TestInitialize]
        //public async Task Initialize()
        private async Task Initialize()
        {
            _tempFolder = ApplicationData.Current.TemporaryFolder;
            
            StorageFile pdfFile = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            await pdfFile.CopyAsync(_tempFolder, FILE_NAME_PDF, NameCollisionOption.ReplaceExisting);

            StorageFile sqlFile = await StorageFileUtility.GetFileFromAppResource(TESTDATA_SQL_SCRIPT);
            await sqlFile.CopyAsync(_tempFolder, FILE_NAME_SQL, NameCollisionOption.ReplaceExisting);
        }

        // Cover Unit Tests using dotCover does not call TestCleanup
        //[TestCleanup]
        //public async Task Cleanup()
        private async Task Cleanup()
        {
            await StorageFileUtility.DeleteAllTempFiles();
            _tempFolder = null;
        }

        [TestMethod]
        public async Task Test_GetExistingFile_NotFound()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            StorageFile file = await StorageFileUtility.GetExistingFile("random.txt", _tempFolder);
            Assert.IsNull(file);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_GetExistingFile_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            StorageFile target = await StorageFileUtility.GetExistingFile(FILE_NAME_PDF, _tempFolder);
            Assert.IsNotNull(target);
            Assert.AreEqual(FILE_NAME_PDF, target.Name);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_DeleteAllTempFiles()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await StorageFileUtility.DeleteAllTempFiles();

            StorageFile target = await StorageFileUtility.GetExistingFile(FILE_NAME_PDF, _tempFolder);
            Assert.IsNull(target);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_DeleteFilesExcept_Match()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await StorageFileUtility.DeleteFilesExcept("tempCopy", FILE_NAME_SQL, _tempFolder);

            StorageFile pdfFile = await StorageFileUtility.GetExistingFile(FILE_NAME_PDF, _tempFolder);
            Assert.IsNull(pdfFile);

            StorageFile sqlFile = await StorageFileUtility.GetExistingFile(FILE_NAME_SQL, _tempFolder);
            Assert.IsNotNull(sqlFile);
            Assert.AreEqual(FILE_NAME_SQL, sqlFile.Name);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_DeleteFilesExcept_Unmatch()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await StorageFileUtility.DeleteFilesExcept("tempCopy", "random", _tempFolder);

            StorageFile pdfFile = await StorageFileUtility.GetExistingFile(FILE_NAME_PDF, _tempFolder);
            Assert.IsNull(pdfFile);

            StorageFile sqlFile = await StorageFileUtility.GetExistingFile(FILE_NAME_SQL, _tempFolder);
            Assert.IsNull(sqlFile);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_DeleteFile_NotFound()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await StorageFileUtility.DeleteFile("random", _tempFolder);
            // Note: no public property or return value to assert

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_DeleteFile_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await StorageFileUtility.DeleteFile(FILE_NAME_SQL, _tempFolder);

            StorageFile sqlFile = await StorageFileUtility.GetExistingFile(FILE_NAME_SQL, _tempFolder);
            Assert.IsNull(sqlFile);

            StorageFile pdfFile = await StorageFileUtility.GetExistingFile(FILE_NAME_PDF, _tempFolder);
            Assert.IsNotNull(pdfFile);
            Assert.AreEqual(FILE_NAME_PDF, pdfFile.Name);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_GetFileFromAppResource_Invalid()
        {
            StorageFile file = await StorageFileUtility.GetFileFromAppResource("random");
            Assert.IsNull(file);
        }

        [TestMethod]
        public async Task Test_GetFileFromAppResource_Valid()
        {
            StorageFile pdfFile = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            Assert.IsNotNull(pdfFile);
            Assert.AreEqual("RZ1070.pdf", pdfFile.Name);
        }

    }
}
