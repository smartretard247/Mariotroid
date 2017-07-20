package Main;

import Drawing.Scene;
import Drawing.DrawLib;
import Enumerations.START_MENU_OPTION;
import Enumerations.GAME_MODE;
import Enumerations.ID;
import Enumerations.SOUND_EFFECT;
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
  private final GLJPanel display;
  private final Dimension windowDim = new Dimension(1280,720);
  private Timer animationTimer;
  private static Timer introTimer;
  private static final Timer messageTimer = new Timer(5000, null);
  private static int frameNumber = 0; // The current frame number for an animation.
  private DrawLib drawLib;
  private static GAME_MODE gameMode = GAME_MODE.INTRO;
  private START_MENU_OPTION startMenuSelection = START_MENU_OPTION.START_GAME;
  private final int INTROLENGTHMS = 6000;
  private boolean won = false;
  private boolean warping = false;
  private boolean showControls = true;
  private boolean showDecor = true;
  private boolean swapBackground = false;
  private static boolean soundEnabled = true;
  private final TestDisplay testDisplay = new TestDisplay();
  private final Scene scene = new Scene(-600, -600, 0, 0.5, 0.5, 1.0);; // trans x & y & z, scale x & y & z
  private final PhysicsEngine phy = new PhysicsEngine();
  private final ObjectContainer game = new ObjectContainer();
  private static String statusMessage = "";
  private final LinkedBlockingQueue<NextProjectile> qProjectiles = new LinkedBlockingQueue<>();
  private boolean slowMo = false;
  private final int TOTAL_LEVELS = 2;
  private int currLevel = 1;
  private static boolean debugging = false;
  private boolean jumpedLastFrame = false;
  private Hero hero;
  
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
        TestDisplay.writeToFile();
        window.dispose();
        System.exit(0);
      }
    });
    introTimer.start();
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

    // start the animation
    startAnimation(); // also control pause function (and remove keyboard response)
    
    introTimer = new Timer(INTROLENGTHMS, (evt)-> {
      gameMode = GAME_MODE.START_MENU;
    });
    introTimer.setRepeats(false);
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
    messageTimer.setRepeats(false);
    SOUND_EFFECT.init(); // uncommment once all wav's in enum SOUND_EFFECT have been added to dir /res/sound
  
    // initial hero settings
    hero = new Hero(ID.ID_HERO, 3, 10, 0, DrawLib.TEX_HERO, 300, 400); // objId, 3 lives, 10 health, 0 score, texId, x, y
    game.addGO(hero);
  }
  
  private void setupVisibles(int level) {
    PhysicsEngine.resetGravity();
    game.clearGOs(); // will not clear the hero
    Hero h = (Hero)game.getGO(ID.ID_HERO);
    switch(level) {
    case 1:// only add level 1 visible objects to this map
      currLevel = 1; // no need to adjust level number on any further cases
      scene.resetAll(); // do not change scene in other cases
      h.setDefaultPosition(10300, 400);
      h.resetAll();
      game.addGO(new Collidable(ID.ID_JETPACK, DrawLib.TEX_JETPACK, 1400, 350));
      game.addGO(new Collidable(ID.ID_SHELL, DrawLib.TEX_SHELL, 300, 800));
      game.addGO(new Collidable(ID.ID_ARMOR, DrawLib.TEX_ARMOR, 5660, 198));
      game.addGO(new Enemy(ID.ID_ENEMY_1, 1, 1, DrawLib.TEX_ENEMY_BASIC, 2000, 800, new Point.Double(-5,0))); // objId, 1 life, 1 health, texId, x, y, sx/sy
      game.addGO(new Enemy(ID.ID_ENEMY_2, 1, 1, DrawLib.TEX_ENEMY_BASIC, 4000, 800, new Point.Double(5,0)));
      game.addGO(new Enemy(ID.ID_ENEMY_3, 1, 1, DrawLib.TEX_ENEMY_BASIC, 8075, 240, new Point.Double(5,0)));
      game.addGO(new Boss(ID.ID_CALAMITY, 1, 20, DrawLib.TEX_CALAMITY, 11000, 500, new Point.Double(10,10), 500));
      game.addGO(new Door(ID.ID_DOOR, 11100, 189, 175, 0));
      break;
    case 2:// setup level 2, only add level 2 game objects to this map
      game.addGO(new Enemy(ID.ID_ENEMY_1, 1, 1, DrawLib.TEX_ENEMY_BASIC, 10000, 950, new Point.Double(5,0)));
      game.addGO(new Enemy(ID.ID_ENEMY_2, 1, 1, DrawLib.TEX_ENEMY_BASIC, 4000, 950, new Point.Double(5,0)));
      game.addGO(new Enemy(ID.ID_ENEMY_3, 1, 1, DrawLib.TEX_ENEMY_BASIC, 8075, 950, new Point.Double(5,0)));
      game.addGO(new Boss(ID.ID_CALAMITY, 1, 20, DrawLib.TEX_CALAMITY, 300, 575, new Point.Double(10,10), 750));
      Boss b = (Boss)game.getGO(ID.ID_CALAMITY);
      b.setMinX(0);
      b.setMaxX(3000); // test this number
      game.addGO(new Door(ID.ID_DOOR, 300, 967, 0, 80, true));
      game.addGO(new Collidable(ID.ID_SWITCH, DrawLib.TEX_SWITCH_ON, 5366, 708));
      break;
    default: System.out.println("Unknown level number while resetting visibles."); break;
    }
  }
  
  public void jumpToLevel(int num) {
    switch(num) {
      case 1: loadLevel(1, "/res/layer_collision_1.png"); break;
      case 2: loadLevel(2, "/res/layer_collision_2.png"); break;
      default: System.out.println("Unknown level number while jumping to level."); break;
    }
  }
  
  /**
   * Loads all black rectangles in the supplied PNG as collision boundaries for the level.  Will
   * remove all collision boundaries from previous level if found.
   * @param levelNum
   * @param fileName 
   */
  private void loadLevel(int levelNum, String fileName) {
    setupVisibles(levelNum);
    LevelBuilder levelBuilder = new LevelBuilder(fileName);
    ArrayList<Rectangle> level = levelBuilder.scanForBoundaries();
    level.stream().forEach((r) -> {
      int id = ID.getNewId();
      game.addTO(id, new Collidable(id, DrawLib.TEX_LEVEL,
              r.x(), r.y(), r.w(), r.h()));
    });
  }
  
  /* 
   * Draws the scene, for each given game mode.
  */
  private void draw(GL2 gl) {
    gl.glPushMatrix(); // save initial transform
    gl.glTranslated(0, 0, -10); // push everything away by 10
    switch(gameMode) {
      case INTRO: drawIntro(gl); break; // END INTRO
      case START_MENU: drawStartMenu(gl); break; // END START MENU
      case DYING:
      case RUNNING: drawNormalGamePlay(gl); break; // END RUNNING
      case PAUSED: drawPauseMenu(gl); break; // END PAUSED
      case GAME_OVER: drawGameOver(gl); break;
      case CREDITS: drawCredits(gl); break;
      case WIN: drawWin(gl); break;
      default: break;
    }
    if(debugging) drawDebug(gl);
    gl.glPopMatrix(); // return to initial transform
  }
  
  /**
   * Adjusts the scene with the hero's movement.  Will adjust a background scene when forBackground
   * is set to true.
   * @param gl 
   * @param forBackgroundScene 
   */
  public void adjustScene(GL2 gl, boolean forBackgroundScene) {
    if(!forBackgroundScene) {
      if(hero.getX() > 600 && hero.getX() < 10500)
        gl.glTranslated(-hero.getX(), scene.transY, scene.transZ);
      else if(hero.getX() <= 600)
        gl.glTranslated(scene.transX, scene.transY, scene.transZ);
      else if(hero.getX() >= 10500)
        gl.glTranslated(-10500, scene.transY, scene.transZ);
    } else {
      if(hero.getX() <= 600)
        gl.glTranslated(-scene.transX*10, -scene.transY*3, scene.globalZ-scene.LEVEL_DEPTH*currLevel);
      else 
        gl.glTranslated(-scene.transX*10+hero.getX()/10, -scene.transY*3, scene.globalZ-scene.LEVEL_DEPTH*currLevel);
    }
  }
  
  /**
   * This is the standard loop for the game, showing level, character, enemies, etc.
   * @param gl 
   */
  private void drawNormalGamePlay(GL2 gl) {
    if(jumpedLastFrame) {
      //drawLib.loadTexture(DrawLib.TEX_LEVEL_DECOR_1, "/res/layer_decor_1.png");// load next level (3+) for background
      jumpedLastFrame = false;
    }
    
    drawBackground(gl);
    gl.glPushMatrix(); // save initial transform
    gl.glScaled(scene.scaleX, scene.scaleY, scene.scaleZ); // set global scale
    gl.glTranslated(0, 0, scene.globalZ); // global z should decrease by 40 after each zoom
    adjustScene(gl, false);
    drawLevel(gl);
    drawHero(gl);
    fireProjectiles(); // fire projectile from the queue
    drawForeground(gl);
    gl.glPopMatrix(); // return to initial transform
    drawHud(gl);
    if(showControls) drawControls(gl);
    drawStatus(gl); // will only draw status' of new messages, for x seconds
  }
  
  private void drawStatus(GL2 gl) {
    // check if we need to display a message
    if(messageTimer.isRunning()) {
      gl.glPushMatrix();
      gl.glColor3dv(new double[] { 1.0, 1.0, 0.0 }, 0);
      gl.glTranslated(0, DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2-60, 0);
      DrawLib.drawText(statusMessage, -60, 0);
      gl.glPopMatrix();
    } else {
      statusMessage = "";
    }
  }
  
  private void drawGameOver(GL2 gl) {
    gl.glPushMatrix();
    gl.glColor3dv(new double[] { 1.0, 0.0, 0.0 }, 0);
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2, 0, 0);
    DrawLib.drawText("GAME OVER", 100+(frameNumber%500*2), 0);
    gl.glPopMatrix();
  }
  
  private void drawControls(GL2 gl) {
      double yOffset = 10;
      String[] keyboardControls = { "Keyboard:", "A - Move left", "D - Move right", "W - Climb wall", "S - Descend wall", "SHIFT - Toggle run", "SPACE - Jump/Double jump", "P - Pause" };
      String[] mouseControls = { "Mouse:", "Left click - Fire primary weapon", "Right click - Fire alternate weapon", (debugging) ? "Middle click - God mode" : "" };
      gl.glPushMatrix();
      gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2, -DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2+yOffset, 0);
      gl.glPushMatrix();
      gl.glColor3dv(new double[] { 1.0, 1.0, 1.0 }, 0);
      for(String s : keyboardControls) {
        DrawLib.drawText(s, 0, 0);
        gl.glTranslated(s.length()*11,0,0);
      }
      gl.glPopMatrix();
      gl.glColor3dv(new double[] { 1.0, 1.0, 1.0 }, 0);
      gl.glTranslated(0,20,0);
      for(String s : mouseControls) {
        DrawLib.drawText(s, 0, 0);
        gl.glTranslated(s.length()*14,0,0);
      }
      gl.glPopMatrix();
  }
  
  private void drawCredits(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2, 0, 0);
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

    gl.glColor3dv( new double[] { 0.0, 1.0, 0.0 }, 0);
    for(int i = 0; i < credits.size(); i++)
      DrawLib.drawText(credits.get(i), windowDim.width/2-60, -windowDim.height/2+(frameNumber%500*2)-(i*20));
    gl.glPopMatrix();
  }
  
  private void drawWin(GL2 gl) {
    gl.glPushMatrix();
    gl.glColor3dv( new double[] { Math.random(), Math.random(), Math.random() }, 0);
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2, 0, 0);
    DrawLib.drawText("YOU WIN!", 100+(frameNumber%500*2), 0);
    gl.glPopMatrix();
  }
  
  /**
   * Draws the background once per frame.
   * @param gl 
   */
  private void drawBackground(GL2 gl) {
    gl.glPushMatrix();
    gl.glScaled(1.1, 1.1, 1);
    if(!swapBackground) {
      DrawLib.drawTexturedRectangle(DrawLib.TEX_BACKGROUND_1, DrawLib.getTexture(DrawLib.TEX_HUD).getWidth(), DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()); // background level
    } else {
      DrawLib.drawTexturedRectangle(DrawLib.TEX_BACKGROUND_2, DrawLib.getTexture(DrawLib.TEX_HUD).getWidth(), DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()); // secondary background level
    }
    gl.glPopMatrix();
  }
  
  private void drawLevel(GL2 gl) {
    gl.glPushMatrix();
    adjustScene(gl, true);
    if(currLevel < TOTAL_LEVELS)
      DrawLib.drawTexturedRectangle(DrawLib.TEX_LEVEL_DECOR_1+(currLevel%2)); // draw the background level
    //else DrawLib.drawTexturedRectangle(DrawLib.TEX_LEVEL_DECOR_1); // use DECOR_1 as default background when no more levels
    gl.glPopMatrix();
    game.getVisibles().forEach((c) -> { c.draw(); }); // draw game objects
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
    gl.glTranslated(DrawLib.getTexture(DrawLib.TEX_LEVEL_DECOR_1).getWidth()/2, DrawLib.getTexture(DrawLib.TEX_LEVEL_DECOR_1).getHeight()/2, 0); // levels MUST be same size as DECOR_1
    if(showDecor) {
      if(currLevel <= TOTAL_LEVELS) DrawLib.drawTexturedRectangle(DrawLib.TEX_LEVEL_DECOR_1+Math.abs(currLevel%2-1));
    }
    gl.glPopMatrix();
  }
  
  private void drawIntro(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(0, 0, 0);
    DrawLib.drawTexturedRectangle(DrawLib.TEX_LOGO);
    gl.glPopMatrix();
  }

  private void drawStartMenu(GL2 gl) {
    int screenWidth = DrawLib.getTexture(DrawLib.TEX_HUD).getWidth();
    int screenHeight = DrawLib.getTexture(DrawLib.TEX_HUD).getHeight();
    double[] selectedTextColor = new double[] { 1.0, 0.0, 0.0 };
    double[] textColor = new double[] { 0.0, 0.0, 0.0 };
    switch(this.startMenuSelection) {
      case START_GAME:
        gl.glPushMatrix();
        gl.glTranslated(0, 50, 0);
        gl.glColor3dv( selectedTextColor, 0);
        DrawLib.drawText("START GAME", -50, 0);
        //DrawLib.drawCustomText("START GAME");
        gl.glTranslated(0, -100, 0);
        gl.glColor3dv( textColor, 0);
        DrawLib.drawText("EXIT", -20, 0);
        //DrawLib.drawCustomText("EXIT");
        gl.glPopMatrix();
        break;
      case EXIT:
        gl.glPushMatrix();
        gl.glTranslated(0, 50, 0);
        gl.glColor3dv( textColor, 0);
        DrawLib.drawText("START GAME", -50, 0);
        gl.glTranslated(0, -100, 0);
        gl.glColor3dv( selectedTextColor, 0);
        DrawLib.drawText("EXIT", -20, 0);
        gl.glPopMatrix();
        break;
      default: break;
    }
  }

  private void drawPauseMenu(GL2 gl) {
    double[] textColor = new double[] { 1.0, 1.0, 0.0 };
    gl.glColor3dv( textColor, 0);
    DrawLib.drawText("GAME PAUSED", -60, 0);
  }

  private void doStartMenuSelection() {
    switch(this.startMenuSelection) {
      case START_GAME: gameMode = GAME_MODE.RUNNING;
        SOUND_EFFECT.THEME.playLoop();
        won = false;
        jumpToLevel(1);
        break;
      case EXIT: System.exit(0);
        break;
      default: break;
    }
  }

  private void drawHud(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(0, 0, 0);
    DrawLib.drawTexturedRectangle(DrawLib.TEX_HUD);
    gl.glPopMatrix();
    drawHealth(gl);
    drawLives(gl);
    drawScore(gl);
    drawAmmoCount(gl);
  }
  
  private void drawAmmoCount(GL2 gl) {
    if(hero.hasSecondaryWeapon()) {
      double xDiff = 10;
      double yDiff = -35;
      double[] textColor = new double[] { 1.0, 1.0, 1.0 };
      double hudWidth = DrawLib.getTexture(DrawLib.TEX_HUD).getWidth();
      double hudHeight = DrawLib.getTexture(DrawLib.TEX_HUD).getHeight();
      double healthHeight = DrawLib.getTexture(DrawLib.TEX_HEALTH).getHeight();
      double shellWidth = DrawLib.getTexture(DrawLib.TEX_SHELL).getWidth()*2;
      double shellHeight = DrawLib.getTexture(DrawLib.TEX_SHELL).getHeight()*2;
      
      gl.glPushMatrix();
      gl.glColor3dv( textColor, 0);
      gl.glTranslated(-hudWidth/2+shellWidth/2+xDiff, hudHeight/2 - healthHeight - shellHeight + yDiff, 0);
      DrawLib.drawTexturedRectangle(DrawLib.TEX_SHELL, shellWidth, shellHeight);
      DrawLib.drawText(Integer.toString(hero.getAmmoCount()), 25, -6);
      gl.glPopMatrix();
    }
  }

  private void drawHealth(GL2 gl) { // draw health in top left corner
    double xDiff = 10;
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2+DrawLib.getTexture(DrawLib.TEX_HEALTH).getWidth()+xDiff,
            DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2-DrawLib.getTexture(DrawLib.TEX_HEALTH).getHeight(), 0);
    for(int i = 0; i < hero.getHealth(); i++) {
      DrawLib.drawTexturedRectangle(DrawLib.TEX_HEALTH);
      gl.glTranslated(DrawLib.getTexture(DrawLib.TEX_HEALTH).getWidth(), 0, 0);
    }
    gl.glPopMatrix();
  }
  
  private void drawLives(GL2 gl) {
    double[] textColor = new double[] { 1.0, 1.0, 1.0 };
    gl.glColor3d(textColor[0], textColor[1], textColor[2]);
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2,
            DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2, 0);
    DrawLib.drawText(Integer.toString(hero.getLives()), 40, -50);
    gl.glPopMatrix();
  }

  private void drawScore(GL2 gl) { // draw score in top right corner
    double diffX = 30;
    double[] textColor = new double[] { 1.0, 1.0, 1.0 };
    gl.glColor3d(textColor[0], textColor[1], textColor[2]);
    gl.glPushMatrix();
    gl.glTranslated(DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2-diffX,
            DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2, 0);
    String text = "SCORE: " + Long.toString(hero.getScore());
    //DrawLib.drawText(text, new Point(DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()-120, DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()-20));
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
    case KeyEvent.VK_F5: debugging = !debugging;
      setStatusMessage((debugging) ? "--DEBUGGING ON--" : "--DEBUGGING OFF--");
      if(!debugging) hero.setGodMode(false);
      break;
    case KeyEvent.VK_F6: showControls = !showControls;
      setStatusMessage((showControls) ? "--CONTROLS ON--" : "--CONTROLS OFF--");
      break;
    case KeyEvent.VK_F7: if(debugging) {
        swapBackground = !swapBackground;
        setStatusMessage((!swapBackground) ? "--BACKGROUND 1--" : "--BACKGROUND 2--");
      }
      break;
    case KeyEvent.VK_F8: if(debugging) { 
        showDecor = !showDecor;
        setStatusMessage((showDecor) ? "--DECOR ON--" : "--DECOR OFF--");
      }
      break;
    case KeyEvent.VK_F9:
        cycleVolume();
        String message = "VOLUME: ";
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
        setStatusMessage(message);
      break;
    }
    
    switch(gameMode) { // controls are based on the game mode
    case DYING:
    case RUNNING:
      if(hero.getLives() > 0) {
        switch (key) {
        case KeyEvent.VK_SHIFT:
          hero.toggleSprint();
          break;
        case KeyEvent.VK_P: // pause/unpause
          gameMode = GAME_MODE.PAUSED;
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
          hero.increaseSpeed(-GameObject.MAX_SPEED_X, 0);
          if(hero.isClimbing()) {
            hero.setClimbing(false);
            hero.setSpeedY(-1);
          }
          break;
        case KeyEvent.VK_D: // move right
          hero.increaseSpeed(GameObject.MAX_SPEED_X, 0);
          if(hero.isClimbing()) {
            hero.setClimbing(false);
            hero.setSpeedY(-1);
          }
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
      case KeyEvent.VK_P: // pause/unpause
        gameMode = GAME_MODE.RUNNING;
        break;
      default: break;
      }
      break; // END PAUSED
    case START_MENU:
      switch(key) {
      case KeyEvent.VK_W: this.startMenuSelection = startMenuSelection.prev(); // scroll upward through menu
        break;
      case KeyEvent.VK_S: this.startMenuSelection = startMenuSelection.next(); // scroll downward through menu
        break;
      case KeyEvent.VK_ENTER: doStartMenuSelection();
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
        hero.setSpeedX(0);
        break;
      case KeyEvent.VK_D: // stop moving right
        hero.setSpeedX(0);
        break;
      case KeyEvent.VK_SPACE: // stop jump, start fall
        //++hero.fallCount;
        //if(!hero.didLand() && hero.fallCount <= 2) hero.setSpeedY(-PhysicsEngine.GRAVITY);
        //if(!hero.didLand()) hero.setSpeedY(-PhysicsEngine.GRAVITY);
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

  private void updateFrame() {
    frameNumber++;
    
    switch(gameMode) {
    case WARPING:
      if(warping) {
        scene.transZ -= scene.LEVEL_DEPTH;
        hero.setDefaultPosition(hero.getX(), hero.getY()); // continue from this point if die
        warping = false;
      } else {
        slowMo = true;
        gameMode = GAME_MODE.RUNNING;
      }
      break;
    case DYING: if(!hero.wasRecentlyDamaged()) { gameMode = GAME_MODE.GAME_OVER; }
    case RUNNING:
      if(won) {
        if(Engine.isSoundEnabled()) {
          SOUND_EFFECT.THEME.stop();
          SOUND_EFFECT.WIN.play(Math.max(SOUND_EFFECT.getGain(), 6f));
        }
        gameMode = GAME_MODE.WIN;
        return;
      } // check for winning conditions
      
      // this is for slow mo jumping to next level
      if(slowMo) { 
        ++scene.globalZ;
        if(scene.globalZ % scene.LEVEL_DEPTH == 0) slowMo = false;
      }
      
      ArrayList<Collidable> visibleObjects = game.getVisibles();
      ArrayList<Integer> toRemove = new ArrayList<>(); // keep track of ids to remove at end of frame
      ArrayList<Projectile> projectiles = new ArrayList<>();
      
      // for all objects
      visibleObjects.stream().forEach((c) -> {
        // move all movables
        if((new Movable()).getClass().isInstance(c)) {
          Movable m = (Movable)c;
          m.move();
        }
        // remove projectiles that collide with the level
        if((new Projectile()).getClass().isInstance(c)) {
          //projectiles.add((Projectile)c); // track visible projectiles
          Projectile p = (Projectile)c;
          List<Collidable> projectileCollisions = p.processCollisions(visibleObjects);
          projectileCollisions.stream().forEach((c1) -> {
            toRemove.add(p.getObjectId());
          });
        } else if((new Boss()).getClass().isInstance(c)) {
          Boss boss = (Boss)c;
          if(boss != null) {
            if(!boss.didRecentlyFire()) qProjectiles.offer(new NextProjectile(hero.getPosition(), true));
            List<Collidable> bossCollisions = boss.processCollisions(visibleObjects);
            bossCollisions.stream().forEach((c1) -> {
              if(new Projectile().getClass().isInstance(c1)) {
                toRemove.add(c1.getObjectId());
                if(boss.getHealth() <= 0) { // enemy died
                  toRemove.add(boss.getObjectId());
                  game.addTO(ID.getNewId(), new Collidable(ID.getLastId(), DrawLib.TEX_HEALTH_ORB, boss.getX(), boss.getY()));
                  hero.addScore(boss.getPointsWorth());
                  activateDoor(); // set warp point, and show powered door
                }
              }
            });
          }
        } else if((new Enemy()).getClass().isInstance(c)) {
          Enemy enemy = (Enemy)c;
          if(enemy != null) {
            List<Collidable> enemyCollisions = enemy.processCollisions(visibleObjects); // return valid collisions
            enemyCollisions.stream().forEach((c1) -> {
              if(new Projectile().getClass().isInstance(c1)) {
                toRemove.add(c1.getObjectId());
                if(enemy.getHealth() <= 0) { // enemy died
                  hero.addScore(enemy.getPointsWorth());
                  toRemove.add(enemy.getObjectId());
                }
              }
            });
          }
        }
      });
      
      // hero collision detection
      List<Collidable> heroCollisions = hero.processCollisions(visibleObjects);
      for(Collidable c : heroCollisions){
        int objId = c.getObjectId();
        int texId = c.getTextureId();
        switch(objId) {
        case ID.ID_JETPACK:
          hero.addScore(250);
          toRemove.add(objId); // remove the jetpack image from the screen
          break;
        case ID.ID_SHELL:
          hero.addScore(1000);
          toRemove.add(objId); // remove the shell image from the screen
          break;
        case ID.ID_ARMOR:
          Engine.setStatusMessage("Got armor!");
          hero.addScore(275);
          toRemove.add(objId); // remove the armor image from the screen
          break;
        case ID.ID_WARP:
          //removeWarp();
          if(++currLevel <= TOTAL_LEVELS){
            hero.setSpeedX(0);
            warping = true;
            gameMode = GAME_MODE.WARPING;
            hero.doJump();
            jumpToLevel(currLevel);
            jumpedLastFrame = true;
          }else{
            setStatusMessage("YOU WIN!!");
            won = true;
          }
          break;
        default: 
          switch(texId) {
            case DrawLib.TEX_SWITCH_ON:
              PhysicsEngine.inverseGravity();
              c.setTextureId(DrawLib.TEX_SWITCH_OFF);
              break;
            case DrawLib.TEX_ENEMY_WEAPON_1:
            case DrawLib.TEX_ENEMY_WEAPON_2:
              toRemove.add(objId);
              break;
            case DrawLib.TEX_HEALTH_ORB:
              toRemove.add(objId);
              break;
            default: break;
          }
          break;
        }
      }
      
      // remove all ids tagged for removal
      toRemove.stream().forEach((id) -> { game.removeAny(id); });
      break;
    default: break;
    }
  }
  
  /**
   * Removes the "closed" door and adds a "powered" door.  Also creates a collidable point at which
   * contact will change game mode to WARPING.
   * @param x
   * @param y 
   */
  private void activateDoor() {
    Door door = (Door)game.getGO(ID.ID_DOOR);
    door.activate();
    game.addGO(door.getWarp());
  }
  
  /*
  private void removeDoor() {
    game.removeAny(ID.ID_DOOR_POWERED);
    game.removeAny(ID.ID_WARP);
  }*/

  public void startAnimation() {
    if (!animating ) {
      if (animationTimer == null) {
        animationTimer = new Timer(30, this);
      }
      animationTimer.start();
      animating = true;
    }
  }

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
    
    switch(gameMode) { // controls are based on the game mode
    case RUNNING:
      switch(key) {
      case MouseEvent.BUTTON1: // left click
        qProjectiles.offer(new NextProjectile(sc, true)); // add the projectile to the queue, to be fired during next update
        break;
      case MouseEvent.BUTTON2: // middle click
        if(debugging) {
          hero.toggleGodMode();
          setStatusMessage((hero.getGodMode()) ? "--GOD MODE ON--" : "--GOD MODE OFF--");
        }
        break;
      case MouseEvent.BUTTON3: // right click
        qProjectiles.offer(new NextProjectile(sc, false)); // add the projectile to the queue, to be fired during next update
        break;
      default: break;
      }
      break;
    default: break;
    }
          
    if (dragging) {
      return;  // don't start a new drag while one is already in progress
    }
    int x = evt.getX();  // mouse location in pixel coordinates.
    int y = evt.getY();
    // TODO: respond to mouse click at (x,y)
    dragging = true;  // might not always be correct!
    prevX = startX = x;
    prevY = startY = y;
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
    Point.Double wc = DrawLib.screenToWorld(evt.getPoint());
    
    switch(gameMode) {
    case START_MENU:
      if(evt.getY() > display.getHeight()/2) this.startMenuSelection = START_MENU_OPTION.EXIT;
      else this.startMenuSelection = START_MENU_OPTION.START_GAME;
      break;
    default: break;
    }
  }    
  
  @Override
  public void mouseClicked(MouseEvent evt) {
    int key = evt.getButton();
    
    switch(gameMode) { // controls are based on the game mode
    case START_MENU:
      switch (key) {
      case MouseEvent.BUTTON1: doStartMenuSelection(); // pick selection
        break;
      case MouseEvent.BUTTON3: // right click to select next option
        this.startMenuSelection = startMenuSelection.next(); // scroll downward through menu
        break;
      default: break;
      }
      break; // END START_MENU
    case PAUSED: // then we are paused
      switch (key) {
      case MouseEvent.BUTTON1:
        gameMode = GAME_MODE.RUNNING;
        break;
      default: break;
      }
      break; // END PAUSED
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
    statusMessage = message;
    messageTimer.start();
  }
  
  public void fireProjectiles() {
    if(!qProjectiles.isEmpty()) {
      NextProjectile np;
      try {
        np = qProjectiles.take();
      
        Projectile fired;
        if(!np.isFromEnemy) {
          Point.Double wc = DrawLib.screenToWorld(np.screenCoord);
          if(debugging) setStatusMessage("(" + wc.x + ", " + wc.y + ")");
          if(np.isFromPrimaryWeapon)
            fired = hero.firePrimaryWeapon(wc);
          else
            fired = hero.fireSecondaryWeapon(wc);
        } else {
          Collidable c = game.getGO(ID.ID_CALAMITY);
          if(c != null) {
            Boss b = (Boss)c;
            fired = b.firePrimaryWeapon(np.worldCoord);
          } else {
            fired = null;
          }
        }

        if(fired != null) game.addTO(fired.getObjectId(), fired);
        else System.out.println("Attempted to fire null projectile.");
      } catch (InterruptedException ex) {
        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  private void drawDebug(GL2 gl) {
    TestDisplay.writeToScreen(gl, DrawLib.getTexture(DrawLib.TEX_HUD).getWidth());
  }
  
  public static void setGameMode(GAME_MODE mode) { gameMode = mode; }
  
  public static int getFrameNumber() { return frameNumber; }
  
  public static boolean isSoundEnabled() { return soundEnabled; }
  
  public static boolean isDebugging() { return debugging; }
  
  public static void cycleVolume() {
    SOUND_EFFECT.volume = SOUND_EFFECT.volume.up();
    if(SOUND_EFFECT.volume == SOUND_EFFECT.Volume.MUTE) SOUND_EFFECT.THEME.stop();
    else { SOUND_EFFECT.THEME.playLoop(); }
  }
}
