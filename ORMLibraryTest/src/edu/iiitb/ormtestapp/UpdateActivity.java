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
import edu.iiitb.ormtestapp.eo.ParcelableStudent;
import edu.iiitb.ormtestapp.eo.Student;
import edu.iiitb.ormtestapp.inheritance.joined.eo.Employee;
import edu.iiitb.ormtestapp.inheritance.joined.eo.FullTimeEmployee;
import edu.iiitb.ormtestapp.inheritance.joined.eo.Intern;
import edu.iiitb.ormtestapp.inheritance.joined.eo.PartTimeEmployee;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.Ford;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.PrimeMinister;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Cricketer;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Footballer;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Sportsman;

public class UpdateActivity extends Activity {

  ListView listView = null;

  private static final String TAG = "DELETE AFTER QUERY";

  private void testSimpleUpdate(ORMHelper ormHelper) {
    Criteria criteria = ormHelper
        .createCriteria(Student.class)
        .add(Restrictions.like("name", "Name%"))
        .add(
            Restrictions.and(Restrictions.gt("cgpa", 1.0),
                Restrictions.lt("cgpa", 4.0)))
        .add(
            Restrictions.or(Restrictions.eq("age", 23),
                Restrictions.like("college", "IIIT-B 0")))
        .add(Restrictions.eq("address", "Address 5"));
    List<Student> studentList = criteria.list();
    for (Student student : studentList) {
      Log.d(
          TAG,
          "id: " + student.getId() + ", name: " + student.getName() + ", age: "
              + student.getAge() + ", address: " + student.getAddress()
              + ", cgpa: " + student.getCgpa() + ", college: "
              + student.getCollege());
      student.setCollege("IIT - Madras");
      student.setAddress("42, M G Road, Chennai");
      ormHelper.update(student);
    }
    criteria = ormHelper.createCriteria(ParcelableStudent.class);
    List<ParcelableStudent> psList = criteria.list();
    for (ParcelableStudent ps: psList) {
    	ps.setAge(ps.getAge() + 1);
    	ormHelper.update(ps);
    }

  }

  private void testInheritanceJoinedSubClassUpdate(ORMHelper ormHelper) {
    Criteria criteria = ormHelper.createCriteria(Intern.class);
    List<Intern> internList = criteria.add(
        Restrictions.between("stipend", 202, 203)).list();
    for (Intern intern : internList) {
      Log.d(TAG,
          "id: " + intern.getId() + ", stipend: " + intern.getStipend()
              + ", hourly rate: " + intern.getHourlyRate() + ", name: "
              + intern.getName());
      intern.setName("Yevgeny Kefeilnikov");
      ormHelper.update(intern);
    }
  }

  private void testInheritanceTablePerClassSubClassUpdate(ORMHelper ormHelper) {
    Criteria criteria = ormHelper.createCriteria(Ford.class);
    List<Ford> fordList = criteria.add(Restrictions.gt("horsePower", 888))
        .list();
    for (Ford ford : fordList) {
      Log.d(
          TAG,
          "id: " + ford.getId() + ", color: " + ford.getColor()
              + ", horse power: " + ford.getHorsePower() + ", mfg: "
              + ford.getMfgYear() + ", model: " + ford.getModel());
      ford.setColor("Whitesmoke");
      ormHelper.update(ford);

    }
  }

