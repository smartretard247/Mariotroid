package Main;

import Drawing.DrawLib;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class GameObject extends Collidable {
  private static final float SPRINT_MULTIPLIER = 1.5f;
  public static float MAX_SPEED_X = PhysicsEngine.getTerminalX();
  public static float MAX_SPEED_Y = PhysicsEngine.getTerminalY();
  protected String name = "Default";
  protected float speedX; // movement increment x
  protected float speedY; // movement increment y
  private Point.Float defSpeed;
  private boolean isSprinting;
  
  public GameObject(int objId, int texId, float x, float y, float w, float h, Point.Float speed) {
    super(objId, texId, x, y, w, h);
    speedX = speed.x;
    speedY = speed.y;
    defSpeed = speed;
    isSprinting = false;
  }
  
  public GameObject(int objId, int texId, float x, float y) {
    this(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight(), new Point.Float(0, 0));
  }
  
  public GameObject() {
    this(-1, -1, 0, 0, 1, 1, new Point.Float(0, 0));
  }
  
  public float getSpeedX() { return speedX; }
  public float getSpeedY() { return speedY; }
  public void setSpeedX(float spdX) { speedX = spdX; }
  public void setSpeedY(float spdY) { speedY = spdY; }
  public void setSpeed(float spdX, float spdY) { speedX = spdX; speedY = spdY; }
  public void setSpeed(Point.Float to) { speedX = to.x; speedY = to.y; }
  public void setDefaultSpeed(Point.Float to) { defSpeed = to; }
  
  public void increaseSpeed(float deltaX, float deltaY) {
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
    if(isSprinting) toggleSprint();
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
