package edu.iiitb.ormtestapp;

import edu.iiitb.ormtestapp.eo.Course;
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
    /*try
    {
    	ormHelper.getWritableDatabase().execSQL("Create table COURSE(_id int, CREDITS int, COURSE_NAME varchar, COURSE_DESCRIPTION varchar);");
    }
    catch(Exception ex){
    	ex.printStackTrace();
    }*/
    Course course = new Course();
    course.set_id(2);
    course.setCourseDescription("Course1");
    course.setCourseName("DM");
    course.setCredits(5);
    ormHelper.persist(course);
  }
}
