using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using Windows.Foundation;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class SeparatorLineGeometryStartPointConverterTest
    {
        private SeparatorLineGeometryStartPointConverter separatorLineGeometryStartPointConverter = new SeparatorLineGeometryStartPointConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = separatorLineGeometryStartPointConverter.Convert(null, null, null, null);
            Assert.AreEqual(new Point(0,0), result);

            // Test wrong type
            result = separatorLineGeometryStartPointConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(new Point(0, 0), result);

            double value = 1;
            result = separatorLineGeometryStartPointConverter.Convert(value, null, null, null);
            Assert.AreEqual(new Point(1,0), result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => separatorLineGeometryStartPointConverter.ConvertBack(null, null, null, null));
        }
    }

    [TestClass]
    public class SeparatorLineGeometryEndPointConverterTest
    {
        private SeparatorLineGeometryEndPointConverter separatorLineGeometryEndPointConverter = new SeparatorLineGeometryEndPointConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = separatorLineGeometryEndPointConverter.Convert(null, null, null, null);
            Assert.AreEqual(new Point(0, 0), result);

            // Test wrong type
            result = separatorLineGeometryEndPointConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(new Point(0, 0), result);

            double value = 1;
            result = separatorLineGeometryEndPointConverter.Convert(value, null, null, null);
            Assert.AreEqual(new Point(1, 0), result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => separatorLineGeometryEndPointConverter.ConvertBack(null, null, null, null));
        }
    }
}
