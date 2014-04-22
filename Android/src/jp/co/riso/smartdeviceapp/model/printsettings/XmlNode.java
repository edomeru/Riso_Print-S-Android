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

public abstract class XmlNode {
    
    public static final String ATTR_NAME = "name";
    public static final String ATTR_ICON = "icon";
    public static final String ATTR_TEXT = "text";
    public static final String ATTR_TYPE = "type";
    
    public static final String NODE_GROUP = "group";
    
    public NamedNodeMap mAttributes;
    
    /**
     * Constructor
     * 
     * @param node
     */
    public XmlNode(Node node) {
        mAttributes = node.getAttributes();
    }
    
    /**
     * @return Attribute
     */
    public String getAttributeValue(String key) {
        if (mAttributes.getNamedItem(key) == null) {
            return "";
        }
        
        return mAttributes.getNamedItem(key).getNodeValue();
    }
}
