# Mango ORM for Android

An **O**bject **R**elational **M**apper for *Android platform*.

### ORMify your app in 4 simple steps.
1. Annotate your Entity `.class` files. [^1]

        @Entity(name = "STUDENTS")
        public class Student {

            @Id
            @Column(name = "_id")
            private long id;

            @Column(name = "AGE")
            private int age;

            @Column(name = "NAME")
            private String name;

            @Column(name = "CGPA")
            private float cgpa;

            @Column(name = "COLLEGE")
            private String college;
        }
        
2. List all the entity files in an `XML` file, say, `entity_objects.xml`.

   		<?xml version="1.0" encoding="utf-8"?>
	    <EntityObjects>
   			<EntityObject>edu.iiitb.foo.eo.Student</EntityObject>
       		<EntityObject>edu.iiitb.bar.eo.Course</EntityObject>
	    </EntityObjects>
	
3. Update the `AndroidManifest.xml` with values of `entity_object_file`, `database_name` and `database_version` in the meta-data.	
		
		<application>
			...
			<meta-data
				android:name="entity_object_file"
				android:value="entity_objects" >
			</meta-data>
			<meta-data
				android:name="database_name"
				android:value="testDB.sqlite" >
			</meta-data>
			<meta-data
				android:name="database_version"
				android:value="1" >
			</meta-data>
			...
		</application>    
		
		
4. Get the instance of `ORMHelper` and perform the **CRUD** Operations as required.

		final ORMHelper ormHelper = ORMHelper.getInstance(getApplicationContext());
		Student student = new Student(21, "Bruce Wayne ", 
			"Gotham City", "Yale Law School");
		ormHelper.persist(student);

That's it! Your droid is ORMified.

----------


###CRUD Operations - A detailed overview


This section provides a detailed overview on performing CRUD Operations.

###Create
This section will give example about Create.

* ####Inheritance
	* ####Table Per Class
	In this section Inheritance by Table Per Class strategy is discussed. [^2]
	
	1. Provide the Inheritance Strategy in the SuperClass Files.

        		@Entity(name = "SPORTSMEN")
        		@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
		        public class Sportsman {

	        	    @Id
    	        	@Column(name = "_id")
	        	    private long id;

    		        @Column(name = "AGE")
            		private int age;

		            @Column(name = "NAME")
        		    private String name;

		        }

	2. Extend the SuperClass in the SubClass.

        		@Entity(name = "FOOTBALLERS")
				public class Footballer extends Sportsman {

					@Column(name = "GOALS")
					private int goals;

					@Column(name = "TEAM")
					private String team;

		        }

	3. Persist the instance of SubClass or SuperClass.
				
				Sportsman sportsman = new Sportsman(44, "Viswanathan Anand");
        		ormHelper.persist(sportsman);	  
        		
        		Sportsman footballer = new Footballer(39, "David Beckham", 17, "England");
				ormHelper.persist(footballer);
				      
	
				      				   
	* ####Joined
	In this section Inheritance by Joined strategy is discussed.
	
	1. Provide the Inheritance Strategy and the Discriminator Column in the SuperClass Files.

        		@Entity(name = "EMPLOYEES")
				@Inheritance(strategy = InheritanceType.JOINED)
				@DiscriminatorColumn(name = "EMPLOYEE_TYPE")
				public abstract class Employee {

	        	    @Id
    	        	@Column(name = "_id")
	        	    private long id;

    		        @Column(name = "AGE")
            		private int age;

		            @Column(name = "NAME")
        		    private String name;

		        }

	2. Extend the SuperClass in the SubClass Files and provide the Discriminator Value.

        		@Entity(name = "PART_TIME_EMPLOYEES")
				@DiscriminatorValue(value = "PART_TIME")
				public class PartTimeEmployee extends Employee {
					
					@Column(name = "HOURLY_RATE")
					private float hourlyRate;

		        }

	3. Persist the instance of SubClass.
				
				Employee partTimeEmployee = new PartTimeEmployee(23, "Harry Potter", 48);
        		ormHelper.persist(partTimeEmployee);	  
