//
//  MainController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.ApplicationModel.Activation;
using Windows.Storage;

namespace SmartDeviceApp.Controllers
{
    public sealed class MainController
    {
        private MainController() { }

        /// <summary>
        /// Initialization
        /// </summary>
        public async static void Initialize()
        {
            await InitializeDataStorage();
            await InitializeControllers();
        }

        /// <summary>
        /// Initiates loading of PDF document
        /// </summary>
        /// <param name="file">PDF file</param>
        /// <returns>task</returns>
        public async static Task FileActivationHandler(StorageFile file)
        {
            if (file == null)
            {
                return;
            }

            await DocumentController.Instance.Unload();
            await PrintPreviewController.Instance.Cleanup();

            await DocumentController.Instance.Load(file);
            await PrintPreviewController.Instance.Initialize();
        }

        public static void Cleanup()
        {
            DatabaseController.Instance.Cleanup();
        }

        /// <summary>
        /// Initializes the database and other data storage
        /// </summary>
        /// <returns>task</returns>
        private static async Task InitializeDataStorage()
        {
            await DatabaseController.Instance.Initialize();
        }

        /// <summary>
        /// Other initializations (TBD)
        /// </summary>
        /// <returns>task</returns>
        private static async Task InitializeControllers()
        {
            // TODO: Verify timing of each initialization
            SettingController.Instance.Initialize();
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();
        }

        #region TEST - Sample PDF Page - FOR DELETION --------------------------------------------------------------------------------

        public async static Task InitializeSamplePdf()
        {
            await DocumentController.Instance.Unload();
            await PrintPreviewController.Instance.Cleanup();

            StorageFile samplePdf = await DummyControllers.DummyProvider.Instance.GetSamplePdf();
            await DocumentController.Instance.Load(samplePdf);
            await PrintPreviewController.Instance.Initialize();
        }

        #endregion TEST - Sample PDF Page - FOR DELETION -----------------------------------------------------------------------------

    }
}
