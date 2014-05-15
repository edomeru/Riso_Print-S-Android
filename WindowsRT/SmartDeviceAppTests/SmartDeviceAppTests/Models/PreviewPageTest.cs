using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class PreviewPageTest
    {

        [TestMethod]
        public void Test_PreviewPageTest()
        {
            PreviewPage previewPage;

            previewPage = new PreviewPage(0, "name.jpg", new Windows.Foundation.Size());
            Assert.AreEqual((uint)0, previewPage.PageIndex);
            Assert.AreEqual("name.jpg", previewPage.Name);
            Assert.AreEqual(0, previewPage.ActualSize.Width);
            Assert.AreEqual(0, previewPage.ActualSize.Height);
        }

    }
}
