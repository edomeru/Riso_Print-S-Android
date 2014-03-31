//
//  XMLParser.m
//  SmartDeviceApp
//
//  Created by Seph on 3/17/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "XMLParser.h"

@interface XMLParser()

@property (nonatomic, strong) NSMutableArray *stack;
@property (nonatomic, strong) NSMutableString *currentString;

- (NSDictionary *)dictionaryWithXMLData:(NSData *)data;

@end

@implementation XMLParser

+ (NSDictionary *)dictionaryFromXMLFile:(NSString *)path
{
    XMLParser *parser = [[XMLParser alloc] init];
    
    // Open file
    NSURL *url = [NSURL fileURLWithPath:path];
    NSData *data = [NSData dataWithContentsOfURL:url];
    return [parser dictionaryWithXMLData:data];
}

- (NSDictionary *)dictionaryWithXMLData:(NSData *)data
{
    self.stack = [[NSMutableArray alloc] init];
    self.currentString = [[NSMutableString alloc] init];
    
    // Create root dictionary
    [self.stack addObject:[NSMutableDictionary dictionary]];
    
    // Create parser
    NSXMLParser *parser = [[NSXMLParser alloc] initWithData:data];
    parser.delegate = self;
    
    if ([parser parse])
    {
        // Return root dictionary
        return [self.stack objectAtIndex:0];
    }
    
    return nil;
}

#pragma mark -
#pragma mark NSXMLParserDelegate

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    // Get top of stack - parent of the new child element
    NSMutableDictionary *parent = [self.stack lastObject];
    
    // Create child element containing the attributes
    NSMutableDictionary *newChild = [NSMutableDictionary dictionary];
    [newChild addEntriesFromDictionary:attributeDict];
    
    // Prepare to add new child
    id existingValue = [parent objectForKey:elementName];
    if (existingValue)
    {
        NSMutableArray *children;
        if ([existingValue isKindOfClass:[NSMutableArray class]])
        {
            children = existingValue;
        }
        else
        {
            children = [NSMutableArray array];
            [children addObject:existingValue];
            
            [parent setObject:children forKey:elementName];
        }
        [children addObject:newChild];
    }
    else
    {
        [parent setObject:newChild forKey:elementName];
    }
    
    // Push child to stack
    [self.stack addObject:newChild];
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    // Get top of stack - current "open" element
    NSMutableDictionary *current = [self.stack lastObject];
    
    NSString *trimmedString = [self.currentString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if ([trimmedString length] > 0)
    {
        // Add text to current dictionary
        [current setObject:trimmedString forKey:@"content-body"];
        
        // Reset string
        [self.currentString setString:@""];
    }
    
    // Pop from stack - close element
    [self.stack removeLastObject];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    // Append to current string
    [self.currentString appendString:string];
}

@end
