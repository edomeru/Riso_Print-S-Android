/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PreviewControlsListener.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.preview;

public interface PreviewControlsListener {
    /**
     * Called when the index page is updated
     * 
     * @param index
     *            page index
     */
    public void onIndexChanged(int index);
    
    /**
     * Used to add bottom margin to glview to allow overlapping of views
     * 
     * @return controls height
     */
    public int getControlsHeight();
    
    /**
     * zoom level changed listener
     * 
     * @param zoomLevel
     *            new zoom level
     */
    public void zoomLevelChanged(float zoomLevel);
    
    /**
     * Set controls Enabled state
     * 
     * @param enabled
     *            new controls enabled state
     */
    public void setControlsEnabled(boolean enabled);
}
