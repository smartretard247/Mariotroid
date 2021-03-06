package Game;

import Drawing.DrawLib;
import com.jogamp.opengl.GL2;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class Drawable {
  public final static GL2 GL = DrawLib.gl;
  private int textureId;
  protected float x; // for moving x direction
  protected float y; // for moving y direction
  protected float width, height; // for building the collision rect
  private float defX;
  private float defY;
  protected boolean flipX, flipY;
  public float color[]; // rgb color tint
  
  /**
   * Creates a complete drawable object.
   * @param texId texture ID
   * @param xPos x coordinate
   * @param yPos y coordinate
   * @param w width
   * @param h height
   */
  public Drawable(int texId, float xPos, float yPos, float w, float h) {
    textureId = texId;
    x = xPos;
    y = yPos;
    defX = x;
    defY = y;
    width = w;
    height = h;
    flipX = false;
    flipY = false;
    color = new float[] { 1.0f, 1.0f, 1.0f };
  }
  
  /**
   * Creates a drawable object, gets width and height using texture ID.
   * @param texId texture ID
   * @param x x coordinate
   * @param y y coordinate
   */
  public Drawable(int texId, float x, float y) {
    this(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
  }
  
  public int getTextureId() { return textureId; }
  public float getX() { return x; }
  public float getY() { return y; }
  public Point.Float getPosition() { return new Point.Float(x, y); }
  public float getW() { return width; }
  public float getH() { return height; }
  public float getLeft() { return x-width/2; }
  public float getRight() { return x+width/2; }
  public float getBottom() { return y-height/2; }
  public float getTop() { return y+height/2; }
  public void setTextureId(int id) { textureId = id; }
  public void setX(float posX) { x = posX; }
  public void setY(float posY) { y = posY; }
  public void setPosition(float posX, float posY) { x = posX; y = posY; }
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
  public void setDefaultPosition(float x, float y) {
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
  
  /**
   * Call this to make images flip along the x-axis, making them seem to flip upside down.
   * @param to
   */
  public void setFlipX(boolean to) { flipX = to; }
  
  /**
   * Sets the objects RGB color while returning the old color.
   * @param r red component
   * @param g green component
   * @param b blue component
   * @return float[] of object's old RGB color
   */
  public float[] setColor(float r, float g, float b) {
    color[0] = r;
    color[1] = g;
    color[2] = b;
    return new float[] { r, g, b };
  }
  public float[] setColor(float[] to) {
    return setColor(to[0], to[1], to[2]);
  }
  public float[] getColor() { return color; }
  
  /**
   * Draws the game object using a valid textureId, if one is not present it will draw a plain
   * rectangle in its place.
   */
  public void draw() {
    GL.glPushMatrix();
    GL.glTranslated(getX(), getY(), 0);
    GL.glColor3f(color[0], color[1], color[2]);
    if(textureId >= 0) {
      if(flipY)
        GL.glRotated(180, 0, 1, 0);
      if(flipX)
        GL.glRotated(180, 1, 0, 0);
      DrawLib.drawTexturedRectangle(textureId, width, height);
    } else {
      DrawLib.drawRectangle(width, height);
    }
    GL.glPopMatrix();
  }
  
  public boolean isFlippedOnY() { return flipY; }
  public boolean isFlippedOnX() { return flipX; }
}
