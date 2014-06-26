using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;
using System.Threading.Tasks;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;
using Windows.UI.Xaml.Media.Imaging;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class DocumentControllerTest
    {

        private const string TESTDATA_PDF_REGULAR = "TestData/PDF/RZ1070.pdf";
        private const string TESTDATA_PDF_ENCRYPTED = "TestData/PDF/128AES_Acrobat.pdf";
        private const string TESTDATA_PDF_RANDOM = "TestData/SqlScript/create_table_printsetting.sql";
        private const string TESTDATA_PDF_NOT_EXISTS = "TestData/PDF/123sampletest.pdf";

        // Cover Unit Tests using dotCover does not call TestInitialize
        //[TestInitialize]
        //public async Task Initialize()
        private async Task Cleanup()
        {
            await DocumentController.Instance.Unload();
        }

        [TestMethod]
        public async Task Test_Load_Null()
        {
            await DocumentController.Instance.Load(null);
            Assert.AreEqual(LoadDocumentResult.NotStarted, DocumentController.Instance.Result);
        }

        [TestMethod]
        public async Task Test_Load_RegularPdf()
        {
            StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            await DocumentController.Instance.Load(file);
            Assert.AreEqual((uint)12, DocumentController.Instance.PageCount);
            Assert.IsNotNull(DocumentController.Instance.PdfFile);
            Assert.IsNotNull(DocumentController.Instance.FileName);
            Assert.AreEqual("RZ1070.pdf", DocumentController.Instance.FileName);
            Assert.AreEqual(LoadDocumentResult.Successful, DocumentController.Instance.Result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Load_UnsupportedPdf()
        {
            StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_ENCRYPTED);
            await DocumentController.Instance.Load(file);
            Assert.AreEqual((uint)0, DocumentController.Instance.PageCount);
            Assert.IsNotNull(DocumentController.Instance.PdfFile);
            Assert.IsNull(DocumentController.Instance.FileName);
            Assert.AreEqual(LoadDocumentResult.UnsupportedPdf, DocumentController.Instance.Result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Load_RandomFile()
        {
            StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_RANDOM);
            await DocumentController.Instance.Load(file);
            Assert.AreEqual((uint)0, DocumentController.Instance.PageCount);
            Assert.IsNotNull(DocumentController.Instance.PdfFile);
            Assert.IsNull(DocumentController.Instance.FileName);
            Assert.AreEqual(LoadDocumentResult.ErrorReadPdf, DocumentController.Instance.Result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Unload()
        {
            await DocumentController.Instance.Unload();
            Assert.AreEqual((uint)0, DocumentController.Instance.PageCount);
            Assert.IsNull(DocumentController.Instance.PdfFile);
            Assert.IsNull(DocumentController.Instance.FileName);
            Assert.AreEqual(LoadDocumentResult.NotStarted, DocumentController.Instance.Result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [UI.UITestMethod]
        public async Task Test_GetLogicalPageImages_NotLoaded()
        {
            List<WriteableBitmap> result =
                await DocumentController.Instance.GetLogicalPageImages(0, 1,
                new System.Threading.CancellationTokenSource());
            Assert.IsNull(result);
        }

        [UI.UITestMethod]
        public async Task Test_GetLogicalPageImages_Loaded_Start()
        {
            StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            await DocumentController.Instance.Load(file);

            List<WriteableBitmap> result =
                await DocumentController.Instance.GetLogicalPageImages(0, 2,
                new System.Threading.CancellationTokenSource());
            Assert.IsNotNull(result);
            Assert.AreEqual(2, result.Count); // Count should be same as requested

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [UI.UITestMethod]
        public async Task Test_GetLogicalPageImages_Loaded_End()
        {
            StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            await DocumentController.Instance.Load(file);

            List<WriteableBitmap> result =
                await DocumentController.Instance.GetLogicalPageImages(11, 5,
                new System.Threading.CancellationTokenSource());
            Assert.IsNotNull(result);
            Assert.AreEqual(1, result.Count); // Should only be one since it is the last page, regardless of request count

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

    }
}
