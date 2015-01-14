//
//  SMGViewController.m
//  SockMatcher
//
//  Created by Joseph Caplan on 9/16/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGViewController.h"
#import "SMGTexturedSquare.h"
#import "SMGSockGame.h"
#import "SMGMainMenuViewController.h"

const int NUM_DIFF_SOCKS = 24;
const int NUM_DIFF_ITEMS = 3;

@interface SMGViewController ()
{
    BOOL touchLocked;
    CGSize screenSize;
    float scaleX;
    float scaleY;
    enum GameState state;
    UIView *overlayView;
    NSDictionary *textAttributes;
    UIButton *PlayAgainButton;
    UIButton *ResumeButton;
    UIButton *MainMenuButton;
    UILabel *FinalScoreLabel;
    UILabel *GamePausedLabel;
    UILabel *GameOverLabel;
    UILabel *ItemLabel;
    UILabel *ScoreLabel;
    UILabel *TapToStartLabel;
    UILabel *TimeLabel;
    EAGLContext *context;
    GLKBaseEffect *effect;
    SMGTexturedSquare *background;
    NSArray *socks;
    NSArray *items;
    NSArray *effects;
    NSMutableAttributedString *itemString;
    NSMutableAttributedString *scoreString;
    NSMutableAttributedString *timeString;
    SMGSockGame *sockGame;
    NSMutableArray *effectsToDraw;
    NSMutableArray *socksToDraw;
    GLuint backgroundTexture;
    GLuint sockTexture;
    GLuint itemTexture;
    GLuint textTexture;
    GLuint effectTexture;
}

@end

@implementation SMGViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    context = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
    
    if (!context) {
        NSLog(@"Failed to create ES context");
    }
    
    GLKView *view;
    view = (GLKView *)self.view;
    view.context = context;
    [EAGLContext setCurrentContext:context];
    effect = [GLKBaseEffect new];

    screenSize = CGSizeMake(MIN([UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height), MAX([UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height));
    
    scaleX = 1280/screenSize.height;
    scaleY = 800/screenSize.width;
    
    effectsToDraw = [NSMutableArray array];
    socksToDraw = [NSMutableArray array];
    
    GLKMatrix4 projectionMatrix = GLKMatrix4MakeOrtho(0, 1280, 0, 800, -1024, 1024);
    effect.transform.projectionMatrix = projectionMatrix;
    
    [self loadTextures];
    
    SMGEndGameProtocol *gameEndProtocol = [SMGEndGameProtocol new];
    gameEndProtocol.delegate = self;
    
    sockGame = [SMGSockGame new];
    sockGame.gameEndProtocol = gameEndProtocol;
    sockGame.sound = self.sound;
    sockGame.highScore = self.highScore;
    sockGame.soundPlayers = self.soundPlayers;

    [self initAttributedStringsAndUILabels];
    [self initButtons];
    overlayView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, screenSize.height, screenSize.width)];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appWillResignActive) name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appWillEnterForeground) name:UIApplicationWillEnterForegroundNotification object:nil];

    [self prepareReady];
}

- (void)appWillResignActive
{
    if (sockGame.gameState == RUNNING) {
        [sockGame pause];
        [self removeRunning];
        self.preferredFramesPerSecond = 1;
    }
}

- (void)appWillEnterForeground
{
    if (sockGame.gameState == PAUSED) {
        self.preferredFramesPerSecond = 30;
        [self preparePaused];
    }
}

