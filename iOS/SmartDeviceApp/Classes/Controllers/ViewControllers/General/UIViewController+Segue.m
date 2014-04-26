//
//  UIViewController+Segue.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "UIViewController+Segue.h"

@implementation UIViewController (Segue)

- (void)performSegueTo:(Class)viewControllerClass
{
    NSString *identifier = [NSString stringWithFormat:@"%@-%@", [self extractBaseName:[self class]], [self extractBaseName:viewControllerClass]];
    [self performSegueWithIdentifier:identifier sender:self];
}

- (void)unwindTo:(Class)viewControllerClass
{
    NSString *identifier = [NSString stringWithFormat:@"UnwindTo%@", [self extractBaseName:viewControllerClass]];
    [self performSegueWithIdentifier:identifier sender:self];
}

- (void)unwindFromOverTo:(Class)viewControllerClass
{
    NSString *identifier = [NSString stringWithFormat:@"UnwindFromOverTo%@", [self extractBaseName:viewControllerClass]];
    [self performSegueWithIdentifier:identifier sender:self];
}

- (NSString *)extractBaseName:(Class)viewControllerClass
{
    NSError *error;
    NSRegularExpression *pattern = [[NSRegularExpression alloc] initWithPattern:@"ViewController$" options:0 error:&error];
    NSString *className = NSStringFromClass(viewControllerClass);
    NSString *baseName = [pattern stringByReplacingMatchesInString:className options:0 range:NSMakeRange(0, [className length]) withTemplate:@""];
    return baseName;
}

@end
