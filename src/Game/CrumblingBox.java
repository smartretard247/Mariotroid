package Game;

import Enumerations.TEX;
import Main.PhysicsEngine;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class CrumblingBox extends Interactive {

  public CrumblingBox(int id, int texId, float x, float y) {
    super(id, texId, x, y);
  }

  public CrumblingBox() {
    this(-1, -1, 0, 0);
  }
  
  @Override
  public void doAction() {
    // do nothing because CrumblingBox breaks only when hit by hero projectile, not hero.interact()
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
        case TEX.NONE:
        case TEX.BOX:
        case TEX.LEVEL:
          if(movingDown()) { // falling straight down
            adjustToTopOf(c);
            setSpeedY(0);
          } else if(movingUp()) { // flying straight upward
            adjustToBottomOf(c);
            setSpeedY(0);
          }
          break;
        default:
          if (this.getTextureId() == TEX.BOX){
            if(new Projectile().getClass().isInstance(c)) {
              if(!(c.getTextureId() == TEX.ENEMY_WEAPON_1 || c.getTextureId() == TEX.ENEMY_WEAPON_2)) {
                if(!isComplete()) {
                  this.setTextureId(TEX.BOX_BROKEN); // change to texture of 'crumbled' rock
                  this.setComplete(true);
                }
              }
              toRemove.add(objId);
            }
          }
          break;
      }
    }
    return toRemove;
  }
  
  @Override
  public void draw() {
    flipX = PhysicsEngine.gravityIsInverted();
    super.draw();
  }
}
