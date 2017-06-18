package Main;

import static Drawing.DrawLib.gl;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Jeezy
 */
public class LevelBuilder {
  private Raster raster;
  
  public LevelBuilder(String textureFileName) {
    loadImage(textureFileName);
  }
  
  /**
   * Scan the supplied image for rectangles, and return an array list of them.
   * @return 
   */
  public ArrayList<Rectangle> scanForBoundaries() {
    ArrayList<Rectangle> found = new ArrayList<>();
    
    // initialize boolean matrix
    java.awt.Rectangle bounds = raster.getBounds();
    //java.awt.Rectangle bounds = new java.awt.Rectangle(0, 0, tex.getWidth(), tex.getHeight());
    
    boolean[][] marked = new boolean[bounds.width][bounds.height]; // pixels already part of other rectangles
    for (boolean[] marked1 : marked) {
      for (int x = 0; x < marked[0].length; x++) {
        marked1[x] = false;
      }
    }
    
    System.out.println("Loading level: " + bounds);

    for(int y = 0; y < bounds.height; y++) {
      for(int x = 0; x < bounds.width; x++) {
        //if not dead pixel, then we can check for new rectangle
        if(!marked[x][y]) {
          if(!isTransparentAt(x,y)) {
            Rectangle r = new Rectangle();
            r.setX(x);
            r.setY(y);
            r.setW(findWidth(x, y, bounds.width)); // find width
            r.setH(findHeight(r, bounds.height)); //height
            System.out.println(r); // found rectangle
            found.add(r);
            markPixels(marked, r); // mark dead pixels
          }
        }
      }
    }
    
    return found;
  }
  
  private int findWidth(int xPos, int yPos, int maxX) {
    for(int i = xPos+1; i < maxX; i++) {
      if(isTransparentAt(i, yPos))
        return i - xPos; // return width of rectangle
    }
    System.out.print("Found rectangle with max width: ");
    return maxX- xPos;
  }
  
  // 1535x97, 193x289
  private int findHeight(Rectangle r, int maxY) {
    for(int height = maxY-1; height >= r.y(); height--) {
      if(verifyHeight((int)r.x(), (int)r.y(), (int)r.w(), height))
        return (int) (height - r.y());
    }
    return (int) (maxY- r.y());
  }
  
  /**
   * Check all pixels inside rectangle are all black. If a single difference is found return false;
   * @param xPos
   * @param yPos
   * @param width
   * @param height
   * @return 
   */
  private boolean verifyHeight(int xPos, int yPos, int width, int height) {
    for(int y = yPos; y < height; y++) {
      for(int x = xPos; x < width; x++) {
        if(isTransparentAt(x, y))
          return false;
      }
    }
    return true;
  }
  
  private boolean isTransparentAt(int x, int y) {
    int[] color = raster.getPixel(x, y, new int[4]);
    return !(color[3] == 255);
  }
  
  private void markPixels(boolean mat[][], Rectangle r) {
    int rows = (int) (r.y() + r.h());
    int cols = (int) (r.x() + r.w());
    for(int y = (int) r.y(); y < rows; y++) {
      for(int x = (int) r.x(); x < cols; x++) {
        mat[x][y] = true;
      }
    }
  }

  /**
   * Loads the listed image attached to given resource name.
   * folder.  Should only be called once during initialization.
   * @param gl 
   */
  private void loadImage(String fileName) {
    try {
      BufferedImage image = ImageIO.read(new File(fileName));
      if(image.getType() != TYPE_INT_RGB) {
        System.out.println("Incorrect image type \"" + ((image.getType() == 13) ? "TYPE_BYTE_INDEXED" : image.getType()) + "\", converting...");
        image = filter(image);
        System.out.println("Successfully converted to TYPE_INT_RGB.  Use this type to conserve time "
                + "to load.");
      }
      //ImageUtil.flipImageVertically(image);
      //tex = AWTTextureIO.newTexture(GLProfile.getDefault(), image, true);
      //tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
      //tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
      raster = image.getData();
    } catch (IOException ex) {
      Logger.getLogger(LevelBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public BufferedImage filter(BufferedImage src) {
    BufferedImage convertedImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < src.getWidth(); x++) {
      for (int y = 0; y < src.getHeight(); y++) {
          convertedImage.setRGB(x, y, src.getRGB(x, y));
      }
    }
    return convertedImage;
  }
}
