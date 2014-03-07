//
//  UIViewController+Segue.h
//  SmartDeviceApp
//
//  Created by Seph on 3/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIViewController (Segue)

- (void)performSegueTo:(Class)viewControllerClass;
- (void)unwindTo:(Class)viewControllerClass;
- (void)unwindFromOverTo:(Class)viewControllerClass;

@end
