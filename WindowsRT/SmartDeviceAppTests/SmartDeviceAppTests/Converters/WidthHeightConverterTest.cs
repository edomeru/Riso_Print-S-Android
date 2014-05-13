using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class WidthConverterTest
    {
        private WidthConverter widthConverter = new WidthConverter();

        [UI.UITestMethod]
        public void Test_Convert()
        {
            var result = widthConverter.Convert(null, null, null, null);
            Assert.AreEqual(Window.Current.Bounds.Width, result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => widthConverter.ConvertBack(null, null, null, null));
        }
    }

    [TestClass]
    public class ResizedViewWidthConverterTest
    {
        private ResizedViewWidthConverter resizedViewWidthConverter = new ResizedViewWidthConverter();

        [UI.UITestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = resizedViewWidthConverter.Convert(null, null, null, null);
            Assert.IsNotNull(result);

            var value = "RightPaneVisible_ResizedWidth";
            result = resizedViewWidthConverter.Convert(value, null, null, null);
            Assert.AreEqual(Window.Current.Bounds.Width - (double)Application.Current.Resources["SIZE_SidePaneWidth"], result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => resizedViewWidthConverter.ConvertBack(null, null, null, null));
        }
    }

    [TestClass]
    public class PrintJobListWidthConverterTest
    {
        private PrintJobListWidthConverter printJobListWidthConverter = new PrintJobListWidthConverter();

        [UI.UITestMethod]
        public void Test_Convert()
        {
            var result = printJobListWidthConverter.Convert(null, null, null, null);
            Assert.IsNotNull(result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => printJobListWidthConverter.ConvertBack(null, null, null, null));
        }
    }

    [TestClass]
    public class HeightConverterTest
    {
        private HeightConverter heightConverter = new HeightConverter();

        [UI.UITestMethod]
        public void Test_Convert()
        {
            var result = heightConverter.Convert(null, null, null, null);
            Assert.AreEqual(Window.Current.Bounds.Height, result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => heightConverter.ConvertBack(null, null, null, null));
        }
    }
}
