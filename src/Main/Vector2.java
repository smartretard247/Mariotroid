package Main;

/**
 *
 * @author Jeezy
 */
class Vector2 {
  public double x, y;
  
  public Vector2(double x, double y){
    this.x = x; 
    this.y = y;
  }
  
  public Vector2 Normalize() {
    float length = (float) Math.sqrt(x * x + y * y);
    x = x / length;
    y = y / length;
    return this;
  }

  public static double Dot(Vector2 a, Vector2 b) {
    return a.x * b.x + a.y * b.y;
  }
}
