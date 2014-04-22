package edu.iiitb.ormtestapp.inheritance.mixed.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name = "CABINET_MINISTER")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "MINISTER_TYPE")
public class CabinetMinister extends Minister {

  @Column(name = "PORTFOLIO")
  private String portfolio;

  @Column(name = "SALARY")
  private float salary;

  public String getPortfolio() {
    return portfolio;
  }

  public void setPortfolio(String portfolio) {
    this.portfolio = portfolio;
  }

  public float getSalary() {
    return salary;
  }

  public void setSalary(float salary) {
    this.salary = salary;
  }

}