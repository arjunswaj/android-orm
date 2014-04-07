package iiitb.dm.ormlibrary.query.impl;


	public class ColumnField{
		String columnName;
		String className;
		String fieldName;


		public ColumnField(String columnName, String className,
				String fieldName) {
			this.columnName = columnName;
			this.className = className;
			this.fieldName = fieldName;
		}


		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public String getFieldName() {
			return fieldName;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

	}
