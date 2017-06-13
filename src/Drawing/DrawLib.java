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
import javax.imageio.ImageIO;

/**
 *
 * @author Jeezy
 */
public class DrawLib {
  public static GL2 gl;
  public static final GLUT glut = new GLUT();
  
  // all images should be listed here, and stored in the textures directory
  private static final String[] textureFileNames = {
    "art/sprites/hero/hero.png",
    "art/logo.png",
    "art/hud/health.png",
    "art/hud/hud.png",
    "art/hud/shell.png",
    "art/level/level0.png",
    "art/level/level1.png",
    "art/level/level2.png",
    "art/level/level3.png",
    "art/level/level4.png",
    "art/level/level5.png",
    "art/level/level6.png",
    "art/level/level7.png"
  };
  private static final Texture[] textures = new Texture[textureFileNames.length];
  
  public static Texture getTexture(int id) { return textures[id]; }
  
  public DrawLib(GL2 context) {
    gl = context;
    loadTextures();
  }
  
  /**
   * Loads all the listed images in textureFileNames, as long as they are stored in the textures
   * folder.  Should only be called once during initialization.
   * @param gl 
   */
  private void loadTextures() {
    for (int i = 0; i < textureFileNames.length; i++) {
      try {
          URL textureURL;
          textureURL = getClass().getClassLoader().getResource(textureFileNames[i]);
          if (textureURL != null) {
            BufferedImage img = ImageIO.read(textureURL);
            ImageUtil.flipImageVertically(img);
            textures[i] = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);
            textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
            textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
          }
      }
      catch (IOException | GLException e) {
        e.printStackTrace();
      }
    }
    textures[0].enable(gl);
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
   * Draws a non-repeating rectangle using the texture specified.
   * @param textureId is an integer matching the array index of the texture
   */
  public static void drawTexturedRectangle(int textureId) {
    double width = textures[textureId].getWidth();
    double height = textures[textureId].getHeight();
    
    gl.glColor3f(1.0f, 1.0f, 1.0f); // remove color before applying texture 
    textures[textureId].enable(gl);
    textures[textureId].bind(gl);  // set texture to use
    gl.glPushMatrix();
    gl.glScaled(width, height, 1);
    TexturedShapes.square(gl, 1, true);
    gl.glPopMatrix();
    textures[textureId].disable(gl);
  }
}
