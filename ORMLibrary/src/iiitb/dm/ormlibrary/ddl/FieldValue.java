package iiitb.dm.ormlibrary.ddl;

import iiitb.dm.ormlibrary.constants.JavaFieldType;

/**
 * Class that captures the value of every field in the Entity along with its
 * type
 * 
 * @author arjun
 * 
 */
public class FieldValue {

  private String fieldName;
  private String fieldValue;
  private JavaFieldType javaFieldType;

  public FieldValue() {
    super();
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldValue() {
    return fieldValue;
  }

  public void setFieldValue(String fieldValue) {
    this.fieldValue = fieldValue;
  }

  public JavaFieldType getJavaFieldType() {
    return javaFieldType;
  }

  public void setJavaFieldType(JavaFieldType javaFieldType) {
    this.javaFieldType = javaFieldType;
  }

}
