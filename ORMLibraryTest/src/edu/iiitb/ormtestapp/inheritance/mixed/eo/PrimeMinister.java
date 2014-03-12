package edu.iiitb.ormtestapp.inheritance.mixed.eo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "PRIME_MINISTER")
@DiscriminatorValue(value = "PM")
public class PrimeMinister extends CabinetMinister {

  @Column(name = "AGE")
  private int age;

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

}