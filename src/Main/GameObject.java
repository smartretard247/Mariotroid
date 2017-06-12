package Main;

import Enumerations.DIRECTION;

/**
 *
 * @author Jeezy
 */
public class GameObject {
  private static final double SPRINT = 10;
  private static final int SPRINT_MULTIPLIER = 2;
  private double MAX_SPEED_X = PhysicsEngine.TERMINAL_SPRINT;
  private double MAX_SPEED_Y = PhysicsEngine.TERMINAL_VELOCITY;
  
  public GameObject(double x, double y, double w, double h) {
    X = x;
    Y = y;
    defX = x;
    defY = y;
    width = w;
    height = h;
    speedX = 0.0;
    speedY = 0.0;
  }
  
  protected String name = "Default";
  
  private double X; // for moving x direction
  private double Y; // for moving y direction
  private double speedX; // movement increment x
  private double speedY; // movement increment y
  private double defX; // can only change if new level, or 'continue' option chosen
  private double defY;
  
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
          case TOP: //System.out.println("Collision from top detected.");
            Y = near.getBottom() - height/2; break; // collision on top, set to bottom of near object
          case BOTTOM: //System.out.println("Collision from bottom detected.");
            Y = near.getTop() + height/2; break; // collision on bottom
          case LEFT: //System.out.println("Collision from left side detected.");
            X = near.getRight() + width/2; break; // collision at left side
          case RIGHT: //System.out.println("Collision from right side detected.");
            X = near.getLeft() - width/2; break; // collision at right side
          default: System.out.println("Invalid intersection.") ; break;
        }
      }
    }
  }
  public void increaseSpeed(double deltaX, double deltaY) {
    speedX += deltaX;
    speedY += deltaY;
    if(Math.abs(speedX) > MAX_SPEED_X)
      speedX = (speedX < 0) ? -MAX_SPEED_X : MAX_SPEED_X;
    if(Math.abs(speedY) > MAX_SPEED_Y)
      speedY = (speedY < 0) ? -MAX_SPEED_Y : MAX_SPEED_Y;
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
    boolean impossibleRight = src.x() > dest.x() + dest.w(); // src is right of dest
    boolean impossibleLeft =  src.x() + src.w() < dest.x(); // src is left of dest
    boolean impossibleTop =  src.y() < dest.y() - dest.h(); // dest is above src
    boolean impossibleBottom = src.y() - src.h() > dest.y(); // src is above dest
    if(impossibleRight || impossibleLeft || impossibleTop || impossibleBottom) {
      return DIRECTION.NONE; // no collisions
    } else { // some collision occurred, figure it out
      if(speedY < 0) return DIRECTION.BOTTOM;
      if(speedX > 0) return DIRECTION.RIGHT;
      if(speedX < 0) return DIRECTION.LEFT;
      if(speedY > 0) return DIRECTION.TOP;
      
    }
    return null;
  }
  
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
  
  public void resetPosition() {
    this.setPosition(defX, defY);
  }
  
  public void resetAll() {
    this.setPosition(defX, defY);
    this.setSpeed(0, 0);
  }
  
  public String getName() { return name; }
  public void setName(String to) { name = to; }
  
  /**
   * Toggles between maximum running speeds.
   */
  public void toggleSprint() {
    if(MAX_SPEED_X == PhysicsEngine.TERMINAL_SPRINT)
      MAX_SPEED_X *= SPRINT_MULTIPLIER;
    else
      MAX_SPEED_X /= SPRINT_MULTIPLIER;
  }
  
  public boolean isSprinting() {
    return (MAX_SPEED_X > PhysicsEngine.TERMINAL_SPRINT);
  }
}
