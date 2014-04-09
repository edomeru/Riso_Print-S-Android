/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PreviewControlsListener.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.preview;

public interface PreviewControlsListener {
    public void onIndexChanged(int index);
    public int getControlsHeight();
    public void zoomLevelChanged(float zoomLevel);
    public void setControlsEnabled(boolean enabled);
}
