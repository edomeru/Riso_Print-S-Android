//
//  PrintSettingsHelper.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PreviewSetting;
@class PrintSetting;
@interface PrintSettingsHelper : NSObject

+ (NSDictionary *)sharedPrintSettingsTree;
+ (PreviewSetting *)defaultPreviewSetting;
+ (void)copyDefaultPrintSettings:(PrintSetting **)printSetting;

@end
