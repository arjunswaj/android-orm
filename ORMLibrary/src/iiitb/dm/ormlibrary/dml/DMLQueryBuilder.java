package iiitb.dm.ormlibrary.dml;

import iiitb.dm.ormlibrary.ddl.FieldValue;

import java.util.List;

/**
 * DML Query Builder Interface
 * 
 * @author arjun
 * 
 */
public interface DMLQueryBuilder {

	  /**
	   * Generate the Insert Query for a given tableName with list of fieldValues
	   * 
	   * @param tableName
	   *          table name
	   * @param fieldValues
	   *          field values
	   * @return Insert SQL query
	   */
	  String generateInsertQuery(String tableName, List<FieldValue> fieldValues);
}
