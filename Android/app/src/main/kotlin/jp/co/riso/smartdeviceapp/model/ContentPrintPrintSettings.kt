package jp.co.riso.smartdeviceapp.model

import com.google.gson.annotations.SerializedName
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings

class ContentPrintPrintSettings {
    @SerializedName("colorMode")
    var colorMode: Int = 0

    @SerializedName("orientation")
    var orientation: Int = 0

    @SerializedName("copies")
    var copies: Int = 1

    @SerializedName("duplex")
    var duplex: Int = 0

    @SerializedName("paperSize")
    var paperSize: Int = 0

    @SerializedName("scaleToFit")
    var scaleToFit: Boolean = true

    @SerializedName("paperType")
    var paperType: Int = 0

    @SerializedName("inputTray")
    var inputTray: Int = 0

    @SerializedName("imposition")
    var imposition: Int = 0

    @SerializedName("impositionOrder")
    var impositionOrder: Int = 0

    @SerializedName("sort")
    var sort: Int = 0

    @SerializedName("booklet")
    var booklet: Boolean = false

    @SerializedName("bookletFinish")
    var bookletFinish: Int = 0

    @SerializedName("bookletLayout")
    var bookletLayout: Int = 0

    @SerializedName("finishingSide")
    var finishingSide: Int = 0

    @SerializedName("staple")
    var staple: Int = 0

    @SerializedName("punch")
    var punch: Int = 0

    @SerializedName("outputTray")
    var outputTray: Int = 0

    @SerializedName("loginId")
    var loginId: String? = null

    companion object {
        fun convertToPrintSettings(contentPrintPrintSettings: ContentPrintPrintSettings): PrintSettings {
            val printSettings = PrintSettings()
            printSettings.setValue(PrintSettings.TAG_COLOR_MODE, contentPrintPrintSettings.colorMode)
            printSettings.setValue(PrintSettings.TAG_ORIENTATION, contentPrintPrintSettings.orientation)
            printSettings.setValue(PrintSettings.TAG_COPIES, contentPrintPrintSettings.copies)
            printSettings.setValue(PrintSettings.TAG_DUPLEX, contentPrintPrintSettings.duplex)
            printSettings.setValue(PrintSettings.TAG_PAPER_SIZE, contentPrintPrintSettings.paperSize)
            printSettings.setValue(PrintSettings.TAG_SCALE_TO_FIT, if (contentPrintPrintSettings.scaleToFit) 1 else 0)
            //printSettings.setValue(PrintSettings.TAG_PAPER_TYPE, contentPrintPrintSettings.paperType)
            printSettings.setValue(PrintSettings.TAG_INPUT_TRAY, contentPrintPrintSettings.inputTray)
            printSettings.setValue(PrintSettings.TAG_IMPOSITION, contentPrintPrintSettings.imposition)
            printSettings.setValue(PrintSettings.TAG_IMPOSITION_ORDER, contentPrintPrintSettings.impositionOrder)
            printSettings.setValue(PrintSettings.TAG_SORT, contentPrintPrintSettings.sort)
            printSettings.setValue(PrintSettings.TAG_BOOKLET, if (contentPrintPrintSettings.booklet) 1 else 0)
            printSettings.setValue(PrintSettings.TAG_BOOKLET_FINISH, contentPrintPrintSettings.bookletFinish)
            printSettings.setValue(PrintSettings.TAG_BOOKLET_LAYOUT, contentPrintPrintSettings.bookletLayout)
            printSettings.setValue(PrintSettings.TAG_FINISHING_SIDE, contentPrintPrintSettings.finishingSide)
            printSettings.setValue(PrintSettings.TAG_STAPLE, contentPrintPrintSettings.staple)
            printSettings.setValue(PrintSettings.TAG_PUNCH, contentPrintPrintSettings.punch)
            printSettings.setValue(PrintSettings.TAG_OUTPUT_TRAY, contentPrintPrintSettings.outputTray)
            return printSettings
        }

        fun convertToContentPrintPrintSettings(printSettings: PrintSettings): ContentPrintPrintSettings {
            val contentPrintPrintSettings = ContentPrintPrintSettings()
            contentPrintPrintSettings.colorMode = printSettings.getValue(PrintSettings.TAG_COLOR_MODE)
            contentPrintPrintSettings.orientation = printSettings.getValue(PrintSettings.TAG_ORIENTATION)
            contentPrintPrintSettings.copies = printSettings.getValue(PrintSettings.TAG_COPIES)
            contentPrintPrintSettings.duplex = printSettings.getValue(PrintSettings.TAG_DUPLEX)
            contentPrintPrintSettings.paperSize = printSettings.getValue(PrintSettings.TAG_PAPER_SIZE)
            contentPrintPrintSettings.scaleToFit = (printSettings.getValue(PrintSettings.TAG_SCALE_TO_FIT) == 1)
            //contentPrintPrintSettings.paperType = printSettings.getValue(PrintSettings.TAG_PAPER_TYPE)
            contentPrintPrintSettings.inputTray = printSettings.getValue(PrintSettings.TAG_INPUT_TRAY)
            contentPrintPrintSettings.imposition = printSettings.getValue(PrintSettings.TAG_IMPOSITION)
            contentPrintPrintSettings.impositionOrder = printSettings.getValue(PrintSettings.TAG_IMPOSITION_ORDER)
            contentPrintPrintSettings.sort = printSettings.getValue(PrintSettings.TAG_SORT)
            contentPrintPrintSettings.booklet = (printSettings.getValue(PrintSettings.TAG_BOOKLET) == 1)
            contentPrintPrintSettings.bookletFinish = printSettings.getValue(PrintSettings.TAG_BOOKLET_FINISH)
            contentPrintPrintSettings.bookletLayout = printSettings.getValue(PrintSettings.TAG_BOOKLET_LAYOUT)
            contentPrintPrintSettings.finishingSide = printSettings.getValue(PrintSettings.TAG_FINISHING_SIDE)
            contentPrintPrintSettings.staple = printSettings.getValue(PrintSettings.TAG_STAPLE)
            contentPrintPrintSettings.punch = printSettings.getValue(PrintSettings.TAG_PUNCH)
            contentPrintPrintSettings.outputTray = printSettings.getValue(PrintSettings.TAG_OUTPUT_TRAY)
            return contentPrintPrintSettings
        }
    }
}