* ####Composition
	* ####One To One
	In this section One To One Composition is discussed.
	1. Provide the OneToOne mapping in the Entity Files.
		
        		@Entity(name = "COUNTRIES")
				public class Country {
				
					@Id
					@Column(name = "_id")
					private long id;
				
					@Column(name = "NAME")
					private String name;
				
					@OneToOne(cascade={CascadeType.DELETE})
					@JoinColumn(name = "CAPITAL_ID")
					private Capital capital;

		        }
		        
	2. Provide the mapped Entity definition.

        		@Entity(name = "CAPITALS")
				public class Capital {

					@Id
					@Column(name = "_id")
					private long id;
				
					@Column(name = "NAME")
					private String name;

		        }

	3. Persist the instance of Composing Entity.
				
				Capital capital = new Capital("New Delhi");
				Country country = new Country("India", capital);
        		ormHelper.persist(country);	       					      
	* ####One To Many
	In this section One To Many Composition is discussed. [^3] 

	1. Provide the OneToMany mapping in the Entity Files.
						
        		@Entity(name = "COUNTRIES")
				public class Country {
				
					@Id
					@Column(name = "_id")
					private long id;
				
					@Column(name = "NAME")
					private String name;
				
					@OneToMany(cascade={CascadeType.DELETE})
					@JoinColumn(name = "COUNTRY_ID")
					private Collection<State> states;

		        }
		        
	2. Provide the mapped Entity definition.

        		@Entity(name = "STATES")
				public class State {

					@Id
					@Column(name = "_id")
					private long id;
				
					@Column(name = "NAME")
					private String name;

		        }

	3. Persist the instance of Composing Entity.
				
				State karnataka = new Capital("Karnataka");
				State gujrat = new Capital("Gujrat");
				State rajasthan = new Capital("Rajasthan");
				
				Collection<State> states = new ArrayList<States>();
				states.add(karnataka);
				states.add(gujrat);			
				states.add(rajasthan);
					
				Country country = new Country("India", states);
        		ormHelper.persist(country);	     
        		
	* ####Many To Many
	In this section Many To Many Composition is discussed.
	
	1. Provide the ManyToMany mapping in the Entity Files.
						
        		@Entity(name = "PERSONS")
				public class Person {
				
					@Id
					@Column(name = "_id")
					private long id;
				
					@Column(name = "NAME")
					private String name;
				
					@ManyToMany
					private Collection<Patent> patents;

		        }
		        
	2. Provide the mapped Entity definition.

        		@Entity(name = "PATENTS")
				public class Patent {

					@Id
					@Column(name = "_id")
					private long id;
				
					@Column(name = "TITLE")
					private String title;

		        }

	3. Persist the instance of Composing Entity.
				
				Person p1 = new Person("Leslie Lamport");
				Person p2 = new Person("James M. Reuter");				
				Patent patent1 = new Patent("Shared Storage");
				Patent patent2 = new Patent("Byzantine Consensus");
								
				Collection<Patent> patentList1 = new ArrayList<Patent>();
				patentList1.add(patent1);
				patentList1.add(patent2);			
				p1.setPatents(patentList1);
					
				Collection<Patent> patentList2 = new ArrayList<Patent>();
				patentList1.add(patent1);
		
				p2.setPatents(patentList2);
				
        		ormHelper.persist(p1);
        		ormHelper.persist(p2);        		
        			

###Retrieve
In this section Retrieve APIs are discussed.This section will give example about Create.

* ####Query All
	All the tuples can be queried by below API.
		
		Criteria criteria = ormHelper.createCriteria(Student.class);
		

* ####Find by Id
	A specific tuple can be queried by below API.
		
		Student student = (Student) ormHelper.find(Student.class, 26L);		

* ####Restrictions
	Restrictions on the tuples can be added using the below APIs.
		
		Criteria criteria = ormHelper.createCriteria(Student.class)
		.add(Restrictions.like("name", "Name%"))
		.add(Restrictions.between("cgpa", 2.4, 4.0)
		.add(
			Restrictions.or(
				Restrictions.eq("age", 23), 
				Restrictions.like("college", "IIIT-B")
			)
		)
		.add(Restrictions.eq("address", "Address 5"));

###Update
In this section Update of the Entity is discussed.
Do a search of Entities using the Criteria Class APIs. Modify the returned entities and invoke the update method on `ORMHelper`.

	ormHelper.update(entity);

###Delete
In this section Update of the Entity is discussed.
Do a search of Entities using the Criteria Class APIs and invoke the delete method on `ORMHelper`.

	ormHelper.delete(entity);

----------
### Authors

[Abhijith Madhav](mailto:abhijith.madhav@iiitb.org), [Arjun S Bharadwaj](mailto:arjun.s.waj@iiitb.org), [Kumudini Kakwani](mailto:kumudini.kakwani@iiitb.org).
[^1]: ***Note**: Add getters and setters, default constructor and id of type `long` in all your `Entity` classes.*

[^2]: ***Note**: Composition of any form cannot be used with Table Per Class strategy. Use Joined strategy instead.*

[^3]: ***Note**: One to Many Mapping requires the use of Collection Interface*