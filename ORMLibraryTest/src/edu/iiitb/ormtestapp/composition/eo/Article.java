package edu.iiitb.ormtestapp.composition.eo;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity(name = "ARTICLE")
public class Article
{
	@Id
	@Column(name = "_id")
	private long id;
	
	@Column(name = "NAME")
	private String name;
	
	@ManyToMany(mappedBy = "articles")
	private Collection<Person> persons;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Collection<Person> getPersons()
	{
		return persons;
	}

	public void setPersons(Collection<Person> persons)
	{
		this.persons = persons;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
