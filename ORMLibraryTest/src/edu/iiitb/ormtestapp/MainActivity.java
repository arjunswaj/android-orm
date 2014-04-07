package edu.iiitb.ormtestapp;

import iiitb.dm.ormlibrary.ORMHelper;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.iiitb.ormtestapp.composition.eo.Article;
import edu.iiitb.ormtestapp.composition.eo.Capital;
import edu.iiitb.ormtestapp.composition.eo.Country;
import edu.iiitb.ormtestapp.composition.eo.Patent;
import edu.iiitb.ormtestapp.composition.eo.Person;
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

  ListView listView = null;
  
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

	  Random randomGen = new Random();
    for (int index = 1; index < 5; index += 1) {
      Intern intern = new Intern();
      intern.setName("Ron " + index + " Clyde");
      intern.setHourlyRate(20 + index);
      intern.setStipend(200 + index);
      intern.setAge(randomGen.nextInt(100));
      ormHelper.persist(intern);

      PartTimeEmployee pte = new PartTimeEmployee();
      pte.setName("Tom " + index + " Hanks");
      pte.setHourlyRate(12 + index);
      pte.setAge(randomGen.nextInt(100));
      ormHelper.persist(pte);

      FullTimeEmployee fte = new FullTimeEmployee();
      fte.setName("Bon " + index + " Snype");
      fte.setSalary(1230 + index);
      fte.setPension(630 + index);
      fte.setAge(randomGen.nextInt(100));
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

	Random randomGen = new Random();
    for (int index = 1; index < 5; index += 1) {
      Sportsman sportsman = new Sportsman();
      sportsman.setName("Vishwanathan " + index + " Anand");
      sportsman.setAge(randomGen.nextInt(100));
      ormHelper.persist(sportsman);

      Cricketer cricketer = new Cricketer();
      cricketer.setName("Saurav " + index + " Ganguly");
      cricketer.setAverage(42.28f + index);
      cricketer.setTeam("India" + index);
      cricketer.setAge(randomGen.nextInt(100));
      ormHelper.persist(cricketer);

      Footballer footballer = new Footballer();
      footballer.setName("David " + index + " Beckham");
      footballer.setGoals(92 + index);
      footballer.setTeam("England" + index);
      footballer.setAge(randomGen.nextInt(100));
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

   private void testManyToManyPersistance(ORMHelper ormHelper) 
  {
		// unidirectional many to many mapping
		Collection<Patent> patents1 = new ArrayList<Patent>();
		Collection<Patent> patents2 = new ArrayList<Patent>();
		Collection<Patent> patents3 = new ArrayList<Patent>();
		for (int index = 1; index < 5; index += 1)
		{
			Patent patent = new Patent();
			patent.setTitle("Distributed Computing Algo " + index);
			switch(index)
			{
			case 1: patents1.add(patent);
			break;
			case 2: patents2.add(patent);
			break;
			case 3: patents3.add(patent);
			break;
			case 4:
				patents1.add(patent);
				patents2.add(patent);
				patents3.add(patent);
				break;
			}
		}

		Collection<Person> persons = new ArrayList<Person>();
		for (int group = 1; group < 4; group += 1)
		{
			Person person = new Person();
			person.setName("Leslie Lamport " + group);
			persons.add(person);
			switch(group)
			{
			case 1: person.setPatents(patents1); // 1 & 4
			break;
			case 2: person.setPatents(patents2);// 2 & 4
			break;
			case 3: person.setPatents(patents3); // 3 & 4
			break;
			}
		}

		// Bidirectional many to many mapping(commented out until query for bidirectional is done)
		Collection<Article> articles = new ArrayList<Article>();
		for (int index = 1; index < 5; index += 1)
		{
			Article article = new Article();
			article.setName("Article " + index);
			//article.setPersons(persons);
			
			articles.add(article);
		}

		for (Person person : persons)
		{
			person.setArticles(articles);
			ormHelper.persist(person);
		}
  }
   
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    List<String> menuList = new ArrayList<String>();
    menuList.add("Simple Persistence");
    menuList.add("Inheritance Joined");
    menuList.add("Inheritance Table Per Class");
    menuList.add("Inheritance Mixed");
    menuList.add("Composition - 1-1, 1-Many");
    menuList.add("Composition - Many-Many");
    menuList.add("Query");
    
    listView = (ListView) findViewById(R.id.mainList);
    listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, menuList));
    final ORMHelper ormHelper = new ORMHelper(getApplicationContext(),
        "testDB.sqlite", null, 1);
    
    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position,
          long id) {
        switch (position) {
        case 0:
          testPersistence(ormHelper);
          break;
        case 1:
          testPersistenceOfInheritedObjectsWithJoinedStrategy(ormHelper);
          break;
        case 2:
          testPersistenceOfInheritedObjectsWithTablePerClassStrategy(ormHelper);
          break;
        case 3:
          testPersistenceOfInheritedObjectsWithMixedStrategy(ormHelper);
          break;
        case 4:
          testPersistenceOfComposition(ormHelper);
          break;
        case 5:
          testManyToManyPersistance(ormHelper);
          break;
        case 6:
          Intent queryScreen = new Intent(MainActivity.this, QueryActivity.class);
          startActivity(queryScreen);
          break;        
        }
      }
    });        
    
  }
}
