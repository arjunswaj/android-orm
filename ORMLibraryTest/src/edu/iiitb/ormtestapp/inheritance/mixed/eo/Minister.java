package edu.iiitb.ormtestapp.inheritance.mixed.eo;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import edu.iiitb.ormtestapp.composition.eo.Article;

@Entity(name = "MINISTER")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Minister {
  @Id
  @Column(name = "_id")
  private long id;
  @Column(name = "STATE")
  private String state;   
  
 

  public long getId() {
    return id;
  }

 

public void setId(long id) {
    this.id = id;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}