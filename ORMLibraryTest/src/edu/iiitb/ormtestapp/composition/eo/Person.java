package edu.iiitb.ormtestapp.composition.eo;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity(name = "PERSON")
public class Person
{

	@Id
	@Column(name = "_id")
	private long id;

	@Column(name = "NAME")
	private String name;

	@ManyToMany
	private Collection<Patent> patents;
	
	@ManyToMany
	private Collection<Phone> phones;

	public Person(String name)
	{
		super();
		this.name = name;
	}

	public Person()
	{
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Collection<Patent> getPatents()
	{
		return patents;
	}

	public void setPatents(Collection<Patent> patents)
	{
		this.patents = patents;
	}

	public Collection<Phone> getPhones()
	{
		return phones;
	}

	public void setPhones(Collection<Phone> phones)
	{
		this.phones = phones;
	}

}
