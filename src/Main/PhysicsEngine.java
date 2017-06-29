/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

/**
 * g = GM/r2
 * @author Jeezy
 */
public class PhysicsEngine {
  public static final int GRAVITY = 5;
  public static final int TERMINAL_VELOCITY = 300;
  public static final int TERMINAL_SPRINT = 10;

  /**
   * Apply gravity to given object.
   * @param obj 
   */
  public static void fall(GameObject obj) {
    double vertSpeed = Math.abs(obj.getSpeedY()) + GRAVITY;
    if (vertSpeed > TERMINAL_VELOCITY) {
      vertSpeed = TERMINAL_VELOCITY;
    }
    obj.setSpeedY(-vertSpeed);
  }
}
