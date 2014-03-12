package edu.iiitb.ormtestapp.inheritance.mixed.eo;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "FORD")
public class Ford extends Car{

  @Column(name = "MODEL")
  private String model;

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }
}