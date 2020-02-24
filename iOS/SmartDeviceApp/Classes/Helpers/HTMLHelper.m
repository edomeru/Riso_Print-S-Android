//
//  HTMLHelper.m
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright © 2019 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "HTMLHelper.h"

@implementation HTMLHelper

+ (void)loadHTML:(NSString*)html toWebView:(UIWebView*)webView
{
    NSURL *url = [[NSBundle mainBundle] URLForResource:html withExtension:@"html"];
    [webView loadRequest:[NSURLRequest requestWithURL:url]];
}

+ (void)loadHTML:(NSString*)html toWebView:(WKWebView*)webView withTrait:(UITraitCollection*)traitCollection andHelpHTML:(BOOL)isHelp
{
    NSURL *url = [HTMLHelper getHtmlUrl:html withTrait:traitCollection];
    if (isHelp) {
        [webView loadFileURL:url allowingReadAccessToURL:url];;
    } else {
        [webView loadRequest:[NSURLRequest requestWithURL:url]];
    }
}

/**
 * Checks whether the OS is in light or dark theme then gets the local HTML file for the detected OS theme.
 * Returns the URL for the local HTML file.
 */
+ (NSURL*)getHtmlUrl:(NSString*)defaultHTML withTrait:(UITraitCollection*)traitCollection
{
    NSURL *url = [[NSBundle mainBundle] URLForResource:defaultHTML withExtension:@"html"];
    if (@available(iOS 13.0, *)) {
        if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            // append "_dark" to get the dark theme version of the local HTML file
            NSString *darkThemeHTML = [NSString stringWithFormat:@"%@%@", defaultHTML, @"_dark"];
            url = [[NSBundle mainBundle] URLForResource:darkThemeHTML withExtension:@"html"];
        }
    }
    return url;
}

@end
