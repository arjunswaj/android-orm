package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class SimpleExpression implements Criterion {

  private String propertyName;
  private Object value;
  private String op;

  protected SimpleExpression(String propertyName, Object value, String op) {
    this.propertyName = propertyName;
    this.value = value;
    this.op = op;
  }

  protected SimpleExpression(String propertyName, Object value, String op,
      boolean ignoreCase) {

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

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }

}
