/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Option.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

import org.w3c.dom.Node;

/**
 * @class Option
 * 
 * @brief Option data class representing an option in PrintSettings.
 * 
 * Data represented by \<option\> tag in XML.
 */
public class Option extends XmlNode {
    private String mTextContent;
    
    /**
     * @brief Creates an Option instance.
     * 
     * @param optionNode Node represented by \<option\> tag in XML.
     */
    public Option(Node optionNode) {
        super(optionNode);
        mTextContent = optionNode.getTextContent();
    }
    
    /**
     * @brief Gets text content of the group node in XML.
     * 
     * @return Text content of the group
     */
    public String getTextContent() {
        return mTextContent;
    }
}
