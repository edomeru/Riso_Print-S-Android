/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * Setting.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model.printsettings

import org.w3c.dom.Node
import kotlin.Int
import kotlin.String

/**
 * @class Setting
 *
 * @brief Setting data class representing a setting in PrintSettings.
 *
 * @param settingNode Node represented by <setting> tag in XML.
 */
class Setting(settingNode: Node) : XmlNode(settingNode) {
    private val mOptions: MutableList<Option>

    /**
     * @brief Gets options of the setting.
     *
     * @return List of Option objects
     */
    val options: List<Option>
        get() = mOptions

    /**
     * @brief Gets the type of a Setting. The value of attribute \"type\" in XML.
     *
     * @retval ATTR_VAL_LIST type="list"
     * @retval ATTR_VAL_BOOLEAN type="boolean"
     * @retval ATTR_VAL_NUMERIC type="numeric"
     * @retval TYPE_INVALID type is not list, boolean or numeric
     */
    val type: Int
        get() {
            val type = getAttributeValue(ATTR_TYPE)
            return when {
                type.equals(ATTR_VAL_LIST, ignoreCase = true) -> {
                    TYPE_LIST
                }
                type.equals(ATTR_VAL_BOOLEAN, ignoreCase = true) -> {
                    TYPE_BOOLEAN
                }
                type.equals(ATTR_VAL_NUMERIC, ignoreCase = true) -> {
                    TYPE_NUMERIC
                }
                else -> TYPE_INVALID
            }
        }

    /**
     * @brief Gets the default value of a Setting. The value of attribute \"default\" in XML.
     *
     * @return The default value as an integer.
     * @retval 1 Setting's attribute type is boolean and value is true.
     * @retval 0 Setting's attribute type is boolean and value is false.
     * @retval -1 Setting's attribute type is invalid.
     */
    val defaultValue: Int
        get() {
            val value = getAttributeValue(ATTR_DEFAULT)
            when (type) {
                TYPE_LIST, TYPE_NUMERIC -> return value.toInt()
                TYPE_BOOLEAN -> return if (value.toBoolean()) 1 else 0
            }
            return -1
        }

    /**
     * @brief Gets the database column name of a Setting. The value of attribute \"dbkey\" in XML.
     *
     * @return Database key
     */
    val dbKey: String
        get() = getAttributeValue(ATTR_DBKEY)

    companion object {
        const val ATTR_DEFAULT = "default" ///< key used to retrieve attribute "default" in XML
        const val ATTR_VAL_LIST = "list" ///< "list" attribute value in XML
        const val ATTR_VAL_BOOLEAN = "boolean" ///< "boolean" attribute value in XML
        const val ATTR_VAL_NUMERIC = "numeric" ///< "numeric" attribute value in XML
        const val ATTR_DBKEY = "dbkey" ///< key used to retrieve attribute "dbkey" in XML
        const val TYPE_INVALID = -1 ///< invalid attribute type
        const val TYPE_LIST = 0 ///< list attribute type
        const val TYPE_BOOLEAN = 1 ///< boolean attribute type
        const val TYPE_NUMERIC = 2 ///< numeric attribute type
    }

    /**
     * @brief Creates a Setting instance.
     */
    init {
        mOptions = ArrayList()
        val optionsList = settingNode.childNodes
        var i = 1
        while (i < optionsList.length) {
            mOptions.add(Option(optionsList.item(i)))
            i += 2
        }
    }
}