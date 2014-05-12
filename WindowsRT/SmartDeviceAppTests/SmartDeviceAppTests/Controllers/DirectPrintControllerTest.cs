using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using System.Threading.Tasks;
using Windows.Storage;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class DirectPrintControllerTest
    {

        private const string TESTDATA_SQL_SCRIPT = "TestData/SqlScript/create_table_printsetting.sql";
        private const string TESTDATA_PDF = "TestData/PDF/RZ1070.pdf";

        [TestMethod]
        public void Test_UnsubscribeEvents()
        {
            DirectPrintController directPrintController = new DirectPrintController(
                "sample job", null, "172.0.0.1", new PrintSettings(), MockUpdatePrintJobProgress,
                MockSetPrintJobResult);

            directPrintController.UnsubscribeEvents();
            // Note: no public property or return value to assert
        }

        
        [TestMethod]
        public void Test_SendPrintJob()
        {
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF);
            //await DefaultsUtility.LoadDefaultsFromSqlScript(TESTDATA_SQL_SCRIPT);
            //DirectPrintController directPrintController = new DirectPrintController(
            //    "sample job", file, "172.0.0.1", new PrintSettings(), MockUpdatePrintJobProgress,
            //    MockSetPrintJobResult);

            //directPrintController.SendPrintJob();
            //// Note: no public property or return value to assert

            //await DefaultsUtility.LoadDefaultsFromSqlScript(null); // Cleanup

            {
                Assert.Fail("Not yet implemented");
            }
        }

        [TestMethod]
        public void Test_CancelPrintJob_Null()
        {
            DirectPrintController directPrintController = new DirectPrintController(
                "sample job", null, "172.0.0.1", new PrintSettings(), MockUpdatePrintJobProgress,
                MockSetPrintJobResult);

            directPrintController.CancelPrintJob();
            // Note: no public property or return value to assert
        }

        // TODO: Crashing
        [TestMethod]
        public void Test_CancelPrintJob_Valid()
        {
            //StorageFile file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF);
            //await DefaultsUtility.LoadDefaultsFromSqlScript(TESTDATA_SQL_SCRIPT);
            //DirectPrintController directPrintController = new DirectPrintController(
            //    "sample job", file, "172.0.0.1", new PrintSettings(), MockUpdatePrintJobProgress,
            //    MockSetPrintJobResult);
            //directPrintController.SendPrintJob();

            //directPrintController.CancelPrintJob();
            //// Note: no public property or return value to assert

            //await DefaultsUtility.LoadDefaultsFromSqlScript(null); // Cleanup

            {
                Assert.Fail("Not yet implemented");
            }
        }

        [TestMethod]
        public void Test_UpdateProgress_Null()
        {
            DirectPrintController directPrintController = new DirectPrintController(
                "sample job", null, "172.0.0.1", new PrintSettings(), null, null);

            directPrintController.UpdateProgress(1.0f);
            // Note: no public property or return value to assert
        }

        [TestMethod]
        public void Test_UpdateProgress_Valid()
        {
            DirectPrintController directPrintController = new DirectPrintController(
                "sample job", null, "172.0.0.1", new PrintSettings(), MockUpdatePrintJobProgress, null);

            directPrintController.UpdateProgress(1.0f);
            // Note: no public property or return value to assert
        }


        [TestMethod]
        public void Test_ReceiveResult_Null()
        {
            DirectPrintController directPrintController = new DirectPrintController(
                "sample job", null, "172.0.0.1", new PrintSettings(), null, null);

            directPrintController.ReceiveResult((int)PrintJobResult.Error);
            // Note: no public property or return value to assert
        }

        [TestMethod]
        public void Test_ReceiveResult_Valid()
        {
            DirectPrintController directPrintController = new DirectPrintController(
                "sample job", null, "172.0.0.1", new PrintSettings(), null, MockSetPrintJobResult);

            directPrintController.ReceiveResult((int)PrintJobResult.Success);
            // Note: no public property or return value to assert
        }

        [TestMethod]
        public async Task Test_CreateStringFromPrintSettings_Valid()
        {
            await DefaultsUtility.LoadDefaultsFromSqlScript(TESTDATA_SQL_SCRIPT);
            DirectPrintController directPrintController = new DirectPrintController(
                "sample job", null, "172.0.0.1", new PrintSettings(), null, MockSetPrintJobResult);
            // Note: no public property or return value to assert
            await DefaultsUtility.LoadDefaultsFromSqlScript(null); // Cleanup
        }

        #region Mock Functions

        private void MockUpdatePrintJobProgress(float progress)
        {
            // Do nothing
        }

        private void MockSetPrintJobResult(string name, DateTime date, int result)
        {
            // Do nothing
        }

        #endregion Mock Functions

    }
}
