package iiitb.dm.ormlibrary.ddl;

import iiitb.dm.ormlibrary.ddl.impl.MappingException;

import java.util.Collection;
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
	 * @throws MappingException 
	   */
	public Collection<String> generateCreateTableStmts(ClassDetails classDetails) throws MappingException;
}
