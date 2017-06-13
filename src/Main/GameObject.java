package Main;

import Enumerations.DIRECTION;
import java.util.ArrayList;

/**
 *
 * @author Jeezy
 */
public class GameObject extends Collidable {
  private static final double SPRINT = 20;
  private static final int SPRINT_MULTIPLIER = 4;
  private double MAX_SPEED_X = PhysicsEngine.TERMINAL_SPRINT;
  private double MAX_SPEED_Y = PhysicsEngine.TERMINAL_VELOCITY;
  protected String name = "Default";
  private double speedX; // movement increment x
  private double speedY; // movement increment y
  private boolean isSprinting;
  
  public GameObject(int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
    speedX = 0.0;
    speedY = 0.0;
    isSprinting = false;
  }
  
  public double getSpeedX() { return speedX; }
  public double getSpeedY() { return speedY; }
  public void setSpeedX(double spdX) { speedX = spdX; }
  public void setSpeedY(double spdY) { speedY = spdY; }
  public void setSpeed(double spdX, double spdY) { speedX = spdX; speedY = spdY; }
  
  /**
   * Moves object if no collisions detected with nearObjects.  Returns direction of collions if one
   * is found, otherwise returns DIRECTION.NONE;
   * @param nearObjects
   * @return 
   */
  public ArrayList<Collidable> move(Collidable[] nearObjects) {
    ArrayList<Collidable> collisions = new ArrayList<>();
    X += speedX;
    Y += speedY;
    
    if(speedX != 0) setFlipY(speedX < 0); // this reverses the sprite with direction changes
    
    for(Collidable near : nearObjects) {
      DIRECTION ofCollision = this.intersect(near.getBoundary(), speedX, speedY);
      if(ofCollision != null) {
        switch(ofCollision) {
          case NONE: break; // continue looking through objects for collision
          case TOP: //System.out.println("Collision from top detected.");
            Y = near.getBottom() - height/2;
            collisions.add(near);
            break;
          case BOTTOM: //System.out.println("Collision from bottom detected.");
            Y = near.getTop() + height/2;
            collisions.add(near); // collision on bottom
            break;
          case LEFT: //System.out.println("Collision from left side detected.");
            X = near.getRight() + width/2;
            collisions.add(near); // collision at left side
            break;
          case RIGHT: //System.out.println("Collision from right side detected.");
            X = near.getLeft() - width/2;
            collisions.add(near); // collision at right side
            break;
          default: System.out.println("Invalid intersection.") ; return null;
        }
      }
    }
    if(speedY <= 0 && collisions.isEmpty()) PhysicsEngine.fall(this);// apply gravity
    return collisions;
  }
  public void increaseSpeed(double deltaX, double deltaY) {
    speedX += deltaX;
    speedY += deltaY;
    if(Math.abs(speedX) > MAX_SPEED_X)
      speedX = (speedX < 0) ? -MAX_SPEED_X : MAX_SPEED_X;
    if(Math.abs(speedY) > MAX_SPEED_Y)
      speedY = (speedY < 0) ? -MAX_SPEED_Y : MAX_SPEED_Y;
  }
  
  public void resetAll() {
    this.resetPosition();
    this.setSpeed(0, 0);
  }
  
  public String getName() { return name; }
  public void setName(String to) { name = to; }
  
  /**
   * Toggles between maximum running speeds.
   */
  public void toggleSprint() {
    if(!isSprinting) {
      MAX_SPEED_X *= SPRINT_MULTIPLIER;
      isSprinting = true;
    } else {
      MAX_SPEED_X /= SPRINT_MULTIPLIER;
      isSprinting = false;
    }
  }
  
  public void setSprinting(boolean to) { isSprinting = to; }
  public boolean isSprinting() { return isSprinting; }
}
