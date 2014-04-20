package edu.iiitb.ormtestapp;

import iiitb.dm.ormlibrary.ORMHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
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
import edu.iiitb.ormtestapp.inheritance.joined.eo.Intern;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.Car;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.Ford;
import edu.iiitb.ormtestapp.inheritance.mixed.eo.PrimeMinister;
import edu.iiitb.ormtestapp.inheritance.tableperconcrete.eo.Cricketer;

public class UpdateActivity extends Activity
{

	ListView listView = null;

	private void testSimpleUpdate(ORMHelper ormHelper)
	{
		Course course = new Course();
		course.setCourseDescription("Modelling data");
		course.setCourseName("DM");
		course.setCredits(5);

		//ormHelper.persist(course);

		course.setCourseDescription("Design");
		course.setCourseName("OOAD");
		course.setCredits(4);

		ormHelper.update(course);
	}

	private void testUpdateOfInheritedObjectsWithJoinedStrategy(
			ORMHelper ormHelper)
	{
		Intern intern = new Intern();
		intern.setName("Ron Clyde");
		intern.setHourlyRate(20);
		intern.setStipend(200);
		intern.setAge(22);
		ormHelper.persist(intern);
		intern.setName("Yevgeny Kefeilnikov");
		ormHelper.update(intern);

		Car car = new Car();
		car.setColor("Red ");
		car.setHorsePower(775);
		car.setMfgYear(1975);
		ormHelper.persist(car);
		car.setColor("White ");
		ormHelper.update(car);

	}

	private void testUpdateOfInheritedObjectsWithTablePerClassStrategy(
			ORMHelper ormHelper)
	{
		Cricketer cricketer = new Cricketer();
		cricketer.setName("Saurav Ganguly");
		cricketer.setAverage(42.28f);
		cricketer.setTeam("India");
		cricketer.setAge(44);
		ormHelper.persist(cricketer);
		cricketer.setName("Dada");
		ormHelper.update(cricketer);
	}

	private void testUpdateOfInheritedObjectsWithMixedStrategy(
			ORMHelper ormHelper)
	{
		Ford ford = new Ford();
		ford.setColor("Blue");
		ford.setHorsePower(885);
		ford.setMfgYear(1985);
		ford.setModel("Fiesta");
		ormHelper.persist(ford);
		ford.setColor("White");
		ormHelper.update(ford);

		PrimeMinister pm = new PrimeMinister();
		pm.setAge(62);
		pm.setPortfolio("Prime Minister");
		pm.setSalary(15000);
		pm.setState("Gujarat ");
		ormHelper.persist(pm);
		pm.setState("Karnataka");
		ormHelper.update(pm);
	}

	private void testUpdateOfOneToOneAndOneToManyAssociation(ORMHelper ormHelper)
	{
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

	private void testUpdateOfManyToManyAssociation(ORMHelper ormHelper)
	{
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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);

		List<String> menuList = new ArrayList<String>();
		menuList.add("Update on object with no inheritance and no composition");
		menuList.add("Update on object which has inherited using a joined inheritance strategy");
		menuList.add("Update on object which has inherited using a table-per-class strategy");
		menuList.add("Update on object which has inherited using a mixed strategy");
		menuList.add("Update on object which has 1-1 and 1-N associations");
		menuList.add("Update on object which has M-N associations");

		listView = (ListView) findViewById(R.id.updateList);
		listView.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, menuList));
		final ORMHelper ormHelper = ORMHelper.getInstance(getApplicationContext());

		listView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id)
			{
				switch (position)
				{
				case 0:
					testSimpleUpdate(ormHelper);
					break;
				case 1:
					testUpdateOfInheritedObjectsWithJoinedStrategy(ormHelper);
					break;
				case 2:
					testUpdateOfInheritedObjectsWithTablePerClassStrategy(ormHelper);
					break;
				case 3:
					testUpdateOfInheritedObjectsWithMixedStrategy(ormHelper);
					break;
				case 4:
					testUpdateOfOneToOneAndOneToManyAssociation(ormHelper);
					break;
				case 5:
					testUpdateOfManyToManyAssociation(ormHelper);
					break;
				}
			}
		});
	}
}
