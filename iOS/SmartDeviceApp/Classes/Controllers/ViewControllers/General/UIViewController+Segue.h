//
//  UIViewController+Segue.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Adds segue-related helper methods to UIViewController.
 */
@interface UIViewController (Segue)

/**
 * Performs segue to a another view controller.
 * The segue name should be: 
 * <CurrentViewControllerName>-<DestinationViewControllerName>.
 * The "ViewController" suffix should be dropped.
 *
 * @param viewControllerClass Class of the destination view controller.
 */
- (void)performSegueTo:(Class)viewControllerClass;

/**
 * Performs an unwinding segue to another view controller.
 * Used for SlideSegue.
 *
 * @param viewControllerClass Class of the view controlller to unwind to.
 */
- (void)unwindTo:(Class)viewControllerClass;

/**
 * Performs an unwinding segue to another view controller.
 * Used for SlideOverSegue.
 *
 * @param viewControllerClass Class of the view controlller to unwind to.
 */
- (void)unwindFromOverTo:(Class)viewControllerClass;

@end
