package edu.iiitb.ormtestapp;

import iiitb.dm.ormlibrary.ORMHelper;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.app.Activity;

public class QueryActivity extends Activity {

	ListView listView = null;
	private static final String TAG = "QUERY BY LIST";

	private void testSimpleQueryByList(ORMHelper ormHelper) {
		Criteria criteria = ormHelper
				.createCriteria(Student.class)
				.add(Restrictions.like("name", "Name%"))
				.add(Restrictions.and(Restrictions.gt("cgpa", 1.0),
						Restrictions.lt("cgpa", 4.0)))
				.add(Restrictions.or(Restrictions.eq("age", 23),
						Restrictions.like("college", "IIIT-B 0")))
				.add(Restrictions.eq("address", "Address 5"));
		List<Student> studentList = criteria.list();
		for (Student student : studentList) {
			Log.d(TAG,
					"id: " + student.getId() + ", name: " + student.getName()
							+ ", age: " + student.getAge() + ", address: "
							+ student.getAddress() + ", cgpa: "
							+ student.getCgpa() + ", college: "
							+ student.getCollege());
		}
		List<ParcelableStudent> psList = ormHelper.createCriteria(
				ParcelableStudent.class).list();
		for (ParcelableStudent ps : psList) {
			Log.d(TAG, "id: " + ps.getId() + ", age: " + ps.getAge());
		}
	}

	private void testInheritanceJoinedSubClassQueryByList(ORMHelper ormHelper) {
		Criteria criteria = ormHelper.createCriteria(Intern.class);
		List<Intern> internList = criteria.add(
				Restrictions.between("stipend", 202, 203)).list();
		for (Intern intern : internList) {
			Log.d(TAG,
					"id: " + intern.getId() + ", stipend: "
							+ intern.getStipend() + ", hourly rate: "
							+ intern.getHourlyRate() + ", name: "
							+ intern.getName());
		}
	}

	private void testInQueryByList(ORMHelper ormHelper) {
		Criteria criteria = ormHelper.createCriteria(Intern.class);
		Integer[] stipendVal = { 201, 202, 203, 204, 205, 206 };
		List<Integer> age = new ArrayList<Integer>(100);
		for (int ctr = 1; ctr <= 100; ctr += 1) {
			age.add(ctr);
		}
		List<Intern> internList = criteria
				.add(Restrictions.in("stipend", stipendVal))
				.add(Restrictions.in("age", age)).list();
		for (Intern intern : internList) {
			Log.d(TAG,
					"id: " + intern.getId() + ", stipend: "
							+ intern.getStipend() + ", hourly rate: "
							+ intern.getHourlyRate() + ", name: "
							+ intern.getName() + ", age: " + intern.getAge());
		}
	}

	private void testInheritanceTablePerClassSubClassQueryByList(
			ORMHelper ormHelper) {
		Criteria criteria = ormHelper.createCriteria(Ford.class);
		List<Ford> fordList = criteria.add(Restrictions.gt("horsePower", 888))
				.list();
		for (Ford ford : fordList) {
			Log.d(TAG, "id: " + ford.getId() + ", color: " + ford.getColor()
					+ ", horse power: " + ford.getHorsePower() + ", mfg: "
					+ ford.getMfgYear() + ", model: " + ford.getModel());
		}
	}

	private void testInheritanceMixedSubClassQueryByList(ORMHelper ormHelper) {
		Criteria criteria = ormHelper.createCriteria(Footballer.class);
		List<Footballer> footballerList = criteria.add(
				Restrictions.or(Restrictions.ge("goals", 95),
						Restrictions.lt("goals", 94))).list();
		for (Footballer footballer : footballerList) {
			Log.d(TAG,
					"id: " + footballer.getId() + ", goals: "
							+ footballer.getGoals() + ", name: "
							+ footballer.getName() + ", team: "
							+ footballer.getTeam());
		}

		criteria = ormHelper.createCriteria(PrimeMinister.class);
		List<PrimeMinister> primeMinisters = criteria.add(
				Restrictions.eq("state", "Gujarat 2")).list();
		for (PrimeMinister pm : primeMinisters) {
			Log.d(TAG,
					"id: " + pm.getId() + ", age: " + pm.getAge()
							+ ", portfolio: " + pm.getPortfolio()
							+ ", salary: " + pm.getSalary() + ", state: "
							+ pm.getState());
		}
	}

