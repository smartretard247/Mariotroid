package Main;

import Drawing.Scene;
import Drawing.DrawLib;
import Enumerations.START_MENU_OPTION;
import Enumerations.GAME_MODE;
import Enumerations.ID;
import Enumerations.PAUSE_MENU_OPTION;
import Enumerations.SOUND_EFFECT;
import Enumerations.TEX;
import Test.TestDisplay;
import java.awt.event.*;
import javax.swing.*;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_ONE;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * A template for a basic JOGL application with support for animation, and for
 * keyboard and mouse event handling, and for a menu.  To enable the support, 
 * uncomment the appropriate lines in main(), in the constructor, and in the
 * init() method.  See all the lines that are marked with "TODO".
 * 
 * See the JOGL documentation at http://jogamp.org/jogl/www/
 * Note that this program is based on JOGL 2.3, which has some differences
 * from earlier versions; in particular, some of the package names have changed.
 */
public class Engine extends JPanel implements GLEventListener, KeyListener, MouseListener, 
        MouseMotionListener, ActionListener {
  //////// VARIBLES
  private static boolean debugging = true;
  
  private final GLJPanel display;
  private final Dimension windowDim = new Dimension(1280,720);
  private Timer animationTimer;
  private static int frameNumber = 0; // The current frame number for an animation.
  private static Timer introTimer;
  private final int INTROLENGTHMS = debugging ? 0 : 3000;
  private static final Timer MESSAGE_TIMER = new Timer(4000, null);
  private static final LinkedList<String> CONVERSATION = new LinkedList<>();
  private static long score = 0;
  private DrawLib drawLib;
  private static GAME_MODE gameMode = GAME_MODE.INTRO;
  private START_MENU_OPTION startMenuSelection = START_MENU_OPTION.START_GAME;
  private PAUSE_MENU_OPTION pauseMenuSelection = PAUSE_MENU_OPTION.CONTINUE;
  private boolean won = false;
  private boolean showControls = true;
  private boolean showDecor = true;
  private boolean swapBackground = false;
  private static boolean soundEnabled = true;
  private final TestDisplay testDisplay = new TestDisplay();
  private final Scene scene = new Scene(-600, -600, 0, 0.5f, 0.5f, 1.0f);; // trans x & y & z, scale x & y & z
  private final PhysicsEngine phy = new PhysicsEngine();
  private static final ObjectContainer GAME = new ObjectContainer();
  private final LinkedBlockingQueue<NextProjectile> qProjectiles = new LinkedBlockingQueue<>();
  private final int TOTAL_LEVELS = 2;
  private int currLevel = 1;
  private Hero hero;
  private boolean leftPressed = false;
  private boolean rightPressed = false;
  private Point currentMousePos;
  private Interactive interactiveObject;
  private boolean pendingInteraction = false;
  private final Rectangle WORLD_WINDOW = new Rectangle();
  
  // getters / setters
  public static ObjectContainer getGameContainer() { return GAME; }
  public static long getScore() { return score; }
  public static void resetScore() { score = 0; }
  public static void addScore(int points) { score += points; }
  public static void setGameMode(GAME_MODE mode) { Engine.gameMode = mode; }
  public static int getFrameNumber() { return frameNumber; }
  public static boolean isSoundEnabled() { return soundEnabled; }
  public static boolean isDebugging() { return debugging; }
  
  ///// START METHODS
  public static void main(String[] args) {
    JFrame window = new JFrame("Mariotroid");
    Engine panel = new Engine();
    window.setContentPane(panel);
    window.pack();
    window.setLocation(10,10);
    window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    window.setVisible(true);
    panel.setFocusable(false);
    window.addWindowListener(new WindowAdapter(){
      @Override
      public void windowClosing(WindowEvent e){
        if(debugging) TestDisplay.writeToFile();
        window.dispose();
        System.exit(0);
      }
    });
  }

  @SuppressWarnings("LeakingThisInConstructor")
  public Engine() {
    GLCapabilities caps = new GLCapabilities(null);
    display = new GLJPanel(caps);
    display.setPreferredSize( windowDim );
    display.addGLEventListener(this);
    setLayout(new BorderLayout());
    add(display,BorderLayout.CENTER);

    // enable keyboard and mouse event handling
    display.requestFocusInWindow();
    display.addKeyListener(this);
    display.addMouseListener(this);
    display.addMouseMotionListener(this);
  }

  /**
   * This method is called when the OpenGL display needs to be redrawn.
   * @param drawable
   */
  @Override
  public void display(GLAutoDrawable drawable) { // called when the panel needs to be drawn
    GL2 gl = drawable.getGL().getGL2();
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    gl.glLoadIdentity();
    draw(gl);
  }

  /**
   * This is called when the GLJPanel is first created.  It can be used to initialize
   * the OpenGL drawing context.
   * @param drawable
   */
  @Override
  public void init(GLAutoDrawable drawable) { // called when the panel is created
    GL2 gl = drawable.getGL().getGL2();
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();
    gl.glFrustum(-windowDim.width/2, windowDim.width/2 ,-windowDim.height/2, windowDim.height/2, 9.9, 101);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glClearColor(0, 0.4f, 0.8f, 0);
    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    drawLib = new DrawLib(gl); // initialize the drawing library before dealing with any textures!!
    MESSAGE_TIMER.setRepeats(false);
    SOUND_EFFECT.init(); // uncommment once all wav's in enum SOUND_EFFECT have been added to dir /res/sound
    currentMousePos = new Point();
    startAnimation(); // start the animation, also controls pause function (and remove keyboard response)
    introTimer = new Timer(INTROLENGTHMS, (evt)-> { gameMode = GAME_MODE.START_MENU; });
    introTimer.setRepeats(false);
    introTimer.start();
  
    // initial hero settings
    hero = new Hero(ID.HERO, 3, 10, TEX.HERO); // objId, 3 lives, 10 health, 0 score, texId, x, y
    GAME.addGO(hero);
  }
  
  /**
   * Wipe all game objects and add all level objects to given level number.
   * @param level 
   */
  private void setupVisibles(int level) {
    PhysicsEngine.resetGravity();
    GAME.clearGOs(); // will not clear the hero
    Hero h = (Hero)GAME.getGO(ID.HERO);
    Boss b;
    switch(level) {
      case 1:// only add level 1 visible objects to this map
        loadDefaults();
        h.setDefaultPosition(300, 189);
        h.resetAll();
        resetScore();
        hero.setGodMode(false);
        GAME.addGO(new Item(ID.JETPACK, TEX.JETPACK, 1800, 800));
        GAME.addGO(new Item(ID.SHELL, TEX.SHELL, 300, 800));
        GAME.addGO(new Item(ID.ARMOR, TEX.ARMOR, 5660, 198));
        GAME.addGO(new Enemy(ID.ENEMY_1, 1, 1, TEX.ENEMY_BASIC, 2000, 800, new Point.Float(-5,0))); // objId, 1 life, 1 health, texId, x, y, sx/sy
        GAME.addGO(new Enemy(ID.ENEMY_2, 1, 1, TEX.ENEMY_BASIC, 4000, 800, new Point.Float(5,0)));
        GAME.addGO(new Enemy(ID.ENEMY_3, 1, 1, TEX.ENEMY_BASIC, 8075, 240, new Point.Float(5,0)));
        GAME.addGO(new Boss(ID.CALAMITY, 1, 20, TEX.CALAMITY, 11000, 500, new Point.Float(10,10), 500));
        b = (Boss)GAME.getGO(ID.CALAMITY);
        b.setMinX(8930);
        GAME.addGO(new Door(ID.DOOR, 11100, 163, 75, 0));
        int boxHeight = DrawLib.getTexture(TEX.BOX).getHeight();
        GAME.addGO(new FallingBox(ID.FALLING_BOX, TEX.BOX, 760, 960));
        GAME.addGO(new FlyingBox(ID.FLYING_BOX, TEX.BOX, 8810, 960-boxHeight*2));
        GAME.addGO(new FlyingBox(ID.FLYING_BOX_2, TEX.BOX, 8810, 960-boxHeight));
        GAME.addGO(new FlyingBox(ID.FLYING_BOX_3, TEX.BOX, 8810, 960));
        break;
      case 2:// setup level 2, only add level 2 game objects to this map
        GAME.addGO(new Enemy(ID.ENEMY_1, 1, 1, TEX.ENEMY_BASIC, 10000, 950, new Point.Float(5,0)));
        GAME.addGO(new Enemy(ID.ENEMY_2, 1, 1, TEX.ENEMY_BASIC, 4000, 950, new Point.Float(5,0)));
        GAME.addGO(new Enemy(ID.ENEMY_3, 1, 1, TEX.ENEMY_BASIC, 8075, 950, new Point.Float(5,0)));
        GAME.addGO(new Boss(ID.CALAMITY, 1, 20, TEX.CALAMITY, 300, 575, new Point.Float(10,10), 750));
        b = (Boss)GAME.getGO(ID.CALAMITY);
        b.setMinX(0);
        b.setMaxX(2570);
        GAME.addGO(new Door(ID.DOOR, 300, 987, -60, 70, true));
        GAME.addGO(new GravitySwitch(ID.SWITCH, TEX.SWITCH, 5366, 702));
        break;
      default: System.out.println("Unknown level number while resetting visibles."); break;
    }
  }
  
  /**
   * Scans PNG image for black rectangles, adding each as a Collidable object on supplied level number.
   * @param num 
   */
  public void loadLevel(int num) {
    String fileName = "";
    switch(num) {
      case 1: fileName = "/res/layer_collision_1.png"; break;
      case 2: fileName = "/res/layer_collision_2.png"; break;
      default: System.out.println("Unknown level number while loading level collisions."); break;
    }
    
    if(!fileName.equals("")) {
      setupVisibles(num);
      LevelBuilder levelBuilder = new LevelBuilder(fileName);
      ArrayList<Rectangle> level = levelBuilder.scanForBoundaries();
      level.stream().forEach((r) -> {
        GAME.addTO(ID.getNewId(), new Platform(ID.getLastId(), TEX.LEVEL, r.x(), r.y(), r.w(), r.h()));
        //GAME.addTO(ID.getNewId(), new Platform(ID.getLastId(), TEX.LEVEL, r.x()-r.w()/2, r.y(), 1, r.h()));
        //GAME.addTO(ID.getNewId(), new Platform(ID.getLastId(), TEX.LEVEL, r.x()+r.w()/2, r.y(), 1, r.h()));
        //GAME.addTO(ID.getNewId(), new Platform(ID.getLastId(), TEX.LEVEL, r.x(), r.y()+r.h()/2, r.w(), 1));
        //GAME.addTO(ID.getNewId(), new Platform(ID.getLastId(), TEX.LEVEL, r.x(), r.y()-r.h()/2, r.w(), 1));
      });
    }
    
    leftPressed = false;
    rightPressed = false;
  }
  
  /**
   * Load defaults for the first level only.
   */
  public void loadDefaults() {
    currLevel = 1; // no need to adjust level number on any further cases
    swapBackground = false;
    showDecor = true;
    won = false;
    scene.resetAll(); // do not change scene in other cases
    SOUND_EFFECT.THEME.playLoop();
    TestDisplay.resetLogWindow();
    hero.setName("Hero");
    Engine.setConversation(new String[] { hero.getName() + ": where am I?", "Could this be a dream?", "I'd better go take a look around." });
  }
  
  /**
   * Increases the current level, sets game mode to WARPING, and calls loadLevel.  If out of levels
   * will set won to true.
   */
  public void jumpToNextLevel() {
    if(++currLevel <= TOTAL_LEVELS) {
      hero.setSpeedX(0);
      gameMode = GAME_MODE.WARPING;
      loadLevel(currLevel);
    } else {
      setConversation(new String[] { "  YOU WIN!!  " });
      won = true;
    }
  }
  
  /**
   * Draws the scene, for each given game mode.
* @param gl 
  */
  private void draw(GL2 gl) {
    gl.glPushMatrix(); // save initial transform
    gl.glTranslated(0, 0, -10); // push everything away by 10
    switch(gameMode) {
      case INTRO: drawIntro(gl); break; // END INTRO
      case START_MENU: drawStartMenu(gl); break; // END START MENU
      case DYING:
      case TALKING:
      case SLOW_MO:
      case RUNNING: drawNormalGamePlay(gl); break; // END RUNNING
      case PAUSED: drawPauseMenu(gl); break; // END PAUSED
      case GAME_OVER: drawGameOver(gl); break;
      case CREDITS: drawCredits(gl); break;
      case WIN: drawWin(gl); break;
      default: break;
    }
    drawLog(gl);
    if(debugging) drawDebugWindow(gl);
    gl.glPopMatrix(); // return to initial transform
  }
  
  /**
   * Adjusts the scene with the hero's movement.
   * @param gl 
   * @param forBackgroundScene adjust for the background scene?
   */
  public void translateScene(GL2 gl, boolean forBackgroundScene) {
    float x = scene.transX;
    float y = scene.transY;
    float z = scene.transZ+scene.globalZ;
    if(!forBackgroundScene) {
      if(hero.getX() > 600 && hero.getX() < 10500)
        x = -hero.getX();
      else if(hero.getX() >= 10500)
        x = -10500;
    } else {
      x *= -10;
      if(!(hero.getX() <= 600))
        x += hero.getX()/10;
      y *= -3;
      z = scene.globalZ-scene.LEVEL_DEPTH*currLevel;
    }
    gl.glTranslated(x, y, z);
  }
  
  /**
   * Sets up the world rectangle based on the current view matrix.
   */
  public void refreshWorldWindow() {
    Point.Float wcTopLeft = DrawLib.screenToWorld(new Point(0,0));
    Point.Float wcBottomRight = DrawLib.screenToWorld(new Point(windowDim.width, windowDim.height));
    WORLD_WINDOW.setX(wcTopLeft.x);
    WORLD_WINDOW.setY(wcTopLeft.y);
    WORLD_WINDOW.setW(Math.abs(wcBottomRight.x-wcTopLeft.x));
    WORLD_WINDOW.setH(Math.abs(wcBottomRight.y-wcTopLeft.y));
  }
  
  /**
   * This is the standard loop for the game, showing level, character, enemies, etc.
   * @param gl 
   */
  private void drawNormalGamePlay(GL2 gl) {
    drawBackground(gl);
    gl.glPushMatrix(); // save initial transform
    gl.glScaled(scene.scaleX, scene.scaleY, scene.scaleZ); // set global scale
    translateScene(gl, false);
    refreshWorldWindow();
    detectInteractiveObject();
    drawLevel(gl);
    drawGameObjects(gl);
    drawHero(gl);
    fireProjectiles(); // fire projectile from the queue
    drawForeground(gl);
    gl.glPopMatrix(); // return to initial transform
    drawHud(gl);
    if(showControls) drawControls(gl);
    drawConversation(gl);
  }
  
  /**
   * Draws a conversation of text across the top of the scene.
   * @param gl the drawing context
   */
  private void drawConversation(GL2 gl) {
    String message = (!CONVERSATION.isEmpty()) ? CONVERSATION.peek() : null;
    if(MESSAGE_TIMER.isRunning() && message != null) { // check if we need to display a message
      int calculatedOffset = 5 * message.length();
      int calculatedHeight = 30; // * message[].length();
      gl.glPushMatrix();
      gl.glTranslated(0, DrawLib.getTexture(TEX.HUD).getHeight()/2-50, 0); // 20 moves to center
      // draw box behind text
      Drawable textBox = new Drawable(TEX.NONE, -message.length(), 5, calculatedOffset*2+message.length(), calculatedHeight*2);
      textBox.setColor(0, 0.2f, 1f);
      textBox.draw();
      gl.glColor3f(1.0f, 1.0f, 0);
      DrawLib.drawText(message, -calculatedOffset, 0);
      gl.glPopMatrix();
    } else if(!CONVERSATION.isEmpty()) {
      CONVERSATION.pop();
      MESSAGE_TIMER.restart();
    }
    if(gameMode == GAME_MODE.TALKING) {
      if(CONVERSATION.isEmpty()) gameMode = GAME_MODE.RUNNING;
    }
  }
  
  /**
   * Draws "GAME OVER" as scrolling text.
   * @param gl 
   */
  private void drawGameOver(GL2 gl) {
    gl.glPushMatrix();
    gl.glColor3fv(new float[] { 1.0f, 0.0f, 0.0f }, 0);
    gl.glTranslated(-DrawLib.getTexture(TEX.HUD).getWidth()/2, 0, 0);
    DrawLib.drawText("GAME OVER", 100+(frameNumber%500*2), 0);
    gl.glPopMatrix();
  }
  
  /**
   * Draws the control guide across the bottom of the scene.
   * @param gl 
   */
  private void drawControls(GL2 gl) {
      float yOffset = 10, vSpace = 20;
      float[] textColor = new float[] { 1.0f, 0.5f, 0.0f };
      String[] debugControls = { "Debugging:", "F4 - Toggle debug", "F5 - Toggle god mode", "F6 - Toggle backgrounds", "F7 - Toggle level decor", "F8 - Jump to next level" };
      String[] keyboardControls = { "Keyboard:", "A - Move left", "D - Move right", "W - Climb wall", "S - Descend wall", "F - Interact", "SHIFT - Toggle run", "SPACE - Jump/double jump", "P - Pause" };
      String[] mouseControls = { "          Right click - Fire alternate weapon", "Mouse: Left click - Fire primary weapon", "F10 - Control display on/off", "F9 - Adjust volume" };
      gl.glPushMatrix();
      gl.glColor3fv(textColor, 0);
      gl.glTranslated(-DrawLib.getTexture(TEX.HUD).getWidth()/2, -DrawLib.getTexture(TEX.HUD).getHeight()/2+yOffset, 0);
      if(debugging) {
        gl.glPushMatrix();
        for(String s : debugControls) {
          DrawLib.drawText(s, 0, 0);
          gl.glTranslated(s.length()*11,0,0);
        }
        gl.glPopMatrix();
        gl.glTranslated(0,vSpace,0);
      }
      gl.glPushMatrix();
      for(String s : keyboardControls) {
        DrawLib.drawText(s, 0, 0);
        gl.glTranslated(s.length()*10,0,0);
      }
      gl.glPopMatrix();
      gl.glTranslated(0,vSpace,0);
      for(String s : mouseControls) {
        DrawLib.drawText(s, 0, 0);
        gl.glTranslated(0,vSpace,0);
      }
      gl.glPopMatrix();
  }
  
  /**
   * Draws scrolling credits.
   * @param gl 
   */
  private void drawCredits(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(TEX.HUD).getWidth()/2, 0, 0);
    ArrayList<String> credits = new ArrayList<>();
    credits.add("TEAM MARIOTROID!");
    credits.add("----------------");
    credits.add("");
    credits.add("Greggie Pascual - Project Lead");
    credits.add("");
    credits.add("Marvelous Agabi - Documentation");
    credits.add("");
    credits.add("Nathan Mitson - Testing");
    credits.add("");
    credits.add("Matthew Miller - Design");
    credits.add("");
    credits.add("Jesse Young - Developer");
    credits.add("");
    credits.add("William Malone - Developer");

    gl.glColor3fv( new float[] { 0.0f, 1.0f, 0.0f }, 0);
    for(int i = 0; i < credits.size(); i++)
      DrawLib.drawText(credits.get(i), windowDim.width/2-60, -windowDim.height/2+(frameNumber%500*2)-(i*20));
    gl.glPopMatrix();
  }
  
  /**
   * Draws scrolling "THE END"
   * @param gl 
   */
  private void drawWin(GL2 gl) {
    gl.glPushMatrix();
    gl.glColor3fv( new float[] { (float)Math.random(), (float)Math.random(), (float)Math.random() }, 0);
    gl.glTranslated(-DrawLib.getTexture(TEX.HUD).getWidth()/2, 0, 0);
    DrawLib.drawText("THE END", 100+(frameNumber%500*2), 0);
    gl.glPopMatrix();
  }
  
  /**
   * Draws the background once per frame.
   * @param gl 
   */
  private void drawBackground(GL2 gl) {
    gl.glPushMatrix();
    gl.glScaled(1.1, 1.1, 1);
    gl.glColor3f(1f, 1f, 1f);
    if(!swapBackground) {
      DrawLib.drawTexturedRectangle(TEX.BACKGROUND_1, DrawLib.getTexture(TEX.HUD).getWidth(), DrawLib.getTexture(TEX.HUD).getHeight()); // background level
    } else {
      DrawLib.drawTexturedRectangle(TEX.BACKGROUND_2, DrawLib.getTexture(TEX.HUD).getWidth(), DrawLib.getTexture(TEX.HUD).getHeight()); // secondary background level
    }
    gl.glPopMatrix();
  }
  
  /**
   * Draws background level decor and all visible objects for current level.
   * @param gl 
   */
  private void drawLevel(GL2 gl) {
    gl.glPushMatrix();
    gl.glColor3f(1f, 1f, 1f);
    translateScene(gl, true);
    if(currLevel < TOTAL_LEVELS)
      DrawLib.drawTexturedRectangle(TEX.LEVEL_DECOR_1+(currLevel%2)); // draw the background level
    gl.glPopMatrix();
  }
  
  /**
   * Draws the hero on top of the scene.
   * @param gl 
   */
  private void drawHero(GL2 gl) {
    //hero.draw(); // handled by drawing visible objects, no need for a second call
  }
  
   /**
    * Draws any foreground objects to simulate depth.
    * @param gl 
    */
  private void drawForeground(GL2 gl) {
    // draw foreground objects here
    gl.glPushMatrix();
    gl.glColor3f(1f, 1f, 1f);
    gl.glTranslated(DrawLib.getTexture(TEX.LEVEL_DECOR_1).getWidth()/2, DrawLib.getTexture(TEX.LEVEL_DECOR_1).getHeight()/2, 0); // levels MUST be same size as DECOR_1
    if(showDecor) {
      int decorIdOffset = Math.abs(currLevel%2-1); // will either be 0 or 1, to swap between pre-loaded decor
      if(currLevel > TOTAL_LEVELS) decorIdOffset = Math.abs(--decorIdOffset);
      DrawLib.drawTexturedRectangle(TEX.LEVEL_DECOR_1+decorIdOffset);
    }
    gl.glPopMatrix();
  }
  
  /**
   * Draws the logo.
   * @param gl 
   */
  private void drawIntro(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(0, 0, 0);
    DrawLib.drawTexturedRectangle(TEX.LOGO);
    gl.glPopMatrix();
  }

  /**
   * Draws the start menu.
   * @param gl 
   * @param isPauseMenu is this the pause menu (only changes text that is displayed)
   */
  private void drawStartMenu(GL2 gl) {
    int y = (frameNumber-(debugging ? 0 : 80) < 175) ? frameNumber-(debugging ? 0 : 80) : 175;
    gl.glPushMatrix();
    gl.glTranslated(0, y, 0);
    DrawLib.drawTexturedRectangle(TEX.LOGO);
    gl.glPopMatrix();
    drawMenu(gl, START_MENU_OPTION.getValuesArray(), startMenuSelection.ordinal());
  }

  /**
   * Draws the pause screen.
   * @param gl 
   */
  private void drawPauseMenu(GL2 gl) {
    gl.glColor3fv(new float[] { 1.0f, 1.0f, 0.0f }, 0); // draw "PAUSED" in yellow first
    gl.glPushMatrix();
    DrawLib.drawText("GAME PAUSED", -60, 300);
    gl.glPopMatrix();
    // handle the actual menu options
    drawMenu(gl, PAUSE_MENU_OPTION.getValuesArray(), pauseMenuSelection.ordinal());
  }
  
  /**
   * Draws a menu with supplied string array, will highlight option at index provided.
   * @param gl
   * @param indexSelected the index to highlight
   */
  private void drawMenu(GL2 gl, ArrayList<String> options, int indexSelected) {
    int top = 50, spacing = -100;
    float[] selectedTextColor = new float[] { 1.0f, 0.0f, 0.0f };
    float[] textColor = new float[] { 0.0f, 0.0f, 0.0f };
    
    gl.glPushMatrix();
    gl.glTranslated(0, top, 0);
    for(int i = 0; i < options.size(); i++) {
      boolean selected = false;
      if(i == indexSelected) {
        selected = true;
        gl.glColor3fv( selectedTextColor, 0);
      } else {
        gl.glColor3fv( textColor, 0);
      }
      int calculatedXOffset = -5 * options.get(i).length();
      DrawLib.drawText(((selected) ? "-->" : "") + options.get(i) + ((selected) ? "<--" : ""), calculatedXOffset + ((selected) ? -30 : 0), 0);
      gl.glTranslated(0, spacing, 0);
    }
    gl.glPopMatrix();
  }
  
  /**
   * Performs the selected menu action, based on the current game mode.
   */
  private void doMenuSelection() {
    switch (gameMode) {
      case PAUSED:
        switch(this.pauseMenuSelection) {
          case RESTART:
            CONVERSATION.clear();
            gameMode = GAME_MODE.RUNNING;
            loadLevel(1);
            break;
          case CONTINUE:
            if(CONVERSATION.isEmpty()) gameMode = GAME_MODE.RUNNING;
            else gameMode = GAME_MODE.TALKING;
            break;
          default: break;
        }
        break;
      case START_MENU:
        switch(this.startMenuSelection) {
          case START_GAME:
            gameMode = GAME_MODE.RUNNING;
            loadLevel(1);
            break;
          case EXIT:
            System.exit(0);
            break;
          default: break;
        }
        break;
      default: break;
    }
  }

  /**
   * Draws the hud, then health, lives, score and ammo count.
   * @param gl 
   */
  private void drawHud(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(0, 0, 0);
    DrawLib.drawTexturedRectangle(TEX.HUD);
    gl.glPopMatrix();
    drawHealth(gl);
    drawLives(gl);
    drawScore(gl);
    drawAmmoCount(gl);
  }
  
  /**
   * Draws secondary ammo count.
   * @param gl 
   */
  private void drawAmmoCount(GL2 gl) {
    if(hero.hasSecondaryWeapon()) {
      float xDiff = 10;
      float yDiff = -35;
      float[] textColor = new float[] { 1.0f, 1.0f, 1.0f };
      float hudWidth = DrawLib.getTexture(TEX.HUD).getWidth();
      float hudHeight = DrawLib.getTexture(TEX.HUD).getHeight();
      float healthHeight = DrawLib.getTexture(TEX.HEALTH).getHeight();
      float shellWidth = DrawLib.getTexture(TEX.SHELL).getWidth()*2;
      float shellHeight = DrawLib.getTexture(TEX.SHELL).getHeight()*2;
      
      gl.glPushMatrix();
      gl.glColor3fv( textColor, 0);
      gl.glTranslated(-hudWidth/2+shellWidth/2+xDiff, hudHeight/2 - healthHeight - shellHeight + yDiff, 0);
      DrawLib.drawTexturedRectangle(TEX.SHELL, shellWidth, shellHeight);
      DrawLib.drawText(Integer.toString(hero.getAmmoCount()), 25, -6);
      gl.glPopMatrix();
    }
  }

  /**
   * Draws a health bar per health, max ten.
   * @param gl 
   */
  private void drawHealth(GL2 gl) { // draw health in top left corner
    float xDiff = 10;
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(TEX.HUD).getWidth()/2+DrawLib.getTexture(TEX.HEALTH).getWidth()+xDiff,
            DrawLib.getTexture(TEX.HUD).getHeight()/2-DrawLib.getTexture(TEX.HEALTH).getHeight(), 0);
    for(int i = 0; i < hero.getHealth(); i++) {
      DrawLib.drawTexturedRectangle(TEX.HEALTH);
      gl.glTranslated(DrawLib.getTexture(TEX.HEALTH).getWidth(), 0, 0);
    }
    gl.glPopMatrix();
  }
  
  /**
   * Draws number of lives.
   * @param gl 
   */
  private void drawLives(GL2 gl) {
    float[] textColor = new float[] { 1.0f, 1.0f, 1.0f };
    gl.glColor3d(textColor[0], textColor[1], textColor[2]);
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(TEX.HUD).getWidth()/2,
            DrawLib.getTexture(TEX.HUD).getHeight()/2, 0);
    DrawLib.drawText(Integer.toString(hero.getLives()), 40, -50);
    gl.glPopMatrix();
  }

  /**
   * Draws current score.
   * @param gl 
   */
  private void drawScore(GL2 gl) { // draw score in top right corner
    float diffX = 30;
    float[] textColor = new float[] { 1.0f, 1.0f, 1.0f };
    gl.glColor3d(textColor[0], textColor[1], textColor[2]);
    gl.glPushMatrix();
    gl.glTranslated(DrawLib.getTexture(TEX.HUD).getWidth()/2-diffX,
            DrawLib.getTexture(TEX.HUD).getHeight()/2, 0);
    String text = "SCORE: " + Long.toString(getScore());
    //DrawLib.drawText(text, new Point(DrawLib.getTexture(TEX.HUD).getWidth()-120, DrawLib.getTexture(TEX.HUD).getHeight()-20));
    DrawLib.drawText(text, -120, -20);
    gl.glPopMatrix();
  }
  
  /**
   * Called when the size of the GLJPanel changes.  Note:  glViewport(x,y,width,height)
   * has already been called before this method is called!
   * @param drawable
   * @param x
   * @param y
   * @param width
   * @param height
   */
  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  /**
   * This is called before the GLJPanel is destroyed.  It can be used to release OpenGL resources.
   * @param drawable
   */
  @Override
  public void dispose(GLAutoDrawable drawable) {
  }

  // ------------ Support for a menu -----------------------
  public JMenuBar createMenuBar() {
    JMenuBar menubar = new JMenuBar();
    MenuHandler menuHandler = new MenuHandler(); // An object to respond to menu commands.
    JMenu menu = new JMenu("Menu"); // Create a menu and add it to the menu bar
    menubar.add(menu);
    JMenuItem item = new JMenuItem("Quit");  // Create a menu command.
    item.addActionListener(menuHandler);  // Set up handling for this command.
    menu.add(item);  // Add the command to the menu.

    // TODO:  Add additional menu commands and menus.
    return menubar;
  }

  private boolean checkForWin() {
    if(won) { // check for winning conditions
      if(SOUND_EFFECT.volume != SOUND_EFFECT.Volume.MUTE) {
        SOUND_EFFECT.THEME.stop();
        SOUND_EFFECT.WIN.play(Math.max(SOUND_EFFECT.getGain(), 6f));
      }
      gameMode = GAME_MODE.WIN;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Transforms the view into the z-axis 40 times by an increment of 1.
   */
  private void transitionToNextLevel() {
    // this is for slow mo jumping to next level
    ++scene.globalZ;
    if(scene.globalZ % scene.LEVEL_DEPTH == 0) gameMode = GAME_MODE.RUNNING;
  }

  /**
   * Checks for pending interaction with an object, if true performs interaction.
   */
  private void processPendingInteraction() {
    if(pendingInteraction) {
      hero.interact(interactiveObject);
      pendingInteraction = false;
    }
  }

  private void pauseGame() {
    gameMode = GAME_MODE.PAUSED;
    this.pauseMenuSelection = PAUSE_MENU_OPTION.CONTINUE;
  }

  private void skipSentence() {
    if(!CONVERSATION.isEmpty()) CONVERSATION.pop(); // skip the current text
    MESSAGE_TIMER.restart();
  }

  /**
   * Draws extra live debugging information in the bottom right corner of screen.
   * @param gl drawing context
   */
  private void drawDebugWindow(GL2 gl) {
    gl.glPushMatrix(); // save initial transform
    translateScene(gl, false);
    Point.Float wc = DrawLib.screenToWorld(currentMousePos); // get world coordinates based on scale and translation from above
    gl.glPopMatrix();
    
    gl.glPushMatrix();
    gl.glTranslated(DrawLib.getTexture(TEX.HUD).getWidth()/3-80, -DrawLib.getTexture(TEX.HUD).getHeight()/2.7f, 0);
    gl.glPushMatrix();
    gl.glColor3f(0.2f, 0, 1f);
    gl.glTranslated(140, 10, 0);
    DrawLib.drawRectangle(300, 100);
    gl.glPopMatrix();
    gl.glColor3f(1f, 0.5f, 0);
    DrawLib.drawText("  GAME MODE: " + gameMode.toString(), 25, 35);
    DrawLib.drawText("SCREEN COORD: (" + currentMousePos.x + ", " + currentMousePos.y + ")", 0, 15);
    DrawLib.drawText("WORLD COORD: (" + (int)wc.x + ", " + (int)wc.y + ")", 7, -5);
    DrawLib.drawText("GLOBAL Z: " + (int)scene.globalZ, 59, -25);
    gl.glPopMatrix();
  }

  private void drawGameObjects(GL2 gl) {
    GAME.getVisiblesWithin(WORLD_WINDOW).forEach((c) -> { c.draw(); }); // draw game objects
  }

  /**
   * A class to define the ActionListener object that will respond to menu commands.
   */
  private class MenuHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent evt) {
      String command = evt.getActionCommand();  // The text of the command.
      if (command.equals("Quit")) {
          System.exit(0);
      }
      // TODO: Implement any additional menu commands.
    }
  }


  // ------------ Support for keyboard handling  ------------
  /**
   * Called when the user presses any key on the keyboard, including
   * special keys like the arrow keys, the function keys, and the shift key.
   * Note that the value of key will be one of the constants from
   * the KeyEvent class that identify keys such as KeyEvent.VK_LEFT,
   * KeyEvent.VK_RIGHT, KeyEvent.VK_UP, and KeyEvent.VK_DOWN for the arrow
   * keys, KeyEvent.VK_SHIFT for the shift key, and KeyEvent.VK_F1 for a
   * function key.
   * @param e
   */
  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();  // Tells which key was pressed.
    
    switch (key) {
      case KeyEvent.VK_F4: debugging = !debugging;
        Engine.setStatusMessage((debugging) ? "--DEBUGGING ON--" : "--DEBUGGING OFF--");
        if(!debugging) hero.setGodMode(false);
        break;
      case KeyEvent.VK_F5:
        if(debugging) {
          hero.toggleGodMode();
          Engine.setStatusMessage((hero.getGodMode()) ? "--GOD MODE ON--" : "--GOD MODE OFF--");
        }
        break;
      case KeyEvent.VK_F6: if(debugging) {
          swapBackground = !swapBackground;
          Engine.setStatusMessage((!swapBackground) ? "--BACKGROUND 1--" : "--BACKGROUND 2--");
        }
        break;
      case KeyEvent.VK_F7: if(debugging) { 
          showDecor = !showDecor;
          Engine.setStatusMessage((showDecor) ? "--DECOR ON--" : "--DECOR OFF--");
        }
        break;
      case KeyEvent.VK_F8:
        if(debugging) {
          jumpToNextLevel();
          Engine.setStatusMessage("--LEVEL SKIPPED--");
        }
        break;
      case KeyEvent.VK_F9:
        cycleVolume();
        String message = "Volume changed to ";
        switch(SOUND_EFFECT.volume) {
          case MUTE:
            message += "MUTED";
            soundEnabled = false;
            break;
          default:
            message += SOUND_EFFECT.volume.toString();
            soundEnabled = true;
            break;
        }
        Engine.setStatusMessage(message);
        break;
      case KeyEvent.VK_F10: showControls = !showControls;
        Engine.setStatusMessage((showControls) ? "--CONTROLS ON--" : "--CONTROLS OFF--");
        break;
    }
    
    switch(gameMode) { // controls are based on the game mode
      case TALKING:
        switch (key) {
          case KeyEvent.VK_P:
            pauseGame();
            break;
          case KeyEvent.VK_ENTER:
            skipSentence();
            break;
          default: break;
        }
        break;
      case DYING:
      case RUNNING:
        if(hero.getLives() > 0) {
          switch (key) {
            case KeyEvent.VK_F:
              pendingInteraction = true;
              break;
            case KeyEvent.VK_SHIFT:
              hero.toggleSprint();
              break;
            case KeyEvent.VK_P: // pause/unpause
              pauseGame();
              break;
            case KeyEvent.VK_W: // climb up
              if(hero.canClimb()) {
                hero.setClimbing(true);
                hero.setSpeedY(5);
              }
              break;
            case KeyEvent.VK_S: // climb down
              if(hero.canClimb()) {
                hero.setClimbing(true);
                hero.setSpeedY(-5);
              }
              break;
            case KeyEvent.VK_A: // move left
              if(rightPressed) {
                hero.setSpeedX(0);
              } else {
                hero.increaseSpeed(-Movable.MAX_SPEED_X, 0);
              }
              if(hero.isClimbing()) {
                hero.setClimbing(false);
                hero.setSpeedY(-1);
              }
              leftPressed = true;
              break;
            case KeyEvent.VK_D: // move right
              if(leftPressed) {
                hero.setSpeedX(0);
              } else {
                hero.increaseSpeed(Movable.MAX_SPEED_X, 0);
              }
              if(hero.isClimbing()) {
                hero.setClimbing(false);
                hero.setSpeedY(-1);
              }
              rightPressed = true;
              break;
            case KeyEvent.VK_SPACE: // jump
              if(hero.canJump()) {
                hero.doJump();
              } else {
                if(hero.canDoubleJump()) {
                  hero.doDoubleJump();
                }
              }
              break;
            default: break;
          }
        }
        break; // END RUNNING
      case PAUSED: // then we are paused, so change keyboard options
        switch (key) {
          case KeyEvent.VK_W: pauseMenuSelection = pauseMenuSelection.prev(); // scroll upward through menu
            break;
          case KeyEvent.VK_S: pauseMenuSelection = pauseMenuSelection.next(); // scroll downward through menu
            break;
          case KeyEvent.VK_ENTER:
            doMenuSelection();
            break;
          case KeyEvent.VK_P: // pause/unpause
            gameMode = GAME_MODE.RUNNING;
            break;
          default: break;
        }
        break; // END PAUSED
      case START_MENU:
        switch(key) {
          case KeyEvent.VK_W: startMenuSelection = startMenuSelection.prev(); // scroll upward through menu
            break;
          case KeyEvent.VK_S: startMenuSelection = startMenuSelection.next(); // scroll downward through menu
            break;
          case KeyEvent.VK_ENTER: doMenuSelection();
            break;
          default: break;
        }
        break; // END START_MENU
      case WIN:
        if(key == KeyEvent.VK_ENTER) gameMode = GAME_MODE.CREDITS;
        break;
      case CREDITS:
      case GAME_OVER:
        if(key == KeyEvent.VK_ENTER) gameMode = GAME_MODE.START_MENU;
        break;
      default: break;
    }
    display.repaint();  // Causes the display() function to be called.
  }

  /**
   * Called when the user types a character.  This function is called in
   * addition to one or more calls to keyPressed and keyTyped. Note that ch is an
   * actual character such as 'A' or '@'.
   * @param e
   */
  @Override
  public void keyTyped(KeyEvent e) { 
    char ch = e.getKeyChar();  // Which character was typed.
    // TODO:  Add code to respond to the character being typed.
    display.repaint();  // Causes the display() function to be called.
  }

  /**
   * Called when the user releases any key.
   * @param e
   */
  @Override
  public void keyReleased(KeyEvent e) { 
    int key = e.getKeyCode();  // Tells which key was pressed.
    switch(gameMode) { // controls are based on the game mode
      case RUNNING:
        switch (key) {
          case KeyEvent.VK_W: // stop climbimg
            hero.setSpeedY(0);
            break;
          case KeyEvent.VK_S: // stop climbimg
            hero.setSpeedY(0);
            break;
          case KeyEvent.VK_A: // stop moving left
            if(rightPressed) {
              hero.increaseSpeed(Movable.MAX_SPEED_X, 0);
            } else {
              hero.setSpeedX(0);
            }
            leftPressed = false;
            break;
          case KeyEvent.VK_D: // stop moving right
            if(leftPressed) {
              hero.increaseSpeed(-Movable.MAX_SPEED_X, 0);
            } else {
              hero.setSpeedX(0);
            }
            rightPressed = false;
            break;
          default: break;
        }
        break;
      default: break;
    }
  }

  // --------------------------- animation support ---------------------------
  /* You can call startAnimation() to run an animation.  A frame will be drawn every
   * 30 milliseconds (can be changed in the call to glutTimerFunc.  The global frameNumber
   * variable will be incremented for each frame.  Call pauseAnimation() to stop animating.
   */

  private boolean animating;  // True if animation is running.  Do not set directly.
                              // This is set by startAnimation() and pauseAnimation().

  /**
   * Performs all per frame updates, to include collision detection and moving objects.
   */
  private void updateFrame() {
    frameNumber++;
    
    switch(gameMode) {
      case WARPING:
        scene.transZ -= scene.LEVEL_DEPTH;
        hero.setDefaultPosition(hero.getX(), hero.getY()); // continue from this point if die
        gameMode = GAME_MODE.SLOW_MO;
        break;
      case SLOW_MO:
        transitionToNextLevel();
        break;
      case DYING: 
        if(!hero.wasRecentlyDamaged()) {
          gameMode = GAME_MODE.GAME_OVER;
        }
        break;
      case RUNNING:
        if(checkForWin()) return;
        processPendingInteraction();
        PhysicsEngine.fall(); // apply gravity to all heavy objects
        PhysicsEngine.drag(); // apply drag to all necessary objects

        ArrayList<Collidable> visibleObjects = GAME.getVisibles();
        ArrayList<Integer> toRemove = new ArrayList<>(); // keep track of ids to remove at end of frame

        // for all objects
        visibleObjects.stream().forEach((c) -> {
          if(c != null) {
            if(c instanceof Movable) { ((Movable)c).move(); } // move all movables
            
            // process all collisions
            List<Integer> collisionIds = c.processCollisions(visibleObjects);
            if(collisionIds != null) toRemove.addAll(collisionIds);
            
            if(c instanceof Boss) {
              if(!((Boss)c).didRecentlyFire()) qProjectiles.offer(new NextProjectile(hero.getPosition(), true)); // fire boss weapon toward hero
            }
          }
        });

        // check for warp collision, then remove all ids tagged for removal
        if(toRemove.contains(ID.WARP)) jumpToNextLevel();
        toRemove.stream().forEach((id) -> { GAME.removeAny(id); });
        
        if(hero.getLives() == 0) { gameMode = GAME_MODE.DYING; } // check for game over
        break;
      default: break;
    }
  }

  /**
   * Initiates a 30 millisecond timer for tracking frames.
   */
  public void startAnimation() {
    if (!animating ) {
      if (animationTimer == null) {
        animationTimer = new Timer(30, this);
      }
      animationTimer.start();
      animating = true;
    }
  }

  /**
   * Stops frame timer.
   */
  public void pauseAnimation() {
    if (animating) {
      animationTimer.stop();
      animating = false;
    }
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    updateFrame();
    display.repaint();
  }

  // ---------------------- support for mouse events ----------------------
  private boolean dragging;    // is a drag operation in progress?
  private int startX, startY;  // starting location of mouse during drag
  private int prevX, prevY;    // previous location of mouse during drag

  /**
   * Called when the user presses a mouse button on the display.
   * @param evt
   */
  @Override
  public void mousePressed(MouseEvent evt) {
    int key = evt.getButton();
    Point sc = evt.getPoint(); // clicked location, to convert to world coords
          
    if (dragging) { return; }
    int x = evt.getX();  // mouse location in pixel coordinates.
    int y = evt.getY();
    dragging = true;  // might not always be correct!
    prevX = startX = x;
    prevY = startY = y;
    
    // handle clicks subject to gameMode
    switch(gameMode) { // controls are based on the game mode
      case RUNNING:
        switch(key) {
          case MouseEvent.BUTTON1: // left click
            qProjectiles.offer(new NextProjectile(sc, true)); // add the projectile to the queue, to be fired during next update
            break;
          case MouseEvent.BUTTON3: // right click
            qProjectiles.offer(new NextProjectile(sc, false)); // add the projectile to the queue, to be fired during next update
            break;
          default: break;
        }
        break;
      default: break;
    }
    
    display.repaint();    //  only needed if display should change
  }

  /**
   * Called when the user releases a mouse button after pressing it on the display.
   * @param evt
   */
  @Override
  public void mouseReleased(MouseEvent evt) {
    if (!dragging) {
      return;
    }
    dragging = false;
    // TODO:  finish drag (generally nothing to do here)
  }

  /**
   * Called during a drag operation when the user drags the mouse on the display/
   * @param evt
   */
  @Override
  public void mouseDragged(MouseEvent evt) {
    if (!dragging) {
      return;
    }
    int x = evt.getX();  // mouse location in pixel coordinates.
    int y = evt.getY();
    // TODO:  respond to mouse drag to new point (x,y)
    prevX = x;
    prevY = y;
    display.repaint();
  }

  // Other methods required for MouseListener, MouseMotionListener.
  @Override
  public void mouseMoved(MouseEvent evt) {
    switch(gameMode) {
      case START_MENU:
        if(evt.getY() > display.getHeight()/2) this.startMenuSelection = START_MENU_OPTION.EXIT;
        else this.startMenuSelection = START_MENU_OPTION.START_GAME;
        break;
      case RUNNING:
        currentMousePos = evt.getPoint();
        break;
      default: break;
    }
  }

  /**
   * Detects whether the current mouse position is on top of an interactive object.  If so, will
   * update interactiveObject to point to it.  If none found, will deselect all interactive objects.
   */
  public void detectInteractiveObject() {
    interactiveObject = GAME.getInteractiveAt(currentMousePos);
    if(interactiveObject != null) {
      interactiveObject.select();
      GAME.deselectAllIOBut(interactiveObject);
    } else {
      GAME.deselectAllIO();
    }
  }
  
  @Override
  public void mouseClicked(MouseEvent evt) {
    int key = evt.getButton();
    
    switch(gameMode) { // controls are based on the game mode
      case START_MENU:
        switch (key) {
          case MouseEvent.BUTTON1: doMenuSelection(); // pick selection
            break;
          case MouseEvent.BUTTON3: // right click to select next option
            this.startMenuSelection = startMenuSelection.next(); // scroll downward through menu
            break;
          default: break;
        }
        break; // END START_MENU
      case WIN:
        if(key == MouseEvent.BUTTON1) gameMode = GAME_MODE.CREDITS;
        break;
      case CREDITS:
      case GAME_OVER:
        switch(key) {
          case MouseEvent.BUTTON1:
            gameMode = GAME_MODE.START_MENU;
          default: break;
        }
      default: break;
    }
  }
  @Override public void mouseEntered(MouseEvent evt) { 
    display.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
  }
  @Override public void mouseExited(MouseEvent evt) { 
    display.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
  
  /**
   * Game will automatically display this message at the bottom of the screen for x seconds.
   * @param message 
   */
  public static void setStatusMessage(String message) {
    TestDisplay.addTestData(message);
  }
  
  /**
   * Adds a conversation, each string will be displayed for 5 seconds.
   * @param convo 
   */
  public static void setConversation(String[] convo) {
    LinkedList<String> reverse = new LinkedList<>();
    for(String s : convo)
      reverse.push(s);
    while(!reverse.isEmpty())
      CONVERSATION.push(reverse.pop());
    MESSAGE_TIMER.restart();
    gameMode = GAME_MODE.TALKING;
  }
  
  /**
   * Process a projectile than has been added to the queue.
   */
  public void fireProjectiles() {
    if(!qProjectiles.isEmpty()) {
      NextProjectile np;
      try {
        np = qProjectiles.take();
        Projectile fired;
        if(!np.isFromEnemy) {
          Point.Float wc = DrawLib.screenToWorld(np.screenCoord);
          //if(debugging) Engine.setConversation(new String[] { wc.x + ", " + wc.y });
          if(np.isFromPrimaryWeapon)
            fired = hero.firePrimaryWeapon(wc);
          else
            fired = hero.fireSecondaryWeapon(wc);
        } else {
          Collidable c = GAME.getGO(ID.CALAMITY);
          if(c != null) {
            Boss b = (Boss)c;
            fired = b.firePrimaryWeapon(np.worldCoord);
          } else {
            fired = null;
          }
        }

        if(fired != null) GAME.addTO(fired.getObjectId(), fired);
        else System.out.println("Attempted to fire null projectile.");
      } catch (InterruptedException ex) {
        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  /**
   * Draws the scrolling log to the top left corner of the scene.
   * @param gl 
   */
  private void drawLog(GL2 gl) {
    if(gameMode == GAME_MODE.RUNNING || gameMode == GAME_MODE.TALKING
            || gameMode == GAME_MODE.DYING || gameMode == GAME_MODE.WARPING)
    TestDisplay.writeToScreen(gl, DrawLib.getTexture(TEX.HUD).getWidth());
  }
  
  /**
   * Cycles through low, medium, high, and mute volume.
   */
  public static void cycleVolume() {
    SOUND_EFFECT.volume = SOUND_EFFECT.volume.up();
    if(SOUND_EFFECT.volume == SOUND_EFFECT.Volume.MUTE) SOUND_EFFECT.THEME.stop();
    else { SOUND_EFFECT.THEME.playLoop(); }
  }
}