- (void)destroy
{
    [background destroy];
    background = nil;
    GLuint texture = backgroundTexture;
    glDeleteTextures(1, &texture);
    for (SMGTexturedSquare *sockSquare in socks)
        [sockSquare destroy];
    socks = nil;
    texture = sockTexture;
    glDeleteTextures(1, &texture);
    for (SMGTexturedSquare *itemSquare in items)
        [itemSquare destroy];
    items = nil;
    texture = itemTexture;
    glDeleteTextures(1, &texture);
    for (SMGTexturedSquare *effectSquare in effects)
        [effectSquare destroy];
    effects = nil;
    texture = effectTexture;
    glDeleteTextures(1, &texture);
    overlayView = nil;
    textAttributes = nil;
    [PlayAgainButton.imageView removeFromSuperview];
    PlayAgainButton = nil;
    [ResumeButton.imageView removeFromSuperview];
    ResumeButton = nil;
    [MainMenuButton.imageView removeFromSuperview];
    MainMenuButton = nil;
    FinalScoreLabel = nil;
    GamePausedLabel = nil;
    GameOverLabel = nil;
    ItemLabel.attributedText = nil;
    ItemLabel = nil;
    ScoreLabel.attributedText = nil;
    ScoreLabel = nil;
    TapToStartLabel.attributedText = nil;
    TapToStartLabel = nil;
    TimeLabel.attributedText = nil;
    TimeLabel = nil;
    [EAGLContext setCurrentContext:nil];
    ((GLKView*)self.view).context = nil;
    context = nil;
    effects = nil;
    effect = nil;
    itemString = nil;
    scoreString = nil;
    timeString = nil;
    [sockGame destroy];
    sockGame = nil;
    effectsToDraw = nil;
    socksToDraw = nil;
    self.soundPlayers = nil;
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:NO];
    
    [self destroy];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillEnterForegroundNotification object:nil];
}

#pragma mark - Prepare Methods

- (void)prepareReady
{
    [overlayView setBackgroundColor:[UIColor colorWithRed:0.0f green:0.0f blue:0.0f alpha:0.75f]];
    [self.view addSubview:overlayView];
    [self.view addSubview:TapToStartLabel];
}

- (void)prepareRunning
{
    //Set time attributed string, time label, and add label to view
    [self.view addSubview:TimeLabel];
    
    //Set score attributed string, score label, and add label to view
    [self.view addSubview:ScoreLabel];
    
    [self.view addSubview:ItemLabel];
}

- (void)preparePaused
{
    [self.view addSubview:overlayView];
    [self.view addSubview:GamePausedLabel];
    [self.view addSubview:ResumeButton];
    [self.view addSubview:MainMenuButton];
}

- (void)prepareGameOver
{
    if (!sockGame.newHighScore) {
        FinalScoreLabel.attributedText = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"Final Score: %d", sockGame.score] attributes:textAttributes];
    }
    
    else {
        FinalScoreLabel.attributedText = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"New High Score: %d", sockGame.score] attributes:textAttributes];
    }
    
    [overlayView setBackgroundColor:[UIColor colorWithRed:0.0f green:0.0f blue:0.0f alpha:1.0]];
    [self.view addSubview:overlayView];
    [self.view addSubview:GameOverLabel];
    [self.view addSubview:FinalScoreLabel];
    [self.view addSubview:PlayAgainButton];
    [self.view addSubview:MainMenuButton];
    touchLocked = YES;
    [self performSelector:@selector(removeTouchLock) withObject:nil afterDelay:1.0f];
}

#pragma mark - Remove Methods

- (void)removeReady
{
    [TapToStartLabel removeFromSuperview];
    [overlayView removeFromSuperview];
}

- (void)removeRunning
{
    [ItemLabel removeFromSuperview];
    [TimeLabel removeFromSuperview];
    [ScoreLabel removeFromSuperview];
}

- (void)removePaused
{
    [GamePausedLabel removeFromSuperview];
    [ResumeButton removeFromSuperview];
    [MainMenuButton removeFromSuperview];
    [overlayView removeFromSuperview];
}

- (void)removeGameOver
{
    [GameOverLabel removeFromSuperview];
    [FinalScoreLabel removeFromSuperview];
    [PlayAgainButton removeFromSuperview];
    [MainMenuButton removeFromSuperview];
    [overlayView removeFromSuperview];
}

#pragma mark - Render Methods

