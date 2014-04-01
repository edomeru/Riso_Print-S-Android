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
    
    public NamedNodeMap mAttributes;
    
    public XmlNode(Node node) {
        mAttributes = node.getAttributes();
    }
    
    public String getAttributeValue(String key) {
        if (mAttributes.getNamedItem(key) == null) {
            return "";
        }
        
        return mAttributes.getNamedItem(key).getNodeValue();
    }
}