  private void testInheritanceMixedSubClassUpdate(ORMHelper ormHelper) {
    Criteria criteria = ormHelper.createCriteria(Footballer.class);
    List<Footballer> footballerList = criteria.add(
        Restrictions.or(Restrictions.ge("goals", 95),
            Restrictions.lt("goals", 94))).list();
    for (Footballer footballer : footballerList) {
      Log.d(
          TAG,
          "id: " + footballer.getId() + ", goals: " + footballer.getGoals()
              + ", name: " + footballer.getName() + ", team: "
              + footballer.getTeam());
      footballer.setTeam("Bangalore FC");
      ormHelper.update(footballer);
    }

    criteria = ormHelper.createCriteria(PrimeMinister.class);
    List<PrimeMinister> primeMinisters = criteria.add(
        Restrictions.eq("state", "Gujarat 2")).list();
    for (PrimeMinister pm : primeMinisters) {
      Log.d(TAG, "id: " + pm.getId() + ", age: " + pm.getAge()
          + ", portfolio: " + pm.getPortfolio() + ", salary: " + pm.getSalary()
          + ", state: " + pm.getState());
      pm.setPortfolio("Railways");
      ormHelper.update(pm);
    }
  }

  private void testInheritanceJoinedSuperClassUpdate(ORMHelper ormHelper) {
    // Test case to test a JOINED inheritance hierarchy when the root entity
    // class is queried
    Criteria criteria = ormHelper.createCriteria(Employee.class);
    List<Employee> employees = criteria.add(Restrictions.gt("age", "30"))
        .list();
    for (Employee employee : employees) {
      if (employee instanceof PartTimeEmployee) {
        PartTimeEmployee pte = (PartTimeEmployee) employee;
        if (pte instanceof Intern) {
          Intern intern = (Intern) pte;
          Log.d(
              TAG,
              "Intern = id: " + intern.getId() + ", name: " + intern.getName()
                  + ", age: " + intern.getAge() + ", hourlyRate: "
                  + intern.getHourlyRate() + ", stipend: "
                  + intern.getStipend());
          intern.setHourlyRate(50);
          ormHelper.update(intern);
        } else
          Log.d(
              TAG,
              "PartTimeEmployee = id: " + pte.getId() + ", name: "
                  + pte.getName() + ", age: " + pte.getAge() + ", hourlyRate: "
                  + pte.getHourlyRate());
        pte.setHourlyRate(280);
        ormHelper.update(pte);
      } else if (employee instanceof FullTimeEmployee) {
        FullTimeEmployee fte = (FullTimeEmployee) employee;
        Log.d(
            TAG,
            "FullTimeEmployee = id: " + fte.getId() + ", name: "
                + fte.getName() + ", age: " + fte.getAge() + ", salary: "
                + fte.getSalary() + ", pension: " + fte.getPension());
        fte.setPension(5000);
        ormHelper.update(fte);
      } else {
        Log.e(TAG, "Employee(ERROR) = id: " + employee.getId() + ", name: "
            + employee.getName() + ", age: " + employee.getAge());
        employee.setName("Markov");
        ormHelper.update(employee);
      }
    }
  }

  private void testInheritanceTablePerClassSuperClassUpdate(ORMHelper ormHelper) {
    // Test case to test a TABLE_PER_CLASS inheritance hierarchy when the root
    // entity class is queried
    Criteria criteria = ormHelper.createCriteria(Sportsman.class);
    List<Sportsman> sportsmen = criteria.add(Restrictions.gt("age", "30"))
        .list();
    for (Sportsman sportsman : sportsmen) {
      if (sportsman instanceof Cricketer) {
        Cricketer c = (Cricketer) sportsman;
        Log.d(TAG, "Cricketer = id: " + c.getId() + ", name: " + c.getName()
            + ", age: " + c.getAge() + ", team: " + c.getTeam() + ", average"
            + c.getAverage());
        c.setTeam("Kolkata Knight Riders");
        ormHelper.update(c);
      } else if (sportsman instanceof Footballer) {
        Footballer f = (Footballer) sportsman;
        Log.d(TAG,
            "Footballer = id: " + f.getId() + ", name: " + f.getName()
                + ", age: " + f.getAge() + ", team: " + f.getTeam() + ", goals"
                + f.getGoals());
        f.setTeam("Manchester United");
        ormHelper.update(f);
      } else {
        Log.d(TAG, "SportsMan = id: " + sportsman.getId() + ", name: "
            + sportsman.getName() + ", age: " + sportsman.getAge());
        sportsman.setAge(42);
        ormHelper.update(sportsman);
      }
    }
  }

