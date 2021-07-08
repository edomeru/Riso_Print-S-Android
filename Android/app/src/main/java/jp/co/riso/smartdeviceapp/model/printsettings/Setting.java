/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Setting.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @class Setting
 * 
 * @brief Setting data class representing a setting in PrintSettings.
 * Data represented by \<setting\> tag in XML.
 */
public class Setting extends XmlNode {
    public static final String ATTR_DEFAULT = "default"; ///< key used to retrieve attribute "default" in XML
    
    public static final String ATTR_VAL_LIST = "list"; ///< "list" attribute value in XML
    public static final String ATTR_VAL_BOOLEAN = "boolean"; ///< "boolean" attribute value in XML
    public static final String ATTR_VAL_NUMERIC = "numeric"; ///< "numeric" attribute value in XML
    public static final String ATTR_DBKEY = "dbkey"; ///< key used to retrieve attribute "dbkey" in XML
    
    public static final int TYPE_INVALID = -1; ///< invalid attribute type
    public static final int TYPE_LIST = 0; ///< list attribute type
    public static final int TYPE_BOOLEAN = 1; ///< boolean attribute type
    public static final int TYPE_NUMERIC = 2; ///< numeric attribute type
    
    private List<Option> mOptions;
    
    /**
     * @brief Creates a Setting instance.
     * 
     * @param settingNode Node represented by \<setting\> tag in XML.
     */
    public Setting(Node settingNode) {
        super(settingNode);
        
        mOptions = new ArrayList<>();
        
        NodeList optionsList = settingNode.getChildNodes();
        for (int i = 1; i < optionsList.getLength(); i += 2) {
            mOptions.add(new Option(optionsList.item(i)));
        }
    }
    
    /**
     * @brief Gets options of the setting.
     * 
     * @return List of Option objects
     */
    public List<Option> getOptions() {
        return mOptions;
    }
    
    /**
     * @brief Gets the type of a Setting. The value of attribute \"type\" in XML.
     * 
     * @retval ATTR_VAL_LIST type="list"
     * @retval ATTR_VAL_BOOLEAN type="boolean"
     * @retval ATTR_VAL_NUMERIC type="numeric"
     * @retval TYPE_INVALID type is not list, boolean or numeric
     */
    public int getType() {
        String type = getAttributeValue(ATTR_TYPE);
        
        if (type.equalsIgnoreCase(ATTR_VAL_LIST)) {
            return TYPE_LIST;
        } else if (type.equalsIgnoreCase(ATTR_VAL_BOOLEAN)) {
            return TYPE_BOOLEAN;
        } else if (type.equalsIgnoreCase(ATTR_VAL_NUMERIC)) {
            return TYPE_NUMERIC;
        }
        
        return TYPE_INVALID;
    }
    
    /**
     * @brief Gets the default value of a Setting. The value of attribute \"default\" in XML.
     * 
     * @return The default value as an integer.
     * @retval 1 Setting's attribute type is boolean and value is true.
     * @retval 0 Setting's attribute type is boolean and value is false.
     * @retval -1 Setting's attribute type is invalid.
     */
    public Integer getDefaultValue() {
        String value = getAttributeValue(ATTR_DEFAULT);
        
        switch (getType()) {
            case TYPE_LIST:
            case TYPE_NUMERIC:
                return Integer.parseInt(value);
            case TYPE_BOOLEAN:
                return Boolean.parseBoolean(value) ? 1 : 0;
        }
        
        return -1;
    }
    
    /**
     * @brief Gets the database column name of a Setting. The value of attribute \"dbkey\" in XML. 
     * 
     * @return Database key
     */
    public String getDbKey() {
        return getAttributeValue(ATTR_DBKEY);
    }
}