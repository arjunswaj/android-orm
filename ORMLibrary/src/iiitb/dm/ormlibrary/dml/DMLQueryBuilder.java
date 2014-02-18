package iiitb.dm.ormlibrary.dml;

/**
 * DML Query Builder Interface
 * 
 * @author arjun
 * 
 */
public interface DMLQueryBuilder {

  /**
   * Generate the Create Table Query from the Class Details
   * 
   * @param classDetails
   *          class Details
   * @return SQL Query
   */
  public String generateCreateTableQuery(ClassDetails classDetails);
}
