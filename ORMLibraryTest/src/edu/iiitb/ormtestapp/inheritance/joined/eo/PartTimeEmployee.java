package edu.iiitb.ormtestapp.inheritance.joined.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name = "PART_TIME_EMPLOYEE")
@DiscriminatorValue(value = "PART_TIME")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "PART_TIME_EMPLOYEE_TYPE")
public class PartTimeEmployee extends Employee {
  @Column(name = "HOURLY_RATE")
  private float hourlyRate;

  public float getHourlyRate() {
    return hourlyRate;
  }

  public void setHourlyRate(float hourlyRate) {
    this.hourlyRate = hourlyRate;
  }

  public String toString() {
    return "PartTimeEmployee id: " + getId() + " name: " + getName();
  }
}
