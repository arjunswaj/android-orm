package iiitb.dm.ormlibrary.query.criterion;

import java.util.Collection;
import java.util.Map;

import iiitb.dm.ormlibrary.query.Criterion;

public class Restrictions {
  /**
   * Don't initialize Restrictions. Use the public static APIs
   */

  private Restrictions() {

  }

  public static Criterion allEq(Map propertyNameValues) {
    return null;
  }

  public static LogicalExpression and(Criterion lhs, Criterion rhs) {
    return new LogicalExpression(lhs, rhs, "AND");
  }

  public static Criterion between(String propertyName, Object lo, Object hi) {
    return null;
  }

  public static SimpleExpression eq(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, "=");
  }

  public static PropertyExpression eqProperty(String propertyName,
      String otherPropertyName) {
    return null;
  }

  public static Criterion idEq(Object value) {
    return null;
  }

  public static Criterion in(String propertyName, Collection values) {
    return null;
  }

  public static Criterion in(String propertyName, Object[] values) {
    return null;
  }

  public static SimpleExpression le(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, "<=");
  }

  public static SimpleExpression ge(String propertyName, Object value) {
    return new SimpleExpression(propertyName, value, ">=");
  }
  
  public static PropertyExpression leProperty(String propertyName,
      String otherPropertyName) {
    return null;
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

  public static PropertyExpression ltProperty(String propertyName,
      String otherPropertyName) {
    return null;
  }

  public static SimpleExpression ne(String propertyName, Object value) {
    return null;
  }

  public static PropertyExpression neProperty(String propertyName,
      String otherPropertyName) {
    return null;
  }

  public static Criterion not(Criterion expression) {
    return null;
  }

  public static LogicalExpression or(Criterion lhs, Criterion rhs) {
    return new LogicalExpression(lhs, rhs, "OR");
  }
}
