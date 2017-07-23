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
  private float x, y, w, h;
  
  public Rectangle() {
    x = 0;
    y = 0;
    w = 0;
    h = 0;
  }
  
  public Rectangle(float X, float Y, float W, float H) {
    x = X;
    y = Y;
    w = W;
    h = H;
  }
  
  public float x() { return x; }
  public float y() { return y; }
  public float w() { return w; }
  public float h() { return h; }
  
  public void setX(float to) { x = to; }
  public void setY(float to) { y = to; }
  public void setW(float to) { w = to; }
  public void setH(float to) { h = to; }
  
  @Override
  public String toString() {
    return "Rectangle@( " + x + " , " + y + " ), width: " + w + " , height: " + h;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
    hash = 53 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
    hash = 53 * hash + (int) (Double.doubleToLongBits(this.w) ^ (Double.doubleToLongBits(this.w) >>> 32));
    hash = 53 * hash + (int) (Double.doubleToLongBits(this.h) ^ (Double.doubleToLongBits(this.h) >>> 32));
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Rectangle other = (Rectangle) obj;
    if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
      return false;
    }
    if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
      return false;
    }
    if (Double.doubleToLongBits(this.w) != Double.doubleToLongBits(other.w)) {
      return false;
    }
    if (Double.doubleToLongBits(this.h) != Double.doubleToLongBits(other.h)) {
      return false;
    }
    return true;
  }
  
}
