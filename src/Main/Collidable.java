package Main;

import Enumerations.DIRECTION;

/**
 *
 * @author Jeezy
 */
public class Collidable extends Drawable {
  public Collidable(int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
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
  
  /**
   * Tests an intersection with the supplied Rect.  Returns true if collision.
   * @param dest
   * @param srcSpeedX
   * @param srcSpeedY
   * @return 
   */
  public DIRECTION intersect(Rectangle dest, double srcSpeedX, double srcSpeedY) {
    if(!collidesWith(dest)) {
      return DIRECTION.NONE; // no collisions
    } else { // some collision occurred, figure it out
      if(srcSpeedY < 0) return DIRECTION.BOTTOM;
      if(srcSpeedX > 0) return DIRECTION.RIGHT;
      if(srcSpeedX < 0) return DIRECTION.LEFT;
      if(srcSpeedY > 0) return DIRECTION.TOP;
    }
    return null;
  }
}
