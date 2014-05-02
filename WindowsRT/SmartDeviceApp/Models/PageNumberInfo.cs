using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Models
{
    public class PageNumberInfo
    {
        private uint _pageIndex;
        private uint _pageTotal;
        private PageViewMode _pageViewMode;
        
        public uint PageIndex
        {
            get { return _pageIndex; }
            set { _pageIndex = value; }
        }

        public uint PageTotal
        {
            get { return _pageTotal; }
            set { _pageTotal = value; }
        }

        public PageViewMode PageViewMode
        {
            get { return _pageViewMode; }
            set { _pageViewMode = value; }
        }

        public PageNumberInfo(uint rightPageIndex, 
            uint pageTotal, PageViewMode pageViewMode)
        {
            _pageIndex = rightPageIndex;
            _pageTotal = pageTotal;
            _pageViewMode = pageViewMode;
        }
    }
}