- (void)glkView:(GLKView *)view drawInRect:(CGRect)rect
{
    glClearColor(0, 0, 0, 1.0);
    glClear(GL_COLOR_BUFFER_BIT);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_BLEND);
    
    effect.transform.modelviewMatrix = GLKMatrix4Identity;
    [background render];
    
    for (SMGSock *sockToDraw in socksToDraw) {
        GLKMatrix4 translationMatrix = GLKMatrix4Identity;
        translationMatrix = GLKMatrix4Translate(translationMatrix, sockToDraw.pos.x, sockToDraw.pos.y, 0);
        effect.transform.modelviewMatrix = translationMatrix;
        [socks[sockToDraw.val] render];
    }
    
    for (SMGDrawn *effectToDraw in effectsToDraw) {
        GLKMatrix4 translationMatrix = GLKMatrix4Identity;
        translationMatrix = GLKMatrix4Translate(translationMatrix, effectToDraw.pos.x, effectToDraw.pos.y, 0);
        effect.transform.modelviewMatrix = translationMatrix;
        [effects[effectToDraw.val] render];
    }
    
    if (sockGame.hasItem) {
        GLKMatrix4 translationMatrix = GLKMatrix4Identity;
        translationMatrix = GLKMatrix4Translate(translationMatrix, sockGame.item.pos.x, sockGame.item.pos.y, 0);
        translationMatrix = GLKMatrix4Translate(translationMatrix, 100, 100, 0);
        translationMatrix = GLKMatrix4RotateZ(translationMatrix, sockGame.itemRot);
        translationMatrix = GLKMatrix4Translate(translationMatrix, -100, -100, 0);
        effect.transform.modelviewMatrix = translationMatrix;
        [items[sockGame.item.val] render];
    }
    
    if (sockGame.isSelected && sockGame.selected == SOCK) {
        GLKMatrix4 translationMatrix = GLKMatrix4Identity;
        translationMatrix = GLKMatrix4Translate(translationMatrix, sockGame.selectedSock.pos.x, sockGame.selectedSock.pos.y, 0);
        effect.transform.modelviewMatrix = translationMatrix;
        [socks[sockGame.selectedSock.val] render];
    }
}

#pragma mark - Update Methods

- (void)update
{
    [timeString replaceCharactersInRange:NSMakeRange(0, timeString.string.length) withString:[NSString stringWithFormat:@"%d", sockGame.time]];
    TimeLabel.attributedText = timeString;
    
    [scoreString replaceCharactersInRange:NSMakeRange(0, scoreString.string.length) withString:[NSString stringWithFormat:@"%d", sockGame.score]];
    ScoreLabel.attributedText = scoreString;
    
    [itemString replaceCharactersInRange:NSMakeRange(0, itemString.string.length) withString:sockGame.itemString];
    ItemLabel.attributedText = itemString;
    [ItemLabel setHidden:sockGame.itemStringHidden];
    
    @synchronized(sockGame.effectsToDraw) {
        effectsToDraw = [sockGame.effectsToDraw copy];
    }
    
    @synchronized(sockGame.socksToDraw) {
        socksToDraw = [sockGame.socksToDraw copy];
    }
}

#pragma mark - Touch Handlers

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    if (sockGame.gameState == RUNNING) {
        for (UITouch* touch in touches) {
            CGPoint location = [touch locationInView:self.view];
            float x = location.x*scaleX;
            float y = 800-location.y*scaleY;
            
            if (x < 1080 || y > 200) {
                [sockGame handleTouchDownWithX:x andY:y];
            }
            
            else {
                [self pauseButtonClicked];
            }
        }
    }
    
    else if (sockGame.gameState == READY) {
        if (!touchLocked && [touches count] > 0)
            [self readyScreenPressed];
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
    if (sockGame.gameState == RUNNING) {
        for (UITouch* touch in touches) {
            CGPoint location = [touch locationInView:self.view];
            [sockGame handleTouchDraggedWithX:location.x*scaleX andY:800-location.y*scaleY];
        }
    }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    if (sockGame.gameState == RUNNING) {
        for (UITouch* touch in touches) {
            CGPoint location = [touch locationInView:self.view];
            [sockGame handleTouchUpWithX:location.x*scaleX andY:800-location.y*scaleY];
        }
    }
}

#pragma mark - Initialization Methods

