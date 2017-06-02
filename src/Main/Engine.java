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
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
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
  private final GLJPanel display;
  private final Dimension windowDim = new Dimension(1280,720);
  private Timer animationTimer;
  
  // variables to translate and rotate the scene
  // private double rotateX = 0;
  // private double rotateY = 0;
  // private double rotateZ = 0;
  private double transX = 0;
  private double transY = 0;
  private final double transZ = 0; // initial depth, won't change because we are simulating 2D
  private double speedX = 1.8;
  private double speedY = 1.8;
  private int frameNumber = 0; // The current frame number for an animation.
  
  // all images should be listed here, and stored in the textures directory
  private final String[] textureFileNames = {
    "cloud.gif",
    "TinySmiley.png"
  };
  private final int TEX_CLOUD = 0; // easier texture identification
  private final int TEX_SMILEY = 1;
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
    //startAnimation();
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
    gl.glPushMatrix(); // save initial transform
    gl.glTranslated(transX, transY, transZ);  //move the world to respond to user input
    draw(gl); // draw the scene, all drawing should be done in draw(), not here
    gl.glPopMatrix(); // return to initial transform
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
    //gl.glEnable(GL2.GL_LIGHTING);        // Enable lighting.
    //gl.glEnable(GL2.GL_LIGHT0);          // Turn on a light.  By default, shines from direction of viewer.
    //gl.glEnable(GL2.GL_NORMALIZE);       // OpenGL will make all normal vectors into unit normals
    //gl.glEnable(GL2.GL_COLOR_MATERIAL);  // Material ambient and diffuse colors can be set by glColor*
    this.loadTextures(gl);
  }
  
  /* 
   * Draws the scene.
  */
  private void draw(GL2 gl) {
    float[] red = { 1.0f, 0, 0 };
    gl.glColor3fv(red, 0);
    drawRectangle(gl, 100, 100); // a basic rectangle
    
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
    drawBlendedRectangle(gl, TEX_SMILEY, 50, 50);
    gl.glPopMatrix();
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
  
  /**
   * Draws textures with alpha blending, use for anything that needs transparency.
   * @param gl
   * @param textureId
   * @param width
   * @param height 
   */
  private void drawBlendedRectangle(GL2 gl, int textureId, double width, double height) {
    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    drawTexturedRectangle(gl, textureId, width, height);
    gl.glDisable(GL_BLEND);
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
      catch (Exception e) {
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
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_A:
        transX -= speedX;
        break;
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_D:
        transX += speedX;
        break;
      case KeyEvent.VK_DOWN:
      case KeyEvent.VK_S:
        transY -= speedY;
        break;
      case KeyEvent.VK_UP:
      case KeyEvent.VK_W:
        transY += speedY;
        break;
      default:
        break;
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
