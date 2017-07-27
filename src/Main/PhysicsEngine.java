package Main;

import java.util.ArrayList;

/**
 * g = GM/r2
 * @author Jeezy
 */
public class PhysicsEngine {
  private static final ArrayList<Heavy> HEAVY_OBJS = new ArrayList<>();
  private static int gravity = -10;
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
   * Adds the supplied movable object to a list of all objects to which gravity will be applied.
   * @param m 
   */
  public static void addHeavy(Heavy m) {
    if(m != null) HEAVY_OBJS.add(m);
  }
  
  public static void removeHeavy(Heavy m) {
    if(HEAVY_OBJS.contains(m)) HEAVY_OBJS.remove(m);
  }
  
  public static void inverseGravity() { gravity = -gravity; }
  public static int getGravity() { return gravity; }
  public static void setGravity(int to) { gravity = to; }
  public static void resetGravity() { gravity = DEFAULT_GRAVITY; }
  public static boolean gravityIsInverted() { return gravity >= 0; }
  public static int getTerminalX() { return TERMINAL_SPRINT; }
  public static int getTerminalY() { return TERMINAL_VELOCITY; }
}
