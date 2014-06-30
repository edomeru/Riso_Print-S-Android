using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class DataItemTest
    {

        [TestMethod]
        public void Test_DataItem()
        {
            DataItem dataItem;
            
            dataItem = new DataItem(string.Empty);
            Assert.AreEqual(string.Empty, dataItem.Title);

            dataItem = new DataItem("title");
            Assert.AreEqual("title", dataItem.Title);
        }

    }
}
