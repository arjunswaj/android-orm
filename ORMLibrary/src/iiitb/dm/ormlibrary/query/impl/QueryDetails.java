package iiitb.dm.ormlibrary.query.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class QueryDetails {

	private List<TableJoinCondition> tablesJoinConditions = new ArrayList<TableJoinCondition>();
	// List of tables to be joined
	private Map<String, List<String>> tablesToBeJoined = new HashMap<String, List<String>>();
	// List of ColumnField, which will be used to map columns to fields of classes.
	private List<ColumnField> columnFieldList = new ArrayList<ColumnField>();
	
	public Map<String, List<String>> getTablesToBeJoined() {
		return tablesToBeJoined;
	}

	public void setTablesToBeJoined(Map<String, List<String>> tablesToBeJoined) {
		this.tablesToBeJoined = tablesToBeJoined;
	}

	public List<TableJoinCondition> getTablesJoinConditions() {
		return tablesJoinConditions;
	}

	public void setTablesJoinConditions(
			List<TableJoinCondition> tablesJoinConditions) {
		this.tablesJoinConditions = tablesJoinConditions;
	}

	public List<ColumnField> getColumnFieldList() {
		return columnFieldList;
	}

	public void setColumnFieldList(List<ColumnField> columnFieldList) {
		this.columnFieldList = columnFieldList;
	}

	
	public String getJoinString()
	{
		StringBuilder sb = new StringBuilder();
		for(TableJoinCondition tableJoinCondition: getTablesJoinConditions())
		{
			sb.append(tableJoinCondition.getJoinString());
		}
		return sb.toString();
	}
	
	public void add(QueryDetails queryDetails)
	{
		tablesToBeJoined.putAll(queryDetails.getTablesToBeJoined());
		tablesJoinConditions.addAll(queryDetails.getTablesJoinConditions());
		columnFieldList.addAll(queryDetails.getColumnFieldList());
	}
	
	public void remove(QueryDetails queryDetails)
	{
		for(Entry<String, List<String>> entry : queryDetails.getTablesToBeJoined().entrySet())
			tablesToBeJoined.remove(entry.getKey());
		tablesJoinConditions.removeAll(queryDetails.getTablesJoinConditions());
		columnFieldList.removeAll(queryDetails.getColumnFieldList());
	}
	
	public boolean joinExists(String tableName, String joinColumn, String otherTableName, String otherJoinColumn)
	{
		return getTablesJoinConditions().contains(new TableJoinCondition(tableName, joinColumn, otherTableName, otherJoinColumn))
				|| getTablesJoinConditions().contains(new TableJoinCondition(otherTableName, otherJoinColumn, tableName, joinColumn));
	}
	
	public void addTableJoinCondition(String tableName, String joinColumn, String otherTableName, String otherJoinColumn)
	{
		tablesJoinConditions.add(new TableJoinCondition(tableName, joinColumn, otherTableName, otherJoinColumn));
		
	}
	
	public class TableJoinCondition{
		private String tableName;
		private String joinColumn;
		private String otherTableName;
		private String otherJoinColumn;

		public TableJoinCondition(String tableName, String joinColumn,
				String otherTableName, String otherJoinColumn) {
			this.tableName = tableName.intern();
			this.joinColumn = joinColumn.intern();
			this.otherTableName = otherTableName.intern();
			this.otherJoinColumn = otherJoinColumn.intern();
		}

		/*
		 * Overridden to compare individual elements of the object.
		 * Used by method joinExists.
		 */
		@Override
		public boolean equals(Object obj)
		{
			TableJoinCondition tableJoinConditionObj = (TableJoinCondition)obj;
			return tableName.equals(tableJoinConditionObj.getTableName())
					&& joinColumn.equals(tableJoinConditionObj.getJoinColumn())
					&& otherTableName.equals(tableJoinConditionObj.getOtherTableName())
					&& otherJoinColumn.equals(tableJoinConditionObj.getOtherJoinColumn());
		}

		
		@Override
		public int hashCode(){
			int hash = 0;
			if(tableName != null)
				hash += tableName.hashCode();
			if(joinColumn != null)
				hash += joinColumn.hashCode();
			if(otherTableName != null)
				hash += otherTableName.hashCode();
			if(otherJoinColumn != null)
				hash += otherJoinColumn.hashCode();
			return hash;
		}
		
		public String getTableName() {
			return tableName;
		}
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		public String getJoinColumn() {
			return joinColumn;
		}
		public void setJoinColumn(String joinColumn) {
			this.joinColumn = joinColumn;
		}
		public String getOtherTableName() {
			return otherTableName;
		}
		public void setOtherTableName(String otherTableName) {
			this.otherTableName = otherTableName;
		}
		public String getOtherJoinColumn() {
			return otherJoinColumn;
		}
		public void setOtherJoinColumn(String otherJoinColumn) {
			this.otherJoinColumn = otherJoinColumn;
		}
		public String getJoinString(){
			return " JOIN " + tableName + " ON " + tableName + "." + joinColumn + "=" + otherTableName + "." + otherJoinColumn;
		}
	}


}
