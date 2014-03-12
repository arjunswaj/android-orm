package edu.iiitb.ormtestapp;

import edu.iiitb.ormtestapp.eo.Course;
import edu.iiitb.ormtestapp.eo.Student;
import edu.iiitb.ormtestapp.inheritance.joined.eo.FullTimeEmployee;
import edu.iiitb.ormtestapp.inheritance.joined.eo.Intern;
import edu.iiitb.ormtestapp.inheritance.joined.eo.PartTimeEmployee;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.CabinetMinister;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.Car;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.Ford;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.Minister;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.PrimeMinister;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Cricketer;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Footballer;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Sportsman;
import iiitb.dm.ormlibrary.ORMHelper;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

  private void testPersistence(ORMHelper ormHelper) {
    Course course = null;
    for (int index = 0; index < 25; index += 1) {
      course = new Course();
      course.setCourseDescription("Course" + index);
      course.setCourseName("DM" + index);
      course.setCredits(5);
      ormHelper.persist(course);
    }

    Student student = null;
    for (int index = 0; index < 25; index += 1) {
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
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE PART_TIME_EMPLOYEE( _id INTEGER, HOURLY_RATE REAL, PART_TIME_EMPLOYEE_TYPE TEXT )");
    ormHelper.getWritableDatabase().execSQL(
        "CREATE TABLE INTERN( _id INTEGER, STIPEND REAL )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE FULL_TIME_EMPLOYEE( _id INTEGER, SALARY INTEGER, PENSION INTEGER )");

    for (int index = 1; index < 25; index += 1) {
      Intern intern = new Intern();
      intern.setName("Ron " + index + " Clyde");
      intern.setHourlyRate(20 + index);
      intern.setStipend(200 + index);
      ormHelper.persist(intern);

      PartTimeEmployee pte = new PartTimeEmployee();
      pte.setName("Tom " + index + " Hanks");
      pte.setHourlyRate(12 + index);
      ormHelper.persist(pte);

      FullTimeEmployee fte = new FullTimeEmployee();
      fte.setName("Bon " + index + " Snype");
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

    for (int index = 1; index < 25; index += 1) {
      Sportsman sportsman = new Sportsman();
      sportsman.setName("Vishwanathan " + index + " Anand");
      ormHelper.persist(sportsman);

      Cricketer cricketer = new Cricketer();
      cricketer.setName("Saurav " + index + " Ganguly");
      cricketer.setAverage(42.28f + index);
      cricketer.setTeam("India" + index);
      ormHelper.persist(cricketer);

      Footballer footballer = new Footballer();
      footballer.setName("David " + index + " Beckham");
      footballer.setGoals(92 + index);
      footballer.setTeam("England" + index);
      ormHelper.persist(footballer);
    }
  }

  private void testPersistenceOfInheritedObjectsWithMixedStrategy(
      ORMHelper ormHelper) {
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE VEHICLE( MFG_YEAR INTEGER, _id INTEGER primary key autoincrement, VEHICLE_TYPE TEXT )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE CAR( COLOR TEXT, _id INTEGER primary key autoincrement, HORSE_POWER INTEGER )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE FORD( COLOR TEXT, _id INTEGER primary key autoincrement, HORSE_POWER INTEGER, MODEL TEXT )");

    for (int index = 1; index < 25; index += 1) {

      Car car = new Car();
      car.setColor("Red " + index);
      car.setHorsePower(775 + index);
      car.setMfgYear(1975 + index);
      ormHelper.persist(car);

      Ford ford = new Ford();
      ford.setColor("Blue " + index);
      ford.setHorsePower(885 + index);
      ford.setMfgYear(1985 + index);
      ford.setModel("Fiesta " + index);
      ormHelper.persist(ford);
    }

    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE MINISTER( STATE TEXT, _id INTEGER primary key autoincrement )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE CABINET_MINISTER( STATE TEXT, _id INTEGER primary key autoincrement, PORTFOLIO TEXT, SALARY REAL, MINISTER_TYPE TEXT )");
    ormHelper
        .getWritableDatabase()
        .execSQL(
            "CREATE TABLE PRIME_MINISTER( AGE INTEGER, _id INTEGER primary key autoincrement )");
    
    for (int index = 1; index < 25; index += 1) {
      Minister minister = new Minister();
      minister.setState("Karnataka" + index);
      ormHelper.persist(minister);
      
      CabinetMinister cabinetMinister = new CabinetMinister();
      cabinetMinister.setPortfolio("Home " + index + " Minister");
      cabinetMinister.setSalary(10000 + index);
      cabinetMinister.setState("Punjab " + index);
      ormHelper.persist(cabinetMinister);
      
      PrimeMinister pm = new PrimeMinister();
      pm.setAge(62 + index);
      pm.setPortfolio("Prime " + index + " Minister");
      pm.setSalary(15000 + index);
      pm.setState("Gujarat " + index);
      ormHelper.persist(pm);     
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ORMHelper ormHelper = new ORMHelper(getApplicationContext(),
        "testDB.sqlite", null, 1);

    // testPersistence(ormHelper);
    // testPersistenceOfInheritedObjectsWithJoinedStrategy(ormHelper);
    // testPersistenceOfInheritedObjectsWithTablePerClassStrategy(ormHelper);
    testPersistenceOfInheritedObjectsWithMixedStrategy(ormHelper);
  }

}
