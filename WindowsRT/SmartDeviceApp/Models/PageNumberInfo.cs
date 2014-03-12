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
        private uint _leftPageIndex;
        private uint _rightPageIndex;
        private uint _pageTotal;
        private PageViewMode _viewMode;

        public uint LeftPageIndex
        {
            get { return _leftPageIndex; }
            set { _leftPageIndex = value; }
        }

        public uint RightPageIndex
        {
            get { return _rightPageIndex; }
            set { _rightPageIndex = value; }
        }

        public uint PageTotal
        {
            get { return _pageTotal; }
            set { _pageTotal = value; }
        }

        public PageViewMode ViewMode
        {
            get { return _viewMode; }
            set { _viewMode = value; }
        }

        public PageNumberInfo(uint leftPageIndex, uint rightPageIndex, 
            uint pageTotal, PageViewMode viewMode)
        {
            _leftPageIndex = leftPageIndex;
            _rightPageIndex = rightPageIndex;
            _pageTotal = pageTotal;
            _viewMode = viewMode;
        }
    }
}
