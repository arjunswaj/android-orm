package edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name = "SPORTSMAN")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Sportsman {
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

  @Override
  public String toString() {
    return "Sportsman id: " + getId() + " name: " + getName();
  }
}
