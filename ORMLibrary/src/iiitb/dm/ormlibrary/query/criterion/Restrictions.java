package iiitb.dm.ormlibrary.query.criterion;

import java.util.Collection;
import java.util.Map;

import iiitb.dm.ormlibrary.query.Criterion;

public class Restrictions {
  /**
   * Don't initialize Restrictions. Use the Static APIs
   */

  private Restrictions() {

  }

  static Criterion allEq(Map propertyNameValues) {
    return null;
  }

  static LogicalExpression and(Criterion lhs, Criterion rhs) {
    return null;
  }

  static Criterion between(String propertyName, Object lo, Object hi) {
    return null;
  }

  static SimpleExpression eq(String propertyName, Object value) {
    return null;
  }

  static PropertyExpression eqProperty(String propertyName,
      String otherPropertyName) {
    return null;
  }

  static Criterion idEq(Object value) {
    return null;
  }

  static Criterion in(String propertyName, Collection values) {
    return null;
  }

  static Criterion in(String propertyName, Object[] values) {
    return null;
  }

  static SimpleExpression le(String propertyName, Object value) {
    return null;
  }

  static PropertyExpression leProperty(String propertyName,
      String otherPropertyName) {
    return null;
  }

  static SimpleExpression like(String propertyName, Object value) {
    return null;
  }

  static SimpleExpression lt(String propertyName, Object value) {
    return null;
  }

  static PropertyExpression ltProperty(String propertyName,
      String otherPropertyName) {
    return null;
  }

  static SimpleExpression ne(String propertyName, Object value) {
    return null;
  }

  static PropertyExpression neProperty(String propertyName,
      String otherPropertyName) {
    return null;
  }

  static Criterion not(Criterion expression) {
    return null;
  }

  static LogicalExpression or(Criterion lhs, Criterion rhs) {
    return null;
  }
}
