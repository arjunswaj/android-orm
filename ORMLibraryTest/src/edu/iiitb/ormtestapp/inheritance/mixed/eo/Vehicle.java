package edu.iiitb.ormtestapp.inheritance.mixed.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name = "VEHICLE")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "VEHICLE_TYPE")
public abstract class Vehicle {
  @Id
  @Column(name = "_id")
  private int id;
  @Column(name = "MFG_YEAR")
  private int mfgYear;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getMfgYear() {
    return mfgYear;
  }

  public void setMfgYear(int mfgYear) {
    this.mfgYear = mfgYear;
  }

}