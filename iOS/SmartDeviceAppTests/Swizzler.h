//
//  Swizzler.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Swizzler : NSObject

- (void)swizzleClassMethod:(Class)targetClass targetSelector:(SEL)targetSelector swizzleClass:(Class)swizzleClass swizzleSelector:(SEL)swizzleSelector;
- (void)swizzleInstanceMethod:(Class)targetClass targetSelector:(SEL)targetSelector swizzleClass:(Class)swizzleClass swizzleSelector:(SEL)swizzleSelector;
- (void)deswizzle;

@end
