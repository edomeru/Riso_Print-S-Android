//
//  DefaultsUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Models;
using System.Linq;

namespace SmartDeviceApp.Common.Utilities
{
    public static class DefaultsUtility
    {

        #region Print Settings

        /*
        public static PagePrintSetting CreateDefaultPrintSetting()
        {
            PagePrintSetting defaultPrintSetting = new PagePrintSetting();

            defaultPrintSetting.ColorMode           = (int)ColorMode.Auto;
            defaultPrintSetting.Orientation         = (int)Orientation.Portrait;
            defaultPrintSetting.Copies              = 1;
            defaultPrintSetting.Duplex              = (int)Duplex.Off;
            defaultPrintSetting.PaperSize           = (int)PaperSize.A4;
            defaultPrintSetting.ScaleToFit          = false;
            defaultPrintSetting.PaperType           = (int)PaperType.Any;
            defaultPrintSetting.InputTray           = (int)InputTray.Auto;
            defaultPrintSetting.Imposition          = (int)Imposition.Off;
            defaultPrintSetting.ImpositionOrder     = (int)ImpositionOrder.TwoUpRightToLeft;
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
         * */

        /// <summary>
        /// Gets the default print settings from list.
        /// </summary>
        /// <param name="printSettingList">print settings list</param>
        /// <returns>PagePrintSetting object</returns>
        public static PrintSettings GetDefaultPrintSettings(PrintSettingList printSettingList)
        {
            PrintSettings defaultPrintSettings = new PrintSettings();

            defaultPrintSettings.ColorMode =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_COLOR_MODE);
            defaultPrintSettings.Orientation =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_ORIENTATION);
            defaultPrintSettings.Copies =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_COPIES);
            defaultPrintSettings.Duplex =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_DUPLEX);
            defaultPrintSettings.PaperSize =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_PAPER_SIZE);
            defaultPrintSettings.ScaleToFit =
                (bool)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT);
            defaultPrintSettings.PaperType =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_PAPER_TYPE);
            defaultPrintSettings.InputTray =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_INPUT_TRAY);
            defaultPrintSettings.Imposition =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_IMPOSITION);
            defaultPrintSettings.ImpositionOrder =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);
            defaultPrintSettings.Sort =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_SORT);
            defaultPrintSettings.Booklet =
                (bool)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_BOOKLET);
            defaultPrintSettings.BookletFinishing =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
            defaultPrintSettings.BookletLayout =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
            defaultPrintSettings.FinishingSide =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_FINISHING_SIDE);
            defaultPrintSettings.Staple =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_STAPLE);
            defaultPrintSettings.Punch =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_PUNCH);
            defaultPrintSettings.OutputTray =
                (int)GetDefault(printSettingList, PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            return defaultPrintSettings;
        }

        /// <summary>
        /// Queries the list specifying the name
        /// </summary>
        /// <param name="printSettingList">print settings list</param>
        /// <param name="printSettingName">print setting name</param>
        /// <returns>"default" value</returns>
        private static object GetDefault(PrintSettingList printSettingList, string printSettingName)
        {
            object defaultValue = 0; // TODO: To update proper error handling

            var query = printSettingList.SelectMany(printSettingGroup => printSettingGroup.PrintSettings)
                .Where(ps => ps.Name == printSettingName);

            if (query != null && query.Count() > 0)
            {
                PrintSetting result = query.First();
                defaultValue = result.Default;
            }

            return defaultValue;
        }

        #endregion Print Settings

    }
}
