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
        public ViewOrientation viewOrientation
        {
            get;
            set;
        }
        public int columns
        {
            get;
            set;
        }
    }
}
