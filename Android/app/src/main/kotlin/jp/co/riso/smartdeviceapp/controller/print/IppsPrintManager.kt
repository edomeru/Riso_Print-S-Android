package jp.co.riso.smartdeviceapp.controller.print

import de.gmuth.ipp.client.CupsClient
import de.gmuth.ipp.client.IppJob
import de.gmuth.ipp.client.IppJobState
import de.gmuth.ipp.client.IppSubscription
import de.gmuth.ipp.client.IppTemplateAttributes.documentFormat
import de.gmuth.ipp.client.IppTemplateAttributes.jobName
import de.gmuth.ipp.core.IppAttribute
import de.gmuth.ipp.core.IppString
import de.gmuth.ipp.core.IppTag
import de.gmuth.ipp.core.toIppString
import jp.co.riso.android.util.FileUtils.delete
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.common.DirectPrintManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URI

class IppsPrintManager (
    private val directPrintManager: DirectPrintManager,
    var pageCount: Int,
    var ipAddress: String,
    var fileName: String,
    private var jobName: String
) {

    private var _ippsJob: IppJob? = null

    // PDF+PJL File
    private val PDF_PJL_DIRNAME = "PDF_PJL_TMP"
    private val PDF_PJL_FILENAME = "PDF_PJL.pdf"
    private val KEY_USER_NAME = "requesting-user-name"
    private val VALUE_USER_NAME = "RISO PRINT-S"

    fun print() {
        CoroutineScope(Dispatchers.IO).launch {
            kotlin.runCatching {
                try {
                    val ippPrinter =
                        CupsClient(URI.create(getIppsPath(ipAddress))).getPrinters().first()
                    val file = File(getFilePath())
                    _ippsJob = ippPrinter.printJob(
                        file,
                        IppAttribute<IppString>(KEY_USER_NAME, IppTag.NameWithoutLanguage, VALUE_USER_NAME.toIppString()),
                        jobName(jobName),
                        documentFormat(DOCUMENT_FORMAT),
                        notifyEvents = listOf(
                            "job-stopped",
                            "job-completed",
                            "job-progress"
                        )
                    )
                    _ippsJob!!.apply {
                        subscription!!.logDetails()
                        initializeStatusMonitoring(subscription!!, pageCount)
                        sendDocument(file)
                        waitForTermination()
                        finalizePrintStatus()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    directPrintManager.onNotifyProgress(
                        DirectPrintManager.PRINT_STATUS_ERROR_CONNECTING,
                        0F
                    )
                } finally {
                    cleanUp()
                }
            }
        }
    }

    fun cancel() {
        _ippsJob?.apply{
            try {
                if (!isTerminated()) {
                    cancel()
                    cleanUp()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ================================================================================
    // Private Methods
    // ================================================================================
    private fun getFilePath(): String {
        return SmartDeviceApp.appContext!!.getExternalFilesDir(AppConstants.CONST_PDF_DIR)
            .toString() + "/" + PDF_PJL_DIRNAME + "/" + PDF_PJL_FILENAME
    }

    private fun initializeStatusMonitoring(eventSubscription: IppSubscription, pageCount: Int) {
        val eventTotal = pageCount + 3
        eventSubscription.processEvents(PRINT_PROGRESS_INTERVAL) {
            if (it.jobState == IppJobState.Processing) {
                val eventCount = it.sequenceNumber
                val progress = if (eventCount < eventTotal) {
                    ((eventCount.toFloat()/eventTotal.toFloat()) * 100)
                } else {
                    PRINTJOB_SENT_PROGRESS_PERCENTAGE
                }
                directPrintManager.onNotifyProgress(
                    DirectPrintManager.PRINT_STATUS_SENDING,
                    progress
                )
            }
        }
    }

    private fun finalizePrintStatus() {
        _ippsJob!!.apply {
            val status = when (state) {
                IppJobState.Completed -> {
                    // Show user 100% status before Job Completion status
                    directPrintManager.onNotifyProgress(
                        DirectPrintManager.PRINT_STATUS_SENDING,
                        PRINTJOB_SENT_PROGRESS_PERCENTAGE
                    )
                    DirectPrintManager.PRINT_STATUS_SENT
                }
                IppJobState.Aborted -> DirectPrintManager.PRINT_STATUS_ERROR
                IppJobState.PendingHeld -> DirectPrintManager.PRINT_STATUS_ERROR_CONNECTING
                IppJobState.ProcessingStopped -> DirectPrintManager.PRINT_STATUS_ERROR_SENDING
                else -> DirectPrintManager.PRINT_STATUS_ERROR
            }
            if (state != IppJobState.Canceled) {
                directPrintManager.onNotifyProgress(
                    status,
                    PRINTJOB_SENT_PROGRESS_PERCENTAGE
                )
            }
        }
    }

    private fun cleanUp() {
        // delete PDF + PJL file
        try {
            delete(File(getFilePath()))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // set IppJob to null
        if (_ippsJob != null) {
            _ippsJob = null
        }
    }

    companion object {
        const val PRINTJOB_SENT_PROGRESS_PERCENTAGE = 100.0f
        const val PRINT_PROGRESS_INTERVAL = 1000L * 2
        const val DOCUMENT_FORMAT = "application/pdf"

        fun getIppsPath(ipAddress: String): String {
            return "ipps://" + ipAddress + ":" + AppConstants.CONST_PORT_IPPS
        }
    }
}