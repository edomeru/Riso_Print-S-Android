/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * XmlNode.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model.printsettings

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

/**
 * @class XmlNode
 *
 * @brief Abstract data class for each XML node.
 *
 * @param node Node element in XML.
 */
abstract class XmlNode(node: Node) {
    private var _attributes: NamedNodeMap = node.attributes

    /**
     * @brief Gets attribute value of a specified key in XML. Represented by key="<value>".
     *
     * @param key Attribute key in XML.
     *
     * @return Attribute value of the specified key
     * @retval "" Key is invalid
     */
    fun getAttributeValue(key: String?): String {
        return if (_attributes.getNamedItem(key) == null) {
            ""
        } else _attributes.getNamedItem(key).nodeValue
    }

    companion object {
        const val ATTR_NAME = "name" ///< key used to retrieve "name" attribute in XML
        const val ATTR_ICON = "icon" ///< key used to retrieve "icon" attribute in XML
        const val ATTR_TEXT = "text" ///< key used to retrieve "text" attribute in XML
        const val ATTR_TYPE = "type" ///< key used to retrieve "type" attribute in XML
        const val NODE_GROUP = "group" ///< key used to retrieve "group" node in XML
    }

}