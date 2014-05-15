using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class PrinterTest
    {
        Printer printer = new Printer();
        [TestMethod]
        public void Test_Printer_GetSetName()
        {
            printer.Name = "Test";
            Assert.AreEqual("Test", printer.Name);
        }

        [TestMethod]
        public void Test_Printer_GetSetIsOnline()
        {
            printer.IsOnline = false;
            Assert.AreEqual(false, printer.IsOnline);
        }
    }
}
