using SmartDeviceApp.Common.Enum;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Models
{
    public class ViewItemParameters
    {
        /// <summary>
        /// Gets/sets the target view orientation
        /// </summary>
        public ViewOrientation viewOrientation
        {
            get;
            set;
        }

        /// <summary>
        /// Gets/sets the target number of columns (for Printers screen)
        /// </summary>
        public int columns
        {
            get;
            set;
        }
    }
}
