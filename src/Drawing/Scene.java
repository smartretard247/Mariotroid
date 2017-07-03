package Drawing;

/**
 *
 * @author Jeezy
 */
public class Scene {
  // variables to translate the scene
  public double transX;
  public double transY;
  public double transZ;
  public double scaleX;
  public double scaleY;
  public double scaleZ;
  
  public Scene() {
    transX = 0.0; // for moving the entire scene
    transY = 0.0;
    transZ = 0.0;
    scaleX = 1.0; // global scaling, change back to 1.0 for distrobution
    scaleY = 1.0;
    scaleY = 1.0;
  }
  public Scene(double tX, double tY, double tZ, double sX, double sY, double sZ) {
    transX = tX;
    transY = tY;
    transZ = tZ;
    scaleX = sX;
    scaleY = sY;
    scaleZ = sZ;
  }
  
  public void setTranslation(double tX, double tY, double tZ) {
    transX = tX;
    transY = tY;
    transZ = tZ;
  }
  
  public void setScale(double sX, double sY, double sZ) {
    transX = sX;
    transY = sY;
    transZ = sZ;
  }
}
