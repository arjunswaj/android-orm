package edu.iiitb.ormtestapp.eo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "COURSE")
public class Course {

  @Id
  @Column(name = "_id")
  private int _id;
  @Column(name = "CREDITS")
  private int credits;
  @Column(name = "COURSE_NAME")
  private String courseName;
  @Column(name = "COURSE_DESCRIPTION")
  private String courseDescription;

  public int get_id() {
    return _id;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public int getCredits() {
    return credits;
  }

  public void setCredits(int credits) {
    this.credits = credits;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public String getCourseDescription() {
    return courseDescription;
  }

  public void setCourseDescription(String courseDescription) {
    this.courseDescription = courseDescription;
  }

}
