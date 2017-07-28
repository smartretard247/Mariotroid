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

  FallingBox(int id, int texId, float x, float y) {
    super(id, texId, x, y);
    weight = 0;
  }

  FallingBox() {
    this(-1, -1, 0, 0);
  }
  
  @Override
  public void doAction() {
    // do nothing because FallingBox falls when hit by hero projectile, not hero.interact()
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
      int texId = c.getTextureId();
      int objId = c.getObjectId();
      switch(texId) {
        case TEX.TEX_FLYING_BOX:
        case TEX.TEX_FALLING_BOX:
        case TEX.TEX_LEVEL:
          this.adjustToTopOf(c);
          this.setSpeedY(0);
          break;
        default:
          if(new Projectile().getClass().isInstance(c)) {
            if(!(c.getTextureId() == TEX.TEX_ENEMY_WEAPON_1 || c.getTextureId() == TEX.TEX_ENEMY_WEAPON_2)) {
              if(!isComplete()) {
                weight = 1f;
                setComplete(true);
                Engine.setStatusMessage("Brick broke free!");
              }
            }
          }
          break;
      }
    }
    return toRemove;
  }
}