  private void testUpdateManyToMany(ORMHelper ormHelper) {
    Criteria criteria = ormHelper.createCriteria(Person.class);
    List<Person> persons = criteria
        .add(Restrictions.eq("name", "Leslie Lamport 1"))
        .createCriteria("patents")
        .add(Restrictions.like("title", "Distributed Computing Algo3")).list();
    for (Person p : persons) {
      Log.d(TAG, "id: " + p.getId() + ", name: " + p.getName());
      p.setName("Donald Knuth");
      ormHelper.update(p);
      for (Patent patent : p.getPatents()) {
        Log.d(TAG, "id: " + patent.getTitle());
        patent.setTitle("Art of Programming");
        ormHelper.update(patent);
      }
    }

    criteria = ormHelper.createCriteria(Country.class);
    List<Country> countries = criteria
        .add(Restrictions.like("name", "India 1")).createCriteria("states")
        .add(Restrictions.like("name", "Karnataka 2")).list();
    for (Country country : countries) {
      Log.d(TAG, "id: " + country.getId() + ", name: " + country.getName()
          + ", capital: " + country.getCapital());
      country.setName("Undivided India");
      ormHelper.update(country);
      for (State state : country.getStates()) {
        Log.d(TAG, "id: " + state.getId() + ", name: " + state.getName());
        state.setName("Hyderabad");
        ormHelper.update(state);
      }
    }
  }

  private void testUpdateSubCriteria(ORMHelper ormHelper) {
    Log.v("testUpdateSubCritera", "Hello.... HELLLLLLOOOOO");
    Criteria criteria = ormHelper.createCriteria(Country.class);
    List<Country> countries = criteria
        .add(Restrictions.like("name", "India 1")).createCriteria("capital")
        .add(Restrictions.like("name", "New 1 Delhi")).list();
    Log.d("UpdateActivity", "Got " + countries.size() + " objects of country");
    for (Country country : countries) {
      Log.d(TAG, "id: " + country.getId() + ", name: " + country.getName()
          + ", capital: " + country.getCapital().getName());
      country.setName("Undivided India");
      ormHelper.update(country);
      for (State state : country.getStates()) {
        Log.d(TAG, "id: " + state.getId() + ", name: " + state.getName());
        state.setName("Hyderabad");
        ormHelper.update(state);
      }
    }
    criteria = ormHelper.createCriteria(Person.class);
    List<Person> persons = criteria
        .add(Restrictions.like("name", "Leslie Lamport 1"))
        .createCriteria("patents")
        .add(Restrictions.like("title", "%Distributed%")).list();
    for (Person person : persons) {
      Log.d(TAG, "id: " + person.getId() + ", name: " + person.getName());
      person.setName("Donald Knuth");
      ormHelper.update(person);
      for (Patent patent : person.getPatents()) {
        Log.d(TAG, "id: " + patent.getTitle());
        patent.setTitle("KMP String match");
        ormHelper.update(patent);
      }
      for (Article article : person.getArticles()) {
        Log.d(TAG, "id: " + article.getName());
        article.setName("GOTO is harmful");
        ormHelper.update(person);
      }
    }
  }

