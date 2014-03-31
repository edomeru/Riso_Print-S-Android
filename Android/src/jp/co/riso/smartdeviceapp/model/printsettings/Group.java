package jp.co.riso.smartdeviceapp.model.printsettings;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Group extends XmlNode {
    public static final String TAG = "Group";

    private List<Setting> mSettings;
    
    public Group(Node groupNode) {
        super(groupNode);
        
        mSettings = new ArrayList<Setting>();

        NodeList settingsList = groupNode.getChildNodes();
        for (int i = 1; i < settingsList.getLength(); i += 2) {
            mSettings.add(new Setting(settingsList.item(i)));    
        }
    }
    
    public List<Setting> getSettings() {
        return mSettings;
    }
}
