package Main;

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
      setColor(1f, 0.5f, 1f);
      setColorNormal(1f, 0.5f, 1f);
      setComplete(true);
    }
  }
}
