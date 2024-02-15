package de.gmuth.ipp.core

/**
 * Copyright (c) 2020 Gerhard Muth
 */

// name or text value, optional language

data class IppString(val text: String, val language: String? = null) {

    override fun toString() = "$text${if (language == null) "" else "[$language]"}"

}

fun String.toIppString(language: String? = null) = IppString(this, language)