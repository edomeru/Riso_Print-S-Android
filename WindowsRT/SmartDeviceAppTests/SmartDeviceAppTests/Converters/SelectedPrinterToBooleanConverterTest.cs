using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class SelectedPrinterToBooleanConverterTest
    {
        private SelectedPrinterToBooleanConverter selectedPrinterToBooleanConverter = new SelectedPrinterToBooleanConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = selectedPrinterToBooleanConverter.Convert(null, null, null, null);
            Assert.AreEqual(false, result);

            // Test wrong type
            result = selectedPrinterToBooleanConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(false, result);

            var value = 0;
            new ViewModelLocator().PrintSettingsViewModel.PrinterId = value;
            result = selectedPrinterToBooleanConverter.Convert(value, null, null, null);
            Assert.AreEqual(true, result);

            value = 1;
            result = selectedPrinterToBooleanConverter.Convert(value, null, null, null);
            Assert.AreEqual(false, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = selectedPrinterToBooleanConverter.ConvertBack(null, null, null, null);
                Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
