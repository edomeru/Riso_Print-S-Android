//
//  LogUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/30.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Diagnostics;

namespace SmartDeviceApp.Common.Utilities
{
    public static class LogUtility
    {
        public const string ERROR_RESOURCE_STRING_NOT_FOUND = "Resource string not found";

        private const string FORMAT_LOG_START = "{0}: [{1}] START";
        private const string FORMAT_LOG_END = "{0}: [{1}] END, elapsed time = {2}sec.";

        private static Dictionary<string, DateTime> _logMap = new Dictionary<string, DateTime>();

        /// <summary>
        /// Logs a message to the debugger console
        /// </summary>
        public static void LogMessage()
        {
            // TODO: Put body here
        }

        /// <summary>
        /// Logs an error message to the debugger console
        /// </summary>
        /// <param name="ex"></param>
        public static void LogError(Exception ex)
        {
            Debug.WriteLine(String.Format("{0}: ERROR: {1}", 
                DateTime.Now,
                ex.ToString()));
        }

        /// <summary>
        /// Marks the start time given the key
        /// </summary>
        /// <param name="key">key</param>
        public static void BeginTimestamp(string key)
        {
#if PERFORMANCE_LOG
            DateTime currTime = DateTime.Now;
            if (!_logMap.ContainsKey(key))
            {
                _logMap.Add(key, currTime);
                Debug.WriteLine(String.Format(FORMAT_LOG_START, currTime, key));
            }
#endif
        }

        /// <summary>
        /// Marks the end time given the key and logs the elapsed time (in seconds) to the
        /// debugger console
        /// </summary>
        /// <param name="key">key</param>
        public static void EndTimestamp(string key)
        {
#if PERFORMANCE_LOG
            DateTime startTime;
            DateTime currTime = DateTime.Now;
            if (_logMap.TryGetValue(key, out startTime))
            {
                _logMap.Remove(key);
                TimeSpan elapsedTime = currTime - startTime;
                Debug.WriteLine(String.Format(FORMAT_LOG_END, currTime, key, elapsedTime.TotalSeconds));
            }
#endif
        }

    }
}
