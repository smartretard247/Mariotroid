package Main;

/**
 *
 * @author Jeezy
 */
public class Collidable extends Drawable {
  public Collidable(int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
  }
  
  public Collidable(int texId, double x, double y) {
    super(texId, x, y, 1, 1);
  }
  
  public Collidable(int texId) {
    super(texId, 0, 0, 1, 1);
  }
  
  public Collidable() {
    super(-1, 0, 0, 0, 0);
  }
  
  /**
   * Returns a rectangle surrounding the object.
   * @return 
   */
  public Rectangle getBoundary() {
    return new Rectangle(X-width/2, Y+height/2, width, height);
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
}
