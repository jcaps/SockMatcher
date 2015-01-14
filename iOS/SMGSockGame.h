//
//  SMGSockGame.h
//  SockMatcher
//
//  Created by Joseph Caplan on 9/21/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SMGDrawn.h"
#import "SMGSock.h"
#import "SMGEndGameProtocol.h"

enum GameState {
    READY,
    RUNNING,
    PAUSED,
    GAMEOVER
};

enum Selectable {
    SOCK,
    ITEM
};

@interface SMGSockGame : NSObject

@property BOOL itemStringHidden;
@property BOOL sound;
@property BOOL hasItem;
@property BOOL isSelected;
@property BOOL newHighScore;
@property enum GameState gameState;
@property enum Selectable selected;
@property int highScore;
@property int score;
@property int selectedSockNum;
@property int time;
@property float itemRot;
@property (strong, atomic) NSMutableArray *effectsToDraw;
@property (strong, nonatomic) NSArray *soundPlayers;
@property (strong, atomic) NSMutableArray *socksToDraw;
@property (strong, nonatomic) NSString *itemString;
@property (strong, nonatomic) SMGDrawn *item;
@property (strong, nonatomic) SMGEndGameProtocol *gameEndProtocol;
@property (strong, nonatomic) SMGSock *selectedSock;

- (id)init;
- (void)destroy;
- (void)handleTouchDownWithX:(float)x andY:(float)y;
- (void)handleTouchDraggedWithX:(float)x andY:(float)y;
- (void)handleTouchUpWithX:(float)x andY:(float)y;
- (void)pause;
- (void)reset;
- (void)resume;
- (void)endGame;

@end
