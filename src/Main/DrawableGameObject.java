package Main;

import com.jogamp.opengl.GL2;

/**
 *
 * @author Jeezy
 */
public class DrawableGameObject extends GameObject {
  private final static GL2 gl = DrawLib.gl;
  private int textureId = -1;
  
  public DrawableGameObject(int texId, double x, double y, double w, double h) {
    super(x, y, w, h);
    textureId = texId;
  }
  
  /**
   * Draws the game object using a valid textureId, if one is not present it will draw a plain
   * rectangle in its place.
   */
  public void draw() {
    gl.glPushMatrix();
    gl.glTranslated(getX(), getY(), 0);
    if(textureId >= 0)
      DrawLib.drawTexturedRectangle(textureId);
    else
      DrawLib.drawRectangle(width, height);
    gl.glPopMatrix();
  }
}
