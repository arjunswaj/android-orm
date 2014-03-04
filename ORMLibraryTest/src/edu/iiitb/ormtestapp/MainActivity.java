package edu.iiitb.ormtestapp;

import edu.iiitb.ormtestapp.eo.Course;
import edu.iiitb.ormtestapp.eo.Student;
import edu.iiitb.ormtestapp.inheritance.joined.eo.FullTimeEmployee;
import edu.iiitb.ormtestapp.inheritance.joined.eo.PartTimeEmployee;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Cricketer;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Footballer;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Sportsman;
import iiitb.dm.ormlibrary.ORMHelper;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

  private void testPersistence(ORMHelper ormHelper) {
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

  private void testPersistenceOfInheritedObjectsWithJoinedStrategy(
      ORMHelper ormHelper) {
    PartTimeEmployee pte = new PartTimeEmployee();
    pte.setId(1);
    pte.setName("Tom Hanks");
    pte.setHourlyRate(12);
    ormHelper.persist(pte);

    FullTimeEmployee fte = new FullTimeEmployee();
    fte.setId(2);
    fte.setName("Jerry Seinfeld");
    fte.setSalary(1230);
    ormHelper.persist(fte);
  }

  private void testPersistenceOfInheritedObjectsWithTablePerClassStrategy(
      ORMHelper ormHelper) {
    Sportsman sportsman = new Sportsman();
    sportsman.setId(1);
    sportsman.setName("Vishwanathan Anand");
    ormHelper.persist(sportsman);

    Cricketer cricketer = new Cricketer();
    cricketer.setId(1);
    cricketer.setName("Saurav Ganguly");
    cricketer.setAverage(42.28f);
    cricketer.setTeam("India");
    ormHelper.persist(cricketer);

    Footballer footballer = new Footballer();
    footballer.setId(1);
    footballer.setName("David Beckham");
    footballer.setGoals(92);
    footballer.setTeam("England");
    ormHelper.persist(footballer);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ORMHelper ormHelper = new ORMHelper(getApplicationContext(),
        "testDB.sqlite", null, 1);

     testPersistence(ormHelper);
    // testPersistenceOfInheritedObjectsWithJoinedStrategy(ormHelper);
    // testPersistenceOfInheritedObjectsWithTablePerClassStrategy(ormHelper);
  }
}
