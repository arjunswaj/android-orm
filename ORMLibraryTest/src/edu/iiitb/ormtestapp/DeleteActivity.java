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

public class DeleteActivity extends Activity
{

	ListView listView = null;

	private void testSimpleDelete(ORMHelper ormHelper)
	{
		Course course = new Course();
		course.setCourseDescription("Modelling data");
		course.setCourseName("DM");
		course.setCredits(5);

		ormHelper.persist(course);
		ormHelper.delete(course);
		
	}

	private void testDeleteOfInheritedObjectsWithJoinedStrategy(
			ORMHelper ormHelper)
	{
		Intern intern = new Intern();
		intern.setName("Ron Clyde");
		intern.setHourlyRate(20);
		intern.setStipend(200);
		intern.setAge(22);
		ormHelper.persist(intern);
		ormHelper.delete(intern);

		Car car = new Car();
		car.setColor("Red ");
		car.setHorsePower(775);
		car.setMfgYear(1975);
		ormHelper.persist(car);
		ormHelper.delete(car);

	}

	private void testDeleteOfInheritedObjectsWithTablePerClassStrategy(
			ORMHelper ormHelper)
	{
		Cricketer cricketer = new Cricketer();
		cricketer.setName("Saurav Ganguly");
		cricketer.setAverage(42.28f);
		cricketer.setTeam("India");
		cricketer.setAge(44);
		ormHelper.persist(cricketer);
		ormHelper.delete(cricketer);
	}

	private void testDeleteOfInheritedObjectsWithMixedStrategy(
			ORMHelper ormHelper)
	{
		Ford ford = new Ford();
		ford.setColor("Blue");
		ford.setHorsePower(885);
		ford.setMfgYear(1985);
		ford.setModel("Fiesta");
		ormHelper.persist(ford);
		ormHelper.delete(ford);

		PrimeMinister pm = new PrimeMinister();
		pm.setAge(62);
		pm.setPortfolio("Prime Minister");
		pm.setSalary(15000);
		pm.setState("Gujarat ");
		ormHelper.persist(pm);
		ormHelper.delete(pm);
	}

	private void testDeleteOfOneToOneAndOneToManyAssociation(ORMHelper ormHelper)
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

		// Delete object
		ormHelper.delete(country);
		
	}

	private void testDeleteOfManyToManyAssociation(ORMHelper ormHelper)
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
		
		ormHelper.delete(leslie);
		ormHelper.delete(donald);		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delete);

		List<String> menuList = new ArrayList<String>();
		menuList.add("Simple Delete");
		menuList.add("Delete object with inheritance using Joined Strategy");
		menuList.add("Delete object with inheritance using Table per Class Strategy");
		menuList.add("Delete object with inheritance using Mixed Strategy");
		menuList.add("Delete object with 1-1 and 1-N associations");
		menuList.add("Delete object with M-N associations");

		listView = (ListView) findViewById(R.id.deleteList);
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
					testSimpleDelete(ormHelper);
					break;
				case 1:
					testDeleteOfInheritedObjectsWithJoinedStrategy(ormHelper);
					break;
				case 2:
					testDeleteOfInheritedObjectsWithTablePerClassStrategy(ormHelper);
					break;
				case 3:
					testDeleteOfInheritedObjectsWithMixedStrategy(ormHelper);
					break;
				case 4:
					testDeleteOfOneToOneAndOneToManyAssociation(ormHelper);
					break;
				case 5:
					testDeleteOfManyToManyAssociation(ormHelper);
					break;
				}
			}
		});
	}
}
