//
//  Swizzler.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "Swizzler.h"
#import <objc/runtime.h>

@implementation Swizzler

Method originalClassMethod = nil;
Method originalInstanceMethod = nil;
Method swizzleClassMethod = nil;
Method swizzleInstanceMethod = nil;

- (void)swizzleClassMethod:(Class)targetClass targetSelector:(SEL)targetSelector swizzleClass:(Class)swizzleClass swizzleSelector:(SEL)swizzleSelector;
{
    originalClassMethod = class_getClassMethod(targetClass, targetSelector);
    swizzleClassMethod = class_getClassMethod(targetClass, swizzleSelector);
    method_exchangeImplementations(originalClassMethod, swizzleClassMethod);
}

- (void)swizzleInstanceMethod:(Class)targetClass targetSelector:(SEL)targetSelector swizzleClass:(Class)swizzleClass swizzleSelector:(SEL)swizzleSelector;
{
    originalInstanceMethod = class_getInstanceMethod(targetClass, targetSelector);
    swizzleInstanceMethod = class_getInstanceMethod(swizzleClass, swizzleSelector);
    method_exchangeImplementations(originalInstanceMethod, swizzleInstanceMethod);
}

- (void)deswizzle
{
    if (originalClassMethod != nil && swizzleClassMethod != nil)
    {
        method_exchangeImplementations(swizzleClassMethod, originalClassMethod);
    }
    
    if (originalInstanceMethod != nil && swizzleInstanceMethod != nil)
    {
        method_exchangeImplementations(swizzleInstanceMethod, originalInstanceMethod);
    }
    
    originalClassMethod = nil;
    originalInstanceMethod = nil;
    swizzleClassMethod = nil;
    swizzleInstanceMethod = nil;
}

@end
