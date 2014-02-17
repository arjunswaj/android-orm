package iiitb.dm.ormlibrary.dml;

import iiitb.dm.ormlibrary.constants.JavaFieldType;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that captures the data of every field in the Entity
 * @author arjun
 *
 */
public class FieldType {

  /**
   * Field Name
   */
  private String fieldName;
  /**
   * Type of the Field
   */
  private JavaFieldType fieldType;
  /**
   * Map of Options and Values
   */
  private Map<String, String> optionValues = new HashMap<String, String>();

  /**
   * Default Constructor
   */
  public FieldType() {
    super();
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public JavaFieldType getFieldType() {
    return fieldType;
  }

  public void setFieldType(JavaFieldType fieldType) {
    this.fieldType = fieldType;
  }

  public Map<String, String> getOptionValues() {
    return optionValues;
  }

  public void setOptionValues(Map<String, String> optionValues) {
    this.optionValues = optionValues;
  }

}
