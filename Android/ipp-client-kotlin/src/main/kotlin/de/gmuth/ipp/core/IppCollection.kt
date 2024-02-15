package de.gmuth.ipp.core

/**
 * Copyright (c) 2020-2022 Gerhard Muth
 */

import de.gmuth.log.Logging

// RFC8010 3.1.6.
data class IppCollection(val members: MutableList<IppAttribute<*>> = mutableListOf()) {

    companion object {
        val log = Logging.getLogger {}
    }

    constructor(vararg attributes: IppAttribute<*>) : this(attributes.toMutableList())

    fun addAttribute(name: String, tag: IppTag, vararg values: Any) =
        add(IppAttribute(name, tag, values.toMutableList()))

    fun add(attribute: IppAttribute<*>) =
        members.add(attribute)

    fun addAll(attributes: Collection<IppAttribute<*>>) =
        members.addAll(attributes)

    @Suppress("UNCHECKED_CAST")
    fun <T> getMember(memberName: String) =
        members.single { it.name == memberName } as IppAttribute<T>

    fun <T> getMemberValue(memberName: String) =
        getMember<T>(memberName).value

    override fun toString() =
        members.joinToString(" ", "{", "}") {
            "${it.name}=${it.values.joinToString(",")}"
        }

    fun logDetails(prefix: String = "") {
        val string = toString()
        if (string.length < 160) {
            log.info { "$prefix$string" }
        } else {
            members.forEach { it.logDetails(prefix) }
        }
    }

}