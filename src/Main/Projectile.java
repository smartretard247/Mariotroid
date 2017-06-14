package Main;

import Drawing.DrawLib;

/**
 *
 * @author Jeezy
 */
public class Projectile extends Collidable {
  private int zRot;
  
  public Projectile(int texId, double x, double y, double w, double h, int zrot) {
    super(texId, x, y, w, h);
    zRot = zrot;
  }
  
  public void setZRot(int to) {
    zRot = to;
  }
  
  @Override
  public void draw() {
    GL.glPushMatrix();
    GL.glTranslated(getX(), getY(), 0);
    GL.glRotated(zRot, 0, 0, 1); // will rotate based on clicked position
    if(this.getTextureId() >= 0) {
      DrawLib.drawTexturedRectangle(this.getTextureId());
    } else {
      GL.glColor3d(color[0], color[1], color[2]);
      DrawLib.drawLine(X, Y, width, height);
    }
    GL.glPopMatrix();
  }
}
