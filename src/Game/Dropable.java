package Game;

/**
 *
 * @author Jeezy
 */
public class Dropable extends Item {
  protected float rate;
  
  public Dropable(int id, int texId, float x, float y, float r) {
    super(id, texId, x, y);
    rate = r;
  }

  public float getRate() { return rate; }
  public void setRate(float to) { rate = to; }
  
  
}
