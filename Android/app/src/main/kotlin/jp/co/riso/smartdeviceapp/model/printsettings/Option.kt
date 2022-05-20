/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * Option.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model.printsettings

import org.w3c.dom.Node

/**
 * @class Option
 *
 * @brief Option data class representing an option in PrintSettings.
 *
 * Data represented by <option> tag in XML.
 */
class Option(optionNode: Node) : XmlNode(optionNode) {
    /**
     * @brief Gets text content of the group node in XML.
     *
     * @return Text content of the group
     */
    val textContent: String = optionNode.textContent

}