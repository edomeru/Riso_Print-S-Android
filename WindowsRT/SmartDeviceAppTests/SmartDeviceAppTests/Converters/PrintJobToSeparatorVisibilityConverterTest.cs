using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.Models;
using System.Collections.ObjectModel;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintJobToSeparatorVisibilityConverterTest
    {
        private PrintJobToSeparatorVisibilityConverter printJobToSeparatorVisibilityConverter = new PrintJobToSeparatorVisibilityConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printJobToSeparatorVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = printJobToSeparatorVisibilityConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var printJob1 = new PrintJob(0, 0, "PRINT_JOB1", DateTime.Now, 0);
            var printJob2 = new PrintJob(1, 0, "PRINT_JOB2", DateTime.Now, 0);
            var jobs = new ObservableCollection<PrintJob>();
            jobs.Add(printJob1);
            jobs.Add(printJob2);
            var group = new PrintJobGroup("PRINTER_NAME", "192.168.1.1", jobs);
            var printJobsList = new PrintJobList();
            printJobsList.Add(group);
            new ViewModelLocator().JobsViewModel.PrintJobsList = printJobsList;

            var value = printJob1;
            result = printJobToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);

            value = printJob2;
            result = printJobToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = printJobToSeparatorVisibilityConverter.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
