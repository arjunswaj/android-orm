package edu.iiitb.ormtestapp.eo;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "COURSE")
public class Course {

  @Id
  @Basic(name = "_id")
  private int _id;
  @Basic(name = "CREDITS")
  private int credits;
  @Basic(name = "COURSE_NAME")
  private String courseName;
  @Basic(name = "COURSE_DESCRIPTION")
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
