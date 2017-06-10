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
  
  public static final int SPRINT = 10;
  public static final int TERMINAL_SPRINT = 40;

  /**
   * Apply gravity to given object.
   * @param obj 
   */
  public void fall(GameObject obj) {
    double vertSpeed = Math.abs(obj.getSpeedY()) + GRAVITY;
    if (vertSpeed > TERMINAL_VELOCITY) {
      vertSpeed = TERMINAL_VELOCITY;
    }
    //obj.setY((obj.getSpeedY() < 0) ? obj.getY() + vertSpeed : obj.getY() - vertSpeed);
    obj.setSpeedY(-vertSpeed);
  }
}
