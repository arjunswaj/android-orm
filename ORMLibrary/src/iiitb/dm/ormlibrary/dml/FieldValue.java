package iiitb.dm.ormlibrary.dml;

import iiitb.dm.ormlibrary.constants.JavaFieldType;

import java.lang.reflect.Field;


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
  private Field field;
  private boolean id;
  private JavaFieldType javaFieldType;
  
  public JavaFieldType getJavaFieldType() {
	return javaFieldType;
}

public void setJavaFieldType(JavaFieldType javaFieldType) {
	this.javaFieldType = javaFieldType;
}

public Field getField() {
	return field;
}

public void setField(Field field) {
	this.field = field;
}

public boolean isId() {
	return id;
}

public void setId(boolean id) {
	this.id = id;
}


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
