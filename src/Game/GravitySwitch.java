package Game;

import Main.Engine;
import Main.PhysicsEngine;

/**
 *
 * @author Jeezy
 */
public class GravitySwitch extends Interactive {

  public GravitySwitch(int id, int texId, int x, int y) {
    super(id, texId, x, y);
    weight = 0;
  }

  @Override
  public void doAction() {
    if(!isComplete()) {
      Engine.setStatusMessage("Gravity reversed!");
      PhysicsEngine.inverseGravity();
      setColor(1f, 0.5f, 1f); // deactivated color
      setColorNormal(1f, 0.5f, 1f); // deactivated color
      Engine.resetGravSwitches();
      setComplete(true);
    }
  }
<<<<<<< HEAD
}
=======
  
  public void toggleOff(){
      setColor(new float[] { 0.5f, 0.3f, 0.8f });
      setColorNormal(1f, 1f, 1f);
      setComplete(false);
  }
}
>>>>>>> f7345177cbc7f8ab84f078ac97a00f2a69ccfc4e
