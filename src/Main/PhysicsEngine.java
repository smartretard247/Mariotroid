package Main;

import java.util.ArrayList;

/**
 * g = GM/r2
 * @author Jeezy
 */
public class PhysicsEngine {
  private static final ArrayList<Heavy> HEAVY_OBJS = new ArrayList<>();
  private static int gravity = -10;
  private static final ArrayList<Heavy> DRAG_OBJS = new ArrayList<>();
  private static int drag = 2;
  
  private static final int DEFAULT_GRAVITY = -10;
  private static final int TERMINAL_VELOCITY = 300;
  private static final int TERMINAL_SPRINT = 20;

  /**
   * Apply gravity to all heavy objects.
   */
  public static void fall() {
    HEAVY_OBJS.forEach((h) -> {
      float vertSpeed = h.getSpeedY() + (gravity * h.getWeight());
      if (Math.abs(vertSpeed) > TERMINAL_VELOCITY) {
        vertSpeed = (vertSpeed < 0) ? -TERMINAL_VELOCITY : TERMINAL_VELOCITY;
      }
      h.setSpeedY(vertSpeed);
    });
  }
  
  /**
   * Apply drag to all heavy objects.
   */
  public static void drag() {
    DRAG_OBJS.forEach((h) -> {
      boolean movingLeft = h.getSpeedX() < 0;
      float horzSpeed = Math.abs(h.getSpeedX()) - (drag * h.getWeight());
      if (horzSpeed <= 0) {
        horzSpeed = 0;
      } else {
        horzSpeed = movingLeft ? -horzSpeed : horzSpeed;
      }
      h.setSpeedX(horzSpeed);
    });
  }
  
  /**
   * Adds the supplied movable object to a list of all objects to which gravity will be applied.
   * @param h 
   */
  public static void addHeavy(Heavy h) {
    if(h != null && !HEAVY_OBJS.contains(h)) HEAVY_OBJS.add(h);
  }
  
  public static void removeHeavy(Heavy h) {
    if(HEAVY_OBJS.contains(h)) HEAVY_OBJS.remove(h);
  }
  
  public static void addDrag(Heavy h) {
    if(h != null && !DRAG_OBJS.contains(h)) DRAG_OBJS.add(h);
  }
  
  public static void inverseGravity() { gravity = -gravity; }
  public static int getGravity() { return gravity; }
  public static void setGravity(int to) { gravity = to; }
  public static void resetGravity() { gravity = DEFAULT_GRAVITY; }
  public static boolean gravityIsInverted() { return gravity >= 0; }
  public static int getTerminalX() { return TERMINAL_SPRINT; }
  public static int getTerminalY() { return TERMINAL_VELOCITY; }
}
