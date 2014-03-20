package iiitb.dm.ormlibrary.ddl;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Class that captures the data of every field in the Entity
 * 
 * @author arjun
 * 
 */
public class FieldTypeDetails {

  /**
   * Field Name
   */
  private String fieldName;
  /**
   * Type of the Field
   */
  private Class<?> fieldType;
  /**
   * Generic type of the Field
   */
  private Type fieldGenericType;
  /**
   * Map of Annotation Name and the key/value pair of all the options associated
   * with it
   */
  private Map<String, Map<String, Object>> annotationOptionValues;

  /**
   * Default Constructor
   */
  public FieldTypeDetails() {
    super();
  }

  public FieldTypeDetails(String fieldName, Class<?> fieldType,
      Map<String, Map<String, Object>> annotationOptionValues) {
    super();
    this.fieldName = fieldName;
    this.fieldType = fieldType;
    this.annotationOptionValues = annotationOptionValues;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Class<?> getFieldType() {
    return fieldType;
  }

  public void setFieldType(Class<?> fieldType) {
    this.fieldType = fieldType;
  }

  public Map<String, Map<String, Object>> getAnnotationOptionValues() {
    return annotationOptionValues;
  }

  public void setAnnotationOptionValues(
      Map<String, Map<String, Object>> annotationOptionValues) {
    this.annotationOptionValues = annotationOptionValues;
  }
  
  public Type getFieldGenericType()
  {
  	return fieldGenericType;
  }

  public void setFieldGenericType(Type fieldGenericType)
  {
  	this.fieldGenericType = fieldGenericType;
  }

}
