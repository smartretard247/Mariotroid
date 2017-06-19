/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

/**
 *
 * @author Jeezy
 */
public class Rectangle {
  private double x, y, w, h;
  
  public Rectangle() {
    x = 0;
    y = 0;
    w = 0;
    h = 0;
  }
  
  public Rectangle(double X, double Y, double W, double H) {
    x = X;
    y = Y;
    w = W;
    h = H;
  }
  
  public double x() { return x; }
  public double y() { return y; }
  public double w() { return w; }
  public double h() { return h; }
  
  public void setX(double to) { x = to; }
  public void setY(double to) { y = to; }
  public void setW(double to) { w = to; }
  public void setH(double to) { h = to; }
  
  @Override
  public String toString() {
    return "Rectangle@( " + x + " , " + y + " ), width: " + w + " , height: " + h;
  }
}
