package iiitb.dm.ormlibrary.ddl;

import iiitb.dm.ormlibrary.dml.ClassDetails;

import java.util.List;

/**
 * DDLQueryBuilder Interface
 * 
 * @author arjun
 * 
 */
public interface DDLQueryBuilder {

	  /**
	   * Generate the Create Table Query from the Class Details
	   * 
	   * @param classDetails
	   *          class Details
	   * @return SQL Query
	   */
	  public String generateCreateTableQuery(ClassDetails classDetails);
}
