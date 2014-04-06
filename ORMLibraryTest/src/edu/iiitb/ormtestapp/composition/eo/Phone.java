package edu.iiitb.ormtestapp.composition.eo;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity(name = "PHONE")
public class Phone
{
	@Id
	@Column(name = "_id")
	private long id;
	
	@Column(name = "NUMBER")
	private int number;
	
/*	@ManyToMany(mappedBy = "phones")
	private Collection<Person> persons;*/
	
	

	
	

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	/*public Collection<Person> getPersons()
	{
		return persons;
	}

	public void setPersons(Collection<Person> persons)
	{
		this.persons = persons;
	}*/

}
