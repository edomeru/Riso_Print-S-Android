//
//  PrinterStatusView.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusHelper.h"

@interface PrinterStatusView : UIImageView <PrinterStatusHelperDelegate>

@property (nonatomic) BOOL onlineStatus;
@property (strong, nonatomic) PrinterStatusHelper *statusHelper;

- (void) setStatus: (BOOL) isOnline;
@end
