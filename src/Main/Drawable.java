package Main;

import Drawing.DrawLib;
import com.jogamp.opengl.GL2;

/**
 *
 * @author Jeezy
 */
public class Drawable {
  public final static GL2 GL = DrawLib.gl;
  private int textureId;
  protected double x; // for moving x direction
  protected double y; // for moving y direction
  protected double width, height; // for building the collision rect
  private double defX;
  private double defY;
  protected boolean flipY;
  public final double color[]; // rgb color for non-sprites
  
  public Drawable(int texId, double x, double y, double w, double h) {
    textureId = texId;
    this.x = x;
    this.y = y;
    defX = x;
    defY = y;
    width = w;
    height = h;
    flipY = false;
    color = new double[] { 0.0, 0.0, 0.0 };
  }
  
  public Drawable(int texId, double x, double y) {
    this(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
  }
  
  public int getTextureId() { return textureId; }
  public double getX() { return x; }
  public double getY() { return y; }
  public double getW() { return width; }
  public double getH() { return height; }
  public double getLeft() { return x-width/2; }
  public double getRight() { return x+width/2; }
  public double getBottom() { return y-height/2; }
  public double getTop() { return y+height/2; }
  public void setTextureId(int id) { textureId = id; }
  public void setX(double posX) { x = posX; }
  public void setY(double posY) { y = posY; }
  public void setPosition(double posX, double posY) { x = posX; y = posY; }
  public void setW(int w) { width = w; }
  public void setH(int h) { height = h; }
  public void setDimensions(int w, int h) { width = w; height = h; } // only applies to rectangle, NOT image
  
  /**
   * Used to update the objects default position.  This may be used to resume from a continue point,
   * for example.  After setting the default position, use resetPosition to move object to its
   * default position.
   * @param x
   * @param y 
   */
  public void setDefaultPosition(double x, double y) {
    defX = x; defY = y;
  } 
  
  /**
   * Reset the position back to the initial x and y coordinates.
   */
  public void resetPosition() {
    this.setPosition(defX, defY);
  }
  
  /**
   * Call this to make images flip along the y-axis, making them seem to turn around.
   * @param to
   */
  public void setFlipY(boolean to) { flipY = to; }
  
  public void setColor(double r, double g, double b) {
    color[0] = r;
    color[1] = g;
    color[2] = b;
  }
  
  /**
   * Draws the game object using a valid textureId, if one is not present it will draw a plain
   * rectangle in its place.
   */
  public void draw() {
    GL.glPushMatrix();
    GL.glTranslated(getX(), getY(), 0);
    if(textureId >= 0) {
      if(flipY)
        GL.glRotated(180, 0, 1, 0);
      DrawLib.drawTexturedRectangle(textureId, width, height);
    } else {
      GL.glColor3d(color[0], color[1], color[2]);
      DrawLib.drawRectangle(width, height);
    }
    GL.glPopMatrix();
  }
  
  public boolean isFlippedOnY() { return flipY; }
}
