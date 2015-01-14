//
//  SMGSock.h
//  SockMatcher
//
//  Created by Joseph Caplan on 9/21/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGDrawn.h"

@interface SMGSock : SMGDrawn

@property BOOL visited;
@property int index;
@property (strong, nonatomic) NSMutableArray *neighbors;
@property (strong, nonatomic) NSMutableArray *corners;

- (id)init;
- (id)initWithIndex:(int)index val:(int)val col:(int)x row:(int)y sockWidth:(int)sockWidth andSockHeight:(int)sockHeight;
- (BOOL)matchesSock:(SMGSock *)otherSock;
- (void)destroy;

@end
