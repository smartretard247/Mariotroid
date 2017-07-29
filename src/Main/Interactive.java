package Main;

import Drawing.DrawLib;
import Enumerations.TEX;

/**
 *
 * @author Jeezy
 */
public abstract class Interactive extends Heavy {
  private boolean interactionComplete = false;
  private boolean selected = false;
  
  public Interactive(int objId, int texId, float x, float y, float w, float h) {
    super(objId, texId, x, y, w, h);
  }
  
  public Interactive(int objId, int texId, float x, float y) {
    this(objId, texId, x, y, (texId >= 0) ? DrawLib.getTexture(texId).getWidth() : 1, (texId >= 0) ? DrawLib.getTexture(texId).getHeight() : 1);
  }
  
  public Interactive(int x, int y) {
    this(-1, TEX.NONE, x, y, 1, 1);
  }
  
  public Interactive() {
    this(-1, TEX.NONE, 0, 0, 1, 1);
  }
  
  public boolean isSelected() { return selected; }
  public boolean isComplete() { return interactionComplete; }
  public void setComplete(boolean to) { interactionComplete = to; }
  
  public void select() {
    if(!selected) {
      setColor(1f, 1f, 0);
      selected = true;
    }
  }
  
  public void deselect() {
    if(selected) {
      setColor(1f, 1f, 1f);
      selected = false;
    }
  }
  
  /**
   * Perform some action.
   */
  public abstract void doAction();
}
