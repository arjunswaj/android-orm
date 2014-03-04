package iiitb.dm.ormlibrary.ddl.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.utils.SQLColTypeEnumMap;
import android.util.Log;

public class DDLStatementBuilderImpl implements DDLStatementBuilder
{

	private final String DDL_TAG = this.getClass().getName();

	@Override
	public String generateCreateTableQuery(ClassDetails classDetails)
	{
		// TODO: Error Checking
		String tableName = (String) classDetails.getAnnotationOptionValues()
				.get("Entity").get("name");

		StringBuilder columnsDescription = new StringBuilder();
		for (FieldTypeDetails fieldTypeDetail : classDetails
				.getFieldTypeDetails())
		{
			String columnName = fieldTypeDetail.getAnnotationOptionValues()
					.get("Column").get("name");

			String columnType = SQLColTypeEnumMap.get(
					fieldTypeDetail.getFieldType().getSimpleName()).toString();
			columnsDescription.append(columnName + " " + columnType + " ");

			if (fieldTypeDetail.getAnnotationOptionValues().get("Id") == null)
				columnsDescription.append(", ");
			else
				columnsDescription.append("primary key autoincrement, ");
		}
		columnsDescription.replace(columnsDescription.length() - 2,
				columnsDescription.length(), "");

		String createStmt = "create table " + tableName + "( "
				+ columnsDescription + " )";
		Log.d(DDL_TAG, createStmt);
		return createStmt;
	}
}
