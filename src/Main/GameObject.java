package Main;

import Drawing.DrawLib;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class GameObject extends Collidable {
  private static final double SPRINT_MULTIPLIER = 1.5;
  public static double MAX_SPEED_X = PhysicsEngine.TERMINAL_SPRINT;
  public static double MAX_SPEED_Y = PhysicsEngine.TERMINAL_VELOCITY;
  protected String name = "Default";
  protected double speedX; // movement increment x
  protected double speedY; // movement increment y
  private Point.Double defSpeed;
  private boolean isSprinting;
  
  public GameObject(int objId, int texId, double x, double y, double w, double h, Point.Double speed) {
    super(objId, texId, x, y, w, h);
    speedX = speed.x;
    speedY = speed.y;
    defSpeed = speed;
    isSprinting = false;
  }
  
  public GameObject(int objId, int texId, double x, double y) {
    this(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight(), new Point.Double(0, 0));
  }
  
  public GameObject() {
    this(-1, -1, 0, 0, 1, 1, new Point.Double(0, 0));
  }
  
  public double getSpeedX() { return speedX; }
  public double getSpeedY() { return speedY; }
  public void setSpeedX(double spdX) { speedX = spdX; }
  public void setSpeedY(double spdY) { speedY = spdY; }
  public void setSpeed(double spdX, double spdY) { speedX = spdX; speedY = spdY; }
  public void setSpeed(Point.Double to) { speedX = to.x; speedY = to.y; }
  public void setDefaultSpeed(Point.Double to) { defSpeed = to; }
  
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
    this.setSpeed(defSpeed);
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
    if(speedX != 0)
      speedX = (speedX > 0) ? MAX_SPEED_X : -MAX_SPEED_X;
  }
  
  public void setSprinting(boolean to) { isSprinting = to; }
  public boolean isSprinting() { return isSprinting; }
}
