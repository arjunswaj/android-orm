package edu.iiitb.ormtestapp;

import iiitb.dm.ormlibrary.ORMHelper;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.criterion.Restrictions;

import java.util.ArrayList;
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

public class DeleteActivity extends Activity {

  ListView listView = null;
  private static final String TAG = "DELETE AFTER QUERY";

  private void testSimpleDelete(ORMHelper ormHelper) {
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
      ormHelper.delete(student);
    }

    criteria = ormHelper.createCriteria(ParcelableStudent.class);
    List<ParcelableStudent> psList = criteria.list();
    for (ParcelableStudent ps: psList) {    	
    	ormHelper.delete(ps);
    }
  }

  private void testInheritanceJoinedSubClassDelete(ORMHelper ormHelper) {
    Criteria criteria = ormHelper.createCriteria(Intern.class);
    List<Intern> internList = criteria.add(
        Restrictions.between("stipend", 202, 203)).list();
    for (Intern intern : internList) {
      Log.d(TAG,
          "id: " + intern.getId() + ", stipend: " + intern.getStipend()
              + ", hourly rate: " + intern.getHourlyRate() + ", name: "
              + intern.getName());
      ormHelper.delete(intern);
    }
  }

  private void testInheritanceTablePerClassSubClassDelete(
      ORMHelper ormHelper) {
    Criteria criteria = ormHelper.createCriteria(Ford.class);
    List<Ford> fordList = criteria.add(Restrictions.gt("horsePower", 888))
        .list();
    for (Ford ford : fordList) {
      Log.d(
          TAG,
          "id: " + ford.getId() + ", color: " + ford.getColor()
              + ", horse power: " + ford.getHorsePower() + ", mfg: "
              + ford.getMfgYear() + ", model: " + ford.getModel());
      ormHelper.delete(ford);
    }
  }

  private void testInheritanceMixedSubClassDelete(ORMHelper ormHelper) {
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
      ormHelper.delete(footballer);
    }

    criteria = ormHelper.createCriteria(PrimeMinister.class);
    List<PrimeMinister> primeMinisters = criteria.add(
        Restrictions.eq("state", "Gujarat 2")).list();
    for (PrimeMinister pm : primeMinisters) {
      Log.d(TAG, "id: " + pm.getId() + ", age: " + pm.getAge()
          + ", portfolio: " + pm.getPortfolio() + ", salary: " + pm.getSalary()
          + ", state: " + pm.getState());
      ormHelper.delete(pm);
    }
  }

  private void testInheritanceJoinedSuperClassDelete(ORMHelper ormHelper) {
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
          ormHelper.delete(intern);
        } else
          Log.d(
              TAG,
              "PartTimeEmployee = id: " + pte.getId() + ", name: "
                  + pte.getName() + ", age: " + pte.getAge() + ", hourlyRate: "
                  + pte.getHourlyRate());
        ormHelper.delete(pte);
      } else if (employee instanceof FullTimeEmployee) {
        FullTimeEmployee fte = (FullTimeEmployee) employee;
        Log.d(
            TAG,
            "FullTimeEmployee = id: " + fte.getId() + ", name: "
                + fte.getName() + ", age: " + fte.getAge() + ", salary: "
                + fte.getSalary() + ", pension: " + fte.getPension());
        ormHelper.delete(fte);
      } else {
        Log.e(TAG, "Employee(ERROR) = id: " + employee.getId() + ", name: "
            + employee.getName() + ", age: " + employee.getAge());
        ormHelper.delete(employee);
      }
    }
  }

  private void testInheritanceTablePerClassSuperClassDelete(
      ORMHelper ormHelper) {
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
        ormHelper.delete(c);
      } else if (sportsman instanceof Footballer) {
        Footballer f = (Footballer) sportsman;
        Log.d(TAG,
            "Footballer = id: " + f.getId() + ", name: " + f.getName()
                + ", age: " + f.getAge() + ", team: " + f.getTeam() + ", goals"
                + f.getGoals());
        ormHelper.delete(f);
      } else {
        Log.d(TAG, "SportsMan = id: " + sportsman.getId() + ", name: "
            + sportsman.getName() + ", age: " + sportsman.getAge());
        ormHelper.delete(sportsman);
      }
    }
  }

  private void testDeleteManyToMany(ORMHelper ormHelper) {
    Criteria criteria = ormHelper.createCriteria(Person.class);
    List<Person> persons = criteria
        .add(Restrictions.eq("name", "Leslie Lamport 1"))
        .createCriteria("patents")
        .add(Restrictions.like("title", "Distributed Computing Algo3")).list();
    for (Person p : persons) {
      Log.d(TAG, "id: " + p.getId() + ", name: " + p.getName());
      ormHelper.delete(p);
      for (Patent patent : p.getPatents()) {
        Log.d(TAG, "id: " + patent.getTitle());
        ormHelper.delete(patent);
      }
    }

    criteria = ormHelper.createCriteria(Country.class);
    List<Country> countries = criteria
        .add(Restrictions.like("name", "India 1")).createCriteria("states")
        .add(Restrictions.like("name", "Karnataka 2")).list();
    for (Country country : countries) {
      Log.d(TAG, "id: " + country.getId() + ", name: " + country.getName()
          + ", capital: " + country.getCapital());
      ormHelper.delete(country);
      for (State state : country.getStates()) {
        Log.d(TAG, "id: " + state.getId() + ", name: " + state.getName());
        ormHelper.delete(state);
      }
    }
  }

  private void testDeleteSubCriteria(ORMHelper ormHelper) {
    Log.v("testDeleteSubCritera", "Hello.... HELLLLLLOOOOO");
    Criteria criteria = ormHelper.createCriteria(Country.class);
    List<Country> countries = criteria
        .add(Restrictions.like("name", "India 1")).createCriteria("capital")
        .add(Restrictions.like("name", "New 1 Delhi")).list();
    Log.d("DeleteActivity", "Got " + countries.size() + " objects of country");
    for (Country country : countries) {
      Log.d(TAG, "id: " + country.getId() + ", name: " + country.getName()
          + ", capital: " + country.getCapital().getName());
      ormHelper.delete(country);
      for (State state : country.getStates()) {
        Log.d(TAG, "id: " + state.getId() + ", name: " + state.getName());
        ormHelper.delete(state);
      }
    }
    criteria = ormHelper.createCriteria(Person.class);
    List<Person> persons = criteria
        .add(Restrictions.like("name", "Leslie Lamport 1"))
        .createCriteria("patents")
        .add(Restrictions.like("title", "%Distributed%")).list();
    for (Person person : persons) {
      Log.d(TAG, "id: " + person.getId() + ", name: " + person.getName());
      ormHelper.delete(person);
      for (Patent patent : person.getPatents()) {
        Log.d(TAG, "id: " + patent.getTitle());
        ormHelper.delete(patent);
      }
      for (Article article : person.getArticles()) {
        Log.d(TAG, "id: " + article.getName());
        ormHelper.delete(person);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_delete);

    List<String> menuList = new ArrayList<String>();
    menuList.add("Simple Delete");
    menuList.add("Inheritance Joined - Subclass");
    menuList.add("Inheritance Table Per Class - Subclass");
    menuList.add("Inheritance Mixed - Subclass");
    menuList.add("Inheritance Joined - Superclass");
    menuList.add("Inheritance Table Per Class - Superclass");
    menuList.add("Delete on Association - Sub Criteria");

    listView = (ListView) findViewById(R.id.deleteList);
    listView.setAdapter(new ArrayAdapter(this,
        android.R.layout.simple_list_item_1, menuList));
    final ORMHelper ormHelper = ORMHelper.getInstance(getApplicationContext());

    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position,
          long id) {
        switch (position) {
        case 0:
          testSimpleDelete(ormHelper);
          break;
        case 1:
          testInheritanceJoinedSubClassDelete(ormHelper);
          break;
        case 2:
          testInheritanceTablePerClassSubClassDelete(ormHelper);
          break;
        case 3:
          testInheritanceMixedSubClassDelete(ormHelper);
          break;
        case 4:
          testInheritanceJoinedSuperClassDelete(ormHelper);
          break;
        case 5:
          testInheritanceTablePerClassSuperClassDelete(ormHelper);
          break;
        case 6:
          testDeleteSubCriteria(ormHelper);
          break;
        }
      }
    });
  }
}
