/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * XmlNode.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @class XmlNode
 * 
 * @brief Abstract data class for each XML node.
 * Data represented by node tags in XML.
 */
public abstract class XmlNode {
    
    public static final String ATTR_NAME = "name"; ///< key used to retrieve "name" attribute in XML
    public static final String ATTR_ICON = "icon"; ///< key used to retrieve "icon" attribute in XML
    public static final String ATTR_TEXT = "text"; ///< key used to retrieve "text" attribute in XML
    public static final String ATTR_TYPE = "type"; ///< key used to retrieve "type" attribute in XML
    
    public static final String NODE_GROUP = "group"; ///< key used to retrieve "group" node in XML
    
    public NamedNodeMap mAttributes;
    
    /**
     * @brief Constructor
     * 
     * @param node Node element in XML.
     */
    public XmlNode(Node node) {
        mAttributes = node.getAttributes();
    }
    
    /**
     * @brief Gets attribute value of a specified key in XML. Represented by key=\"\<value\"\>.
     * 
     * @param key Attribute key in XML. 
     * 
     * @return attribute value of the specified key
     * @retval "" key is invalid
     */
    public String getAttributeValue(String key) {
        if (mAttributes.getNamedItem(key) == null) {
            return "";
        }
        
        return mAttributes.getNamedItem(key).getNodeValue();
    }
}
