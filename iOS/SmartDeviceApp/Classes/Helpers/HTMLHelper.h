//
//  HTMLHelper.h
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright © 2019 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <WebKit/WKWebView.h>

@interface HTMLHelper : NSObject

/**
 * Load HTML file to web view. This method is only called if device OS version is below iOS 9
 */
+ (void)loadHTML:(NSString*)html toWebView:(UIWebView*)webView;

/**
 * Load HTML file to web view. This method is only called if device OS version is at least iOS 9
 */
+ (void)loadHTML:(NSString*)html toWebView:(WKWebView*)webView withTrait:(UITraitCollection*)traitCollection andHelpHTML:(BOOL)isHelp;

@end
