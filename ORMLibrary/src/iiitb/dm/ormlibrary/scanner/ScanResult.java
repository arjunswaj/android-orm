package iiitb.dm.ormlibrary.scanner;

import iiitb.dm.ormlibrary.dml.FieldValue;

import java.util.List;

public class ScanResult {
	
	String tableName;
	List<FieldValue> fieldValues;
	
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<FieldValue> getFieldValues() {
		return fieldValues;
	}
	public void setFieldValues(List<FieldValue> fieldValues) {
		this.fieldValues = fieldValues;
	}
	
	

}
