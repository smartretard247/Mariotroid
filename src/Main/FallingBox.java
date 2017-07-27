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
    weight = 0;
  }

  FallingBox() {
    this(-1, -1, -1, 0, 0);
  }
  
  @Override
  public void doAction() {
    weight = 1f;
  }
  
  /**
   * Handle collision detection
   * @param nearObjects
   * @return a list of ids that need to be removed from the scene
   */
  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects) {
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Integer> toRemove = new LinkedList<>();
    for(Collidable c : collisions) {
      int collisiontexId = c.getTextureId();
      switch(collisiontexId) {
      case TEX.TEX_LEVEL:
        this.adjustToTopOf(c);
        this.setSpeedY(0);
        break;
      default:
        break;
      }
    }
    return toRemove;
  }
}
