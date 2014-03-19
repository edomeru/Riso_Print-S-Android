using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Common.Utilities
{
    public class DefaultsUtility
    {
        public static PrintSetting CreateDefaultPrintSetting()
        {
            PrintSetting defaultPrintSetting = new PrintSetting();

            defaultPrintSetting.ColorMode           = (int)ColorMode.Auto; ;
            defaultPrintSetting.Orientation         = (int)Orientation.Portrait;
            defaultPrintSetting.Copies              = 1;
            defaultPrintSetting.Duplex              = (int)Duplex.Off;
            defaultPrintSetting.PaperSize           = (int)PaperSize.A4;
            defaultPrintSetting.ScaleToFit          = false;
            defaultPrintSetting.PaperType           = (int)PaperType.Any;
            defaultPrintSetting.InputTray           = (int)InputTray.Auto;
            defaultPrintSetting.Imposition          = (int)Imposition.Off;
            defaultPrintSetting.ImpositionOrder     = (int)ImpositionOrder.RightToLeft;
            defaultPrintSetting.Sort                = (int)Sort.PerPage;
            defaultPrintSetting.Booklet             = false;
            defaultPrintSetting.BookletFinishing    = (int)BookletFinishing.PaperFolding;
            defaultPrintSetting.BookletLayout       = (int)BookletLayout.LeftToRight;
            defaultPrintSetting.FinishingSide       = (int)FinishingSide.Left;
            defaultPrintSetting.Staple              = (int)Staple.Off;
            defaultPrintSetting.Punch               = (int)Punch.Off;
            defaultPrintSetting.OutputTray          = (int)OutputTray.Auto;

            return defaultPrintSetting;
        }
    }
}
