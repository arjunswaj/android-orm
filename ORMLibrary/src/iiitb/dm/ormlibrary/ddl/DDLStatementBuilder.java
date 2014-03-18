package iiitb.dm.ormlibrary.ddl;

import java.util.Map;

import javax.persistence.InheritanceType;

/**
 * DDLQueryBuilder Interface
 * 
 * @author arjun
 * 
 */
public interface DDLStatementBuilder {

	  /**
	   * Generate the Create Table Query from the Class Details
	   * 
	   * @param classDetails
	   *          class Details
	   * @return SQL Query
	   */
	public String generateCreateTableQuery(ClassDetails classDetails, ClassDetails superClassDetails);
}
