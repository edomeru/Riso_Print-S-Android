//
//  InitialDataController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/05/19.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Utilities;
using System.Threading.Tasks;
using Windows.Storage;

namespace SmartDeviceApp.Controllers
{
    public static class InitialDataController
    {

        private static string PDF_FILE_PATH = "Resources/Dummy/RZ1070.pdf";
        private static string KEY_ISSAMPLEDATAALREADYLOADED = "IsSampleDataAlreadyLoaded";
        private static string DB_SAMPLE_DATA_FILE_PATH = "Resources/Dummy/SampleData.sql";

        /// <summary>
        /// Opens a PDF at start
        /// </summary>
        /// <returns></returns>
        public async static Task InitializeSamplePdf()
        {
            await DocumentController.Instance.Unload();
            await PrintPreviewController.Instance.Cleanup();

            StorageFile samplePdf = await GetSamplePdf();
            await DocumentController.Instance.Load(samplePdf);
            await PrintPreviewController.Instance.Initialize();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private static async Task<StorageFile> GetSamplePdf()
        {
            Windows.ApplicationModel.Package package = Windows.ApplicationModel.Package.Current;
            Windows.Storage.StorageFolder installedLocation = package.InstalledLocation;

            StorageFile samplePdf = await StorageFileUtility.GetFileFromAppResource(PDF_FILE_PATH);
            return samplePdf;
        }

        /// <summary>
        /// Inserts sample data on first run of app after installation
        /// </summary>
        /// <returns>task</returns>
        public static async Task InsertSampleData()
        {
            bool isPreviouslyLoaded = false;

            var localSettings = ApplicationData.Current.LocalSettings;
            if (localSettings.Values.ContainsKey(KEY_ISSAMPLEDATAALREADYLOADED))
            {
                isPreviouslyLoaded = (bool)localSettings.Values[KEY_ISSAMPLEDATAALREADYLOADED];
            }
            else
            {
                localSettings.Values[KEY_ISSAMPLEDATAALREADYLOADED] = true;
            }

            if (!isPreviouslyLoaded)
            {
                //await _dbConnection.ExecuteAsync(string.Format(FORMAT_PRAGMA_FOREIGN_KEYS, OFF)); // Disable foreign keys
                await DatabaseController.Instance.ExecuteScript(DB_SAMPLE_DATA_FILE_PATH);
                //await _dbConnection.ExecuteAsync(string.Format(FORMAT_PRAGMA_FOREIGN_KEYS, ON)); // Re-enable foreign keys
            }
        }

    }
}
