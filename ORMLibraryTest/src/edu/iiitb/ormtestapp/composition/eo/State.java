package edu.iiitb.ormtestapp.composition.eo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "STATE")
public class State {
  @Id
  @Column(name = "_id")
  private int id;
  @Column(name = "NAME")
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}