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

public class Setting extends XmlNode {
    public static final String TAG = "Setting";
    public static final String ATTR_DEFAULT = "default";
    
    public static final String ATTR_VAL_LIST = "list";
    public static final String ATTR_VAL_BOOLEAN = "boolean";
    public static final String ATTR_VAL_NUMERIC = "numeric";
    public static final String ATTR_DBKEY = "dbkey";
    
    public static final int TYPE_INVALID = -1;
    public static final int TYPE_LIST = 0;
    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_NUMERIC = 2;
    
    private List<Option> mOptions;
    
    /**
     * Constructor
     * 
     * @param settingNode
     */
    public Setting(Node settingNode) {
        super(settingNode);
        
        mOptions = new ArrayList<Option>();
        
        NodeList optionsList = settingNode.getChildNodes();
        for (int i = 1; i < optionsList.getLength(); i += 2) {
            mOptions.add(new Option(optionsList.item(i)));
        }
    }
    
    /**
     * @return list of options
     */
    public List<Option> getOptions() {
        return mOptions;
    }
    
    /**
     * @return type
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
     * @return default value
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
     * @return database key
     */
    public String getDbKey() {
        return getAttributeValue(ATTR_DBKEY);
    }
}