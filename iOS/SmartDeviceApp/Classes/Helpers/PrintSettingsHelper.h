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
@interface PrintSettingsHelper : NSObject
/**
 Returns the print settings tree from the printsettings.xml
 */
+ (NSDictionary *)sharedPrintSettingsTree;
/**
 Returns the preview setting containing the default settings from the printsettings.xml
 */
+ (PreviewSetting *)defaultPreviewSetting;
/**
 Copies the default print setting values from the printsettings.xml to the Print setting object
 @param printSetting - pointer to the destination print setting object
 */
+ (void)copyDefaultPrintSettings:(PrintSetting **)printSetting;
/**
 Copies the settings value from the Print Settings objectto the Preview setting object
 @param printSetting - source print setting object
 @param previewSetting - pointer to the destination  Preview setting object
 */
+ (void)copyPrintSettings:(PrintSetting *)printSetting toPreviewSetting:(PreviewSetting **) previewSetting;
/**
 Add an observer to the preview setting object values
 @param observer - object to observe the preview setting object values
 @param previewSetting - pointer preview setting object to observe
 */
+ (void)addObserver:(id)observer toPreviewSetting:(PreviewSetting **)previewSetting;
/**
 Remove the observer to the preview setting object values
 @param observer - object observing the preview setting object values
 @param previewSetting - pointer preview setting object being observed
 */
+ (void)removeObserver:(id)observer fromPreviewSetting:(PreviewSetting **)previewSetting;
@end
