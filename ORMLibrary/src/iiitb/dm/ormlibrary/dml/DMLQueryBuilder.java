package iiitb.dm.ormlibrary.dml;

import java.util.List;

/**
 * DML Query Builder Interface
 * @author arjun
 *
 */
public interface DMLQueryBuilder {

  /**
   * Generate the Create Table Query from the table name and the field types
   * 
   * @param tableName
   *          table name
   * @param fieldTypes
   *          field types
   * @return SQL Query
   */
  public String generateCreateTableQuery(String tableName,
      List<FieldType> fieldTypes);
}
