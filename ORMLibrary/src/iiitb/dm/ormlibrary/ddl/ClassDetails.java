package iiitb.dm.ormlibrary.ddl;

import iiitb.dm.ormlibrary.utils.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * ClassDetails captured from Reflection
 * 
 * @author arjun
 * 
 */
public class ClassDetails {

  /**
   * Name of the Class
   */
  private String className;
  /**
   * Map of all the Annotation Names associated with Class (Key) and Map of all
   * the associated key/value pair (Value)
   */
  private Map<String, Map<String, Object>> annotationOptionValues;
  /**
   * Field Details List
   */
  private List<FieldTypeDetails> fieldTypeDetails;
  
  private String columnsDescription;

  /**
   * List of all the Sub Classes
   */
  private List<ClassDetails> subClassDetails;

  public ClassDetails(String className,
      Map<String, Map<String, Object>> annotationOptionValues,
      List<FieldTypeDetails> fieldTypeDetails) {
    super();
    this.className = className;
    this.annotationOptionValues = annotationOptionValues;
    this.fieldTypeDetails = fieldTypeDetails;
    subClassDetails = new LinkedList<ClassDetails>();
  }
  
  public String getColumnsDescription() {
		return columnsDescription;
	}

	public void setColumnsDescription(String columnsDescription) {
		this.columnsDescription = columnsDescription;
	}

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Map<String, Map<String, Object>> getAnnotationOptionValues() {
    return annotationOptionValues;
  }

  public void setAnnotationOptionValues(
      Map<String, Map<String, Object>> annotationOptionValues) {
    this.annotationOptionValues = annotationOptionValues;
  }

  public List<FieldTypeDetails> getFieldTypeDetails() {
    return fieldTypeDetails;
  }

  public void setFieldTypeDetails(List<FieldTypeDetails> fieldTypeDetails) {
    this.fieldTypeDetails = fieldTypeDetails;
  }

  public List<ClassDetails> getSubClassDetails() {
    return subClassDetails;
  }

  public void setSubClassDetails(List<ClassDetails> subClassDetails) {
    this.subClassDetails = subClassDetails;
  }

	public FieldTypeDetails getFieldTypeDetailsByColumnName(String columnName)
	{
		for (FieldTypeDetails fieldTypeDetails : getFieldTypeDetails())
		{
			Map<String, Object> fieldAnnotationOptionValues = fieldTypeDetails
					.getAnnotationOptionValues().get(Constants.COLUMN);
			if (fieldAnnotationOptionValues == null)
				continue;
			if (columnName == (String) fieldAnnotationOptionValues
					.get(Constants.NAME))
				return fieldTypeDetails;
		}
		return null;
	}
}
