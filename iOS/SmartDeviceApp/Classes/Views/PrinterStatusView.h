//
//  PrinterStatusView.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusHelper.h"

@interface PrinterStatusView : UIImageView

@property (nonatomic) BOOL onlineStatus;

- (void) setStatus: (BOOL) isOnline;
@end
