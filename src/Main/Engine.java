package Main;

import Drawing.DrawLib;
import Enumerations.START_MENU_OPTION;
import Enumerations.GAME_MODE;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_ONE;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
  private final Timer messageTimer = new Timer(5000, this);
  private int frameNumber = 0; // The current frame number for an animation.
  private DrawLib drawLib;
  public static GAME_MODE gameMode = GAME_MODE.INTRO;
  private START_MENU_OPTION startMenuSelection = START_MENU_OPTION.START_GAME;
  private final int INTROLENGTHMS = 4000;
  private final int MAX_GAME_OBJECTS = 1;
  private final int TEX_NONE = -1;
  private final int TEX_HERO = 0; // easier texture identification
  private final int TEX_LOGO = 1;
  private final int TEX_HEALTH = 2;
  private final int TEX_HUD = 3;
  private final int TEX_SHELL = 4;
  private final int TEX_COLLISIONS_START = 5; // collision textures between this
  private final int TEX_COLLISIONS_END = 12;  // and this
  
  public Scene scene; // trans x & y, scale x & y
  public Hero hero;
  public PhysicsEngine phy = new PhysicsEngine();
  public DrawableGameObject[] gameObjects = new DrawableGameObject[MAX_GAME_OBJECTS];
  private String statusMessage = "";
  
  ///// START METHODS

  public static void main(String[] args) {
    JFrame window = new JFrame("Mariotroid");
    Engine panel = new Engine();
    window.setContentPane(panel);
    /* TODO: If you want to have a menu, comment out the following line. */
    //window.setJMenuBar(panel.createMenuBar());
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
    this.startAnimation(); // also control pause function (and remove keyboard response)
    
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
    gl.glClearColor(0, 0.4f, 0.8f, 0);
    gl.glClear( GL.GL_COLOR_BUFFER_BIT ); // TODO? Omit depth buffer for 2D.
    gl.glLoadIdentity();             // Set up modelview transform.
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
    //gl.glEnable(GL2.GL_DEPTH_TEST);  // required for 3D drawing, not usually for 2D.
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();
    gl.glOrtho(-windowDim.width/2, windowDim.width/2 ,-windowDim.height/2, windowDim.height/2, -2, 2);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glClearColor( 0, 0, 0, 1 );
    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    //gl.glEnable(GL2.GL_LIGHTING);        // Enable lighting.
    //gl.glEnable(GL2.GL_LIGHT0);          // Turn on a light.  By default, shines from direction of viewer.
    //gl.glEnable(GL2.GL_NORMALIZE);       // OpenGL will make all normal vectors into unit normals
    //gl.glEnable(GL2.GL_COLOR_MATERIAL);  // Material ambient and diffuse colors can be set by glColor*
    messageTimer.setRepeats(false);
    drawLib = new DrawLib(gl); // initialize the drawing library before dealing with any textures!!
    scene = new Scene(60, 135, 0.5, 0.5);
    hero = new Hero(3, 0, 10, TEX_HERO, -150, 0, // 3 lives, 0 score, 10 health, texId, x, y
          DrawLib.getTexture(TEX_HERO).getWidth(), // width
          DrawLib.getTexture(TEX_HERO).getHeight()); // height
    
    // initialize all game objects here
    for(int i = 0; i < MAX_GAME_OBJECTS; i++) {
      gameObjects[i] = new DrawableGameObject(TEX_NONE, -50, -505, 400, 50);
    }
    
  }
  
  /* 
   * Draws the scene, for each given game mode.
  */
  private void draw(GL2 gl) {
    switch(gameMode) {
      case INTRO: drawIntro(gl); break; // END INTRO
      case START_MENU: drawStartMenu(gl); break; // END START MENU
      case RUNNING: drawNormalGamePlay(gl); break; // END RUNNING
      case PAUSED: drawPauseMenu(gl); break; // END PAUSED
      case GAME_OVER: drawGameOver(gl); break;
      default: break;
    }
  }
  
  /**
   * This is the standard loop for the game, showing level, character, enemies, etc.
   * @param gl 
   */
  private void drawNormalGamePlay(GL2 gl) {
    gl.glPushMatrix(); // save initial transform
    gl.glScaled(scene.scaleX, scene.scaleY, 1); // set global scale
    gl.glTranslated(scene.transX, scene.transY, 0);  //move the world to respond to user input
    
    drawBackground(gl);
    drawHero(gl);
    drawCollisions(gl); // USE THIS LINE ONLY WHEN TESTING COLLISIONS!!
    drawForeground(gl);
    
    gl.glPopMatrix(); // return to initial transform

    drawHud(gl);
    drawHealth(gl);
    drawLives(gl);
    drawScore(gl);
    drawStatus(gl); // will only draw status' of new messages, for x seconds
  }
  
  private void drawStatus(GL2 gl) {
    // check if we need to display a message
    if(messageTimer.isRunning()) {
      gl.glPushMatrix();
      gl.glTranslated(0, -DrawLib.getTexture(TEX_HUD).getHeight()/2, 0);
      DrawLib.drawText(statusMessage, new double[] { 1.0, 1.0, 0.0 }, -60, 20);
      gl.glPopMatrix();
    } else {
      statusMessage = "";
    }
  }
  
  private void drawGameOver(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(TEX_HUD).getWidth()/2, 0, 0);
    DrawLib.drawText("GAME OVER", new double[] { 1.0, 0.0, 0.0 }, 100+(frameNumber%500*2), 0);
    gl.glPopMatrix();
  }
  
  /**
   * Draws the background once per frame.
   * @param gl 
   */
  private void drawBackground(GL2 gl) {
    // back ground objects
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
    //TEST BLOCK /////////////////////
    gl.glPushMatrix();
    float[] red = { 1.0f, 0, 0 };
    gl.glColor3fv(red, 0);
    gl.glTranslated(60, 0, 0);
    DrawLib.drawRectangle(100, 100); // a basic rectangle
    gl.glPopMatrix();
    // END TEST BLOCK //////////////
  }
  
  private void drawIntro(GL2 gl) {
    DrawLib.drawTexturedRectangle(TEX_LOGO);
  }
  
  /**
   * This is only temporary to display the collision PNG.
   * @param gl 
   */
  private void drawCollisions(GL2 gl) { // TODO: only draw collisions 'close' to character
    gl.glPushMatrix();
    for(int i = TEX_COLLISIONS_START; i < TEX_COLLISIONS_END; i++) {
      DrawLib.drawTexturedRectangle(i);
      gl.glTranslated(DrawLib.getTexture(i).getWidth(), 0, 0);
    }
    gl.glPopMatrix();
    
    // draw test object
    gameObjects[0].draw();
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
        hero.resetAll();
        break;
      case EXIT: System.exit(0);
        break;
      default: break;
    }
  }

  private void drawHud(GL2 gl) {
    gl.glPushMatrix();
    DrawLib.drawTexturedRectangle(TEX_HUD);
    gl.glPopMatrix();
  }

  private void drawHealth(GL2 gl) { // draw health in top left corner
    double xDiff = 10;
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(TEX_HUD).getWidth()/2+DrawLib.getTexture(TEX_HEALTH).getWidth()+xDiff,
            DrawLib.getTexture(TEX_HUD).getHeight()/2-DrawLib.getTexture(TEX_HEALTH).getHeight(), 0);
    for(int i = 0; i < hero.getHealth(); i++) {
      DrawLib.drawTexturedRectangle(TEX_HEALTH);
      gl.glTranslated(DrawLib.getTexture(TEX_HEALTH).getWidth(), 0, 0);
    }
    gl.glPopMatrix();
  }
  
  private void drawLives(GL2 gl) {
    double[] textColor = new double[] { 1.0, 1.0, 1.0 };
    gl.glPushMatrix();
    gl.glTranslated(-DrawLib.getTexture(TEX_HUD).getWidth()/2, DrawLib.getTexture(TEX_HUD).getHeight()/2, 0);
    DrawLib.drawText(Integer.toString(hero.getLives()), textColor, 30, -50);
    gl.glPopMatrix();
  }

  private void drawScore(GL2 gl) { // draw score in top right corner
    double diffX = 30;
    double[] textColor = new double[] { 1.0, 1.0, 1.0 };
    gl.glPushMatrix();
    gl.glTranslated(DrawLib.getTexture(TEX_HUD).getWidth()/2-diffX, DrawLib.getTexture(TEX_HUD).getHeight()/2, 0);
    DrawLib.drawText("SCORE: " + Long.toString(hero.getScore()), textColor, -120, -20);
    gl.glPopMatrix();
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
        if(!hero.isSprinting())
          hero.toggleSprint();
        break;
      case KeyEvent.VK_P: // pause/unpause
        gameMode = GAME_MODE.PAUSED;
        break;
      case KeyEvent.VK_A: // move left
        hero.increaseSpeed(-10, 0);
        break;
      case KeyEvent.VK_D: // move right
        hero.increaseSpeed(10, 0);
        break;
      case KeyEvent.VK_SPACE: // jump
        hero.setSpeedY(60);
        break;
      case KeyEvent.VK_S: // crouch
        // TODO: change to crouch (image and collision rect will shrink)
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
      case KeyEvent.VK_SHIFT:
        if(hero.isSprinting())
          hero.toggleSprint();
        break;
      case KeyEvent.VK_A: // stop moving left
        hero.setSpeedX(0);
        break;
      case KeyEvent.VK_D: // stop moving right
        hero.setSpeedX(0);
        break;
      case KeyEvent.VK_SPACE: // stop jump, start fall
        hero.setSpeedY(0);
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
    case RUNNING: hero.move(gameObjects);
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
    case RUNNING:
      switch(key) {
      case MouseEvent.BUTTON1: // left click
        try {
          hero.loseHealth(1); // lose 1 health, TEST
        } catch (GameOverException ex) {
          this.gameMode = GAME_MODE.GAME_OVER;
        }
        break;
      case MouseEvent.BUTTON2: // middle click
        setStatus("HERO!");
        break;
      case MouseEvent.BUTTON3: // right click
        hero.addScore(100);
        break;
      default: break;
      }
      break; // END START_MENU
    case GAME_OVER:
      switch(key) {
      case MouseEvent.BUTTON1:
        this.gameMode = GAME_MODE.START_MENU;
      default: break;
      }
    }
  }
  @Override public void mouseEntered(MouseEvent evt) { }
  @Override public void mouseExited(MouseEvent evt) { }
  
  /**
   * Game will automatically display this message at the bottom of the screen for x seconds.
   * @param message 
   */
  public void setStatus(String message) {
    this.statusMessage = message;
    messageTimer.start();
  }
}
