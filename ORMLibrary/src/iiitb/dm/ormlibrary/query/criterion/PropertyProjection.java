package iiitb.dm.ormlibrary.query.criterion;

import iiitb.dm.ormlibrary.query.Projection;

public class PropertyProjection implements Projection {
  private String propertyName;
  private boolean grouped;

  protected PropertyProjection(String prop) {
    this.propertyName = prop;
  }

  public boolean isGrouped() {
    return grouped;
  }

  public String getPropertyName() {
    return propertyName;
  }  
}
