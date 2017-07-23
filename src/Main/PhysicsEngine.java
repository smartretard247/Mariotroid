package Main;

/**
 * g = GM/r2
 * @author Jeezy
 */
public class PhysicsEngine {
  private static int gravity = 5;
  private static final int DEFAULT_GRAVITY = 5;
  private static final int TERMINAL_VELOCITY = 300;
  private static final int TERMINAL_SPRINT = 20;

  /**
   * Apply gravity to given object.
   * @param obj 
   */
  public static void fall(GameObject obj) {
    float vertSpeed = Math.abs(obj.getSpeedY()) + gravity;
    if (vertSpeed > TERMINAL_VELOCITY) {
      vertSpeed = TERMINAL_VELOCITY;
    }
    obj.setSpeedY(-vertSpeed);
  }
  
  public static void inverseGravity() { gravity = -gravity; }
  public static int getGravity() { return gravity; }
  public static void setGravity(int to) { gravity = to; }
  public static void resetGravity() { gravity = DEFAULT_GRAVITY; }
  public static boolean gravityIsInverted() { return gravity < 0; }
  
  public static int getTerminalX() { return TERMINAL_SPRINT; }
  public static int getTerminalY() { return TERMINAL_VELOCITY; }
}
