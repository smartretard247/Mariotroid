package Main;

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
  private final Map<Integer, Collidable> gameObjects = new HashMap<>();
  private final Map<Integer, Collidable> tempObjects = new HashMap<>(); // like level and projectiles
  private final Map<Integer, Interactive> interactiveObjects = new HashMap<>();
  //private final ArrayList<Integer> visibleObjects = new ArrayList<>();
  
  public ArrayList<Collidable> getVisibles() {
    ArrayList<Collidable> v = new ArrayList<>();
    v.addAll(gameObjects.values());
    v.addAll(tempObjects.values());
    v.addAll(interactiveObjects.values());
    return v;
  }
  
  public void addGO(Collidable c) {
    gameObjects.put(c.getObjectId(), c);
  }
  
  public void addIO(Interactive i) {
    interactiveObjects.put(i.getObjectId(), i);
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
    Hero h = (Hero)gameObjects.get(ID.ID_HERO);
    gameObjects.clear();
    if(h != null) addGO(h);
    tempObjects.clear();
    interactiveObjects.clear();
    nextId = FIRST_CUSTOM_ID;
  }
  
  public void addTO(int id, Collidable c) {
    if(!tempObjects.containsKey(id)) {
      tempObjects.put(id, c);
    }
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
   * Locates an interactive object at the given world coordinates.
   * @param at world coordinates
   * @return the selected interactive object
   */
  public Interactive getInteractive(Point.Double at) {
    Rectangle point = new Rectangle(at.x, at.y, 1, 1);
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
    for(Interactive i : interactiveObjects.values()) {
      i.deselect();
    }
  }
}
