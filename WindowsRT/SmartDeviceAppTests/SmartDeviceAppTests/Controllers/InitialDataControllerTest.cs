using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Storage;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class InitialDataControllerTest
    {
        private const string KEY_ISSAMPLEDATAALREADYLOADED = "IsSampleDataAlreadyLoaded";

        [TestInitialize]
        public async Task Initialize()
        {
            await DatabaseController.Instance.Initialize();
        }

        [TestCleanup]
        public void Cleanup()
        {
            DatabaseController.Instance.Cleanup();
        }

        [TestMethod]
        public async Task Test_InsertSampleData_ExistingKey()
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values[KEY_ISSAMPLEDATAALREADYLOADED] = false;

            await InitialDataController.InsertSampleData();
            // Note: no public property to assert
        }

        [TestMethod]
        public async Task Test_InsertSampleData_KeyNotFound()
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values.Remove(KEY_ISSAMPLEDATAALREADYLOADED);

            await InitialDataController.InsertSampleData();
            // Note: no public property to assert
        }
    }
}
