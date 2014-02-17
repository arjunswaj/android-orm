package iiitb.dm.ormlibrary.ddl;

import java.util.List;

/**
 * DDLQueryBuilder Interface
 * 
 * @author arjun
 * 
 */
public interface DDLQueryBuilder {

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
