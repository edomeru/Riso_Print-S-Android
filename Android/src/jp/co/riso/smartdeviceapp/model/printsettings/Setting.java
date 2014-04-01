package jp.co.riso.smartdeviceapp.model.printsettings;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Setting extends XmlNode{
    public static final String TAG = "Setting";

    public static final int TYPE_INVALID = -1;
    public static final int TYPE_LIST = 0;
    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_NUMERIC = 2;
    
    private List<Option> mOptions;
    
    public Setting(Node settingNode) {
        super(settingNode);
        
        mOptions = new ArrayList<Option>();

        NodeList optionsList = settingNode.getChildNodes();
        for (int i = 1; i < optionsList.getLength(); i += 2) {
            mOptions.add(new Option(optionsList.item(i)));    
        }
    }
    
    public List<Option> getOptions() {
        return mOptions;
    }
    
    public int getType() {
        String type = getAttributeValue("type");
        
        if (type.equalsIgnoreCase("list")) {
            return TYPE_LIST;
        } else if (type.equalsIgnoreCase("boolean")) {
            return TYPE_BOOLEAN;
        } else if (type.equalsIgnoreCase("numeric")) {
            return TYPE_NUMERIC;
        }
        
        return TYPE_INVALID;
    }
    
    public Integer getDefaultValue() {
        String value = getAttributeValue("default");
        
        switch (getType()) {
            case TYPE_LIST:
            case TYPE_NUMERIC:
                return Integer.parseInt(value);
            case TYPE_BOOLEAN:
                return Boolean.parseBoolean(value) ? 1 : 0;
        }
        
        return -1;
    }
}