- (void)loadTextures
{
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], GLKTextureLoaderOriginBottomLeft, nil];
    
    GLKTextureInfo *textureInfo = [self loadTextureWithFilename:@"background.png" andOptions:options];
    if (textureInfo) {
        background = [[SMGTexturedSquare alloc] initWithEffect:effect andTextureInfo:textureInfo];
        backgroundTexture = textureInfo.name;
    }
    
    textureInfo = [self loadTextureWithFilename:@"sockatlas.png" andOptions:options];
    if (textureInfo) {
        socks = [self loadSocksWithTextureInfo:textureInfo];
        sockTexture = textureInfo.name;
    }
    
    textureInfo = [self loadTextureWithFilename:@"itematlas.png" andOptions:options];
    if (textureInfo) {
        items = [self loadItemsWithTextureInfo:textureInfo];
        itemTexture = textureInfo.name;
    }
    
    textureInfo = [self loadTextureWithFilename:@"effectatlas.png" andOptions:options];
    if (textureInfo) {
        effects = [self loadEffectsWithTextureInfo:textureInfo];
        effectTexture = textureInfo.name;
    }
    
    options = nil;
    textureInfo = nil;
}

- (GLKTextureInfo *)loadTextureWithFilename:(NSString *)fileName andOptions:(NSDictionary *)options
{
    NSString *path = [[NSBundle mainBundle] pathForResource:fileName ofType:nil];
    
    return [GLKTextureLoader textureWithContentsOfFile:path options:options error:nil];
}

- (NSArray *)loadSocksWithTextureInfo:(GLKTextureInfo *)textureInfo
{
    const float SOCK_WIDTH = 216.0f;
    const float SOCK_HEIGHT = 160.0f;
    const float SOCK_TEXTURE_WIDTH = 0.20;
    const float SOCK_TEXTURE_HEIGHT = 0.20;
    const int NUM_COLS = 5;

    NSMutableArray *sockArray = [NSMutableArray array];
    
    TexturedQuad quad;
    quad.bl.geometryVertex = GLKVector2Make(0, 0);
    quad.br.geometryVertex = GLKVector2Make(SOCK_WIDTH, 0);
    quad.tl.geometryVertex = GLKVector2Make(0, SOCK_HEIGHT);
    quad.tr.geometryVertex = GLKVector2Make(SOCK_WIDTH, SOCK_HEIGHT);
    
    float row, col, u, u2, v, v2;
    
    for (int i = 0; i < NUM_DIFF_SOCKS; i++) {
        row = i / NUM_COLS;
        col = i % NUM_COLS;
        
        u = col * SOCK_TEXTURE_WIDTH;
        u2 = u + SOCK_TEXTURE_WIDTH;
        v2 = 1 - (row * SOCK_TEXTURE_HEIGHT);
        v = v2 - SOCK_TEXTURE_HEIGHT;
        
        quad.bl.textureVertex = GLKVector2Make(u, v);
        quad.br.textureVertex = GLKVector2Make(u2, v);
        quad.tl.textureVertex = GLKVector2Make(u, v2);
        quad.tr.textureVertex = GLKVector2Make(u2, v2);

        [sockArray addObject:[[SMGTexturedSquare alloc] initWithEffect:effect textureInfo:textureInfo texturedQuad:quad]];
    }
    
    return sockArray;
}

- (NSArray *)loadItemsWithTextureInfo:(GLKTextureInfo *)textureInfo
{
    const float ITEM_DIM = 200;
    const float ITEM_TEXTURE_DIM = 0.5;
    const int NUM_COLS = 2;
    
    NSMutableArray *itemArray = [NSMutableArray array];
    
    TexturedQuad quad;
    quad.bl.geometryVertex = GLKVector2Make(0, 0);
    quad.br.geometryVertex = GLKVector2Make(ITEM_DIM, 0);
    quad.tl.geometryVertex = GLKVector2Make(0, ITEM_DIM);
    quad.tr.geometryVertex = GLKVector2Make(ITEM_DIM, ITEM_DIM);
    
    float row, col, u, u2, v, v2;
    
    for (int i = 0; i < NUM_DIFF_ITEMS; i++) {
        row = i / NUM_COLS;
        col = i % NUM_COLS;
        
        u = col * ITEM_TEXTURE_DIM;
        u2 = u + ITEM_TEXTURE_DIM;
        v2 = 1 - (row * ITEM_TEXTURE_DIM);
        v = v2 - ITEM_TEXTURE_DIM;
        
        quad.bl.textureVertex = GLKVector2Make(u, v);
        quad.br.textureVertex = GLKVector2Make(u2, v);
        quad.tl.textureVertex = GLKVector2Make(u, v2);
        quad.tr.textureVertex = GLKVector2Make(u2, v2);

        [itemArray addObject:[[SMGTexturedSquare alloc] initWithEffect:effect textureInfo:textureInfo texturedQuad:quad]];
    }
         
    return itemArray;
}

