package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class LogicalExpression implements Criterion {

  protected LogicalExpression(Criterion lhs, Criterion rhs, String op) {

  }

  @Override
  public boolean isGrouped() {
    // TODO Auto-generated method stub
    return false;
  }

}
