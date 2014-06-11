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

using SmartDeviceApp.ViewModels;
using System.Threading.Tasks;
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
            PrintPreviewController.Instance.Cleanup();

            // Reset to Home screen
            (new ViewModelLocator().HomeViewModel).IsProgressRingActive = true; // Enable loading
            new ViewModelLocator().ViewControlViewModel.GoToHomePage.Execute(null);

            await DocumentController.Instance.Load(file);
            await PrintPreviewController.Instance.Initialize();

            // Change to correct screen after loading
            new ViewModelLocator().ViewControlViewModel.GoToHomePage.Execute(null);
        }

        /// <summary>
        /// Clean-up
        /// </summary>
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

#if INITIAL_DATA_ON
            await InitialDataController.InsertSampleData();
#endif //INITIAL_DATA_ON
        }

        /// <summary>
        /// Other initializations
        /// </summary>
        /// <returns>task</returns>
        private static async Task InitializeControllers()
        {
            SettingController.Instance.Initialize();
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();
        }

    }
}