- (NSArray *)loadEffectsWithTextureInfo:(GLKTextureInfo *)textureInfo
{
    const float EFFECT_TEXTURE_DIM = 0.5;
    const int NUM_COLS = 2;
    const float EFFECT_WIDTH = 216.0f;
    const float EFFECT_HEIGHT = 160.0f;

    NSMutableArray *effectArray = [NSMutableArray array];
    
    TexturedQuad quad;
    quad.bl.geometryVertex = GLKVector2Make(0, 0);
    quad.br.geometryVertex = GLKVector2Make(EFFECT_WIDTH, 0);
    quad.tl.geometryVertex = GLKVector2Make(0, EFFECT_HEIGHT);
    quad.tr.geometryVertex = GLKVector2Make(EFFECT_WIDTH, EFFECT_HEIGHT);
    
    float row, col, u, u2, v, v2;
    
    for (int i = 0; i < NUM_DIFF_ITEMS; i++) {
        row = i / NUM_COLS;
        col = i % NUM_COLS;
        
        u = col * EFFECT_TEXTURE_DIM;
        u2 = u + EFFECT_TEXTURE_DIM;
        v2 = 1 - (row * EFFECT_TEXTURE_DIM);
        v = v2 - EFFECT_TEXTURE_DIM;
        
        quad.bl.textureVertex = GLKVector2Make(u, v);
        quad.br.textureVertex = GLKVector2Make(u2, v);
        quad.tl.textureVertex = GLKVector2Make(u, v2);
        quad.tr.textureVertex = GLKVector2Make(u2, v2);
        
        [effectArray addObject:[[SMGTexturedSquare alloc] initWithEffect:effect textureInfo:textureInfo texturedQuad:quad]];
    }
    
    return effectArray;
}

- (void)initAttributedStringsAndUILabels
{
    //
    //Set string attributes
    //
    textAttributes = @{
                            NSFontAttributeName: [UIFont fontWithName:@"ChalkboardSE-Bold" size:(28.0f*screenSize.width/320)],
                            NSForegroundColorAttributeName: [UIColor whiteColor],
                            NSStrokeColorAttributeName: [UIColor blackColor],
                            NSStrokeWidthAttributeName: [NSNumber numberWithFloat:-5.0f]};
    
    //
    //Ready State
    //
    TapToStartLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, screenSize.height, screenSize.width)];
    [TapToStartLabel setBackgroundColor:[UIColor clearColor]];
    [TapToStartLabel setCenter:CGPointMake(screenSize.height/2, screenSize.width/2)];
    [TapToStartLabel setTextAlignment:NSTextAlignmentCenter];
    TapToStartLabel.attributedText = [[NSAttributedString alloc] initWithString:@"Tap to Start" attributes:textAttributes];
    
    //
    //Running State
    //
    
    //Item
    itemString = [[NSMutableAttributedString alloc] initWithString:@"Item" attributes:textAttributes];
    ItemLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, screenSize.height, screenSize.width)];
    [ItemLabel setBackgroundColor:[UIColor clearColor]];
    [ItemLabel setTextAlignment:NSTextAlignmentCenter];
    [ItemLabel setCenter:CGPointMake(screenSize.height/2, screenSize.width/4)];
    ItemLabel.attributedText = itemString;
    [ItemLabel setHidden:YES];
    
    //Time
    timeString = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"%d", sockGame.time] attributes:textAttributes];
    TimeLabel = [[UILabel alloc] initWithFrame:CGRectMake(1080/scaleX, 0, 200/scaleX, 200/scaleY)];
    [TimeLabel setBackgroundColor:[UIColor clearColor]];
    [TimeLabel setTextAlignment:NSTextAlignmentCenter];
    TimeLabel.attributedText = timeString;
    
    //Score
    scoreString= [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"%d", sockGame.score] attributes:textAttributes];
    ScoreLabel = [[UILabel alloc] initWithFrame:CGRectMake(880/scaleX, 200/scaleY, 400/scaleX, 200/scaleY)];
    [ScoreLabel setBackgroundColor:[UIColor clearColor]];
    [ScoreLabel setTextAlignment:NSTextAlignmentCenter];
    [ScoreLabel setCenter:CGPointMake(1180/scaleX, 300/scaleY)];
    ScoreLabel.attributedText = scoreString;
    
    //
    //Paused State
    //
    GamePausedLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, screenSize.height, screenSize.width)];
    [GamePausedLabel setBackgroundColor:[UIColor clearColor]];
    [GamePausedLabel setCenter:CGPointMake(screenSize.height/2, screenSize.width/4)];
    [GamePausedLabel setTextAlignment:NSTextAlignmentCenter];
    GamePausedLabel.attributedText = [[NSAttributedString alloc] initWithString:@"Game Paused" attributes:textAttributes];
    
    //
    //GameOver State
    //
    GameOverLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, screenSize.height, screenSize.width)];
    [GameOverLabel setBackgroundColor:[UIColor clearColor]];
    [GameOverLabel setTextAlignment:NSTextAlignmentCenter];
    [GameOverLabel setCenter:CGPointMake(screenSize.height/2, screenSize.width/8)];
    GameOverLabel.attributedText = [[NSAttributedString alloc] initWithString:@"Game Over" attributes:textAttributes];
    FinalScoreLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, screenSize.height, screenSize.width)];
    [FinalScoreLabel setBackgroundColor:[UIColor clearColor]];
    [FinalScoreLabel setTextAlignment:NSTextAlignmentCenter];
    [FinalScoreLabel setCenter:CGPointMake(screenSize.height/2, screenSize.width/4)];
}

