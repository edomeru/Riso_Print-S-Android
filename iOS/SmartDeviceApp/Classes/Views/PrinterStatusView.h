//
//  PrinterStatusView.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusHelper.h"

/**
 * Custom view for the online/offline indicator of printers.
 * The online/offline status indicates whether the printer is reachable via SNMP.\n\n
 * This is displayed as either:
 *  - a green circle (online)
 *  - a gray circle (offline)
 *
 * This is used in the following screens:
 *  - "Printers"
 *  - "Print Settings" (printer selection)
 */
@interface PrinterStatusView : UIImageView

/**
 * Sets the displayed printer status.
 *
 * @param isOnline YES if online, NO if offline
 */
- (void)setStatus:(BOOL)isOnline;

@end
