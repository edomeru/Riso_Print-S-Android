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
    public class SelectedPrinterIdToTextConverterTest
    {
        private SelectedPrinterIdToTextConverter selectedPrinterIdToTextConverter = new SelectedPrinterIdToTextConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test no printer
            var result = selectedPrinterIdToTextConverter.Convert(null, null, null, null);
            Assert.AreEqual("Choose printer", result);

            // Test no printer name
            new ViewModelLocator().PrintSettingsViewModel.PrinterName = String.Empty;
            result = selectedPrinterIdToTextConverter.Convert(0, null, null, null);
            Assert.AreEqual("No name", result);

            new ViewModelLocator().PrintSettingsViewModel.PrinterName = "PRINTER_NAME";
            result = selectedPrinterIdToTextConverter.Convert(1, null, null, null);
            Assert.AreEqual("PRINTER_NAME", result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = selectedPrinterIdToTextConverter.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
