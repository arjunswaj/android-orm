package edu.iiitb.ormtestapp.inheritance.joined.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "PART_TIME_EMPLOYEE")
@DiscriminatorValue(value = "PART_TIME")
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
