package Main;

import Drawing.DrawLib;

/**
 *
 * @author Jeezy
 */
public class Interactive extends Collidable {
  private boolean selected = false;
  private final int selectedTexId;
  private final int deselectedTexId;
  
  public Interactive(int objId, int texId, int selTexId, double x, double y, double w, double h) {
    super(objId, texId, x, y, w, h);
    deselectedTexId = texId;
    selectedTexId = selTexId;
  }
  
  public Interactive(int objId, int texId, double x, double y) {
    this(objId, texId, texId, x, y, (texId >= 0) ? DrawLib.getTexture(texId).getWidth() : 1, (texId >= 0) ? DrawLib.getTexture(texId).getHeight() : 1);
  }
  
  public Interactive(int x, int y) {
    this(-1, DrawLib.TEX_NONE, DrawLib.TEX_NONE, x, y, 1, 1);
  }
  
  public Interactive() {
    this(-1, DrawLib.TEX_NONE, DrawLib.TEX_NONE, 0, 0, 1, 1);
  }
  
  public boolean isSelected() { return selected; }
  
  public void select() {
    selected = true;
    this.setTextureId(selectedTexId);
  }
  
  public void deselect() {
    selected = false;
    this.setTextureId(deselectedTexId);
  }
}
