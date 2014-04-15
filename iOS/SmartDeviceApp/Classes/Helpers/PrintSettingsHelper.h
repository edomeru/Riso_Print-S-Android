//
//  PrintSettingsHelper.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#define PREVIEWSETTING_CONTEXT @"PreviewSettingContext"

@class PreviewSetting;
@class PrintSetting;
@interface PrintSettingsHelper : NSObject

+ (NSDictionary *)sharedPrintSettingsTree;
+ (PreviewSetting *)defaultPreviewSetting;
+ (void)copyDefaultPrintSettings:(PrintSetting **)printSetting;
+ (void)copyPrintSettings:(PrintSetting *)printSetting toPreviewSetting:(PreviewSetting **) previewSetting;
+ (void)addObserver:(id)observer toPreviewSetting:(PreviewSetting **)previewSetting;
+ (void)removeObserver:(id)observer fromPreviewSetting:(PreviewSetting **)previewSetting;
@end
