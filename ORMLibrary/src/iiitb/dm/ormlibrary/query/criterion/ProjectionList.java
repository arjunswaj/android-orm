package iiitb.dm.ormlibrary.query.criterion;

import java.util.ArrayList;
import java.util.List;

import iiitb.dm.ormlibrary.query.Projection;

public class ProjectionList {
  protected ProjectionList() {
  }

  private List<Projection> elements = new ArrayList<Projection>();

  public ProjectionList add(Projection projection) {
    elements.add(projection);
    return this;
  }

  public List<Projection> getElements() {
    return elements;
  }

}
