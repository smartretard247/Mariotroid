package Drawing;

import Enumerations.TEX;
import Test.TestDisplay;
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
import java.io.InputStream;
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
  public static CustomFont cf;
  
  private static final Map<Integer, String> textureIdMap = new HashMap<>();
  
  public DrawLib(GL2 context) {
    gl = context;
    
  // all images should be listed here, and stored in the textures directory
    textureIdMap.put(TEX.HERO, "/res/hero.png");
    textureIdMap.put(TEX.HERO_DEAD, "/res/hero_dead.png");
    textureIdMap.put(TEX.HERO_RUN1, "/res/hero_run_step_1.png");
    textureIdMap.put(TEX.HERO_RUN2, "/res/hero_run_step_2.png");
    textureIdMap.put(TEX.HERO_BACKPACK1, "/res/hero_backpack_run_step_1.png");
    textureIdMap.put(TEX.HERO_TRANSPARENT, "/res/hero_transparent.png");
    textureIdMap.put(TEX.LOGO, "/res/logo.png");
    textureIdMap.put(TEX.HEALTH, "/res/hud_health.png");
    textureIdMap.put(TEX.HUD, "/res/layer_hud.png");
    textureIdMap.put(TEX.SHELL, "/res/hud_shell.png");
    textureIdMap.put(TEX.ALT_WEAPON, "/res/projectile_blue.png");
    textureIdMap.put(TEX.PRI_WEAPON, "/res/projectile_orange.png");
    textureIdMap.put(TEX.ENEMY_WEAPON_1, "/res/projectile_purple.png");
    textureIdMap.put(TEX.ENEMY_WEAPON_2, "/res/projectile_green.png");
    textureIdMap.put(TEX.JETPACK, "/res/jetpack.png");
    textureIdMap.put(TEX.ENEMY_BASIC, "/res/enemy_basic.png");
    textureIdMap.put(TEX.LEVEL_DECOR_1, "/res/layer_decor_1.png"); // only load the first two levels
    textureIdMap.put(TEX.LEVEL_DECOR_2, "/res/layer_decor_2.png"); // the rest will alternate btw these id's
    textureIdMap.put(TEX.CALAMITY, "/res/calamity.png");
    textureIdMap.put(TEX.DOOR, "/res/door.png");
    textureIdMap.put(TEX.DOOR_POWERED, "/res/door_powered.png");
    textureIdMap.put(TEX.ARMOR, "/res/armor.png");
    textureIdMap.put(TEX.BACKGROUND_1, "/res/background_1.jpg");
    textureIdMap.put(TEX.BACKGROUND_2, "/res/background_2.jpg");
    textureIdMap.put(TEX.HEALTH_ORB, "/res/small_health.png");
    textureIdMap.put(TEX.TRANSPARENT, "/res/transparent.png");
    textureIdMap.put(TEX.SWITCH, "/res/switch.png");
    textureIdMap.put(TEX.BOX, "/res/box.png");
    //textureIdMap.put(TEX.TEXT_BOX, "/res/text_box.png");
    textureIdMap.put(TEX.PHANTOM, "/res/phantom.png");
    textureIdMap.put(TEX.WEAPON_PICKUP, "/res/weapon_pickup.png");
    textureIdMap.put(TEX.AMMO_ORB, "/res/small_ammo.png");
    textureIdMap.put(TEX.BIG_HEALTH_ORB, "/res/big_health.png");
    textureIdMap.put(TEX.BIG_AMMO_ORB, "/res/big_ammo.png");
    
    // load custom font
    String fontName = "/res/spac3.ttf";
    InputStream is = DrawLib.class.getResourceAsStream(fontName);
    cf = new CustomFont(is);
    
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
    textureIdMap.keySet().forEach((i) -> { loadTexture(i, textureIdMap.get(i)); });
  }
  
  /**
   * Loads a single texture with index as ID and resource as a path to the image.  Will remove any
   * texture that already exists with the same ID.
   * @param index
   * @param resource 
   */
  public void loadTexture(int index, String resource) {
    try {
      URL textureURL = getClass().getResource(resource);
      if (textureURL != null) {
        BufferedImage img = ImageIO.read(textureURL);
        ImageUtil.flipImageVertically(img);
        Texture temp = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);
        temp.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
        temp.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
        if(textures.containsKey(index)) textures.remove(index);
        textures.put(index, temp);
      } else {
        System.out.println("Invalid textureURL in DrawLib.loadTexture: " + resource);
      }
    } catch (IOException | GLException e) {
      System.out.println("Could not create a texture, see loadTexture().");
      //e.printStackTrace();
    }
  }
  
  public static void drawLine(float fromX, float fromY, float toX, float toY) {
    gl.glBegin (GL2.GL_LINES);
    gl.glVertex3d(fromX, fromY, 0);
    gl.glVertex3d(toX, toY, 0);
    gl.glEnd();
  }
  
  /**
   * Draws given text on the screen, in the given color.  Use rasterPosX and rasterPosY to adjust
   * where the text is displayed.
   * @param text
   * @param rasterPosX
   * @param rasterPosY 
   */
  public static void drawText(String text, float rasterPosX, float rasterPosY) {
    gl.glRasterPos2d(rasterPosX, rasterPosY);
    glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, text);
  }
  
  /**
   * Draws the given text with custom font.
   * @param text 
   */
  public static void drawCustomText(String text) {
    BufferedImage textAsBitmap = cf.getImage(text);
    Texture temp = AWTTextureIO.newTexture(GLProfile.getDefault(), textAsBitmap, true);
    temp.enable(gl);
    temp.bind(gl);  // set texture to use
    gl.glPushMatrix();
    gl.glScaled(temp.getWidth(), temp.getHeight(), 1);
    TexturedShapes.square(gl, 1, true);
    gl.glPopMatrix();
    temp.disable(gl);
  }
  
  /**
   * Draws a rectangle with the current color based on the given scales, centered at (0,0,0) and
   * facing in the +z direction.
   * @param width
   * @param height
  */
  public static void drawRectangle(float width, float height) {
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
  public static void drawTexturedRectangle(int textureId, float width, float height) {
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
    float width = textures.get(textureId).getWidth();
    float height = textures.get(textureId).getHeight();
    drawTexturedRectangle(textureId, width, height);
  }
  
  /**
   * Given a screen coordinate p, will return a point corresponding to the world position.
   * @param p
   * @return 
   */
  public static Point.Float screenToWorld(Point p) {
    int[] viewport = new int[4]; //var to hold the viewport info
    double[] modelview = new double[16]; //var to hold the modelview info
    double[] projection = new double[16]; //var to hold the projection matrix info
    double wcoord[] = new double[4]; //variables to hold world x,y,z coordinates
    
    gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, modelview, 0 ); //get the modelview info
    gl.glGetDoublev( GL2.GL_PROJECTION_MATRIX, projection, 0 ); //get the projection matrix info
    gl.glGetIntegerv( GL2.GL_VIEWPORT, viewport, 0 ); //get the viewport info
 
    //get the world coordinates from the screen coordinates
    int realy = viewport[3] - (int) p.y - 1;
    DrawLib.glu.gluUnProject((float) p.x, (float) realy, 0.0,
              modelview, 0,
              projection, 0, 
              viewport, 0, 
              wcoord, 0);
    return new Point.Float((float)wcoord[0], (float)wcoord[1]);
  }
  
  /**
   * Generate texture Id from given ids (drops) at given rate (rates).
   * @param drops
   * @param rates
   * @return 
   */
  public static int generateDropTex(int[] drops, float[] rates) {
    int dropTex = -1;
    double rand = Math.random();
    TestDisplay.addTestData("rand = " + rand);
    for (int drop = 0; drop < drops.length ; drop++){
      if (rand < rates[drop]){
        dropTex = drops[drop];
        break;
      }
    }
    return dropTex;
  }
}
