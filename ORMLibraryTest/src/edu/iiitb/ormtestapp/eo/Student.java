package edu.iiitb.ormtestapp.eo;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "STUDENT")
public class Student {

  @Id
  @Basic(name = "ID")
  private int _id;
  @Basic(name = "AGE")
  private int age;
  @Basic(name = "NAME")
  private String name;
  @Basic(name = "ADDRESS")
  private String address;

  public int get_id() {
    return _id;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

}
