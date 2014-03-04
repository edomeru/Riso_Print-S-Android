//
//  InputUtils.h
//  SmartDeviceApp
//
//  Created by Lester Joseph Valencia on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface InputUtils : NSObject

/**
 Checks the format of the given IP Address.
 
 @return YES if valid, NO otherwise.
 **/
+ (BOOL)validateAndFormatIP:(NSString**)ip;

@end
