//
//  PrintSettingsHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#define PREVIEWSETTING_CONTEXT @"PreviewSettingContext"

@class PreviewSetting;
@class PrintSetting;

/**
 * PrintSettingsHelper is a helper class that provides methods for the printer's settings.
 * This class also reads the default settings from the printsettings.xml.
 */
@interface PrintSettingsHelper : NSObject

/**
 * Returns the print settings tree from the printsettings.xml
 */
+ (NSDictionary *)sharedPrintSettingsTree;

// for ORPHIS FW
//+ (NSDictionary *)sharedPrintSettingsTreeFW:(NSString *)printerName;

/**
 * Returns the preview setting containing the default settings from the printsettings.xml
 */
+ (PreviewSetting *)defaultPreviewSetting;

/**
 * Copies the default print setting values from the printsettings.xml to the preview setting object
 * @param printSetting - pointer to the destination print setting object
 */+ (void)copyDefaultPreviewSetting:(PreviewSetting **)previewSetting;

/**
 * Copies the default print setting values from the printsettings.xml to the Print setting object
 * @param printSetting - pointer to the destination print setting object
 * @param printerName - printer name for determining its type
 */
+ (void)copyDefaultPrintSettings:(PrintSetting **)printSetting
                     printerName:(NSString *)name;

/**
 * Copies the settings value from the Print Settings object to the Preview setting object
 * @param printSetting - source print setting object
 * @param previewSetting - pointer to the destination  Preview setting object
 */
+ (void)copyPrintSettings:(PrintSetting *)printSetting toPreviewSetting:(PreviewSetting **) previewSetting;

/**
 * Add an observer to the preview setting object values
 * @param observer - object to observe the preview setting object values
 * @param previewSetting - pointer preview setting object to observe
 */
+ (void)addObserver:(id)observer toPreviewSetting:(PreviewSetting **)previewSetting;

/**
 * Remove the observer to the preview setting object values
 * @param observer - object observing the preview setting object values
 * @param previewSetting - pointer preview setting object being observed
 */
+ (void)removeObserver:(id)observer fromPreviewSetting:(PreviewSetting **)previewSetting;

/**
 * Returns printer series search filter based on printer name
 * @param printerName - printer name for determining its type
 */
+ (NSString *)getPrinterType:(NSString *)printerName;

/**
 * Checks if the printer name is IS Series.
 *
 * @return YES if IS Series, NO otherwise
 */
+ (BOOL)isISSeries:(NSString *)printerName;

/**
 * Checks if the printer name is GD Series.
 *
 * @return YES if GD Series, NO otherwise
 */
+ (BOOL)isGDSeries:(NSString *)printerName;

/**
 * Checks if the printer name is FW Series.
 *
 * @return YES if FW Series, NO otherwise
 */
+ (BOOL)isFWSeries:(NSString *)printerName;

/**
 * Checks if the printer name is FT Series.
 *
 * @return YES if FT Series, NO otherwise
 */
+ (BOOL)isFTSeries:(NSString *)printerName;

/**
 * Checks if the printer name is GL Series.
 *
 * @return YES if GL Series, NO otherwise
 */
+ (BOOL)isGLSeries:(NSString *)printerName;

/**
 * Checks if the printer name is FT or GL Series.
 *
 * @return YES if FT or GL Series, NO otherwise
 */
+ (BOOL)isFTorGLSeries:(NSString *)printerName;
@end
