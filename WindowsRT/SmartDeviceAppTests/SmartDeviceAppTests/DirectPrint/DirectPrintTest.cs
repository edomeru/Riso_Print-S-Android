using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using DirectPrint;
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;
using Windows.Networking;

namespace SmartDeviceAppTests.DirectPrintTest
{
    [TestClass]
    public class DirectPrintTest
    {
        private const string TESTDATA_PDF_REGULAR = "TestData/PDF/PDF-lorem.pdf";
        private DirectPrint.DirectPrint _directPrint = new DirectPrint.DirectPrint();

        private void Test_callback(int value)
        {
        }

        private void Test_progress_callback(float value)
        {
        }

        [TestMethod]
        public void Test_directprint_job()
        {
            var job = new directprint_job();
            job.job_name = "TEST";
            job.file = null;
                // await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            job.print_settings = "PRINT_SETTINGS";
            job.ip_address = "192.168.1.199";
            job.callback = new directprint_callback(Test_callback);
            job.progress_callback = new progress_callback(Test_progress_callback);
            job.progress = 1.0F;
            job.cancel_print = 0;
            Assert.IsNotNull(job);
        }

        [TestMethod]
        public void Test_DirectPrint()
        {
            var directPrint = new DirectPrint.DirectPrint();
            Assert.IsNotNull(directPrint);
        }

        [TestMethod]
        public async Task Test_startLPRPrint()
        {
            // Note: Test for coverage only; No tests to assert

            // Test null
            _directPrint.startLPRPrint(null);            

            var job = new directprint_job();
            job.job_name = "TEST";
            job.file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            job.print_settings = "";
            job.ip_address = "192.168.1.199";
            job.callback = new directprint_callback(Test_callback);
            job.progress_callback = new progress_callback(Test_progress_callback);
            job.progress = 1.0F;
            job.cancel_print = 0;

            _directPrint.receiveData(new HostName("192.168.1.199"), 0);
            
            _directPrint.startLPRPrint(job);
        }
        
        [TestMethod]
        public async Task Test_cancelPrint()
        {
            var job = new directprint_job();
            job.job_name = "TEST";
            job.file = await StorageFileUtility.GetFileFromAppResource(TESTDATA_PDF_REGULAR);
            job.print_settings = "PRINT_SETTINGS";
            job.ip_address = "192.168.1.199";
            job.callback = new directprint_callback(Test_callback);
            job.progress_callback = new progress_callback(Test_progress_callback);
            job.progress = 1.0F;
            job.cancel_print = 1;
            _directPrint.receiveData(new HostName("192.168.1.199"), 0);
            await Task.Run(() => _directPrint._startLPRPrint(job));     
            _directPrint.cancelPrint();
        }


    }
}
