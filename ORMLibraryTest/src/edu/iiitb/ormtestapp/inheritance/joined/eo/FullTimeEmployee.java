package edu.iiitb.ormtestapp.inheritance.joined.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "FULL_TIME_EMPLOYEE")
@DiscriminatorValue(value = "FULL_TIME")
public class FullTimeEmployee extends Employee {
  @Column(name = "SALARY")
  private long salary;
  @Column(name = "PENSION")
  private long pension;

  public long getPension() {
    return pension;
  }

  public void setPension(long pension) {
    this.pension = pension;
  }

  public long getSalary() {
    return salary;
  }

  public void setSalary(long salary) {
    this.salary = salary;
  }

  public String toString() {
    return "FullTimeEmployee id: " + getId() + " name: " + getName();
  }
}
