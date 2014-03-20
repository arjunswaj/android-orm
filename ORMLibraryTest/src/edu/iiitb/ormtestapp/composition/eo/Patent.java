package edu.iiitb.ormtestapp.composition.eo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "PATENT")
public class Patent
{
	@Id
	@Column(name = "_id")
	private int id;
	
	@Column(name = "TITLE")
	private String title;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}


	
}
