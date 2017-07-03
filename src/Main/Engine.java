package Main;

import Drawing.Scene;
import Drawing.DrawLib;
import Enumerations.START_MENU_OPTION;
import Enumerations.GAME_MODE;
import Enumerations.ID;
//import Test.Test;
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
  private static final Timer messageTimer = new Timer(5000, null);
  private int frameNumber = 0; // The current frame number for an animation.
  private DrawLib drawLib;
  public static GAME_MODE gameMode = GAME_MODE.INTRO;
  private START_MENU_OPTION startMenuSelection = START_MENU_OPTION.START_GAME;
  private final int INTROLENGTHMS = 400;
  private boolean won = false;
  
  public Scene scene; // trans x & y & z, scale x & y & z
  public Hero hero;
  public PhysicsEngine phy = new PhysicsEngine();
  //public final Map<Integer, Collidable> gameObjects = new HashMap<>();
  //public final Map<Integer, Collidable> visibleObjects = new HashMap<>();
  public ObjectContainer game = new ObjectContainer();
  private static String statusMessage = "";
  private final LinkedList<NextProjectile> qProjectiles = new LinkedList<>();
  
  ///// START METHODS

  public static void main(String[] args) {
    JFrame window = new JFrame("Mariotroid");
    Engine panel = new Engine();
    window.setContentPane(panel);
    window.pack();
    window.setLocation(10,10);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
    panel.setFocusable(false);
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
    
    Timer introTimer = new Timer(INTROLENGTHMS, (evt)-> {
      this.gameMode = GAME_MODE.START_MENU;
    });
    introTimer.setRepeats(false);
    introTimer.start();
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
    //gl.glOrtho(-windowDim.width/2, windowDim.width/2 ,-windowDim.height/2, windowDim.height/2, -9.9, 50);
    gl.glFrustum(-windowDim.width/2, windowDim.width/2 ,-windowDim.height/2, windowDim.height/2, 9.9, 50);
    
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glClearColor(0, 0.4f, 0.8f, 0);
    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    
    drawLib = new DrawLib(gl); // initialize the drawing library before dealing with any textures!!
    messageTimer.setRepeats(false);
    
    scene = new Scene(-600, -600, 0, 0.5, 0.5, 1.0); // initial scale and translations
    hero = new Hero(ID.ID_HERO, 3, 10, 0, DrawLib.TEX_HERO, 300, 400); // objId, 3 lives, 10 health, 0 score, texId, x, y
    
    // initialize all game objects here
    game.addGO(hero);
    game.addGO(new Collidable(ID.ID_JETPACK, DrawLib.TEX_JETPACK, 1400, 350));
    game.addGO(new Collidable(ID.ID_ALT_WEAPON, DrawLib.TEX_ALT_WEAPON, 300, 900));
    game.addGO(new Collidable(ID.ID_ARMOR, DrawLib.TEX_ARMOR, 5681, 198));
    game.addGO(new Enemy(ID.ID_ENEMY_1, 1, 1, DrawLib.TEX_ENEMY_BASIC, 2000, 800, new Point(-5,0))); // objId, 1 life, 1 health, texId, x, y, sx/sy
    game.addGO(new Enemy(ID.ID_ENEMY_2, 1, 1, DrawLib.TEX_ENEMY_BASIC, 4000, 800, new Point(5,0)));
    game.addGO(new Enemy(ID.ID_ENEMY_3, 1, 1, DrawLib.TEX_ENEMY_BASIC, 8075, 240, new Point(5,0)));
    game.addGO(new Boss(ID.ID_CALAMITY, 1, 20, DrawLib.TEX_CALAMITY, 11000, 500, new Point(10,10)));
    game.addGO(new Collidable(ID.ID_DOOR, DrawLib.TEX_DOOR, 11200, 189));
    game.addGO(new Collidable(ID.ID_DOOR_POWERED, DrawLib.TEX_DOOR_POWERED, 11200, 189));
  }
  
  private void resetVisibles(int level) {
    game.clearVisibles();
    game.addVisible(ID.ID_HERO); // all levels need the hero!
    switch(level) {
    case 1:// only add level 1 visible objects to this map
      game.addVisible(ID.ID_JETPACK);
      game.addVisible(ID.ID_ALT_WEAPON);
      game.addVisible(ID.ID_ENEMY_1);
      game.addVisible(ID.ID_ENEMY_2);
      game.addVisible(ID.ID_ENEMY_3);
      game.addVisible(ID.ID_ARMOR);
      game.addVisible(ID.ID_CALAMITY);
      game.addVisible(ID.ID_DOOR);
      break;
    default: System.out.println("Unknown level number while resetting visibles."); break;
    }
  }
  
  public void jumpToLevel(int num) {
    switch(num) {
      case 1: loadLevel(1, "res/layer_collision_1.png"); break;
      case 2: loadLevel(2, "res/layer_collision_1.png"); break;
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
    resetVisibles(levelNum);
    LevelBuilder levelBuilder = new LevelBuilder(fileName);
    ArrayList<Rectangle> level = levelBuilder.scanForBoundaries();
    level.stream().forEach((r) -> {
      int id = ID.getNewId();
      game.addVisible(id, new Collidable(id, DrawLib.TEX_LEVEL,
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
      case RUNNING: drawNormalGamePlay(gl); break; // END RUNNING
      case PAUSED: drawPauseMenu(gl); break; // END PAUSED
      case GAME_OVER: drawGameOver(gl); break;
      case CREDITS: drawCredits(gl); break;
      default: break;
    }
    gl.glPopMatrix(); // return to initial transform
  }
  
  /**
   * Adjusts the scene with the hero's movement.  Will adjust a background scene when forBackground
   * is set to true.
   * @param gl
   * @param forBackground 
   */
  public void adjustScene(GL2 gl, boolean forBackground) {
    if(!forBackground) {
      if(hero.getX() > 600 && hero.getX() < 10500)
        gl.glTranslated(-hero.getX(), scene.transY, scene.transZ);
      else if(hero.getX() <= 600)
        gl.glTranslated(scene.transX, scene.transY, scene.transZ);
      else if(hero.getX() >= 10500)
        gl.glTranslated(-10500, scene.transY, scene.transZ);
    } else {
      if(hero.getX() <= 600)
        gl.glTranslated(-scene.transX*10, -scene.transY*3, -40);
      else
        gl.glTranslated(-scene.transX*10+hero.getX()/10, -scene.transY*3, -40);
    }
  }
  
  /**
   * This is the standard loop for the game, showing level, character, enemies, etc.
   * @param gl 
   */
  private void drawNormalGamePlay(GL2 gl) {
    gl.glPushMatrix(); // save initial transform
    gl.glScaled(scene.scaleX, scene.scaleY, scene.scaleZ); // set global scale
    
    adjustScene(gl, false);
    
    drawBackground(gl);
    drawHero(gl);
    
    fireProjectiles(); // fire projectile from the queue
    
    drawForeground(gl);
    
    gl.glPopMatrix(); // return to initial transform
    drawHud(gl);
    drawStatus(gl); // will only draw status' of new messages, for x seconds
  }
  
  private void drawStatus(GL2 gl) {
    // check if we need to display a message
    if(messageTimer.isRunning()) {
      gl.glPushMatrix();
      gl.glTranslated(0, -DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2, 0);
      DrawLib.drawText(statusMessage, new double[] { 1.0, 1.0, 0.0 }, -60, 20);
      gl.glPopMatrix();
    } else {
      statusMessage = "";
    }
  }
  
  private void drawGameOver(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2, 0, 0);
    DrawLib.drawText("GAME OVER", new double[] { 1.0, 0.0, 0.0 }, 100+(frameNumber%500*2), 0);
    gl.glPopMatrix();
  }
  
  private void drawCredits(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2, 0, 0);
    String credits = "TEAM MARIOTROID!";
    DrawLib.drawText(credits, new double[] { 0.0, 1.0, 0.0 }, windowDim.width/2-50, -windowDim.height/2+(frameNumber%500*2));
    gl.glPopMatrix();
  }
  
  /**
   * Draws the background once per frame.
   * @param gl 
   */
  private void drawBackground(GL2 gl) {
    // back ground level
    gl.glPushMatrix();
    adjustScene(gl, true);
    DrawLib.drawTexturedRectangle(DrawLib.TEX_BACKGROUND_LEVEL);
    gl.glPopMatrix();
    
    // draw game objects
    game.getVisibles().forEach((c) -> { c.draw(); });
  }
  
  /**
   * Draws the hero on top of the scene.
   * @param gl 
   */
  private void drawHero(GL2 gl) {
    hero.draw();
  }
  
   /**
    * Draws any foreground objects to simulate depth.
    * @param gl 
    */
  private void drawForeground(GL2 gl) {
    // draw foreground objects here
    gl.glPushMatrix();
    gl.glTranslated(DrawLib.getTexture(DrawLib.TEX_LEVEL_DECOR).getWidth()/2, DrawLib.getTexture(DrawLib.TEX_LEVEL_DECOR).getHeight()/2, 0);
    DrawLib.drawTexturedRectangle(DrawLib.TEX_LEVEL_DECOR);
    gl.glPopMatrix();
  }
  
  private void drawIntro(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(0, 0, 0);
    DrawLib.drawTexturedRectangle(DrawLib.TEX_LOGO);
    gl.glPopMatrix();
  }

  private void drawStartMenu(GL2 gl) {
    double[] selectedTextColor = new double[] { 1.0, 0.0, 0.0 };
    double[] textColor = new double[] { 0.0, 0.0, 0.0 };
    switch(this.startMenuSelection) {
      case START_GAME:
        gl.glPushMatrix();
        gl.glTranslated(0, 50, 0);
        DrawLib.drawText("START GAME", selectedTextColor, -50, 0);
        gl.glTranslated(0, -100, 0);
        DrawLib.drawText("EXIT", textColor, -20, 0);
        gl.glPopMatrix();
        break;
      case EXIT:
        gl.glPushMatrix();
        gl.glTranslated(0, 50, 0);
        DrawLib.drawText("START GAME", textColor, -50, 0);
        gl.glTranslated(0, -100, 0);
        DrawLib.drawText("EXIT", selectedTextColor, -20, 0);
        gl.glPopMatrix();
        break;
      default: break;
    }
  }

  private void drawPauseMenu(GL2 gl) {
    DrawLib.drawText("GAME PAUSED", new double[] { 1.0, 1.0, 0.0 }, -60, 0);
  }

  private void doStartMenuSelection() {
    switch(this.startMenuSelection) {
      case START_GAME: gameMode = GAME_MODE.RUNNING;
        won = false;
        hero.resetAll();
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
      double xDiff = -30;
      double[] textColor = new double[] { 1.0, 1.0, 1.0 };
      gl.glPushMatrix();
      gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2+DrawLib.getTexture(DrawLib.TEX_ALT_WEAPON).getWidth()/2+xDiff,
              DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2
                      -DrawLib.getTexture(DrawLib.TEX_HEALTH).getHeight()
                      -DrawLib.getTexture(DrawLib.TEX_ALT_WEAPON).getHeight(), 0);
      DrawLib.drawTexturedRectangle(DrawLib.TEX_ALT_WEAPON,
              DrawLib.getTexture(DrawLib.TEX_ALT_WEAPON).getWidth()/3,
              DrawLib.getTexture(DrawLib.TEX_ALT_WEAPON).getHeight()/3);
      DrawLib.drawText(Integer.toString(hero.getAmmoCount()), textColor, 20, -6);
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
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2,
            DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2, 0);
    DrawLib.drawText(Integer.toString(hero.getLives()), textColor, 40, -50);
    gl.glPopMatrix();
  }

  private void drawScore(GL2 gl) { // draw score in top right corner
    double diffX = 30;
    double[] textColor = new double[] { 1.0, 1.0, 1.0 };
    gl.glPushMatrix();
    gl.glTranslated(DrawLib.getTexture(DrawLib.TEX_HUD).getWidth()/2-diffX,
            DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2, 0);
    DrawLib.drawText("SCORE: " + Long.toString(hero.getScore()), textColor, -120, -20);
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
    switch(gameMode) { // controls are based on the game mode
    case RUNNING:
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
        if(!hero.didLand()) hero.setSpeedY(-PhysicsEngine.GRAVITY);
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
    case RUNNING:
      if(won) { gameMode = GAME_MODE.CREDITS; return; } // check for winning conditions
      ArrayList<Collidable> visibleObjects = game.getVisibles();
      ArrayList<Integer> toRemove = new ArrayList<>(); // keep track of ids to remove at end of frame
      
      // move all objects
      for(Collidable c : visibleObjects) {
        if((new Boss()).getClass().isInstance(c)) {
          Boss b = (Boss)c;
          b.move();
        } else if((new Movable()).getClass().isInstance(c)) {
          Movable m = (Movable)c;
          m.move();
        } else if((new Projectile()).getClass().isInstance(c)) {
          Projectile p = (Projectile)c;
          p.move();
          if(Math.abs(c.getX()) > Math.abs(hero.getX()) + 3000) toRemove.add(p.getObjectId()); // remove offscreen projectiles
        }
      }
    
      // enemy movement and collision detection
      for(int i = ID.ID_ENEMY_1; i <= ID.ID_ENEMY_3; i++) {
        Enemy enemy = (Enemy)(game.getVisible(i));
        if(enemy != null) {
          List<Collidable> enemyCollisions = enemy.processCollisions(visibleObjects);
          for(Collidable c : enemyCollisions) {
            if(new Projectile().getClass().isInstance(c)) {
              toRemove.add(c.getObjectId());
              if(enemy.getHealth() <= 0) { // enemy died
                hero.addScore(enemy.getPointsWorth());
                toRemove.add(enemy.getObjectId());
              }
            }
          }
        }
      }
      
      // boss movement and collision detection
      Boss boss = (Boss)(game.getVisible(ID.ID_CALAMITY));
      if(boss != null) {
        List<Collidable> bossCollisions = boss.processCollisions(visibleObjects);
        for(Collidable c : bossCollisions) {
          if(new Projectile().getClass().isInstance(c)) {
            toRemove.add(c.getObjectId());
            if(boss.getHealth() <= 0) { // enemy died
              toRemove.add(boss.getObjectId());
              hero.addScore(boss.getPointsWorth());
              game.addVisible(ID.ID_DOOR_POWERED); // add door after calamity is defeated!
              game.removeVisible(ID.ID_DOOR);
            }
          }
        }
      }
      
      // hero movement and collision detection
      List<Collidable> heroCollisions = hero.processCollisions(visibleObjects);
      for(Collidable c : heroCollisions){
        int id = c.getObjectId();
        switch(id) {
        case ID.ID_JETPACK:
          Engine.setStatusMessage("Got jetpack!");
          hero.addScore(250);
          toRemove.add(id); // remove the jetpack image from the screen
          break;
        case ID.ID_ALT_WEAPON:
          Engine.setStatusMessage("Got missles!");
          hero.addScore(1000);
          toRemove.add(id); // remove the shell image from the screen
          break;
        case ID.ID_ARMOR:
          Engine.setStatusMessage("Got armor!");
          hero.addScore(275);
          toRemove.add(id); // remove the armor image from the screen
          break;
        case ID.ID_DOOR_POWERED:
          won = true;
          break;
        default: break;
        }
      }
      
      // remove all ids tagged for removal
      toRemove.stream().forEach((id) -> { game.removeVisible(id); });
      
      // set textures based on speed here
      if(hero.standingStill()) hero.setTextureId(DrawLib.TEX_HERO);
      else if(hero.movingLeft() || hero.movingRight()) hero.setTextureId(DrawLib.TEX_HERO_RUN1);
      break;
    default: break;
    }
  }

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
        qProjectiles.add(new NextProjectile(sc, true)); // add the projectile to the queue, to be fired during next update
        break;
      case MouseEvent.BUTTON2: // middle click
        break;
      case MouseEvent.BUTTON3: // right click
        qProjectiles.add(new NextProjectile(sc, false)); // add the projectile to the queue, to be fired during next update
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
  public void mouseMoved(MouseEvent evt) { }    
  
  @Override
  public void mouseClicked(MouseEvent evt) {
    int key = evt.getButton();
    Point sc = evt.getPoint(); // clicked location, to convert to world coords
    
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
    case CREDITS:
    case GAME_OVER:
      switch(key) {
      case MouseEvent.BUTTON1:
        this.gameMode = GAME_MODE.START_MENU;
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
      NextProjectile np = qProjectiles.pop();
      Point sc = np.screenCoord;
      Point.Double wc = DrawLib.screenToWorld(sc);
      setStatusMessage("Clicked point in world: (" + wc.x + ", " + wc.y + ")");
      
      // can only shoot in front of direction the hero is facing
      //if(wc.x >= hero.getX() - hero.getW()/2 || (hero.isFlippedOnY() && (wc.x <= hero.getX() + hero.getW()/2))) {
        Projectile fired;
        if(np.isFromPrimaryWeapon)
          fired = hero.firePrimaryWeapon(wc);
        else
          fired = hero.fireSecondaryWeapon(wc);
        if(fired != null) game.addVisible(fired.getObjectId(), fired);
        else System.out.println("Attempted to fire null projectile.");
      //}
    }
  }
}
