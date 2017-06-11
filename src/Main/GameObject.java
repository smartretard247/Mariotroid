package Main;

/**
 *
 * @author Jeezy
 */
public class GameObject {
  private static double maxSpeedX = PhysicsEngine.TERMINAL_SPRINT;
  private static double maxSpeedY = PhysicsEngine.TERMINAL_VELOCITY;
  
  public GameObject(double x, double y, int w, int h, double speedx, double speedy) {
    X = x;
    Y = y;
    speedX = 0.0;
    speedY = 0.0;
    width = w;
    height = h;
  }
  public GameObject() {
    this(0.0, 0.0, 50, 50, 0.0, 0.0);
  }
  
  protected String name = "Default";
  
  protected double X; // for moving x direction
  protected double Y; // for moving y direction
  private double speedX; // movement increment x
  private double speedY; // movement increment y
  
  protected double width, height; // for building the collision rect
  
  public double getX() { return X; }
  public double getY() { return Y; }
  public double getW() { return width; }
  public double getH() { return height; }
  public double getSpeedX() { return speedX; }
  public double getSpeedY() { return speedY; }
  
  public double getLeft() { return X-width/2; }
  public double getRight() { return X+width/2; }
  public double getBottom() { return Y-height/2; }
  public double getTop() { return Y+height/2; }
  
  public void setX(double posX) { X = posX; }
  public void setY(double posY) { Y = posY; }
  public void setPosition(double posX, double posY) { X = posX; Y = posY; }
  public void setW(int w) { width = w; }
  public void setH(int h) { height = h; }
  public void setDimensions(int w, int h) { width = w; height = h; }
  public void setSpeedX(double spdX) { speedX = spdX; }
  public void setSpeedY(double spdY) { speedY = spdY; }
  public void setSpeed(double spdX, double spdY) { speedX = spdX; speedY = spdY; }
  
  public void move(GameObject[] nearObjects) {
    X += speedX;
    Y += speedY;
    
    for(GameObject near : nearObjects) {
      DIRECTION ofCollision = this.intersect(near.getCollisionRect());
      if(ofCollision != null) {
        switch(ofCollision) {
          case NONE:
            if(speedY <= 0) PhysicsEngine.fall(this);// apply gravity
            return; // no collision so move is valid
          case TOP: System.out.println("Collision from top detected.");
            Y = near.getBottom() - height/2; break; // collision on top, set to bottom of near object
          case BOTTOM: System.out.println("Collision from bottom detected.");
            Y = near.getTop() + height/2; break; // collision on bottom
          case LEFT: System.out.println("Collision from left side detected.");
            X = near.getRight() + width/2; break; // collision at left side
          case RIGHT: System.out.println("Collision from right side detected.");
            X = near.getLeft() - width/2; break; // collision at right side
          default: System.out.println("Invalid intersection.") ;break;
        }
      }
    }
  }
  public void increaseSpeed(double deltaX, double deltaY) {
    speedX += deltaX;
    speedY += deltaY;
    if(Math.abs(speedX) > maxSpeedX)
      speedX = (speedX < 0) ? -maxSpeedX : maxSpeedX;
    if(Math.abs(speedY) > maxSpeedY)
      speedY = (speedY < 0) ? -maxSpeedY : maxSpeedY;
  }
  
  public Rectangle getCollisionRect() {
    return new Rectangle(X-width/2, Y+height/2, width, height);
  }
  
  /**
   * Tests an intersection with the supplied Rect.  Returns true if collision.
   * @param dest
   * @return 
   */
  public DIRECTION intersect(Rectangle dest) {
    Rectangle src = this.getCollisionRect();
    if(src.x() > dest.x() + dest.w() // src is right of dest
            || src.x() + src.w() < dest.x() // src is left of dest
            || src.y() < dest.y() - dest.h() // dest is above src
            || src.y() - src.h() > dest.y()) { // src is above dest
      return DIRECTION.NONE;
    } else { // some collision occurred, figure it out
      // check if objects are in same y space
      /*if(((src.y() < dest.y()) && (src.y() > dest.y() - dest.h()))
              || ((src.y() - src.h() < dest.y()) && (src.y() - src.h() > dest.y() - dest.h()))) {
        if(src.x() + src.w() > dest.x()) {
          return DIRECTION.RIGHT;
        }
        if(src.x() < dest.x() + dest.w()) {
          return DIRECTION.LEFT;
        }
      }
      
      // check if objects are contain the same x space
      if(((src.x() > dest.x()) && (src.x() < dest.x() + dest.w()))
              || ((src.x() + src.w() > dest.x()) && (src.x() + src.w() < dest.x() + dest.w()))) {
        if(src.y() - src.h() < dest.y()) {
          return DIRECTION.BOTTOM;
        }
        if(src.y() > dest.y() - dest.h()) {
          return DIRECTION.TOP;
        }
      }*/
    }
    return null;
  }
  
  public void resetAll() {
    this.setPosition(0.0, 0.0);
    this.setSpeed(0, 0);
  }
  
  public String getName() { return name; }
  public void setName(String to) { name = to; }
}
