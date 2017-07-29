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
  private final float[] selColor;
  private final float[] normalColor ;
  
  public Interactive(int objId, int texId, float x, float y, float w, float h, float[] sColor) {
    super(objId, texId, x, y, w, h);
    normalColor = new float[] { 1f, 1f, 1f };
    selColor = sColor;
  }
  
  public Interactive(int objId, int texId, float x, float y, float w, float h) {
    this(objId, texId, x, y, w, h, new float[] { 0.5f, 0.3f, 0.8f });
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
    if(!selected && !interactionComplete) {
      setColor(selColor);
      selected = true;
    }
  }
  
  public void deselect() {
    if(selected) {
      setColor(normalColor);
      selected = false;
    }
  }
  
  public void setColorSelected(float r, float g, float b) { 
    selColor[0] = r;
    selColor[1] = g;
    selColor[2] = b;
  }
  
  public void setColorNormal(float r, float g, float b) { 
    normalColor[0] = r;
    normalColor[1] = g;
    normalColor[2] = b;
  }
  
  /**
   * Perform some action.
   */
  public abstract void doAction();
}
