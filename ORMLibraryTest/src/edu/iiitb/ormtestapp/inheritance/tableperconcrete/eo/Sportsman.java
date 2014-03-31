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
  private long id;
  @Column(name = "NAME")
  private String name;
  @Column(name = "AGE")
  private int age;

  public long getId() {
    return id;
  }

  public void setId(long id) {
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

public int getAge()
{
	return age;
}

public void setAge(int age)
{
	this.age = age;
}
}
