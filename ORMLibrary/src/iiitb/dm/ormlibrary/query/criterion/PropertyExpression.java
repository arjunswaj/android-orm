package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class PropertyExpression implements Criterion {

  private String propertyName;
  private String otherPropertyName;
  private String op;
  
  protected PropertyExpression(String propertyName, String otherPropertyName,
      String op) {
    this.propertyName = propertyName;
    this.otherPropertyName = otherPropertyName;
    this.op = op;
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

  public String getOtherPropertyName() {
    return otherPropertyName;
  }

  public void setOtherPropertyName(String otherPropertyName) {
    this.otherPropertyName = otherPropertyName;
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }
  
}
