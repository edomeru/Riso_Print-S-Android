﻿//
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
            PrintPreviewController.Instance.Cleanup();

            StorageFile samplePdf = await StorageFileUtility.GetFileFromAppResource(PDF_FILE_PATH);
            await DocumentController.Instance.Load(samplePdf);
            await PrintPreviewController.Instance.Initialize();
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
                await DatabaseController.Instance.EnablePragmaForeignKeys(false); // Disable foreign keys temporarily
                await DatabaseController.Instance.ExecuteScript(DB_SAMPLE_DATA_FILE_PATH);
                await DatabaseController.Instance.EnablePragmaForeignKeys(true); // Re-enable foreign keys
            }
        }

    }
}
