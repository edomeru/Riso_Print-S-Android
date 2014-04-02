//
//  PrintSettingConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class PrintSettingConverter
    {

        public class ColorModeToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                ColorMode? convertedValue = null;

                if (Enum.IsDefined(typeof(ColorMode), intValue))
                {
                    convertedValue = (ColorMode)intValue;
                }

                return convertedValue;
            }
        }

        public class OrientationToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Orientation? convertedValue = null;

                if (Enum.IsDefined(typeof(Orientation), intValue))
                {
                    convertedValue = (Orientation)intValue;
                }

                return convertedValue;
            }
        }

        public class OrientationIntToBoolConverter
        {

            public static bool Convert(int value)
            {
                bool isPortrait = true;

                if ((int)Orientation.Landscape == value)
                {
                    isPortrait = false;
                }

                return isPortrait;
            }

        }

        public class DuplexToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Duplex? convertedValue = null;

                if (Enum.IsDefined(typeof(Duplex), intValue))
                {
                    convertedValue = (Duplex)intValue;
                }

                return convertedValue;
            }
        }

        public class DuplexIntToBoolConverter
        {

            public static bool Convert(int value)
            {
                bool convertedValue = (value == (int)Duplex.Off) ? false : true;

                return convertedValue;
            }

        }

        public class PaperSizeToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                PaperSize? convertedValue = null;

                if (Enum.IsDefined(typeof(PaperSize), intValue))
                {
                    convertedValue = (PaperSize)intValue;
                }

                return convertedValue;
            }
        }

        public class PaperTypeToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                PaperType? convertedValue = null;

                if (Enum.IsDefined(typeof(PaperType), intValue))
                {
                    convertedValue = (PaperType)intValue;
                }

                return convertedValue;
            }
        }

        public class PaperSizeIntToSizeConverter
        {

            public static Size Convert(int value)
            {
                Size paperSize;
                switch (value)
                {
                    case (int)PaperSize.A3W:
                        paperSize = PrintSettingConstant.PAPER_SIZE_A3W;
                        break;
                    case (int)PaperSize.A3:
                        paperSize = PrintSettingConstant.PAPER_SIZE_A3;
                        break;
                    case (int)PaperSize.A5:
                        paperSize = PrintSettingConstant.PAPER_SIZE_A5;
                        break;
                    case (int)PaperSize.A6:
                        paperSize = PrintSettingConstant.PAPER_SIZE_A6;
                        break;
                    case (int)PaperSize.B4:
                        paperSize = PrintSettingConstant.PAPER_SIZE_B4;
                        break;
                    case (int)PaperSize.B5:
                        paperSize = PrintSettingConstant.PAPER_SIZE_B5;
                        break;
                    case (int)PaperSize.Foolscap:
                        paperSize = PrintSettingConstant.PAPER_SIZE_FOOLSCAP;
                        break;
                    case (int)PaperSize.Tabloid:
                        paperSize = PrintSettingConstant.PAPER_SIZE_TABLOID;
                        break;
                    case (int)PaperSize.Legal:
                        paperSize = PrintSettingConstant.PAPER_SIZE_LEGAL;
                        break;
                    case (int)PaperSize.Letter:
                        paperSize = PrintSettingConstant.PAPER_SIZE_LETTER;
                        break;
                    case (int)PaperSize.Statement:
                        paperSize = PrintSettingConstant.PAPER_SIZE_STATEMENT;
                        break;
                    case (int)PaperSize.A4:
                    default:
                        paperSize = PrintSettingConstant.PAPER_SIZE_A4;
                        break;
                }

                return paperSize;
            }
        }

        public class InputTrayToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                InputTray? convertedValue = null;

                if (Enum.IsDefined(typeof(InputTray), intValue))
                {
                    convertedValue = (InputTray)intValue;
                }

                return convertedValue;
            }
        }

        public class ImpositionToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Imposition? convertedValue = null;

                if (Enum.IsDefined(typeof(Imposition), intValue))
                {
                    convertedValue = (Imposition)intValue;
                }

                return convertedValue;
            }
        }

        public class ImpositionIntToNumberOfPagesConverter
        {

            public static int Convert(int value)
            {
                int pagesPerSheet = 1;
                switch (value)
                {
                    case (int)Imposition.TwoUp:
                        pagesPerSheet = 2;
                        break;
                    case (int)Imposition.FourUp:
                        pagesPerSheet = 4;
                        break;
                    case (int)Imposition.Off:
                    default:
                        // Do nothing
                        break;
                }

                return pagesPerSheet;
            }

        }

        public class ImpositionOrderToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                ImpositionOrder? convertedValue = null;

                if (Enum.IsDefined(typeof(ImpositionOrder), intValue))
                {
                    convertedValue = (ImpositionOrder)intValue;
                }

                return convertedValue;
            }
        }

        public class SortToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Sort? convertedValue = null;

                if (Enum.IsDefined(typeof(Sort), intValue))
                {
                    convertedValue = (Sort)intValue;
                }

                return convertedValue;
            }
        }

        public class BookletFinishingToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                BookletFinishing? convertedValue = null;

                if (Enum.IsDefined(typeof(BookletFinishing), intValue))
                {
                    convertedValue = (BookletFinishing)intValue;
                }

                return convertedValue;
            }
        }

        public class BookletLayoutToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                BookletLayout? convertedValue = null;

                if (Enum.IsDefined(typeof(BookletLayout), intValue))
                {
                    convertedValue = (BookletLayout)intValue;
                }

                return convertedValue;
            }
        }

        public class FinishingSideToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                FinishingSide? convertedValue = null;

                if (Enum.IsDefined(typeof(FinishingSide), intValue))
                {
                    convertedValue = (FinishingSide)intValue;
                }

                return convertedValue;
            }
        }

        public class StapleToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Staple? convertedValue = null;

                if (Enum.IsDefined(typeof(Staple), intValue))
                {
                    convertedValue = (Staple)intValue;
                }

                return convertedValue;
            }
        }

        public class PunchToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Punch? convertedValue = null;

                if (Enum.IsDefined(typeof(Punch), intValue))
                {
                    convertedValue = (Punch)intValue;
                }

                return convertedValue;
            }
        }

        public class PunchIntToNumberOfHolesConverter
        {

            public static int Convert(int value)
            {
                int numberOfHoles = 0;
                switch (value)
                {
                    case (int)Punch.TwoHoles:
                        numberOfHoles = 2;
                        break;
                    case (int)Punch.ThreeHoles:
                        numberOfHoles = 3;
                        break;
                    case (int)Punch.FourHoles:
                        numberOfHoles = 4;
                        break;
                    case (int)Punch.Off:
                    default:
                        // Do nothing
                        break;
                }

                return numberOfHoles;
            }

        }

        public class PunchIntToDistanceBetweenHolesConverter
        {

            public static double Convert(int value)
            {
                double distance = 0;
                switch (value)
                {
                    case (int)Punch.TwoHoles:
                        distance = PrintSettingConstant.PUNCH_BETWEEN_TWO_HOLES_DISTANCE;
                        break;
                    case (int)Punch.ThreeHoles:
                        distance = PrintSettingConstant.PUNCH_BETWEEN_THREE_HOLES_DISTANCE;
                        break;
                    case (int)Punch.FourHoles:
                        distance = PrintSettingConstant.PUNCH_BETWEEN_FOUR_HOLES_DISTANCE;
                        break;
                    case (int)Punch.Off:
                    default:
                        // Do nothing
                        break;
                }

                return distance * ImageConstant.BASE_DPI;
            }

        }

        public class OutputTrayToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                OutputTray? convertedValue = null;

                if (Enum.IsDefined(typeof(OutputTray), intValue))
                {
                    convertedValue = (OutputTray)intValue;
                }

                return convertedValue;
            }
        }

    }

}
