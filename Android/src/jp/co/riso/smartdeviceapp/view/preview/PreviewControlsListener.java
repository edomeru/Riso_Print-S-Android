/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PreviewControlsListener.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.preview;

/**
 * @interface PreviewControlsListener
 * @brief Interface for PrintPreviewView Events
 */
public interface PreviewControlsListener {
    
    /**
     * @brief Called when the index page is updated
     * 
     * @param index page index
     */
    public void onIndexChanged(int index);
    
    /**
     * @brief Used to add bottom margin to GLView to allow overlapping of views
     * 
     * @return Height of the control view
     */
    public int getControlsHeight();
    
    /**
     * @brief Notify that the pan has changed
     * 
     * @param panX new x pan
     * @param panY new y pan
     */
    public void panChanged(float panX, float panY);
    
    /**
     * @brief Notify that the zoom level has changed
     * 
     * @param zoomLevel New zoom level
     */
    public void zoomLevelChanged(float zoomLevel);
    
    /**
     * @brief Set whether the curl view controls are enabled
     * 
     * @param enabled New controls enabled state
     */
    public void setControlsEnabled(boolean enabled);
}
