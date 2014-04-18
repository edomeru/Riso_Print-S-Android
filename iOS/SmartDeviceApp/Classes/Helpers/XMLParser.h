//
//  XMLParser.h
//  SmartDeviceApp
//
//  Created by Seph on 3/17/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface XMLParser : NSObject<NSXMLParserDelegate>

/**
 Creates a dictionary by parsing an XML file
 @param path
        Path of the XML file
 @return Dictionary object of the parsed XML file
 */
+ (NSDictionary *)dictionaryFromXMLFile:(NSString *)path;

@end
