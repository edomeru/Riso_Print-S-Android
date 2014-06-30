using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Common.Constants;
using Windows.Foundation;

namespace SmartDeviceAppTests.Common.Constant
{
    [TestClass]
    public class PrintSettingConstantTest
    {

        [TestMethod]
        public void Test_PaperSizes()
        {
            Size size;

            size = PrintSettingConstant.PAPER_SIZE_A3;
            Assert.AreEqual(297.0, size.Width);
            Assert.AreEqual(420.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_A3W;
            Assert.AreEqual(316.0, size.Width);
            Assert.AreEqual(460.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_A4;
            Assert.AreEqual(210.0, size.Width);
            Assert.AreEqual(297.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_A5;
            Assert.AreEqual(148.0, size.Width);
            Assert.AreEqual(210.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_A6;
            Assert.AreEqual(105.0, size.Width);
            Assert.AreEqual(148.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_B4;
            Assert.AreEqual(257.0, size.Width);
            Assert.AreEqual(364.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_B5;
            Assert.AreEqual(182.0, size.Width);
            Assert.AreEqual(257.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_B6;
            Assert.AreEqual(128.0, size.Width);
            Assert.AreEqual(182.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_FOOLSCAP;
            Assert.AreEqual(216.0, size.Width);
            Assert.AreEqual(340.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_TABLOID;
            Assert.AreEqual(280.0, size.Width);
            Assert.AreEqual(432.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_LEGAL;
            Assert.AreEqual(216.0, size.Width);
            Assert.AreEqual(356.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_LETTER;
            Assert.AreEqual(216.0, size.Width);
            Assert.AreEqual(280.0, size.Height);

            size = PrintSettingConstant.PAPER_SIZE_STATEMENT;
            Assert.AreEqual(140.0, size.Width);
            Assert.AreEqual(216.0, size.Height);
        }

    }
}
