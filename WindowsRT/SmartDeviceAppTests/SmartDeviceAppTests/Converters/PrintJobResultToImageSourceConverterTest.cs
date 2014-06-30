using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class PrintJobResultToImageSourceConverterTest
    {
        private PrintJobResultToImageSourceConverter printJobResultToImageSourceConverter = new PrintJobResultToImageSourceConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printJobResultToImageSourceConverter.Convert(null, null, null, null);
            Assert.AreEqual(null, result);

            // Test wrong type
            result = printJobResultToImageSourceConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(null, result);

            // Test success
            var value = 0;
            result = printJobResultToImageSourceConverter.Convert(value, null, null, null);
            Assert.AreEqual(new Uri("ms-appx:///Resources/Images/img_btn_job_status_ok.png"), result);

            // Test error
            value = 1;
            result = printJobResultToImageSourceConverter.Convert(value, null, null, null);
            Assert.AreEqual(new Uri("ms-appx:///Resources/Images/img_btn_job_status_ng.png"), result);

            value = 2;
            result = printJobResultToImageSourceConverter.Convert(value, null, null, null);
            Assert.AreEqual(null, result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => printJobResultToImageSourceConverter.ConvertBack(null, null, null, null));
        }
    }
}
