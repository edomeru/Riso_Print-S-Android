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
        public static void Initialize()
        {
            InitializeDataStorage();
        }

        /// <summary>
        /// Initiates loading of PDF document
        /// </summary>
        /// <param name="e"></param>
        public static void FileActivationHandler(FileActivatedEventArgs e)
        {
            // Should handle only one file
            if (e.Files.Count != 1)
            {
                return;
            }

            DocumentController.Instance.Load(e.Files[0] as StorageFile);
        }

        /// <summary>
        /// Initializes the database and other data storage
        /// </summary>
        private static void InitializeDataStorage()
        {
            DatabaseController.Instance.Initialize();
        }

    }
}
