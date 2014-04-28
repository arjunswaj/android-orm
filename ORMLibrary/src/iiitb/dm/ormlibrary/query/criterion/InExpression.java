package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class InExpression implements Criterion {

  private String propertyName;
  private Object[] values;

  protected InExpression(String propertyName, Object[] values) {
    this.propertyName = propertyName;
    this.values = values;
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

  public Object[] getValues() {
    return values;
  }

  public void setValues(Object[] values) {
    this.values = values;
  }

}
