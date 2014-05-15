using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class PrintJobTest
    {

        private int _id = 1;
        private PrintJob _printJob;

        private void Initialize()
        {
            _printJob = new PrintJob();
            _printJob.Id = _id;
            _printJob.PrinterId = 1;
            _printJob.Name = "name.pdf";
            _printJob.Date = DateTime.Now;
        }

        private void Cleanup()
        {
            _printJob = null;
        }

        [TestMethod]
        public void Test_PrintJob_Equals_Object_Null()
        {
            Initialize();

            object target = null;
            bool result = _printJob.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintJob_Equals_Object_Other()
        {
            Initialize();

            object target = false;
            bool result = _printJob.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintJob_Equals_Object_Same()
        {
            Initialize();

            object target = _printJob;
            bool result = _printJob.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintJob_Equals_Null()
        {
            Initialize();

            PrintJob target = null;
            bool result = _printJob.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintJob_Equals_Same()
        {
            Initialize();

            PrintJob target = _printJob;
            bool result = _printJob.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintJob_GetHashCode()
        {
            Initialize();

            int result = _printJob.GetHashCode();
            Assert.AreEqual(_id.GetHashCode(), result);

            Cleanup();
        }

    }
}
