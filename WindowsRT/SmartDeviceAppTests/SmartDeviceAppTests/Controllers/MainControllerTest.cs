using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System.Threading.Tasks;
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class MainControllerTest
    {

        private const string TESTDATA_PDF_REGULAR = "TestData/PDF/RZ1070.pdf";

        [TestMethod]
        public void Test_Initialize()
        {
            MainController.Initialize();
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public async Task Test_FileActivationHandler_Null()
        {
            await MainController.FileActivationHandler(null);
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_FileActivationHandler_Valid()
        {
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);

            //await MainController.FileActivationHandler(file);
            //// Note: no public properties or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

        [TestMethod]
        public void Test_Cleanup()
        {
            MainController.Cleanup();
            // Note: no public properties or return value to assert
        }

        [TestMethod]
        public void Test_InitializeSamplePdf()
        {
            //await MainController.InitializeSamplePdf();
            //// Note: no public properties or return value to assert

            {
                Assert.Inconclusive("UI Test");
            }
        }

    }
}
