package Main;

import Drawing.DrawLib;
import Enumerations.ID;
import com.jogamp.opengl.GL2;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jeezy
 */
public class ObjectContainer {
  private static final int FIRST_CUSTOM_ID = 100000;
  private static int nextId = FIRST_CUSTOM_ID; // next id for custom objects, like floors/wall and projectiles
  private final Map<Integer, Collidable> gameObjects = new HashMap<>();
  private final Map<Integer, Collidable> tempObjects = new HashMap<>(); // like level and projectiles
  private final Map<Integer, Interactive> interactiveObjects = new HashMap<>();
  private final float GRID_SIZE = 1/8*11502;
  
  public ArrayList<Collidable> getVisibles() {
    ArrayList<Collidable> v = new ArrayList<>();
    v.addAll(gameObjects.values());
    v.addAll(tempObjects.values());
    v.addAll(interactiveObjects.values());
    return v;
  }
  
  /**
   * Only returns objects that are within the supplied world rectangle.
   * @param window
   * @return game objects in window
   */
  public ArrayList<Collidable> getVisiblesWithin(Rectangle window) {
    ArrayList<Collidable> v = new ArrayList<>();
    for(Collidable c : gameObjects.values()) {
      if(c.collidesWith(window)) v.add(c);
    }
    for(Collidable c : tempObjects.values()) {
      if(c.collidesWith(window)) v.add(c);
    }
    for(Collidable c : interactiveObjects.values()) {
      if(c.collidesWith(window)) v.add(c);
    }
    if(gameObjects.containsKey(ID.HERO)) v.add(getGO(ID.HERO));
    return v;
  }
  
  /**
   * Gets all the objects in grid provided and plus/minus one grid.
   * @param centerGridNum
   * @return 
   */
  public ArrayList<Collidable> getGOByCenterGrid(int centerGridNum) {
    ArrayList<Collidable> v = new ArrayList<>();
    for(Collidable c : gameObjects.values()) {
      int cGrid = (int)(c.getX()/GRID_SIZE);
      if(cGrid == centerGridNum || cGrid == centerGridNum+1 || cGrid == centerGridNum-1) v.add(c);
    }
    for(Collidable c : tempObjects.values()) {
      int cGrid = (int)(c.getX()/GRID_SIZE);
      if(cGrid == centerGridNum || cGrid == centerGridNum+1 || cGrid == centerGridNum-1) v.add(c);
    }
    for(Collidable c : interactiveObjects.values()) {
      int cGrid = (int)(c.getX()/GRID_SIZE);
      if(cGrid == centerGridNum || cGrid == centerGridNum+1 || cGrid == centerGridNum-1) v.add(c);
    }
    return v;
  }
  
  public void addGO(Collidable c) {
    if(c instanceof Interactive)
      addIO((Interactive)c);
    else
      gameObjects.putIfAbsent(c.getObjectId(), c);
  }
  
  private void addIO(Interactive i) {
    interactiveObjects.putIfAbsent(i.getObjectId(), i);
  }
  
  private void removeGO(int id) {
    if(gameObjects.containsKey(id)) gameObjects.remove(id);
  }
  
  private void removeIO(int id) {
    if(interactiveObjects.containsKey(id)) interactiveObjects.remove(id);
  }
  
  private void removeGO(Collidable c) {
    gameObjects.remove(c.getObjectId(), c);
  }
  
  public Collidable getGO(int id) {
    if(gameObjects.containsKey(id))
      return gameObjects.get(id);
    return null;
  }
  
  public Interactive getIO(int id) {
    if(interactiveObjects.containsKey(id))
      return interactiveObjects.get(id);
    return null;
  }
  
  /**
   * Clears all game, temp, and interactive objects except for the hero.
   */
  public void clearGOs() {
    Hero h = (Hero)gameObjects.get(ID.HERO);
    gameObjects.clear();
    if(h != null) addGO(h);
    tempObjects.clear();
    interactiveObjects.clear();
    nextId = FIRST_CUSTOM_ID;
  }
  
  public void addTO(int id, Collidable c) {
    if(!tempObjects.containsKey(id))
      tempObjects.put(id, c);
  }
  
  private void removeTO(int id) {
    if(tempObjects.containsKey(id)) tempObjects.remove(id);
  }
  
  /**
   * Removes any instance of id within game, temp, and interactive objects.
   * @param id 
   */
  public void removeAny(int id) {
    removeGO(id);
    removeTO(id);
    removeIO(id);
  }
  
  public static final int getNewId() { return nextId++; }
  public static final int getLastId() {
    if(nextId == FIRST_CUSTOM_ID) return FIRST_CUSTOM_ID;
    else return nextId-1;
  }
  
  /**
   * Locates an interactive object at the given screen coordinates.
   * @param at screen coordinates
   * @return the selected interactive object
   */
  public Interactive getInteractiveAt(Point at) {
    Point.Float wc = DrawLib.screenToWorld(at);
    Rectangle point = new Rectangle(wc.x, wc.y, 1, 1);
    for(Interactive i : interactiveObjects.values()) {
      if(i.collidesWith(point)) {
        i.select();
        return i;
      }
    }
    return null;
  }
  
  /**
   * Deselects all interactive objects, called when a selected object becomes null.
   */
  public void deselectAllIO() {
    interactiveObjects.values().forEach((i) -> { i.deselect(); });
  }
  
  /**
   * Deselects all BUT supplied interactive objects.
   * @param i
   */
  public void deselectAllIOBut(Interactive i) {
    if(interactiveObjects.containsKey(i.getObjectId())) {
      interactiveObjects.values().forEach((i2) -> {
        if(i2.getObjectId() != i.getObjectId())
          i2.deselect();
      });
    }
  }
  
  /**
   * Removes the "closed" door and adds a "powered" door.  Also creates a collidable point at which
   * contact will change game mode to WARPING.
   */
  public void activateDoor() {
    Door door = (Door)getGO(ID.DOOR);
    door.activate();
    addGO(door.getWarp());
    Engine.setStatusMessage("Warp activated.");
  }
}
