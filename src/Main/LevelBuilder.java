package Main;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Jeezy
 */
public class LevelBuilder {
  private Raster raster;
  private final DataLoader dl;
  private final String imageFileName;
  private final String dataFileName;
  private boolean firstLoad = true;
  
  public LevelBuilder(String fileName, int num) {
    imageFileName = fileName;
    dataFileName = imageFileName.substring(imageFileName.lastIndexOf("/")+1, imageFileName.length()-4) + Integer.toString(num) + ".txt";
    URL level = getClass().getClassLoader().getResource(dataFileName);
    if(level != null)
      dl = new DataLoader(level);
    else
      dl = new DataLoader(dataFileName);
    if(dl.getData().isEmpty())
      loadImage(imageFileName);
    else
      firstLoad = false;
  }
  
  /**
   * Scan the supplied image for rectangles, and return an array list of them.
   * @return 
   */
  public ArrayList<Rectangle> scanForBoundaries() {
    ArrayList<Rectangle> found = new ArrayList<>();
    
    //if text file does not exist
    if(firstLoad) {
      java.awt.Rectangle bounds = raster.getBounds();

      // initialize boolean matrix
      boolean[][] marked = new boolean[bounds.width][bounds.height]; // pixels already part of other rectangles
      for (boolean[] marked1 : marked) {
        for (int x = 0; x < marked[0].length; x++) {
          marked1[x] = false;
        }
      }

      System.out.println("Loading level: " + bounds);

      // search for rectangles with the raster
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
              markPixels(marked, r); // mark dead pixels
              // adjust to center before adding to found
              r.setX(r.x()+r.w()/2);
              r.setY(bounds.height-r.y()-r.h()/2);
              found.add(r);
              dl.addLine(r.toString());
            }
          }
        }
      }
      //save the rectangles to a file for future use
      if(!dl.getData().isEmpty())
        dl.saveToFile(dataFileName);
    } else { // otherwise load the rectangles already in the file
      dl.getData().forEach((st) -> {
        // st == Rectangle@( 773.0, 1103.5 ), width: 1524.0, height: 97.0
        Scanner sc = new Scanner(st);
        ArrayList<Double> var = new ArrayList<>();
        while (var.size() < 4) { // untill we have all four variables for the rectangle
          if(sc.hasNextDouble())
            var.add(var.size(), sc.nextDouble()); // use the double
          else
            sc.next(); // else no double found so skip ahead
        }
        Rectangle r = new Rectangle(var.get(0), var.get(1), var.get(2), var.get(3));
        found.add(r); // add to found rectangles
        System.out.println(r); // log rectangle
      });
    }
    
    return found;
  }
  
  private int findWidth(int xPos, int yPos, int maxX) {
    for(int i = xPos+1; i < maxX; i++) {
      if(isTransparentAt(i, yPos))
        return i - xPos; // return width of rectangle
    }
    return maxX - xPos;
  }
  
  // 1535x97, 193x289
  private int findHeight(Rectangle r, int maxY) {
    for(int height = maxY-1; height >= r.y(); height--) {
      if(verifyHeight((int)r.x(), (int)r.y(), (int)r.w(), height))
        return (int) (height - r.y());
    }
    return (int) (maxY - r.y());
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
      URL url = getClass().getClassLoader().getResource(fileName);
      //BufferedImage image = ImageIO.read(new File(fileName));
      BufferedImage image = ImageIO.read(url);
      if(image.getType() != TYPE_INT_RGB) {
        System.out.println("Incorrect image type \"" + ((image.getType() == 13) ? "TYPE_BYTE_INDEXED" : image.getType()) + "\", converting...");
        image = filter(image);
        System.out.println("Successfully converted to TYPE_INT_RGB.  Use this type to conserve time "
                + "to load.");
      }
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
