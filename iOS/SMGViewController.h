//
//  SMGViewController.h
//  SockMatcher
//
//  Created by Joseph Caplan on 9/16/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGTexturedSquare.h"
#import "SMGEndGameProtocol.h"
#import <UIKit/UIKit.h>
#import <GLKit/GLKit.h>

@interface SMGViewController : GLKViewController <GLKViewDelegate, UIApplicationDelegate, SMGEndGameProtocolDelegate>

@property BOOL sound;
@property int highScore;
@property (strong, nonatomic) NSArray *soundPlayers;

@end
