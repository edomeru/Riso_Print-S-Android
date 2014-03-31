//
//  XMLParser.h
//  SmartDeviceApp
//
//  Created by Seph on 3/17/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface XMLParser : NSObject<NSXMLParserDelegate>

+ (NSDictionary *)dictionaryFromXMLFile:(NSString *)path;

@end
