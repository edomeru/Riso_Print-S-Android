using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class WidthConverterTest
    {
        private WidthConverter widthConverter = new WidthConverter();

        [UITestMethod]
        public void Test_Convert()
        {
            var result = widthConverter.Convert(null, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(Window.Current.Bounds.Width, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = widthConverter.ConvertBack(null, null, null, null);
                Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }

    [TestClass]
    public class ResizedViewWidthConverterTest
    {
        private ResizedViewWidthConverter resizedViewWidthConverter = new ResizedViewWidthConverter();

        [UITestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = resizedViewWidthConverter.Convert(null, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.IsNotNull(result);

            var value = "RightPaneVisible_ResizedWidth";
            result = resizedViewWidthConverter.Convert(value, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(Window.Current.Bounds.Width - (double)Application.Current.Resources["SIZE_SidePaneWidth"], result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = resizedViewWidthConverter.ConvertBack(null, null, null, null);
                Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }

    [TestClass]
    public class PrintJobListWidthConverterTest
    {
        private PrintJobListWidthConverter printJobListWidthConverter = new PrintJobListWidthConverter();

        [UITestMethod]
        public void Test_Convert()
        {
            var result = printJobListWidthConverter.Convert(null, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.IsNotNull(result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = printJobListWidthConverter.ConvertBack(null, null, null, null);
                Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }

    [TestClass]
    public class HeightConverterTest
    {
        private HeightConverter heightConverter = new HeightConverter();

        [UITestMethod]
        public void Test_Convert()
        {
            var result = heightConverter.Convert(null, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(Window.Current.Bounds.Height, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = heightConverter.ConvertBack(null, null, null, null);
                Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
