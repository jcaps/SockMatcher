//
//  SMGSock.m
//  SockMatcher
//
//  Created by Joseph Caplan on 9/21/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGSock.h"

@implementation SMGSock

- (id)init
{
    self = [super init];
    
    self.visited = NO;
    self.index = 0;
    self.neighbors = [[NSMutableArray alloc] init];
    self.corners = [[NSMutableArray alloc] init];
    
    return self;
}

- (id)initWithIndex:(int)index val:(int)val col:(int)x row:(int)y sockWidth:(int)sockWidth andSockHeight:(int)sockHeight
{
    self = [super initWithVal:val];
    
    self.base = CGPointMake(x*sockWidth, y*sockHeight);
    self.pos = self.base;
    self.index = index;
    self.visited = NO;
    self.neighbors = [[NSMutableArray alloc] init];
    self.corners = [[NSMutableArray alloc] init];
    
    return self;
}

- (BOOL)matchesSock:(SMGSock *)otherSock
{
    return self.val == otherSock.val;
}

- (void)destroy
{
    [self.neighbors removeAllObjects];
    self.neighbors = nil;
    [self.corners removeAllObjects];
    self.corners = nil;
}

@end
