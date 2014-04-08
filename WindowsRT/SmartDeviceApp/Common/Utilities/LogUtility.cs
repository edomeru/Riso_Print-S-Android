using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Common.Utilities
{
    public static class LogUtility
    {
        public const string ERROR_RESOURCE_STRING_NOT_FOUND = "Resource string not found";

        public static void LogMessage()
        {

        }

        public static void LogError(Exception ex)
        {
            Debug.WriteLine(String.Format("{0}: ERROR: {1}", 
                DateTime.Now,
                ex.ToString()));
        }
    }
}
