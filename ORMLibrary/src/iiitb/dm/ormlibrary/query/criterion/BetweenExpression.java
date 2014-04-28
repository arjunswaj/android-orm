package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class BetweenExpression implements Criterion {

  private String propertyName;
  private Object lo;
  private Object hi;

  protected BetweenExpression(String propertyName, Object lo, Object hi) {
    this.propertyName = propertyName;
    this.lo = lo;
    this.hi = hi;
  }

  @Override
  public boolean isGrouped() {
    // TODO Auto-generated method stub
    return false;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public Object getLo() {
    return lo;
  }

  public void setLo(Object lo) {
    this.lo = lo;
  }

  public Object getHi() {
    return hi;
  }

  public void setHi(Object hi) {
    this.hi = hi;
  }

}
