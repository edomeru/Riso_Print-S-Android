using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.Common.Utilities
{
    [TestClass]
    public class DefaultsUtilityTest
    {

        [TestMethod]
        public async Task Test_LoadDefaultsFromSqlScript_Null()
        {
            await DefaultsUtility.LoadDefaultsFromSqlScript(null);
        }

        [TestMethod]
        public void Test_GetDefaultValueFromSqlScript_NullKey_Boolean()
        {
            bool result = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(null, ListValueType.Boolean);
            Assert.IsFalse(result);
        }

        [TestMethod]
        public void Test_GetDefaultValueFromSqlScript_NullKey_Int()
        {
            int result = (int)DefaultsUtility.GetDefaultValueFromSqlScript(null, ListValueType.Int);
            Assert.AreEqual(-1, result);
        }

        [TestMethod]
        public void Test_GetDefaultValueFromSqlScript_NullKey_String()
        {
            string result = (string)DefaultsUtility.GetDefaultValueFromSqlScript(null, ListValueType.String);
            Assert.IsNull(result);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Null()
        {
            PrintSettings printSettings =
                DefaultsUtility.GetDefaultPrintSettings(null);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(-1, printSettings.ColorMode);
            Assert.AreEqual(-1, printSettings.Orientation);
            Assert.AreEqual(-1, printSettings.Copies);
            Assert.AreEqual(-1, printSettings.Duplex);
            Assert.AreEqual(-1, printSettings.PaperSize);
            Assert.AreEqual(false, printSettings.ScaleToFit);
            Assert.AreEqual(-1, printSettings.PaperType);
            Assert.AreEqual(-1, printSettings.InputTray);
            Assert.AreEqual(-1, printSettings.Imposition);
            Assert.AreEqual(-1, printSettings.ImpositionOrder);
            Assert.AreEqual(-1, printSettings.Sort);
            Assert.AreEqual(false, printSettings.Booklet);
            Assert.AreEqual(-1, printSettings.BookletFinishing);
            Assert.AreEqual(-1, printSettings.BookletLayout);
            Assert.AreEqual(-1, printSettings.FinishingSide);
            Assert.AreEqual(-1, printSettings.Staple);
            Assert.AreEqual(-1, printSettings.Punch);
            Assert.AreEqual(-1, printSettings.OutputTray);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_ColorMode()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_COLOR_MODE,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 1
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(1, printSettings.ColorMode);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Orientation()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_ORIENTATION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.Orientation);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Copies()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_COPIES,
                Type = PrintSettingType.numeric,
                Value = 100,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.Copies);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Duplex()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_DUPLEX,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.Duplex);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_PaperSize()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PAPER_SIZE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 2
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(2, printSettings.PaperSize);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_ScaleToFit()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT,
                Type = PrintSettingType.boolean,
                Value = false,
                Default = true
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.IsTrue(printSettings.ScaleToFit);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_PaperType()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PAPER_TYPE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.PaperType);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_InputTray()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_INPUT_TRAY,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.InputTray);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Imposition()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.Imposition);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_ImpositionOrder()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.ImpositionOrder);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Sort()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_SORT,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.Sort);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Booklet()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_BOOKLET,
                Type = PrintSettingType.numeric,
                Value = true,
                Default = false
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.IsFalse(printSettings.Booklet);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_BookletFinish()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.BookletFinishing);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_BookletLayout()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.BookletLayout);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_FinishingSide()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_FINISHING_SIDE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.FinishingSide);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Staple()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_STAPLE,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.Staple);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Punch()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_PUNCH,
                Type = PrintSettingType.numeric,
                Value = 1,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.Punch);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_OutputTray()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY,
                Type = PrintSettingType.numeric,
                Value = 2,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(0, printSettings.OutputTray);
        }

        [TestMethod]
        public void Test_GetDefaultPrintSettings_Unknown()
        {
            PrintSetting printSetting = new PrintSetting()
            {
                Name = "dummy_name",
                Type = PrintSettingType.numeric,
                Value = 100,
                Default = 0
            };
            PrintSettingGroup group = new PrintSettingGroup()
            {
                Name = "dummy_group",
                Text = "dummy_text",
                PrintSettings = new List<PrintSetting>()
                {
                    printSetting
                }
            };
            PrintSettingList list = new PrintSettingList()
            {
                group
            };

            PrintSettings printSettings = DefaultsUtility.GetDefaultPrintSettings(list);
            Assert.IsNotNull(printSettings);
            Assert.AreEqual(-1, printSettings.ColorMode);
            Assert.AreEqual(-1, printSettings.Orientation);
            Assert.AreEqual(-1, printSettings.Copies);
            Assert.AreEqual(-1, printSettings.Duplex);
            Assert.AreEqual(-1, printSettings.PaperSize);
            Assert.AreEqual(false, printSettings.ScaleToFit);
            Assert.AreEqual(-1, printSettings.PaperType);
            Assert.AreEqual(-1, printSettings.InputTray);
            Assert.AreEqual(-1, printSettings.Imposition);
            Assert.AreEqual(-1, printSettings.ImpositionOrder);
            Assert.AreEqual(-1, printSettings.Sort);
            Assert.AreEqual(false, printSettings.Booklet);
            Assert.AreEqual(-1, printSettings.BookletFinishing);
            Assert.AreEqual(-1, printSettings.BookletLayout);
            Assert.AreEqual(-1, printSettings.FinishingSide);
            Assert.AreEqual(-1, printSettings.Staple);
            Assert.AreEqual(-1, printSettings.Punch);
            Assert.AreEqual(-1, printSettings.OutputTray);
        }

    }
}
