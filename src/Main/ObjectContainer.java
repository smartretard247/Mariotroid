package Main;

import Game.Interactive;
import Game.Hero;
import Game.Door;
import Game.Collidable;
import Drawing.DrawLib;
import Enumerations.ID;
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
  private final Map<Integer, Interactive> interactiveObjects = new HashMap<>();
  private final Map<Integer, Collidable> permanentObjects = new HashMap<>();
  private final Map<Integer, Map<Integer, Collidable>> objectGrid = new HashMap<>();
  
  public ArrayList<Collidable> getVisibles() {
    ArrayList<Collidable> v = new ArrayList<>();
    objectGrid.values().stream().forEach( (map) -> {
      v.addAll(map.values());
    });
    v.addAll(permanentObjects.values());
    return v;
  }
  
  /**
   * Only returns objects that are within the supplied world rectangle.
   * @param window
   * @return game objects in window
   */
  public ArrayList<Collidable> getVisiblesWithin(Rectangle window) {
    ArrayList<Collidable> v = new ArrayList<>();
    objectGrid.values().forEach( (map) -> {
      map.values().stream().forEach( (c) -> {
        if(c.collidesWith(window)) v.add(c);
      });
      permanentObjects.values().stream().forEach( (c) -> {
        if(c.collidesWith(window)) v.add(c);
      });
    });
    //if(gameObjects.containsKey(ID.HERO)) v.add(getGO(ID.HERO));
    return v;
  }
  
  /**
   * Checks if the supplied object moved to a new grid, if so swaps it into the correct list.
   * @param c 
   */
  public void updateGridLocation(Collidable c) {
    if(c != null && c.isGridFlagged()) {
      int objId = c.getObjectId();
      int oldGrid = c.getOldGrid();
      int currGrid = c.getCurrGrid();
      if(objectGrid.get(currGrid) == null)
        objectGrid.put(currGrid, new HashMap<>());
      objectGrid.get(currGrid).put(objId, c);
      if(objectGrid.get(oldGrid) == null)
        objectGrid.put(oldGrid, new HashMap<>());
      objectGrid.get(oldGrid).remove(c.getObjectId());
      c.resetCurrGrid();
    }
  }
  
  /**
   * Gets all the objects in same grid as object provided and plus/minus one grid
   * @param c collidable object
   * @return 
   */
  public ArrayList<Collidable> getGOsNear(Collidable c) {
    int grid = c.getCurrGrid();
    ArrayList<Collidable> v = new ArrayList<>();
    if(objectGrid != null) {
      Map<Integer, Collidable> m0 = objectGrid.get(grid);
      if(m0 != null) v.addAll(m0.values());
      if(grid > 0) {
        Map<Integer, Collidable> m1 = objectGrid.get(grid-1);
        if(m1 != null) v.addAll(m1.values());
      }
      if(grid < objectGrid.size()-1) {
        Map<Integer, Collidable> m2 = objectGrid.get(grid+1);
        if(m2 != null) v.addAll(m2.values());
      }
    }
    v.addAll(permanentObjects.values());
    return v;
  }
  
  public void addGO(Collidable c) {
    if(c != null) { // && DOESNT ALREADY EXIST
      if(c instanceof Interactive)
        addIO((Interactive)c); // put a reference to it in the interactive objects map, AND...
      if(c.getW() >= Collidable.getGridSize()) {
        addPO(c);
      } else {
        int currGrid = c.getCurrGrid();
        if(objectGrid.get(currGrid) == null)
          objectGrid.put(currGrid, new HashMap<>());
        objectGrid.get(currGrid).putIfAbsent(c.getObjectId(), c);
      }
      c.resetCurrGrid();
    }
  }
  
  private void addIO(Interactive i) {
    interactiveObjects.putIfAbsent(i.getObjectId(), i);
  }
  
  private void addPO(Collidable c) {
    permanentObjects.putIfAbsent(c.getObjectId(), c);
  }
  
  private void removePO(int id) {
    if(permanentObjects.containsKey(id)) permanentObjects.remove(id);
  }
  
  /**
   * Removes object from grid, and interactive map if exists.
   * @param id the id of the object to remove
   */
  public void removeGO(int id) {
    removeIO(id); // remove here first, as this is duplicate
    for(Map<Integer, Collidable> m : objectGrid.values()) {
      if(m.containsKey(id)) {
        m.remove(id);
        return;
      }
    }
    removePO(id); // call this if objects wasn't found (rare permanent objects)
  }
  
  private void removeIO(int id) {
    if(interactiveObjects.containsKey(id)) interactiveObjects.remove(id);
  }
  
  public Collidable getGO(int id) {
    for(Map<Integer, Collidable> map : objectGrid.values()) {
      if(map.containsKey(id)) {
        return map.get(id);
      }
    }
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
    clearGrid(); // but leave hero
    interactiveObjects.clear();
    permanentObjects.clear();
    nextId = FIRST_CUSTOM_ID;
  }
  
  private void clearGrid() {
    Hero h = (Hero)getGO(ID.HERO);
    objectGrid.values().forEach( (map) -> {
      map.clear();
    });
    if(h != null) addGO(h);
  }
  
  public static final int getNewId() { return nextId++; }
  public static final int getLastId() {
    if(nextId == FIRST_CUSTOM_ID) return FIRST_CUSTOM_ID;
    else return nextId-1;
  }
  
  public ArrayList<Interactive> getInteractivesInGrid(int grid) {
    ArrayList<Interactive> list = new ArrayList<>();
    Map<Integer, Collidable> map = objectGrid.get(grid);
    if(map != null) {
      map.values().forEach( (c) -> {
        if(c instanceof Interactive) list.add((Interactive)c);
      });
    }
    return list;
  }
  
  /**
   * Locates an interactive object at the given screen coordinates.
   * @param at screen coordinates
   * @return the selected interactive object
   */
  public Interactive getInteractiveAt(Point at) {
    Point.Float wc = DrawLib.screenToWorld(at);
    Rectangle point = new Rectangle(wc.x, wc.y, 1, 1);
    
    Map<Integer, Collidable> map = objectGrid.get((int)(wc.x/Collidable.getGridSize()));
    if(map != null) {
      for(Collidable c : map.values()) {
        if(c instanceof Interactive) {
          if(c.collidesWith(point)) {
            Interactive i = (Interactive)c;
            i.select();
            return i;
          }
        }
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
