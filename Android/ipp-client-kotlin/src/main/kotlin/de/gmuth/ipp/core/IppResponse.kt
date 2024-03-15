package de.gmuth.ipp.core

/**
 * Copyright (c) 2020-2021 Gerhard Muth
 */

import de.gmuth.ipp.core.IppTag.Printer
import de.gmuth.ipp.core.IppTag.Unsupported
import java.nio.charset.Charset

class IppResponse : IppMessage {

    override val codeDescription: String
        get() = status.toString()

    var status: IppStatus
        get() = IppStatus.fromShort(code!!)
        set(ippStatus) {
            code = ippStatus.code
        }

    // https://datatracker.ietf.org/doc/html/rfc8011#page-42
    val statusMessage: IppString
        get() = operationGroup.getValue("status-message")

    fun hasStatusMessage() =
            operationGroup.containsKey("status-message")

    val printerGroup: IppAttributesGroup
        get() = getSingleAttributesGroup(Printer)

    val unsupportedGroup: IppAttributesGroup
        get() = getSingleAttributesGroup(Unsupported)

    fun isSuccessful() = status.isSuccessful()

    constructor() : super()

    constructor(
            status: IppStatus,
            version: String = "1.1",
            requestId: Int = 1,
            charset: Charset = Charsets.UTF_8,
            naturalLanguage: String = "en"
    ) : super(version, requestId, charset, naturalLanguage) {
        code = status.code
    }
}