- (void)initButtons
{
    ResumeButton = [[UIButton alloc] initWithFrame:CGRectMake(50/scaleX, screenSize.width/2 - screenSize.width/8, 540/scaleX, 400/scaleY)];
    [ResumeButton setBackgroundImage:[UIImage imageNamed:@"resumeButton.png"] forState:UIControlStateNormal];
    [ResumeButton addTarget:self action:@selector(resumeButtonClicked) forControlEvents:UIControlEventTouchUpInside];
    
    MainMenuButton = [[UIButton alloc] initWithFrame:CGRectMake(690/scaleX, screenSize.width/2 - screenSize.width/8, 540/scaleX, 400/scaleY)];
    [MainMenuButton setBackgroundImage:[UIImage imageNamed:@"mainMenuButton.png"] forState:UIControlStateNormal];
    [MainMenuButton addTarget:self action:@selector(mainMenuButtonClicked) forControlEvents:UIControlEventTouchUpInside];
    
    PlayAgainButton = [[UIButton alloc] initWithFrame:CGRectMake(50/scaleX, screenSize.width/2 - screenSize.width/8, 540/scaleX, 400/scaleY)];
    [PlayAgainButton setBackgroundImage:[UIImage imageNamed:@"playAgainButton.png"] forState:UIControlStateNormal];
    [PlayAgainButton addTarget:self action:@selector(playAgainButtonClicked) forControlEvents:UIControlEventTouchUpInside];
}

#pragma mark - Selector Methods

- (void)removeTouchLock
{
    touchLocked = NO;
}

#pragma mark - Button Selector Methods

- (void)readyScreenPressed
{
    [self removeReady];
    [self prepareRunning];
    [sockGame resume];
}

- (void)pauseButtonClicked
{
    [sockGame pause];
    [self removeRunning];
    [self preparePaused];
}

- (void)resumeButtonClicked
{
    [self removePaused];
    [self prepareRunning];
    [sockGame resume];
}

- (void)mainMenuButtonClicked
{
    if (sockGame.gameState == PAUSED) {
        [self performSegueWithIdentifier:@"unwindToMainMenu" sender:self];
    }
    
    else if (sockGame.gameState == GAMEOVER && !touchLocked) {
        [self performSegueWithIdentifier:@"unwindToMainMenu" sender:self];
    }
}

- (void)playAgainButtonClicked
{
    if (!touchLocked) {
        [sockGame reset];
        [self removeGameOver];
        [self prepareReady];
    }
}

- (void)endingGame
{
    [self removeRunning];
    [self prepareGameOver];
}
@end
