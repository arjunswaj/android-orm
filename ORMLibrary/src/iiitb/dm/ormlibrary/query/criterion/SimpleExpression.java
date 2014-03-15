package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class SimpleExpression implements Criterion {

  protected SimpleExpression(String propertyName, Object value, String op) {

  }

  protected SimpleExpression(String propertyName, Object value, String op,
      boolean ignoreCase) {

  }

  @Override
  public boolean isGrouped() {
    // TODO Auto-generated method stub
    return false;
  }

}
