package Main;

import Enumerations.TEX;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class FallingBox extends Interactive {

  FallingBox(int id, int texId, int selTexId, float x, float y) {
    super(id, texId, selTexId, x, y);
  }
  
  @Override
  public void doAction() {
    PhysicsEngine.fall(this);
  }
  
  public List<Collidable> processCollisions(ArrayList<Collidable> nearObjects) {
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Collidable> invalidCollisions = new LinkedList<>();
    for(Collidable c : collisions) {
      int collisiontexId = c.getTextureId();
      switch(collisiontexId) {
      case TEX.TEX_LEVEL: // do nothing
        this.adjustToTopOf(c);
        this.setSpeedY(0);
        break;
      default:
        invalidCollisions.add(c); // remove all but level collisions, will be processed be other classes
        break;
      }
    }
    collisions.removeAll(invalidCollisions);
    return collisions;
  }
}
