/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Option.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

import org.w3c.dom.Node;

public class Option extends XmlNode {
    public static final String TAG = "Option";
    
    private String mTextContent;
    
    public Option(Node optionNode) {
        super(optionNode);
        mTextContent = optionNode.getTextContent();
    }
    
    public String getTextContent() {
        return mTextContent;
    }
}