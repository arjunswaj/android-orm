package edu.iiitb.ormtestapp;

import iiitb.dm.ormlibrary.ORMHelper;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import edu.iiitb.ormtestapp.composition.eo.Capital;
import edu.iiitb.ormtestapp.composition.eo.Country;
import edu.iiitb.ormtestapp.composition.eo.Patent;
import edu.iiitb.ormtestapp.composition.eo.Person;
import edu.iiitb.ormtestapp.composition.eo.Phone;
import edu.iiitb.ormtestapp.composition.eo.State;
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
      student = new Student(21 + (index % 3), "Name " + (index % 5), "Address "
          + (index % 9), 1.0f + (float) (index / 8), "IIIT-B " + (index % 4));
      ormHelper.persist(student);
    }
  }

  private void testPersistenceOfInheritedObjectsWithJoinedStrategy(
      ORMHelper ormHelper) {
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE EMPLOYEE( NAME TEXT, _id INTEGER primary key autoincrement, EMPLOYEE_TYPE TEXT )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE PART_TIME_EMPLOYEE( _id INTEGER, HOURLY_RATE REAL, PART_TIME_EMPLOYEE_TYPE TEXT )");
    // ormHelper.getWritableDatabase().execSQL(
    // "CREATE TABLE INTERN( _id INTEGER, STIPEND REAL )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE FULL_TIME_EMPLOYEE( _id INTEGER, SALARY INTEGER, PENSION INTEGER )");

    for (int index = 1; index < 5; index += 1) {
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

    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE SPORTSMAN( NAME TEXT, _id INTEGER primary key autoincrement )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE FOOTBALLER( NAME TEXT, _id INTEGER primary key autoincrement, TEAM TEXT, GOALS INTEGER )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE CRICKETER( NAME TEXT, _id INTEGER primary key autoincrement, TEAM TEXT, AVERAGE REAL )");

    for (int index = 1; index < 5; index += 1) {
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
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE VEHICLE( MFG_YEAR INTEGER, _id INTEGER primary key autoincrement, VEHICLE_TYPE TEXT )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE CAR( COLOR TEXT, _id INTEGER primary key autoincrement, HORSE_POWER INTEGER )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE FORD( COLOR TEXT, _id INTEGER primary key autoincrement, HORSE_POWER INTEGER, MODEL TEXT )");

    for (int index = 1; index < 5; index += 1) {

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

    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE MINISTER( STATE TEXT, _id INTEGER primary key autoincrement )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE CABINET_MINISTER( STATE TEXT, _id INTEGER primary key autoincrement, PORTFOLIO TEXT, SALARY REAL, MINISTER_TYPE TEXT )");
    // ormHelper
    // .getWritableDatabase()
    // .execSQL(
    // "CREATE TABLE PRIME_MINISTER( AGE INTEGER, _id INTEGER primary key autoincrement )");

    for (int index = 1; index < 5; index += 1) {
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

  private void testPersistenceOfComposition(ORMHelper ormHelper) {
    /*
     * ormHelper .getWritableDatabase() .execSQL(
     * "CREATE TABLE COUNTRY( NAME TEXT, _id INTEGER primary key autoincrement, CAPITAL_ID INTEGER )"
     * ); ormHelper .getWritableDatabase() .execSQL(
     * "CREATE TABLE CAPITAL( NAME TEXT, _id INTEGER primary key autoincrement )"
     * ); ormHelper .getWritableDatabase() .execSQL(
     * "CREATE TABLE STATES( NAME TEXT, _id INTEGER primary key autoincrement, COUNTRY_ID INTEGER )"
     * );
     */
    for (int index = 1; index < 5; index += 1) {
      Country country = new Country();
      country.setName("India " + index);

      Capital capital = new Capital();
      capital.setName("New " + index + " Delhi");

      Collection<State> states = new ArrayList<State>();
      for (int stateIndex = 0; stateIndex < 5; stateIndex += 1) {
        State state = new State();
        state.setName(index + " Karnataka " + stateIndex);
        states.add(state);
      }
      country.setStates(states);
      country.setCapital(capital);
      ormHelper.persist(country);
    }
  }

  private void testManyToManyPersistance(ORMHelper ormHelper) {
	  
	Collection<Person> persons = new ArrayList<Person>();
	
	for (int index = 1; index < 3; index += 1)
	{
		Person person = new Person();
		person.setName("Leslie Lamport " + index);
		persons.add(person);
	}
	
    Collection<Patent> patents = new ArrayList<Patent>();
    for (int index = 1; index < 5; index += 1) {
      Patent patent = new Patent();
      patent.setTitle("Distributed Computing Algo" + index);
      patents.add(patent);
    }
    
    for (Person person : persons)
    	person.setPatents(patents);
    
    Collection<Phone> phones = new ArrayList<Phone>();
	for (int index = 1; index < 5; index += 1)
	{
		Phone phone = new Phone();
		phone.setNumber(100000000 + index);
		phone.setPersons(persons);
		phones.add(phone);
	}
	
	for (Person person : persons)
	{
		person.setPhones(phones);
		ormHelper.persist(person);
	}
  }
   
  private void testQueryByList(ORMHelper ormHelper) {
    Criteria criteria = ormHelper
        .createCriteria(Student.class)
        .add(Restrictions.like("NAME", "Name%"))
        .add(
            Restrictions.and(Restrictions.gt("CGPA", 1.0),
                Restrictions.lt("CGPA", 4.0)))
        .add(
            Restrictions.or(Restrictions.eq("AGE", 23),
                Restrictions.like("COLLEGE", "IIIT-B 0")))
        .add(Restrictions.eq("ADDRESS", "Address 5"));
    List<Student> studentList = criteria.list();
    for(Student student: studentList) {
      Log.d("QUERY BY LIST", "id: " + student.getId() + ", name: " + student.getName() + ", age: "
          + student.getAge() + ", address: " + student.getAddress() + ", cgpa: " + student.getCgpa() + ", college: "
          + student.getCollege());
    }
    
    criteria = ormHelper
        .createCriteria(Intern.class);
    List<Intern> internList = criteria.add(
        Restrictions.and(Restrictions.ge("stipend", 202),
            Restrictions.le("stipend", 203))).list();
    for(Intern intern: internList) {
      Log.d("QUERY BY LIST", "id: " + intern.getId() + 
          ", stipend: " + intern.getStipend() + ", hourly rate: " 
          + intern.getHourlyRate() + ", name: "+ intern.getName());
    }
    
    criteria = ormHelper
        .createCriteria(Ford.class);
    List<Ford> fordList = criteria.add(
        Restrictions.gt("horse_power", 888)).list();
    for(Ford ford: fordList) {
      Log.d(
          "QUERY BY LIST",
          "id: " + ford.getId() + ", color: " + ford.getColor()
              + ", horse power: " + ford.getHorsePower() + ", mfg: "
              + ford.getMfgYear() + ", model: " + ford.getModel());
    }
    
    criteria = ormHelper.createCriteria(Footballer.class);
    List<Footballer> footballerList = criteria.add(
        Restrictions.or(Restrictions.ge("goals", 95),
            Restrictions.lt("goals", 94))).list();
    for (Footballer footballer : footballerList) {
      Log.d("QUERY BY LIST", "id: " + footballer.getId() + ", goals: "
          + footballer.getGoals() + ", name: " + footballer.getName()
          + ", team: " + footballer.getTeam());
    }
    
    criteria = ormHelper.createCriteria(Sportsman.class);
    List<Sportsman> sportsmanList = criteria.list();
    for (Sportsman sportsman : sportsmanList) {
      Log.d("QUERY BY LIST", "id: " + sportsman.getId()
          + ", name: " + sportsman.getName());
    }
    
    criteria = ormHelper.createCriteria(PrimeMinister.class);
    List<PrimeMinister> primeMinisters = criteria.add(
        Restrictions.eq("state", "Gujarat 2")).list();
    for (PrimeMinister pm : primeMinisters) {
      Log.d("QUERY BY LIST", "id: " + pm.getId() + ", age: " + pm.getAge()
          + ", portfolio: " + pm.getPortfolio() + ", salary: " + pm.getSalary()
          + ", state: " + pm.getState());
    }
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ORMHelper ormHelper = new ORMHelper(getApplicationContext(),
        "testDB.sqlite", null, 1);

    testPersistence(ormHelper);
    testPersistenceOfInheritedObjectsWithJoinedStrategy(ormHelper);
    testPersistenceOfInheritedObjectsWithTablePerClassStrategy(ormHelper);
    testPersistenceOfInheritedObjectsWithMixedStrategy(ormHelper);
    testPersistenceOfComposition(ormHelper);
    testManyToManyPersistance(ormHelper);
    
    testQueryByList(ormHelper);
    
  }
}
