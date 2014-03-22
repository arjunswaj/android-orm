package iiitb.dm.ormlibrary.query.criterion;


public final class Projections {
  public static PropertyProjection property(String propertyName) {
    return new PropertyProjection(propertyName);
  }

  public static ProjectionList projectionList() {
    return new ProjectionList();
  }
}
