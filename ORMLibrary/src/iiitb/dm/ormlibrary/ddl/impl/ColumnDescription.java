package iiitb.dm.ormlibrary.ddl.impl;

import java.util.ArrayList;


public class ColumnDescription {

	String columnName;
	String columnType;
	ArrayList<String> columnConstraints;
	
	
	public ColumnDescription(String columnName, String columnType, ArrayList<String> columnConstraints)
	{
		this.columnName = columnName;
		this.columnType = columnType;
		this.columnConstraints = columnConstraints;
	}
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public ArrayList<String> getColumnConstraints() {
		return columnConstraints;
	}
	public void setColumnConstraints(ArrayList<String> columnConstraints) {
		this.columnConstraints = columnConstraints;
	}
	
	
}
