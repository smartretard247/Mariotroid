package Main;

import Drawing.DrawLib;

/**
 *
 * @author Jeezy
 */
public class Collidable extends Drawable {
  private final int objectId;
  
  public Collidable(int objId, int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
    objectId = objId;
  }
  
  public Collidable(int objId, int texId, double x, double y) {
    this(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
  }
  
  public Collidable(int objId, int texId) {
    this(objId, texId, 0, 0);
  }
  
  public Collidable() {
    this(-1, -1, 0, 0, 0, 0);
  }
  
  /**
   * Returns a rectangle surrounding the object.
   * @return 
   */
  public Rectangle getBoundary() {
    return new Rectangle(x-width/2, y+height/2, width, height);
  }
  
  /**
   * Test if this object's boundary collides with supplied rectangle.
   * @param dest
   * @return 
   */
  public boolean collidesWith(Rectangle dest) {
    Rectangle src = this.getBoundary();
    return !(src.x() > dest.x() + dest.w() // src is right of dest
            || src.x() + src.w() < dest.x() // src is left of dest
            || src.y() < dest.y() - dest.h() // dest is above src
            || src.y() - src.h() > dest.y()); // src is above dest
  }
  
  public final int getObjectId() { return objectId; }
}
