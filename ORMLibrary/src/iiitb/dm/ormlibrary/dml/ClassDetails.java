package iiitb.dm.ormlibrary.dml;

import java.util.List;
import java.util.Map;

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
  private Map<String, Map<String, String>> annotationOptionValues;
  /**
   * Field Details List
   */
  private List<FieldTypeDetails> fieldTypeDetails;

  public ClassDetails(String className,
      Map<String, Map<String, String>> annotationOptionValues,
      List<FieldTypeDetails> fieldTypeDetails) {
    super();
    this.className = className;
    this.annotationOptionValues = annotationOptionValues;
    this.fieldTypeDetails = fieldTypeDetails;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Map<String, Map<String, String>> getAnnotationOptionValues() {
    return annotationOptionValues;
  }

  public void setAnnotationOptionValues(
      Map<String, Map<String, String>> annotationOptionValues) {
    this.annotationOptionValues = annotationOptionValues;
  }

  public List<FieldTypeDetails> getFieldTypeDetails() {
    return fieldTypeDetails;
  }

  public void setFieldTypeDetails(List<FieldTypeDetails> fieldTypeDetails) {
    this.fieldTypeDetails = fieldTypeDetails;
  }

}
