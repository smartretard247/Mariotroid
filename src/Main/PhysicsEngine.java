package Main;

/**
 * g = GM/r2
 * @author Jeezy
 */
public class PhysicsEngine {
  private static int gravity = 5;
  public static final int TERMINAL_VELOCITY = 300;
  public static final int TERMINAL_SPRINT = 20;

  /**
   * Apply gravity to given object.
   * @param obj 
   */
  public static void fall(GameObject obj) {
    double vertSpeed = Math.abs(obj.getSpeedY()) + gravity;
    if (vertSpeed > TERMINAL_VELOCITY) {
      vertSpeed = TERMINAL_VELOCITY;
    }
    obj.setSpeedY(-vertSpeed);
  }
  
  public static void inverseGravity() {
    gravity = -gravity;
  }
  
  public static boolean gravityIsInverted() { return gravity < 0; }
  
  public static void resetGravity() { gravity = Math.abs(gravity); }
  
  public static int getGravity() { return gravity; }
}
