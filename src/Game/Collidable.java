package Game;

import Drawing.DrawLib;
import Enumerations.TEX;
import Main.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public abstract class Collidable extends Drawable {
  private final static float GRID_SIZE = 1750f;
  public static float getGridSize() { return GRID_SIZE; }
  private final int objectId;
  private int currGrid;
  private int oldGrid;
  
  /**
   *
   * @param objId
   * @param texId
   * @param x
   * @param y
   * @param w
   * @param h
   */
  public Collidable(int objId, int texId, float x, float y, float w, float h) {
    super(texId, x, y, w, h);
    objectId = objId;
    currGrid = (int)(x/GRID_SIZE);
    oldGrid = currGrid;
  }
  
  /**
   *
   * @param objId
   * @param texId
   * @param x
   * @param y
   */
  public Collidable(int objId, int texId, float x, float y) {
    this(objId, texId, x, y, (texId >= 0) ? DrawLib.getTexture(texId).getWidth() : 1, (texId >= 0) ? DrawLib.getTexture(texId).getHeight() : 1);
  }
  
  /**
   *
   * @param x
   * @param y
   */
  public Collidable(int x, int y) {
    this(-1, TEX.NONE, x, y, 1, 1);
  }
  
  /**
   *
   */
  public Collidable() {
    this(-1, TEX.NONE, 0, 0, 1, 1);
  }
  
  public int getOldGrid() { return oldGrid; }
  public int getCurrGrid() { return currGrid; }
  public void resetCurrGrid() {
    oldGrid = currGrid;
    currGrid = (int)(x/GRID_SIZE);
  }
  public boolean isGridFlagged() { return !(oldGrid == currGrid); }
  
  /**
   * Returns a rectangle surrounding the object.
   * @return 
   */
  public Rectangle getBoundary() {
    return new Rectangle(x-width/2, y+height/2, width, height);
  }
  
  /**
   * Returns a list of collidable objects that have collided with this object.
   * @param nearObjects
   * @return 
   */
  public List<Collidable> getCollisions(ArrayList<Collidable> nearObjects) {  
    List<Collidable> collisions = new LinkedList<>();
    for(Collidable near : nearObjects) {
      if(!getBoundary().equals(near.getBoundary()))
        if(collidesWith(near.getBoundary()))
          collisions.add(near);
    }
    return collisions;
  }
  
  /**
   * Test if this object's boundary collides with supplied rectangle.
   * @param dest
   * @return 
   */
  public boolean collidesWith(Rectangle dest) {
    Rectangle src = this.getBoundary();
    return !(src.x() > dest.x() + dest.w() // src is right of dest
            || src.x() + src.w() < dest.x() // src is left of dest
            || src.y() < dest.y() - dest.h() // dest is above src
            || src.y() - src.h() > dest.y()); // src is above dest
  }
  
  /**
   *
   * @return
   */
  public final int getObjectId() { return objectId; }
  
  /**
   *
   * @param nearObjects
   * @return
   */
  public abstract List<Integer> processCollisions(ArrayList<Collidable> nearObjects);
}