  private void testUpdateOfOneToOneAndOneToManyAssociation(ORMHelper ormHelper) {
    // Persist
    Country country = new Country();
    country.setName("India");
    Capital capital = new Capital();
    capital.setName("New Delhi");
    Collection<State> states = new ArrayList<State>();
    State karnataka = new State();
    karnataka.setName("Karnataka ");
    states.add(karnataka);
    State kerala = new State();
    kerala.setName("Kerala");
    states.add(kerala);
    country.setStates(states);
    country.setCapital(capital);
    ormHelper.persist(country);

    // Update object
    country.setName("Bharata");
    country.getCapital().setName("Dilli");
    for (State state : country.getStates())
      state.setName("Karunaadu");
    ormHelper.update(country);

    // Delink existing 1-1 association and create a new 1-1 association
    Capital newCapital = new Capital();
    newCapital.setName("Bangalore");
    ormHelper.persist(newCapital);
    country.setCapital(newCapital);

    // 1-M association with new object
    State newState = new State();
    newState.setName("Telengana");
    ormHelper.persist(newState);
    country.getStates().add(newState);

    ormHelper.update(country);
  }

  private void testUpdateOfManyToManyAssociation(ORMHelper ormHelper) {
    Person leslie = new Person();
    leslie.setName("Leslie");
    Person donald = new Person();
    donald.setName("Donald");

    Patent distributedAlgo = new Patent();
    distributedAlgo.setTitle("Distributed");
    Patent latex = new Patent();
    latex.setTitle("Latex");
    Patent sortingAlgo = new Patent();
    sortingAlgo.setTitle("Sorting");

    Article java = new Article();
    java.setName("Java");
    Collection<Article> articles = new ArrayList<Article>();
    articles.add(java);

    Collection<Patent> lesliePatents = new ArrayList<Patent>();
    lesliePatents.add(distributedAlgo);
    lesliePatents.add(latex);

    Collection<Patent> donaldPatents = new ArrayList<Patent>();
    donaldPatents.add(latex);
    donaldPatents.add(sortingAlgo);

    leslie.setPatents(lesliePatents);
    leslie.setArticles(articles);
    donald.setPatents(donaldPatents);
    donald.setArticles(articles);
    ormHelper.persist(leslie);
    ormHelper.persist(donald);

    distributedAlgo.setTitle("Distributed computing algorithm");
    latex.setTitle("Latex Algorithm");
    sortingAlgo.setTitle("Sorting Algorithm");
    leslie.setName("Leslie Lamport");
    leslie.getPatents().add(sortingAlgo);
    ormHelper.update(leslie);

    donald.setName("Donald Knuth");
    donald.getPatents().remove(latex);
    ormHelper.update(donald);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_update);

    List<String> menuList = new ArrayList<String>();
    menuList.add("Simple Update");
    menuList.add("Inheritance Joined - Subclass");
    menuList.add("Inheritance Table Per Class - Subclass");
    menuList.add("Inheritance Mixed - Subclass");
    menuList.add("Inheritance Joined - Superclass");
    menuList.add("Inheritance Table Per Class - Superclass");
    menuList.add("Update on Association - Sub Criteria");

    menuList.add("Update on object which has 1-1 and 1-N associations");
    menuList.add("Update on object which has M-N associations");

    listView = (ListView) findViewById(R.id.updateList);
    listView.setAdapter(new ArrayAdapter(this,
        android.R.layout.simple_list_item_1, menuList));
    final ORMHelper ormHelper = ORMHelper.getInstance(getApplicationContext());

    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position,
          long id) {
        switch (position) {
        case 0:
          testSimpleUpdate(ormHelper);
          break;
        case 1:
          testInheritanceJoinedSubClassUpdate(ormHelper);
          break;
        case 2:
          testInheritanceTablePerClassSubClassUpdate(ormHelper);
          break;
        case 3:
          testInheritanceMixedSubClassUpdate(ormHelper);
          break;
        case 4:
          testInheritanceJoinedSuperClassUpdate(ormHelper);
          break;
        case 5:
          testInheritanceTablePerClassSuperClassUpdate(ormHelper);
          break;
        case 6:
          testUpdateSubCriteria(ormHelper);
          break;
        case 7:
          testUpdateOfOneToOneAndOneToManyAssociation(ormHelper);
          break;
        case 8:
          testUpdateOfManyToManyAssociation(ormHelper);
          break;
        }
      }
    });
  }
}
