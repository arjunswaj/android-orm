package iiitb.dm.ormlibrary.query.criterion;

import java.util.Collection;

import iiitb.dm.ormlibrary.query.Criterion;

public class Restrictions {
  /**
   * Don't initialize Restrictions. Use the public static APIs
   */

  private Restrictions() {

  }

  public static LogicalExpression and(Criterion lhs, Criterion rhs) {
    return new LogicalExpression(lhs, rhs, "AND");
  }

  public static SimpleExpression eq(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, "=");
  }

  public static BetweenExpression between(String propertyName, Object lo, Object hi) {
    return new BetweenExpression(propertyName, lo, hi);
  }
  
  public static SimpleExpression le(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, "<=");
  }

  public static SimpleExpression ge(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, ">=");
  }
  
  public static SimpleExpression like(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, "like");
  }

  public static SimpleExpression gt(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, ">");
  }
  
  public static SimpleExpression lt(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, "<");
  }

  public static SimpleExpression ne(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, "<>");
  }

  public static LogicalExpression or(Criterion lhs, Criterion rhs) {
    return new LogicalExpression(lhs, rhs, "OR");
  }
  
  public static InExpression in(String propertyName, Object[] values) {
    return new InExpression( propertyName, values );
  }
  
  public static InExpression in(String propertyName, Collection<?> values) {
    return new InExpression( propertyName, values.toArray() );
  }
}
