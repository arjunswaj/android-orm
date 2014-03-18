package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Criterion;

public class LogicalExpression implements Criterion {

  private Criterion rhs;
  private Criterion lhs;
  private String op;

  protected LogicalExpression(Criterion lhs, Criterion rhs, String op) {
    this.lhs = lhs;
    this.rhs = rhs;
    this.op = op;
  }

  @Override
  public boolean isGrouped() {
    // TODO Auto-generated method stub
    return false;
  }

  public Criterion getRhs() {
    return rhs;
  }

  public void setRhs(Criterion rhs) {
    this.rhs = rhs;
  }

  public Criterion getLhs() {
    return lhs;
  }

  public void setLhs(Criterion lhs) {
    this.lhs = lhs;
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }

}
