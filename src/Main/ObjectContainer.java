package Main;

import Enumerations.ID;
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
  //private final ArrayList<Integer> visibleObjects = new ArrayList<>();
  
  public ArrayList<Collidable> getVisibles() {
    ArrayList<Collidable> v = new ArrayList<>();
    /*for(Integer i : visibleObjects) {
      if(gameObjects.containsKey(i)) v.add(gameObjects.get(i));
    }*/
    v.addAll(gameObjects.values());
    v.addAll(tempObjects.values());
    return v;
  }
  
  /*public ArrayList<Integer> getVisibleIds() {
    ArrayList<Integer> v = new ArrayList<>();
    v.addAll(visibleObjects);
    v.addAll(tempObjects.keySet());
    return v;
  }*/
  
  public void addGO(Collidable c) {
    gameObjects.put(c.getObjectId(), c);
  }
  
  private void removeGO(int id) {
    if(gameObjects.containsKey(id)) gameObjects.remove(id);
  }
  
  private void removeGO(Collidable c) {
    gameObjects.remove(c.getObjectId(), c);
  }
  
  public Collidable getGO(int id) {
    if(gameObjects.containsKey(id))
      return gameObjects.get(id);
    return null;
  }
  
  public void clearGOs() {
    Hero h = (Hero)gameObjects.get(ID.ID_HERO);
    gameObjects.clear();
    if(h != null) addGO(h);
    tempObjects.clear();
    nextId = FIRST_CUSTOM_ID;
  }
  
  /*public Collidable getVisible(int id) {
    if(visibleObjects.contains(id)) {
      Collidable c = gameObjects.get(id);
      if(c != null) return c;
      c = tempObjects.get(id);
      if(c != null) return c;
    }
    return null;
  }*/
  
  /*public void addVisible(int id) {
    if(!visibleObjects.contains(id))
      visibleObjects.add(id);
  }*/
  
  public void addTO(int id, Collidable c) {
    if(!tempObjects.containsKey(id)) {
      tempObjects.put(id, c);
    }
  }
  
  private void removeTO(int id) {
    if(tempObjects.containsKey(id)) tempObjects.remove(id);
  }
  
  public void removeAny(int id) {
    removeGO(id);
    removeTO(id);
  }
  
  /*public void removeVisible(int id) {
    if(visibleObjects.contains(id)) {
      if(tempObjects.containsKey(id)) tempObjects.remove(id);
      else visibleObjects.remove(visibleObjects.indexOf(id));
    } else {
      System.out.println("Could not remove object with id " + id + ", does not exist in visibles.");
    }
  }*/
  
  /*public void clearVisibles() {
    visibleObjects.clear();
    tempObjects.clear();
    nextId = FIRST_CUSTOM_ID;
  }*/
  
  public static final int getNewId() { return nextId++; }
  public static final int getLastId() {
    if(nextId == FIRST_CUSTOM_ID) return FIRST_CUSTOM_ID;
    else return nextId-1;
  }
}
