package Main;

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
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
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
  private final GLUT glut = new GLUT();
  private final GLJPanel display;
  private final Dimension windowDim = new Dimension(1280,720);
  private Timer animationTimer;
  private int frameNumber = 0; // The current frame number for an animation.
  private GAME_MODES gameMode = GAME_MODES.INTRO;
  private START_MENU_OPTIONS startMenuSelection = START_MENU_OPTIONS.START_GAME;
  private long score = 0;
  
  // variables to translate the scene
  private double transX = 0; // for moving the entire scene
  private double transY = 0; // for moving the entire scene
  private double scaleX = 0.2; // global scaling
  private double scaleY = 0.2; // global scaling
  
  // these will most likely have to be wrapped in a class
  private double heroX = 0; // for moving the hero only
  private double heroY = 0; // for moving the hero only
  private double speedX = 10.0; // movement increment
  private double speedY = 10.0; // movement increment
  
  // all images should be listed here, and stored in the textures directory
  private final String[] textureFileNames = {
    "hero.png",
    "logo.png",
    "level.png",
    "cloud.gif",
    "TinySmiley.png"
  };
  private final int TEX_HERO = 0; // easier texture identification
  private final int TEX_LOGO = 1;
  private final int TEX_COLLISIONS = 2;
  private final int TEX_CLOUD = 3;
  private final int TEX_SMILEY = 4;
  private final Texture[] textures = new Texture[textureFileNames.length];
  
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
    panel.requestFocusInWindow(); //
  }

  @SuppressWarnings("LeakingThisInConstructor")
  public Engine() {
    GLCapabilities caps = new GLCapabilities(null);
    display = new GLJPanel(caps);
    display.setPreferredSize( windowDim );
    display.addGLEventListener(this);
    setLayout(new BorderLayout());
    add(display,BorderLayout.CENTER);
    // TODO:  Other components could be added to the main panel.

    // enable keyboard event handling
    display.requestFocusInWindow();
    display.addKeyListener(this);

    // TODO:  Uncomment the next one or two lines to enable mouse event handling
    //display.addMouseListener(this);
    //display.addMouseMotionListener(this);

    // start the animation
    this.startAnimation(); // also control pause function (and remove keyboard response)
    
    Timer introTimer = new Timer(4000, (evt)-> {
      this.gameMode = GAME_MODES.START_MENU;
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
    this.loadTextures(gl);
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
      default: break;
    }
  }
  
  /**
   * This is the standard loop for the game, showing level, character, enemies, etc.
   * @param gl 
   */
  private void drawNormalGamePlay(GL2 gl) {
    gl.glPushMatrix(); // save initial transform
    gl.glScaled(scaleX, scaleY, 1); // set global scale
    gl.glTranslated(transX, transY, 0);  //move the world to respond to user input
    // THIS BLOCK FOR TESTING ONLY ///////////////////////
    // snippet as reference to drawing a textured object
    gl.glPushMatrix();
    gl.glTranslated(0, 100, 0);
    drawTexturedRectangle(gl, TEX_CLOUD, 200, 80);
    gl.glPopMatrix();
    // end reference

    gl.glPushMatrix();
    gl.glTranslated(-300, 0, 0);
    drawTexturedRectangle(gl, TEX_SMILEY, 50, 50);
    gl.glTranslated(0, 60, 0);
    gl.glPopMatrix();
    // END TEST BLOCK

    drawBackground(gl);
    drawHero(gl);
    drawForeground(gl);
    //drawCollisions(gl); // USE THIS LINE ONLY WHEN TESTING COLLISIONS!!

    gl.glPopMatrix(); // return to initial transform
  }
  
  /**
   * Draws the background once per frame.
   * @param gl 
   */
  private void drawBackground(GL2 gl) {
    
  }
  
  /**
   * Draws the hero on top of the scene.
   * @param gl 
   */
  private void drawHero(GL2 gl) {
    gl.glPushMatrix();
    gl.glTranslated(heroX, heroY, 0);  //move the world to respond to user input
    drawTexturedRectangle(gl, TEX_HERO, textures[TEX_HERO].getWidth(), textures[TEX_HERO].getHeight());
    gl.glPopMatrix();
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
    drawRectangle(gl, 100, 100); // a basic rectangle
    gl.glPopMatrix();
    // END TEST BLOCK //////////////
    
    //drawHud(gl); // TODO: implement HUD function
    drawHealth(gl);
    drawScore(gl);
  }
  
  /**
   * Draws a rectangle using the texture and dimensions specified.
   * @param gl
   * @param textureId is an integer matching the array index of the texture
   * @param width
   * @param height 
   */
  private void drawTexturedRectangle(GL2 gl, int textureId, double width, double height) {
    gl.glColor3f(1.0f, 1.0f, 1.0f); // remove color before applying texture 
    textures[textureId].enable(gl);
    textures[textureId].bind(gl);  // set texture to use
    gl.glPushMatrix();
    gl.glScaled(width, height, 1);
    TexturedShapes.square(gl, 1, true);
    gl.glPopMatrix();
    textures[textureId].disable(gl);
  }
  
  /* 
   * Draws a rectangle with the current color based on the given scales, centered at (0,0,0) and
   * facing in the +z direction.
  */
  private void drawRectangle(GL2 gl, double width, double height) {
    gl.glPushMatrix();
    gl.glScaled(width, height, 1);
    gl.glBegin(GL.GL_TRIANGLE_FAN);
    gl.glVertex3d(-0.5, -0.5, 0);
    gl.glVertex3d(0.5, -0.5, 0);
    gl.glVertex3d(0.5, 0.5, 0);
    gl.glVertex3d(-0.5, 0.5, 0);
    gl.glEnd();
    gl.glPopMatrix();
  }
  
  /**
   * Draws given text on the screen, in the given color.  Use rasterPosX and rasterPosY to adjust
   * where the text is displayed.
   * @param gl
   * @param text
   * @param color
   * @param rasterPosX
   * @param rasterPosY 
   */
  private void drawText(GL2 gl, String text, double[] color, double rasterPosX, double rasterPosY) {
    gl.glColor3d(color[0], color[1], color[2]);
    gl.glRasterPos2d(rasterPosX, rasterPosY);
    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, text);

  }
  
  private void drawIntro(GL2 gl) {
    //gl.glPushMatrix();
    drawTexturedRectangle(gl, TEX_LOGO, textures[TEX_LOGO].getWidth(), textures[TEX_LOGO].getHeight());
    //gl.glPopMatrix();
  }
  
  private void drawCollisions(GL2 gl) {
    //gl.glPushMatrix();
    drawTexturedRectangle(gl, TEX_COLLISIONS, textures[TEX_COLLISIONS].getWidth(), textures[TEX_COLLISIONS].getHeight());
    //gl.glPopMatrix();
  }
  
  /**
   * Loads all the listed images in textureFileNames, as long as they are stored in the textures
   * folder.  Should only be called once during initialization.
   * @param gl 
   */
  private void loadTextures(GL2 gl) {
    for (int i = 0; i < textureFileNames.length; i++) {
      try {
          URL textureURL;
          textureURL = getClass().getClassLoader().getResource(textureFileNames[i]);
          if (textureURL != null) {
            BufferedImage img = ImageIO.read(textureURL);
            ImageUtil.flipImageVertically(img);
            textures[i] = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);
            textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
          }
      }
      catch (IOException | GLException e) {
        e.printStackTrace();
      }
    }
    textures[0].enable(gl);
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
        drawText(gl, "START GAME", selectedTextColor, -50, 0);
        gl.glTranslated(0, -100, 0);
        drawText(gl, "EXIT", textColor, -20, 0);
        gl.glPopMatrix();
        break;
      case EXIT:
        gl.glPushMatrix();
        gl.glTranslated(0, 50, 0);
        drawText(gl, "START GAME", textColor, -50, 0);
        gl.glTranslated(0, -100, 0);
        drawText(gl, "EXIT", selectedTextColor, -20, 0);
        gl.glPopMatrix();
        break;
      default: break;
    }
  }

  private void drawPauseMenu(GL2 gl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  private void doStartMenuSelection() {
    switch(this.startMenuSelection) {
      case START_GAME: gameMode = GAME_MODES.RUNNING;
        break;
      case EXIT: System.exit(0);
        break;
      default: break;
    }
  }

  private void drawHud(GL2 gl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  private void drawHealth(GL2 gl) { // draw health in top left corner
    double[] textColor = new double[] { 1.0, 1.0, 1.0 };
    gl.glPushMatrix();
    gl.glTranslated(-windowDim.width*2, windowDim.height*2, 0);
    drawText(gl, "HEALTH HERE", textColor, -120, 20); // maybe get hearts for health image?
    gl.glPopMatrix();
  }

  private void drawScore(GL2 gl) { // draw score in top right corner
    double[] textColor = new double[] { 1.0, 1.0, 1.0 };
    gl.glPushMatrix();
    gl.glTranslated(windowDim.width*2, windowDim.height*2, 0);
    drawText(gl, "SCORE: " + Long.toString(score), textColor, -120, 20);
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
        case KeyEvent.VK_P: // pause/unpause
          gameMode = GAME_MODES.PAUSED;
          break;
        // hero movements
        case KeyEvent.VK_A: // move left
          heroX -= speedX;
          break;
        case KeyEvent.VK_D: // move right
          heroX += speedX;
          break;
        case KeyEvent.VK_SPACE: // jump
          // TODO: jump function
          break;
        case KeyEvent.VK_S: // crouch
          // TODO: change to crouch (image and collision rect will shrink)
          break;
        default: break;
      }
      break; // END RUNNING
    case PAUSED: // then we are paused, so change keyboard options
      switch (key) {
        // hero movements
        case KeyEvent.VK_P: // pause/unpause
          gameMode = GAME_MODES.RUNNING;
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
    // TODO:  add any other updating required for the next frame.
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
  @Override public void mouseMoved(MouseEvent evt) { }    
  @Override public void mouseClicked(MouseEvent evt) { }
  @Override public void mouseEntered(MouseEvent evt) { }
  @Override public void mouseExited(MouseEvent evt) { }
}
