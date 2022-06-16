/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * Group.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model.printsettings

import org.w3c.dom.Node

/**
 * @class Group
 *
 * @brief Group data class representing a group in PrintSettings.
 *
 * Data represented by <group> tag in XML.
 */
class Group(groupNode: Node) : XmlNode(groupNode) {
    private val _settings: MutableList<Setting>

    /**
     * @brief Gets settings of a Group.
     *
     * @return List of Setting objects.
     */
    val settings: List<Setting>
        get() = _settings

    /**
     * @brief Creates a Group instance.
     *
     * @param groupNode Node represented by \<group></group>\> tag in XML.
     */
    init {
        _settings = ArrayList()
        val settingsList = groupNode.childNodes
        var i = 1
        while (i < settingsList.length) {
            _settings.add(Setting(settingsList.item(i)))
            i += 2
        }
    }
}