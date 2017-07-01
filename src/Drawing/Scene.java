package Drawing;

/**
 *
 * @author Jeezy
 */
public class Scene {
  // variables to translate the scene
  public double transX;
  public double transY;
  public double scaleX;
  public double scaleY;
  
  public Scene() {
    transX = 0.0; // for moving the entire scene
    transY = 0.0; // for moving the entire scene
    scaleX = 1.0; // global scaling, change back to 1.0 for distrobution
    scaleY = 1.0;
  }
  public Scene(double tX, double tY, double sX, double sY) {
    transX = tX;
    transY = tY;
    scaleX = sX;
    scaleY = sY;
  }
}
