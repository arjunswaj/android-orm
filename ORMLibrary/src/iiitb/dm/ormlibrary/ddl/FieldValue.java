package iiitb.dm.ormlibrary.ddl;


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
  private Class<?> fieldType;

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

  public Class<?> getFieldType() {
    return fieldType;
  }

  public void setFieldType(Class<?> fieldType) {
    this.fieldType = fieldType;
  }

}