	private void testInheritanceJoinedSuperClassQueryByList(ORMHelper ormHelper) {
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
					Log.d(TAG, "Intern = id: " + intern.getId() + ", name: "
							+ intern.getName() + ", age: " + intern.getAge()
							+ ", hourlyRate: " + intern.getHourlyRate()
							+ ", stipend: " + intern.getStipend());
				} else
					Log.d(TAG,
							"PartTimeEmployee = id: " + pte.getId()
									+ ", name: " + pte.getName() + ", age: "
									+ pte.getAge() + ", hourlyRate: "
									+ pte.getHourlyRate());
			} else if (employee instanceof FullTimeEmployee) {
				FullTimeEmployee fte = (FullTimeEmployee) employee;
				Log.d(TAG,
						"FullTimeEmployee = id: " + fte.getId() + ", name: "
								+ fte.getName() + ", age: " + fte.getAge()
								+ ", salary: " + fte.getSalary()
								+ ", pension: " + fte.getPension());
			} else {
				Log.e(TAG, "Employee(ERROR) = id: " + employee.getId()
						+ ", name: " + employee.getName() + ", age: "
						+ employee.getAge());
			}
		}
	}

	private void testInheritanceTablePerClassSuperClassQueryByList(
			ORMHelper ormHelper) {
		// Test case to test a TABLE_PER_CLASS inheritance hierarchy when the
		// root
		// entity class is queried
		Criteria criteria = ormHelper.createCriteria(Sportsman.class);
		List<Sportsman> sportsmen = criteria.add(Restrictions.gt("age", "30"))
				.list();
		for (Sportsman sportsman : sportsmen) {
			if (sportsman instanceof Cricketer) {
				Cricketer c = (Cricketer) sportsman;
				Log.d(TAG,
						"Cricketer = id: " + c.getId() + ", name: "
								+ c.getName() + ", age: " + c.getAge()
								+ ", team: " + c.getTeam() + ", average"
								+ c.getAverage());
			} else if (sportsman instanceof Footballer) {
				Footballer f = (Footballer) sportsman;
				Log.d(TAG,
						"Footballer = id: " + f.getId() + ", name: "
								+ f.getName() + ", age: " + f.getAge()
								+ ", team: " + f.getTeam() + ", goals"
								+ f.getGoals());
			} else {
				Log.d(TAG, "SportsMan = id: " + sportsman.getId() + ", name: "
						+ sportsman.getName() + ", age: " + sportsman.getAge());
			}
		}
	}

	private void testQueryManyToMany(ORMHelper ormHelper) {
		Criteria criteria = ormHelper.createCriteria(Person.class);
		List<Person> persons = criteria
				.add(Restrictions.eq("name", "Leslie Lamport 1"))
				.createCriteria("patents")
				.add(Restrictions.like("title", "Distributed Computing Algo3"))
				.list();
		for (Person p : persons) {
			Log.d(TAG, "id: " + p.getId() + ", name: " + p.getName());
			for (Patent patent : p.getPatents())
				Log.d(TAG, "id: " + patent.getTitle());
		}

		criteria = ormHelper.createCriteria(Country.class);
		List<Country> countries = criteria
				.add(Restrictions.like("name", "India 1"))
				.createCriteria("states")
				.add(Restrictions.like("name", "Karnataka 2"))
				.addOrder(Order.asc("id")).list();
		for (Country country : countries) {
			Log.d(TAG,
					"id: " + country.getId() + ", name: " + country.getName()
							+ ", capital: " + country.getCapital());
			for (State state : country.getStates())
				Log.d(TAG,
						"id: " + state.getId() + ", name: " + state.getName());
		}
	}

	private void testQuerySubCriteria(ORMHelper ormHelper) {
		Criteria criteria = ormHelper.createCriteria(Country.class);
		List<Country> countries = criteria
				.add(Restrictions.like("name", "India 1"))
				.createCriteria("capital")
				.add(Restrictions.like("name", "New 1 Delhi")).list();
		Log.d("QueryActivity", "Got " + countries.size()
				+ " objects of country");
		for (Country country : countries) {
			Log.d("QUERY BY LIST", "id: " + country.getId() + ", name: "
					+ country.getName() + ", capital: "
					+ country.getCapital().getName());
			for (State state : country.getStates())
				Log.d("QUERY BY LIST", "id: " + state.getId() + ", name: "
						+ state.getName());
		}
		criteria = ormHelper.createCriteria(Person.class);
		List<Person> persons = criteria
				.add(Restrictions.like("name", "Leslie Lamport 1"))
				.createCriteria("patents")
				.add(Restrictions.like("title", "%Distributed%"))
				.addOrder(Order.desc("title")).list();
		for (Person person : persons) {
			Log.d("Query BY LIST", "id: " + person.getId() + ", name: "
					+ person.getName());
			for (Patent patent : person.getPatents())
				Log.d("QUERY BY LIST", "id: " + patent.getTitle());
			for (Article article : person.getArticles())
				Log.d("QUERY BY LIST", "id: " + article.getName());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query);

		List<String> menuList = new ArrayList<String>();
		menuList.add("Simple Query");
		menuList.add("Inheritance Joined - Subclass");
		menuList.add("Inheritance Table Per Class - Subclass");
		menuList.add("Inheritance Mixed - Subclass");
		menuList.add("Inheritance Joined - Superclass");
		menuList.add("Inheritance Table Per Class - Superclass");
		menuList.add("Inheritance Joined - Many To Many");
		menuList.add("Query on Association - Sub Criteria");
		menuList.add("In Query on Inheritance");

		listView = (ListView) findViewById(R.id.queryList);
		listView.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, menuList));
		final ORMHelper ormHelper = ORMHelper
				.getInstance(getApplicationContext());
		;

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				switch (position) {
				case 0:
					testSimpleQueryByList(ormHelper);
					break;
				case 1:
					testInheritanceJoinedSubClassQueryByList(ormHelper);
					break;
				case 2:
					testInheritanceTablePerClassSubClassQueryByList(ormHelper);
					break;
				case 3:
					testInheritanceMixedSubClassQueryByList(ormHelper);
					break;
				case 4:
					testInheritanceJoinedSuperClassQueryByList(ormHelper);
					break;
				case 5:
					testInheritanceTablePerClassSuperClassQueryByList(ormHelper);
					break;
				case 6:
					testQueryManyToMany(ormHelper);
					break;
				case 7:
					testQuerySubCriteria(ormHelper);
					break;
				case 8:
					testInQueryByList(ormHelper);
					break;
				}
			}
		});
	}

}
