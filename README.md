# Mango ORM for Android

An **O**bject **R**elational **M**apper for *Android platform*.

### ORMify your app in 4 simple steps.
1. Annotate your Entity `.class` files. [^1]

        @Entity(name = "STUDENT")
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

[^1]: ***Note**: Add getters and setters, default constructor and id of type `long` in all your `Entity` classes.*

----------


###CRUD Operations - A detailed overview


This section provides a detailed overview on performing CRUD Operations.

###Create
This section will give example about Create.

* ####Inheritance
	* ####Table Per Class
		In this section Inheritance by Table per class strategy is discussed.
		
		1. Provide the Inheritance Strategy in the SuperClass Files.

        		@Entity(name = "SPORTSMAN")
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

        		@Entity(name = "FOOTBALLER")
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

        		@Entity(name = "EMPLOYEE")
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

        		@Entity(name = "PART_TIME_EMPLOYEE")
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
		This section will give example about One To One Mapping.
	* ####One To Many
		This section will give example about One To Many Mapping.
	* ####Many To One
		This section will give example about Many To One Mapping.
	* ####Many To Many
		This section will give example about Many To Many Mapping.	

###Retrieve
This section will give example about Retrieve.

* ####Inheritance
	This section will give example about Criteria Class APIs.

###Update
This section will give example about Update.

###Delete
This section will give example about Delete.

----------
### Authors

[Abhijith Madhav](mailto:abhijith.madhav@iiitb.org), [Arjun S Bharadwaj](mailto:arjun.s.waj@iiitb.org), [Kumudini Kakwani](mailto:kumudini.kakwani@iiitb.org).