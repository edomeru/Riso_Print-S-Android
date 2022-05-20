/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PreviewControlsListener.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.preview

/**
 * @interface PreviewControlsListener
 *
 * @brief Interface for PrintPreviewView Events
 */
interface PreviewControlsListener {
    /**
     * @brief Called when the index page is updated
     *
     * @param index Page index
     */
    fun onIndexChanged(index: Int)

    /**
     * @brief Used to add bottom margin to GLView to allow overlapping of views
     *
     * @return Height of the control view
     */
    val controlsHeight: Int

    /**
     * @brief Notify that the pan has changed
     *
     * @param panX New x pan
     * @param panY New y pan
     */
    fun panChanged(panX: Float, panY: Float)

    /**
     * @brief Notify that the zoom level has changed
     *
     * @param zoomLevel New zoom level
     */
    fun zoomLevelChanged(zoomLevel: Float)

    /**
     * @brief Set whether the curl view controls are enabled
     *
     * @param enabled New controls enabled state
     */
    fun setControlsEnabled(enabled: Boolean)
}