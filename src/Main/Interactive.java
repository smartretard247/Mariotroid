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
  private final int selectedTexId;
  private final int deselectedTexId;
  
  public Interactive(int objId, int texId, int selTexId, float x, float y, float w, float h) {
    super(objId, texId, x, y, w, h);
    deselectedTexId = texId;
    selectedTexId = selTexId;
  }
  
  public Interactive(int objId, int texId, int selTexId, float x, float y) {
    this(objId, texId, selTexId, x, y, (texId >= 0) ? DrawLib.getTexture(texId).getWidth() : 1, (texId >= 0) ? DrawLib.getTexture(texId).getHeight() : 1);
  }
  
  public Interactive(int x, int y) {
    this(-1, TEX.TEX_NONE, TEX.TEX_NONE, x, y, 1, 1);
  }
  
  public Interactive() {
    this(-1, TEX.TEX_NONE, TEX.TEX_NONE, 0, 0, 1, 1);
  }
  
  public boolean isSelected() { return selected; }
  public boolean isComplete() { return interactionComplete; }
  public void setComplete(boolean to) { interactionComplete = to; }
  
  public void select() {
    if(!selected) {
      this.setTextureId(selectedTexId);
      selected = true;
    }
  }
  
  public void deselect() {
    if(selected) {
      this.setTextureId(deselectedTexId);
      selected = false;
    }
  }
  
  /**
   * Performs some action.
   */
  public abstract void doAction();
}
