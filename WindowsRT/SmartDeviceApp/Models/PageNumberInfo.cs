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
        
        /// <summary>
        /// Page index (zero-based)
        /// </summary>
        public uint PageIndex
        {
            get { return _pageIndex; }
            set { _pageIndex = value; }
        }

        /// <summary>
        /// Page total
        /// </summary>
        public uint PageTotal
        {
            get { return _pageTotal; }
            set { _pageTotal = value; }
        }

        /// <summary>
        /// Page view mode
        /// </summary>
        public PageViewMode PageViewMode
        {
            get { return _pageViewMode; }
            set { _pageViewMode = value; }
        }

        /// <summary>
        /// Constructor of PageNumberInfo
        /// </summary>
        /// <param name="rightPageIndex">right page index</param>
        /// <param name="pageTotal">page total</param>
        /// <param name="pageViewMode">page view mode</param>
        /// <param name="isBooklet">true when booklet is enabled, false otherwise</param>
        public PageNumberInfo(uint rightPageIndex, 
            uint pageTotal, PageViewMode pageViewMode, bool isBooklet)
        {
            _pageIndex = rightPageIndex;
            _pageTotal = pageTotal;
            _pageViewMode = pageViewMode;

            _pageTotal = getPageCount(isBooklet);
            getPageIndex();
        }

        private uint getPageCount(bool isBooklet)
        {
            uint count = _pageTotal;

            if (_pageViewMode != Common.Enum.PageViewMode.SinglePageView)
            {
                count = getNextIntegerMultiple(_pageTotal, 2);

                if (isBooklet)
                {
                    count = getNextIntegerMultiple(_pageTotal, 4);
                }
            }
            

            return count;
        }

        private uint getNextIntegerMultiple(uint n, int m)
        {
            if (m == 0)
            {
                return n;
            }

            if (n % m != 0)
            {
                return (uint)(n + (m - n % m));
            }
            return n;
        }

        private void getPageIndex()
        {
            if (_pageViewMode != Common.Enum.PageViewMode.SinglePageView)
            {
                if (_pageIndex > 1)
                {
                    _pageIndex *= 2;
                    _pageIndex--;
                }
                    
            }
        }
    }
}
