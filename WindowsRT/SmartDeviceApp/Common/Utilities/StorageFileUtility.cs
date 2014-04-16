//
//  StorageFileUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/05.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Threading.Tasks;
using Windows.Storage;

namespace SmartDeviceApp.Common.Utilities
{
    public static class StorageFileUtility
    {

        /// <summary>
        /// Retrives the file from the specified location
        /// </summary>
        /// <param name="fileName">file name</param>
        /// <param name="folderLocation">folder location</param>
        /// <returns>task; file if the file exists, else null</returns>
        public async static Task<StorageFile> GetExistingFile(string fileName,
            StorageFolder folderLocation)
        {
            StorageFile storageFile = null;
            try
            {
                storageFile = await folderLocation.GetFileAsync(fileName);
            }
            catch (System.IO.FileNotFoundException)
            {
                // File does not exist
            }

            return storageFile;
        }

        /// <summary>
        /// Deletes all files inside AppData temporary store
        /// </summary>
        /// <returns>task</returns>
        public async static Task DeleteAllTempFiles()
        {
            StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
            var files = await tempFolder.GetFilesAsync();
            foreach (var file in files)
            {
                await file.DeleteAsync(StorageDeleteOption.PermanentDelete);
            }
        }

        /// <summary>
        /// Deletes all files with specified part of the file name
        /// </summary>
        /// <param name="keyword">substring of the file name to be deleted</param>
        /// <param name="folderLocation">folder location</param>
        /// <returns>task</returns>
        public async static Task DeleteFiles(string keyword, StorageFolder folderLocation)
        {
            var files = await folderLocation.GetFilesAsync();
            foreach (var file in files)
            {
                if (file.Name.Contains(keyword))
                {
                    await file.DeleteAsync(StorageDeleteOption.PermanentDelete);
                }
            }
        }

        /// <summary>
        /// Deletes a file (exact file name match)
        /// </summary>
        /// <param name="fileName">file name to be deleted</param>
        /// <param name="folderLocation">folder location</param>
        /// <returns>task</returns>
        public async static Task DeleteFile(string fileName, StorageFolder folderLocation)
        {
            var file = await GetExistingFile(fileName, folderLocation);
            if (file != null)
            {
                await file.DeleteAsync(StorageDeleteOption.PermanentDelete);
            }
        }

        /// <summary>
        /// Retrieves the file from the installation path
        /// </summary>
        /// <param name="filePath">relative file path</param>
        /// <returns>resource file</returns>
        public async static Task<StorageFile> GetFileFromAppResource(string filePath)
        {
            Uri uri = new Uri("ms-appx:///" + filePath);
            return await StorageFile.GetFileFromApplicationUriAsync(uri);
        }

    }
}
