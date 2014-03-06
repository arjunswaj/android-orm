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
      course.setCourseDescription("Course" + index);
      course.setCourseName("DM" + index);
      course.setCredits(5);
      ormHelper.persist(course);
    }

    Student student = null;
    for (int index = 0; index < 5; index += 1) {
      student = new Student();
      student.setAddress("Address" + index);
      student.setAge(20 + index);
      student.setName("Name" + index);
      ormHelper.persist(student);
    }
  }

  private void testPersistenceOfInheritedObjectsWithJoinedStrategy(
      ORMHelper ormHelper) {
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE EMPLOYEE( NAME TEXT, _id INTEGER primary key autoincrement, EMPLOYEE_TYPE TEXT )");
    ormHelper.getWritableDatabase().execSQL(
        "CREATE TABLE PART_TIME_EMPLOYEE( _id INTEGER, HOURLY_RATE REAL )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE FULL_TIME_EMPLOYEE( _id INTEGER, SALARY INTEGER, PENSION INTEGER )");

    for (int index = 1; index < 5; index += 1) {
      PartTimeEmployee pte = new PartTimeEmployee();
      pte.setName("Tom " + index  + " Hanks");
      pte.setHourlyRate(12 + index);
      ormHelper.persist(pte);

      FullTimeEmployee fte = new FullTimeEmployee();
      fte.setName("Jerry " + index + " Seinfeld");
      fte.setSalary(1230 + index);
      fte.setPension(630 + index);
      ormHelper.persist(fte);
    }
  }

  private void testPersistenceOfInheritedObjectsWithTablePerClassStrategy(
      ORMHelper ormHelper) {

    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE SPORTSMAN( NAME TEXT, _id INTEGER primary key autoincrement )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE FOOTBALLER( NAME TEXT, _id INTEGER primary key autoincrement, TEAM TEXT, GOALS INTEGER )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE CRICKETER( NAME TEXT, _id INTEGER primary key autoincrement, TEAM TEXT, AVERAGE REAL )");

    Sportsman sportsman = new Sportsman();
    sportsman.setName("Vishwanathan Anand");
    ormHelper.persist(sportsman);

    Cricketer cricketer = new Cricketer();
    cricketer.setName("Saurav Ganguly");
    cricketer.setAverage(42.28f);
    cricketer.setTeam("India");
    ormHelper.persist(cricketer);

    Footballer footballer = new Footballer();
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
    testPersistenceOfInheritedObjectsWithJoinedStrategy(ormHelper);
    // testPersistenceOfInheritedObjectsWithTablePerClassStrategy(ormHelper);
  }
}
