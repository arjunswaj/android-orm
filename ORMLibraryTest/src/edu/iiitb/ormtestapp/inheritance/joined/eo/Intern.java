package edu.iiitb.ormtestapp.inheritance.joined.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "INTERN")
@DiscriminatorValue(value = "INTERN")
public class Intern extends PartTimeEmployee {
  @Column(name = "STIPEND")
  private float stipend;

  public float getStipend() {
    return stipend;
  }

  public void setStipend(float stipend) {
    this.stipend = stipend;
  }

  
}
