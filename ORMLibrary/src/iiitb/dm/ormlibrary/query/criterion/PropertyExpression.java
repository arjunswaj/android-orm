package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class PropertyExpression implements Criterion {

  protected PropertyExpression(String propertyName, String otherPropertyName,
      String op) {

  }

  @Override
  public boolean isGrouped() {
    // TODO Auto-generated method stub
    return false;
  }

}
