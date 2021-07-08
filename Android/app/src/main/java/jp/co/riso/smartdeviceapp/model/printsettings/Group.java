/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Group.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @class Group
 * 
 * @brief Group data class representing a group in PrintSettings. 
 * 
 * Data represented by \<group\> tag in XML.
 */
public class Group extends XmlNode {
    private List<Setting> mSettings;
    
    /**
     * @brief Creates a Group instance.
     * 
     * @param groupNode Node represented by \<group\> tag in XML.
     */
    public Group(Node groupNode) {
        super(groupNode);
        
        mSettings = new ArrayList<>();

        NodeList settingsList = groupNode.getChildNodes();
        for (int i = 1; i < settingsList.getLength(); i += 2) {
            mSettings.add(new Setting(settingsList.item(i)));    
        }
    }
    
    /**
     * @brief Gets settings of a Group.
     * 
     * @return List of Setting objects.
     */
    public List<Setting> getSettings() {
        return mSettings;
    }
}
