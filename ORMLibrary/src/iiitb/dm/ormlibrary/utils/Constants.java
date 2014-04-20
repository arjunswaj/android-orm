package iiitb.dm.ormlibrary.utils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import android.content.Entity;

public class Constants
{
	// Annotation names
	public static final String INHERITANCE = Inheritance.class.getSimpleName();
	public static final String ENTITY = Entity.class.getSimpleName();
	public static final String COLUMN = Column.class.getSimpleName();
	public static final String ONE_TO_ONE = OneToOne.class.getSimpleName();
	public static final String ONE_TO_MANY = OneToMany.class.getSimpleName();
	public static final String JOIN_COLUMN = JoinColumn.class.getSimpleName();
	public static final String DISCRIMINATOR_COLUMN = DiscriminatorColumn.class
			.getSimpleName();
	public static final String DISCRIMINATOR_VALUE = DiscriminatorValue.class
			.getSimpleName();
	public static final String MANY_TO_ONE = ManyToOne.class.getSimpleName();
	public static final String MANY_TO_MANY = ManyToMany.class.getSimpleName();
	public static final String JOIN_TABLE = JoinTable.class.getSimpleName();
	public static final String ID = Id.class.getSimpleName();

	// Annotation option keys
	public static final String STRATEGY = "strategy";
	public static final String NAME = "name";
	public static final String MAPPED_BY = "mappedBy";
	public static final String JOIN_COLUMNS = "joinColumns";
	public static final String INVERSE_JOIN_COLUMNS = "inverseJoinColumns";
	public static final String VALUE = "value";
	public static final String REFERENCED_COLUMN_NAME = "referencedColumnName";

	// Misc
	public static final String ID_VALUE = "_id";
	public static final String ID_VALUE_CAPS = "ID";
	public static final String EMPTY = "";
	public static final String ENTITY_OBJECT_FILE = "entity_object_file";
  public static final String XML = "xml";
  public static final String DATABASE_NAME = "database_name";
  public static final String DATABASE_VERSION = "database_version";

}
