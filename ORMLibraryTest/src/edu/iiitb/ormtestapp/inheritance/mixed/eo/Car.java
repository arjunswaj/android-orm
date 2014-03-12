package edu.iiitb.ormtestapp.inheritance.mixed.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name = "CAR")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorValue(value = "4-WHEELER")
public class Car extends Vehicle{

  @Column(name = "COLOR")
  private String color;

  @Column(name = "HORSE_POWER")
  private int horsePower;

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public int getHorsePower() {
    return horsePower;
  }

  public void setHorsePower(int horsePower) {
    this.horsePower = horsePower;
  }

}