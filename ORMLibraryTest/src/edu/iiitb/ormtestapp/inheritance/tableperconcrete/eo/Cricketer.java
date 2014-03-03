package edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "CRICKETER")
public class Cricketer extends Sportsman {

  @Column(name = "AVERAGE")
  private float average;

  @Column(name = "TEAM")
  private String team;

  public float getAverage() {
    return average;
  }

  public void setAverage(float average) {
    this.average = average;
  }

  public String getTeam() {
    return team;
  }

  public void setTeam(String team) {
    this.team = team;
  }

}
