package edu.iiitb.ormtestapp;

import edu.iiitb.ormtestapp.eo.Course;
import edu.iiitb.ormtestapp.eo.Student;
import iiitb.dm.ormlibrary.ORMHelper;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ORMHelper ormHelper = new ORMHelper(getApplicationContext(),
        "testDB.sqlite", null, 1);
    
    Course course = null;
    for (int index = 0; index < 5; index += 1) {
      course = new Course();
      course.set_id(index);
      course.setCourseDescription("Course" + index);
      course.setCourseName("DM" + index);
      course.setCredits(5);
      ormHelper.persist(course);
    }
    
    Student student = null;
    for (int index = 0; index < 5; index += 1) {
      student = new Student();
      student.set_id(index);
      student.setAddress("Address" + index);
      student.setAge(20 + index);
      student.setName("Name" + index);
      ormHelper.persist(student);
    }
  }
}
