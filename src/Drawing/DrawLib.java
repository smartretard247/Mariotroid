package Drawing;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
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
  
  private static final Map<Integer, String> textureIdMap = new HashMap<>();
  public static final int TEX_TEST = -3;
  public static final int TEX_FLOOR = -2;
  public static final int TEX_NONE = -1;
  public static final int TEX_HERO = 0; // easier texture identification
  public static final int TEX_HERO_RUN1 = 1;
  public static final int TEX_HERO_RUN2 = 2;
  public static final int TEX_HERO_BACKPACK1 = 3;
  public static final int TEX_LOGO = 4;
  public static final int TEX_HEALTH = 5;
  public static final int TEX_HUD = 6;
  public static final int TEX_SHELL = 7;
  public static final int TEX_JETPACK = -8;
  public static final int TEX_ALT_WEAPON = 9;
  public static final int TEX_COLLISIONS_START = 10; // collision textures between this
  public static final int TEX_COLLISIONS_END = 16;  // and this

  public DrawLib(GL2 context) {
    gl = context;
    
  // all images should be listed here, and stored in the textures directory
    textureIdMap.put(TEX_HERO, "art/sprites/hero/hero.png");
    textureIdMap.put(TEX_HERO_RUN1, "art/sprites/hero/hero_run_step_1.png");
    textureIdMap.put(TEX_HERO_RUN2, "art/sprites/hero/hero_run_step_2.png");
    textureIdMap.put(TEX_HERO_BACKPACK1, "art/sprites/hero/hero_backpack_run_step_1.png");
    textureIdMap.put(TEX_LOGO, "art/logo.png");
    textureIdMap.put(TEX_HEALTH, "art/hud/health.png");
    textureIdMap.put(TEX_HUD, "art/hud/hud.png");
    textureIdMap.put(TEX_SHELL, "art/hud/shell.png");
    //textureIdMap.put(TEX_JETPACK, "art/hud/shell.png");
    textureIdMap.put(TEX_ALT_WEAPON, "art/hud/shell.png");
    textureIdMap.put(TEX_COLLISIONS_START, "art/level/level0.png");
    textureIdMap.put(TEX_COLLISIONS_START+1, "art/level/level1.png");
    textureIdMap.put(TEX_COLLISIONS_START+2, "art/level/level2.png");
    textureIdMap.put(TEX_COLLISIONS_START+3, "art/level/level3.png");
    textureIdMap.put(TEX_COLLISIONS_START+4, "art/level/level4.png");
    textureIdMap.put(TEX_COLLISIONS_START+5, "art/level/level5.png");
    textureIdMap.put(TEX_COLLISIONS_START+6, "art/level/level6.png");
    textureIdMap.put(TEX_COLLISIONS_END, "art/level/level7.png");
    
    loadTextures(); // must load after filename 'puts' above
  }

  private static Map<Integer, Texture> textures = new HashMap<>();
  
  public static Texture getTexture(int id) { return textures.get(id); }
  
  /**
   * Loads all the listed images in textureFileNames, as long as they are stored in the textures
   * folder.  Should only be called once during initialization.
   * @param gl 
   */
  private void loadTextures() {
    textureIdMap.keySet().forEach((i) -> {
    //for (int i = 0; i < textureFileNames.length; i++) {
      try {
          URL textureURL;
          textureURL = getClass().getClassLoader().getResource(textureIdMap.get(i));
          if (textureURL != null) {
            BufferedImage img = ImageIO.read(textureURL);
            ImageUtil.flipImageVertically(img);
            Texture temp = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);
            temp.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
            temp.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
            textures.put(i, temp);
          }
      } catch (IOException | GLException e) {
        e.printStackTrace();
      }
    });
    textures.get(0).enable(gl);
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
}
