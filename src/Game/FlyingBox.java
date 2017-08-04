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
public class FlyingBox extends Interactive {

  public FlyingBox(int id, int texId, float x, float y) {
    super(id, texId, x, y);
    PhysicsEngine.addDrag(this);
  }

  public FlyingBox() {
    this(-1, -1, 0, 0);
  }
  
  @Override
  public void doAction() {
    // do nothing because FlyingBox flys off when hit by hero projectile, not hero.interact()
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
          if(movingDown()) { // falling straight down
            adjustToTopOf(c);
            setSpeedY(0);
          } else if(movingDownAndRight()) { // falling right and down
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToLeftOf(c);
            } else {
              adjustToTopOf(c);
              setSpeedY(0);
            }
          } else if(movingDownAndLeft()) { // falling left and down
            if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToRightOf(c);
            } else {
              adjustToTopOf(c);
              setSpeedY(0);
            }
          } else if(movingLeft()) { // moving left
            adjustToRightOf(c);
          } else if(movingRight()) { // moving right
            adjustToLeftOf(c);
          } else if(movingUpAndLeft()) { // flying upward and to the left
            if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getBottom() - getTop())) {
              adjustToRightOf(c);
            } else {
              adjustToBottomOf(c);
              setSpeedY(0);
            }
          } else if(movingUpAndRight()) { // flying upward and to the right
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
              adjustToLeftOf(c);
            } else {
              adjustToBottomOf(c);
              setSpeedY(0);
            }
          } else if(movingUp()) { // flying straight upward
            adjustToBottomOf(c);
            setSpeedY(0);
          }
          break;
        default:
          if(new Projectile().getClass().isInstance(c)) {
            if(!(c.getTextureId() == TEX.ENEMY_WEAPON_1 || c.getTextureId() == TEX.ENEMY_WEAPON_2)) {
              if(!isComplete()) {
                Projectile p = (Projectile)c;
                setSpeed(p.getSpeed());
              }
            }
            toRemove.add(objId);
          }
          break;
      }
    }
    return toRemove;
  }
}
