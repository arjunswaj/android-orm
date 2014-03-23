package edu.iiitb.ormtestapp.eo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "STUDENT")
public class Student {

  @Id
  @Column(name = "_id")
  private int _id;
  @Column(name = "AGE")
  private int age;
  @Column(name = "NAME")
  private String name;
  @Column(name = "ADDRESS")
  private String address;
  @Column(name = "CGPA")
  private float cgpa;
  @Column(name = "COLLEGE")
  private String college;

  public Student() {
    
  }
  public Student(int age, String name, String address, float cgpa,
      String college) {
    super();
    this.age = age;
    this.name = name;
    this.address = address;
    this.cgpa = cgpa;
    this.college = college;
  }

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

  public float getCgpa() {
    return cgpa;
  }

  public void setCgpa(float cgpa) {
    this.cgpa = cgpa;
  }

  public String getCollege() {
    return college;
  }

  public void setCollege(String college) {
    this.college = college;
  }

}
