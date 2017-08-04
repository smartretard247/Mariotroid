package Game;

import Enumerations.TEX;
import Main.Engine;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class FallingBox extends Interactive {

  public FallingBox(int id, int texId, float x, float y) {
    super(id, texId, x, y);
    weight = 0;
  }

  public FallingBox() {
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
        case TEX.BOX:
        case TEX.LEVEL:
          if(movingDownIgnoreX()) {
            this.adjustToTopOf(c);
            this.setSpeedY(0);
          }
          break;
        default:
          if(new Projectile().getClass().isInstance(c)) {
            if(!(c.getTextureId() == TEX.ENEMY_WEAPON_1 || c.getTextureId() == TEX.ENEMY_WEAPON_2)) {
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
