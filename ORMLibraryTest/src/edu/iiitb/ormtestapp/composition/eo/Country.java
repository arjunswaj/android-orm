package edu.iiitb.ormtestapp.composition.eo;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity(name = "COUNTRY")
public class Country {
  @Id
  @Column(name = "_id")
  private long id;
  @Column(name = "NAME")
  private String name;
  
  @OneToOne
  @JoinColumn(name = "CAPITAL_ID")
  private Capital capital;
  
  @OneToMany
  @JoinColumn(name = "COUNTRY_ID")
  private Collection<State> states;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Capital getCapital() {
    return capital;
  }

  public void setCapital(Capital capital) {
    this.capital = capital;
  }

  public Collection<State> getStates() {
    return states;
  }

  public void setStates(Collection<State> states) {
    this.states = states;
  }

}