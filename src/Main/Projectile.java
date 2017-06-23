package Main;

import Drawing.DrawLib;

/**
 *
 * @author Jeezy
 */
public class Projectile extends Collidable {
  private int zRot;
  private double speedX, speedY; // speed will be calculated by rise/run
  
  public Projectile(int texId, int zrot, double x, double y, double w, double h, double sX, double sY, boolean flipY) {
    super(texId, x, y, w, h);
    speedX = sX;
    speedY = sY;
    this.flipY = flipY;
    setColor(1.0, 0.0, 0.0);
    zRot = zrot;
  }
  
  public Projectile(int texId, int zrot, double x, double y, double sX, double sY, boolean flipY) {
    super(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getWidth());
    speedX = sX;
    speedY = sY;
    this.flipY = flipY;
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
      GL.glRotated(flipY ? 90 : -90, 0, 0, 1); // rotate it 90 degress onto its side
      DrawLib.drawTexturedRectangle(this.getTextureId(), width*3, height*3);
    } else {
      GL.glColor3d(color[0], color[1], color[2]);
      GL.glLineWidth((float) height);
      if(flipY) 
        DrawLib.drawLine(0, 0, -width, 0);
      else
        DrawLib.drawLine(0, 0, width, 0);
    }
    GL.glPopMatrix();
    if(flipY) { //update location based on speed
      x -= speedX;
      y -= speedY;
    } 
    else {
      x += speedX;
      y += speedY;
    }
  }
}
