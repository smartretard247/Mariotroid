package Game;

import Enumerations.ID;
import Enumerations.TEX;
import Drawing.DrawLib;
import Main.Engine;
import Main.GameOverException;
import Main.PhysicsEngine;
import Test.TestDisplay;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Enemy extends Living implements Drops {
  protected int pointsWorth = 100;
  protected static final int BASE_DAMAGE = 2; // damage dealt by contact, not projectiles
  protected int dropTex;
  
  public Enemy(int objId, int startLives, int startHealth, int texId, float x, float y, Point.Float speed) {
    super(objId, startLives, startHealth, texId, x, y, speed);
    dropTex = DrawLib.generateDropTex(new int[] { TEX.HEALTH_ORB, TEX.SHELL }, new float[] { 0.3f, 0.6f });
  }
  
  public Enemy() {
    this(-1, 1, 1, TEX.ENEMY_BASIC, 0, 0, new Point.Float(0, 0));
  }
  
  @Override
  public void resetAll(){
      super.resetAll();
  }
  
  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects) {
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Integer> toRemove = new LinkedList<>();
    for(Collidable c : collisions) {
      int texId = c.getTextureId();
      int objId = c.getObjectId();
      switch(texId) {
        case TEX.BOX:
        case TEX.LEVEL:
          if(movingDown()) { // falling straight down
            adjustToTopOf(c);
            setSpeedY(0);
          } else if(movingDownAndRight()) { // falling right and down
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToLeftOf(c);
              reverseSpeedX();
            } else {
              adjustToTopOf(c);
              setSpeedY(0);
            }
          } else if(movingDownAndLeft()) { // falling left and down
            if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToRightOf(c);
              reverseSpeedX();
            } else {
              adjustToTopOf(c);
              setSpeedY(0);
            }
          } else if(movingLeft()) { // moving left
            adjustToRightOf(c);
            reverseSpeedX(); // reverse direction
          } else if(movingRight()) { // moving right
            adjustToLeftOf(c);
            reverseSpeedX(); // reverse direction
          } else if(movingUpAndLeft()) { // flying upward and to the left
            if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getBottom() - getTop())) {
              adjustToRightOf(c);
              reverseSpeedX(); // reverse direction
            } else {
              adjustToBottomOf(c);
              setSpeedY(0);
            }
          } else if(movingUpAndRight()) { // flying upward and to the right
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
              adjustToLeftOf(c);
              reverseSpeedX(); // reverse direction
            } else {
              adjustToBottomOf(c);
              setSpeedY(0);
            }
          } else if(movingUp()) { // flying straight upward
            adjustToBottomOf(c);
            setSpeedY(0);
          }
          break;
        default:
          if(new Projectile().getClass().isInstance(c)) {
            if(!(c.getTextureId() == TEX.ENEMY_WEAPON_1 || c.getTextureId() == TEX.ENEMY_WEAPON_2)) {
              Projectile p = (Projectile)c;
              try {
                if(Engine.isDebugging()) TestDisplay.addTestData("Enemy HP: " + getHealth());
                loseHealth(p.getDamage());
                if(Engine.isDebugging()) TestDisplay.addTestData("Enemy damage: " + p.getDamage() + " / Enemy HP: " + getHealth());
              } catch (GameOverException ex) { // enemy died
                if(Engine.isDebugging()) TestDisplay.addTestData("Enemy destroyed");
                Engine.addScore(getPointsWorth());
                deathAction();
                toRemove.add(getObjectId());
              }
              toRemove.add(objId);
            }
          }
          break;
      }
    }
    return toRemove;
  }
  
  @Override
  public void draw() {
    flipX = PhysicsEngine.gravityIsInverted();
    super.draw();
  }
  
  /**
   * Sets flipX to fX before calling super.draw()
   * @param fX flip on x axis?
   */
  public void draw(boolean fX) {
    flipX = fX;
    super.draw();
  }
  
  public int getPointsWorth() { return pointsWorth; }
  public void setPointsWorth(int to) { pointsWorth = to; }
  
  public static int getBaseDamage() { return BASE_DAMAGE; }
  
  @Override
  protected void deathAction() { 
    drop();
  }
  
  /**
   * Changes the drops for the enemy to allow custom drops on specific enemies
   * 
   * Drops are the TEX number from enumerations, while rates refer to the rate
   * escalating to 1.0 for the items.  For example if rates are {0.3, 0.6}
   * then the first item  will drop if the random value is between 0-.2999, the 
   * second item will drop if the value is between .3-.6, and any value higher
   * will have no drop.
   * 
   * @param drops new array of drop TEX integer values
   * @param rates new array of rates in float for drops
   */
  public void changeDrop(int[] drops, float[] rates) {
    dropTex = DrawLib.generateDropTex(drops, rates);
  }

  /**
   * Implementation of interface "Drops".  dropTex should already be set, this function simply
   * drops the texture ID to current location.
   */
  @Override
  public void drop() {
    //TestDisplay.addTestData("rand = " + rand);
    if (dropTex == TEX.SHELL && !Engine.ammoCanDrop()) dropTex = -1;
    TestDisplay.addTestData("dropTex = " + dropTex);
    if (dropTex != -1) Engine.getGameContainer().addGO(new Item(ID.getNewId(), dropTex, getX(), getY()));
  }
}
