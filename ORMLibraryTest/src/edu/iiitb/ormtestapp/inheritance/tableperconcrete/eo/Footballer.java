package edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "FOOTBALLER")
public class Footballer extends Sportsman {

  @Column(name = "GOALS")
  private int goals;

  @Column(name = "TEAM")
  private String team;

  public int getGoals() {
    return goals;
  }

  public void setGoals(int goals) {
    this.goals = goals;
  }

  public String getTeam() {
    return team;
  }

  public void setTeam(String team) {
    this.team = team;
  }

}
