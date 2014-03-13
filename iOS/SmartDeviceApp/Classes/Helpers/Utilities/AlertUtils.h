//
//  AlertUtils.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

#define ALERT_ADD_PRINTER   @"Add Printer Info"

@interface AlertUtils : NSObject

/**
 Displays an AlertView.
 
 @param RESULT_TYPE
        one of pre-defined enum values
 @param title
        title for the UIAlertView
 @param details
        extra information needed when displaying the alert
        (ex. specify printer IP when adding printer failed)
 */
+ (void)displayResult:(RESULT_TYPE)result withTitle:(NSString*)title withDetails:(NSArray*)details;

@end
