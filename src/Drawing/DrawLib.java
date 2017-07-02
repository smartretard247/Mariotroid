package Drawing;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 *
 * @author Jeezy
 */
public class DrawLib {
  public static GL2 gl;
  public static final GLUT glut = new GLUT();
  public static final GLU glu = new GLU();
  
  private static final Map<Integer, String> textureIdMap = new HashMap<>();
  public static final int TEX_TEST = -3;
  public static final int TEX_LEVEL = -2;
  public static final int TEX_NONE = -1;
  public static final int TEX_HERO = 0; // easier texture identification
  public static final int TEX_HERO_RUN1 = 1;
  public static final int TEX_HERO_RUN2 = 2;
  public static final int TEX_HERO_BACKPACK1 = 3;
  public static final int TEX_LOGO = 4;
  public static final int TEX_HEALTH = 5;
  public static final int TEX_HUD = 6;
  public static final int TEX_SHELL = 7;
  public static final int TEX_JETPACK = 8;
  public static final int TEX_ALT_WEAPON = 9;
  public static final int TEX_ENEMY_BASIC = 10;
  public static final int TEX_LEVEL_DECOR = 11;
  public static final int TEX_CALAMITY = 12;
  public static final int TEX_DOOR = 13;
  public static final int TEX_DOOR_POWERED = 14;
  public static final int TEX_ARMOR = 15;

  public DrawLib(GL2 context) {
    gl = context;
    
  // all images should be listed here, and stored in the textures directory
    textureIdMap.put(TEX_HERO, "/res/hero.png");
    textureIdMap.put(TEX_HERO_RUN1, "/res/hero_run_step_1.png");
    textureIdMap.put(TEX_HERO_RUN2, "/res/hero_run_step_2.png");
    textureIdMap.put(TEX_HERO_BACKPACK1, "/res/hero_backpack_run_step_1.png");
    textureIdMap.put(TEX_LOGO, "/res/logo.png");
    textureIdMap.put(TEX_HEALTH, "/res/hud_health.png");
    textureIdMap.put(TEX_HUD, "/res/layer_hud.png");
    textureIdMap.put(TEX_SHELL, "/res/hud_shell.png");
    textureIdMap.put(TEX_JETPACK, "/res/jetpack.png");
    textureIdMap.put(TEX_ALT_WEAPON, "/res/projectile_blue.png");
    textureIdMap.put(TEX_ENEMY_BASIC, "/res/enemy_basic.png");
    textureIdMap.put(TEX_LEVEL_DECOR, "/res/layer_decor_1.png");
    textureIdMap.put(TEX_CALAMITY, "/res/calamity.png");
    textureIdMap.put(TEX_DOOR, "/res/door.png");
    textureIdMap.put(TEX_DOOR_POWERED, "/res/door_powered.png");
    textureIdMap.put(TEX_ARMOR, "/res/armor.png");
    
    loadTextures(); // must load after filename 'puts' above
  }

  private static final Map<Integer, Texture> textures = new HashMap<>();
  
  public static Texture getTexture(int id) { return textures.get(id); }
  
  /**
   * Loads all the listed images in textureFileNames, as long as they are stored in the textures
   * folder.  Should only be called once during initialization.
   * @param gl 
   */
  private void loadTextures() {
    textureIdMap.keySet().forEach((i) -> {
      try {
        URL textureURL = getClass().getResource(textureIdMap.get(i));
        if (textureURL != null) {
          BufferedImage img = ImageIO.read(textureURL);
          ImageUtil.flipImageVertically(img);
          Texture temp = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);
          temp.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
          temp.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
          textures.put(i, temp);
        } else {
          System.out.println("Invalid textureURL in DrawLib.loadTextures: " + textureIdMap.get(i));
        }
      } catch (IOException | GLException e) {
        e.printStackTrace();
      }
    });
    //textures.get(0).enable(gl);
  }
  
  public static void drawLine(double fromX, double fromY, double toX, double toY) {
    gl.glBegin (GL2.GL_LINES);
    gl.glVertex3d(fromX, fromY, 0);
    gl.glVertex3d(toX, toY, 0);
    gl.glEnd();
  }
  
  /**
   * Draws given text on the screen, in the given color.  Use rasterPosX and rasterPosY to adjust
   * where the text is displayed.
   * @param text
   * @param color
   * @param rasterPosX
   * @param rasterPosY 
   */
  public static void drawText(String text, double[] color, double rasterPosX, double rasterPosY) {
    gl.glColor3d(color[0], color[1], color[2]);
    gl.glRasterPos2d(rasterPosX, rasterPosY);
    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, text);
  }
  
  /**
   * Draws a rectangle with the current color based on the given scales, centered at (0,0,0) and
   * facing in the +z direction.
   * @param width
   * @param height
  */
  public static void drawRectangle(double width, double height) {
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
   * Draws a non-repeating rectangle using the texture and dimensions specified.
   * @param textureId is an integer matching the array index of the texture
   * @param width
   * @param height
   */
  public static void drawTexturedRectangle(int textureId, double width, double height) {
    gl.glColor3f(1.0f, 1.0f, 1.0f); // remove color before applying texture 
    textures.get(textureId).enable(gl);
    textures.get(textureId).bind(gl);  // set texture to use
    gl.glPushMatrix();
    gl.glScaled(width, height, 1);
    TexturedShapes.square(gl, 1, true);
    gl.glPopMatrix();
    textures.get(textureId).disable(gl);
  }
  
  /**
   * Draws the texture with textureId with its default dimensions
   * @param textureId 
   */
  public static void drawTexturedRectangle(int textureId) {
    double width = textures.get(textureId).getWidth();
    double height = textures.get(textureId).getHeight();
    drawTexturedRectangle(textureId, width, height);
  }
  
  /**
   * Given a screen coordinate p, will return a point corresponding to the world position.
   * @param p
   * @return 
   */
  public static Point screenToWorld(Point p) {
    int[] viewport = new int[4]; //var to hold the viewport info
    double[] modelview = new double[16]; //var to hold the modelview info
    double[] projection = new double[16]; //var to hold the projection matrix info
    double wcoord[] = new double[4]; //variables to hold world x,y,z coordinates
    
    gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, modelview, 0 ); //get the modelview info
    gl.glGetDoublev( GL2.GL_PROJECTION_MATRIX, projection, 0 ); //get the projection matrix info
    gl.glGetIntegerv( GL2.GL_VIEWPORT, viewport, 0 ); //get the viewport info
 
    //get the world coordinates from the screen coordinates
    int realy = viewport[3] - (int) p.y - 1;
    DrawLib.glu.gluUnProject((double) p.x, (double) realy, 0.0,
              modelview, 0,
              projection, 0, 
              viewport, 0, 
              wcoord, 0);
    return new Point((int)wcoord[0], (int)wcoord[1]);
  }
}
